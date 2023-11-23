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

import java.util.List;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.meta.GmMetaModel;

public class TypesIsPlainConsistencyCheck implements ValidatorCheck {
	
	@SuppressWarnings("unused")
	@Override
	public boolean check(GmMetaModel metaModel, List<MetaModelValidationViolation> validationErrors) {
		
//		if (metaModel.getEntityTypes() != null) {
//			for (GmEntityType t : metaModel.getEntityTypes()) {
//				if (t != null) {
//					
//					//only true means plain, null or false means not plain
//					if (/*isPlain(t)*/false) {
//						//check if max one isPlain supertype
//						List<String> plainSupertypes = new ArrayList<String>();
//						if (t.getSuperTypes() != null) {
//							for (GmEntityType supertype : t.getSuperTypes()) {
//								if (/*supertype != null && isPlain(supertype)*/ false) {
//									plainSupertypes.add(supertype.getTypeSignature());
//								}
//							}
//						}
//						
//						if (plainSupertypes.size() > 1) {
//							ViolationBuilder.to(validationErrors).withEntityType(t)
//								.add(MetaModelValidationViolationType.TYPE_PLAIN_TYPE_WITH_MORE_THAN_ONE_PLAIN_SUPERTYPE, 
//									"entity type '" + t.getTypeSignature() + "' is plain, but has more than one plain supertype " + 
//									Arrays.toString(plainSupertypes.toArray()));
//						}
//					} else {
//						//check all supertypes are not isPlain
//						if (t.getSuperTypes() != null) {
//							for (GmEntityType supertype : t.getSuperTypes()) {
//								if (/*supertype != null && isPlain(supertype)*/ false) {
//									ViolationBuilder.to(validationErrors).withEntityType(t)
//										.add(MetaModelValidationViolationType.TYPE_NOT_PLAIN_TYPE_WITH_PLAIN_SUPERTYPE, 
//											"entity type '" + t.getTypeSignature() + "' is not plain, but has plain supertype '" + 
//											supertype.getTypeSignature() + "'");
//								}
//							}
//						}
//					}
//				}
//			}
//		}
		
		return true;
	}

}
