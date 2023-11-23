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
package com.braintribe.model.access.collaboration.persistence.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.emptySet;

import java.io.File;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * @author peter.gazdik
 */
public class ModifiedEntitiesSupplier implements Supplier<Set<GenericEntity>> {

	private final File gmmlFile;
	private final ManagedGmSession csaSession;

	public ModifiedEntitiesSupplier(File gmmlFile, ManagedGmSession csaSession) {
		this.gmmlFile = gmmlFile;
		this.csaSession = csaSession;
	}

	@Override
	public Set<GenericEntity> get() {
		if (!gmmlFile.exists())
			return emptySet();

		return new GmmlFileProcessor().run();
	}

	private class GmmlFileProcessor {
		private final Set<GenericEntity> result = newSet();

		public Set<GenericEntity> run() {
			CsaPersistenceTools.parseGmmlFile(gmmlFile, this::visitManipulation);

			return result;
		}

		private void visitManipulation(Manipulation m) {
			switch (m.manipulationType()) {
				case INSTANTIATION:
					// This we can ignore, because later we must encounter a globalId assignment, and we handle that
					return;

				case CHANGE_VALUE:
					visitChangeValueManipulation((ChangeValueManipulation) m);
					return;

				case ACQUIRE:
				case DELETE:
				case ADD:
				case CLEAR_COLLECTION:
				case REMOVE:
					visitManipulationWithOwner((AtomicManipulation) m);
					return;

				default:
					return;
			}
		}

		private void visitChangeValueManipulation(ChangeValueManipulation m) {
			if (GenericEntity.globalId.equals(m.getOwner().getPropertyName()))
				notifyEntity(m.getNewValue());
			else
				visitManipulationWithOwner(m);
		}

		private void visitManipulationWithOwner(AtomicManipulation m) {
			EntityReference ref = (EntityReference) m.manipulatedEntity();

			if (ref.referenceType() == EntityReferenceType.global)
				notifyEntity(ref.getRefId());
		}

		private void notifyEntity(Object _globalId) {
			String globalId = (String) _globalId;

			GenericEntity entity = csaSession.findEntityByGlobalId(globalId);
			if (entity != null)
				result.add(entity);
		}

	}

}
