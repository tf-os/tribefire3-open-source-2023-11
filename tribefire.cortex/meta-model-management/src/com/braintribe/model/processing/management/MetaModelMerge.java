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
package com.braintribe.model.processing.management;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.management.impl.util.MetaModelDependencyRegistry;
import com.braintribe.model.processing.management.impl.util.MetaModelDependencyRegistry.DependencyLink;
import com.braintribe.model.processing.management.impl.util.MetaModelTypeRegistry;

public class MetaModelMerge {
	
	public static enum MergeMode {Import, Merge};
	
	private static final Logger LOG = Logger.getLogger(MetaModelMerge.class);
	
	/**
	 * @param includedTypes
	 *            Anybody knows?
	 */
	public void merge(Set<GmMetaModel> sources, GmMetaModel target, Predicate<GmType> includedTypes, MergeMode mode) {
		MetaModelTypeRegistry registry = new MetaModelTypeRegistry(target);
		
		for (GmMetaModel source : sources) {
			mergeInternal(source, target, registry);
		}

		if (mode == MergeMode.Merge) {
			cleanupUnusedTypes(target, registry);
		}
	}

	private void cleanupUnusedTypes(GmMetaModel target, MetaModelTypeRegistry registry) {
		MetaModelDependencyRegistry dependencyRegistry = new MetaModelDependencyRegistry(target);
		Set<GmType> unusedTypes = registry.getRemaining();
		for (GmType unused : unusedTypes) {
			LOG.debug(unused.getTypeSignature() + " not refered in sources, cleanup candidate");
			
			//check if not same artifact binding as target model
			if (unused.getDeclaringModel() == target) {
				LOG.debug(unused.getTypeSignature() + " not cleaning up as it's bindingArtifact matches the target metaModel");
				continue;
			}

			Set<DependencyLink> dependencyLinks = dependencyRegistry.getDependencyLinks(unused);
			if ((dependencyLinks != null) && (dependencyLinks.size() > 0)) {
				LOG.debug(unused.getTypeSignature() + " not cleaning up as it has following dependents: " +
						Arrays.toString(MetaModelDependencyRegistry.extractDescriptions(dependencyLinks).toArray()));
				continue;
			}
			
//			if (target.getEntityTypes() != null) {
//				target.getEntityTypes().remove(unused);
//			}
//			
//			if (target.getEnumTypes() != null) {
//				target.getEnumTypes().remove(unused);
//			}
			
			LOG.debug(unused.getTypeSignature() + " cleaned up");
		}
	}

	private void mergeInternal(GmMetaModel source, GmMetaModel target, MetaModelTypeRegistry registry) {
//		if ((source.getBaseType() != null) && (target.getBaseType() == null)) {
//			target.setBaseType(source.getBaseType());
//		}
//		
//		if ((source.getSimpleTypes() != null) && (target.getSimpleTypes() == null)) {
//			target.setSimpleTypes(new HashSet<GmSimpleType>());
//		}
//		copyIfNotThere(source.getSimpleTypes(), target.getSimpleTypes(), registry);
//		
//		if ((source.getEntityTypes() != null) && (target.getEntityTypes() == null)) {
//			target.setEntityTypes(new HashSet<GmEntityType>());
//		}
//		copyIfNotThere(source.getEntityTypes(), target.getEntityTypes(), registry);
//		
//		if ((source.getEnumTypes() != null) && (target.getEnumTypes() == null)) {
//			target.setEnumTypes(new HashSet<GmEnumType>());
//		}
//		copyIfNotThere(source.getEnumTypes(), target.getEnumTypes(), registry);
	}

	private <T extends GmType> void copyIfNotThere(Set<T> sourceTypes, Set<T> targetTypes, MetaModelTypeRegistry registry) {
		if (sourceTypes != null) {
			for (T sourceType : sourceTypes) {
				registry.remove(sourceType);
				
				if (!targetTypes.contains(sourceType)) {
					targetTypes.add(sourceType);
				}
			}
		}
	}

}
