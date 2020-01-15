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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import org.codebrewer.dump1090processor.basestation.entity.BaseStationMessage;
import org.springframework.integration.annotation.Filter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

@Service
@ManagedResource(
    objectName = "org.codebrewer.dump1090processor:type=Counter,name=InvalidMessageFilteringService",
    description = "Filters invalid messages from the stream of processed messages")
public class InvalidMessageFilteringService {
  private final AtomicLong invalidMessageCount = new AtomicLong();

  @Filter
  Boolean filterInvalidMessage(BaseStationMessage payload) {
    if (Objects.equals(payload, BaseStationMessage.INVALID_MESSAGE)) {
      invalidMessageCount.incrementAndGet();

      return false;
    }

    return true;
  }

  /**
   * Gets the total number of invalid messages received since application startup. Invalid messages
   * are those which cannot be fully parsed from the incoming stream of CSV data.
   *
   * @return the total number of invalid messages received since application startup.
   */
  @ManagedAttribute(
      description = "The total number of invalid messages received since application startup")
  public long getInvalidMessageCount() {
    return invalidMessageCount.get();
  }
}
