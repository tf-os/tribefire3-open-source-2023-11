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
package com.braintribe.model.access.collaboration.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.Collection;

import com.braintribe.cfg.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmModelElement;
import com.braintribe.model.processing.query.tools.PreparedQueries;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * This is not intended to be Thread-safe.
 * 
 * @author peter.gazdik
 */
public class CortexManipulationPersistence extends AbstractManipulationPersistence<CortexGmmlManipulationPersistence> {

	private String dataModelName;
	private String dataServiceModelName;
	private boolean mergeModelAndData;

	@Required
	public void setDataModelName(String dataModelName) {
		this.dataModelName = dataModelName;
	}

	@Required
	public void setDataServiceModelName(String dataServiceModelName) {
		this.dataServiceModelName = dataServiceModelName;
	}

	/** If set to true, this persistence persists model and data into data.man only (no splitting) */
	public void setMergeModelAndData(boolean mergeModelAndData) {
		this.mergeModelAndData = mergeModelAndData;
	}

	@Override
	public void onCollaborativeAccessInitialized(CollaborativeAccess csa, ManagedGmSession csaSession) {
		super.onCollaborativeAccessInitialized(csa, csaSession);

		if (!mergeModelAndData)
			csaSession.listeners().addFirst(new DeletedModelElementFleshPurger());
	}

	@Override
	protected CortexGmmlManipulationPersistence createGmmlPersistence() {
		return new CortexGmmlManipulationPersistence(mergeModelAndData);
	}

	@Override
	public void initializeModels(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		if (mergeModelAndData)
			return;

		super.initializeModels(context);

		deployCortexModel(context);
		deployCortexServiceModel(context);
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		if (!mergeModelAndData)
			super.initializeData(context);
		else
			initializeModelAndData(context);
	}

	@Override
	protected void initializeModelAndData(PersistenceInitializationContext context) {
		super.initializeModelAndData(context);

		deployCortexModel(context);
		deployCortexServiceModel(context);
	}

	private void deployCortexModel(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		deployModel(context, dataModelName);
	}

	private void deployCortexServiceModel(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		deployModel(context, dataServiceModelName);
	}

	private void deployModel(PersistenceInitializationContext context, String modelName) throws ManipulationPersistenceException {
		if (modelName == null)
			return;

		GmMetaModel model = context.getSession().query().select(PreparedQueries.modelByName(modelName)).unique();
		if (model == null)
			throw new ManipulationPersistenceException("Something went terribly wrong. " + modelName + " not found in the smood.");

		model.deploy();
	}

	/**
	 * This listener makes sure that iff some model element is being deleted, it's flesh (non-skeleton) properties are purged/cleared. The point is
	 * that manipulations on model elements are split, those skeleton related are stored separately from the rest. When we delete an entity, this
	 * change is recorded in the model part, but the data part contains no information that the data should no longer be there. For example, if
	 * somebody later created the element again (just instantiation), with the same globalId, next time the persistence initialization is performed,
	 * this entity would have it's non-skeleton properties have the values that was there when the first entity was deleted. To avoid this we also set
	 * such properties to their
	 * 
	 * See {@link CortexGmmlManipulationPersistence#isSkeletonRelevant}.
	 */
	class DeletedModelElementFleshPurger implements ManipulationListener {

		@Override
		public void noticeManipulation(Manipulation manipulation) {
			if (manipulation.manipulationType() != ManipulationType.DELETE)
				return;

			GenericEntity entity = ((DeleteManipulation) manipulation).getEntity();
			if (!(entity instanceof GmModelElement))
				return;

			EntityType<?> et = entity.entityType();

			for (Property property : et.getProperties()) {
				GenericModelType propertyType = property.getType();

				if (propertyType.isEntity()) {
					handleEntityProperty(entity, property);

				} else if (propertyType.isCollection()) {
					handleCollectionType(entity, property, (CollectionType) propertyType);
				}
			}
		}

		private void handleEntityProperty(GenericEntity entity, Property property) {
			if (!isSkeletonType(property.getType()))
				// AFAIK this code is unreachable, there is no non-skeleton entity property on any model element
				property.set(entity, null);
		}

		private void handleCollectionType(GenericEntity entity, Property property, CollectionType propertyType) {
			GenericModelType elementType = propertyType.getCollectionElementType();

			/* Currently all model element properties of type collections are collections of entities. but for if there are ever simple value
			 * collections, they will be considered skeleton related for now - see CortexGmmlManipulation splitting. */
			if (!elementType.isEntity() || isSkeletonType(elementType))
				return;

			/* we are dealing with a property of GmModelElement which is a collection, but not a collection of other elements; probably meta-data */
			Collection<?> c = property.getDirectUnsafe(entity);
			if (!isEmpty(c))
				c.clear();
		}

		private boolean isSkeletonType(GenericModelType type) {
			return GmModelElement.T.isAssignableFrom(type);
		}
	}

	@Override
	public String toString() {
		return "CortexManipulationPersistence: Data Model: " + dataModelName + ", Service Model: " + dataServiceModelName;
	}
}
