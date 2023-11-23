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
package com.braintribe.model.processing.cortex.service.access;

import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.aspect.AspectConfiguration;
import com.braintribe.model.cortexapi.access.SetupAccessResponse;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class AspectSetup extends ServiceBase {

	private static final Logger logger = Logger.getLogger(AspectSetup.class);
	private List<AccessAspect> defaultAspects;
	private PersistenceGmSession session;
	private IncrementalAccess access;
	
	public AspectSetup(PersistenceGmSession session, IncrementalAccess access, List<AccessAspect> defaultAspects) {
		this.session = session;
		this.defaultAspects = defaultAspects;
		this.access = access;
	}

	
	public SetupAccessResponse run(boolean resetToDefault) {
		
		AspectConfiguration aspectConfiguration = access.getAspectConfiguration();
		if (aspectConfiguration == null) {
			notifyInfo("Create new AspectConfiguration for access: "+access.getExternalId(), logger);
			aspectConfiguration = session.create(AspectConfiguration.T);
			access.setAspectConfiguration(aspectConfiguration);
		} else {
			notifyInfo("Ensure aspects on existing AspectConfiguration of access: "+access.getExternalId(), logger);
			if (resetToDefault) {
				notifyInfo("Cleaning aspects on existing AspectConfiguration of access: "+access.getExternalId()+" (reset=true)", logger);
				aspectConfiguration.getAspects().clear();
			}
		}
		
		
		List<AccessAspect> aspects = aspectConfiguration.getAspects();
		boolean addedDefaults = false;
		for (AccessAspect defaultAspect : defaultAspects) {
			
			AccessAspect cortexInstance = findAspect(session, defaultAspect.getExternalId());
			if (cortexInstance == null){
				notifyWarning("No instance of default deployable: "+defaultAspect.getExternalId()+" found in cortex", logger);
				continue;
			}
			
			if (!aspects.contains(cortexInstance)) {
				aspects.add(cortexInstance);
				notifyInfo("Added missing default aspect: "+defaultAspect.getExternalId(), logger);
				addedDefaults = true;
			}
			
		}
		
		if (!addedDefaults) {
			notifyInfo("All default aspects were found on AspectConfiguration of access: "+access.getExternalId()+". Nothing added.", logger);
		}
		
		addNotifications(Notifications.build().add().command().refresh("Refresh Access").close().list());
		return createResponse("Ensured default aspects for access: "+access.getExternalId(), SetupAccessResponse.T);
	}


	
}
