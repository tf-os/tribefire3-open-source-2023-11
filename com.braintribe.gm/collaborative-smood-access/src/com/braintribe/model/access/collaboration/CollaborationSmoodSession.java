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
package com.braintribe.model.access.collaboration;

import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.processing.session.api.notifying.interceptors.CollectionEnhancer;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.impl.managed.AbstractManagedGmSession;
import com.braintribe.model.processing.session.impl.session.collection.CollectionEnhancingPropertyAccessInterceptor;
import com.braintribe.model.processing.smood.Smood;

/**
 * @author peter.gazdik
 */
public class CollaborationSmoodSession extends AbstractManagedGmSession {

	public CollaborationSmoodSession() {
		super(false);

		interceptors().with(ManipulationTracking.class).before(CollectionEnhancer.class).add(noChangeAssignmentIgnoringMtpai());
		interceptors().with(CollectionEnhancer.class).add(new CollectionEnhancingPropertyAccessInterceptor());
	}

	private static ManipulationTrackingPropertyAccessInterceptor noChangeAssignmentIgnoringMtpai() {
		ManipulationTrackingPropertyAccessInterceptor mtpai = new ManipulationTrackingPropertyAccessInterceptor();
		mtpai.ignoredNoChangeAssignments = true;
		return mtpai;
	}

	public void setSmood(Smood smood) {
		super.setBackup(smood);
	}

	@Override
	public ResourceAccess resources() {
		throw new UnsupportedOperationException("Method 'SmoodSession.resources' is not supported!");
	}

}
