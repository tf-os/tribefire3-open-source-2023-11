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
package tribefire.extension.simple.deployables.access;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.processing.IdGenerator;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;

import tribefire.extension.simple.model.data.Company;
import tribefire.extension.simple.model.data.Department;
import tribefire.extension.simple.model.data.Person;

/**
 * A simple {@link IncrementalAccess} implementation that stores entities from the <code>simple-data-model</code> in memory. The implementation is
 * based on {@link BasicAccessAdapter}.<br>
 * The {@link #loadPopulation() population} can {@link #isInitializeWithExampleData() optionally} be initialized with {@link #createExampleData()
 * example data}.
 *
 * @author michael.lafite
 */
public class SimpleInMemoryAccess extends BasicAccessAdapter implements InitializationAware {

	/**
	 * The <code>Logger</code> used by this class.
	 */
	private static Logger logger = Logger.getLogger(SimpleInMemoryAccess.class);

	/**
	 * Specifies whether or not to {@link #createExampleData() create example data}.
	 *
	 * @see #isInitializeWithExampleData()
	 */
	private boolean initializeWithExampleData;

	/**
	 * The in-memory storage. For the sake of simplicity this is just a {@link HashSet}.
	 */
	private final Collection<GenericEntity> storage = new HashSet<>();

	/**
	 * A simple counter used to assign ids. (Note that one would normally use an {@link IdGenerator}.)
	 */
	private long idCounter = 1;

	/**
	 * The set of already used ids. (Note that one would normally use an {@link IdGenerator}.)
	 */
	private final Set<String> usedIds = new HashSet<>();

	/**
	 * Creates a new access instance. During deployment of the access also {@link #postConstruct()} will be called.
	 *
	 * @see #postConstruct()
	 */
	public SimpleInMemoryAccess() {
		// this constructor is called first during deployment
		// just try setting a break point here to debug!
		logger.debug(SimpleInMemoryAccess.class.getSimpleName() + " instantiated.");
		// no model required for this simple test
		setMetaModelProvider(() -> null);
	}

	/**
	 * Whether or not to {@link #createExampleData() create example data}. This depends on
	 * {@link tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess#getInitializeWithExampleData()}.
	 */
	public boolean isInitializeWithExampleData() {
		return initializeWithExampleData;
	}

	/**
	 * @see #isInitializeWithExampleData()
	 */
	public void setInitializeWithExampleData(boolean initializeWithExampleData) {
		this.initializeWithExampleData = initializeWithExampleData;
	}

	/**
	 * Initializes this access. Dependent on {@link #isInitializeWithExampleData()} this also {@link #createExampleData() creates example data}.<br>
	 * This method is called automatically after creation of all deployables (see {@link InitializationAware#postConstruct()}).
	 */
	@Override
	public void postConstruct() {
		// this method is called during deployment (after the constructor)
		// just try setting a break point here to debug!
		logger.trace("Initializing " + SimpleInMemoryAccess.class.getSimpleName() + " ...");
		if (isInitializeWithExampleData()) {
			createExampleData();
		}
		logger.info(SimpleInMemoryAccess.class.getSimpleName() + " initialized.");
	}

	/**
	 * Returns the current population, i.e. the full storage (all entities).<br>
	 * This is invoked by super type methods from {@link BasicAccessAdapter} as part of query processing.
	 */
	@Override
	protected Collection<GenericEntity> loadPopulation() {
		return this.storage;
	}

	/**
	 * Updates the storage based on the given <code>manipulationReport</code>, which provides information about
	 * {@link com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport#getCreatedEntities() created},
	 * {@link com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport#getDeletedEntities() deleted} and
	 * {@link com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport#getUpdatedEntities() updated} entities.
	 * <p>
	 * Note that this (simple) implementation does not support all features, for example entity type and collection properties cannot be updated.
	 *
	 * @throws ModelAccessException
	 *             if anything goes wrong.
	 */
	@Override
	protected void save(AdapterManipulationReport manipulationReport) throws ModelAccessException {
		try {
			// handle new entities
			for (GenericEntity entity : manipulationReport.getCreatedEntities()) {
				setIdUnlessSet(entity);
				storage.add(entity);
			}

			// handle deleted entities
			for (GenericEntity entity : manipulationReport.getDeletedEntities()) {
				// get respective entity from storage
				GenericEntity entityFromStorage = getEntityByReference(entity.reference());

				storage.remove(entityFromStorage);
			}

			// handle updated entities
			for (GenericEntity entityFromManipulationReport : manipulationReport.getTouchedPropertiesOfEntities().keySet()) {
				Set<Property> touchedProperties = manipulationReport.getTouchedPropertiesOfEntities().get(entityFromManipulationReport);

				// get respective entity from storage
				GenericEntity entityFromStorage = getEntityByReference(entityFromManipulationReport.reference());

				for (Property touchedProperty : touchedProperties) {
					Object newValue = touchedProperty.get(entityFromManipulationReport);

					if (newValue instanceof GenericEntity || newValue instanceof Collection || newValue instanceof Map) {
						throw new NotImplementedException("Cannot update property " + touchedProperty.getName() + " from entity "
								+ entityFromManipulationReport.entityType().getTypeSignature() + ": updates not supported for property type "
								+ touchedProperty.getType().getTypeName() + "! (That's just because this is a simplified access implementation.)");
					}
					touchedProperty.set(entityFromStorage, newValue);
				}
			}
		} catch (RuntimeException e) {
			throw new ModelAccessException("Error while processing manipulation report!", e);
		}
	}

	/**
	 * Retrieves an entity from the storage.
	 *
	 * @param searchedEntityReference
	 *            the entity reference for the searched entity.
	 * @return the entity from the storage matching the passed <code>searchedEntityReference</code>.
	 * @throws IllegalArgumentException
	 *             if the entity cannot be found.
	 */
	private GenericEntity getEntityByReference(EntityReference searchedEntityReference) {
		for (GenericEntity entity : storage) {
			EntityReference entityReference = entity.reference();
			if (entityReference.getTypeSignature().equals(searchedEntityReference.getTypeSignature())
					&& entityReference.getRefId().equals(searchedEntityReference.getRefId())) {
				return entity;
			}
		}

		throw new IllegalArgumentException("Searched entity " + searchedEntityReference + " not found!");
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
				throw new IllegalStateException("Cannot handle " + entity.entityType().getTypeSignature() + " id '" + existingId + " of type '"
						+ existingId.getClass().getSimpleName() + ". SimpleInMemoryAccess only supports ids of type string or long.");
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

	/**
	 * Creates some example data, i.e. a {@link Company} with a few {@link Department}s and some {@link Person}s {@link Company#getEmployees()
	 * employed at} that company.
	 */
	private void createExampleData() {
		logger.debug("Creating example data ...");

		Person person1 = Person.T.create();
		person1.setFirstName("Jane");
		person1.setLastName("Smith");
		storage.add(person1);

		Person person2 = Person.T.create();
		person2.setFirstName("John");
		person2.setLastName("Adams");
		storage.add(person2);

		Person person3 = Person.T.create();
		person3.setFirstName("Jack");
		person3.setLastName("Taylor");
		storage.add(person3);

		Person person4 = Person.T.create();
		person4.setFirstName("Jim");
		person4.setLastName("Taylor");
		person4.setFather(person3);
		storage.add(person4);

		Company company = Company.T.create();

		Department department1 = Department.T.create();
		department1.setName("Marketing");
		department1.setNumberOfEmployees(1);
		department1.setCompany(company);
		department1.setManager(person1);
		storage.add(department1);

		Department department2 = Department.T.create();
		department2.setName("R&D");
		department2.setNumberOfEmployees(2);
		department2.setCompany(company);
		department2.setManager(person2);
		storage.add(department2);

		company.setName("Acme");
		company.setCeo(person3);
		company.setAverageRevenue(new BigDecimal("1234567890"));
		company.getEmployees().add(person1);
		company.getEmployees().add(person2);
		company.getEmployees().add(person3);
		company.getEmployees().add(person4);
		company.getDepartments().add(department1);
		company.getDepartments().add(department2);
		storage.add(company);

		// create ids for all entities
		storage.forEach(this::setIdUnlessSet);

		// we obviously could create a lot more example data here

		logger.debug("Successfully created example data.");
	}
}
