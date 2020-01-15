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

import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.AIRBORNE_POSITION;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.AIRBORNE_VELOCITY;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.AIR_TO_AIR;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.ALL_CALL_REPLY;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.IDENTIFICATION_AND_CATEGORY;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.SURFACE_POSITION;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.SURVEILLANCE_ALTITUDE;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.SURVEILLANCE_ID;
import static org.codebrewer.dump1090processor.basestation.domain.TransmissionType.getByRawValue;

import org.codebrewer.dump1090processor.Assertions;
import org.junit.jupiter.api.Test;

class TransmissionTypeTest {
  @Test
  void shouldReturnNullForNullRawValue() {
    Assertions.assertThat(getByRawValue(null)).isNull();
  }

  @Test
  void shouldReturnNullForRawValueLessThan1() {
    Assertions.assertThat(getByRawValue((short) 0)).isNull();
  }

  @Test
  void shouldReturnNullForRawValueGreaterThan8() {
    Assertions.assertThat(getByRawValue((short) 9)).isNull();
  }

  @Test
  void shouldReturnIdentificationAndCategoryForRawValue1() {
    Assertions.assertThat(getByRawValue((short) 1)).isEqualTo(IDENTIFICATION_AND_CATEGORY);
  }

  @Test
  void shouldReturnSurfacePositionForRawValue2() {
    Assertions.assertThat(getByRawValue((short) 2)).isEqualTo(SURFACE_POSITION);
  }

  @Test
  void shouldReturnAirbornePositionForRawValue3() {
    Assertions.assertThat(getByRawValue((short) 3)).isEqualTo(AIRBORNE_POSITION);
  }

  @Test
  void shouldReturnAirborneVelocityForRawValue4() {
    Assertions.assertThat(getByRawValue((short) 4)).isEqualTo(AIRBORNE_VELOCITY);
  }

  @Test
  void shouldReturnSurveillanceAltitudeForRawValue5() {
    Assertions.assertThat(getByRawValue((short) 5)).isEqualTo(SURVEILLANCE_ALTITUDE);
  }

  @Test
  void shouldReturnSurveillanceIdForRawValue6() {
    Assertions.assertThat(getByRawValue((short) 6)).isEqualTo(SURVEILLANCE_ID);
  }

  @Test
  void shouldReturnAirToAirForRawValue7() {
    Assertions.assertThat(getByRawValue((short) 7)).isEqualTo(AIR_TO_AIR);
  }

  @Test
  void shouldReturnAllCallReplyForRawValue8() {
    Assertions.assertThat(getByRawValue((short) 8)).isEqualTo(ALL_CALL_REPLY);
  }
}
