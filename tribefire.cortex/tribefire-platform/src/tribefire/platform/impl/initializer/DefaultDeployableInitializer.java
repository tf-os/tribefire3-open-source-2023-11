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
package tribefire.platform.impl.initializer;

import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public class DefaultDeployableInitializer extends SimplePersistenceInitializer {

	private List<Deployable> deployables;

	public static String defaultDeployableGlobalId(String externalId) {
		return "default:deployable/" + externalId;
	}

	@Required
	public void setDeployables(List<Deployable> deployables) {
		this.deployables = deployables;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		CloningContext cc = new SessionCreatingCloningContext(context.getSession());

		for (Deployable deployable : deployables)
			deployable.clone(cc);
	}

	private static class SessionCreatingCloningContext extends StandardCloningContext {
		private final ManagedGmSession session;

		public SessionCreatingCloningContext(ManagedGmSession session) {
			this.session = session;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			GenericEntity clone = session.create(entityType);

			if (clone instanceof Deployable)
				if (instanceToBeCloned.getGlobalId() == null) {
					Deployable deployable = (Deployable) instanceToBeCloned;
					clone.setGlobalId(defaultDeployableGlobalId(deployable.getExternalId()));
				}

			return clone;
		}

		@Override
		public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property, GenericEntity instanceToBeCloned,
				GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {

			return !property.isGlobalId() || clonedInstance.getGlobalId() == null;
		}

	}

}
