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
package com.braintribe.gwt.codec.registry.client;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.gwt.ioc.client.Configurable;

public class CodecRegistry<E> {
	private Map<Class<?>, CodecEntry<E>> codecEntries = new HashMap<Class<?>, CodecEntry<E>>();
	
	@Configurable
	public void setCodecMap(Map<Class<?>, Supplier<? extends Codec<?, E>>> codecMap) {
		codecEntries.clear();
		
		for (Map.Entry<Class<?>, Supplier<? extends Codec<?, E>>> entry: codecMap.entrySet()) {
			Class<?> clazz = entry.getKey();
			Supplier<? extends Codec<?, E>> codecSupplier = entry.getValue();
			
			CodecEntry<E> codecEntry = new CodecEntry<E>(clazz, codecSupplier);
			codecEntries.put(clazz, codecEntry);
		}
	}
	
	public CodecEntry<E> requireCodecEntry(Class<?> clazz) throws CodecException {
		CodecEntry<E> codecEntry = getCodecEntry(clazz);
		
		if (codecEntry != null)
			return codecEntry;
		
		throw new CodecException("no codec registered for class " +  clazz);
	}
	
	public <T> Codec<T, E> requireCodec(Class<?> clazz) throws CodecException {
		Codec<T, E> codec = getCodec(clazz);
		
		if (codec != null)
			return codec;
		
		throw new CodecException("no codec registered for class " +  clazz);
	}
	
	public CodecEntry<E> getCodecEntry(Class<?> clazz) {
		return codecEntries.get(clazz);
	}
	
	public <T> Codec<T, E> getCodec(Class<?> clazz) {
		CodecEntry<E> codecEntry = codecEntries.get(clazz);
		if (codecEntry != null)
			return getCodecFromSupplier(codecEntry.getCodec());
		
		return null;
	}
	
	private <T> Codec<T, E> getCodecFromSupplier(Supplier<Codec<T, E>> supplier) {
		Codec<T, E> codec = supplier.get();
		if (codec instanceof CodecRegistryAware) {
			CodecRegistryAware<E> codecRegistryAware = (CodecRegistryAware<E>) codec;
			codecRegistryAware.intializeCodecRegistry(this);
		}
		
		return codec;
	}
}
