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
package com.braintribe.utils.stream.tracking.stream;

import java.io.IOException;
import java.io.InputStream;

import com.braintribe.utils.stream.BasicDelegateInputStream;
import com.braintribe.utils.stream.tracking.InputStreamTracker;
import com.braintribe.utils.stream.tracking.data.StreamInformation;

public class TrackedInputStream extends BasicDelegateInputStream {

	private InputStreamTracker tracker;
	private StreamInformation info;

	public TrackedInputStream(InputStreamTracker tracker, StreamInformation info, InputStream delegate) {
		super(delegate);
		this.tracker = tracker;
		this.info = info;
	}

	@Override
	protected void afterRead(int n) throws IOException {
		if (n == EOF) {
			info.eofReached();
			tracker.eofReached(info);
		} else {
			info.bytesTransferred += n;
		}
	}
	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (info.closed()) {
				tracker.streamClosed(info);
			}
		}
	}

	public StreamInformation getStreamInformation() {
		return info;
	}
}
