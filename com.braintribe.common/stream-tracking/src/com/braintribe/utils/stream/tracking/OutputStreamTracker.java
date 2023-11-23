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
package com.braintribe.utils.stream.tracking;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.braintribe.logging.Logger;
import com.braintribe.utils.RandomTools;
import com.braintribe.utils.stream.tracking.data.StreamInformation;
import com.braintribe.utils.stream.tracking.data.StreamStatistics;
import com.braintribe.utils.stream.tracking.data.StreamTrackingCollection;
import com.braintribe.utils.stream.tracking.stream.TrackedOutputStream;

/**
 * Utility class to keep track of OutputStreams and also to get statistics.
 */
public class OutputStreamTracker {

	private static final Logger logger = Logger.getLogger(OutputStreamTracker.class);

	private static String DEFAULT_TENANT = "default";

	private ConcurrentHashMap<String, StreamTrackingCollection> trackingDataMap = new ConcurrentHashMap<>();

	public OutputStream wrapOutputStream(OutputStream delegate, String tenant, String clientIdentifier, String context) {
		if (tenant == null) {
			tenant = DEFAULT_TENANT;
		}

		String streamId = RandomTools.newStandardUuid();

		if (logger.isDebugEnabled()) {
			logger.debug("Wrapping OutputStream " + streamId + " for tenant " + tenant + ", clientIdentifier: " + clientIdentifier + " and context "
					+ context);
		}

		StreamInformation info = new StreamInformation(tenant, streamId, clientIdentifier, context);
		TrackedOutputStream wrapper = new TrackedOutputStream(this, info, delegate);

		StreamTrackingCollection trackingData = trackingDataMap.computeIfAbsent(tenant, t -> new StreamTrackingCollection(t));
		trackingData.registerNewStream(streamId, info);

		return wrapper;
	}

	public void streamClosed(StreamInformation streamInfo) {
		StreamTrackingCollection trackingData = trackingDataMap.get(streamInfo.getTenant());
		trackingData.registerEofReached();
		trackingData.registerStreamClosed(streamInfo);
	}

	public TreeMap<String, StreamStatistics> getStatistics() {
		TreeMap<String, StreamStatistics> result = new TreeMap<>();
		List<StreamTrackingCollection> dataListClone = new ArrayList<>(trackingDataMap.values());
		for (StreamTrackingCollection d : dataListClone) {
			result.put(d.getTenant(), d.getStatistics());
		}
		return result;
	}

	public List<StreamTrackingCollection> getAllStreamTrackingCollections() {
		return new ArrayList<>(trackingDataMap.values());
	}

}
