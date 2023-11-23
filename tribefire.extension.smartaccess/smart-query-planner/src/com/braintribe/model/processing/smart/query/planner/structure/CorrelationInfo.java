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
package com.braintribe.model.processing.smart.query.planner.structure;

import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;

/**
 * 
 */
public class CorrelationInfo {

	private final GmEntityType delegateType1;
	private final GmProperty correlationProperty1;

	private final GmEntityType delegateType2;
	private final GmProperty correlationProperty2;
	private final GmProperty smartProperty;

	public CorrelationInfo(GmProperty smartProperty, GmProperty p1, GmProperty p2) {
		this.smartProperty = smartProperty;
		this.delegateType1 = p1.getDeclaringType();
		this.correlationProperty1 = p1;
		this.delegateType2 = p2.getDeclaringType();
		this.correlationProperty2 = p2;
	}

	public GmProperty getSmartProperty() {
		return smartProperty;
	}

	public GmProperty getCorrelationProperty(GmEntityType delegateType) {
		if (delegateType1 == delegateType)
			return correlationProperty1;

		if (delegateType2 == delegateType)
			return correlationProperty2;

		return null;
	}
}
