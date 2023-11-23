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
package com.braintribe.model.generic.pr.criteria.stringifier;

import static com.braintribe.model.generic.typecondition.stringifier.TypeConditionStringifier.containsJunction;
import static com.braintribe.utils.lcd.StringTools.isEmpty;

import java.util.List;

import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.DisjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.JunctionCriterion;
import com.braintribe.model.generic.pr.criteria.ListElementCriterion;
import com.braintribe.model.generic.pr.criteria.MapCriterion;
import com.braintribe.model.generic.pr.criteria.MapEntryCriterion;
import com.braintribe.model.generic.pr.criteria.MapKeyCriterion;
import com.braintribe.model.generic.pr.criteria.MapValueCriterion;
import com.braintribe.model.generic.pr.criteria.NegationCriterion;
import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.PlaceholderCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.pr.criteria.RecursionCriterion;
import com.braintribe.model.generic.pr.criteria.SetElementCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.pr.criteria.TypedCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.stringifier.TypeConditionStringifier;

/**
 * @author peter.gazdik
 */
public class TraversingCriteriaStringifier extends AbstractStringifier {

	public static String stringify(TraversingCriterion tc) {
		TraversingCriteriaStringifier stringifier = new TraversingCriteriaStringifier();
		stringifier.view(tc);
		return stringifier.builder.toString();
	}

	public TraversingCriteriaStringifier() {
		super();
	}

	public TraversingCriteriaStringifier(AbstractStringifier parent) {
		super(parent);
	}

	private void view(TraversingCriterion tc) {
		if (tc == null) {
			print("Null-TC");
			return;
		}

		switch (tc.criterionType()) {
			// constant
			case JOKER:
				print("joker");
				return;

			// root
			case ROOT:
				print("root");
				return;

			// model
			case ENTITY:
				view((EntityCriterion) tc);
				return;
			case PROPERTY:
				view((PropertyCriterion) tc);
				return;

			// model-collections
			case LIST_ELEMENT:
				view((ListElementCriterion) tc);
				return;
			case SET_ELEMENT:
				view((SetElementCriterion) tc);
				return;
			case MAP:
				view((MapCriterion) tc);
				return;
			case MAP_ENTRY:
				view((MapEntryCriterion) tc);
				return;
			case MAP_KEY:
				view((MapKeyCriterion) tc);
				return;
			case MAP_VALUE:
				view((MapValueCriterion) tc);
				return;

			// logic
			case CONJUNCTION:
				view((ConjunctionCriterion) tc);
				return;
			case DISJUNCTION:
				view((DisjunctionCriterion) tc);
				return;
			case NEGATION:
				view((NegationCriterion) tc);
				return;

			// structure
			case PATTERN:
				view((PatternCriterion) tc);
				return;
			case RECURSION:
				view((RecursionCriterion) tc);
				return;

			// type/value conditions
			case TYPE_CONDITION:
				view((TypeConditionCriterion) tc);
				return;
			case VALUE_CONDITION:
				view((ValueConditionCriterion) tc);
				return;

			// placehodler
			case PLACEHOLDER:
				view((PlaceholderCriterion) tc);
				return;

			// bullshit
			case BASIC:
			case PROPERTY_TYPE:
			default:
				print(tc.toString());
		}
	}

	private void view(EntityCriterion tc) {
		print("entity");
		printTypeSignature(tc);
	}

	private void view(PropertyCriterion tc) {
		print("property");
		printOptionalValue(tc.getPropertyName(), "[", "]");
		printTypeSignature(tc);
	}

	private void view(ListElementCriterion tc) {
		print("listElement");
		printTypeSignature(tc);
	}

	private void view(SetElementCriterion tc) {
		print("setElement");
		printTypeSignature(tc);
	}

	private void view(MapCriterion tc) {
		print("map");
		printTypeSignature(tc);
	}

	private void view(MapEntryCriterion tc) {
		print("mapEntry");
		printTypeSignature(tc);
	}

	private void view(MapKeyCriterion tc) {
		print("mapKey");
		printTypeSignature(tc);
	}

	private void view(MapValueCriterion tc) {
		print("mapValue");
		printTypeSignature(tc);
	}

	private void view(ConjunctionCriterion tc) {
		viewJunction("AND", tc);
	}

	private void view(DisjunctionCriterion tc) {
		viewJunction("OR", tc);
	}

	private void viewJunction(String andOrOr, JunctionCriterion tc) {
		print(andOrOr);
		printCriteriaList(tc.getCriteria());
	}

	private void view(NegationCriterion tc) {
		print("NOT ");
		view(tc.getCriterion());
	}

	private void view(PatternCriterion tc) {
		print("pattern");
		printCriteriaList(tc.getCriteria());
	}

	private void printCriteriaList(List<TraversingCriterion> criteria) {
		levelUp();
		for (TraversingCriterion _tc : criteria) {
			println("");
			print("- ");
			view(_tc);
		}
		levelDown();
	}

	private void view(RecursionCriterion tc) {
		print("recursion[");
		print(numOrQuestionMark(tc.getMinRecursion()));
		print(" - ");
		print(numOrQuestionMark(tc.getMaxRecursion()));
		print("]");
		view(tc.getCriterion());
	}

	private String numOrQuestionMark(Integer i) {
		return i != null ? i.toString() : "?";
	}

	private void view(TypeConditionCriterion tc) {
		print("type");

		TypeCondition typeCondition = tc.getTypeCondition();

		if (containsJunction(typeCondition)) {
			println("");

			levelUp();
			TypeConditionStringifier.stringify(typeCondition, this);
			levelDown();

			println("");

		} else {
			print(" ");
			print(TypeConditionStringifier.stringify(typeCondition));
		}
	}

	private void view(ValueConditionCriterion tc) {
		print("(");
		print(tc.getPropertyPath());
		print(" ");
		print(tc.getOperator().name());
		print(" ");
		print("" + tc.getOperand());
	}

	private void view(PlaceholderCriterion tc) {
		print("placeholder ");
		print(tc.getName());
	}

	private void printTypeSignature(TypedCriterion tc) {
		printOptionalValue(tc.getTypeSignature(), "<", ">");
	}

	private void printOptionalValue(String value, String prefix, String suffix) {
		if (!isEmpty(value)) {
			print(prefix);
			print(value);
			print(suffix);
		}
	}


}
