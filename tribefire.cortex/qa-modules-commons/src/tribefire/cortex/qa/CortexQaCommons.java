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
package tribefire.cortex.qa;

import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.cortex.model.QaSerp;

/**
 * @author peter.gazdik
 */
public class CortexQaCommons {

	public static final String mainModuleName = "qa-main-module";
	public static final String subModuleName = "qa-sub-module";
	public static final String otherModuleName = "qa-other-module";
	
	public static <T> Supplier<T> qaServiceExpertSupplier(EntityType<? extends Deployable> deployableType) {
		return () -> {
			throw new UnsupportedOperationException("Unexpected deployment of: " + deployableType.getShortName());
		};
	}

	public static void createServiceProcessor(ManagedGmSession session, EntityType<? extends QaSerp> spType, String origin) {
		QaSerp sp = session.createRaw(spType, spGlobalId(spType, origin));
		sp.setName( spType.getShortName() + " von " + origin);
		sp.setExternalId(spType.getShortName() + "/" + origin);
	}

	public static String spGlobalId(EntityType<? extends QaSerp> spType, String origin) {
		return "qa.serviceProcessor." + origin + "." + spType.getShortName();
	}


}
