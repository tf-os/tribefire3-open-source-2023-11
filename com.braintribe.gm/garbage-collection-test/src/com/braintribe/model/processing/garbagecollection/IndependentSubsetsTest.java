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
package com.braintribe.model.processing.garbagecollection;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;
import com.braintribe.utils.genericmodel.ReachableEntitiesFinder;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.GraphTools;

public class IndependentSubsetsTest {

	protected BasicPersistenceGmSession session = null;

	@Before
	public void setUp() {
		this.session = createSession();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIndependentSubsets1() {

		final Set<GenericEntity> nodes = new HashSet<GenericEntity>();
		final ComplexEntity a = createNode("a");
		final ComplexEntity b = createNode("b");
		nodes.addAll(CollectionTools.getSet(a, b));

		Set<Set<GenericEntity>> indepentedSubsets = GraphTools.findIndependentSubsets(nodes,
				new ReachableEntitiesFinder());
		Set<Set<GenericEntity>> expectedSubsets = CollectionTools.getSet(getSet(a), getSet(b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// Link a->b
		a.setComplexEntityProperty(b);
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(a, b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// Add c, d with c->d
		final ComplexEntity c = createNode("c");
		final ComplexEntity d = createNode("d");
		c.setComplexEntityProperty(d);
		nodes.addAll(CollectionTools.getSet(c, d));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// Add d, f where c->e
		final ComplexEntity e = createNode("e");
		final ComplexEntity f = createNode("f");
		c.setComplexEntityList(CollectionTools.getList(e));
		nodes.addAll(CollectionTools.getSet(e, f));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d, e), getSet(f));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// d->f
		d.setComplexEntityProperty(f);
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d, e, f));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// f->c Creating cycle
		f.setComplexEntityProperty(c);
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d, e, f));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// e->f Creating second cycle
		e.setComplexEntityProperty(f);
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d, e, f));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// Add k, l with k->l and l->k
		final ComplexEntity k = createNode("k");
		final ComplexEntity l = createNode("l");
		k.setComplexEntityProperty(l);
		l.setComplexEntityProperty(k);
		nodes.addAll(CollectionTools.getSet(k, l));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(c, d, e, f), getSet(k, l));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// k->a
		k.setComplexEntityList(CollectionTools.getList(a));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(c, d, e, f), getSet(k, l, a, b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// k->d
		k.setComplexEntityList(CollectionTools.getList(a, d));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(c, d, e, f, k, l, a, b), getSet(c, d, e, f, k, l, a, b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

	}

	/**
	 * This methods tests the graph a->b,c forcing the algorithm to start from every possible node
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIndependentSubsets2() {

		final Set<GenericEntity> nodes = new HashSet<>();
		final ComplexEntity a = createNode("a");
		final ComplexEntity b = createNode("b");
		final ComplexEntity c = createNode("c");
		nodes.addAll(CollectionTools.getSet(a, b, c));

		Set<Set<GenericEntity>> indepentedSubsets = GraphTools.findIndependentSubsets(nodes,
				new ReachableEntitiesFinder());
		Set<Set<GenericEntity>> expectedSubsets = CollectionTools.getSet(getSet(a), getSet(b), getSet(c));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// first case : a->b,c
		a.setComplexEntityList(CollectionTools.getList(b, c));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b, c), getSet(a, b, c));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// second case : b->a,c
		a.setComplexEntityList(null);
		b.setComplexEntityList(CollectionTools.getList(a, c));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b, c), getSet(a, b, c));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		// third case : c->a,b
		b.setComplexEntityList(null);
		c.setComplexEntityList(CollectionTools.getList(a, b));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b, c), getSet(a, b, c));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();
	}

	/**
	 * This methods tests the graph a->b,c considering the "node space" A = {a, b}
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testIndependentSubsets3() {

		final Set<GenericEntity> nodes = new HashSet<>();
		final ComplexEntity a = createNode("a");
		final ComplexEntity b = createNode("b");
		final ComplexEntity c = createNode("c");
		nodes.addAll(CollectionTools.getSet(a, b));

		Set<Set<GenericEntity>> indepentedSubsets = GraphTools.findIndependentSubsets(nodes,
				new ReachableEntitiesFinder());
		Set<Set<GenericEntity>> expectedSubsets = CollectionTools.getSet(getSet(a), getSet(b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();

		a.setComplexEntityList(CollectionTools.getList(b, c));
		indepentedSubsets = GraphTools.findIndependentSubsets(nodes, new ReachableEntitiesFinder());
		expectedSubsets = CollectionTools.getSet(getSet(a, b), getSet(a, b));
		assertThat(CollectionUtils.isEqualCollection(indepentedSubsets, expectedSubsets))
				.as("The sets are not equal!").isTrue();
	}

	private static Set<GenericEntity> getSet(final GenericEntity... genericEntities) {
		return CollectionTools.getSet(genericEntities);
	}

	private ComplexEntity createNode(final String name) {
		final ComplexEntity node = this.session.create(ComplexEntity.T);
		node.setStringProperty(name);
		return node;
	}

	private static BasicPersistenceGmSession createSession() {
		final IncrementalAccess access = TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel());
		final BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(access);
		return session;
	}
}
