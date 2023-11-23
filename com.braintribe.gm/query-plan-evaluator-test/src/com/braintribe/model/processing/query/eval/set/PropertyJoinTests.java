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

import static com.braintribe.model.processing.query.eval.set.base.TupleSetBuilder.valueProperty;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSetTests;
import com.braintribe.model.processing.query.eval.set.base.ModelBuilder;
import com.braintribe.model.processing.query.test.model.Company;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * Tests filtered set with the underlying source-set (for {@link Person} entities).
 */
public class PropertyJoinTests extends AbstractEvalTupleSetTests {

	private Owner pA, pB, pC;
	private Company cA1, cA2, cB1, cB2, cB3, cB4;

	private SourceSet personSet;

	/** Builds {@link Owner} with different "company" properties. Joins to these properties are then being tested. */
	@Before
	public void buildData() {
		registerAtSmood(pA = ModelBuilder.owner("owner1"));
		registerAtSmood(pB = ModelBuilder.owner("owner2"));
		registerAtSmood(pC = ModelBuilder.owner("owner3"));
		registerAtSmood(cA1 = ModelBuilder.company("companyA1"));
		registerAtSmood(cA2 = ModelBuilder.company("companyA2"));
		registerAtSmood(cB1 = ModelBuilder.company("companyB1"));
		registerAtSmood(cB2 = ModelBuilder.company("companyB2"));
		registerAtSmood(cB3 = ModelBuilder.company("companyB3")); // for right joins
		registerAtSmood(cB4 = ModelBuilder.company("companyB4")); // for right joins

		pA.setCompany(cA1);
		pB.setCompany(cB1);
		pC.setCompany(null);

		pA.setCompanySet(asSet(cA1, cA2, null));
		pB.setCompanySet(asSet(cB1, cB2));
		pC.setCompanySet(null);

		pA.setCompanyList(Arrays.asList(cA1, cA2, null));
		pB.setCompanyList(Arrays.asList(cB1, cB2));
		pC.setCompanyList(null);

		pA.setCompanyMap(CollectionTools2.<String, Company> asMap("cA1", cA1, "cA2", cA2, "cA3", null));
		pB.setCompanyMap(CollectionTools2.<String, Company> asMap("cB1", cB1, "cB2", cB2));
		pC.setCompanyMap(null);

		personSet = builder.sourceSet(Owner.class);
	}

	// ################################
	// ## . . . . . Entity . . . . . ##
	// ################################

	@Test
	public void entityJoin() throws Exception {
		evaluate(builder.entityJoin(personSet, valueProperty(personSet, "company"), null));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pB, cB1);
		assertNoMoreTuples();
	}

	@Test
	public void entityLeftJoin() throws Exception {
		evaluate(builder.entityJoin(personSet, valueProperty(personSet, "company"), JoinKind.left));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pB, cB1);
		assertContainsTuple(pC, null);
		assertNoMoreTuples();
	}

	@Test
	public void entityRightJoin() throws Exception {
		evaluate(builder.entityJoin(personSet, valueProperty(personSet, "company"), JoinKind.right));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pB, cB1);
		assertContainsTuple(null, cA2);
		assertContainsTuple(null, cB2);
		assertContainsTuple(null, cB3);
		assertContainsTuple(null, cB4);
		assertNoMoreTuples();
	}

	// ################################
	// ## . . . . . . Set . . . . . .##
	// ################################

	@Test
	public void setJoin() throws Exception {
		evaluate(builder.setJoin(personSet, valueProperty(personSet, "companySet"), null));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pA, cA2);
		assertContainsTuple(pA, null);
		assertContainsTuple(pB, cB1);
		assertContainsTuple(pB, cB2);
		assertNoMoreTuples();
	}

	@Test
	public void setLeftJoin() throws Exception {
		evaluate(builder.setJoin(personSet, valueProperty(personSet, "companySet"), JoinKind.left));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pA, cA2);
		assertContainsTuple(pA, null);
		assertContainsTuple(pB, cB1);
		assertContainsTuple(pB, cB2);
		assertContainsTuple(pC, null);
		assertNoMoreTuples();
	}

	@Test
	public void setRightJoin() throws Exception {
		evaluate(builder.setJoin(personSet, valueProperty(personSet, "companySet"), JoinKind.right));

		assertContainsTuple(pA, cA1);
		assertContainsTuple(pA, cA2);
		assertContainsTuple(pA, null);
		assertContainsTuple(pB, cB1);
		assertContainsTuple(pB, cB2);
		assertContainsTuple(null, cB3);
		assertContainsTuple(null, cB4);
		assertNoMoreTuples();
	}

	// ################################
	// ## . . . . . . List . . . . . ##
	// ################################

	@Test
	public void listJoin() throws Exception {
		evaluate(builder.listJoin(personSet, valueProperty(personSet, "companyList"), null));

		assertContainsTuple(pA, 0, cA1);
		assertContainsTuple(pA, 1, cA2);
		assertContainsTuple(pA, 2, null);
		assertContainsTuple(pB, 0, cB1);
		assertContainsTuple(pB, 1, cB2);
		assertNoMoreTuples();
	}

	@Test
	public void listLeftJoin() throws Exception {
		evaluate(builder.listJoin(personSet, valueProperty(personSet, "companyList"), JoinKind.left));

		assertContainsTuple(pA, 0, cA1);
		assertContainsTuple(pA, 1, cA2);
		assertContainsTuple(pA, 2, null);
		assertContainsTuple(pB, 0, cB1);
		assertContainsTuple(pB, 1, cB2);
		assertContainsTuple(pC, null, null);
		assertNoMoreTuples();
	}

	@Test
	public void listRightJoin() throws Exception {
		evaluate(builder.listJoin(personSet, valueProperty(personSet, "companyList"), JoinKind.right));

		assertContainsTuple(pA, 0, cA1);
		assertContainsTuple(pA, 1, cA2);
		assertContainsTuple(pA, 2, null);
		assertContainsTuple(pB, 0, cB1);
		assertContainsTuple(pB, 1, cB2);
		assertContainsTuple(null, null, cB3);
		assertContainsTuple(null, null, cB4);
		assertNoMoreTuples();
	}

	// ################################
	// ## . . . . . . Map . . . . . .##
	// ################################

	@Test
	public void mapJoin() throws Exception {
		evaluate(builder.mapJoin(personSet, valueProperty(personSet, "companyMap"), null));

		assertContainsTuple(pA, "cA1", cA1);
		assertContainsTuple(pA, "cA2", cA2);
		assertContainsTuple(pA, "cA3", null);
		assertContainsTuple(pB, "cB1", cB1);
		assertContainsTuple(pB, "cB2", cB2);
		assertNoMoreTuples();
	}

	@Test
	public void mapLeftJoin() throws Exception {
		evaluate(builder.mapJoin(personSet, valueProperty(personSet, "companyMap"), JoinKind.left));

		assertContainsTuple(pA, "cA1", cA1);
		assertContainsTuple(pA, "cA2", cA2);
		assertContainsTuple(pA, "cA3", null);
		assertContainsTuple(pB, "cB1", cB1);
		assertContainsTuple(pB, "cB2", cB2);
		assertContainsTuple(pC, null, null);
		assertNoMoreTuples();
	}

	@Test
	public void mapRightJoin() throws Exception {
		evaluate(builder.mapJoin(personSet, valueProperty(personSet, "companyMap"), JoinKind.right));

		assertContainsTuple(pA, "cA1", cA1);
		assertContainsTuple(pA, "cA2", cA2);
		assertContainsTuple(pA, "cA3", null);
		assertContainsTuple(pB, "cB1", cB1);
		assertContainsTuple(pB, "cB2", cB2);
		assertContainsTuple(null, null, cB3);
		assertContainsTuple(null, null, cB4);
		assertNoMoreTuples();
	}

}
