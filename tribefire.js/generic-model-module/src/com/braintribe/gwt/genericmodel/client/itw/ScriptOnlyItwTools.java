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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gwt.core.client.GwtScriptOnly;
import com.google.gwt.core.client.JavaScriptObject;

@GwtScriptOnly
public class ScriptOnlyItwTools {

	/**
	 * This function resembles the hidden method available only in the GWT emulation JRE and addresses it via JSNI
	 * @param <T> type of newly create class
	 * @param packageName the packageName which must end with a '.'
	 * @param compoundClassName the simple Class name (without package)
	 * @param superClass the super class of the class
	 */
	public static native <T> Class<T> createForClass(String packageName, String compoundClassName, JavaScriptObject typeId,
			Class<?> superClass)
	/*-{
		return @java.lang.Class::createForClass(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/Class;)(packageName, compoundClassName, typeId, superClass);
	}-*/;
	
	/**
	 * This function resembles the hidden method available only in the GWT emulation JRE and addresses it via JSNI 
	 * @param <T> type of newly create enum
	 * @param packageName the packageName which must end with a '.'
	 * @param compoundClassName the simple Class name (without package)
	 * @param superClass the super class of the class
	 */
	public static native <T extends Enum<T>> Class<T> createForEnum(String packageName, String compoundClassName, JavaScriptObject typeId,
			Class<? super T> superClass, JavaScriptObject enumConstantsFunc, JavaScriptObject enumValueOfFunc) /*-{
		return @java.lang.Class::createForEnum(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/Class;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)
			(packageName, compoundClassName, typeId, superClass, enumConstantsFunc, enumValueOfFunc);
	}-*/;

	
	public static native Enum<?> createEnumInstance(String name, int ordinal, String nameOfGetClass, String nameOfGetDeclaringClass,
			Class<? extends Enum<?>> enumClass)	/*-{
		var enumInstance = @java.lang.Enum::new(Ljava/lang/String;I)(name, ordinal);
		enumInstance[nameOfGetClass] = function() {
			return enumClass;
		}
		enumInstance[nameOfGetDeclaringClass] = function() {
			return enumClass;
		}
		enumInstance.@Object::___clazz = enumClass;
		return enumInstance;
	}-*/;
	
	
	/**
	 * This function resembles the hidden method available only in the GWT emulation JRE and addresses it via JSNI 
	 * @param <T> type of newly create interface
	 * @param packageName the packageName which must end with a '.'
	 * @param className the simple Class name of the interface (without package)
	 */
	public static native <T> Class<T> createForInterface(String packageName, String className) /*-{
		return @java.lang.Class::createForInterface(Ljava/lang/String;Ljava/lang/String;)(packageName, className);
	}-*/;

	public static native GenericJavaScriptObject portableObjectCreate(Object prototype) /*-{
		return @com.google.gwt.lang.Runtime::portableObjCreate(Lcom/google/gwt/core/client/JavaScriptObject;)(prototype);
	}-*/;
	
	public static native void setClass(Object object, Class<?> javaClass) /*-{
		object.@Object::___clazz = javaClass;
	}-*/;
	
	public static native void setCastableTypeMap(Object object, CastableTypeMap castableTypeMap) /*-{
		object.@java.lang.Object::castableTypeMap = castableTypeMap;
	}-*/;
	
	public static native CastableTypeMap getCastableTypeMap(Object object) /*-{
		return object.@java.lang.Object::castableTypeMap;
	}-*/;

	public static native void setPrototype(Object object, GenericJavaScriptObject prototype) /*-{
		object.prototype = prototype;
	}-*/;
	
	public static native GenericJavaScriptObject getPrototype(Object object) /*-{
		return object.prototype;
	}-*/;

	public static native Object eval(String source) /*-{
		return eval(source);
	}-*/;

	public static native JavaScriptObject createProvider(Object o)/*-{
		return function(){return o;};
	}-*/;
	
	public static native void setProperty(Object owner, String propertyName, Object value)/*-{
		owner[propertyName] = value;
	}-*/;

	
	public static native <T> T getObjectProperty(Object object, String propertyName) /*-{
		return object[propertyName];
	}-*/;

	public static native void forEachPropertyName(Object owner, Consumer<String> consumer)
	/*-{
		for (var propertyName in owner) {
			consumer.@Consumer::accept(Ljava/lang/Object;)(propertyName);
		}
	}-*/;

	public static native void forEachEntry(Object owner, BiConsumer<String, Object> consumer)
	/*-{
		for (var propertyName in owner) {
			consumer.@BiConsumer::accept(Ljava/lang/Object;Ljava/lang/Object;)(propertyName, owner[propertyName]);
		}
	}-*/;

}
