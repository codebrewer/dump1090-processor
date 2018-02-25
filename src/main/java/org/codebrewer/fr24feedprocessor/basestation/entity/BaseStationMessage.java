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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Transient;

/**
 * Abstract base class for BaseStation message entities.
 */
@Entity
@Inheritance
@DiscriminatorColumn(length = 3, name = "message_type")
public abstract class BaseStationMessage {
  /**
   * An instance used when the raw data received for a message cannot be parsed into a valid
   * BaseStation message.
   */
  public static BaseStationMessage INVALID_MESSAGE = new BaseStationMessage() {
  };

  @Id
  @GeneratedValue
  private long id;
  @Transient
  private Integer sessionId; // transient fr24feed/dump1090 property
  @Transient
  private Integer aircraftId; // transient fr24feed/dump1090 property
  @Column(length = 6, nullable = false)
  private String icaoAddress;
  @Transient
  private Integer flightId; // transient fr24feed/dump1090 property
  @Column(nullable = false)
  private Instant creationTimestamp;
  @Column(nullable = false)
  private Instant receptionTimestamp;

  protected BaseStationMessage() {
  }

  protected BaseStationMessage(Builder builder) {
    icaoAddress = builder.icaoAddress;
    creationTimestamp = builder.creationTimestamp;
    receptionTimestamp = builder.receptionTimestamp;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Integer getSessionId() {
    return sessionId;
  }

  public void setSessionId(Integer sessionId) {
    this.sessionId = sessionId;
  }

  public Integer getAircraftId() {
    return aircraftId;
  }

  public void setAircraftId(Integer aircraftId) {
    this.aircraftId = aircraftId;
  }

  public String getIcaoAddress() {
    return icaoAddress;
  }

  public void setIcaoAddress(String icaoAddress) {
    this.icaoAddress = icaoAddress;
  }

  public Integer getFlightId() {
    return flightId;
  }

  public void setFlightId(Integer flightId) {
    this.flightId = flightId;
  }

  public Instant getCreationTimestamp() {
    return creationTimestamp;
  }

  public void setCreationTimestamp(Instant creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public Instant getReceptionTimestamp() {
    return receptionTimestamp;
  }

  public void setReceptionTimestamp(Instant receptionTimestamp) {
    this.receptionTimestamp = receptionTimestamp;
  }

  /**
   * Abstract base class for generically building BaseStation message subclasses.
   *
   * <p>Based on Item 2 of Effective Java, 3rd Edition by Josh Bloch.
   *
   * @param <M> the type of BaseStation message that is built
   * @param <B> the type of builder that builds messages of type {@code <M>}
   */
  static abstract class Builder<M extends BaseStationMessage, B extends Builder<M, B>> {
    private final String icaoAddress;
    private final Instant creationTimestamp;
    private final Instant receptionTimestamp;

    /**
     * Sole constructor for this class, with parameters for properties common to all BaseStation
     * message types and to this type.
     *
     * @param icaoAddress the 24 bit address assigned by the ICAO to an aircraft transponder,
     * represented as a 6 digit hexadecimal number, not null
     * @param creationTimestamp the instant at which the message was created
     * @param receptionTimestamp the instant at which the message was received
     */
    Builder(String icaoAddress, Instant creationTimestamp, Instant receptionTimestamp) {
      this.icaoAddress = icaoAddress;
      this.creationTimestamp = creationTimestamp;
      this.receptionTimestamp = receptionTimestamp;
    }

    /**
     * Builds a message of the type specified by this builder's generic type parameter and
     * configured by this builder's properties.
     *
     * @return a message of type {@link #<M>}
     */
    abstract M build();

    /**
     * Gets this builder.
     *
     * @return this builder
     */
    protected abstract B self(); // Subclasses must override this method to return "this"
  }
}
