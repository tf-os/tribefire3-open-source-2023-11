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
package tribefire.extension.messaging.model.conditions.properties;

import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import tribefire.extension.messaging.model.conditions.Condition;
import tribefire.extension.messaging.model.conditions.Negation;


public interface PropertyNegation extends PropertyCondition, Negation {

	EntityType<PropertyNegation> T = EntityTypes.T(PropertyNegation.class);

	String operand = "operand";

	@Name("Operand")
	@Mandatory
	PropertyCondition getOperand();
	void setOperand(PropertyCondition operand);

	@Override
	default <C extends Condition> C operand() {
		return (C) getOperand();
	}

}
