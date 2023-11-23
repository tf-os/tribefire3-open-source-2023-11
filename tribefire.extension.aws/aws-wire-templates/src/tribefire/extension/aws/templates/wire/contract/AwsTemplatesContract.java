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
package tribefire.extension.aws.templates.wire.contract;

import com.braintribe.model.aws.deployment.S3Connector;
import com.braintribe.model.aws.deployment.processor.S3BinaryProcessor;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.aws.templates.api.S3BinaryProcessTemplateContext;

public interface AwsTemplatesContract extends WireSpace {

	S3BinaryProcessor s3StorageBinaryProcessor(S3BinaryProcessTemplateContext context);
	
	S3Connector connector(S3BinaryProcessTemplateContext context);

	ExternalResourcesContext externalResourcesContext(S3BinaryProcessTemplateContext context);
	
}
