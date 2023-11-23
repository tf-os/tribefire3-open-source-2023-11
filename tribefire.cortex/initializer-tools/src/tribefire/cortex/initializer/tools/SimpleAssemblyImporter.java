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
package tribefire.cortex.initializer.tools;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * Straight-forward assembly importer that imports everything that isn't in the cortex yet. 
 * 
 * @author peter.gazdik
 */
public class SimpleAssemblyImporter {

	public static <T> T importAssembly(T assembly, ManagedGmSession session, String globalIdPrefix) {
		AssImportingCloningContext cc = new AssImportingCloningContext(session, globalIdPrefix);

		T result = BaseType.INSTANCE.clone(cc, assembly, StrategyOnCriterionMatch.skip);

		cc.ensureStableGlobalIds();

		return result;
	}

	private static class AssImportingCloningContext extends StandardCloningContext {

		private final ManagedGmSession session;
		private final String globalIdPrefix;

		private final List<GenericEntity> createdEntities = newList();
		private final Map<EntityType<?>, Integer> entitiesPerType = newMap();

		public AssImportingCloningContext(ManagedGmSession session, String globalIdPrefix) {
			this.session = session;
			this.globalIdPrefix = globalIdPrefix;
		}

		public void ensureStableGlobalIds() {
			for (GenericEntity ge : createdEntities)
				if (ge.getGlobalId() == null)
					ge.setGlobalId(globalIdFor(ge));
		}

		private String globalIdFor(GenericEntity ge) {
			EntityType<?> et = ge.entityType();
			int counter = getAndIncrementCounter(et);

			return globalIdPrefix + "/" + et.getShortName() + (counter == 1 ? "" : "" + counter);
		}

		private int getAndIncrementCounter(EntityType<?> et) {
			int count = 1 + entitiesPerType.getOrDefault(et, 0);
			entitiesPerType.put(et, count);

			return count;
		}

		@Override
		public <T> T getAssociated(GenericEntity entity) {
			T ass = super.getAssociated(entity);
			if (ass != null)
				return ass;

			String globalId = entity.getGlobalId();
			if (globalId != null)
				return session.findEntityByGlobalId(globalId);

			return null;
		}

		@Override
		public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
			GenericEntity entity = session.create(entityType);
			createdEntities.add(entity);
			return entity;
		}

		@Override
		public Object postProcessCloneValue(GenericModelType propertyOrElementType, Object clonedValue) {
			return super.postProcessCloneValue(propertyOrElementType, clonedValue);
		}

	}

}
