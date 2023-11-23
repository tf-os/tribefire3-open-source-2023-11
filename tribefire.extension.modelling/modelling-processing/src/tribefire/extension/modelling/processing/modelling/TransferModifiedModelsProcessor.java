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
package tribefire.extension.modelling.processing.modelling;

import com.braintribe.model.processing.accessrequest.api.AbstractStatefulAccessRequestProcessor;

import tribefire.extension.modelling.commons.ModellingConstants;
import tribefire.extension.modelling.model.api.data.ModelSelection;
import tribefire.extension.modelling.model.api.request.ModellingResponse;
import tribefire.extension.modelling.model.api.request.TransferModifiedModels;

public class TransferModifiedModelsProcessor
		extends AbstractStatefulAccessRequestProcessor<TransferModifiedModels, ModellingResponse>
		implements ModellingConstants {

	private ModellingProcessorConfig config;

	public TransferModifiedModelsProcessor(ModellingProcessorConfig config) {
		this.config = config;
	}
	
	@Override
	public ModellingResponse process() {
		
		ModelSelection models = request().getModels();
		String transferOperation = request().getTransferOperation();
		
		return null;
	}

}
