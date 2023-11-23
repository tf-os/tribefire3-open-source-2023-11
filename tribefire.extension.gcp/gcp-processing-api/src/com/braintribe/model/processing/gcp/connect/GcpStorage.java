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
package com.braintribe.model.processing.gcp.connect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface GcpStorage {

	GcpBucket get(String bucketName);
	GcpBucket getOrCreate(String bucketName);
	int getBucketCount(int max);
	boolean deleteBucket(String bucketName);

	void deleteBlob(String bucketName, String key);

	InputStream openInputStream(String bucketName, String key, Long start) throws IOException;
	WritableByteChannel openWriteChannel(String bucketName, String key, String mimeType);
	ReadableByteChannel openReadChannel(String bucketName, String key, Long start) throws IOException;

}
