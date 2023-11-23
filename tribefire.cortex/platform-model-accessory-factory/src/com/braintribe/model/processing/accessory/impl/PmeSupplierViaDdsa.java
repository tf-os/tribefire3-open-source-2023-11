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
package com.braintribe.model.processing.accessory.impl;

import static java.util.Collections.emptyList;

import com.braintribe.cfg.Required;
import com.braintribe.model.accessory.GetAccessModel;
import com.braintribe.model.accessory.ModelRetrievingRequest;
import com.braintribe.model.accessory.GetModelByName;
import com.braintribe.model.accessory.GetServiceModel;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentials;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentialsSupplier;
import com.braintribe.model.processing.accessory.api.PmeKey;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * {@link PlatformModelEssentialsSupplier} which resolves the relevant models via DDSA.
 *
 * @see PmeSupplierFromCortex
 * 
 * @author peter.gazdik
 */
public class PmeSupplierViaDdsa extends PmeSupplierBase {

	private Evaluator<ServiceRequest> evaluator;

	/**
	 * The evaluator for retrieving the models for the {@link PlatformModelEssentials}.
	 * <p>
	 * It must be authorized to evaluate {@link ModelRetrievingRequest}s.
	 */
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	protected PlatformModelEssentials createNewAccessPme(String accessId, String perspective, boolean extended) {
		return createBasedOn(GetAccessModel.create(accessId, perspective, extended));
	}

	@Override
	protected PlatformModelEssentials createNewServiceDomainPme(String serviceDomainId, String perspective, boolean extended) {
		return createBasedOn(GetServiceModel.create(serviceDomainId, perspective, extended));
	}

	@Override
	public PlatformModelEssentials getForModelName(String modelName, String perspective) {
		return createBasedOn(GetModelByName.create(modelName, perspective));
	}

	private PlatformModelEssentials createBasedOn(ModelRetrievingRequest getModel) {
		GmMetaModel model = getModel.eval(evaluator).get();
		
		return new BasicPmeBuilder(model, PmeKey.create(model.getName(), getModel.getPerspective(), null, emptyList())).build();
	}

}
