/*
 * Copyright 2018 Mark Scott
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
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.codebrewer.fr24feedprocessor.basestation.domain.StatusMessageType;

/**
 * Class for BaseStation "status message" entities.
 */
@Entity
@DiscriminatorValue("STA")
class StatusMessage extends BaseStationMessage {
  @Column(length = 3)
  @Enumerated(EnumType.STRING)
  private StatusMessageType statusMessageType;

  @SuppressWarnings("unused")
  StatusMessage() {
    // No-arg constructor required by Hibernate
    super();
  }

  private StatusMessage(Builder builder) {
    super(builder);
    this.statusMessageType = builder.statusMessageType;
  }

  public StatusMessageType getStatusMessageType() {
    return statusMessageType;
  }

  public void setStatusMessageType(StatusMessageType statusMessageType) {
    this.statusMessageType = statusMessageType;
  }

  /**
   * A builder for the {@code StatusMessage} entity type.
   */
  static class Builder extends BaseStationMessage.Builder<StatusMessage, Builder> {
    private final StatusMessageType statusMessageType;

    /**
     * Sole constructor for this class, with parameters for properties common to all BaseStation
     * message types and to this type.
     *
     * @param icaoAddress the 24 bit address assigned by the ICAO to an aircraft transponder,
     * represented as a 6 digit hexadecimal number, not null
     * @param creationTimestamp the instant at which the message was created
     * @param receptionTimestamp the instant at which the message was received
     * @param statusMessageType the type of status message to be built
     */
    Builder(String icaoAddress,
            Instant creationTimestamp,
            Instant receptionTimestamp,
            StatusMessageType statusMessageType) {
      super(icaoAddress, creationTimestamp, receptionTimestamp);
      this.statusMessageType = statusMessageType;
    }

    @Override
    StatusMessage build() {
      return new StatusMessage(this);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
