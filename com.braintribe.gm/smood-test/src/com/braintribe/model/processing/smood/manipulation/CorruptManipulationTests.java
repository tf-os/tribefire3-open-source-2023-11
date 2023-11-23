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
package com.braintribe.model.processing.smood.manipulation;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.test.model.Address;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.query.test.model.diamond.DiamondBase;
import com.braintribe.model.processing.query.test.model.diamond.DiamondLeaf;
import com.braintribe.model.processing.query.test.model.diamond.DiamondLeft;
import com.braintribe.model.processing.query.test.model.diamond.DiamondRight;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.NotifyingSessionRunnable;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * Tests that Smood's state is not changed in any way if corrupt manipulations are applied.
 */
public class CorruptManipulationTests extends AbstractSmoodManipulationTests {

	public CorruptManipulationTests() {
		this.defaultManipulationMode = ManipulationTrackingMode.PERSISTENT;
	}

	@Test
	public void simpleManipulationIsUndone() {
		applyCorruptManipulations(session -> {
			Company c = Company.T.create();

			Person p = session.create(Person.T);
			p.setCompany(c);
		});

		assertEntityCountForType(GenericEntity.T, 0);
	}

	@Test
	public void demoHowToTrackForExistingEntity() {
		applyManipulations(session -> {
			Company c = session.create(Company.T);
			c.setId(1L);
		});

		applyManipulations(session -> {
			Company c = Company.T.create();
			c.setId(1L);

			session.attach(c);

			c.setName("Company1");
		});

		assertFindsByProperty(Company.T, "name", "Company1");
	}

	@Test
	public void complexManipulationIsUndone() {
		applyManipulations(session -> {
			Company c = session.create(Company.T);
			c.setId(1L);
			c.setName("Company1");
		});

		assertEntityCountForType(GenericEntity.T, 1 /* The one company */);

		applyCorruptManipulations(session -> {
			// Prepare entity that already exists
			Company c = Company.T.create();
			c.setId(1L);
			session.attach(c);

			// Complex valid manipulations, all have to be undone
			Address a = session.create(Address.T);
			a.setName("Elm Street");

			c.setAddress(a);
			c.getAddressList().add(a);
			c.getAddressSet().add(a);
			c.getAddressMap().put("a", a);

			// Invalid Manipulation
			Person p = Person.T.create();
			c.getPersons().add(p);
		});

		assertEntityCountForType(GenericEntity.T, 1 /* The one company */);

		Company c = smood.getEntity(Company.T, 1L);
		assertThat(c.getAddress()).isNull();
		assertThat(c.getAddressList()).isEmpty();
		assertThat(c.getAddressSet()).isEmpty();
		assertThat(c.getAddressMap()).isEmpty();
	}

	@Test
	public void creatingEntityThatAlreadyExists() {
		applyManipulations(session -> {
			Company c = session.create(Company.T);
			c.setId(1L);
			c.setName("Company1");
		});

		assertEntityCountForType(GenericEntity.T, 1 /* The one company */);

		applyCorruptManipulations(session -> {
			Company c = session.create(Company.T);
			c.setId(1L); // illegal change - id already used
			c.setName("Company2");
		});

		assertEntityCountForType(GenericEntity.T, 1 /* The one company */);
		assertAllEntities(1);

		Company c = smood.getEntity(Company.T, 1L);
		assertThat(c.getName()).isEqualTo("Company1");
	}

	/**
	 * We configure {@link DiamondLeft#getBaseProperty()} and {@link DiamondRight#getBaseProperty()} as unique. Imagine we are adding a
	 * {@link DiamondLeaf} that violates the uniqueness with a DiamondRight which is not a DiamondLeft. If we first update the DiamondLeft index and
	 * then fail on DiamondRight and exit with exception, the DiamondLeft index would be polluted. We have to make sure that is not the case.
	 * 
	 * There are two version of this test, because as of writing the test, this one (R) passes coincidentally, as the very first unique index that is
	 * touched is the DiamondRight index. Thus the DiamondLeft or DiamondLeaf indices are not polluted. The other test that starts with an instance of
	 * DataLeft pollutes the DiamondRight side and the test fails. We keep both so that with any order of index updates at least one fails.
	 */
	@Test
	public void diamondUnique_R() {
		applyManipulations(session -> {
			DiamondRight right = session.create(DiamondRight.T);
			right.setBaseProperty("base");
		});

		applyCorruptManipulations(session -> {
			DiamondLeaf leaf = session.create(DiamondLeaf.T);
			leaf.setBaseProperty("base"); // violates uniqueness
		});

		assertNoEntityIndexed(DiamondLeft.T);
		assertNoEntityIndexed(DiamondLeaf.T);
	}

	/**
	 * @see #diamondUnique_R()
	 */
	@Test
	public void diamondUnique_L() {
		applyManipulations(session -> {
			DiamondLeft left = session.create(DiamondLeft.T);
			left.setBaseProperty("base");
		});

		applyCorruptManipulations(session -> {
			DiamondLeaf leaf = session.create(DiamondLeaf.T);
			leaf.setBaseProperty("base"); // violates uniqueness
		});

		assertNoEntityIndexed(DiamondRight.T);
		assertNoEntityIndexed(DiamondLeaf.T);
	}

	/** Just like {@link #diamondUnique_R()}, but attaching an existing entity instead of causing a CVM. */
	@Test
	public void diamondUnique_attach_R() {
		applyManipulations(session -> {
			DiamondRight right = session.create(DiamondRight.T);
			right.setBaseProperty("base");
		});

		DiamondLeaf leaf = DiamondLeaf.T.create();
		leaf.setBaseProperty("base"); // violates uniqueness

		failIfCanRegister(leaf);

		assertNoEntityIndexed(DiamondLeft.T);
		assertNoEntityIndexed(DiamondLeaf.T);
	}

	/** @see #diamondUnique_attach_R() */
	@Test
	public void diamondUnique_attach_L() {
		applyManipulations(session -> {
			DiamondLeft left = session.create(DiamondLeft.T);
			left.setBaseProperty("base");
		});

		DiamondLeaf leaf = DiamondLeaf.T.create();
		leaf.setBaseProperty("base"); // violates uniqueness

		failIfCanRegister(leaf);
		assertNoEntityIndexed(DiamondRight.T);
		assertNoEntityIndexed(DiamondLeaf.T);
	}

	@Test
	public void diamondUnique_changeValueR() {
		applyManipulations(session -> {
			DiamondRight right = session.create(DiamondRight.T);
			right.setId(66L);
			right.setBaseProperty("base");

			DiamondLeaf leaf = session.create(DiamondLeaf.T);
			leaf.setId(99L);
			leaf.setBaseProperty("base2");
		});

		applyCorruptManipulations(session -> {
			DiamondLeaf leaf = DiamondLeaf.T.create();
			leaf.setId(99L);
			leaf.attach(session);

			leaf.setBaseProperty("base");// violates uniqueness
		});

		// We cannot find the Leaf/Left by value "base"
		assertNoEntityIndexed(DiamondLeft.T);
		assertNoEntityIndexed(DiamondLeaf.T);

		// .. but we cannot find the Leaf/Left by the original value "base2"
		assertEntityIndexed(DiamondLeft.T, "base2");
		assertEntityIndexed(DiamondLeaf.T, "base2");
	}

	private void failIfCanRegister(DiamondBase entity) {
		try {
			smood.registerEntity(entity, false);
			fail("Excpetion was expected.");

		} catch (Exception e) {
			// empty
		}
	}

	private void assertNoEntityIndexed(EntityType<? extends DiamondBase> typeToQuery) {
		assertThat(queryByBaseProperty(typeToQuery, "base")).isEmpty();
	}

	private void assertEntityIndexed(EntityType<? extends DiamondBase> typeToQuery, String baseValue) {
		assertThat(queryByBaseProperty(typeToQuery, baseValue)).isNotEmpty();
	}

	private List<GenericEntity> queryByBaseProperty(EntityType<? extends DiamondBase> typeToQuery, String baseValue) {
		EntityQuery query = EntityQueryBuilder.from(typeToQuery).where().property(DiamondBase.baseProperty).eq(baseValue).done();
		return smood.queryEntities(query).getEntities();
	}

	protected void applyCorruptManipulations(NotifyingSessionRunnable r) {
		try {
			applyManipulations(r);

			Assertions.fail("Exception was expected due to a corrupt manipulation.");

		} catch (Exception e) {
			return;
		}
	}

	/**
	 * This gets the entities based on the internal referenceByEntity map, and is thus different than {@link #assertEntityCountForType}.
	 */
	private void assertAllEntities(int count) {
		Set<GenericEntity> entities = smood.getAllEntities();

		if (count == 0) {
			BtAssertions.assertThat(entities).isNullOrEmpty();
		} else {
			BtAssertions.assertThat(entities).isNotNull().hasSize(count);
		}
	}

}