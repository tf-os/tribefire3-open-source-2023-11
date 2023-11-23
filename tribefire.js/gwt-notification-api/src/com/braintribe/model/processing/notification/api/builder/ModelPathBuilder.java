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
package com.braintribe.model.processing.notification.api.builder;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmPropertyPathElement;
import com.braintribe.model.path.GmRootPathElement;

/**
 * Builder API to fluently create a {@link GmModelPath} 
 */
public interface ModelPathBuilder {

	/**
	 * Adds a given {@link GmModelPathElement} to the modelPath. 
	 */
	ModelPathBuilder addElement(GmModelPathElement element, boolean isSelected, boolean openWithAction);
	/**
	 * Adds a given {@link GmModelPathElement} to the modelPath. 
	 */
	ModelPathBuilder addElement(GmModelPathElement element);
	/**
	 * Creates and adds a new {@link GmRootPathElement} based on given typeSignature and value to the modelPath. 
	 */
	ModelPathBuilder addElement(String typeSignature, Object value, boolean isSelected, boolean openWithAction);
	/**
	 * Creates and adds a new {@link GmRootPathElement} based on given typeSignature and value to the modelPath. 
	 */
	ModelPathBuilder addElement(String typeSignature, Object value);
	/**
	 * Creates and adds a new {@link GmRootPathElement} based on given entity to the modelPath.
	 */
	ModelPathBuilder addElement(GenericEntity entity, boolean isSelected, boolean openWithAction);
	/**
	 * Creates and adds a new {@link GmRootPathElement} based on given entity to the modelPath.
	 */
	ModelPathBuilder addElement(GenericEntity entity);
	/**
	 * Creates and adds a new {@link GmPropertyPathElement} based on given entity and property to the modelPath.
	 */
	ModelPathBuilder addElement(GenericEntity entity, String property, boolean isSelected, boolean openWithAction);
	/**
	 * Creates and adds a new {@link GmPropertyPathElement} based on given entity and property to the modelPath.
	 */
	ModelPathBuilder addElement(GenericEntity entity, String property);
	
	/**
	 * Builds the {@link GmModelPath} based on the elements added before. 
	 */
	NotificationBuilder close();
	
	
}
