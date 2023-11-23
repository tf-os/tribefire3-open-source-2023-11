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
package tribefire.extension.opentracing.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.opentracing.service.OpentracingAspect;
import tribefire.extension.opentracing.service.OpentracingServiceProcessor;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 *
 */
@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	@Managed
	public OpentracingServiceProcessor opentracingServiceProcessor(
			ExpertContext<tribefire.extension.opentracing.model.deployment.service.OpentracingServiceProcessor> context) {

		tribefire.extension.opentracing.model.deployment.service.OpentracingServiceProcessor deployable = context.getDeployable();

		OpentracingServiceProcessor bean = new OpentracingServiceProcessor();
		bean.setDeployable(deployable);

		return bean;
	}

	@Managed
	public OpentracingAspect opentracingAspect(ExpertContext<tribefire.extension.opentracing.model.deployment.service.OpentracingAspect> context) {

		tribefire.extension.opentracing.model.deployment.service.OpentracingAspect deployable = context.getDeployable();

		OpentracingAspect bean = new OpentracingAspect();

		return bean;
	}

}
