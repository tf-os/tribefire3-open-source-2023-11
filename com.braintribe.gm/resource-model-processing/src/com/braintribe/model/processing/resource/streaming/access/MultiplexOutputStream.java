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
package com.braintribe.model.processing.resource.streaming.access;

import java.io.IOException;
import java.io.OutputStream;

public class MultiplexOutputStream extends OutputStream {

	private OutputStream delegates[];
	
	public MultiplexOutputStream(OutputStream... delegates) {
		super();
		this.delegates = delegates;
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream delegate: delegates)
			delegate.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		for (OutputStream delegate: delegates)
			delegate.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream delegate: delegates)
			delegate.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		for (OutputStream delegate: delegates)
			delegate.flush();
	}

	@Override
	public void close() throws IOException {
		for (OutputStream delegate: delegates)
			delegate.close();
	}
}
