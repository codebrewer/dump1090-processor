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

package org.codebrewer.dump1090processor.basestation.integration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.codebrewer.dump1090processor.basestation.service.EmptyMessageFilteringService;
import org.codebrewer.dump1090processor.basestation.service.InvalidMessageFilteringService;
import org.codebrewer.dump1090processor.basestation.service.MessagePayloadTransformerService;
import org.codebrewer.dump1090processor.basestation.service.MessageProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;

class BaseStationIntegrationConfigurationTest {
  private MessageProducerService producerService;
  private EmptyMessageFilteringService emptyMessageFilteringService;
  private MessagePayloadTransformerService transformerService;
  private InvalidMessageFilteringService invalidMessageFilteringService;
  private TcpReceivingChannelAdapter channelAdapter;

  @BeforeEach
  void setUp() {
    producerService = Mockito.mock(MessageProducerService.class);
    emptyMessageFilteringService = Mockito.mock(EmptyMessageFilteringService.class);
    transformerService = Mockito.mock(MessagePayloadTransformerService.class);
    invalidMessageFilteringService = Mockito.mock(InvalidMessageFilteringService.class);
    channelAdapter = Mockito.mock(TcpReceivingChannelAdapter.class);
  }

  @Test
  void shouldCreateIntegrationFlow() {
    final BaseStationIntegrationConfiguration configuration =
        new BaseStationIntegrationConfiguration(
            producerService,
            emptyMessageFilteringService,
            transformerService,
            invalidMessageFilteringService);

    when(producerService.tcpMessageClient()).thenReturn(channelAdapter);
    configuration.tcpMessageClient();
    verify(producerService, Mockito.times(1)).tcpMessageClient();
  }
}
