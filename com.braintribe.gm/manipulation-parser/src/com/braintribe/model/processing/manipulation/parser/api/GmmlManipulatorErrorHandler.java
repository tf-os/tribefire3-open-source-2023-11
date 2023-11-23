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
package com.braintribe.model.processing.manipulation.parser.api;

import java.util.logging.StreamHandler;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.parser.impl.listener.GmmlManipulatorParserListener;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.TrackingErrorHandler;

/**
 * Handler for various non-syntax related errors that occur while parsing/applying GMML with {@link GmmlManipulatorParserListener}.
 * 
 * @see StreamHandler
 * @see LenientErrorHandler
 * @see TrackingErrorHandler
 * 
 * @author peter.gazdik
 */
public interface GmmlManipulatorErrorHandler {

	default void onStart() {
		// Optional
	}

	default void onEnd() {
		// Optional
	}

	void typeNotFound(String typeSignature);

	void propertyNotFound(GenericEntity entity, String propertyName);

	void enumConstantNotFound(EnumType enumType, String enumConstantName);

	void entityNotFound(String globalId);

	/** @see GmmlManipulatorParserConfiguration#problematicEntitiesRegistry() */
	void problematicEntityReferenced(String globalId);

	void variableNotEntityType(String variableName);

	void propertyNotCollection(GenericEntity entity, Property property);

	void propertyValueNotCollectionCannotClear(GenericEntity entity, Property property, Object value);

	void propertySettingFailed(GenericEntity entity, Property property, Object value, RuntimeException e);

	void wrongValueTypeToAddToCollection(Object value, CollectionType type);

	void wrongTypeForListAdd(Object index, boolean indexOk, Object value, boolean valueOk, GenericModelType valueType);

	void wrongTypeForMapPut(Object key, boolean keyOk, Object value, boolean valueOk, MapType type);

	void cannotResolvePropertyOfNonEntity(Object nonEntity, String propertyName);

	void typeNotGenericModelType(Object type);

	// GlobalId

	void globalIdSettingFailed(GenericEntity entity, Object globalId, RuntimeException e);

	/**
	 * Unreachable if {@link #globalIdSettingFailed(GenericEntity, Object, RuntimeException)} throws an exception (i.e. not handled leniently).
	 */
	void globalIdAdjusted(GenericEntity entity, Object globalId, String newGlobalId, RuntimeException e);

}
