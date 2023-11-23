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
package com.braintribe.model.processing.webrpc.server.multipart;

import java.io.IOException;
import java.io.OutputStream;

import com.braintribe.model.generic.session.OutputStreamProvider;
import com.braintribe.utils.stream.DecoratorOutputStream;

public class PartOutputStreamProvider  implements OutputStreamProvider {
	
	public enum State {
		initial, opened, closed
	}
	
	private PartAcquiring partAcquiring;
	private String bindId;
	private State state = State.initial;
	private OutputStream out;
	
	public PartOutputStreamProvider(PartAcquiring partAcquiring, String bindId) {
		super();
		this.partAcquiring = partAcquiring;
		this.bindId = bindId;
	}
	
	@Override
	public OutputStream openOutputStream() throws IOException {
		return out = new PartDecoratorOutputStream(partAcquiring.openPartStream(bindId));
	}
	
	private class PartDecoratorOutputStream extends DecoratorOutputStream {
		public PartDecoratorOutputStream(OutputStream delegate) {
			super(delegate);
			state = State.opened;
		}
		
		@Override
		public void close() throws IOException {
			state = State.closed;
			super.close();
		}
	}
	
	public State getState() {
		return state;
	}
	
	public String getBindId() {
		return bindId;
	}
	
	public OutputStream getOut() {
		return out;
	}
}
