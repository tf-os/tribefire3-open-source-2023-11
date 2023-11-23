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
package tribefire.extension.antivirus.model.service.request;

import java.util.List;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Min;
import com.braintribe.model.generic.annotation.meta.MinLength;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;
import tribefire.extension.antivirus.model.service.result.VirusInformation;

public interface ScanForVirus extends AntivirusRequest {

	EntityType<ScanForVirus> T = EntityTypes.T(ScanForVirus.class);

	@Override
	EvalContext<? extends VirusInformation> eval(Evaluator<ServiceRequest> evaluator);

	String resources = "resources";
	String providerSpecifications = "providerSpecifications";
	String parallelExecution = "parallelExecution";

	@Name("Resources")
	@Description("A list resources to be virus scanned")
	@MinLength(1)
	@Mandatory
	List<Resource> getResources();
	void setResources(List<Resource> resources);

	@Name("Provider Specifications")
	@Description("Optional list of Provider Specifications. If not specified the Provider Specification(s) from the module will be used")
	List<ProviderSpecification> getProviderSpecifications();
	void setProviderSpecifications(List<ProviderSpecification> providerSpecifications);

	@Name("Parallel Execution")
	@Description("Number of parallel virus scanning (0 means sequentially)")
	@Min("0")
	@Mandatory
	@Initializer("0")
	int getParallelExecution();
	void setParallelExecution(int parallelExecution);
}
