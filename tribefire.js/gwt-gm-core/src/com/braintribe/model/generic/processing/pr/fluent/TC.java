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
package com.braintribe.model.generic.processing.pr.fluent;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import java.util.function.Consumer;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.pr.criteria.JunctionCriterion;
import com.braintribe.model.generic.pr.criteria.NegationCriterion;
import com.braintribe.model.generic.pr.criteria.PlaceholderCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.typecondition.TypeCondition;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(name="tc", namespace=GmCoreApiInteropNamespaces.gm)
@SuppressWarnings("unusable-by-js")
public class TC {

	public static CriterionBuilder<TC> create() {
		TC tc = new TC();
		return tc.getCriterionBuilder(tc, criterion -> tc.result = criterion);
	}

	@JsIgnore
	protected TraversingCriterion result;

	@JsIgnore
	protected TC() {
	}

	public TraversingCriterion done() {
		return result;
	}

	@JsIgnore
	protected CriterionBuilder<TC> getCriterionBuilder(TC tc, Consumer<TraversingCriterion> receiver) {
		return new CriterionBuilder<TC>(tc, receiver);
	}

	// Temporary, please don't use yet
	
	// ############################################################
	// ## . . . . . . . . Simple builder methods . . . . . . . . ##
	// ############################################################

	public static PlaceholderCriterion placeholder(String name) {
		PlaceholderCriterion result = PlaceholderCriterion.T.create();
		result.setName(name);
		return result;
	}

	public static NegationCriterion negation(TraversingCriterion tc) {
		NegationCriterion result = NegationCriterion.T.create();
		result.setCriterion(tc);
		return result;
	}

	public static TypeConditionCriterion typeCondition(TypeCondition typeCondition) {
		TypeConditionCriterion result = TypeConditionCriterion.T.create();
		result.setTypeCondition(typeCondition);
		return result;
	}

	@JsMethod(name="containsTraversionCriterion")
	public static boolean containsPlaceholder(TraversingCriterion tc) {
		if (tc == null)
			return false;

		switch (tc.criterionType()) {
			case CONJUNCTION:
			case DISJUNCTION:
				return containsPlaceholder((JunctionCriterion) tc);

			case NEGATION:
				return containsPlaceholder(((NegationCriterion) tc).getCriterion());

			case PLACEHOLDER:
				return true;

			default:
				return false;
		}
	}

	@JsMethod(name="containsJunctionCriterion")
	public static boolean containsPlaceholder(JunctionCriterion jtc) {
		for (TraversingCriterion tc : nullSafe(jtc.getCriteria()))
			if (containsPlaceholder(tc))
				return true;
		return false;
	}

}
