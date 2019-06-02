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

import static org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage.INVALID_MESSAGE;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.transformer.AbstractPayloadTransformer;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

@Service
@ManagedResource(
    objectName = "org.codebrewer.fr24feedprocessor:type=Counter,name=TransformerService",
    description = "Transforms the incoming stream of data into BaseStation messages")
public class MessagePayloadTransformerService
    extends AbstractPayloadTransformer<Object, BaseStationMessage> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MessagePayloadTransformerService.class);

  private final MessageParsingService messageParsingService;
  private final AtomicLong invalidMessageCount = new AtomicLong();
  private final AtomicLong validMessageCount = new AtomicLong();

  @Autowired
  public MessagePayloadTransformerService(MessageParsingService messageParsingService) {
    this.messageParsingService = messageParsingService;
  }

  private BaseStationMessage incrementInvalidMessageCountAndGetInvalidMessageConstant() {
    invalidMessageCount.incrementAndGet();

    return INVALID_MESSAGE;
  }

  @Override
  protected BaseStationMessage transformPayload(Object payload) {
    if (payload == null) {
      return incrementInvalidMessageCountAndGetInvalidMessageConstant();
    }

    final String payloadString;

    if (payload instanceof byte[]) {
      payloadString = new String((byte[]) payload, StandardCharsets.US_ASCII);
    } else if (payload instanceof char[]) {
      payloadString = new String((char[]) payload);
    } else {
      payloadString = payload.toString();
    }

    BaseStationMessage baseStationMessage = null;

    try {
      baseStationMessage = messageParsingService.parseCsvMessageText(payloadString);
    } catch (Exception e) {
      LOGGER.error("Failed to parse message payload", e);
    }

    if (baseStationMessage == null) {
      return incrementInvalidMessageCountAndGetInvalidMessageConstant();
    }

    validMessageCount.incrementAndGet();

    return baseStationMessage;
  }

  /**
   * Gets the total number of invalid messages received since application startup. An invalid
   * message is one of non-zero length that cannot be successfully parsed to produce a
   * {@code BaseStationMessage} of some type.
   *
   * @return the total number of invalid messages received since application startup.
   */
  @ManagedAttribute(
      description = "The total number of invalid messages received since application startup")
  public long getInvalidMessageCount() {
    return invalidMessageCount.get();
  }

  /**
   * Gets the total number of valid messages received since application startup. A valid message is
   * one that can be successfully parsed to produce a {@code BaseStationMessage} of some type.
   *
   * @return the total number of valid messages received since application startup.
   */
  @ManagedAttribute(
      description = "The total number of valid messages received since application startup")
  public long getValidMessageCount() {
    return validMessageCount.get();
  }
}
