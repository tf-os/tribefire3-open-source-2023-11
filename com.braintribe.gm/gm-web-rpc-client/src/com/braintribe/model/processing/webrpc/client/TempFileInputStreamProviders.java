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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.braintribe.model.generic.session.DuplexStreamProvider;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.stream.BasicDelegateInputStream;
import com.braintribe.utils.stream.BasicDelegateOutputStream;

public class TempFileInputStreamProviders {

    public static DuplexStreamProvider create(String name) throws IOException {
    	
    	File tempFile = File.createTempFile("TempFileInputStreamProvider", name);
        TempFileStreamProvider provider = new TempFileStreamProvider(tempFile);
        FileTools.deleteFileWhenOrphaned(tempFile);
        return provider;
    }
    
    private static class TempFileStreamProvider implements DuplexStreamProvider {
    	private File file;
    	
    	public TempFileStreamProvider(File file) {
			super();
			this.file = file;
		}

		@Override
    	public InputStream openInputStream() throws IOException {
			// us an anonymous override in order to keep a back reference to the provider
    		return new BasicDelegateInputStream(new FileInputStream(file)) {
    			// Need to hold a reference to the file so that it won't get deleted while the stream is still in use (see FileTools.deleteFileWhenOrphaned)
    			@SuppressWarnings("unused")
				private File streamFile = file;
    		};
    	}
		
		@Override
		public OutputStream openOutputStream() throws IOException {
			// us an anonymous override in order to keep a back reference to the provider
			return new BasicDelegateOutputStream(new FileOutputStream(file)) {
    			// Need to hold a reference to the file so that it won't get deleted while the stream is still in use (see FileTools.deleteFileWhenOrphaned)
    			@SuppressWarnings("unused")
    			private File streamFile = file;
			};
		}
    }
}
