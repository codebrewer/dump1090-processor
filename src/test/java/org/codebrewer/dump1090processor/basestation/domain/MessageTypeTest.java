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

class MessageTypeTest {
  private static void assertMessageTokenCount(MessageType messageType, int expectedTokenCount) {
    Assertions.assertThat(messageType.getMessageTokenCount()).isEqualTo(expectedTokenCount);
  }

  @Test
  void shouldReturnCorrectMessageTokenCountForMessageTypeAIR() {
    assertMessageTokenCount(MessageType.AIR, 10);
  }

  @Test
  void shouldReturnCorrectMessageTokenCountForMessageTypeID() {
    assertMessageTokenCount(MessageType.ID, 11);
  }

  @Test
  void shouldReturnCorrectMessageTokenCountForMessageTypeMSG() {
    assertMessageTokenCount(MessageType.MSG, 22);
  }

  @Test
  void shouldReturnCorrectMessageTokenCountForMessageTypeSTA() {
    assertMessageTokenCount(MessageType.STA, 11);
  }
}
