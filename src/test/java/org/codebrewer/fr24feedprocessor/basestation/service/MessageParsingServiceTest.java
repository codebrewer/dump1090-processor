/*
 * Copyright 2019 Mark Scott
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

package org.codebrewer.fr24feedprocessor.basestation.service;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.codebrewer.fr24feedprocessor.Assertions;
import org.codebrewer.fr24feedprocessor.basestation.domain.MessageType;
import org.codebrewer.fr24feedprocessor.basestation.domain.StatusMessageType;
import org.codebrewer.fr24feedprocessor.basestation.domain.TransmissionType;
import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.CallSignMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.IdMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.NewAircraftMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.StatusMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.TransmissionMessage;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CrsRegistry;
import org.geolatte.geom.crs.Geographic2DCoordinateReferenceSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MessageParsingServiceTest {
  private static final Geographic2DCoordinateReferenceSystem COORDINATE_REFERENCE_SYSTEM =
      CrsRegistry.getGeographicCoordinateReferenceSystemForEPSG(4326);

  private static final Instant DUMMY_MESSAGE_TIMESTAMP_INSTANT = Instant.now();

  private static String DUMMY_MESSAGE_TIMESTAMP_STRING;

  private static String getCsvMessageWithDummyTimestamp(String csvMessagePattern) {
    return String.format(csvMessagePattern, DUMMY_MESSAGE_TIMESTAMP_STRING, ",");
  }

  @BeforeAll
  static void setUp() {
    final DateTimeFormatter dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd,HH:mm:ss.SSS");

    final LocalDateTime dummyMessageTimestamp =
        LocalDateTime.ofInstant(DUMMY_MESSAGE_TIMESTAMP_INSTANT, ZoneId.systemDefault());

    DUMMY_MESSAGE_TIMESTAMP_STRING = dateTimeFormatter.format(dummyMessageTimestamp);
  }

  private final MessageParsingService messageParsingService = new MessageParsingService();

  @Test
  void shouldThrowIllegalArgumentExceptionIfCsvMessageTextIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(null))
        .withMessage("Message token array has zero length");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfMessageTypeIsUnexpected() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(MessageType.CLK.name()))
        .withMessage("Unexpected message type: 'CLK'");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfStatusMessageTypeIsUnexpected() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("STA,,333,510,4CA7B9,610,%s,,,AD");

    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(csvMessage))
        .withMessage("Unexpected status message type: 'AD'");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfCsvMessageTextHasTooFewTokens() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(MessageType.STA.name()))
        .withMessage(
            String
                .format("Expected %d tokens but found 1", MessageType.STA.getMessageTokenCount()));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfCsvMessageTextHasMissingDateToken() {
    final String csvMessage = "AIR,,333,297,400981,397,,22:27:09.480,2019/05/11,22:27:09.480";

    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(csvMessage))
        .withMessage("Date () and time (22:27:09.480) must be provided");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfCsvMessageTextHasMissingTimeToken() {
    final String csvMessage = "AIR,,333,297,400981,397,2019/05/11,,2019/05/11,22:27:09.480";

    assertThatIllegalArgumentException()
        .isThrownBy(() -> messageParsingService.parseCsvMessageText(csvMessage))
        .withMessage("Date (2019/05/11) and time () must be provided");
  }

  @Test
  void shouldReturnNewAircraftMessageMessageFromNewAircraftCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("AIR,,333,380,4075FD,480,%s,%s");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage)
              .isExactlyInstanceOf(NewAircraftMessage.class);

    final NewAircraftMessage newAircraftMessage = (NewAircraftMessage) baseStationMessage;

    Assertions.assertThat(newAircraftMessage)
              .hasId(0L)
              .hasIcaoAddress("4075FD")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT);
  }

  @Test
  void shouldReturnIdMessageMessageFromIdCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("ID,,333,378,48C22B,478,%s,,,RYR6LF");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage).isExactlyInstanceOf(IdMessage.class);

    final IdMessage idMessage = (IdMessage) baseStationMessage;

    Assertions.assertThat(idMessage)
              .hasId(0L)
              .hasIcaoAddress("48C22B")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign("RYR6LF");
  }

  @Test
  void shouldReturnIdAndCategoryTransmissionMessageFromIdAndCategoryTransmissionCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("MSG,1,333,365,40066B,465,%s,,,BCS2135,,,,,,,,,,,");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.IDENTIFICATION_AND_CATEGORY)
              .hasIcaoAddress("40066B")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign("BCS2135")
              .hasAltitude(null)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(null)
              .hasEmergency(null)
              .hasIdentActive(null)
              .hasOnGround(null);
  }

  @Test
  void shouldReturnSurfacePositionTransmissionMessageFromSurfacePositionTransmissionCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,2,333,410,405637,510,%s,,,,0,,,55.95252,-3.36499,,,,,,-1");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.SURFACE_POSITION)
              .hasIcaoAddress("405637")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(0f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(new Point<>(new G2D(-3.36499, 55.95252d), COORDINATE_REFERENCE_SYSTEM))
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(null)
              .hasEmergency(null)
              .hasIdentActive(null)
              .hasOnGround(true);
  }

  @Test
  void shouldReturnAirbornePositionTransmissionMessageFromAirbornePositionTransmissionCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,3,333,417,45D967,517,%s,,,,39000,,,56.37831,-2.75441,,,0,0,0,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.AIRBORNE_POSITION)
              .hasIcaoAddress("45D967")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(39000f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(new Point<>(new G2D(-2.75441d, 56.37831d), COORDINATE_REFERENCE_SYSTEM))
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(false)
              .hasEmergency(false)
              .hasIdentActive(false)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnAirbornePositionTransmissionMessageWithNullPositionFromAirbornePositionTransmissionCsvMessageTextWithUnparseableLatitude() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,3,333,417,45D967,517,%s,,,,39000,,,,-2.75441,,,0,0,0,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.AIRBORNE_POSITION)
              .hasIcaoAddress("45D967")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(39000f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(false)
              .hasEmergency(false)
              .hasIdentActive(false)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnAirbornePositionTransmissionMessageWithNullPositionFromAirbornePositionTransmissionCsvMessageTextWithUnparseableLongitude() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,3,333,417,45D967,517,%s,,,,39000,,,56.37831,,,,0,0,0,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.AIRBORNE_POSITION)
              .hasIcaoAddress("45D967")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(39000f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(false)
              .hasEmergency(false)
              .hasIdentActive(false)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnAirborneVelocityTransmissionMessageFromAirborneVelocityTransmissionCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,4,333,417,45D967,517,%s,,,,,465.0,41.1,,,-64,,,,,");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.AIRBORNE_VELOCITY)
              .hasIcaoAddress("45D967")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(null)
              .hasGroundSpeed(465f)
              .hasTrack(41.1f)
              .hasPosition(null)
              .hasVerticalRate((short) -64)
              .hasSquawk(null)
              .hasAlert(null)
              .hasEmergency(null)
              .hasIdentActive(null)
              .hasOnGround(null);
  }

  @Test
  void shouldReturnSurveillanceAltitudeMessageFromSurveillanceAltitudeCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,5,333,445,405FD4,545,%s,,,,8375,,,,,,,0,,0,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.SURVEILLANCE_ALTITUDE)
              .hasIcaoAddress("405FD4")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(8375f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(false)
              .hasEmergency(null)
              .hasIdentActive(false)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnSurveillanceIdMessageFromSurveillanceIdCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,6,333,445,405FD4,545,%s,,,,16475,,,,,,2726,0,0,0,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.SURVEILLANCE_ID)
              .hasIcaoAddress("405FD4")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(16475f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk((short) 2726)
              .hasAlert(false)
              .hasEmergency(false)
              .hasIdentActive(false)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnAirToAirMessageFromAirToAirCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,7,333,445,405FD4,545,%s,,,,16475,,,,,,,,,,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.AIR_TO_AIR)
              .hasIcaoAddress("405FD4")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(16475f)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(null)
              .hasEmergency(null)
              .hasIdentActive(null)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnAllCallReplyMessageFromAllCallReplyCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,8,333,434,39C494,534,%s,,,,,,,,,,,,,,0");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage)
              .hasId(0L)
              .hasTransmissionType(TransmissionType.ALL_CALL_REPLY)
              .hasIcaoAddress("39C494")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT)
              .hasCallSign(null)
              .hasAltitude(null)
              .hasGroundSpeed(null)
              .hasTrack(null)
              .hasPosition(null)
              .hasVerticalRate(null)
              .hasSquawk(null)
              .hasAlert(null)
              .hasEmergency(null)
              .hasIdentActive(null)
              .hasOnGround(false);
  }

  @Test
  void shouldReturnNullMessageFromTransmissionCsvMessageTextWithMissingTransmissionType() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,,,,,,%s,,,,,,,,,,,,,,");

    Assertions.assertThat(messageParsingService.parseCsvMessageText(csvMessage)).isNull();
  }

  @Test
  void shouldReturnNullMessageFromTransmissionCsvMessageTextWithInvalidTransmissionType() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("MSG,9,,,,,%s,,,,,,,,,,,,,,");

    Assertions.assertThat(messageParsingService.parseCsvMessageText(csvMessage)).isNull();
  }

  @Test
  void shouldReturnSignalLostStatusMessageMessageFromSignalLostStatusCsvMessageText() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("STA,,333,510,4CA7B9,610,%s,,,SL");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage).isExactlyInstanceOf(StatusMessage.class);

    final StatusMessage statusMessage = (StatusMessage) baseStationMessage;

    Assertions.assertThat(statusMessage)
              .hasId(0L)
              .hasStatusMessageType(StatusMessageType.SL)
              .hasIcaoAddress("4CA7B9")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT);
  }

  @Test
  void shouldTruncateCsvMessageTimeTokenAtThreeDecimalPlaes() {
    final String csvMessage =
        String.format(
            "STA,,333,510,4CA7B9,610,%s,,,SL",
            DUMMY_MESSAGE_TIMESTAMP_STRING + "987");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage).isExactlyInstanceOf(StatusMessage.class);

    final StatusMessage statusMessage = (StatusMessage) baseStationMessage;

    Assertions.assertThat(statusMessage)
              .hasId(0L)
              .hasStatusMessageType(StatusMessageType.SL)
              .hasIcaoAddress("4CA7B9")
              .hasTimestamp(DUMMY_MESSAGE_TIMESTAMP_INSTANT);
  }

  @Test
  void shouldReturnNullBooleanPropertyWhenCsvMessageTextTokenNotParseableAsBoolean() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp(
            "MSG,8,333,434,39C494,534,%s,,,,,,,,,,,,,,X");
    final BaseStationMessage parsedMessage = messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(parsedMessage)
              .isExactlyInstanceOf(TransmissionMessage.class);

    final TransmissionMessage transmissionMessage = (TransmissionMessage) parsedMessage;

    Assertions.assertThat(transmissionMessage).hasOnGround(null);
  }

  @Test
  void shouldTrimCallSignOfLeadingAndTrailingWhitespace() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("ID,,333,378,48C22B,478,%s,,,  ABCD  ");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage).isInstanceOf(CallSignMessage.class);

    final CallSignMessage callSignMessage = (CallSignMessage) baseStationMessage;

    Assertions.assertThat(callSignMessage).hasCallSign("ABCD");
  }

  @Test
  void shouldTruncateCallSignAtEightCharacters() {
    final String csvMessage =
        getCsvMessageWithDummyTimestamp("ID,,333,378,48C22B,478,%s,,,123456789");
    final BaseStationMessage baseStationMessage =
        messageParsingService.parseCsvMessageText(csvMessage);

    Assertions.assertThat(baseStationMessage).isInstanceOf(CallSignMessage.class);

    final CallSignMessage callSignMessage = (CallSignMessage) baseStationMessage;

    Assertions.assertThat(callSignMessage).hasCallSign("12345678");
  }
}
