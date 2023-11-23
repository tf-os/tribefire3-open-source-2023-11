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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.stream.StreamProviders;

public class FileStreamProviders extends StreamProviders {
	
	public static InputStreamProvider from(File file) {
		return new FileInputStreamProvider(file);
	}
	
	private static class FileInputStreamProvider implements InputStreamProvider {
		private final File file;
		
		public FileInputStreamProvider(File file) {
			this.file = file;
		}
		
		@Override
		public InputStream openInputStream() throws IOException {
			return new FileInputStream(file);
		}
	}
	
}
