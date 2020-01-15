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

package org.codebrewer.dump1090processor.basestation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DomainUtilsTest {
  private static void assertExpectedMessageType(MessageType messageType) {
    Assertions.assertThat(DomainUtils.isExpectedMessageType(messageType)).isTrue();
  }

  private static void assertNotExpectedMessageType(MessageType messageType) {
    Assertions.assertThat(DomainUtils.isExpectedMessageType(messageType)).isFalse();
  }

  private static void assertExpectedStatusMessageType(StatusMessageType statusMessageType) {
    Assertions.assertThat(DomainUtils.isExpectedStatusMessageType(statusMessageType)).isTrue();
  }

  private static void assertNotExpectedStatusMessageType(StatusMessageType statusMessageType) {
    Assertions.assertThat(DomainUtils.isExpectedStatusMessageType(statusMessageType)).isFalse();
  }

  @Test
  void shouldExpectMessageTypeAIR() {
    assertExpectedMessageType(MessageType.AIR);
  }

  @Test
  void shouldExpectMessageTypeID() {
    assertExpectedMessageType(MessageType.ID);
  }

  @Test
  void shouldExpectMessageTypeMSG() {
    assertExpectedMessageType(MessageType.MSG);
  }

  @Test
  void shouldExpectMessageTypeSTA() {
    assertExpectedMessageType(MessageType.STA);
  }

  @Test
  void shouldNotExpectMessageTypeCLK() {
    assertNotExpectedMessageType(MessageType.CLK);
  }

  @Test
  void shouldNotExpectMessageTypeSEL() {
    assertNotExpectedMessageType(MessageType.SEL);
  }

  @Test
  void shouldNotExpectNullMessageType() {
    assertNotExpectedMessageType(null);
  }

  @Test
  void shouldExpectStatusMessageTypeRM() {
    assertExpectedStatusMessageType(StatusMessageType.RM);
  }

  @Test
  void shouldExpectStatusMessageTypeSL() {
    assertExpectedStatusMessageType(StatusMessageType.SL);
  }

  @Test
  void shouldNotExpectStatusMessageTypeAD() {
    assertNotExpectedStatusMessageType(StatusMessageType.AD);
  }

  @Test
  void shouldNotExpectStatusMessageTypeOK() {
    assertNotExpectedStatusMessageType(StatusMessageType.OK);
  }

  @Test
  void shouldNotExpectStatusMessageTypePL() {
    assertNotExpectedStatusMessageType(StatusMessageType.PL);
  }

  @Test
  void shouldNotExpectNullStatusMessageType() {
    assertNotExpectedStatusMessageType(null);
  }

  @Test
  void shouldTrimCallSignOfLeadingAndTrailingWhitespace() {
    Assertions.assertThat(DomainUtils.getValidatedCallSign("  ABCD  ")).isEqualTo("ABCD");
  }

  @Test
  void shouldTruncateCallSignAtEightCharacters() {
    Assertions.assertThat(DomainUtils.getValidatedCallSign("123456789")).isEqualTo("12345678");
  }

  @Test
  void shouldReturnNullCallSignForNullInput() {
    Assertions.assertThat(DomainUtils.getValidatedCallSign(null)).isNull();
  }
}
