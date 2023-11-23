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
package tribefire.extension.antivirus.wire.space;

import java.util.List;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;
import tribefire.extension.antivirus.service.AntivirusProcessor;
import tribefire.extension.antivirus.service.HealthCheckProcessor;
import tribefire.module.wire.contract.PlatformReflectionContract;
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

	@Import
	private PlatformReflectionContract platformReflection;

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public AntivirusProcessor antivirusProcessor(ExpertContext<tribefire.extension.antivirus.model.deployment.service.AntivirusProcessor> context) {
		AntivirusProcessor bean = new AntivirusProcessor();

		tribefire.extension.antivirus.model.deployment.service.AntivirusProcessor deployable = context.getDeployable();

		List<ProviderSpecification> providerSpecifications = deployable.getProviderSpecifications();
		bean.setProviderSpecifications(providerSpecifications);
		return bean;
	}

	// -----------------------------------------------------------------------
	// PROCESSOR
	// -----------------------------------------------------------------------

	@Managed
	public HealthCheckProcessor healthCheckProcessor(
			ExpertContext<tribefire.extension.antivirus.model.deployment.service.HealthCheckProcessor> context) {

		tribefire.extension.antivirus.model.deployment.service.HealthCheckProcessor deployable = context.getDeployable();

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setProviderSpecificationType(deployable.getProviderSpecificationType());
		bean.setProviderModuleSpecificationMethod(deployable.getProviderModuleSpecificationMethod());
		bean.setProviderActivatedMethod(deployable.getProviderActivatedMethod());
		return bean;
	}

}
