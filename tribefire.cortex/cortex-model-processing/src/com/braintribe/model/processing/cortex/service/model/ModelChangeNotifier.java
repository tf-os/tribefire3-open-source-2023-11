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
package com.braintribe.model.processing.cortex.service.model;

import com.braintribe.model.cortexapi.model.ModelChangeResponse;
import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modelnotification.OnModelChanged;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class ModelChangeNotifier extends ServiceBase {

	private NotifyModelChanged request;
	private PersistenceGmSession session;
	
	public ModelChangeNotifier(NotifyModelChanged request, PersistenceGmSession session) {
		this.request = request;
		this.session = session;
	}
	
	public ModelChangeResponse run() {
		
		GmMetaModel model = request.getModel();
		if (model == null) {
			return createConfirmationResponse("Please provide a model.", Level.WARNING, ModelChangeResponse.T);
		}
		
		String modelName = model.getName();
		OnModelChanged onModelChanged = OnModelChanged.T.create();
		onModelChanged.setModelName(modelName);
		onModelChanged.eval(session).get();
		
		return createResponse("Notified internal caches about changes in model: "+modelName, ModelChangeResponse.T);
	}
	
	
}
