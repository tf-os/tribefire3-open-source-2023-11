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
package tribefire.extension.elasticsearch.model.api.request.doc;

import java.util.HashMap;

import com.braintribe.model.generic.base.EnumBase;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EnumTypes;

/**
 * Defines the type of the <code>operator</code> applied in comparisons.
 */
public enum Operator implements EnumBase {
	equal,
	notEqual,
	greater,
	greaterOrEqual,
	less,
	lessOrEqual;

	public static final EnumType T = EnumTypes.T(Operator.class);

	@Override
	public EnumType type() {
		return T;
	}

	public static String[] getOperatorSigns() {
		return new String[] { "=", "!=", ">", ">=", "<", "<=", };
	}

	public static String[] getCollectionsOperatorSigns() {
		return new String[] { "=", "!=", "contains" };
	}

	private static HashMap<String, Operator> signToOperator = new HashMap<String, Operator>();

	static {
		signToOperator.put("=", Operator.equal);
		signToOperator.put("!=", Operator.notEqual);
		signToOperator.put(">", Operator.greater);
		signToOperator.put(">=", Operator.greaterOrEqual);
		signToOperator.put("<", Operator.less);
		signToOperator.put("<=", Operator.lessOrEqual);
	}

	public static String getSignToOperator(Operator op) {
		for (String operatorSign : signToOperator.keySet()) {
			if (signToOperator.get(operatorSign) == op)
				return operatorSign;
		}
		return "";
	}

	public static Operator getOperatorToSign(String sign) {
		return signToOperator.get(sign);
	}

}
