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
package com.braintribe.model.generic.typecondition.stringifier;

import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;

import com.braintribe.model.generic.tools.AbstractStringifier;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.basic.IsType;
import com.braintribe.model.generic.typecondition.basic.IsTypeKind;
import com.braintribe.model.generic.typecondition.logic.TypeConditionConjunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionDisjunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionJunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionNegation;
import com.braintribe.model.generic.typecondition.origin.IsDeclaredIn;
import com.braintribe.model.generic.typecondition.origin.IsRelatedToTypeDeclaredIn;
import com.braintribe.model.generic.typecondition.param.CollectionElementCondition;
import com.braintribe.model.generic.typecondition.param.MapKeyCondition;
import com.braintribe.model.generic.typecondition.param.MapValueCondition;
import com.braintribe.model.generic.typecondition.param.TypeParameterCondition;

/**
 * @author peter.gazdik
 */
public class TypeConditionStringifier extends AbstractStringifier {

	public static String stringify(TypeCondition tc) {
		TypeConditionStringifier stringifier = new TypeConditionStringifier();
		stringifier.view(tc);
		return stringifier.builder.toString();
	}

	public static void stringify(TypeCondition tc, AbstractStringifier parent) {
		new TypeConditionStringifier(parent).view(tc);
	}

	private TypeConditionStringifier() {
		super();
	}

	private TypeConditionStringifier(AbstractStringifier parent) {
		super(parent);
	}

	private void view(TypeCondition tc) {
		if (tc == null) {
			print("Null-TC");
			return;
		}

		switch (tc.tcType()) {
			// basic
			case isAnyType:
				print("isAnyType");
				return;
			case isAssignableTo:
				view((IsAssignableTo) tc);
				return;
			case isType:
				view((IsType) tc);
				return;
			case isTypeKind:
				view((IsTypeKind) tc);
				return;

			// logic
			case conjunction:
				view((TypeConditionConjunction) tc);
				return;
			case disjunction:
				view((TypeConditionDisjunction) tc);
				return;
			case negation:
				view((TypeConditionNegation) tc);
				return;

			// origin
			case isDeclaredIn:
				view((IsDeclaredIn) tc);
				return;
			case isRelatedToTypeDeclaredIn:
				view((IsRelatedToTypeDeclaredIn) tc);
				return;

			// param
			case collectionElementCondition:
				view((CollectionElementCondition) tc);
				return;
			case mapKeyCondition:
				view((MapKeyCondition) tc);
				return;
			case mapValueCondition:
				view((MapValueCondition) tc);
				return;

			// CF
			default:
				print(tc.toString());
		}
	}

	// basic

	private void view(IsAssignableTo tc) {
		print("instanceof " + tc.getTypeSignature());
	}

	private void view(IsType tc) {
		print("isType " + tc.getTypeSignature());
	}

	private void view(IsTypeKind tc) {
		print("is " + tc.getKind());
	}

	// logic

	private void view(TypeConditionConjunction tc) {
		viewJunction("AND", tc);
	}

	private void view(TypeConditionDisjunction tc) {
		viewJunction("OR", tc);
	}

	private void viewJunction(String andOrOr, TypeConditionJunction tcJunction) {
		if (containsOtherJunctions(tcJunction)) {
			print(andOrOr);

			levelUp();
			for (TypeCondition tc : nullSafe(tcJunction.getOperands())) {
				println("");
				print("- ");
				view(tc);
			}
			levelDown();

		} else {
			andOrOr = " " + andOrOr + " ";
			boolean first = true;
			for (TypeCondition tc : nullSafe(tcJunction.getOperands())) {
				if (first)
					first = false;
				else
					print(andOrOr);

				view(tc);
			}
		}
	}

	private boolean containsOtherJunctions(TypeConditionJunction tcJunction) {
		for (TypeCondition tc : nullSafe(tcJunction.getOperands())) {
			if (containsJunction(tc))
				return true;
		}

		return false;
	}

	private void view(TypeConditionNegation tc) {
		print("NOT ");
		view(tc.getOperand());
	}

	// origin

	private void view(IsDeclaredIn tc) {
		print("declaredIn " + tc.getModelName() + "");
	}

	private void view(IsRelatedToTypeDeclaredIn tc) {
		print("related2TypeDeclaredIn " + tc.getModelName());
	}

	// param

	private void view(CollectionElementCondition tc) {
		viewNestedCondition("ColElement", tc.getCondition());
	}

	private void view(MapKeyCondition tc) {
		viewNestedCondition("MapKey", tc.getCondition());
	}

	private void view(MapValueCondition tc) {
		viewNestedCondition("MapValue", tc.getCondition());
	}

	private void viewNestedCondition(String parentPrefix, TypeCondition tc) {
		if (containsJunction(tc)) {
			println(parentPrefix);
			println(":");

			levelUp();
			view(tc);
			levelDown();

		} else {
			print(parentPrefix);
			print(" ");
			view(tc);
		}
	}

	public static boolean containsJunction(TypeCondition tc) {
		switch (tc.tcType()) {
			// basic + origin
			case isAnyType:
			case isAssignableTo:
			case isType:
			case isTypeKind:
			case isDeclaredIn:
			case isRelatedToTypeDeclaredIn:
				return false;

			case negation:
				return containsJunction(((TypeConditionNegation) tc).getOperand());

			case collectionElementCondition:
			case mapKeyCondition:
			case mapValueCondition:
				return containsJunction(((TypeParameterCondition) tc).getCondition());

			case conjunction:
			case disjunction:
				return true;

			// CF
			default:
				throw new RuntimeException("Unsupported TypeCondition '" + tc + "' of type: " + tc.tcType().name());
		}
	}

}
