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


public class CondensedJavaScriptPrototypes implements JavaScriptPrototypes {
	
	private CallPrototype parseDecimal = new StandardCallPrototype("a", 1);
	private CallPrototype dateFromLong = new StandardCallPrototype("b", 1);
	private CallPrototype parseLongBox = new StandardCallPrototype("c", 1);
	
	private CallPrototype list = new StandardCallPrototype("d", 1);
	private CallPrototype set = new StandardCallPrototype("e", 1);
	private CallPrototype map = new StandardCallPrototype("f", 1);
	
	private CallPrototype create = new StandardCallPrototype("g", 1);
	
	private CallPrototype boxFloat = new StandardCallPrototype("i", 1);
	private CallPrototype boxDouble = new StandardCallPrototype("j", 1);
	private CallPrototype boxBoolean = new StandardCallPrototype("k", 1);
	private CallPrototype boxInteger = new StandardCallPrototype("l", 1);
	private CallPrototype boxLong = new StandardCallPrototype("m", 1);
	
	private CallPrototype parseLong = new StandardCallPrototype("n", 1);
	
	private CallPrototype resolveType = new StandardCallPrototype("p", 2);
	private CallPrototype typeReflection = new StandardCallPrototype("q", 0);
	
	private CallPrototype resolveProperty = new StandardCallPrototype("r", 2);
	
	private CallPrototype setValue = new StandardCallPrototype("t", 3);
	private CallPrototype setAbsence = new StandardCallPrototype("u", 3);
	
	private CallPrototype parseEnum = new StandardCallPrototype("v", 2);
	
	@Override
	public CallPrototype parseDecimal() { return parseDecimal; }
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
