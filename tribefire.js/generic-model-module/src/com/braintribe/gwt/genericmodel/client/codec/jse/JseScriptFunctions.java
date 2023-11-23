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
package com.braintribe.gwt.genericmodel.client.codec.jse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.gwt.browserfeatures.client.JsArray;
import com.braintribe.gwt.browserfeatures.client.JsArrayList;
import com.braintribe.gwt.browserfeatures.client.JsStringMap;
import com.braintribe.gwt.genericmodel.client.codec.api.GmDecodingContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.model.generic.proxy.ProxyValue;
import com.braintribe.model.generic.reflection.AbstractProperty;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.ScalarType;
import com.braintribe.model.generic.reflection.VdHolder;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GWT;

/**
 * @author dirk.scheffler
 *
 */
public class JseScriptFunctions extends JavaScriptObject {
	public static final AbsenceInformation standardAbsenceInformation = GMF.absenceInformation();
	
	protected JseScriptFunctions() {
		
	}
	
	public static <T> List<T> buildList(JsList<T> literal) {
		int size = literal.size();
		List<T> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(literal.get(i));
		}
		return list;
	}
	
	public static List<Object> buildListProxyAware(JsList<?> literal) {
		int size = literal.size();
		List<Object> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			Object e = literal.get(i);
			if (e instanceof ProxyValue) {
				list.add(null);
				proxyContext.deferListInsert(list, (ProxyValue)e, i);
			}
			else {
				list.add(e);
			}
		}
		return list;
	}
	
	public static List<Object> buildListProxyAndScriptAware(JsArray<Object> literal) {
		int size = literal.length();
		List<Object> list = new JsArrayList<>(literal);
		for (int i = 0; i < size; i++) {
			Object e = literal.get(i);
			if (e instanceof ProxyValue) {
				list.set(i, null);
				proxyContext.deferListInsert(list, (ProxyValue)e, i);
			}
		}
		return list;
	}
	
	public static <T> Set<T> buildSet(JsList<T> literal) {
		Set<T> set = new HashSet<>(literal.size());
		int size = literal.size();
		for (int i = 0; i < size; i++) {
			set.add(literal.get(i));
		}
		return set;
	}
	
	public static Set<Object> buildSetProxyAware(JsList<?> literal) {
		Set<Object> set = new HashSet<>(literal.size());
		int size = literal.size();
		for (int i = 0; i < size; i++) {
			Object e = literal.get(i);
			if (e instanceof ProxyValue) {
				proxyContext.deferCollectionAdd(set, (ProxyValue)e);
			}
			else {
				set.add(e);
			}
		}
		return set;
	}
	
	public static <K, V> Map<K, V> buildMap(JsList<?> literal) {
		Map<Object, Object> map = new HashMap<>();
		int size = literal.size();
		int i = 0; 
		while (i < size) {
			map.put(literal.get(i++), literal.get(i++));
		}
		Map<K, V> castedMap = (Map<K, V>)map;
		return castedMap;
	}
	
	public static Map<Object, Object> buildMapProxyAware(JsList<?> literal) {
		Map<Object, Object> map = new HashMap<>();
		int size = literal.size();
		int i = 0; 
		while (i < size) {
			Object key = literal.get(i++);
			Object value = literal.get(i++);
			
			if (key instanceof ProxyValue) {
				if (value instanceof ProxyValue) {
					proxyContext.deferMapPut(map, (ProxyValue)key, (ProxyValue)value);
				}
				proxyContext.deferMapPut(map, (ProxyValue)key, value);
			}
			else if (value instanceof ProxyValue) {
				proxyContext.deferMapPut(map, key, (ProxyValue)value);
			}
			else {
				map.put(key, value);
			}
		}
		return map;
	}


	public static EntityType<?> resolveEntityTypeProxyAware(String typeSignature) {
		EntityType<?> entityType = GMF.getTypeReflection().findType(typeSignature);
		return entityType != null? 
				entityType:
				proxyContext.getProxyEntityType(typeSignature);
	}
	
	public static ScalarType resolveEnumTypeProxyAware(String typeSignature) {
		ScalarType scalarType = GMF.getTypeReflection().findType(typeSignature);
		return scalarType != null?
				scalarType:
				proxyContext.getProxyEnumType(typeSignature);
	}
	
	public static GenericModelType resolveType(String typeSignature) {
		return GMF.getTypeReflection().getType(typeSignature);
	}
	
	public static native GenericModelType resolveTypeFromContext(String typeSignature) /*-{
		return this.z.@GmDecodingContext::resolveType(Ljava/lang/String;)(typeSignature);
	}-*/;
	
	public static Property resolveProperty(EntityType<?> entityType, String propertyName) {
		Property property = entityType.findProperty(propertyName);
		
		// if the property is not being found we padd back a dummy property which will silently swallow property setting happening in the JS eval codec
		if (property == null) {
			property = substituteProperty;
		}
		
		return property;
	}

	public static Property resolvePropertyTypeLenient(EntityType<?> entityType, String propertyName) {
		if (entityType == null) 
			return substituteProperty;
		
		Property property = entityType.findProperty(propertyName);
		
		// if the property is not being found we padd back a dummy property which will silently swallow property setting happening in the JS eval codec
		if (property == null) {
			return substituteProperty;
		}
		else {
			return property;
		}
	}
	
	public static Enum<?> resolveEnum(EnumType enumType, String name) {
		return enumType.getEnumValue(name);
	}
	
	public static Object resolveEnumProxyAware(ScalarType scalarType, String name) {
		return scalarType.instanceFromString(name);
	}
	
	public static Enum<?> resolveEnumTypeLenient(EnumType enumType, String name) {
		if (enumType == null)
			return null;
		
		return enumType.getEnumValue(name);
	}
	
	public static GenericEntity createLenient(GmDecodingContext context, EntityType<?> et) {
		if (et == null)
			return null;
		else
			return context == null? et.createRaw(): context.create(et);
	}
	
	public static JseScriptFunctions create() {
		return create(GWT.isScript(), false, null);
	}
	
	public static Date createDate(String millies) {
		return new Date(new Long(millies));
	}
	
	private static native <V> V buildStringMap(JavaScriptObject backend) /*-{
		var map = @java.util.HashMap::new()();
		var keys = Object.keys(backend);
		
		for (var i = 0; i < keys.length; i++) {
			var key = keys[i];
			map.@java.util.Map::put(Ljava/lang/Object;Ljava/lang/Object;)(key, backend[key]);
		}
		
		return map;
	}-*/;
	
	private static native Map<String, Object> buildStringMapProxyAware(JavaScriptObject backend) /*-{
		var map = @java.util.HashMap::new()();
		var keys = Object.keys(backend);
		
		for (var i = 0; i < keys.length; i++) {
			var key = keys[i];
			@JseScriptFunctions::putValueProxyAware(Ljava/util/Map;Ljava/lang/String;Ljava/lang/Object;)(map,key,backend[key]);
		}
		
		return map;
	}-*/;
	
	private static void putValueProxyAware(Map<Object, Object> map, String key, Object value) {
		if (value instanceof ProxyValue) {
			proxyContext.deferMapPut(map, key, (ProxyValue)value);
		}
		else {
			map.put(key, value);
		}
	}
	
	public static Map<Object, Object> buildStringMapProxyAndScriptAware(JsList<?> literal) {
		Map<Object, Object> map = (Map<Object,Object>)(Map<?,?>)new JsStringMap<>(literal);
		int size = literal.size();
		int i = 0; 
		while (i < size) {
			Object key = literal.get(i++);
			Object value = literal.get(i++);
			
			if (value instanceof ProxyValue) {
				proxyContext.deferMapPut(map, key, (ProxyValue)value);
				map.remove(key);
			}
		}
		return map;
	}
	
	public static void setProperty(GenericEntity entity, Property property, Object value) {
//		if(entity != null)
			property.setDirectUnsafe(entity, value);
	}
	
	public static void setPropertyProxyAware(GenericEntity entity, Property property, Object value) {
		if (value instanceof ProxyValue) {
			proxyContext.deferPropertyAssignment(entity, property, (ProxyValue)value);
		}
		else {
			property.setDirectUnsafe(entity, value);
		}
	}
	
	public static ProxyContext proxyContext;
	
	public static void setPropertyAbsent(GenericEntity entity, Property property) {
		property.setDirectUnsafe(entity, VdHolder.standardAiHolder);
	}
	
	public static void setPropertyAbsent(GenericEntity entity, Property property, AbsenceInformation absenceInformation) {
		property.setAbsenceInformation(entity, absenceInformation);
	}
	
	/**
	 * 	The following script functions are registered (here in alphabetical order):
	 *  w.a = set default absence
	 *  w.A = set dynamic absence
	 *  w.C = create entity
	 *  w.d = double to Double
	 *  w.D = string to BigDecimal
	 *  w.e = string to EnumType (or ScalarType)
	 *  w.E = resolveEnum from EnumType and name
	 *  w.f = float to Float
	 *  w.i = int to integer
	 *  w.l = long to Long (script) / string to Long 
	 *  w.L = JsArray to List
	 *  w.M = JsArray to Map
	 *  w.m = JavaScriptObject to Map<String, Object>
	 *  w.n = false;
	 *  w.P = resolveProperty from EntityType and name
	 *  w.S = JsArray to Set
	 *  w.s = setProperty
	 *  w.t = long to Date (script) / string to Date
	 *  w.T = string to EntityType
	 *  w.y = true;
  	 *  w.z = context;
	 */
	@com.google.gwt.core.client.UnsafeNativeLong
	private static native JseScriptFunctions create(boolean isScript, boolean proxyAware, GmDecodingContext context) /*-{
		var w = {};
		w.z = context;
		
		w.y = @Boolean::TRUE;
		w.n = @Boolean::FALSE;
		
		w.i = @Integer::valueOf(I);
		w.l = isScript? 
			@Long::valueOf(J): 
			@Long::new(Ljava/lang/String;);
		w.f = @Float::valueOf(F);
		w.d = @Double::valueOf(D);
		w.D = @java.math.BigDecimal::new(Ljava/lang/String;);
		w.t = isScript? 
			@java.util.Date::new(J): 
			@JseScriptFunctions::createDate(Ljava/lang/String;);
	
		if (proxyAware) {
			w.L = isScript?
				@JseScriptFunctions::buildListProxyAndScriptAware(Lcom/braintribe/gwt/browserfeatures/client/JsArray;):
				@JseScriptFunctions::buildListProxyAware(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.S = @JseScriptFunctions::buildSetProxyAware(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.M = @JseScriptFunctions::buildMapProxyAware(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.m = isScript? 
				@JseScriptFunctions::buildStringMapProxyAndScriptAware(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;):
				@JseScriptFunctions::buildStringMapProxyAware(Lcom/google/gwt/core/client/JavaScriptObject;);
		}
		else {
			w.L = isScript?
				@com.braintribe.gwt.browserfeatures.client.JsArrayList::new(Lcom/braintribe/gwt/browserfeatures/client/JsArray;):
				@JseScriptFunctions::buildList(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.S = @JseScriptFunctions::buildSet(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.M = @JseScriptFunctions::buildMap(Lcom/braintribe/gwt/genericmodel/client/codec/jse/JsList;);
			w.m = isScript? 
				@com.braintribe.gwt.browserfeatures.client.JsStringMap::new(Lcom/google/gwt/core/client/JavaScriptObject;):
				@JseScriptFunctions::buildStringMap(Lcom/google/gwt/core/client/JavaScriptObject;);
		}
		
		if (proxyAware) {
			w.T = @JseScriptFunctions::resolveEntityTypeProxyAware(Ljava/lang/String;);
			w.e = @JseScriptFunctions::resolveEnumTypeProxyAware(Ljava/lang/String;);
		}
		else {
			if (isScript) {
				w.e = w.T = context == null?
					@JseScriptFunctions::resolveType(Ljava/lang/String;):
					@JseScriptFunctions::resolveTypeFromContext(Ljava/lang/String;);
			}
			else {
				w.e = w.T = context == null?
					@JseScriptFunctions::resolveType(Ljava/lang/String;):
					function(t) {
						return context.@GmDecodingContext::resolveType(Ljava/lang/String;)(t);
					};
			}
		}
		
		w.P = context == null || proxyAware?
			@JseScriptFunctions::resolveProperty(Lcom/braintribe/model/generic/reflection/EntityType;Ljava/lang/String;):
			@JseScriptFunctions::resolvePropertyTypeLenient(Lcom/braintribe/model/generic/reflection/EntityType;Ljava/lang/String;);
			
		if (proxyAware) {
			w.E = @JseScriptFunctions::resolveEnumProxyAware(Lcom/braintribe/model/generic/reflection/ScalarType;Ljava/lang/String;);
		}
		else {
			w.E = context == null?
				@JseScriptFunctions::resolveEnum(Lcom/braintribe/model/generic/reflection/EnumType;Ljava/lang/String;):
				@JseScriptFunctions::resolveEnumTypeLenient(Lcom/braintribe/model/generic/reflection/EnumType;Ljava/lang/String;);
		}
			
		w.C = context == null || proxyAware?
			function(t) { 
				return @JseScriptFunctions::createRaw(Lcom/braintribe/gwt/genericmodel/client/codec/api/GmDecodingContext;Lcom/braintribe/model/generic/reflection/EntityType;)(context,t);
			}:
			function(t) { 
				return @JseScriptFunctions::createLenient(Lcom/braintribe/gwt/genericmodel/client/codec/api/GmDecodingContext;Lcom/braintribe/model/generic/reflection/EntityType;)(context,t);
			};

		if (proxyAware) {
			w.s = @JseScriptFunctions::setPropertyProxyAware(Lcom/braintribe/model/generic/GenericEntity;Lcom/braintribe/model/generic/reflection/Property;Ljava/lang/Object;);
		}
		else {
			w.s = @JseScriptFunctions::setProperty(Lcom/braintribe/model/generic/GenericEntity;Lcom/braintribe/model/generic/reflection/Property;Ljava/lang/Object;);
		}
		w.a = @JseScriptFunctions::setPropertyAbsent(Lcom/braintribe/model/generic/GenericEntity;Lcom/braintribe/model/generic/reflection/Property;);
		w.A = @JseScriptFunctions::setPropertyAbsent(Lcom/braintribe/model/generic/GenericEntity;Lcom/braintribe/model/generic/reflection/Property;Lcom/braintribe/model/generic/pr/AbsenceInformation;);

		
		return w;
	}-*/;
	
	public static GenericEntity createRaw(GmDecodingContext context, EntityType<?> et) {
		if (context != null)
			return context.create(et);
		else
			return et.createRaw();
	}
	
	public static JseScriptFunctions create(GmDecodingContext context) {
		return create(GWT.isScript(), context.getProxyContext() != null, context);
	}
	
	private static native void log(String msg) /*-{
		$wnd.console.log(msg);
	}-*/;

	
	private static final Property substituteProperty;
	
	static {
		substituteProperty = new AbstractProperty(null, false, false) {
			@Override
			public EntityType<?> getDeclaringType() {
				return null;
			}
			@SuppressWarnings("unusable-by-js")
			@Override
			public void setDirectUnsafe(GenericEntity entity, Object value) {
				// do nothing
			}
			@SuppressWarnings("unusable-by-js")
			@Override
			public <T> T getDirectUnsafe(GenericEntity entity) {
				return null;
			}
		};
	}
	

}
