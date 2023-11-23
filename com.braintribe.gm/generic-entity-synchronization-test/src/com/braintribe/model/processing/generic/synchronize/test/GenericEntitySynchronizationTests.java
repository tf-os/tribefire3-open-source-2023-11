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
package com.braintribe.model.processing.generic.synchronize.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.generic.synchronize.BasicEntitySynchronization;
import com.braintribe.model.processing.generic.synchronize.GenericEntitySynchronizationException;
import com.braintribe.model.processing.generic.synchronize.api.IdentityManager;
import com.braintribe.model.processing.generic.synchronize.api.SynchronizationContext;
import com.braintribe.model.processing.generic.synchronize.test.model.Address;
import com.braintribe.model.processing.generic.synchronize.test.model.Person;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class GenericEntitySynchronizationTests extends AbstractEntitySynchronizationTests {

	public GenericEntitySynchronizationTests() {
	}
	
	/* ******************************************
	 * Test methods
	 * ******************************************/
	
	@Test
	public void testBasicEntityImportWithCommit() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessMemoryOnly();
		
		Person person = newDefaultPerson();
		Address address = newDefaultAddress();
		
		BasicEntitySynchronization
			.newInstance()
			.session(session)
			.commitAfterSynchronization()
			.addEntity(person)
			.addEntity(address)
			.addIdentityManager()
				.externalId()
			.close()
			.addIdentityManager()
				.globalId()
			.close()
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));

		
	}

	@Test
	public void testBasicEntityImportWithoutCommit() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessMemoryOnly();
		
		Person person = newDefaultPerson();
		Address address = newDefaultAddress();
		
		BasicEntitySynchronization
			.newInstance()
			.session(session)
			.addEntity(person)
			.addEntity(address)
			.synchronize();
		
		// Assert before commit
		BtAssertions.assertThat(queryUnique(session,Person.class)).isNull();
		BtAssertions.assertThat(queryUnique(session,Address.class)).isNull();
		assertEntityEqual(person, queryCacheUnique(session,Person.class));
		assertEntityEqual(address, queryCacheUnique(session,Address.class));

		session.commit();

		// Assert after commit
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));

	}

	@Test
	public void testBasicEntitySynchronizationNoIdentityManagement() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();


		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));

		// Change person
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		// Synchronize second time
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		List<Person> persons = session.query().entities(EntityQueryBuilder.from(Person.class).done()).list();
		BtAssertions.assertThat(persons.size()).isEqualTo(2);
		
		Set<String> expectedNames = new HashSet<String>(Arrays.asList("John", "Jane"));
		Set<String> expectedDescriptions = new HashSet<String>(Arrays.asList("John Doe description", "Jane Doe description"));
		for (Person p : persons) {
			BtAssertions.assertThat(expectedNames.remove(p.getFirstName())).isTrue();
			BtAssertions.assertThat(expectedDescriptions.remove(p.getDescription())).isTrue();
		}
		BtAssertions.assertThat(expectedNames).isEmpty();
		BtAssertions.assertThat(expectedDescriptions).isEmpty();
	}

	@Test
	public void testBasicEntitySynchronizationWithGlobalId() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		person.setGlobalId("12345");

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Synchronize second time with different values
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}
	
	@Test
	public void testBasicEntitySynchronizationWithExternalId() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "ssnr");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));

		// Change person
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		// Synchronize second time with different values
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}
	
	@Test
	public void testBasicEntitySynchronizationWithManualExternalId() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "ssnr");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.externalId()
					.close();
		
		//Import Person
		synchronization
			.addEntity(person)
			.synchronize();

		// Assert import
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Change person
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		// Synchronize second time with different values
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}

	private GmMetaModel buildTestModel() {
		NewMetaModelGeneration modelGen = new NewMetaModelGeneration();
		GmMetaModel model = modelGen.buildMetaModel("TestModel", Stream.of(Person.T, Address.T).collect(Collectors.toSet()));
		return model;
	}
	
	
	@Test
	public void testBasicEntitySynchronizationWithManualExternalIds() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "firstName");
		configureExternalId(model, Person.class, "lastName");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.externalId()
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));

		// Change person
		person.setDescription("New Description");
		
		// Syncchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Import a new person with different firstname
		Person person2 = newDefaultPerson();
		person2.setFirstName("Jane");
		
		synchronization
			.clearEntities()
			.addEntity(person)
			.addEntity(person2)
			.synchronize();
		
		assertEntitiesEqual(Arrays.asList(person,person2), query(session, Person.class));
		
	}
	
	@Test
	public void testBasicEntitySynchronizationWithGenericExternalId() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false)
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.generic()
							.responsibleFor(Person.class)
							.addIdentityProperty("ssnr")
					.close();
		
		//Import Person
		synchronization
			.addEntity(person)
			.synchronize();

		// Assert import
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Change person
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		// Synchronize second time with different values
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}
	
	@Test
	public void testBasicEntitySynchronizationWithManualGlobalId() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		person.setGlobalId("12345");

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.globalId()
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Synchronize second time with different values
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}
	
	@Test
	public void testBasicEntitySynchronizationWithIdProperty() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();
		person.setId(1L);
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.idProperty()
					.close();
					
		
		//Import Person
		synchronization
			.addEntity(person)
			.synchronize();

		// Assert import
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Change person
		person.setFirstName("Jane");
		person.setDescription("Jane Doe description");
		
		// Synchronize second time with different values
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
	}
	
	
	@Test
	public void testBasicGenericIdManagerConfigurationIssue() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		Person person = newDefaultPerson();
		
		try {
			BasicEntitySynchronization
				.newInstance()
				.session(session)
				.commitAfterSynchronization()
				.addIdentityManager()
					.generic()
					//.responsibleFor(Person.class) //missing responsibleFor is causing the exception.
					.addIdentityProperty("ssnr")
				.close()
				.addEntity(person)
				.synchronize();
			
		} catch (IllegalArgumentException e) {
			BtAssertions.assertThat(e.getMessage()).isEqualTo("responsibleFor missing.");
		}
		
	}
	
	@Test
	public void testBasicEntitySynchronizationWithExternalIds() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "firstName");
		configureExternalId(model, Person.class, "lastName");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));

		// Change person
		person.setDescription("New Description");
		
		// Syncchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Import a new person with different firstname
		Person person2 = newDefaultPerson();
		person2.setFirstName("Jane");
		
		synchronization
			.clearEntities()
			.addEntity(person)
			.addEntity(person2)
			.synchronize();
		
		assertEntitiesEqual(Arrays.asList(person,person2), query(session, Person.class));
		
	}

	@Test
	public void testBasicEntitySynchronizationWithGenericExternalIds() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.generic()
							.addIdentityProperties("firstName", "lastName")
							.responsibleFor(Person.class)
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));

		// Change person
		person.setDescription("New Description");
		
		// Syncchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		
		// Import a new person with different firstname
		Person person2 = newDefaultPerson();
		person2.setFirstName("Jane");
		
		synchronization
			.clearEntities()
			.addEntity(person)
			.addEntity(person2)
			.synchronize();
		
		assertEntitiesEqual(Arrays.asList(person,person2), query(session, Person.class));
		
	}
	
	
	@Test
	public void testBasicEntitySynchronizationWithGenericExternalIdsAndExcludes() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.generic()
						.responsibleFor(Person.class)
						.addIdentityProperties("firstName", "lastName")
						.addExcludedProperty("description")
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		// Expect description to be null since it's excluded.
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),null,person.getSsnr()});
		
		// Change person
		person.setDescription("New Description");
		person.setSsnr(9999);
		
		// Syncchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),null,person.getSsnr()});
		
	}
	
	@Test
	public void testBasicEntitySynchronizationWithGenericExternalIdsAndIncludes() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.generic()
						.responsibleFor(Person.class)
						.addIdentityProperties("firstName", "lastName")
						.addIncludedProperties("firstName", "lastName", "description")
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		// Expect ssnr to be null since it's not included.
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),person.getDescription(),null});
		
		// Change person
		person.setDescription("New Description");
		person.setSsnr(9999);
		
		// Syncchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),person.getDescription(),null});
		
	}

	@Test
	public void testBasicEntitySynchronizationWithGenericExternalIdsAndConflictingIncludesAndExcludes() throws Exception {
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFile();
		
		Person person = newDefaultPerson();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false) // disable default identity managers.
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager()
						.generic()
						.responsibleFor(Person.class)
						.addIdentityProperties("firstName", "lastName")
						.addIncludedProperties("firstName", "lastName", "description")
						.addExcludedProperty("description")
					.close();
		
		// Import first time
		synchronization
			.addEntity(person)
			.synchronize();
		
		// Expect description to be null since it's excluded (stronger then include) and ssnr to be null since it's not included.
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),null,null});

		// Change person
		person.setDescription("New Description");
		person.setSsnr(9999);
		
		// Synchronize again
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertUniqueEntityExistsWithProperties(
				queryUnique(session,Person.class), 
				new String[]{"firstName","lastName","description","ssnr"}, 
				new Object[]{person.getFirstName(),person.getLastName(),null,null});
		
	}
	@Test
	public void testBasicEntitySynchronizationWithDependenciesAndNoIdentityManagement() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "firstName");
		configureExternalId(model, Person.class, "lastName");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();
		Address address = newDefaultAddress();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import person and address separately 
		synchronization
			.addEntity(person)
			.addEntity(address)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));
		
		// Create new address and assign to person
		Address newAddress = newDefaultAddress();
		newAddress.setZipCode(1050);
		person.setAddress(newAddress);
		
		// Synchronize person only (with reference to address)
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntitiesEqual(Arrays.asList(address,newAddress), query(session,Address.class));
		
	}
	
	@Test
	public void testBasicEntitySynchronizationWithDependenciesAndExternalIds() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "firstName");
		configureExternalId(model, Person.class, "lastName");
		
		configureExternalId(model, Address.class, "street");
		configureExternalId(model, Address.class, "zipCode");
		configureExternalId(model, Address.class, "city");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();
		Address address = newDefaultAddress();

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import person and address separately 
		synchronization
			.addEntity(person)
			.addEntity(address)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));
		
		// Create new address and assign to person
		Address newAddress = newDefaultAddress();
		person.setAddress(newAddress);
		
		// Synchronize person only (with reference to address)
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(newAddress, queryUnique(session,Address.class));
		
	}

	@Test
	public void testBasicEntitySynchronizationWithDependenciesAndGlobalId() throws Exception {
		
		GmMetaModel model = buildTestModel();
		
		configureExternalId(model, Person.class, "firstName");
		configureExternalId(model, Person.class, "lastName");
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		
		Person person = newDefaultPerson();
		Address address = newDefaultAddress();
		// Assign globalId to address for identity management
		address.setGlobalId("12345");

		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance()
					.session(session)
					.commitAfterSynchronization()
					.addDefaultIdentityManagers();
		
		// Import person and address separately 
		synchronization
			.addEntity(person)
			.addEntity(address)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));
		
		// Create new address (with same globalId), change a property and assign to person
		Address newAddress = newDefaultAddress();
		newAddress.setGlobalId("12345");
		newAddress.setZipCode(1050);
		person.setAddress(newAddress);
		
		// Synchronize person only (with reference to address)
		synchronization
			.clearEntities()
			.addEntity(person)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(newAddress, queryUnique(session,Address.class));
		
	}
	
	
	@Test
	public void testBasicEntitySynchronizationWithCustomIdManager() throws Exception {
		GmMetaModel model = buildTestModel();
		
		PersistenceGmSession session = GmTestToolsX.newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(model);
		Person person = newDefaultPerson();
		person.setId(1L);
		Address address = newDefaultAddress();
		
		BasicEntitySynchronization synchronization = 
				BasicEntitySynchronization
					.newInstance(false)
					.session(session)
					.commitAfterSynchronization()
					.addIdentityManager(new IdentityManager() {
						
						@Override
						public boolean isResponsible(GenericEntity instanceToBeCloned, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) {
							return (instanceToBeCloned instanceof Person);
						}
						
						@Override
						public GenericEntity findEntity(GenericEntity instanceToBeCloned, EntityType<? extends GenericEntity> entityType, SynchronizationContext context) throws GenericEntitySynchronizationException {
							PersistenceGmSession session = context.getSession();
							try {
								return session.query().entity(Person.T, ((Person)instanceToBeCloned).getId()).find();
							} catch (GmSessionException e) {
								throw new GenericEntitySynchronizationException("Error while searching for person.",e);
							}
						}
						
						@Override
						public boolean canTransferProperty(GenericEntity instanceToBeCloned, GenericEntity clonedInstance, EntityType<? extends GenericEntity> entityType, Property property, SynchronizationContext context) {
							return true;
						}
					});
					
		
		// Import person and address separately 
		synchronization
			.addEntity(person)
			.addEntity(address)
			.synchronize();
		
		assertEntityEqual(person, queryUnique(session,Person.class));
		assertEntityEqual(address, queryUnique(session,Address.class));
		
		
		Person p2 = newDefaultPerson();
		p2.setId(person.getId());
		p2.setFirstName("New Firstname");
		p2.setLastName("New Lastname");
		
		Address a2 = newDefaultAddress();
		a2.setCity("New City");
		
		synchronization
			.clearEntities()
			.addEntity(p2)
			.addEntity(a2)
			.synchronize();
		
		
		assertEntityEqual(p2, queryUnique(session,Person.class));
		BtAssertions.assertThat(query(session, Address.class).size()).isEqualTo(2);
		
	}

	static class GmTestToolsX {

		static PersistenceGmSession newSessionWithSmoodAccessMemoryOnly() {
			return GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		}
		
		static PersistenceGmSession newSessionWithSmoodAccessAndTemporaryFile() {
			return GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		}
		
		static PersistenceGmSession newSessionWithSmoodAccessAndTemporaryFileAndModelAccessory(GmMetaModel model) {
			SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly("testAccess", model);
			PersistenceGmSession session = GmTestTools.newSession(access);
			ModelAccessory ma = session.getModelAccessory();
			return session;
		}
	}
	
}
