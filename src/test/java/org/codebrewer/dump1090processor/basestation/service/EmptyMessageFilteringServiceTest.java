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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmptyMessageFilteringServiceTest {
  private EmptyMessageFilteringService filteringService;

  @BeforeEach
  void setUp() {
    filteringService = new EmptyMessageFilteringService();
  }

  @Test
  void shouldReturnFalseForNullPayload() {
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(0L);
    assertThat(filteringService.filterEmptyMessage(null)).isFalse();
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(1L);
  }

  @Test
  void shouldReturnFalseForZeroLengthPayload() {
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(0L);
    assertThat(filteringService.filterEmptyMessage(new byte[0])).isFalse();
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(1L);
  }

  @Test
  void shouldReturnTrueForNonZeroLengthPayload() {
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(0L);
    assertThat(filteringService.filterEmptyMessage(new byte[] { 0 })).isTrue();
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(0L);
  }

  @Test
  void shouldMaintainCorrectCountOfEmptyMessages() {
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(0L);
    filteringService.filterEmptyMessage(new byte[] { 0 });
    filteringService.filterEmptyMessage(null);
    filteringService.filterEmptyMessage(new byte[] { 0 });
    filteringService.filterEmptyMessage(null);
    filteringService.filterEmptyMessage(null);
    filteringService.filterEmptyMessage(null);
    assertThat(filteringService.getEmptyMessageCount()).isEqualTo(4L);
  }
}
