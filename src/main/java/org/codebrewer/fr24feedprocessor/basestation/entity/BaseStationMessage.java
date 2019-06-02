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
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;

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
  public static final BaseStationMessage INVALID_MESSAGE = new BaseStationMessage() {
  };

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long id;
  @Column(length = 6, nullable = false)
  private String icaoAddress;
  @Column(nullable = false)
  private Instant timestamp;

  protected BaseStationMessage() {
  }

  protected BaseStationMessage(Builder builder) {
    icaoAddress = builder.icaoAddress;
    timestamp = builder.creationTimestamp;
  }

  public long getId() {
    return id;
  }

  public String getIcaoAddress() {
    return icaoAddress;
  }

  public Instant getTimestamp() {
    return timestamp;
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

    /**
     * Sole constructor for this class, with parameters for properties common to all BaseStation
     * message types.
     *
     * @param icaoAddress the 24 bit address assigned by the ICAO to an aircraft transponder,
     * represented as a 6 digit hexadecimal number, not null
     * @param timestamp the instant at which the message was received, not null
     */
    Builder(String icaoAddress, Instant timestamp) {
      this.icaoAddress = Objects.requireNonNull(icaoAddress, "ICAO address is required");
      this.creationTimestamp = Objects.requireNonNull(timestamp, "Timestamp is required");
    }

    /**
     * Builds a message of the type specified by this builder's generic type parameter and
     * configured by this builder's properties.
     *
     * @return a message of type {@link #<M>}
     */
    public abstract M build();

    /**
     * Gets this builder.
     *
     * @return this builder
     */
    protected abstract B self(); // Subclasses must override this method to return "this"
  }
}
