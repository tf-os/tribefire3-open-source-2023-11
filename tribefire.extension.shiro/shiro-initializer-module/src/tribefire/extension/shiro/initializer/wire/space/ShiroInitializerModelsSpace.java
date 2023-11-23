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
package tribefire.extension.shiro.initializer.wire.space;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.shiro.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.shiro.initializer.wire.contract.ShiroInitializerModelsContract;
import tribefire.extension.shiro.templates.api.ShiroTemplateContext;
import tribefire.extension.shiro.templates.wire.contract.ShiroMetaDataContract;

/**
 * @see {@link ShiroInitializerModelsContract}
 */
@Managed
public class ShiroInitializerModelsSpace extends AbstractInitializerSpace implements ShiroInitializerModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private ShiroMetaDataContract metaData;

	@Import
	private ShiroInitializerSpace initializer;

	@Managed
	@Override
	public GmMetaModel configuredServiceModel() {
		ShiroTemplateContext context = initializer.defaultContext();
		return metaData.serviceModel(context);
	}

	@Managed
	@Override
	public GmMetaModel configuredDeploymentModel() {
		ShiroTemplateContext context = initializer.defaultContext();
		return metaData.deploymentModel(context);
	}
}
