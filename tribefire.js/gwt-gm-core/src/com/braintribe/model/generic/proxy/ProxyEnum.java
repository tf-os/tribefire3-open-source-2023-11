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
package com.braintribe.model.generic.proxy;

import com.braintribe.model.generic.annotation.GmSystemInterface;
import com.braintribe.model.generic.reflection.GenericModelType;

@GmSystemInterface
public class ProxyEnum implements ProxyValue {

	private final String constantName;
	private Enum<?> actualEnum;
	private final ProxyEnumType type;

	public ProxyEnum(ProxyEnumType type, String constantName) {
		super();
		this.type = type;
		this.constantName = constantName;
	}

	public String getConstantName() {
		return constantName;
	}

	public void linkActualEnum(Enum<?> actualEnum) {
		this.actualEnum = actualEnum;
	}

	@Override
	public Enum<?> actualValue() {
		return actualEnum;
	}

	@Override
	public GenericModelType type() {
		return type;
	}
}
