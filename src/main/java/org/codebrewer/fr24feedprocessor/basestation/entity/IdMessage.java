/*
 * Copyright 2018, 2019 Mark Scott
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

package org.codebrewer.fr24feedprocessor.basestation.entity;

import java.time.Instant;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Class for BaseStation "identification message" entities.
 */
@Entity
@DiscriminatorValue("ID")
public class IdMessage extends CallSignMessage {
  @SuppressWarnings("unused")
  IdMessage() {
    // No-arg constructor required by Hibernate
    super();
  }

  private IdMessage(Builder builder) {
    super(builder);
  }

  /**
   * A builder for the {@code IdMessage} entity type.
   */
  static class Builder extends CallSignMessage.Builder {
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

    @Override
    IdMessage build() {
      return new IdMessage(this);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
