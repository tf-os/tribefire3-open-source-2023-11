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
package com.braintribe.model.processing.webrpc.client;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

class StreamCache implements AutoCloseable {
	private final Map<String, CallStreamCapture> callStreamCaptures;
	private final Map<String, OutputStream> captureStreams = new HashMap<>();
	private final Map<String, StreamPipe> pipes = new HashMap<>();
	private final StreamPipeFactory streamPipeFactory;

	public StreamCache(Map<String, CallStreamCapture> callStreamCaptures, StreamPipeFactory streamPipeFactory) {
		super();
		this.callStreamCaptures = callStreamCaptures;
		this.streamPipeFactory = streamPipeFactory;
	}

	public StreamPipe acquirePipe(String bindId) {
		return pipes.computeIfAbsent(bindId, id -> streamPipeFactory.newPipe("GmWebRpcClient-stream-part-" + id));
	}

	public Pair<OutputStream, Boolean> acquireStream(String bindId) {
		OutputStream out = captureStreams.get(bindId);
		boolean fresh = false;
		if (out == null) {
			fresh = true;
			CallStreamCapture callStreamCapture = callStreamCaptures.get(bindId);

			if (callStreamCapture != null) {
				out = callStreamCapture.openStream();
			} else {
				StreamPipe pipe = acquirePipe(bindId);
				out = pipe.openOutputStream();
			}
			captureStreams.put(bindId, out);
		}

		return Pair.of(out, fresh);
	}

	@Override
	public void close() {
		for (OutputStream out : captureStreams.values()) {
			try {
				out.close();
			} catch (Exception e) {
				GmWebRpcClientBase.logger.error("error while closing stream: " + out, e);
			}
		}
	}

	public void checkPipeSatisfaction() {
		List<String> unsatisfiedPipes = null;

		for (Map.Entry<String, StreamPipe> entry : pipes.entrySet()) {
			if (!entry.getValue().wasOutputStreamOpened()) {
				if (unsatisfiedPipes == null) {
					unsatisfiedPipes = new ArrayList<>();
				}
				unsatisfiedPipes.add(entry.getKey());
			}
		}

		if (unsatisfiedPipes != null)
			throw new IllegalStateException("Missing data for instances of TransientSource with the following globalIds: " + unsatisfiedPipes);
	}
}