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
package com.braintribe.model.processing.itw.asm;

import static com.braintribe.utils.lcd.CollectionTools2.asTreeMap;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author peter.gazdik
 */
public class AsmAnnotationInstance {

	private final AsmClass annotationClass;
	private Map<String, Object> annotationValues;

	public AsmAnnotationInstance(AsmClass annotationClass, Object... values) {
		this(annotationClass, asTreeMap(values));
	}

	public AsmAnnotationInstance(AsmClass annotationClass, Map<String, Object> annotationValues) {
		this.annotationClass = annotationClass;
		this.annotationValues = annotationValues;
	}

	public AsmClass getAnnotationClass() {
		return annotationClass;
	}

	public void addAnnotationValue(String name, Object value) {
		if (annotationValues == null) {
			annotationValues = new TreeMap<>();
		}

		annotationValues.put(name, value);
	}

	public Map<String, Object> getAnnotationValues() {
		return annotationValues != null ? annotationValues : Collections.<String, Object> emptyMap();
	}

}
