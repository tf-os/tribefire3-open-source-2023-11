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
package com.braintribe.utils.stream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.braintribe.model.generic.session.InputStreamProvider;

public class MemoryInputStreamProviders {

	public static InputStreamProvider from(String content) {
		if (content == null) {
			throw new NullPointerException("The content must not be null.");
		}
		try {
			return new ByteArrayInputStreamProvider(content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("For reasons unknown the content "+content+" could not be converted to a UTF-8 byte array.", e);
		}
	}

	public static InputStreamProvider from(byte[] content) {
		if (content == null) {
			throw new NullPointerException("The content must not be null.");
		}
		return new ByteArrayInputStreamProvider(content);
	}

	private static class ByteArrayInputStreamProvider implements InputStreamProvider {
		private final byte[] content;
		
		public ByteArrayInputStreamProvider(byte[] content) {
			this.content = content;
		}
		
		@Override
		public InputStream openInputStream() throws IOException {
			return new ByteArrayInputStream(content);
		}
	}
	
}
