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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

import org.junit.Test;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.tracking.data.StreamStatistics;

public class StreamTrackingTests {

	@Test
	public void testInputStreamTracking() throws Exception {
		InputStreamTracker tracker = new InputStreamTracker();

		String expected = "Hello, world!";

		ByteArrayInputStream bain = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
		try (InputStream wrapped = tracker.wrapInputStream(bain, "test", "localhost", "some meaningful context")) {
			String actual = IOTools.slurp(wrapped, "UTF-8");
			assertThat(actual).isEqualTo(expected);
		}

		TreeMap<String, StreamStatistics> stats = tracker.getStatistics();
		assertThat(stats).hasSize(1);
		StreamStatistics ss = stats.get("test");
		assertThat(ss.getTotalStreamsOpened()).isEqualTo(1);
		assertThat(ss.getStreamsFullyTransferred()).isEqualTo(1);
		assertThat(ss.getStreamsPartiallyTransferred()).isEqualTo(0);
		assertThat(ss.getTotalBytesTransferred()).isEqualTo(expected.getBytes(StandardCharsets.UTF_8).length);

	}

	@Test
	public void testOutputStreamTracking() throws Exception {

		OutputStreamTracker tracker = new OutputStreamTracker();
		String expected = "Hello, world!";

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (OutputStream wrapped = tracker.wrapOutputStream(baos, "test", "localhost", "some meaningful context")) {
			wrapped.write(expected.getBytes(StandardCharsets.UTF_8));
		}

		TreeMap<String, StreamStatistics> stats = tracker.getStatistics();
		assertThat(stats).hasSize(1);
		StreamStatistics ss = stats.get("test");
		assertThat(ss.getTotalStreamsOpened()).isEqualTo(1);
		assertThat(ss.getStreamsFullyTransferred()).isEqualTo(1);
		assertThat(ss.getStreamsPartiallyTransferred()).isEqualTo(0);
		assertThat(ss.getTotalBytesTransferred()).isEqualTo(expected.getBytes(StandardCharsets.UTF_8).length);

	}

}
