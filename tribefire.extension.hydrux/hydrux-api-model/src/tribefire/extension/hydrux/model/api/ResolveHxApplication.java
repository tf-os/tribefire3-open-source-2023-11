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
package tribefire.extension.hydrux.model.api;

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.model.deployment.prototyping.HxMainView;
import tribefire.extension.js.model.deployment.UxModule;

/**
 * Resolves the {@link HxApplication} meta-data for given {@link #getTargetDomainId() domainId}.
 * 
 * @author peter.gazdik
 */
public interface ResolveHxApplication extends HxRequest {

	EntityType<ResolveHxApplication> T = EntityTypes.T(ResolveHxApplication.class);

	@Mandatory
	String getTargetDomainId();
	void setTargetDomainId(String targetDomainId);

	Set<String> getUseCases();
	void setUseCases(Set<String> useCases);

	/**
	 * The name of the {@link UxModule} to set on the {@link HxApplication#getView() result's view}. This is only relevant for the prototyping domain,
	 * in which case the view is {@link HxMainView}.
	 */
	String getPrototypingModule();
	void setPrototypingModule(String prototypingModule);

	@Override
	EvalContext<HxApplication> eval(Evaluator<ServiceRequest> evaluator);
}
