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
package com.braintribe.model.processing.session.interceptors;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EntityFlags;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.processing.smood.Smood;

/**
 * @author peter.gazdik
 */
public class InterceptorTests extends AbstractSessionBasedTest {

	@Test
	public void getEmptyCollection_Unbound() throws Exception {
		Person p = Person.T.create();
		assertCollectionsNotNull(p);
	}

	@Test
	public void getEmptyCollection_Session() throws Exception {
		Person p = newPerson("john");
		assertCollectionsNotNull(p);
	}

	@Test
	public void getEmptyCollection_SessionFromSmood() throws Exception {
		Person p = Person.T.create();

		Smood s = new Smood(EmptyReadWriteLock.INSTANCE);
		s.initialize(p);

		assertCollectionsNotNull(p);
	}

	@Test
	public void getEmptyCollection_AfterBindingToSession() throws Exception {
		Person p = Person.T.create();

		/* This will force the NonNullCollectionEnsuringPropertyAccessInterceptor to set collection values as normal
		 * collections. */
		assertCollectionsNotNull(p);

		/* Calling a collection getter here will cause it to be wrapped inside an EnhancedCollection, since we are bound
		 * to a session now. */
		session.attach(p);
		assertCollectionsNotNull(p);
	}

	@Test
	public void lazyLoading_NullCollection() throws Exception {
		Person p = newPerson("john");
		session.commit();

		p = querySinglePerson(noPropertiesTC());

		checkOnlyIdentifyingAndGlobalIdLoaded(p);
		assertCollectionsNotNull(p);
	}

	private void assertCollectionsNotNull(Person p) {
		/* We test that null values are not returned for new instance and that the exact same value is returned by every
		 * getter invocation (the null collection handling is idempotent). */
		assertThat(p.getFriendList()).isNotNull().isSameAs(p.getFriendList()).isEmpty();
		assertThat(p.getFriendSet()).isNotNull().isSameAs(p.getFriendSet()).isEmpty();
		assertThat(p.getFriendMap()).isNotNull().isSameAs(p.getFriendMap()).isEmpty();
	}

	/* NOTE that this is obviously also testing standard property manipulation tracking (as opposed to tracking
	 * manipulations of collections), as we are doing manipulations and committing them. */
	@Test
	public void lazyLoading() throws Exception {
		/* Create entity with various properties */
		Person p = newPerson("john");
		p.setBestFriend(p);
		p.setNumbers(asList(1, 2, 3));
		p.setFriendList(asList(p, p));
		p.setFriendSet(asSet(p));
		p.setFriendMap(asMap("me", p));

		/* Commit the changes - this is working only iff manipulation tracking works. */
		session.commit();

		/* Query the person entity via a new session, having all (non-id) properties absent */
		p = querySinglePerson(noPropertiesTC());

		/* Check all properties are absent */
		checkOnlyIdentifyingAndGlobalIdLoaded(p);

		/* This assertions test lazy loading works correctly */
		assertThat(p.getName()).isEqualTo("john");
		assertThat(p.getBestFriend()).isSameAs(p);

		assertThat(p.getNumbers()).isEqualTo(asList(1, 2, 3));

		assertThat(p.getFriendList()).isEqualTo(asList(p, p));
		assertThat(p.getFriendSet()).isEqualTo(asSet(p));
		assertThat(p.getFriendMap()).isEqualTo(asMap("me", p));

		checkNoPropertyIsAbsent(p);
	}

	@Test
	public void lazyLoadingOnShallowInstance() throws Exception {
		/* Create entity with various properties */
		Person p = newPerson("john");
		p.setBestFriend(p);
		p.setNumbers(asList(1, 2, 3));

		/* Commit the changes - this is working only iff manipulation tracking works. */
		session.commit();

		/* Query the person entity via a new session, having all (non-id) properties absent */
		PersistenceGmSession newSession = newPersistenceSession();
		p = newSession.query().entity(Person.T, p.getId()).findLocalOrBuildShallow();

		/* Check entity is shallow (ensuring on empty session must result in a shallow instance), all properties
		 * absent */
		assertShallow(p, true);
		checkOnlyIdentifyingAndGlobalIdLoaded(p);

		/* This assertion tests lazy loading works correctly. We use an entity to better test the traversing criteria -
		 * this tests both "all simple properties" and "one custom property" */
		assertThat(p.getBestFriend()).isSameAs(p);

		/* Check entity is not shallow anymore, simple properties are not absent */
		assertShallow(p, false);
		checkSimplePropertiesAreNotAbsent(p);

		/* Just for fun, let's check standard lazy-loading now */
		assertThat(p.getName()).isEqualTo("john");
		assertThat(p.getNumbers()).isEqualTo(asList(1, 2, 3));
	}

	/**
	 * This test was added with bugfix for BTT-5845
	 */
	@Test
	public void lazyLoadingOnShallowInstance_WhenHavingPrimitiveProperty() throws Exception {
		/* Create entity with various properties */
		Flag f = session.create(Flag.T);
		f.setName("flag");
		f.set$active(true);

		/* Commit the changes - this is working only iff manipulation tracking works. */
		session.commit();

		/* Query the person entity via a new session, having all (non-id) properties absent */
		PersistenceGmSession newSession = newPersistenceSession();
		f = newSession.query().entity(Flag.T, f.getId()).findLocalOrBuildShallow();

		/* Check entity is shallow (ensuring on empty session must result in a shallow instance), all properties
		 * absent */
		assertShallow(f, true);

		// If there was the original bug
		assertThat(f.get$active()).isTrue();
	}

	private void checkOnlyIdentifyingAndGlobalIdLoaded(GenericEntity ge) {
		EntityType<GenericEntity> et = ge.entityType();

		for (Property p : et.getProperties()) {
			if (p.isIdentifying() || p.isGlobalId())
				continue;

			assertThat(p.getAbsenceInformation(ge)).as("Prop. '" + p.getName() + "' should be absent!").isNotNull();
		}
	}

	private void checkNoPropertyIsAbsent(GenericEntity ge) {
		EntityType<GenericEntity> et = ge.entityType();

		for (Property p : et.getProperties())
			assertThat(p.getAbsenceInformation(ge)).as("Prop. '" + p.getName() + "' shouldn't be absent!").isNull();
	}

	private void checkSimplePropertiesAreNotAbsent(GenericEntity ge) {
		EntityType<GenericEntity> et = ge.entityType();

		for (Property p : et.getProperties()) {
			if (!p.getType().isSimple())
				continue;

			assertThat(p.getAbsenceInformation(ge)).as("Prop. '" + p.getName() + "' should not be absent!").isNull();
		}
	}

	private void assertShallow(GenericEntity ge, boolean b) {
		assertThat(EntityFlags.isShallow(ge)).isEqualTo(b);
	}

}
