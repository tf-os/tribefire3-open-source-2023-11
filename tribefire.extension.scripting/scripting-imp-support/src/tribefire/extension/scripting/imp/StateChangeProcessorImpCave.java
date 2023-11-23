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
package tribefire.extension.scripting.imp;

import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;
import com.braintribe.product.rat.imp.impl.deployable.BasicDeployableImp;

import tribefire.extension.scripting.statechange.model.deployment.ScriptedStateChangeProcessor;

/**
 * An {@link AbstractImpCave} specialized in {@link StateChangeProcessor}
 */
public class StateChangeProcessorImpCave extends AbstractImpCave<StateChangeProcessor, BasicDeployableImp<StateChangeProcessor>> {

	public StateChangeProcessorImpCave(PersistenceGmSession session) {
		super(session, "externalId", StateChangeProcessor.T);
	}

	@Override
	protected BasicDeployableImp<StateChangeProcessor> buildImp(StateChangeProcessor instance) {
		return new BasicDeployableImp<StateChangeProcessor>(session(), instance);
	}

	public ScriptedStateChangeProcessorImp<ScriptedStateChangeProcessor> createScriptedStateChangeProcessor(String name, String externalId) {
		ScriptedStateChangeProcessor scriptedStateChangeProcessor = session().create(ScriptedStateChangeProcessor.T);
		scriptedStateChangeProcessor.setExternalId(externalId);
		scriptedStateChangeProcessor.setName(name);
		return new ScriptedStateChangeProcessorImp<ScriptedStateChangeProcessor>(session(), scriptedStateChangeProcessor);
	}
}
