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

import static org.assertj.core.api.Assertions.assertThat;

import org.codebrewer.fr24feedprocessor.basestation.entity.BaseStationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class InvalidMessageFilteringServiceTest {
  private InvalidMessageFilteringService filteringService;
  private BaseStationMessage validMessagePayload;

  @BeforeEach
  void setUp() {
    filteringService = new InvalidMessageFilteringService();
    validMessagePayload = Mockito.mock(BaseStationMessage.class);
  }

  @Test
  void shouldReturnFalseForInvalidMessage() {
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(0L);
    assertThat(filteringService.filterInvalidMessage(BaseStationMessage.INVALID_MESSAGE)).isFalse();
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(1L);
  }

  @Test
  void shouldReturnTrueForValidMessage() {
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(0L);
    assertThat(filteringService.filterInvalidMessage(validMessagePayload)).isTrue();
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(0L);
  }

  @Test
  void shouldMaintainCorrectCountOfInvalidMessage() {
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(0L);
    filteringService.filterInvalidMessage(validMessagePayload);
    filteringService.filterInvalidMessage(BaseStationMessage.INVALID_MESSAGE);
    filteringService.filterInvalidMessage(validMessagePayload);
    filteringService.filterInvalidMessage(BaseStationMessage.INVALID_MESSAGE);
    filteringService.filterInvalidMessage(BaseStationMessage.INVALID_MESSAGE);
    filteringService.filterInvalidMessage(BaseStationMessage.INVALID_MESSAGE);
    assertThat(filteringService.getInvalidMessageCount()).isEqualTo(4L);
  }
}
