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
package com.braintribe.gwt.genericmodel.client.itw;

import com.braintribe.model.generic.reflection.GmtsEnhancedEntityStub;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;

/**
 * Documented to force an import, thus using short name in JS code 
 * 
 * @see GmtsEnhancedEntityStub
 * @see PropertyAccessInterceptor
 * 
 */
public class GenericAccessorMethods {
	
	public native static JavaScriptObject buildGenericGetter(Property property)/*-{
		return function(){
			return this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::getProperty(*)(property, this, false);
		}
	}-*/;
	
	public native static JavaScriptObject buildGenericSetter(Property property)/*-{
		return function(v){
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;
	
	public native static JavaScriptObject buildUnboxingGetter(Property property)/*-{
		return function(){
		 	var boxer = this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::getProperty(*)(property, this, false);
			if(boxer != null)
				return boxer.valueOf();
			else
				return null;
		}
	}-*/;
	
	public native static JavaScriptObject buildIntBoxingSetter(Property property)/*-{
		return function(v){
			v = @java.lang.Integer::valueOf(I)(v);
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;
	
	public native static JavaScriptObject buildNullableIntBoxingSetter(Property property)/*-{
		return function(v){
			v = v == null ? null : @java.lang.Integer::valueOf(I)(v);
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;

	public native static JavaScriptObject buildFloatBoxingSetter(Property property)/*-{
		return function(v){
			v = @java.lang.Float::valueOf(F)(v);
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;

	public native static JavaScriptObject buildNullableFloatBoxingSetter(Property property)/*-{
		return function(v){
			v = v == null ? null : @java.lang.Float::valueOf(F)(v);
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;
	
	@SuppressWarnings("unusable-by-js")
	@UnsafeNativeLong
	public native static JavaScriptObject buildLongUnboxingGetter(Property property)/*-{
		return function(){
		 	var boxer = this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::getProperty(*)(property, this, false);
			if(boxer != null)
				return boxer.@Long::longValue()();
			else
				return null;
		}
	}-*/;
	
	@SuppressWarnings("unusable-by-js")
	@UnsafeNativeLong
	public native static JavaScriptObject buildLongBoxingSetter(Property property)/*-{
		return function(v){
			v = @java.lang.Long::valueOf(J)(v);
			this.@GmtsEnhancedEntityStub::pai.@PropertyAccessInterceptor::setProperty(*)(property, this, v, false);
		}
	}-*/;
}
