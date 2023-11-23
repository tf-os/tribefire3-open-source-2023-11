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
package com.braintribe.model.processing.aws.connect;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.aws.api.ConnectionStatistics;

public interface S3Connector {

	Set<String> getBucketsList();

	List<String> getFilesList(String bucketName);

	void uploadFile(String bucketName, String key, InputStream in, Long fileSize, String contentType);

	void downloadFile(String bucketName, String key, OutputStream os, Long start, Long end);

	InputStream openStream(String bucketName, String key, Long start, Long end);

	void deleteFile(String bucketName, String key);

	Map<String, ConnectionStatistics> getStatisticsPerRegion();

	String generatePresignedUrl(String bucketName, String key, long ttlInMs);
}
