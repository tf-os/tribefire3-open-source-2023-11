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
package com.braintribe.model.processing.manipulation.parser.impl.listener.error;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;

/**
 * {@link GmmlManipulatorErrorHandler} for the lenient approach, ignoring any errors.
 * <p>
 * A little more details about what ignoring errors means. We will use the term "missing value" for any value that was not resolved properly, such as
 * an instance of an unknown type, unknown enum constant and entity that was not found by the referenced globalId
 * <ul>
 * <li>Assigning a missing value to a property is ignored.
 * <li>Adding a missing value to a <tt>set</tt> is ignored.
 * <li>Putting an entry with either a missing key or missing value to a <tt>map</tt> is ignored.
 * <li>Adding a missing value to a <tt>list</tt> adds <tt>null</tt> to the <tt>list</tt>.
 * </ul>
 * 
 * @author peter.gazdik
 */
public class LenientErrorHandler implements GmmlManipulatorErrorHandler {

	public static final LenientErrorHandler INSTANCE = new LenientErrorHandler();

	protected LenientErrorHandler() {
	}

	@Override
	public void typeNotFound(String typeSignature) {
		// ignore
	}

	@Override
	public void propertyNotFound(GenericEntity entity, String propertyName) {
		// ignore
	}

	@Override
	public void enumConstantNotFound(EnumType enumType, String enumConstantName) {
		// ignore
	}

	@Override
	public void entityNotFound(String globalId) {
		// ignore
	}

	@Override
	public void problematicEntityReferenced(String globalId) {
		// ignore
	}

	@Override
	public void variableNotEntityType(String variableName) {
		// ignore
	}

	@Override
	public void propertyNotCollection(GenericEntity entity, Property property) {
		// ignore
	}

	@Override
	public void propertyValueNotCollectionCannotClear(GenericEntity entity, Property property, Object value) {
		// ignore
	}

	@Override
	public void propertySettingFailed(GenericEntity entity, Property property, Object value, RuntimeException e) {
		// ignore
	}

	@Override
	public void wrongValueTypeToAddToCollection(Object value, CollectionType type) {
		// ignore
	}

	@Override
	public void wrongTypeForListAdd(Object index, boolean indexOk, Object value, boolean valueOk, GenericModelType valueType) {
		// ignore
	}

	@Override
	public void wrongTypeForMapPut(Object key, boolean keyOk, Object value, boolean valueOk, MapType type) {
		// ignore
	}

	@Override
	public void cannotResolvePropertyOfNonEntity(Object nonEntity, String propertyName) {
		// ignore
	}

	@Override
	public void typeNotGenericModelType(Object type) {
		// ignore
	}

	// globalId

	@Override
	public void globalIdSettingFailed(GenericEntity entity, Object globalId, RuntimeException e) {
		// ignore
	}

	@Override
	public void globalIdAdjusted(GenericEntity entity, Object globalId, String newGlobalId, RuntimeException e) {
		// ignore
	}

}
