/*
 * Copyright 2019, 2020 Mark Scott
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codebrewer.dump1090processor.basestation.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.codebrewer.dump1090processor.basestation.domain.DomainUtils;
import org.codebrewer.dump1090processor.basestation.domain.MessageType;
import org.codebrewer.dump1090processor.basestation.domain.StatusMessageType;
import org.codebrewer.dump1090processor.basestation.domain.TransmissionType;
import org.codebrewer.dump1090processor.basestation.entity.BaseStationMessage;
import org.codebrewer.dump1090processor.basestation.entity.IdMessage;
import org.codebrewer.dump1090processor.basestation.entity.NewAircraftMessage;
import org.codebrewer.dump1090processor.basestation.entity.StatusMessage;
import org.codebrewer.dump1090processor.basestation.entity.TransmissionMessage;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * A service that can parse messages received from a BaseStation message feed.
 */
@Service
public class MessageParsingService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageParsingService.class);
  private static final Geographic2DCoordinateReferenceSystem COORDINATE_REFERENCE_SYSTEM =
      CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  private static Instant parseTimestamp(String dateToken, String timeToken) {
    if (StringUtils.isEmpty(dateToken) || StringUtils.isEmpty(timeToken)) {
      throw new IllegalArgumentException(
          String.format("Date (%s) and time (%s) must be provided", dateToken, timeToken));
    }

    // Values such as '2017-12-23T16:01:15.4294967295Z' have been seen, which causes a
    // DateTimeParseException, so truncate after 3 decimals
    //
    final int dotPosition = timeToken.indexOf('.');

    if (timeToken.length() > dotPosition + 4) {
      timeToken = timeToken.substring(0, 12);
    }

    final LocalDate localDate = LocalDate.parse(dateToken, DATE_FORMATTER);
    final LocalTime localTime = LocalTime.parse(timeToken);

    return LocalDateTime.of(localDate, localTime).atZone(ZoneId.systemDefault()).toInstant();
  }

  private static Short tokenAsShort(String token) {
    try {
      return Short.parseShort(token);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Point<G2D> tokensAsPosition(String lon, String lat) {
    try {
      return new Point<>(
          new G2D(Double.parseDouble(lon), Double.parseDouble(lat)),
          COORDINATE_REFERENCE_SYSTEM);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Float tokenAsFloat(String token) {
    try {
      return Float.parseFloat(token);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Boolean tokenAsBoolean(String token) {
    try {
      return Integer.parseInt(token) != 0;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public MessageParsingService() {
  }

  /**
   * Creates a {@code BaseStationMessage} from its comma-separated value text representation as
   * received on the incoming message feed.
   *
   * @param csvMessageText the comma-separated value text representation of a message, not null
   *
   * @return a {@code BaseStationMessage} created by parsing the CSV message text
   *
   * @throws IllegalArgumentException if parsing fails
   */
  public BaseStationMessage parseCsvMessageText(String csvMessageText) {
    final String[] tokens =
        StringUtils.commaDelimitedListToStringArray(StringUtils.trimWhitespace(csvMessageText));

    if (tokens.length == 0) {
      throw new IllegalArgumentException("Message token array has zero length");
    }

    final MessageType messageType = MessageType.valueOf(tokens[0]);

    if (!DomainUtils.isExpectedMessageType(messageType)) {
      throw new IllegalArgumentException(
          String.format("Unexpected message type: '%s'", messageType));
    }

    final int requiredTokenCount = messageType.getMessageTokenCount();

    if (tokens.length < requiredTokenCount) {
      throw new IllegalArgumentException(
          String.format("Expected %d tokens but found %d", requiredTokenCount, tokens.length));
    }

    final String icaoAddress = tokens[4];
    final Instant creationTimestamp = parseTimestamp(tokens[6], tokens[7]);

    switch (messageType) {
      case AIR:
        final NewAircraftMessage.Builder newAircraftMessageBuilder =
            new NewAircraftMessage.Builder(icaoAddress, creationTimestamp);

        return newAircraftMessageBuilder.build();
      case ID:
        final IdMessage.Builder idMessageBuilder =
            new IdMessage.Builder(icaoAddress, creationTimestamp);

        return idMessageBuilder.callSign(DomainUtils.getValidatedCallSign(tokens[10])).build();
      case MSG:
        final TransmissionType transmissionType =
            TransmissionType.getByRawValue(tokenAsShort(tokens[1]));

        if (transmissionType == null) {
          LOGGER.error("Unable to parse transmission type: '{}'", tokens[1]);

          return null;
        }

        final TransmissionMessage.Builder transmissionMessageBuilder =
            new TransmissionMessage.Builder(icaoAddress, creationTimestamp)
                .transmissionType(transmissionType);

        switch (transmissionType) {
          case IDENTIFICATION_AND_CATEGORY:
            transmissionMessageBuilder.callSign(DomainUtils.getValidatedCallSign(tokens[10]));
            break;
          case SURFACE_POSITION:
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .groundSpeed(tokenAsFloat(tokens[12]))
                .track(tokenAsFloat(tokens[13]))
                .position(tokensAsPosition(tokens[15], tokens[14]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case AIRBORNE_POSITION:
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .position(tokensAsPosition(tokens[15], tokens[14]))
                .alert(tokenAsBoolean(tokens[18]))
                .emergency(tokenAsBoolean(tokens[19]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case AIRBORNE_VELOCITY:
            transmissionMessageBuilder
                .groundSpeed(tokenAsFloat(tokens[12]))
                .track(tokenAsFloat(tokens[13]))
                .verticalRate(tokenAsShort(tokens[16]));
            break;
          case SURVEILLANCE_ALTITUDE:
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .alert(tokenAsBoolean(tokens[18]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case SURVEILLANCE_ID:
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .squawk(tokenAsShort(tokens[17]))
                .alert(tokenAsBoolean(tokens[18]))
                .emergency(tokenAsBoolean(tokens[19]))
                .identActive(tokenAsBoolean(tokens[20]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case AIR_TO_AIR:
            transmissionMessageBuilder
                .altitude(tokenAsFloat(tokens[11]))
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          case ALL_CALL_REPLY:
            transmissionMessageBuilder
                .onGround(tokenAsBoolean(tokens[21]));
            break;
          default:
            throw new IllegalArgumentException(
                String.format(
                    "Unexpected transmission message type received: '%s'", transmissionType));
        }

        return transmissionMessageBuilder.build();
      case STA:
        final StatusMessageType statusMessageType = StatusMessageType.valueOf(tokens[10]);

        if (!DomainUtils.isExpectedStatusMessageType(statusMessageType)) {
          throw new IllegalArgumentException(
              String.format("Unexpected status message type: '%s'", statusMessageType));
        }

        final StatusMessage.Builder statusMessageBuilder =
            new StatusMessage.Builder(icaoAddress, creationTimestamp)
                .statusMessageType(statusMessageType);

        return statusMessageBuilder.build();
      default:
        throw new IllegalArgumentException(
            String.format(
                "Unexpected message type received: '%s'", messageType));
    }
  }
}
