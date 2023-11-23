// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.processing.platformreflection.java;

import java.lang.management.ThreadInfo;

public class ExtendedThreadInfo {
	  long cpuTimeInNanos;
      long blockedCount;
      long blockedTimeInMs;
      long waitedCount;
      long waitedTimeInMs;
      boolean deltaDone;
      ThreadInfo info;

      public ExtendedThreadInfo(long cpuTimeInNanos, ThreadInfo info) {
          blockedCount = info.getBlockedCount();
          blockedTimeInMs = info.getBlockedTime();
          waitedCount = info.getWaitedCount();
          waitedTimeInMs = info.getWaitedTime();
          this.cpuTimeInNanos = cpuTimeInNanos;
          this.info = info;
      }

      void setDelta(long cpuTime, ThreadInfo info) {
          if (deltaDone) throw new IllegalStateException("setDelta already called once");
          blockedCount = info.getBlockedCount() - blockedCount;
          blockedTimeInMs = info.getBlockedTime() - blockedTimeInMs;
          waitedCount = info.getWaitedCount() - waitedCount;
          waitedTimeInMs = info.getWaitedTime() - waitedTimeInMs;
          this.cpuTimeInNanos = cpuTime - this.cpuTimeInNanos;
          deltaDone = true;
          this.info = info;
      }
}
