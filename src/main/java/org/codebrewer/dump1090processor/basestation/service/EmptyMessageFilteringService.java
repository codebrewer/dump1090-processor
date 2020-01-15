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

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.integration.annotation.Filter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

@Service
@ManagedResource(
    objectName = "org.codebrewer.dump1090processor:type=Counter,name=EmptyMessageFilteringService",
    description = "Filters empty messages from the incoming stream of data")
public class EmptyMessageFilteringService {
  private final AtomicLong emptyMessageCount = new AtomicLong();

  @Filter
  Boolean filterEmptyMessage(byte[] payload) {
    if (payload == null || payload.length == 0) {
      emptyMessageCount.incrementAndGet();

      return false;
    }

    return true;
  }

  /**
   * Gets the total number of empty messages received since application startup. Empty messages
   * are occasionally seen in the feed from dump1090-mutability.
   *
   * @return the total number of empty messages received since application startup.
   */
  @ManagedAttribute(
      description = "The total number of empty messages received since application startup")
  public long getEmptyMessageCount() {
    return emptyMessageCount.get();
  }
}
