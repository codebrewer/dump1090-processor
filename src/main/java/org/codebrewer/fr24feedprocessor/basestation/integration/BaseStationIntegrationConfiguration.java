/*
 * Copyright 2018 Mark Scott
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

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.codebrewer.fr24feedprocessor.basestation.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.transformer.AbstractPayloadTransformer;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * A configuration for an integration flow that connects to the BaseStation message feed available
 * on some host.
 *
 * <p>Incoming messages are transformed into {@link BaseStationMessage} entities and placed on the
 * message channel named by {@link #BASE_STATION_MESSAGE_CHANNEL_NAME}.
 */
@Configuration
@ManagedResource(description = "An integration flow that reads from the BaseStation message feed")
public class BaseStationIntegrationConfiguration {
  static final String BASE_STATION_MESSAGE_CHANNEL_NAME = "baseStationMessageChannel";

  private static final AtomicLong EMPTY_MESSAGE_COUNT = new AtomicLong();
  private static final AtomicLong INVALID_MESSAGE_COUNT = new AtomicLong();
  private static final AtomicLong VALID_MESSAGE_COUNT = new AtomicLong();
  private static final Logger LOGGER =
      LoggerFactory.getLogger(BaseStationIntegrationConfiguration.class);

  private final TcpReceivingChannelAdapter baseStationFeedAdapter;

  /**
   * Sole constructor for this class, with a parameter indicating whether or not the feed should be
   * established automatically at startup.
   *
   * <p>The value of the parameter can be specified using the {@code basestation.feed.start.auto}
   * property and defaults to true if undefined.
   *
   * @param autoStart whether or not the feed should be established automatically at startup
   */
  public BaseStationIntegrationConfiguration(
      @Value("${basestation.feed.start.auto:true}") boolean autoStart) {
    LOGGER.info("BaseStation message feed auto-start: {}", autoStart);
    baseStationFeedAdapter = new TcpReceivingChannelAdapter();
    baseStationFeedAdapter.setAutoStartup(autoStart);
  }

  @Bean
  public IntegrationFlow tcpMessageClient(@Value("${fr24feed.host}") String host,
                                          @Value("${basestation.feed.port}") int port) {
    final TcpNetClientConnectionFactory clientConnectionFactory =
        new TcpNetClientConnectionFactory(host, port);

    clientConnectionFactory.setSingleUse(false);
    baseStationFeedAdapter.setClientMode(true);
    baseStationFeedAdapter.setConnectionFactory(clientConnectionFactory);

    return IntegrationFlows.from(baseStationFeedAdapter)
                           .filter(byte[].class, source -> {
                             if (source == null || source.length == 0) {
                               LOGGER.warn("Received null or zero-length message payload");
                               EMPTY_MESSAGE_COUNT.incrementAndGet();

                               return false;
                             }

                             return true;
                           })
                           .transform(new AbstractPayloadTransformer<Object, BaseStationMessage>() {
                             @Override
                             protected BaseStationMessage transformPayload(Object payload) {
                               final String payloadString;

                               if (payload instanceof byte[]) {
                                 payloadString =
                                     new String((byte[]) payload, StandardCharsets.US_ASCII);
                               } else if (payload instanceof char[]) {
                                 payloadString = new String((char[]) payload);
                               } else {
                                 payloadString = payload.toString();
                               }

                               final long currentCount = VALID_MESSAGE_COUNT.incrementAndGet();

                               if (currentCount % 100 == 0) {
                                 LOGGER.debug("Raw message count = {}", currentCount);
                               }

                               BaseStationMessage baseStationMessage = null;

                               try {
                                 baseStationMessage = EntityUtils.fromCsvMessageText(payloadString);
                               } catch (Exception e) {
                                 LOGGER.error("Failed to parse message payload", e);
                               }

                               if (baseStationMessage == null) {
                                 INVALID_MESSAGE_COUNT.incrementAndGet();

                                 return INVALID_MESSAGE;
                               }

                               return baseStationMessage;
                             }
                           })
                           .channel(BASE_STATION_MESSAGE_CHANNEL_NAME)
                           .get();
  }

  /**
   * Gets the total number of empty messages received since application startup. Empty messages
   * are occasionally seen in the feed from dump1090-mutability.
   *
   * @return the total number of empty messages received since application startup.
   */
  @ManagedAttribute
  public long getEmptyMessageCount() {
    return EMPTY_MESSAGE_COUNT.get();
  }

  /**
   * Gets the total number of invalid messages received since application startup. An invalid
   * message is one of non-zero length that cannot be successfully parsed to produce a
   * {@code BaseStationMessage} of some type.
   *
   * @return the total number of invalid messages received since application startup.
   */
  @ManagedAttribute
  public long getInvalidMessageCount() {
    return INVALID_MESSAGE_COUNT.get();
  }

  /**
   * Gets the total number of valid messages received since application startup. A valid message is
   * one that can be successfully parsed to produce a {@code BaseStationMessage} of some type.
   *
   * @return the total number of valid messages received since application startup.
   */
  @ManagedAttribute
  public long getValidMessageCount() {
    return VALID_MESSAGE_COUNT.get();
  }

  /**
   * Requests connection to the BaseStation message feed.
   *
   * <p>Has no effect if already started.
   */
  @ManagedOperation
  public void start() {
    LOGGER.info("'Start' requested for BaseStation message feed");
    baseStationFeedAdapter.start();
  }

  /**
   * Requests disconnection from the BaseStation message feed.
   *
   * <p>Has no effect if already stopped.
   */
  @ManagedOperation
  public void stop() {
    LOGGER.info("'Stop' requested for BaseStation message feed");
    baseStationFeedAdapter.stop();
  }
}
