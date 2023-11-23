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


public class PrettyJavaScriptPrototypes implements JavaScriptPrototypes {
	
	private CallPrototype parseDecmial = new StandardCallPrototype("parseDecimal", 1);
	private CallPrototype dateFromLong = new StandardCallPrototype("dateFromLong", 1);
	private CallPrototype parseLongBox = new StandardCallPrototype("parseLongBox", 1);
	
	private CallPrototype list = new StandardCallPrototype("list", 1);
	private CallPrototype set = new StandardCallPrototype("set", 1);
	private CallPrototype map = new StandardCallPrototype("map", 1);
	
	private CallPrototype create = new StandardCallPrototype("create", 1);
	
	private CallPrototype boxFloat = new StandardCallPrototype("boxFloat", 1);
	private CallPrototype boxDouble = new StandardCallPrototype("boxDouble", 1);
	private CallPrototype boxBoolean = new StandardCallPrototype("boxBoolean", 1);
	private CallPrototype boxInteger = new StandardCallPrototype("boxInteger", 1);
	private CallPrototype boxLong = new StandardCallPrototype("boxLong", 1);
	
	private CallPrototype parseLong = new StandardCallPrototype("parseLong", 1);
	
	private CallPrototype resolveType = new StandardCallPrototype("resolveType", 2);
	private CallPrototype typeReflection = new StandardCallPrototype("typeReflection", 0);
	
	private CallPrototype resolveProperty = new StandardCallPrototype("resolveProperty", 2);
	
	private CallPrototype setValue = new StandardCallPrototype("setValue", 3);
	private CallPrototype setAbsence = new StandardCallPrototype("setAbsence", 3);
	
	private CallPrototype parseEnum = new StandardCallPrototype("parseEnum", 2);
	
	@Override
	public CallPrototype parseDecimal() { return parseDecmial; }
	@Override
	public CallPrototype dateFromLong() { return dateFromLong; }
	@Override
	public CallPrototype boxFloat() { return boxFloat; }
	@Override
	public CallPrototype boxDouble() { return boxDouble; }
	@Override
	public CallPrototype boxBoolean() { return boxBoolean; }
	@Override
	public CallPrototype boxInteger() { return boxInteger; }
	@Override
	public CallPrototype boxLong() { return boxLong; }
	@Override
	public CallPrototype parseLong() { return parseLong; }
	@Override
	public CallPrototype parseLongBox() { return parseLongBox; }
	@Override
	public CallPrototype resolveType() { return resolveType; }
	@Override
	public CallPrototype typeReflection() { return typeReflection; }
	@Override
	public CallPrototype resolveProperty() { return resolveProperty; }
	@Override
	public CallPrototype setValue() { return setValue; }
	@Override
	public CallPrototype setAbsence() { return setAbsence; }
	@Override
	public CallPrototype create() { return create; }
	@Override
	public CallPrototype parseEnum() { return parseEnum; }
	@Override
	public CallPrototype list() { return list; }
	@Override
	public CallPrototype set() { return set; }
	@Override
	public CallPrototype map() { return map; }
}
