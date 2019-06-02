/*
 * Copyright 2018, 2019 Mark Scott
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

package org.codebrewer.fr24feedprocessor.basestation.integration;

import static org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage.INVALID_MESSAGE;
import static org.codebrewer.fr24feedprocessor.basestation.integration.BaseStationIntegrationConfiguration.BASE_STATION_MESSAGE_CHANNEL_NAME;

import java.util.Objects;
import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.codebrewer.fr24feedprocessor.basestation.repository.BaseStationMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * An endpoint for consuming BaseStation messages.
 *
 * <p>By default, valid consumed messages are persisted to the BaseStation repository. A managed
 * operation is provided to give runtime control over persistence.
 */
@MessageEndpoint
@ManagedResource(
    objectName = "org.codebrewer.fr24feedprocessor:type=Control,name=BaseStationMessageEndpoint",
    description = "An endpoint for handling BaseStation messages")
public class BaseStationMessageEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseStationMessageEndpoint.class);

  private final BaseStationMessageRepository repository;
  private volatile boolean persistMessages;

  /**
   * Sole constructor for this class.
   *
   * <p>Whether or not BaseStation messages are persisted can be specified using the
   * {@code basestation.feed.persist} property. Messages are persisted if the property is undefined.
   *
   * @param repository a repository to which BaseStation message entities can be persisted
   * @param persistMessages whether or not BaseStation message entities should be persisted
   */
  @Autowired
  public BaseStationMessageEndpoint(
      BaseStationMessageRepository repository,
      @Value("${basestation.feed.persist:true}") boolean persistMessages) {
    LOGGER.info("BaseStation message persistence: {}", persistMessages);
    this.repository = repository;
    this.persistMessages = persistMessages;
  }

  /**
   * Handles incoming BaseStation message payloads.
   *
   * <p>Messages are received from the channel named by
   * {@link BaseStationIntegrationConfiguration#BASE_STATION_MESSAGE_CHANNEL_NAME
   * BASE_STATION_MESSAGE_CHANNEL_NAME}.
   *
   * @param baseStationMessage an incoming BaseStation message
   */
  @SuppressWarnings("UnresolvedMessageChannel")
  @ServiceActivator(inputChannel = BASE_STATION_MESSAGE_CHANNEL_NAME)
  public void consume(@Payload BaseStationMessage baseStationMessage) {
    if (persistMessages && !Objects.equals(baseStationMessage, INVALID_MESSAGE)) {
      repository.save(baseStationMessage);
    }
  }

  /**
   * Indicates whether or not BaseStation messages received by this endpoint are persisted to the
   * application's database.
   *
   * @return true if received messages are persisted to the database, otherwise false
   */
  @ManagedOperation(
      description = "Whether or not BaseStation messages are saved to persistent storage")
  public boolean isPersistMessages() {
    return persistMessages;
  }

  /**
   * Controls whether or not BaseStation messages received by this endpoint are persisted to the
   * application's database.
   *
   * @param persistMessages true if messages should be persisted to the database, false if not
   */
  @ManagedOperation(
      description = "Control whether or not BaseStation messages are saved to persistent storage")
  public void setPersistMessages(boolean persistMessages) {
    LOGGER.info("Persist messages: {}", persistMessages);
    this.persistMessages = persistMessages;
  }
}
