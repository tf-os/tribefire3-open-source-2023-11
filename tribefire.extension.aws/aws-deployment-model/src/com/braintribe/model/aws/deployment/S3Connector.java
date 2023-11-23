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
package com.braintribe.model.aws.deployment;

import com.braintribe.model.aws.deployment.cloudfront.CloudFrontConfiguration;
import com.braintribe.model.deployment.connector.Connector;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface S3Connector extends Connector, HasName {

	final EntityType<S3Connector> T = EntityTypes.T(S3Connector.class);

	@Name("Access Key")
	@Mandatory
	String getAwsAccessKey();
	void setAwsAccessKey(String awsAccessKey);

	@Name("Secret Key")
	@Mandatory
	String getAwsSecretAccessKey();
	void setAwsSecretAccessKey(String awsSecretAccessKey);

	@Name("Region")
	@Mandatory
	S3Region getRegion();
	void setRegion(S3Region region);

	@Name("Streaming Pool Size")
	@Initializer("10")
	Integer getStreamingPoolSize();
	void setStreamingPoolSize(Integer streamingPoolSize);

	@Name("HTTP Pool Size")
	Integer getHttpConnectionPoolSize();
	void setHttpConnectionPoolSize(Integer httpConnectionPoolSize);

	@Name("Connection Acquisition Timeout")
	Long getConnectionAcquisitionTimeout();
	void setConnectionAcquisitionTimeout(Long connectionAcquisitionTimeout);

	@Name("Connection Timeout")
	Long getConnectionTimeout();
	void setConnectionTimeout(Long connectionTimeout);

	@Name("Socket Timeout")
	Long getSocketTimeout();
	void setSocketTimeout(Long socketTimeout);

	@Name("URL Override")
	String getUrlOverride();
	void setUrlOverride(String urlOverride);

	@Name("CloudFront Configuration")
	CloudFrontConfiguration getCloudFrontConfiguration();
	void setCloudFrontConfiguration(CloudFrontConfiguration cloudFrontConfiguration);
}
