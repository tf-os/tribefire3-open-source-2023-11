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
package com.braintribe.model.processing.query.eval.set;

import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.SourceSet;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class CartesianProductTests extends AbstractEvalTupleSetTests {

	private Person pA, pB;
	private Company cA, cB;

	private SourceSet personSet;
	private SourceSet companySet;

	private CartesianProduct cartesianProduct;

	@Test
	public void firstOperandEmpty() throws Exception {
		buildCompany();
		buildQueryPlan();

		evaluate(cartesianProduct);

		assertNoMoreTuples();
	}

	@Test
	public void secondOperandEmpty() throws Exception {
		buildPerson();
		buildQueryPlan();

		evaluate(cartesianProduct);

		assertNoMoreTuples();
	}

	@Test
	public void productOftwoNonEmptySets() throws Exception {
		buildPerson();
		buildCompany();
		buildQueryPlan();

		evaluate(cartesianProduct);

		assertContainsTuple(pA, cA);
		assertContainsTuple(pA, cB);
		assertContainsTuple(pB, cA);
		assertContainsTuple(pB, cB);
		assertNoMoreTuples();
	}

	private void buildPerson() {
		registerAtSmood(pA = ModelBuilder.person("personA"));
		registerAtSmood(pB = ModelBuilder.person("personB"));
	}

	private void buildCompany() {
		registerAtSmood(cA = ModelBuilder.company("companyA"));
		registerAtSmood(cB = ModelBuilder.company("companyB"));
	}

	private void buildQueryPlan() {
		personSet = builder.sourceSet(Person.class);
		companySet = builder.sourceSet(Company.class);

		cartesianProduct = builder.cartesianProduct(personSet, companySet);
	}

}
