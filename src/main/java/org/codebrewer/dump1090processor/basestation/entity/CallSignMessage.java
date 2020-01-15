/*
 * Copyright 2018, 2019, 2020 Mark Scott
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

package org.codebrewer.dump1090processor.basestation.entity;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Abstract base class for BaseStation message entities that have a callsign property.
 */
@Entity
public abstract class CallSignMessage extends BaseStationMessage {
  @Column(length = 8)
  private String callSign;

  @SuppressWarnings("WeakerAccess")
  protected CallSignMessage() {
    // No-arg constructor required by Hibernate
    super();
  }

  CallSignMessage(Builder builder) {
    super(builder);
    callSign = builder.callSign;
  }

  public String getCallSign() {
    return callSign;
  }

  /**
   * A builder for the {@code CallSignMessage} entity type.
   */
  public static abstract class Builder extends BaseStationMessage.Builder<CallSignMessage, Builder> {
    private String callSign;

    /**
     * Sole constructor for this class, with parameters for properties common to all BaseStation
     * message types.
     *
     * @param icaoAddress the 24 bit address assigned by the ICAO to an aircraft transponder,
     * represented as a 6 digit hexadecimal number, not null
     * @param timestamp the instant at which the message was received, not null
     */
    Builder(String icaoAddress, Instant timestamp) {
      super(icaoAddress, timestamp);
    }

    public Builder callSign(String callSign) {
      this.callSign = callSign;

      return self();
    }
  }
}
