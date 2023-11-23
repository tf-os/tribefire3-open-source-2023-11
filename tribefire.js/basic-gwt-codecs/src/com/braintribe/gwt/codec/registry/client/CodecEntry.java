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

import java.util.function.Supplier;

import com.braintribe.codec.Codec;

public class CodecEntry<E> {
	private Class<?> clazz;
	private Supplier<? extends Codec<?, E>> codecSupplier;
	
	public CodecEntry(Class<?> clazz, Supplier<? extends Codec<?, E>> codecSupplier) {
		this.clazz = clazz;
		this.codecSupplier = codecSupplier;
	}
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	@SuppressWarnings("rawtypes")
	public <T> Supplier<Codec<T, E>> getCodec() {
		return (Supplier) codecSupplier;
	}
}
