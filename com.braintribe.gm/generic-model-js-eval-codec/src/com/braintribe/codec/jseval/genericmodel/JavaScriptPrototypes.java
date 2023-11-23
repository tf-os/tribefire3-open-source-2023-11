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
package com.braintribe.codec.jseval.genericmodel;



public interface JavaScriptPrototypes {
	public CallPrototype parseDecimal();
	public CallPrototype dateFromLong();
	public CallPrototype boxFloat();
	public CallPrototype boxDouble();
	public CallPrototype boxBoolean();
	public CallPrototype boxInteger();
	public CallPrototype boxLong();
	public CallPrototype parseLong();
	public CallPrototype parseLongBox();
	public CallPrototype resolveType();
	public CallPrototype typeReflection();
	public CallPrototype resolveProperty();
	public CallPrototype setValue();
	public CallPrototype setAbsence();
	public CallPrototype create();
	public CallPrototype parseEnum();
	public CallPrototype list();
	public CallPrototype set();
	public CallPrototype map();
}
