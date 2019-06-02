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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class MessageProducerServiceTest {
  @Test
  void shouldCreateTcpReceivingChannelAdapter() {
    final MessageProducerService messageProducerService =
        new MessageProducerService("localhost", 5000, false);

    assertNotNull(messageProducerService.tcpMessageClient());
  }

  @Test
  void shouldStartAndStop() {
    final MessageProducerService messageProducerService =
        new MessageProducerService("localhost", 5000, false);
    final AbstractServerConnectionFactory connectionFactory = new TcpNetServerConnectionFactory(0);
    final TcpReceivingChannelAdapter adapter = messageProducerService.tcpMessageClient();
    final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    taskScheduler.setPoolSize(1);
    taskScheduler.initialize();
    adapter.setConnectionFactory(connectionFactory);
    adapter.setTaskScheduler(taskScheduler);

    assertFalse(messageProducerService.isRunning());
    messageProducerService.start();
    Assertions.assertTrue(messageProducerService.isRunning());
    messageProducerService.stop();
    assertFalse(messageProducerService.isRunning());
  }
}
