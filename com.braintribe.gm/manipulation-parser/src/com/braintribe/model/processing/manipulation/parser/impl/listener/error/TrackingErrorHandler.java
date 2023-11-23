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

import static com.braintribe.utils.lcd.CollectionTools2.acquireSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;

/**
 * Lenient {@link GmmlManipulatorErrorHandler} which records all the errors.
 * <p>
 * See {@link LenientErrorHandler} to see what lenient handling means in terms of actual effect on the final data.
 * 
 * @author peter.gazdik
 */
public class TrackingErrorHandler implements GmmlManipulatorErrorHandler {

	// These are only supposed to be read from the outside

	private final String accessId;

	public final Set<String> missingTypes = newSet();

	public final Map<EntityType<?>, Set<String>> missingProperties = newMap();
	public final Map<EnumType, Set<String>> missingConstants = newMap();

	public final Set<String> problematicGlobalIds = newSet();
	public final Set<String> notFoundEntities = newSet();

	public final Set<String> otherProblems = newSet();

	public TrackingErrorHandler(String accessId) {
		this.accessId = accessId;
	}

	@Override
	public void onStart() {
		missingTypes.clear();
		missingProperties.clear();
		missingConstants.clear();
		problematicGlobalIds.clear();
		notFoundEntities.clear();
		otherProblems.clear();
	}

	@Override
	public void typeNotFound(String typeSignature) {
		missingTypes.add(typeSignature);
	}

	@Override
	public void propertyNotFound(GenericEntity entity, String propertyName) {
		acquireSet(missingProperties, entity.entityType()).add(propertyName);
	}

	@Override
	public void enumConstantNotFound(EnumType enumType, String enumConstantName) {
		acquireSet(missingConstants, enumType).add(enumConstantName);
	}

	@Override
	public void entityNotFound(String globalId) {
		notFoundEntities.add(globalId);
	}

	@Override
	public void problematicEntityReferenced(String globalId) {
		// nothing to do, the globalId will be problematic forever
	}

	@Override
	public void variableNotEntityType(String variableName) {
		otherProblems.add("Variable is not an entity type: " + variableName);
	}

	@Override
	public void propertyNotCollection(GenericEntity entity, Property property) {
		otherProblems.add("Property " + property + " is not a collection. Entity:" + entity);
	}

	@Override
	public void propertyValueNotCollectionCannotClear(GenericEntity entity, Property property, Object value) {
		otherProblems.add("Cannot clear property '" + property + "' of entity '" + entity + "' as it is not a collection. Property value: " + value);
	}

	@Override
	public void propertySettingFailed(GenericEntity entity, Property property, Object value, RuntimeException e) {
		otherProblems.add("Value '" + value + "' for property '" + property + "' cannot be set. Entity:" + entity + ". Error: " + e.getMessage());
	}

	@Override
	public void wrongValueTypeToAddToCollection(Object value, CollectionType type) {
		otherProblems.add("Invalid value type. Cannot add '" + value + "' to:" + type.getTypeSignature());
	}

	@Override
	public void wrongTypeForListAdd(Object index, boolean indexOk, Object value, boolean valueOk, GenericModelType valueType) {
		otherProblems.add("Invalid value type. Cannot add element to list. Index: '" + index + "' , value: '" + value + "', value type:"
				+ valueType.getTypeSignature());
	}

	@Override
	public void wrongTypeForMapPut(Object key, boolean keyOk, Object value, boolean valueOk, MapType type) {
		otherProblems.add("Cannot put entry ('" + key + "', '" + value + "') to: " + type);
	}

	@Override
	public void cannotResolvePropertyOfNonEntity(Object nonEntity, String propertyName) {
		otherProblems.add("Cannot resolve property '" + propertyName + ". Owner is not an entity, but: " + nonEntity);
	}

	@Override
	public void typeNotGenericModelType(Object type) {
		otherProblems.add("This is not a GenericModelType: " + type);
	}

	// globalId

	@Override
	public void globalIdSettingFailed(GenericEntity entity, Object globalId, RuntimeException e) {
		if (globalId instanceof String)
			problematicGlobalIds.add((String) globalId);

		otherProblems.add("Error while setting globalId '" + globalId + "' for entity: " + entity + ". Error: " + e.getMessage());
	}

	@Override
	public void globalIdAdjusted(GenericEntity entity, Object globalId, String newGlobalId, RuntimeException e) {
		if (globalId instanceof String)
			problematicGlobalIds.add((String) globalId);

		otherProblems
				.add("GlobalId '" + globalId + "' had to be changed to '" + newGlobalId + "' for entity: " + entity + ". Error: " + e.getMessage());
	}

	public void writeReport(Writer writer) {
		if (hasErrors())
			writerErrors(writer);
	}

	public boolean hasErrors() {
		return !allEmpty();
	}

	private boolean allEmpty() {
		return missingTypes.isEmpty() && //
				missingProperties.isEmpty() && //
				missingConstants.isEmpty() && //
				problematicGlobalIds.isEmpty() && //
				notFoundEntities.isEmpty() && //
				otherProblems.isEmpty();
	}

	private void writerErrors(Writer writer) {
		new ErrorWriter(writer).write();
	}

	private class ErrorWriter {

		private final Writer writer;

		public ErrorWriter(Writer writer) {
			this.writer = writer;
		}

		public void write() {
			writeMissingTypes();
			writeMissingProperties();
			writeMissingConstants();
			writeNotFoundEntities();
			writeOtherProblems();
		}

		private void writeMissingTypes() {
			if (!missingTypes.isEmpty())
				append("Missing types: " + missingTypes);
		}

		private void writeMissingProperties() {
			if (!missingProperties.isEmpty())
				for (Entry<EntityType<?>, Set<java.lang.String>> e : missingProperties.entrySet())
					append("Missing properties for entity type '" + e.getKey().getTypeSignature() + ": " + e.getValue());
		}

		private void writeMissingConstants() {
			if (!missingConstants.isEmpty())
				for (Entry<EnumType, Set<String>> e : missingConstants.entrySet())
					append("Missing constants for enum type '" + e.getKey().getTypeSignature() + ": " + e.getValue());
		}

		private void writeNotFoundEntities() {
			if (!notFoundEntities.isEmpty())
				append("Entities not found: " + notFoundEntities);
		}

		private void writeOtherProblems() {
			if (!otherProblems.isEmpty())
				for (String s : otherProblems)
					append(s);
		}

		private void append(String s) {
			try {
				writer.append(s + "\n");
			} catch (IOException e) {
				throw new RuntimeException("Error while appending issue descriptor in access: " + accessId + ". Text being writtern: " + s, e);
			}
		}
	}
}
