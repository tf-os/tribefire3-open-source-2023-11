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
package com.braintribe.model.processing.accessory.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.components.ModelExtension;

/**
 * @author peter.gazdik
 */
public class PmeKey {

	public static PmeKey create(String modelName, String perspective, String ownerType, List<? extends ModelExtension> extensions) {
		return new PmeKey(modelName, perspective, ownerType, extractSortedModelNames(extensions));
	}

	private static List<String> extractSortedModelNames(List<? extends ModelExtension> extensions) {
		return extensions.stream() //
				.map(ModelExtension::getModels) //
				.filter(m -> m != null) //
				.flatMap(Set::stream) //
				.map(GmMetaModel::getName) //
				.sorted() //
				.collect(Collectors.toList());
	}

	public final String modelName;
	public final String perspective;
	public final String ownerType;
	public final List<String> depModelNames;

	private PmeKey(String modelName, String perspective, String ownerType, List<String> depModelNames) {
		this.modelName = modelName;
		this.perspective = perspective;
		this.ownerType = ownerType;
		this.depModelNames = depModelNames;
	}

	@Override
	public int hashCode() {
		return modelName.hashCode() + 31 * (safeHash(perspective) + 31 * (safeHash(ownerType) + 31 * depModelNames.hashCode()));
	}

	private int safeHash(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		PmeKey other = (PmeKey) obj;

		return safeEquals(modelName, other.modelName) && //
				safeEquals(perspective, other.perspective) && //
				safeEquals(ownerType, other.ownerType) && //
				depModelNames.equals(other.depModelNames);
	}

	private boolean safeEquals(Object o1, Object o2) {
		return o1 == o2 || //
				(o1 != null && o1.equals(o2));
	}

}
