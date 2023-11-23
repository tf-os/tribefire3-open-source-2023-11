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
package com.braintribe.gwt.customization.client;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.gwt.genericmodel.client.codec.dom4.GmXmlCodec;
import com.braintribe.gwt.gmview.client.GmSelectionListener;
import com.braintribe.gwt.gmview.client.GmSelectionSupport;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.GenericModelType;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * Utility class with methods to be used in the JS side, via JsInterop.
 * It is used for being able to manipulate Java objects within JS code via the exposed methods.
 * This is inside the PD namespace.
 *
 */
@JsType(namespace = "$pd.util")
@SuppressWarnings("unusable-by-js")
public class JsUtil {
	
	private static class TypeAndRefId {
		private String typeSignature;
		private Object key;
		
		public TypeAndRefId(String typeSignature, Object key) {
			this.typeSignature = typeSignature;
			this.key = key;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((typeSignature == null) ? 0 : typeSignature.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeAndRefId other = (TypeAndRefId) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (typeSignature == null) {
				if (other.typeSignature != null)
					return false;
			} else if (!typeSignature.equals(other.typeSignature))
				return false;
			return true;
		}
	}
	
	public static Map<TypeAndRefId, Object> entityReferences = new HashMap<>();
	
	@JsConstructor
	public JsUtil() {
		super();
	}
	
	/**
	 * Adds a new entry to the entityReferences map, which types from refId in the PD space, and external refIds from
	 * the explorer space.
	 */
	@JsMethod
	public void putReferences(String typeSignature, Object refId, Object externalRefId) {
		entityReferences.put(new TypeAndRefId(typeSignature, refId), externalRefId);
	}
	
	/**
	 * Returns the externalRefId mapped to the given typeSignature and refId.
	 */
	@JsMethod
	public Object getReference(String typeSignature, Object refId) {
		return entityReferences.get(new TypeAndRefId(typeSignature, refId));
	}
	
	/**
	 * Returns the externalRefId mapped to the given entity.
	 */
	@JsMethod
	public Object getReferenceByEntity(GenericEntity entity) {
		return entityReferences.get(new TypeAndRefId(entity.entityType().getTypeSignature(), entity.reference().getRefId()));
	}
	
	@JsMethod
	public boolean hasReferences() {
		return !entityReferences.isEmpty();
	}
	
	/**
	 * Prepares a {@link ModelPath}.
	 */
	@JsMethod
	public ModelPath prepareModelPath() {
		return new ModelPath();
	}
	
	/**
	 * Prepares a {@link RootPathElement} with the given object.
	 */
	@JsMethod
	public RootPathElement prepareRootPathElement(Object object) {
		GenericModelType type = GMF.getTypeReflection().getType(object);
		return new RootPathElement(type , object);
	}
	
	/**
	 * Adds the given {@link ModelPathElement} to the given {@link ModelPath}.
	 */
	@JsMethod
	public void addElementToModelPath(ModelPath modelPath, ModelPathElement modelPathElement) {
		modelPath.add(modelPathElement);
	}
	
	@JsMethod
	public boolean isEntity(Object entity) {
		return entity instanceof GenericEntity;
	}
	
	@JsMethod
	public GmSelectionListener createSelectionListener(Object externalListener, GmSelectionSupport externalSupport) {
		return new GmSelectionListener() {
			@Override
			public void onSelectionChanged(GmSelectionSupport gmSelectionSupport) {
				nativeSelectionChanged(externalListener, externalSupport);
			}
		};
	}
	
	@JsMethod
	public <T> String encodeData(T data) {
		GmXmlCodec<T> codec = new GmXmlCodec<>();
		return codec.encode(data);
	}
	
	@JsMethod
	public <T> T decodeData(String encodedData) {
		GmXmlCodec<T> codec = new GmXmlCodec<>();
		return codec.decode(encodedData);
	}
	
	private native void nativeSelectionChanged(Object listener, GmSelectionSupport support) /*-{
		listener.onSelectionChanged(support);
	}-*/;

}
