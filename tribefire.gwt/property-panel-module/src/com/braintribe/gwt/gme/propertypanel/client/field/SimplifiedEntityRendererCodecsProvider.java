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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.util.function.Supplier;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.codec.registry.client.CodecEntry;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * This Function is responsible for providing codecs based on the given Class.
 * @author michel.docouto
 *
 */
public class SimplifiedEntityRendererCodecsProvider extends CodecRegistry<String> {
	
	private CodecRegistry<String> codecRegistry;
	private Supplier<SimplifiedEntityRendererCodec> simplifiedEntityFieldRendererCodecSupplier;
	
	/**
	 * Configures the required {@link CodecRegistry} used as renderers.
	 */
	@Required
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}
	
	/**
	 * Configures the simplified entity renderer codec.
	 */
	@Required
	public void setSimplifiedEntityFieldRendererCodec(Supplier<SimplifiedEntityRendererCodec> simplifiedEntityFieldRendererCodecSupplier) {
		this.simplifiedEntityFieldRendererCodecSupplier = simplifiedEntityFieldRendererCodecSupplier;
	}
	
	@Override
	public <T> Codec<T, String> getCodec(Class<?> clazz) {
		Codec<T, String> codec = codecRegistry.getCodec(clazz);
		if (codec != null)
			return codec;
		
		if (!isGenericEntityInstance(clazz))
			return null;
		
		return (Codec<T, String>) simplifiedEntityFieldRendererCodecSupplier.get();
	}
	
	@Override
	public CodecEntry<String> getCodecEntry(Class<?> clazz) {
		CodecEntry<String> codecEntry = codecRegistry.getCodecEntry(clazz);
		if (codecEntry != null)
			return codecEntry;
		
		if (!isGenericEntityInstance(clazz))
			return null;
		
		return new CodecEntry<>(clazz, simplifiedEntityFieldRendererCodecSupplier);
	}

	private boolean isGenericEntityInstance(Class<?> clazz) {
		return GMF.getTypeReflection().getType(clazz) instanceof EntityType<?>;
	}

}
