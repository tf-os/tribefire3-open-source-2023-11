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
package com.braintribe.codec.registry;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.string.StringCodec;

@SuppressWarnings("rawtypes")
public class CodecRegistry implements Cloneable {
	protected HashMap<Object, Codec> codecs = new HashMap<Object, Codec>();
	
	public CodecRegistry() {
	}
	
	public CodecRegistry(Map<String, Codec> codecs) {
	    if (codecs != null) {
	        for (Map.Entry<String, Codec> entry: codecs.entrySet()) {
	            registerCodec(entry.getKey(), entry.getValue());
	        }
	    }
	}
	
	public void setCodecs(HashMap<Object, Codec> codecs) {
        this.codecs = codecs;
    }
	
	@Override
	@SuppressWarnings("unchecked")
	public CodecRegistry clone() {
		try {
			CodecRegistry r = (CodecRegistry)super.clone();
			r.codecs = (HashMap<Object, Codec>)codecs.clone();
			return r;
		} catch (CloneNotSupportedException e) {
			throw new Error("unexpected exception", e);
		}
	}
	
	public void registerCodec(String name, Codec codec) {
		registerCodec(name, codec, true);
	}
	
	public void registerCodec(String name, Codec codec, boolean override) {
		if (!override && codecs.containsKey(name)) return;
		
		codecs.put(name, codec);
		//TODO: get type parameters from codec using reflection, and use them to register it
		//TODO: alternatively, look for annotations (name, type)
	}

	public void registerCodec(Class type, Codec codec) {
		registerCodec(type, codec, true);
	}
	
	public void registerCodec(Class type, Codec codec, boolean override) {
		if (!override && codecs.containsKey(type)) return;
		
		codecs.put(type, codec);
	}
	
	public void registerCodec(String name, Class type, Codec codec) {
		registerCodec(name, type, codec, true);
	}
	
	public void registerCodec(String name, Class type, Codec codec, boolean override) {
		registerCodec(name, codec, override);
		registerCodec(type, codec, override);
	}
	
	
	public Codec getCodec(String name) {
		// first look if the codec exists
		Codec c = codecs.get(name);
		
		if (c != null) return c;
		else return null;
	}

	public <T> Codec<T, String> getCodec(Class<T> type) {
		Codec<T, String> c = codecs.get(type);
		return c;
	}
	
	public Codec requireCodec(String name) {
		Codec c = getCodec(name);
		if (c==null) throw new IllegalArgumentException("don't know codec named \"" + name + "\"");
		return c;
	}
	
	public <T> Codec<T, String> requireCodec(Class<T> type) {
		Codec<T, String> c = getCodec(type);
		if (c==null) throw new IllegalArgumentException("don't know codec for type \"" + type + "\"");
		return c;
	}

	public boolean hasCodec(String name) {
		return codecs.containsKey(name);
	}
	
	public boolean hasCodec(Class type) {
		return codecs.containsKey(type);
	}
	
	@SuppressWarnings("unchecked")
	public Object optionalDecode(String data, String name) throws Exception {
		Codec c = getCodec(name);
		return c == null ? data : c.decode(data);
	}
	
	@SuppressWarnings("unchecked")
	public String optionalEncode(Object obj, String name) throws Exception {
		Codec<Object, String> c = getCodec(name);
		return c.encode(obj);
	}
	
	@SuppressWarnings("unchecked")
	public Object decode(String data, String name) throws Exception {
		Codec c = requireCodec(name);
		return c.decode(data);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T decode(String data, Class<T> type) throws Exception {
		Codec c = getCodec(type);
		
		if (c==null) {
			if (type == String.class) return (T)data; //NOTE: this cast is safe
			throw new IllegalArgumentException("don't know codec for type \"" + type + "\"");
		}
		
		return (T) c.decode(data);
	}
	
	@SuppressWarnings("unchecked")
	public String encode(Object obj, String name) throws Exception {
		Codec<Object, String> c = requireCodec(name);
		return c.encode(obj);
	}
	
	public <T> String encode(T obj, Class<T> type) throws Exception {
		Codec<T, String> c = getCodec(type);
		
		if (c==null) {
			if (type == String.class) return (String)obj; //NOTE: this cast is safe
			throw new IllegalArgumentException("don't know codec for type \"" + type + "\"");
		}
		
		return c.encode(obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> String encode(T obj) throws Exception {
		//TODO: if no exact match is found for the class, try superclasses and interfaces.
		return encode(obj, (Class<T>)obj.getClass()); //NOTE: this case is safe.
	}

	public Map<String, Codec> createCodecMapFromTypeMap(Map<String, String> typeMap) {
		Map<String, Codec> codecMap = new HashMap<String, Codec>();
		for (Map.Entry<String, String> entry: typeMap.entrySet()) {
			String propertyName = entry.getKey();
			String typeName = entry.getValue();
			Codec codec = getCodec(typeName);
			if (codec == null) codec = new StringCodec();
			codecMap.put(propertyName, codec);
		}
		return codecMap;
	}

	public void addCodecs(Map<String, Codec> m) {
		codecs.putAll(m);
	}
}
