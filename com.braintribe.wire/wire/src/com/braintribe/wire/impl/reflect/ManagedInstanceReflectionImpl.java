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
package com.braintribe.wire.impl.reflect;

import java.lang.reflect.Type;

import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.reflect.ManagedInstanceReflection;
import com.braintribe.wire.api.space.WireSpace;

public class ManagedInstanceReflectionImpl implements ManagedInstanceReflection {

	@Override
	public Scope scope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WireSpace space() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPublic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isParameterized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type[] parameterTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type returnType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T instance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T instance(Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

}
