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
package tribefire.extension.messaging.model.conditions.types;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.annotation.meta.Priority;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import tribefire.extension.messaging.model.conditions.Comparison;

@SelectiveInformation("IF Type ${operator} '${typeName}'")
public interface TypeComparison extends Comparison, TypeCondition {

	EntityType<TypeComparison> T = EntityTypes.T(TypeComparison.class);

	@Name("Operator")
	@Initializer("enum(tribefire.extension.messaging.model.conditions.types.TypeOperator,isAssignableTo)")
	TypeOperator getOperator();
	@Priority(1.0d)
	@Mandatory
	void setOperator(TypeOperator operator);

	@Name("Type Name")
	String getTypeName();
	@Priority(0.9d)
	@Mandatory
	void setTypeName(String typeName);

	@Override
	default boolean matches(GenericEntity entity) {

		EntityType<?> actualType = entity.entityType();
		String actualTypeName = actualType.getShortName();

		TypeOperator operator = getOperator();
		String compareTypeName = getTypeName();


		return switch (operator) {
			case equal -> Comparison.compareEquality(compareTypeName, actualTypeName);
			case notEqual -> !Comparison.compareEquality(compareTypeName, actualTypeName);
			case like -> Comparison.compareLike(compareTypeName, actualTypeName, false);
			case iLike -> Comparison.compareLike(compareTypeName, actualTypeName, true);
			case isAssignableTo -> compareAssignableTo(compareTypeName, actualType);
		};

	}

	static boolean compareAssignableTo(String compareTypeName, EntityType<?> actualType) {
		if (compareTypeName != null) {
			Iterable<EntityType<?>> transitiveSuperTypes = actualType.getTransitiveSuperTypes(true, true);

			for (EntityType<?> superType : transitiveSuperTypes) {
				String actualTypeName = superType.getShortName();
				if (Comparison.compareEquality(compareTypeName, actualTypeName)) {
					return true;
				}
			}

		}
		return false;
	}

}
