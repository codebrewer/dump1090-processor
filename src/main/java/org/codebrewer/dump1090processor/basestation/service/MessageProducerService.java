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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

@Service
@ManagedResource(
    objectName = "org.codebrewer.dump1090processor:type=Control,name=MessageProducerService",
    description = "A service that connects to the BaseStation message feed")
public class MessageProducerService {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MessageProducerService.class);

  private final TcpReceivingChannelAdapter messageProducer;

  public MessageProducerService(
      @Value("${dump1090.host:localhost}") String host,
      @Value("${basestation.feed.port:30003}") int port,
      @Value("${basestation.feed.start.auto:true}") boolean autoStart) {
    LOGGER.info("MessageProducerService: {}:{}, auto-start: {}", host, port, autoStart);

    final TcpNetClientConnectionFactory clientConnectionFactory =
        new TcpNetClientConnectionFactory(host, port);

    clientConnectionFactory.setSingleUse(false);
    messageProducer = new TcpReceivingChannelAdapter();
    messageProducer.setClientMode(true);
    messageProducer.setConnectionFactory(clientConnectionFactory);
    messageProducer.setAutoStartup(autoStart);
  }

  public TcpReceivingChannelAdapter tcpMessageClient() {
    return messageProducer;
  }

  /**
   * Gets whether or not the connection to the BaseStation message feed is currently established.
   *
   * @return whether or not the connection to the BaseStation message feed is currently established.
   */
  @ManagedAttribute(
      description =
          "Whether or not the connection to the BaseStation message feed is currently established")
  public boolean isRunning() {
    return messageProducer.isRunning();
  }

  /**
   * Requests connection to the BaseStation message feed.
   *
   * <p>Has no effect if already started.
   */
  @ManagedOperation(description = "Connect to the feed and start reading BaseStation messages")
  public void start() {
    LOGGER.info("'Start' requested for BaseStation message feed");
    messageProducer.start();
  }

  /**
   * Requests disconnection from the BaseStation message feed.
   *
   * <p>Has no effect if already stopped.
   */
  @ManagedOperation(description = "Stop reading BaseStation messages and disconnect from the feed")
  public void stop() {
    LOGGER.info("'Stop' requested for BaseStation message feed");
    messageProducer.stop();
  }
}
