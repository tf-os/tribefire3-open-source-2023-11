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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipeFactory;

public class DataCaptureStorage implements AutoCloseable {
	private final static Logger logger = Logger.getLogger(DataCaptureStorage.class);
	
	private Consumer<InputStreamProvider> receiver;
	private StreamPipe pipe;
	private StreamPipeFactory streamPipeFactory;

	private boolean notifyReceiver;

	private String callId;

	public DataCaptureStorage(String callId, StreamPipeFactory streamPipeFactory, Consumer<InputStreamProvider> receiver) {
		this.callId = callId;
		this.streamPipeFactory = streamPipeFactory;
		this.receiver = receiver;
	}
	
	public void activateNotification() {
		this.notifyReceiver = true;
	}
	
	public InputStream wrap(InputStream in) {
		if (receiver == null)
			return in;
		
		pipe = streamPipeFactory.newPipe("multipart-capture-" + callId); 
		
		return new CapturingInputStream(in, pipe.openOutputStream());
	}
	
	@Override
	public void close() {
		if (receiver != null && notifyReceiver) {
			receiver.accept(pipe::openInputStream);
		}
	}
}

class CapturingInputStream extends InputStream {
	private InputStream delegate;
	private OutputStream out;
	
	public CapturingInputStream(InputStream delegate, OutputStream out) {
		super();
		this.delegate = delegate;
		this.out = out;
	}

	@Override
	public int read() throws IOException {
		int res = delegate.read();
		
		if (res != -1)
			out.write(res);
		else
			out.close();
			
		return res;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int res = delegate.read(b, off, len);
		
		if (res != -1)
			out.write(b, off, res);
		else
			out.close();
		
		return res;
	}
}