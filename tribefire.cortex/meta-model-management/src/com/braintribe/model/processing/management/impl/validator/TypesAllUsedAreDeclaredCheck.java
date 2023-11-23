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
import java.util.Set;

import com.braintribe.model.management.MetaModelValidationViolation;
import com.braintribe.model.management.MetaModelValidationViolationType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.management.impl.util.MetaModelDependencyRegistry;
import com.braintribe.model.processing.management.impl.util.MetaModelDependencyRegistry.DependencyLink;

public class TypesAllUsedAreDeclaredCheck implements ValidatorCheck {
	
	@Override
	public boolean check(GmMetaModel metaModel, List<MetaModelValidationViolation> validationErrors) {
		
		MetaModelDependencyRegistry dependencyRegistry = new MetaModelDependencyRegistry(metaModel);
		for (GmType dependency : dependencyRegistry.getDependencies()) {
//			if (metaModel.getBaseType() == dependency) {
//				continue;
//			}
//			
//			if ((metaModel.getSimpleTypes() != null) && (metaModel.getSimpleTypes().contains(dependency))) {
//				continue;
//			}
//			
//			if ((metaModel.getEntityTypes() != null) && (metaModel.getEntityTypes().contains(dependency))) {
//				continue;
//			}
//			
//			if ((metaModel.getEnumTypes() != null) && (metaModel.getEnumTypes().contains(dependency))) {
//				continue;
//			}
		
			Set<DependencyLink> dependencyLinks = dependencyRegistry.getDependencyLinks(dependency);
			if (dependency == null) {
				ViolationBuilder.to(validationErrors).withTypeAndDependers(dependency, dependencyLinks)
					.add(MetaModelValidationViolationType.TYPE_NULL_HAS_REFERENCES, 
						"<null> type encountered with following dependents: " +
						MetaModelDependencyRegistry.describe(dependencyLinks));
			} else {
				ViolationBuilder.to(validationErrors).withTypeAndDependers(dependency, dependencyLinks)
					.add(MetaModelValidationViolationType.TYPE_NOT_DECLARED_HAS_REFERENCES, 
						"Type '" + dependency.getTypeSignature() + 
						"' not listed in the metaModel.simpleTypes, .entityTypes or .enumTypes, but has following dependents: " + 
						MetaModelDependencyRegistry.describe(dependencyLinks));
			}
		}
		
		return true;
	}

}
