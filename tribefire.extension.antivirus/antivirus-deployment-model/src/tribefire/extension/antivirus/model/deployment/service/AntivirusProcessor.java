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
package tribefire.extension.antivirus.model.deployment.service;

import java.util.List;

import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;

/**
 * Antivirus Processor
 * 
 *
 */
public interface AntivirusProcessor extends AccessRequestProcessor {

	final EntityType<AntivirusProcessor> T = EntityTypes.T(AntivirusProcessor.class);

	String providerSpecifications = "providerSpecifications";

	@Name("Provider Specifications")
	@Description("Optional list of Provider Specifications. If not specified the Provider Specification(s) from the requests will be used")
	List<ProviderSpecification> getProviderSpecifications();
	void setProviderSpecifications(List<ProviderSpecification> providerSpecifications);

	String getAntivirusContext();
	void setAntivirusContext(String antivirusContext);
}
