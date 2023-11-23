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
package com.braintribe.model.processing.management.impl.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.MetaModelValidationViolationType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;

public class TypesLoopsInHierarchyCheck implements ValidatorCheck {
	
	@Override
	public boolean check(GmMetaModel metaModel, List<MetaModelValidationViolation> validationErrors) {	
		Set<List<String>> loops = new HashSet<List<String>>();
//		if (metaModel.getEntityTypes() != null) {
//			for (GmEntityType t : metaModel.getEntityTypes()) {
//				Stack<GmEntityType> stack = new Stack<GmEntityType>();
//				
//				if (t != null) {
//					stack.push(t);
//					checkLoops(stack, loops);
//					stack.pop();
//				}
//			}
//		}
		
		for (List<String> loop : loops) {
			ViolationBuilder.to(validationErrors).withEntityTypeList(loop)
				.add(MetaModelValidationViolationType.TYPE_HIERARCHY_LOOP, describeLoop(loop));
		}
		
		return true;
	}

	private String describeLoop(List<String> loop) {
		StringBuilder sb = new StringBuilder();
		if (loop != null) {
			for (int i = 0; i <= loop.size(); i++) { //size + 1 to show that it loops 
				sb.append(loop.get(i % loop.size()));
				if (i != loop.size()) {
					sb.append("->");
				}
			}
		}
		return null;
	}

	private void checkLoops(Stack<GmEntityType> stack, Set<List<String>> loops) {
		int loopIndex = -1;
		GmEntityType entityType = stack.peek();
		if (stack.size() > 1) {
			for (int i = stack.size() - 2; i >= 0; i--) {
				if ((stack.get(i) != null && entityType != null) && 
						stack.get(i).getTypeSignature().equals(entityType.getTypeSignature())) {
					loopIndex = i;
					break;
				}
			}
		}
		
		if (loopIndex > -1) {
			loops.add(normalizeLoop(stack, loopIndex));
		} else {
			if ((entityType != null) && (entityType.getSuperTypes() != null)) {
				for (GmEntityType superType : entityType.getSuperTypes()) {

					if (superType != null) {
						stack.push(superType);
						checkLoops(stack, loops);
						stack.pop();
					}
				}
			}
		}
	}

	private List<String> normalizeLoop(Stack<GmEntityType> stack, int loopIndex) {
		List<String> loop = new ArrayList<String>(); 
		for (int i = loopIndex; i < stack.size() - 1; i++) {
			loop.add(stack.get(i) == null ? null : stack.get(i).getTypeSignature());
		}
		
		int indexOfMin = loop.indexOf(Collections.min(loop));
		
		List<String> res = new ArrayList<String>();
		for (int i = indexOfMin; i < indexOfMin + loop.size(); i++) {
			res.add(loop.get(i % loop.size()));
		}
		return res;
	}

}
