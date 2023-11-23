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
import java.io.OutputStream;

import com.braintribe.utils.stream.BasicDelegateOutputStream;
import com.braintribe.utils.stream.tracking.OutputStreamTracker;
import com.braintribe.utils.stream.tracking.data.StreamInformation;

public class TrackedOutputStream extends BasicDelegateOutputStream {

	private OutputStreamTracker tracker;
	private StreamInformation info;

	public TrackedOutputStream(OutputStreamTracker tracker, StreamInformation info, OutputStream delegate) {
		super(delegate);
		this.tracker = tracker;
		this.info = info;
	}

	@Override
	public void write(int b) throws IOException {
		super.write(b);
		info.bytesTransferred++;
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
		info.bytesTransferred += b.length;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		info.bytesTransferred += len;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			if (info.closed()) {
				info.eofReached();
				tracker.streamClosed(info);
			}
		}
	}

	public StreamInformation getStreamInformation() {
		return info;
	}
}
