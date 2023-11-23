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
package com.braintribe.model.io.metamodel.render.context;

import java.util.List;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.utils.lcd.StringTools;

/**
 * 
 */
public class PropertyDescriptor implements Comparable<PropertyDescriptor> {
	public final String name;
	public final JavaType type;

	public final boolean isInherited;
	public final List<String> annotations;

	public PropertyDescriptor(GmProperty gmProperty, JavaType type, boolean isInherited, List<String> annotations) {
		this.name = gmProperty.getName();
		this.type = type;
		this.isInherited = isInherited;
		this.annotations = annotations;
	}

	public String getNameStartingWithUpperCase() {
		return StringTools.capitalize(name);
	}

	@Override
	public int compareTo(PropertyDescriptor other) {
		return name.compareTo(other.name);
	}

}
