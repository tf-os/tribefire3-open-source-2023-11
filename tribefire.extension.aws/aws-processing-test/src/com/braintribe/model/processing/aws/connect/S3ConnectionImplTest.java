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

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.model.processing.aws.service.AwsTestCredentials;
import com.braintribe.utils.RandomTools;

public class S3ConnectionImplTest {

	private static S3ConnectionImpl connection;
	private final static String bucketName = "playground-rku";

	@BeforeClass
	public static void beforeClass() {
		com.braintribe.model.aws.deployment.S3Connector deployable = com.braintribe.model.aws.deployment.S3Connector.T.create();
		deployable.setRegion(S3Region.eu_central_1);
		deployable.setAwsAccessKey(AwsTestCredentials.getAccessKey());
		deployable.setAwsSecretAccessKey(AwsTestCredentials.getSecretAccessKey());

		connection = new S3ConnectionImpl();
		connection.setS3ConnectorDeployable(deployable);
		// connection.setHttpConnectionPoolSize(10);
	}

	@AfterClass
	public static void shutdown() throws Exception {
		connection.preDestroy();
	}

	@Test
	public void testBinaryProcessorGet() throws Exception {
		String key = "test/" + RandomTools.newStandardUuid() + ".txt";
		byte[] bytes = ("Hello, world! from " + S3ConnectionImpl.class.getName()).getBytes(StandardCharsets.UTF_8);
		InputStream in = new ByteArrayInputStream(bytes);

		connection.uploadFile(bucketName, key, in, (long) bytes.length, "text/plain");
		String url = connection.generatePresignedUrl(bucketName, key, 15000l);
		System.out.println("Generated presigned URL: " + url);

		connection.deleteFile(bucketName, key);
	}

	@Test
	public void testInvalidCredentials() throws Exception {
		com.braintribe.model.aws.deployment.S3Connector deployable = com.braintribe.model.aws.deployment.S3Connector.T.create();
		deployable.setRegion(S3Region.eu_central_1);
		deployable.setAwsAccessKey(AwsTestCredentials.getAccessKey());
		deployable.setAwsSecretAccessKey("<not existing>");

		S3ConnectionImpl failConnection = new S3ConnectionImpl();
		failConnection.setS3ConnectorDeployable(deployable);
		try {
			Set<String> list = failConnection.getBucketsList();
			fail("Received unexpected list of buckets: " + list);
		} catch (Exception expected) {
			System.out.println("Received expected exception: " + expected.getMessage());
		}
	}
}
