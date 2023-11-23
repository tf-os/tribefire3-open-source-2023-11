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
package com.braintribe.gwt.genericmodel.client.itw.gb;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.generic.reflection.GmtsEnhancedEntityStub;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * {@link GmtsEnhancedEntityStub}
 * {@link LongFacade}
 * {@link DecimalFacade}
 * {@link PropertyAccessInterceptor}
 * @author dirk.scheffler
 *
 */
public abstract class JsValueAdaption {

	public static Pair<JavaScriptObject,JavaScriptObject> getAdapterFunctions(Property property) {
		switch (property.getType().getTypeCode()) {
		// base type
		case objectType:
			break;

		// simple types
		case booleanType:
			return property.isNullable()?
					new Pair<>(createConversionGetter(property, getBooleanToPrimitive()), createConversionSetter(property, getPrimitiveToBooolean())):
					null;
		case floatType:
			return property.isNullable()?
					new Pair<>(createConversionGetter(property, getFloatToPrimitive()), createConversionSetter(property, getPrimitiveToFloat())):
					null;
		case doubleType:
			return property.isNullable()?
					new Pair<>(createConversionGetter(property, getDoubleToPrimitive()), createConversionSetter(property, getPrimitiveToDouble())):
					null;
		case integerType:
			return property.isNullable()?
					new Pair<>(createConversionGetter(property, getIntegerToPrimitive()), createConversionSetter(property, getPrimitiveToInteger())):
					null;
			
		case dateType:
			return new Pair<>(createConversionGetter(property, getToJsDate()), createConversionSetter(property, getToJavaDate()));
		case decimalType:
			return new Pair<>(createConversionGetter(property, getDecimalFacadeConstructor()), createUnboxingSetter(property));
		case longType:
			return new Pair<>(createConversionGetter(property, getLongFacadeConstructor()), createUnboxingSetter(property));
		
		// collection types
		case listType:
			return new Pair<>(createConversionGetter(property, getListFacadeConstructor()), createUnboxingSetter(property));
		case mapType:
			return new Pair<>(createConversionGetter(property, getMapFacadeConstructor()), createUnboxingSetter(property));
		case setType:
			return new Pair<>(createConversionGetter(property, getSetFacadeConstructor()), createUnboxingSetter(property));
			
		default:
			return null;
		
		}
		// TODO check switch block and return statement
		return null;
	}
	
	private static native JavaScriptObject getSetFacadeConstructor() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getMapFacadeConstructor() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getListFacadeConstructor() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getIntegerToPrimitive() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getDoubleToPrimitive() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getPrimitiveToDouble() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getPrimitiveToInteger() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getPrimitiveToFloat() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getFloatToPrimitive() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getBooleanToPrimitive() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getPrimitiveToBooolean() /*-{
		return null;
	}-*/;

	private static native JavaScriptObject getToJsDate() /*-{
		return function(date) {
			return new Date(date.@java.util.Date::getTime()());
		};
	}-*/;
	
	private static native JavaScriptObject getToJavaDate() /*-{
		return function(date) {
			return @Date::new(J)(date.getTime());
		};
	}-*/;
	
	
	private static native JavaScriptObject getLongFacadeConstructor() /*-{
		return @LongFacade::new(Ljava/lang/Long;);
	}-*/;
	
	private static native JavaScriptObject getDecimalFacadeConstructor() /*-{
		return @DecimalFacade::new(Ljava/lang/Long;);
	}-*/;

	private static native JavaScriptObject createConversionGetter(Property property, JavaScriptObject conversionFunction) /*-{
	    return function() {
			var v = this.@GmtsEnhancedEntityStub::pai.
				@PropertyAccessInterceptor::getProperty(Lcom/braintribe/model/generic/reflection/Property;Lcom/braintribe/model/generic/GenericEntity;)
					(property, this);
			return v == null? null: conversionFunction(v);
		};	
	}-*/;
	
	private static native JavaScriptObject createConversionSetter(Property property, JavaScriptObject conversionFunction) /*-{
	    return function(value) {
	    	value = conversionFunction(value);
			return this.@GmtsEnhancedEntityStub::pai.
				@PropertyAccessInterceptor::setProperty(Lcom/braintribe/model/generic/reflection/Property;Lcom/braintribe/model/generic/GenericEntity;Ljava/lang/Object;)
					(property, this, value);
		};	
	}-*/;
	
	/**
	 * {@link JsFacade}
	 */
	private static native JavaScriptObject createUnboxingSetter(Property property) /*-{
	    return function(value) {
	    	value = value.@JsBox::unbox()();
			return this.@GmtsEnhancedEntityStub::pai.
				@PropertyAccessInterceptor::setProperty(Lcom/braintribe/model/generic/reflection/Property;Lcom/braintribe/model/generic/GenericEntity;Ljava/lang/Object;)
					(property, this, value);
		};	
	}-*/;
	
}
