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
package com.braintribe.utils.stream.tracking.data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;

public class StreamStatistics {

	private String tenant;

	private int openConnections;
	private long oldestConnectionDuration;
	private double averageOpenConnectionsAge;

	private long totalStreamsOpened;
	private long streamsClosed;

	private long totalBytesTransferred;
	private long totalDurations;

	private double averageFileSize;
	private long maxDuration = 0l;
	private double maxThroughput;
	private double minThroughput;
	private double averageThroughput;
	private ZonedDateTime lastActivity;

	private long streamsFullyTransferred;
	private long streamsPartiallyTransferred;

	private SortedMap<Integer, List<String>> topStreamers;

	public int getOpenConnections() {
		return openConnections;
	}

	public void setOpenConnections(int openConnections) {
		this.openConnections = openConnections;
	}

	public long getOldestConnectionDuration() {
		return oldestConnectionDuration;
	}

	public void setOldestConnectionDuration(long oldestConnectionDuration) {
		this.oldestConnectionDuration = oldestConnectionDuration;
	}

	public double getAverageOpenConnectionsAge() {
		return averageOpenConnectionsAge;
	}

	public void setAverageOpenConnectionsAge(double averageOpenConnectionsAge) {
		this.averageOpenConnectionsAge = averageOpenConnectionsAge;
	}

	public long getTotalStreamsOpened() {
		return totalStreamsOpened;
	}

	public void setTotalStreamsOpened(long totalStreamsOpened) {
		this.totalStreamsOpened = totalStreamsOpened;
	}

	public long getStreamsClosed() {
		return streamsClosed;
	}

	public void setStreamsClosed(long inputStreamsClosed) {
		this.streamsClosed = inputStreamsClosed;
	}

	public long getTotalBytesTransferred() {
		return totalBytesTransferred;
	}

	public void setTotalBytesTransferred(long totalBytesTransferred) {
		this.totalBytesTransferred = totalBytesTransferred;
	}

	public long getTotalDurations() {
		return totalDurations;
	}

	public void setTotalDurations(long totalDurations) {
		this.totalDurations = totalDurations;
	}

	public long getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(long maxDuration) {
		this.maxDuration = maxDuration;
	}

	public double getMaxThroughput() {
		return maxThroughput;
	}

	public void setMaxThroughput(double maxThroughput) {
		this.maxThroughput = maxThroughput;
	}

	public double getMinThroughput() {
		return minThroughput;
	}

	public void setMinThroughput(double minThroughput) {
		this.minThroughput = minThroughput;
	}

	public double getAverageThroughput() {
		return averageThroughput;
	}

	public void setAverageThroughput(double averageThroughput) {
		this.averageThroughput = averageThroughput;
	}

	public SortedMap<Integer, List<String>> getTopStreamers() {
		return topStreamers;
	}

	public void setTopStreamers(SortedMap<Integer, List<String>> topStreamers) {
		this.topStreamers = topStreamers;
	}

	public double getAverageFileSize() {
		return averageFileSize;
	}

	public void setAverageFileSize(double averageFileSize) {
		this.averageFileSize = averageFileSize;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public ZonedDateTime getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(ZonedDateTime lastActivity) {
		this.lastActivity = lastActivity;
	}

	public long getStreamsFullyTransferred() {
		return streamsFullyTransferred;
	}

	public void setStreamsFullyTransferred(long streamsFullyTransferred) {
		this.streamsFullyTransferred = streamsFullyTransferred;
	}

	public long getStreamsPartiallyTransferred() {
		return streamsPartiallyTransferred;
	}

	public void setStreamsPartiallyTransferred(long streamsPartiallyTransferred) {
		this.streamsPartiallyTransferred = streamsPartiallyTransferred;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Open streams: " + openConnections + "\n");
		sb.append("Oldest open stream: " + StringTools.prettyPrintDuration(oldestConnectionDuration / Numbers.NANOSECONDS_PER_MILLISECOND, true, null)
				+ "\n");
		sb.append("Average age of open stream: "
				+ StringTools.prettyPrintMilliseconds(averageOpenConnectionsAge / Numbers.NANOSECONDS_PER_MILLISECOND, true, null) + "\n");
		sb.append("Total streams opened: " + totalStreamsOpened + "\n");
		sb.append("Total streams closed: " + streamsClosed + "\n");

		sb.append("Total bytes transferred: " + StringTools.prettyPrintBytesDecimal(totalBytesTransferred) + "\n");
		sb.append("Average file size: " + StringTools.prettyPrintBytesDecimal((long) averageFileSize) + "\n");
		sb.append("Maximum duration: " + StringTools.prettyPrintMilliseconds(maxDuration / Numbers.NANOSECONDS_PER_MILLISECOND, true, null) + "\n");
		sb.append("Maximum throughput: " + StringTools.prettyPrintBytesDecimal((long) maxThroughput) + " / s\n");
		sb.append("Minimum throughput: " + StringTools.prettyPrintBytesDecimal((long) minThroughput) + " / s\n");
		sb.append("Average throughput: " + StringTools.prettyPrintBytesDecimal((long) averageThroughput) + " / s\n");
		if (!topStreamers.isEmpty()) {
			sb.append("Top streamers:\n");
			for (Map.Entry<Integer, List<String>> entry : topStreamers.entrySet()) {
				sb.append(entry.getKey() + ": " + entry.getValue() + "\n");
			}
		}
		if (lastActivity != null) {
			sb.append("Last activity: " + DateTools.ISO8601_DATE_WITH_MS_FORMAT_AND_Z.format(lastActivity) + "\n");
		} else {
			sb.append("Last activity: No activity yet recorded.\n");
		}
		sb.append("Total streams fully transferred: " + streamsFullyTransferred + "\n");
		sb.append("Total streams partially transferred: " + streamsPartiallyTransferred + "\n");

		return sb.toString();
	}
}
