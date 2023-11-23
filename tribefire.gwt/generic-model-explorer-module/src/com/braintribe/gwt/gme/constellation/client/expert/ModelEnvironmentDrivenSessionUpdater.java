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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.List;

import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.processing.session.api.persistence.ModelEnvironmentDrivenGmSession;

/**
 * This expert is responsible for updating its list of sessions once the modelEnvironment is updated.
 * @author michel.docouto
 *
 */
public class ModelEnvironmentDrivenSessionUpdater {
	
	private List<? extends ModelEnvironmentDrivenGmSession> dataSessions;
	private List<? extends ModelEnvironmentDrivenGmSession> workbenchSessions;
	
	/**
	 * Configures the list of data sessions to be updated.
	 */
	@Configurable
	public void setDataSessions(List<? extends ModelEnvironmentDrivenGmSession> dataSessions) {
		this.dataSessions = dataSessions;
	}
	
	/**
	 * Configures the list of workbench sessions to be updated.
	 */
	@Configurable
	public void setWorkbenchSessions(List<? extends ModelEnvironmentDrivenGmSession> workbenchSessions) {
		this.workbenchSessions = workbenchSessions;
	}
	
	public void updateModelEnvironment(ModelEnvironment modelEnvironment) {
		if (dataSessions != null) {
			for (ModelEnvironmentDrivenGmSession session : dataSessions)
				session.configureModelEnvironment(modelEnvironment);
		}
		
		if (workbenchSessions != null) {
			ModelEnvironment workbenchModelEnvironment = ModelEnvironment.T.create();
			workbenchModelEnvironment.setDataModel(modelEnvironment.getWorkbenchModel());
			workbenchModelEnvironment.setDataAccessId(modelEnvironment.getWorkbenchModelAccessId());
			workbenchModelEnvironment.setMetaModelAccessId(modelEnvironment.getMetaModelAccessId());
			for (ModelEnvironmentDrivenGmSession session : workbenchSessions)
				session.configureModelEnvironment(workbenchModelEnvironment);
		}
	}

}
