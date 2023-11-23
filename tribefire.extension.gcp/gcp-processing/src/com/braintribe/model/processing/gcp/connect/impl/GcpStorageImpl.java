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
package com.braintribe.model.processing.gcp.connect.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.stream.StreamSupport;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.gcp.connect.GcpBucket;
import com.braintribe.model.processing.gcp.connect.GcpStorage;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;

public class GcpStorageImpl implements GcpStorage {

	private final static Logger logger = Logger.getLogger(GcpStorageImpl.class);

	private Storage storage;

	public GcpStorageImpl(Storage storage) {
		this.storage = storage;
	}

	@Override
	public GcpBucket get(String bucketName) {
		Bucket bucket = null;
		try {
			bucket = storage.get(bucketName);
			if (bucket != null) {
				return new GcpBucketImpl(bucket);
			}
		} catch (Exception e) {
			logger.error("Could not lookup bucket " + bucketName);
		}
		return null;
	}

	@Override
	public GcpBucket getOrCreate(String bucketName) {

		Bucket bucket = null;
		try {
			bucket = storage.get(bucketName);
			if (bucket != null) {
				return new GcpBucketImpl(bucket);
			}
		} catch (Exception e) {
			logger.error("Could not lookup bucket " + bucketName);
		}
		bucket = storage.create(BucketInfo.of(bucketName));
		return new GcpBucketImpl(bucket);
	}

	@Override
	public void deleteBlob(String bucketName, String key) {
		BlobId blobId = BlobId.of(bucketName, key);
		storage.delete(blobId);
	}

	@Override
	public InputStream openInputStream(String bucketName, String key, Long start) throws IOException {
		Blob blob = storage.get(BlobId.of(bucketName, key));
		if (blob == null) {
			throw new NotFoundException("Could not find the blob " + key + " in bucket " + bucketName);
		}
		ReadChannel reader = blob.reader();
		if (start != null) {
			reader.seek(start);
		}
		InputStream rawInputStream = Channels.newInputStream(reader);
		return rawInputStream;
	}

	@Override
	public WritableByteChannel openWriteChannel(String bucketName, String key, String mimeType) {
		BlobId blobId = BlobId.of(bucketName, key);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(mimeType).build();
		WriteChannel writer = storage.writer(blobInfo);
		return writer;
	}

	@Override
	public ReadableByteChannel openReadChannel(String bucketName, String key, Long start) throws IOException {
		Blob blob = storage.get(BlobId.of(bucketName, key));
		if (blob == null) {
			throw new NotFoundException("Could not find the blob " + key + " in bucket " + bucketName);
		}
		ReadChannel reader = blob.reader();
		if (start != null && start > 0) {
			reader.seek(start);
		}
		return reader;
	}

	@Override
	public boolean deleteBucket(String bucketName) {
		GcpBucket bucket = get(bucketName);
		if (bucket == null) {
			return true;
		}
		return bucket.delete();
	}

	@Override
	public int getBucketCount(int max) {
		int totalCount = 0;

		Page<Bucket> buckets = storage.list();
		do {
			Iterable<Bucket> blobIterator = buckets.iterateAll();
			long count = StreamSupport.stream(blobIterator.spliterator(), false).count();
			totalCount += (int) count;

			buckets = buckets.getNextPage();

			if (totalCount > max) {
				break;
			}
		} while (buckets != null);
		return totalCount;
	}

}
