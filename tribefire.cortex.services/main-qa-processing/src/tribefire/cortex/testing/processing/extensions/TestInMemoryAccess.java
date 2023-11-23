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
package tribefire.cortex.testing.processing.extensions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.user.User;

public class TestInMemoryAccess extends BasicAccessAdapter implements InitializationAware {

	private Collection<GenericEntity> population = new HashSet<>();

	/**
	 * A simple counter used to assign ids. (Note that one would normally use an {@link IdGenerator}.)
	 */
	private long idCounter = 1;

	/**
	 * The set of already used ids. (Note that one would normally use an {@link IdGenerator}.)
	 */
	private final Set<String> usedIds = new HashSet<>();

	/**
	 * Will be called by Wire after bean is created (all properties set). It creates the default instances of the
	 * access.
	 */
	@Override
	public void postConstruct() {
		population = createInitialPopulation();
	}

	/**
	 * Creates three initial user entities.
	 * 
	 * @return the collection containing the created users.
	 */
	protected Collection<GenericEntity> createInitialPopulation() {

		Set<User> userList = new HashSet<User>();

		userList.add(createUser("john.doe@somdomain.com", "John", "Doe"));
		userList.add(createUser("jane.doe@somdomain.com", "Jane", "Doe"));
		userList.add(createUser("jim.smith@somdomain.com", "Jim", "Smith"));

		final Set<GenericEntity> population = new HashSet<GenericEntity>();
		GenericModelType type = GMF.getTypeReflection().getType(userList);

		/* Traverses the userList set to collect all assigned entities and add them to the population */
		type.traverse(userList, null, new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				population.add(entity);
			}
		});

		return population;

	}

	protected User createUser(String email, String firstName, String lastName) {
		User user = User.T.create();
		user.setName(firstName + " " + lastName);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		setIdUnlessSet(user);
		return user;
	}

	/**
	 * This method is called by the {@link BasicAccessAdapter} while performing a query. Return the full population for
	 * the given type and let the {@link BasicAccessAdapter} do the rest.
	 */
	@Override
	protected Iterable<GenericEntity> queryPopulation(final String typeSignature) throws ModelAccessException {

		// Acquire the collection of entities identified by the given
		// typeSignature.
		EntityType<GenericEntity> type = typeReflection.getEntityType(typeSignature);
		Set<GenericEntity> typePopulation = collectTypePopulation(type);
		return typePopulation;
	}

	/**
	 * This method is called by the {@link BasicAccessAdapter} when manipulations are committed from the client.<br />
	 * The passed context contains informations about the affected (created, updated and deleted) entities. This method
	 * applies the changes described in the context to the actual entities stored in the population collection.
	 */
	@Override
	protected void save(AdapterManipulationReport context) throws ModelAccessException {

		// Handle new entities
		for (GenericEntity entity : context.getCreatedEntities()) {
			// Add the new entity to the in-memory storage (type population).
			setIdUnlessSet(entity);
			population.add(entity);
		}

		// handle deleted entities
		for (GenericEntity entity : context.getDeletedEntities()) {
			GenericEntity entityFromPopulation = findEntityInPopulation(entity.reference());
			population.remove(entityFromPopulation);
		}

		// Handle updated entities by iterating through map of touched
		// properties.
		for (Map.Entry<GenericEntity, Set<Property>> updateEntry : context.getTouchedPropertiesOfEntities().entrySet()) {
			GenericEntity updatedEntity = updateEntry.getKey();
			Set<Property> updatedProperties = updateEntry.getValue();

			// Get the EntityType and the EntityReference of current entity from
			// the TypeReflection
			EntityReference updatedEntityReference = updatedEntity.reference();

			// Lookup the entity from the in-memory storage by using the
			// EntityRefernce
			GenericEntity entityFromPopulation = findEntityInPopulation(updatedEntityReference);
			if (entityFromPopulation == null) {
				throw new ModelAccessException("No entity with reference: " + updatedEntityReference + " found.");
			}

			// Iterate through list of updated properties and update each of the
			// properties of current entity
			// by using the EntityType.
			for (Property updatedProperty : updatedProperties) {

				// Get the property value by inspecting the updated property.
				Object propertyValue = updatedProperty.get(updatedEntity);

				// Update property using Reflection
				updatedProperty.set(entityFromPopulation, propertyValue);

			}
		}
	}

	/**
	 * Internal method that runs through the population collection and searches for an entity matching the typeSignature
	 * and id of passed entityReference.
	 * 
	 * @return the entity matching the entityReference. Null if no entity is found.
	 */
	protected GenericEntity findEntityInPopulation(EntityReference entityReference) {

		for (GenericEntity entity : population) {
			EntityReference currentReference = entity.reference();
			if (currentReference.getTypeSignature().equals(entityReference.getTypeSignature())
					&& currentReference.getRefId().equals(entityReference.getRefId())) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * Returns a collection of GenericEntity instances that are assignable for the given type.
	 */
	private Set<GenericEntity> collectTypePopulation(EntityType<GenericEntity> entityType) {

		// @formatter:off
		return population
				.stream()
				.filter((e) -> entityType.isAssignableFrom(e.type())) // Filtering the population collection for instances that matches the given type
				.collect(Collectors.toSet());
 
		// @formatter:on
	}

	/**
	 * Assigns a new id for the given <code>entity</code> (unless the id property is already set).
	 *
	 * @throws NotImplementedException
	 *             if the id isn't either of type <code>string</code> or <code>long</code>.
	 */
	private void setIdUnlessSet(GenericEntity entity) {
		Object existingId = entity.getId();
		String id;
		if (existingId != null) {
			if (!(existingId instanceof String)) {
				throw new IllegalStateException("Entity can only have id of type String in SimpleInMemoryAccess. Entity type: "
						+ entity.entityType().getTypeSignature() + " id '" + existingId + " of type '" + existingId.getClass().getSimpleName());
			}

			id = (String) existingId;

		} else {
			// id is not set yet --> assign a new id
			do {
				id = "" + idCounter++;

			} while (usedIds.contains(id)); // repeat if id is already used

			entity.setId(id);
		}

		// remember id so that we don't re-use it again
		usedIds.add(id);
	}

}
