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
package com.braintribe.model.processing.traversing.test.legacy;

import static com.braintribe.model.generic.typecondition.TypeConditions.and;
import static com.braintribe.model.generic.typecondition.TypeConditions.hasCollectionElement;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.processing.traversing.test.builder.Builder;
import com.braintribe.model.processing.traversing.test.builder.EntityPrinter;
import com.braintribe.model.processing.traversing.test.model.TraverseeA;

/**
 * 
 */
public class SimpleCloningTests {

	private TraverseeA root;
	private TraverseeA clonedRoot;

	@Before
	public void prepareRoot() {
		TraverseeA qs = Builder.traverseeA("Queen of Spades").create();
		TraverseeA qc = Builder.traverseeA("Queen of Clubs").create();
		TraverseeA qd = Builder.traverseeA("Queen of Diamonds").create();
		TraverseeA qh = Builder.traverseeA("Queen of Hearts").create();

		TraverseeA ks = Builder.traverseeA("King of Spades").addToSetA(qs).addToListA(qs).create();
		TraverseeA kc = Builder.traverseeA("King of Clubs").addToSetA(qc).addToListA(qc).create();
		TraverseeA kd = Builder.traverseeA("King of Diamonds").addToSetA(qd).addToListA(qd).create();
		TraverseeA kh = Builder.traverseeA("King of Hearts").addToSetA(qh).addToListA(qh).create();

		root = Builder.traverseeA("Ace of Whatever").someA(kh).addToSetA(ks, kc, kh, kd).addToListA(ks, kc, kh, kd).create();
	}

	@Test
	public void noPropertiesLoaded() throws Exception {
		// @formatter:off
		TraversingCriterion tc = TC.create()
						.joker()
				.done();
		// @formatter:on

		cloneWith(tc);
	}

	@Test
	public void simpleEnumLoaded() throws Exception {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.negation()
					.typeCondition(or(isKind(TypeKind.simpleType), isKind(TypeKind.enumType)))
				.done();
		// @formatter:on
		
		cloneWith(tc);
	}

	@Test
	public void singleEntityReferenceLoaded() throws Exception {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.negation()
					.typeCondition(or(isKind(TypeKind.simpleType), isKind(TypeKind.entityType)))
				.done();
		// @formatter:on
		
		cloneWith(tc);
	}

	@Test
	public void setLoaded() throws Exception {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.negation()
					.typeCondition(
						or(
							isKind(TypeKind.simpleType),
							and(
								isKind(TypeKind.setType),
								hasCollectionElement(isKind(TypeKind.entityType))
								)
							)
					)
				.done();
		// @formatter:on
		
		cloneWith(tc);
	}

	@Test
	public void rootSetLoaded() throws Exception {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.negation()
					.disjunction()
						.typeCondition(TypeConditions.isKind(TypeKind.simpleType))
						.pattern()
							.root()
							.entity()
							.property("setA")
						.close()
					.close()
				.done();
		// @formatter:on
		
		cloneWith(tc);
	}
	
	private void cloneWith(TraversingCriterion tc) {
		cloneWith(tc, StrategyOnCriterionMatch.partialize);
	}

	private void cloneWith(TraversingCriterion tc, StrategyOnCriterionMatch strategy) {
		EntityType<TraverseeA> et = GMF.getTypeReflection().getEntityType(TraverseeA.class);

		StandardCloningContext cc = new StandardCloningContext();
		cc.setMatcher(matcher(tc));

		clonedRoot = (TraverseeA) et.clone(cc, root, strategy);

		EntityPrinter.print(clonedRoot);
	}

	private Matcher matcher(TraversingCriterion tc) {
		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);

		return matcher;
	}

}
