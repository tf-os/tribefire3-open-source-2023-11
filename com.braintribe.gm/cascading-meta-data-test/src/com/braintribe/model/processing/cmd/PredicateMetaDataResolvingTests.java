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
package com.braintribe.model.processing.cmd;

import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.EXPLICIT_PREDICATE;
import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.EXPLICIT_PREDICATE_ERASURE;
import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.PREDICATE;
import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.PREDICATE_ERASURE;
import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.UNMODIFIABLE_MANDTORY_PROPETY;
import static com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider.UNMODIFIABLE_PROPETY;

import java.util.function.Supplier;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.meta.data.prompt.Confidential;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.cmd.test.model.Person;
import com.braintribe.model.processing.cmd.test.provider.PredicateMdProvider;
import com.braintribe.model.processing.meta.cmd.extended.ModelMdDescriptor;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class PredicateMetaDataResolvingTests extends MetaDataResolvingTestBase {

	// #######################################
	// ## . . . . . . Predicate . . . . . . ##
	// #######################################

	/** @see PredicateMdProvider#addPredicateMd */
	@Test
	public void predicate() {
		assertTrue(getMetaData().useCase(PREDICATE).is(Visible.T));
	}

	/** @see PredicateMdProvider#addPredicateErasureMd */
	@Test
	public void predicateErasure() {
		assertFalse(getMetaData().useCase(PREDICATE_ERASURE).is(Visible.T));
	}

	@Test
	public void predicateDefault() {
		assertTrue(getMetaData().useCase("DEFAULT").is(Visible.T));
	}

	// #######################################
	// ## . . . . Explicit Predicate . . . .##
	// #######################################

	/** @see PredicateMdProvider#addExplicitPredicateMd */
	@Test
	public void explicitPredicate() {
		assertTrue(getMetaData().useCase(EXPLICIT_PREDICATE).is(Confidential.T));
	}

	/** @see PredicateMdProvider#addExplicitPredicateErasureMd */
	@Test
	public void explicitPredicateErasure() {
		assertFalse(getMetaData().useCase(EXPLICIT_PREDICATE_ERASURE).is(Confidential.T));
	}

	@Test
	public void explicitPredicateDefault() {
		assertFalse(getMetaData().useCase("DEFAULT").is(Confidential.T));
	}

	// #######################################
	// ## . . . Predicate - Extended . . . .##
	// #######################################

	@Test
	public void predicateExtended() {
		ModelMdDescriptor mmdd = getMetaData().useCase(PREDICATE).meta(Visible.T).exclusiveExtended();
		assertTrue(mmdd.getIsTrue());
	}

	@Test
	public void predicateErasureExtended() {
		ModelMdDescriptor mmdd = getMetaData().useCase(PREDICATE_ERASURE).meta(Visible.T).exclusiveExtended();
		assertFalse(mmdd.getIsTrue());
	}

	// #######################################
	// ## . . . . Special treatment . . . . ##
	// #######################################

	/** @see PredicateMdProvider#addPredicateMd */
	@Test
	public void unmodifiableOnly() {
		assertFalse(getMetaData().entityType(Person.T).property(UNMODIFIABLE_PROPETY).is(Modifiable.T));
		assertFalse(getMetaData().entityType(Person.T).property(UNMODIFIABLE_PROPETY).preliminary(true).is(Modifiable.T));
	}

	/** @see PredicateMdProvider#addPredicateErasureMd */
	@Test
	public void unmodifiableAndMandatory() {
		assertFalse(getMetaData().entityType(Person.T).property(UNMODIFIABLE_MANDTORY_PROPETY).is(Modifiable.T));
		assertTrue(getMetaData().entityType(Person.T).property(UNMODIFIABLE_MANDTORY_PROPETY).preliminary(true).is(Modifiable.T));
	}

	// #######################################
	// ## . . . . . . Assertions . . . . . .##
	// #######################################

	private void assertTrue(boolean resolvedPredicateValue) {
		BtAssertions.assertThat(resolvedPredicateValue).isTrue();
	}

	private void assertFalse(boolean resolvedPredicateValue) {
		BtAssertions.assertThat(resolvedPredicateValue).isFalse();
	}

	@Override
	protected Supplier<GmMetaModel> getModelProvider() {
		return new PredicateMdProvider();
	}

}
