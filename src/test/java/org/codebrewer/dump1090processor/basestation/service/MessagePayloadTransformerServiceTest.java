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

import static org.assertj.core.api.Assertions.assertThat;
import static org.codebrewer.dump1090processor.basestation.entity.BaseStationMessage.INVALID_MESSAGE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.codebrewer.dump1090processor.basestation.entity.BaseStationMessage;
import org.codebrewer.dump1090processor.basestation.entity.IdMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MessagePayloadTransformerServiceTest {
  private MessageParsingService parsingService;
  private MessagePayloadTransformerService transformerService;

  private BaseStationMessage sendNullMessage() {
    return transformerService.transformPayload(null);
  }

  @BeforeEach
  void setUp() {
    parsingService = Mockito.mock(MessageParsingService.class);
    transformerService = new MessagePayloadTransformerService(parsingService);
  }

  @Test
  void shouldReturnInvalidMessageConstantForNullPayload() {
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
    assertThat(sendNullMessage()).isSameAs(INVALID_MESSAGE);
    verifyZeroInteractions(parsingService);
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
  }

  @Test
  void shouldReturnMessageForByteArrayPayload() {
    final BaseStationMessage baseStationMessage = Mockito.mock(IdMessage.class);
    final String messageText = "Test message";
    final byte[] messagePayload = messageText.getBytes(StandardCharsets.US_ASCII);

    when(parsingService.parseCsvMessageText(eq(messageText))).thenReturn(baseStationMessage);
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
    assertThat(transformerService.transformPayload(messagePayload)).isSameAs(baseStationMessage);
    verify(parsingService, Mockito.times(1)).parseCsvMessageText(eq(messageText));
    assertThat(transformerService.getValidMessageCount()).isEqualTo(1L);
  }

  @Test
  void shouldReturnMessageForCharArrayPayload() {
    final BaseStationMessage baseStationMessage = Mockito.mock(IdMessage.class);
    final String messageText = "Test message";
    final char[] messagePayload = messageText.toCharArray();

    when(parsingService.parseCsvMessageText(eq(messageText))).thenReturn(baseStationMessage);
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
    assertThat(transformerService.transformPayload(messagePayload)).isSameAs(baseStationMessage);
    verify(parsingService, Mockito.times(1)).parseCsvMessageText(eq(messageText));
    assertThat(transformerService.getValidMessageCount()).isEqualTo(1L);
  }

  @Test
  void shouldReturnInvalidMessageConstantWhenParsingServiceThrowsException() {
    when(parsingService.parseCsvMessageText(anyString())).thenThrow(IllegalArgumentException.class);
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
    assertThat(transformerService.transformPayload(new Object())).isSameAs(INVALID_MESSAGE);
    verify(parsingService, Mockito.times(1)).parseCsvMessageText(anyString());
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
  }

  @Test
  void shouldMaintainCorrectCountOfEmptyMessages() {
    final BaseStationMessage baseStationMessage = Mockito.mock(IdMessage.class);
    final String messageText = "Test message";
    final byte[] messagePayload = messageText.getBytes(StandardCharsets.US_ASCII);

    when(parsingService.parseCsvMessageText(eq(messageText))).thenReturn(baseStationMessage);
    assertThat(transformerService.getValidMessageCount()).isEqualTo(0L);
    sendNullMessage();
    sendNullMessage();
    transformerService.transformPayload(messagePayload);
    sendNullMessage();
    transformerService.transformPayload(messagePayload);
    transformerService.transformPayload(messagePayload);
    sendNullMessage();
    assertThat(transformerService.getValidMessageCount()).isEqualTo(3L);
  }
}
