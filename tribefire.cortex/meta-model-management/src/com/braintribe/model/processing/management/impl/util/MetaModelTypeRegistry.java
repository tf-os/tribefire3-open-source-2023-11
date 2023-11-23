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
package com.braintribe.model.processing.management.impl.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;

public class MetaModelTypeRegistry {
	
	private static final Logger LOG = Logger.getLogger(MetaModelTypeRegistry.class);
	
	private final Map<String, Set<GmType>> types = new HashMap<String, Set<GmType>>();

	public MetaModelTypeRegistry(GmMetaModel target) {
		enumerate(target);
	}
	
	private void enumerate(GmMetaModel target) {
		types.clear();
		
//		Set<GmEntityType> entityTypes = target.getEntityTypes();
//		if (entityTypes != null) {
//			for (GmEntityType entityType : entityTypes) {
//				mapType(entityType, types);
//			}
//		}
//		
//		Set<GmEnumType> enumTypes = target.getEnumTypes();
//		if (enumTypes != null) {
//			for (GmEnumType enumType : enumTypes) {
//				mapType(enumType, types);
//			}
//		}
	}

	private void mapType(GmType type, Map<String, Set<GmType>> map) {
		String artifactString = declaringModelToString(type);
		Set<GmType> types = map.get(artifactString);
		if (types == null) {
			types = new HashSet<GmType>();
			map.put(artifactString, types);
		}
		
		boolean wasNotThere = types.add(type);
		if (!wasNotThere) {
			LOG.trace("'" + type.toString() + "' was already present in the types set of artifact '" + artifactString + "'");
		}
	}


	private String declaringModelToString(GmType type) {
		return type.getDeclaringModel().getName() + "#" + type.getDeclaringModel().getVersion();
	}

	public void remove(GmType sourceType) {
		String k = declaringModelToString(sourceType);
		Set<GmType> set = types.get(k);
		if (set == null) {
			LOG.trace("No types found in artifact '" + k + "'");
		} else {
			if (set.remove(sourceType)) {
				LOG.trace("Type '" + sourceType.getTypeSignature() + "' in artifact '" + k + "' removed");
			} else {
				LOG.trace("No type '" + sourceType.getTypeSignature() + "' found in artifact '" + k + "' to be removed");
			}
		}
	}

	public Set<GmType> getRemaining() {
		Set<GmType> res = new HashSet<GmType>();
		for (Set<GmType> v : types.values()) {
			res.addAll(v);
		}
		return Collections.unmodifiableSet(res);
	}

}
