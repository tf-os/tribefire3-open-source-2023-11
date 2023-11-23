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
package com.braintribe.model.processing.am;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityReferencesVisitor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardTraversingContext;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedCollection;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedList;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedMap;
import com.braintribe.model.processing.session.impl.session.collection.EnhancedSet;

public class AssemblyMonitoring {
	private static Logger logger = Logger.getLogger(AssemblyMonitoring.class);
	private final List<EntityMigrationListener> entityMigrationsListeners = new ArrayList<EntityMigrationListener>();
	private List<ManipulationListener> manipulationListeners = new ArrayList<ManipulationListener>();
	private boolean initialized = false;
	private boolean isAbsenceResolvable = false;
	
	private final ReferenceManager referenceManager = new ReferenceManager() {
		@Override
		protected void onJoin(GenericEntity entity) {
			if (initialized)
				fireOnJoin(entity);
		}
		
		@Override
		protected void onLeave(GenericEntity entity) {
			if (initialized)
				fireOnLeave(entity);
		}
	};
	
	private AssemblyOrigin origin;
	
	private final AssemblyManipulationListener manipulationListener = new AssemblyManipulationListener();
	
	public static AssemblyMonitoringBuilder newInstance() {
		return new AssemblyMonitoringBuilderImpl();
	}
	
	protected AssemblyMonitoring(AssemblyMonitoringBuilder context, GenericEntity root) {
		init(context, new EntityOrigin(root));
	}
	
	protected AssemblyMonitoring(AssemblyMonitoringBuilder context, GenericEntity root, Property property) {
		init(context, new PropertyOrigin(property, root));
	}
	
	protected AssemblyMonitoring(AssemblyMonitoringBuilder context, EnhancedList<?> list) {
		init(context, new CollectionOrigin(list));
	}
	
	protected AssemblyMonitoring(AssemblyMonitoringBuilder context, EnhancedSet<?> set) {
		init(context, new CollectionOrigin(set));
	}
	
	protected AssemblyMonitoring(AssemblyMonitoringBuilder context, EnhancedMap<?,?> map) {
		init(context, new CollectionOrigin(map));
	}
	
	protected void init(AssemblyMonitoringBuilder context, AssemblyOrigin origin) {
		this.isAbsenceResolvable = context.isAbsenceResolvable();
		
		init(context.getSession(), origin);
	}
	
	protected void init(NotifyingGmSession session, AssemblyOrigin origin) {
		this.origin = origin;
		
		session.listeners().add(manipulationListener);
		
		origin.scan();
		initialized = true;
	}
	
	public Map<GenericEntity, RefereeData> getReferenceMap() {
		return referenceManager.getReferenceMap();
	}

	public Set<GenericEntity> getEntities() {
		return referenceManager.getEntities();
	}
	
	public void addManpiulationListener(ManipulationListener manipulationListener) {
		List<ManipulationListener> newValue = new ArrayList<ManipulationListener>(manipulationListeners);
		newValue.add(manipulationListener);
		manipulationListeners = newValue;
	}
	
	public void removeManpiulationListener(ManipulationListener manipulationListener) {
		List<ManipulationListener> newValue = new ArrayList<ManipulationListener>(manipulationListeners);
		newValue.remove(manipulationListener);
		manipulationListeners = newValue;
	}
	
	public void addEntityMigrationListener(EntityMigrationListener listener) {
		entityMigrationsListeners.add(listener);
	}
	
	public void removeEntityMigrationListener(EntityMigrationListener listener) {
		entityMigrationsListeners.remove(listener);
	}
	
	protected void fireManipulationEvent(Manipulation manipulation) {
		for (ManipulationListener listener: manipulationListeners) {
			try {
				listener.noticeManipulation(manipulation);
			}
			catch (Exception e) {
				logger.error("error while distributing manipulation event", e);
			}
		}
	}
	
	protected void fireOnJoin(GenericEntity entity) {
		EntityMigrationListener listeners[] = entityMigrationsListeners.toArray(new EntityMigrationListener[entityMigrationsListeners.size()]);
		
		for (EntityMigrationListener listener: listeners) {
			try {
				listener.onJoin(entity);
			}
			catch (Exception e) {
				logger.error("error while distributing onJoin event", e);
			}
		}
	}
	
	protected void fireOnLeave(GenericEntity entity) {
		EntityMigrationListener listeners[] = entityMigrationsListeners.toArray(new EntityMigrationListener[entityMigrationsListeners.size()]);
		
		for (EntityMigrationListener listener: listeners) {
			try {
				listener.onLeave(entity);
			}
			catch (Exception e) {
				logger.error("error while distributing onLeave event", e);
			}
		}
	}
	
	private abstract class AssemblyOrigin extends EntityReferencesVisitor {
		protected GenericEntity entity;

		@Override
		protected void visitEntityReference(GenericEntity entity, TraversingContext traversingContext) {
			referenceManager.addReference(getReferee(traversingContext, getRootReferee()), entity);
		}

		public abstract void scan();
		public abstract boolean isMigrationTarget(LocalEntityProperty target);
		protected abstract GenericEntity getRootReferee();

		protected void traverse(GenericModelType type, Object object, TraversingVisitor visitor) {
			StandardTraversingContext tc = new AssemblyMonitoringTraversingContext(isAbsenceResolvable);
			tc.setTraversingVisitor(visitor);

			type.traverse(tc, object);
		}
	}

	private abstract class AbstractEntityOrigin extends AssemblyOrigin {
		
		protected AbstractEntityOrigin(GenericEntity entity) {
			this.entity = entity;
		}
	}

	static GenericEntity getReferee(TraversingContext traversingContext, GenericEntity rootReferee) {
		switch (traversingContext.getCurrentCriterionType()) {
			case ROOT:
				return rootReferee;
			case PROPERTY:
				return getEntity(traversingContext, 2); // ENTITY, PROPERTY
			case SET_ELEMENT:
			case LIST_ELEMENT:
				return getEntity(traversingContext, 3); // ENTITY, PROPERTY, LIST_ELEMENT
			case MAP_KEY:
			case MAP_VALUE:
				return getEntity(traversingContext, 5); // ENTITY, PROPERTY, MAP, MAP_ENTRY, MAP_KEY
			default:
				return null;
		}
	}

	private static GenericEntity getEntity(TraversingContext traversingContext, int rear_offset) {
		Stack<Object> os = traversingContext.getObjectStack();
		return (GenericEntity) os.get(os.size() - rear_offset);
	}

	private class EntityOrigin extends AbstractEntityOrigin {
		public EntityOrigin(GenericEntity entity) {
			super(entity);
		}
		
		@Override
		public void scan() {
			EntityType<?> entityType = entity.entityType();
			traverse(entityType, entity, this);
		}
		
		@Override
		public boolean isMigrationTarget(LocalEntityProperty target) {
			GenericEntity entity = target.getEntity();
			return referenceManager.hasReference(entity);
		}

		@Override
		protected GenericEntity getRootReferee() {
			return null; // we are not referenced by any other entity
		}
	}
	
	private class PropertyOrigin extends AbstractEntityOrigin {
		private final Property property;
		
		public PropertyOrigin(Property property, GenericEntity entity) {
			super(entity);
			this.property = property;
		}
		
		@Override
		public void scan() {
			GenericModelType type = property.getType();
			Object value = property.get(entity);
			traverse(type, value, this);
		}
		
		@Override
		public boolean isMigrationTarget(LocalEntityProperty target) {
			GenericEntity targetEntity = target.getEntity();
			
			if (entity == targetEntity && target.getPropertyName().equals(property.getName())) {
				return true;
			}
			else {
				return referenceManager.hasReference(targetEntity);
			}
		}

		@Override
		protected GenericEntity getRootReferee() {
			return entity;
		}
	}
	
	private class CollectionOrigin extends AssemblyOrigin {
		private final CollectionType collectionType;
		
		public CollectionOrigin(EnhancedCollection collection) {
			this.collectionType = collection.type();
			
			Object value = null;

			switch (collectionType.getCollectionKind()) {
			case list:
				EnhancedList<?> enhancedList = (EnhancedList<?>)collection;
				enhancedList.addManipulationListener(manipulationListener);
				value = enhancedList;
				break;
			case map:
				EnhancedMap<?,?> enhancedMap = (EnhancedMap<?, ?>) collection; 
				enhancedMap.addManipulationListener(manipulationListener);
				value = enhancedMap;
				break;
			case set:
				EnhancedSet<?> enhancedSet = (EnhancedSet<?>) collection; 
				enhancedSet.addManipulationListener(manipulationListener);
				value = enhancedSet;
				break;
			}
			
			PseudoRoot root = PseudoRoot.T.create();
			root.setValue(value);
			entity = root;
		}
		
		@Override
		public void scan() {
			traverse(PseudoRoot.T, entity, this);
		}
		
		@Override
		public boolean isMigrationTarget(LocalEntityProperty target) {
			GenericEntity entity = target.getEntity();
			return referenceManager.hasReference(entity);
		}
		
		@Override
		protected GenericEntity getRootReferee() {
			// not referenced by any other entity
			return null;
		}
		
	}
	
	private class AssemblyManipulationListener implements ManipulationListener {
		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (manipulation instanceof PropertyManipulation) {
				PropertyManipulation propertyManipulation = (PropertyManipulation)manipulation;
				LocalEntityProperty target = (LocalEntityProperty)propertyManipulation.getOwner();
				
				// analyze for migration
				if (origin.isMigrationTarget(target)) {
					ReferenceCountDelta referenceCountDelta = new ReferenceCountDelta(referenceManager, origin.entity, isAbsenceResolvable);
					referenceCountDelta.applyReferenceMigration(propertyManipulation);
					fireManipulationEvent(propertyManipulation);
				}
			}
		}
	}
	
	protected static class AssemblyMonitoringTraversingContext extends StandardTraversingContext {
		private final boolean isAbsenceResolvable;

		public AssemblyMonitoringTraversingContext(boolean isAbsenceResolvable) {
			this.isAbsenceResolvable = isAbsenceResolvable;
		}

		@SuppressWarnings("unusable-by-js")
		@Override
		public boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation) {
			return isAbsenceResolvable;
		}
	}
	
}
