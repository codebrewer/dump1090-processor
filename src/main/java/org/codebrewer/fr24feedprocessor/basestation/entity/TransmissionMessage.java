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
import java.util.Objects;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.codebrewer.fr24feedprocessor.basestation.domain.TransmissionType;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

/**
 * Class for BaseStation "transmission message" entities.
 */
@Entity
@DiscriminatorValue(value = "MSG")
public class TransmissionMessage extends CallSignMessage {
  private TransmissionType transmissionType;
  private Float altitude;
  private Float groundSpeed;
  private Float track;
  private Point<G2D> position;
  private Short verticalRate;
  private Short squawk;
  private Boolean alert;
  private Boolean emergency;
  private Boolean identActive;
  private Boolean onGround;

  @SuppressWarnings("unused")
  TransmissionMessage() {
    // No-arg constructor required by Hibernate
    super();
  }

  private TransmissionMessage(Builder builder) {
    super(builder);
    transmissionType = builder.transmissionType;
    altitude = builder.altitude;
    groundSpeed = builder.groundSpeed;
    track = builder.track;
    position = builder.position;
    verticalRate = builder.verticalRate;
    squawk = builder.squawk;
    alert = builder.alert;
    emergency = builder.emergency;
    identActive = builder.identActive;
    onGround = builder.onGround;
  }

  public TransmissionType getTransmissionType() {
    return transmissionType;
  }

  public Float getAltitude() {
    return altitude;
  }

  public Float getGroundSpeed() {
    return groundSpeed;
  }

  public Float getTrack() {
    return track;
  }

  public Point<G2D> getPosition() {
    return position;
  }

  public Short getVerticalRate() {
    return verticalRate;
  }

  public Short getSquawk() {
    return squawk;
  }

  public Boolean getAlert() {
    return alert;
  }

  public Boolean getEmergency() {
    return emergency;
  }

  public Boolean getIdentActive() {
    return identActive;
  }

  public Boolean getOnGround() {
    return onGround;
  }

  /**
   * A builder for the {@code TransmissionMessage} entity type.
   */
  static class Builder extends CallSignMessage.Builder {
    private TransmissionType transmissionType;
    private Float altitude;
    private Float groundSpeed;
    private Float track;
    private Point<G2D> position;
    private Short verticalRate;
    private Short squawk;
    private Boolean alert;
    private Boolean emergency;
    private Boolean identActive;
    private Boolean onGround;

    /**
     * Sole constructor for this class, with parameters for properties common to all BaseStation
     * message types and to this type.
     *
     * @param icaoAddress the 24 bit address assigned by the ICAO to an aircraft transponder,
     * represented as a 6 digit hexadecimal number, not null
     * @param creationTimestamp the instant at which the message was received, not null
     */
    Builder(String icaoAddress, Instant creationTimestamp) {
      super(icaoAddress, creationTimestamp);
    }

    @Override
    TransmissionMessage build() {
      return new TransmissionMessage(this);
    }

    @Override
    protected Builder self() {
      return this;
    }

    Builder transmissionType(TransmissionType transmissionType) {
      this.transmissionType = transmissionType;

      return self();
    }

    Builder altitude(Float altitude) {
      this.altitude = altitude;

      return self();
    }

    Builder groundSpeed(Float groundSpeed) {
      this.groundSpeed = groundSpeed;

      return self();
    }

    Builder track(Float track) {
      this.track = track;

      return self();
    }

    Builder position(Point<G2D> position) {
      this.position = position;

      return self();
    }

    Builder verticalRate(Short verticalRate) {
      this.verticalRate = verticalRate;

      return self();
    }

    Builder squawk(Short squawk) {
      this.squawk = squawk;

      return self();
    }

    Builder alert(Boolean alert) {
      this.alert = alert;

      return self();
    }

    Builder emergency(Boolean emergency) {
      this.emergency = emergency;

      return self();
    }

    Builder identActive(Boolean identActive) {
      this.identActive = identActive;

      return self();
    }

    Builder onGround(Boolean onGround) {
      this.onGround = onGround;

      return self();
    }
  }
}
