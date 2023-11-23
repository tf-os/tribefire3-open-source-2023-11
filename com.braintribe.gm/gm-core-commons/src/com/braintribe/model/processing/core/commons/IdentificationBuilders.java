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
package com.braintribe.model.processing.core.commons;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;

public class IdentificationBuilders {

	@SuppressWarnings("rawtypes")
	public static IdentificationBuilder<?> id(Object id) {
		return new IdentificationBuilderImpl(id);
	}
	
	public static IdentificationBuilderFromGenericEntity<?> fromInstance(GenericEntity instance) {
		return new IdentificationBuilderFromGenericEntityImpl(instance);
	}
	
	
	private static class IdentificationBuilderImpl<I extends IdentificationBuilderImpl<I>> implements IdentificationBuilder<I> {
		protected String outerNamespace = "";
		protected String namespace = "";
		protected Object id;
		protected I self;
		
		public IdentificationBuilderImpl(Object id) {
			this.id = id;
			this.self = (I)this;
		}
		
		public IdentificationBuilderImpl() {
			this.self = (I)this;
		}

		@Override
		public I outerNamespace(String outerNamespace) {
			this.outerNamespace = outerNamespace;
			return self;
		}

		@Override
		public I namespace(String namespace) {
			this.namespace = namespace;
			return self;
		}

		@Override
		public I namespace(Class<? extends GenericEntity> typeSignature) {
			this.namespace = typeSignature.getName();
			return self;
		}

		@Override
		public I namespace(EntityType<?> typeSignature) {
			this.namespace = typeSignature.getTypeSignature();
			return self;
		}
		
		@Override
		public String build() {
			return escape(outerNamespace) + ":" + escape(namespace) + ":" + escape(id.toString());
		}
		
		
	}
	
	private static class IdentificationBuilderFromGenericEntityImpl 
		extends IdentificationBuilderImpl<IdentificationBuilderFromGenericEntityImpl> 
		implements IdentificationBuilderFromGenericEntity<IdentificationBuilderFromGenericEntityImpl> {
		
		private final String typeSignature;
		
		private IdentificationBuilderFromGenericEntityImpl(GenericEntity entity) {
			this.typeSignature = entity.entityType().getTypeSignature(); 
			this.id = entity.getId();
		}

		@Override
		public IdentificationBuilderFromGenericEntityImpl namespaceFromType() {
			namespace = typeSignature;
			return self;
		}
	}

	protected static String escape(String s) {
		return s.replace("&", "&amp;").replace(":", "&#58;");
	}
	
	protected static String unescape(String s) {
		return s.replace("&#58;", ":").replace("&amp;", "&");
	}

}
