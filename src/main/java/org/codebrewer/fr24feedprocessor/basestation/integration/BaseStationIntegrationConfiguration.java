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

import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.codebrewer.fr24feedprocessor.basestation.service.EmptyMessageFilteringService;
import org.codebrewer.fr24feedprocessor.basestation.service.MessagePayloadTransformerService;
import org.codebrewer.fr24feedprocessor.basestation.service.MessageProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

/**
 * A configuration for an integration flow that connects to the BaseStation message feed available
 * on some host.
 *
 * <p>Incoming messages are transformed into {@link BaseStationMessage} entities and placed on the
 * message channel named by {@link #BASE_STATION_MESSAGE_CHANNEL_NAME}.
 */
@Configuration
public class BaseStationIntegrationConfiguration {
  static final String BASE_STATION_MESSAGE_CHANNEL_NAME = "baseStationMessageChannel";

  private final MessageProducerService messageProducerService;
  private final EmptyMessageFilteringService emptyMessageFilteringService;
  private final MessagePayloadTransformerService messagePayloadTransformerService;

  /**
   * Sole constructor for this class.
   *
   * <p>The value of the {@code autoStart} parameter can be specified using the
   * {@code basestation.feed.start.auto} property and defaults to true if undefined.
   *
   * @param messageProducerService a service for producing the message feed
   * @param emptyMessageFilteringService a service for removing empty messages from the message feed
   * @param messagePayloadTransformerService a service for transforming incoming message payloads
   * into {@code BaseStationMessage} objects
   */
  @Autowired
  public BaseStationIntegrationConfiguration(
      MessageProducerService messageProducerService,
      EmptyMessageFilteringService emptyMessageFilteringService,
      MessagePayloadTransformerService messagePayloadTransformerService) {
    this.messageProducerService = messageProducerService;
    this.messagePayloadTransformerService = messagePayloadTransformerService;
    this.emptyMessageFilteringService = emptyMessageFilteringService;
  }

  @Bean
  public IntegrationFlow tcpMessageClient() {
    return IntegrationFlows.from(messageProducerService.tcpMessageClient())
                           .filter(emptyMessageFilteringService)
                           .transform(messagePayloadTransformerService)
                           .channel(BASE_STATION_MESSAGE_CHANNEL_NAME)
                           .get();
  }
}
