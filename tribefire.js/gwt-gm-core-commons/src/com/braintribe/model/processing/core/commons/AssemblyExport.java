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
package com.braintribe.model.processing.core.commons;

import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.or;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.basic.TypeKind;

public class AssemblyExport {

	public static <T> T flatExport(T assembly) {
		StandardMatcher matcher = new StandardMatcher();
		TraversingCriterion tc = TC.create()
					.conjunction()
						.property()
						.typeCondition(or(
								isKind(TypeKind.collectionType),
								isKind(TypeKind.entityType)
							))
					.close()
					.done();
							
		matcher.setCriterion(tc);
		
		T exportedAssembly = (T)GMF.getTypeReflection().getBaseType().clone(assembly, matcher, StrategyOnCriterionMatch.partialize);
		
		return exportedAssembly;
	}

}
