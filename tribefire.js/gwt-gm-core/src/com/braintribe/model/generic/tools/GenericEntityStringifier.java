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
package com.braintribe.model.generic.tools;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;

public class GenericEntityStringifier {
	
	private static Set<Property> specialProperties = 
			asSet(
					GenericEntity.T.getProperty(GenericEntity.globalId),
					GenericEntity.T.getProperty(GenericEntity.partition));
	
	
	private final Collection<String> excludes = new ArrayList<>();
	private boolean referenceOnly = false;
	private boolean includeSpecialProperties = false;
	private boolean traverse = false;

	private String lineSeparator = System.lineSeparator();
	private String indent = " ";
	private boolean supressNullValues = false;
	private boolean shortIdInReference = false;
	
	/**
	 * Private constructor. use {@link #newInstance()} to get an instance.
	 */
	private GenericEntityStringifier() {
		
	}
	
	/**
	 * Returns a new instance of {@link GenericEntityStringifier}
	 */
	public static GenericEntityStringifier newInstance() {
		return new GenericEntityStringifier();
	}

	public GenericEntityStringifier addExclude(String name) {
		this.excludes.add(name);
		return this;
	}
	
	public GenericEntityStringifier referenceOnly() {
		this.referenceOnly = true;
		return this;
	}
	
	public GenericEntityStringifier includeSpecialProperties() {
		this.includeSpecialProperties = true;
		return this;
	}

	public GenericEntityStringifier traverse() {
		this.traverse = true;
		return this;
	}
	
	public GenericEntityStringifier lineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
		return this;
	}
	
	public GenericEntityStringifier intend(String indent) {
		this.indent = indent;
		return this;
	}
	
	public GenericEntityStringifier supressNullValues() {
		this.supressNullValues = true;
		return this;
	}
	
	public GenericEntityStringifier shortIdInReference() {
		this.shortIdInReference = true;
		return this;
	}
	
	
	/**
	 * Returns a string representation of the given {@link GenericEntity} instance.
	 */
	public String stringify (GenericEntity entity) {
		try {
			StringWriter writer = new StringWriter();
			this.stringify(entity, writer);
			return writer.toString();
		} catch(IOException ioe) {
			throw new RuntimeException("Could not stringify the entity "+entity, ioe);
		}
	}
	
	/**
	 * Returns a string representation of the given {@link GenericEntity} instance.
	 */
	public void stringify (GenericEntity entity, Writer writer) throws IOException {
		final List<GenericEntity> entities = new ArrayList<>();
		
		if (traverse) {
			traverseAllEntities(entity, entities);
		} else {
			entities.add(entity);
		}
		
		for (GenericEntity entityToStringify : entities) {
			appendEntity(entityToStringify, this.referenceOnly, writer);
		}
	}

	/**
	 * Traverses all referenced entities recursively and adds it to the given entity collection.
	 */
	private static void traverseAllEntities(GenericEntity entity, final List<GenericEntity> entities) {
		entity.entityType().traverse(entity, null, new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity e, EntityCriterion criterion, TraversingContext traversingContext) {
				entities.add(e);
			}
		});
	}

	/**
	 * Returns a string representation of the given {@link GenericEntity} instance.
	 */
	private void appendEntity (GenericEntity entity, boolean refOnly, Writer writer) throws IOException {
		EntityType<GenericEntity> type = entity.entityType();
		
		appendReference(type, entity, writer);
		
		if (refOnly) {
			return;
		}
		
		writer.append(' ');
		writer.append('{');
		appendLineBreak(writer);

		List<Property> properties = type.getProperties();
		for (Property property : properties) {
			Object propertyValue = property.get(entity);
			
			if (isExcluded(property, propertyValue)) {
				continue;
			}
			
			writer.append(indent);
			appendProperty(property.getType(), property.getName(), propertyValue, writer);
			appendLineBreak(writer);
			
		}
		writer.append('}');
		
	}

	private boolean isExcluded(Property property, Object propertyValue) {
		return property.isIdentifier() //
				|| (!includeSpecialProperties && isSpecialProperty(property)) //
				|| (supressNullValues && propertyValue == null);
	}

	private void appendValue(GenericModelType valueType, Object propertyValue, Writer writer) throws IOException {
		
		if (propertyValue == null) {
			writer.append("null");
			return;
		}
		
		switch (valueType.getTypeCode()) {
		case stringType:
		case booleanType:
		case integerType:
		case longType:
		case doubleType:
		case floatType:
		case decimalType:
		case dateType:
		case enumType:
			writer.append(propertyValue.toString());
			break;
		case objectType:
			appendValue(GMF.getTypeReflection().getType(propertyValue), propertyValue, writer);
			break;
		case entityType:
			appendEntity((GenericEntity)propertyValue,true, writer);
			break;
		case listType:
			writer.append('[');
			appendCollection((CollectionType) valueType, (List<Object>) propertyValue, writer);
			writer.append(']');
			break;
		case setType:
			writer.append('(');
			appendCollection((CollectionType) valueType, (Set<Object>) propertyValue, writer);
			writer.append(')');
			break;
		case mapType:
			writer.append('{');
			appendCollection((CollectionType) valueType, ((Map<Object,Object>)propertyValue).entrySet(), writer);
			writer.append('}');
			break;
		
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void appendCollection(CollectionType propertyCollectionType, Collection<?> collection, Writer writer) throws IOException {
		int i = 0;
		for (Object element : collection) {
			
			if ((i++) > 0) writer.append(", ");
			if (i > 10) appendLineBreak(writer);
			
			switch (propertyCollectionType.getTypeCode()) {
			case setType:
			case listType:
				appendValue(propertyCollectionType.getCollectionElementType(), element, writer);
				break;
			case mapType:
				Entry<Object, Object> entry = (Map.Entry<Object,Object>) element;
				appendValue(propertyCollectionType.getParameterization()[0], entry.getKey(), writer);
				writer.append(":");
				appendValue(propertyCollectionType.getParameterization()[1], entry.getValue(), writer);
				break;
			}
			
		}
	}

	private void appendReference(EntityType<GenericEntity> type, GenericEntity entity, Writer writer) throws IOException {
		Object idValue = entity.getId();
		
		writer.append(type.getShortName());
		writer.append("(");
		
		if (idValue != null) {
			if (shortIdInReference) {
				appendValue(BaseType.INSTANCE, idValue, writer);
			} else {
				appendProperty(BaseType.INSTANCE, GenericEntity.id, idValue, writer,":");
			}
		}
		
		writer.append(")");
	}

	private void appendProperty(GenericModelType propertyType,  String propertyName, Object propertyValue, Writer writer) throws IOException {
		appendProperty(propertyType, propertyName, propertyValue, writer, ": ");
	}
	private void appendProperty(GenericModelType propertyType,  String propertyName, Object propertyValue, Writer writer, String delimiter) throws IOException {
		writer.append(propertyName);
		writer.append(delimiter);
		appendValue(propertyType, propertyValue, writer);
	}
	
	private void appendLineBreak(Writer writer) throws IOException {
		writer.append(lineSeparator);
	}
	
	private static boolean isSpecialProperty(Property property) {
		return specialProperties.contains(property);
	}

	
}
