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
package com.braintribe.model.processing.gcp.service;

import java.io.InputStream;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.gcp.deployment.GcpConnector;
import com.braintribe.model.processing.gcp.connect.GcpBucket;
import com.braintribe.model.processing.gcp.connect.GcpStorageConnectorImpl;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.IOTools;
import com.google.cloud.storage.Bucket;

@Category(SpecialEnvironment.class)
public class ManualTest {

	@Test
	public void listBuckets() throws Exception {
	
		GcpConnector deployable = GcpConnector.T.create();
		
		boolean useJson = true;
		if (useJson) {
			InputStream inputStream = ManualTest.class.getClassLoader().getResourceAsStream("com/braintribe/model/processing/gcp/service/gcp-test-service-account.json");
			String jsonCredentials = IOTools.slurp(inputStream, "UTF-8");
			deployable.setJsonCredentials(jsonCredentials);
		} else {
			deployable.setPrivateKeyId("f01bbbc27a379d5f0df2e7ac6aef2715563988ff");
			deployable.setPrivateKey("-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC/zXf/HYsoyMFl\nX9/nR+ZA5P6mz5WAYfEqPsr0E2VgvN8/uMvcIaiT/n6hq7OcO73odGx62aK5fO9l\ny4I11mgBrupcsUz93dn8ZPCNDPljdY07hI210hWcUc08mDm94Jy8ZxxNiN5XW+Ao\ngBZd/hyhBNL3UM1yyAuOVzKj2uWjFSTNCXaSGWcm3OQu74ODLJ935X8R3htOpM5m\nBNPFj85CLkJU+gEyfl7RhLYUGnr/8MyqUzZIQsbTWpwdJN1gTvGa8oJV9YAQrGh7\nlEYeB+4Sxtvvil5ycMxdSVr75OIyZ4xw1AIvHNmxgp3DKM1oxNzQgtk2tSnQRNOu\nHTnCyC69AgMBAAECggEALGcg1Iq+YJ65RwK/DwfyIivhWDTOY35JGoKW8ZJb8d1H\nwbTCXR/dqwVLw5nAZvbdrCbnxjkEFvT5ZxOJ/XS5d0pLL1JMhKkLggbMOZs7UO8x\n1wmE1QbhweKeJN0I4RBSuLsWLkz1TDwU1MtyYigl8JDxVhf6uAvfwQAm1RAkAXDI\n7duN0bdqF+7ToywLNOrb69lkEOYaY1lnT8RlFOjNcWDHvxctllQ2aICx1tjC1uSx\n9KOEV8Qo5Uw/wgkiwUoZssjjCIDcjdYlYbEpxRVAghrakShJrrKAK4tRWYA1hhfo\nR9cQqTEq+fsqH4V9tCyqemtJubZMjoGiWbk7Ox26gQKBgQDpBI5/O5f6Hx1iN2D5\nMROZ1zZmsDnaL+iN1hRRvmQYhkrKkVlsrfQpC3JxqyvaFvrb09HxQuQvJrHa2Dlu\n1j3k8bK+HhXEIJjBD86TltaMRN5n74b6aDE8YJava3GUNVUg8NL+U8xbqMB1WiH5\npRDH0a+3Bec5UF9NaZw6DjPNCQKBgQDSuEYf8nXjSeAdKqd49XZ8O/ZvvvKO2OJF\n8SJNobKFTgpTHic3fL9zC4laN8DoUrrNhEYhIH48Q8GTEMzYH+LVgS6o/sbhA2bR\neMFjDR1x4OJeEJF7i20joHGo2cnqD3kr284d+NhrfPHfm5XS2+SzFDCtEuedrCKG\nAipjuUm1FQKBgBJvctqsG1UFmyyHFxv//v3/4eTz8k/J1ItMMaVZ+89Y6JfoMfwA\nMz4AEtjdYwXJJaW4t3ZwBV+Znx6iVsDnIFR5rtuNw8kVjyBPINfaajUEydfNQo+T\nKLoPJjzQsla4dXi6tOa8OIHT8w4VyHRY0+ALPeysAeQt8KxOO7b4Iq8JAoGBAMW/\nBJ/qa1M2+sikRLGwuNZxdxz69qmzQvUcC3MK8XtD91B1FZxux0Nxah5t/+yF5qQD\nI8Tmt1BazYfz1ihkE/0E1ALnxT9Yb97YEbOglSdPQV/A8KYd+/fZE5C/gUrT5oSQ\nK+fYpVCJ2npbichtbzvXEK769Lz00xkrYxhew5rZAoGAUC2hQetLLthpr9AwlQ/N\njF9CQF20nHuLoZ2CQWExAbIfxFjPYIneUJ1h6ZiD+8MOQ4GNrzrfEHqvsEJhmw0f\nSoqXi6viwrlOG0KYv4vNlIl0wRfZlZkAn75hEcTQXrn3chtELq4kt+wsDG9/QjIN\nfbGcpxjFCrX7NlyATVKrFzc=\n-----END PRIVATE KEY-----\n");
			deployable.setClientId("115481586852980143850");
			deployable.setClientEmail("gcp-storage-test@tribefire-staging.iam.gserviceaccount.com");
			deployable.setTokenServerUri("https://oauth2.googleapis.com/token");
			deployable.setProjectId("tribefire-staging");
		}
		 
		GcpStorageConnectorImpl connection = new GcpStorageConnectorImpl();
		connection.setConnector(deployable);

		String bucketName = "documents-staging-unique-11111111";
		
		try {
			GcpBucket bucket = connection.ensureBucket(bucketName);
			System.out.printf("Bucket %s created or retrieved.%n", bucket.getName());
		} catch(Exception e) {
			e.printStackTrace();
		}

		
		boolean deleted = connection.deleteBucket(bucketName);
		
		System.out.println("Bucket "+bucketName+" deleted: "+deleted);

	}
	
}
/*
credentials = ServiceAccountCredentials.newBuilder()
		.setPrivateKeyId("f01bbbc27a379d5f0df2e7ac6aef2715563988ff")
		.setPrivateKey(privKey)
		.setClientEmail("gcp-storage-test@tribefire-staging.iam.gserviceaccount.com")
		.setClientId("115481586852980143850")
		.setTokenServerUri(new URI("https://oauth2.googleapis.com/token"))
		.setProjectId("tribefire-staging").build();
*/