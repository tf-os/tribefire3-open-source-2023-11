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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.braintribe.common.lcd.Numbers;

/**
 * Stores all tracked streams for a specific tenant.
 */
public class StreamTrackingCollection {

	private String tenant;

	public StreamTrackingCollection(String tenant) {
		this.tenant = tenant;
	}

	private ConcurrentHashMap<String, StreamInformation> openStreams = new ConcurrentHashMap<>();

	private AtomicLong totalStreamsOpened = new AtomicLong(0);
	private AtomicLong streamsClosed = new AtomicLong(0);

	private AtomicLong totalBytesTransferred = new AtomicLong(0);
	private AtomicLong totalDurations = new AtomicLong(0);

	private transient Long maxDuration = 0l;
	private transient Double maxThroughput = -1d;
	private transient Double minThroughput = -1d;

	private AtomicLong streamsFullyTransferred = new AtomicLong(0);
	private AtomicLong streamsPartiallyTransferred = new AtomicLong(0);

	private long lastActivity = -1;

	public void registerNewStream(String streamId, StreamInformation info) {
		openStreams.put(streamId, info);
		totalStreamsOpened.incrementAndGet();
		lastActivity = System.currentTimeMillis();
	}

	public void registerStreamClosed(StreamInformation streamInfo) {
		openStreams.remove(streamInfo.getStreamId());
		streamsClosed.incrementAndGet();
		lastActivity = System.currentTimeMillis();

		if (!streamInfo.readFully()) {
			streamsPartiallyTransferred.incrementAndGet();
		}

		updateStatistics(streamInfo);
	}

	public long getTimeSinceLastActivity() {
		if (lastActivity == -1) {
			return -1;
		}
		return System.currentTimeMillis() - lastActivity;
	}

	private void updateStatistics(StreamInformation info) {
		long bytesRead = info.getBytesTransferred();
		long duration = info.duration();

		totalBytesTransferred.addAndGet(bytesRead);
		totalDurations.addAndGet(duration);

		if (duration > maxDuration) {
			maxDuration = duration;
		}

		double durationInSeconds = ((double) duration) / ((double) Numbers.NANOSECONDS_PER_SECOND);
		if (durationInSeconds != 0d) {
			double throughput = bytesRead / durationInSeconds;
			if (throughput > maxThroughput) {
				maxThroughput = throughput;
			}
			if ((throughput > 0) && (throughput < minThroughput || minThroughput == -1d)) {
				minThroughput = throughput;
			}
		}
	}

	public void registerEofReached() {
		streamsFullyTransferred.incrementAndGet();
	}

	public StreamStatistics getStatistics() {
		StreamStatistics stats = new StreamStatistics();
		stats.setTenant(tenant);
		stats.setOpenConnections(openStreams.size());

		Map<String, StreamInformation> streamsCopy = new HashMap<>(openStreams);
		long maxOpenAge = streamsCopy.values().stream().mapToLong(StreamInformation::age).max().orElse(-1l);
		double avgOpenAge = streamsCopy.values().stream().mapToLong(StreamInformation::age).average().orElse(-1d);
		stats.setOldestConnectionDuration(maxOpenAge);
		stats.setAverageOpenConnectionsAge(avgOpenAge);

		if (streamsClosed.get() != 0) {
			stats.setAverageThroughput(totalBytesTransferred.doubleValue() / (totalDurations.doubleValue() / Numbers.NANOSECONDS_PER_SECOND));
			stats.setAverageFileSize(totalBytesTransferred.doubleValue() / streamsClosed.doubleValue());
		}

		stats.setTotalStreamsOpened(totalStreamsOpened.get());
		stats.setStreamsClosed(streamsClosed.get());

		stats.setTotalBytesTransferred(totalBytesTransferred.get());
		stats.setTotalDurations(totalDurations.get());

		stats.setStreamsFullyTransferred(streamsFullyTransferred.get());
		stats.setStreamsPartiallyTransferred(streamsPartiallyTransferred.get());

		stats.setMaxDuration(maxDuration);
		stats.setMaxThroughput(maxThroughput);
		stats.setMinThroughput(minThroughput);

		if (lastActivity != -1) {
			stats.setLastActivity(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastActivity), ZoneId.systemDefault()));
		}

		Map<String, Integer> topStreamersById = new HashMap<>();
		for (StreamInformation info : streamsCopy.values()) {
			String id = info.getClientIdentifier();
			int count = topStreamersById.computeIfAbsent(id, i -> Integer.valueOf(0));
			topStreamersById.put(id, count + 1);
		}
		TreeMap<Integer, List<String>> topStreamersByCount = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : topStreamersById.entrySet()) {
			List<String> list = topStreamersByCount.computeIfAbsent(entry.getValue(), i -> new ArrayList<>());
			list.add(entry.getKey());
		}
		stats.setTopStreamers(topStreamersByCount);

		return stats;
	}

	public static StreamStatistics getCombinedStatistics(Collection<StreamTrackingCollection> streamTrackingDataCollection) {
		StreamStatistics combinedStats = new StreamStatistics();
		combinedStats.setTenant("Combined");

		int count = streamTrackingDataCollection.size();

		long combinedMaxOpenAge = 0;
		double combinedAverageAgeSum = 0d;

		long combinedTotalBytesTransferred = 0L;
		long combinedTotalDurations = 0L;
		long combinedStreamsClosed = 0;

		long combinedTotalStreamsOpened = 0l;

		long combinedStreamsFullyTransferred = 0l;
		long combinedStreamsPartiallyTransferred = 0l;

		Long combinedMaxDuration = null;
		Double combinedMinThroughput = null;
		Double combinedMaxThroughput = null;

		long combinedLastActivity = -1;

		Map<String, Integer> topStreamersById = new HashMap<>();

		for (StreamTrackingCollection st : streamTrackingDataCollection) {

			Map<String, StreamInformation> streamsCopy = new HashMap<>(st.openStreams);

			long maxOpenAge = streamsCopy.values().stream().mapToLong(StreamInformation::age).max().orElse(-1l);
			if (maxOpenAge > combinedMaxOpenAge) {
				combinedMaxOpenAge = maxOpenAge;
			}
			combinedAverageAgeSum += streamsCopy.values().stream().mapToLong(StreamInformation::age).average().orElse(-1d);

			combinedTotalBytesTransferred += st.totalBytesTransferred.get();
			combinedTotalDurations += st.totalDurations.get();
			combinedStreamsClosed += st.streamsClosed.get();

			combinedTotalStreamsOpened += st.totalStreamsOpened.get();

			combinedStreamsFullyTransferred += st.streamsFullyTransferred.get();
			combinedStreamsPartiallyTransferred += st.streamsPartiallyTransferred.get();

			if (combinedMaxDuration == null || (st.maxDuration != null && st.maxDuration > combinedMaxDuration)) {
				combinedMaxDuration = st.maxDuration;
			}
			if (combinedMinThroughput == null || (st.minThroughput != null && st.minThroughput < combinedMinThroughput)) {
				combinedMinThroughput = st.minThroughput;
			}
			if (combinedMaxThroughput == null || (st.minThroughput != null && st.minThroughput > combinedMaxThroughput)) {
				combinedMaxThroughput = st.minThroughput;
			}

			if (st.lastActivity > combinedLastActivity) {
				combinedLastActivity = st.lastActivity;
			}

			for (StreamInformation info : streamsCopy.values()) {
				String id = info.getClientIdentifier();
				int currentCount = topStreamersById.computeIfAbsent(id, i -> Integer.valueOf(0));
				topStreamersById.put(id, currentCount + 1);
			}

		}

		combinedStats.setOldestConnectionDuration(combinedMaxOpenAge);
		combinedStats.setAverageOpenConnectionsAge(combinedAverageAgeSum / count);

		if (combinedStreamsClosed != 0) {
			combinedStats.setAverageThroughput(combinedTotalBytesTransferred / ((double) combinedTotalDurations / Numbers.NANOSECONDS_PER_SECOND));
			combinedStats.setAverageFileSize(combinedTotalBytesTransferred / (double) combinedStreamsClosed);
		}

		combinedStats.setTotalStreamsOpened(combinedTotalStreamsOpened);
		combinedStats.setStreamsClosed(combinedStreamsClosed);

		combinedStats.setTotalBytesTransferred(combinedTotalBytesTransferred);
		combinedStats.setTotalDurations(combinedTotalDurations);

		combinedStats.setStreamsFullyTransferred(combinedStreamsFullyTransferred);
		combinedStats.setStreamsPartiallyTransferred(combinedStreamsPartiallyTransferred);

		combinedStats.setMaxDuration(combinedMaxDuration);
		combinedStats.setMinThroughput(combinedMinThroughput);
		combinedStats.setMaxThroughput(combinedMaxThroughput);

		if (combinedLastActivity != -1) {
			combinedStats.setLastActivity(ZonedDateTime.ofInstant(Instant.ofEpochMilli(combinedLastActivity), ZoneId.systemDefault()));
		}

		TreeMap<Integer, List<String>> topStreamersByCount = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : topStreamersById.entrySet()) {
			List<String> list = topStreamersByCount.computeIfAbsent(entry.getValue(), i -> new ArrayList<>());
			list.add(entry.getKey());
		}
		combinedStats.setTopStreamers(topStreamersByCount);

		return combinedStats;
	}

	public String getTenant() {
		return tenant;
	}
}
