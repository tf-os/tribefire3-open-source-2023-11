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
package com.braintribe.model.processing.manipulation.marshaller;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AcquireManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.type.BaseTypeImpl;
import com.braintribe.model.generic.reflection.type.custom.EnumTypeImpl;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;

public class RemoteManipulationStringifier extends ManipulationStringifier {

	private final Map<String, String> typeToVar;
	private final Map<EntityReference, String> referenceToVar;
	private final ModelOracle modelOracle;

	public RemoteManipulationStringifier() {
		this(newMap(), emptyMap(), emptySet(), null);
	}

	public RemoteManipulationStringifier(Map<String, String> typeToVar, Map<EntityReference, String> referenceToVar, Collection<String> varNames) {
		this(typeToVar, referenceToVar, varNames, null);
	}
	
	public RemoteManipulationStringifier(Map<String, String> typeToVar, Map<EntityReference, String> referenceToVar, Collection<String> varNames, ModelOracle modelOracle) {
		this.typeToVar = typeToVar;
		this.referenceToVar = CodingMap.create(EntRefHashingComparator.INSTANCE);
		this.referenceToVar.putAll(referenceToVar);
		this.modelOracle = modelOracle;
		recognizeVarNames(varNames);
	}

	// temporary, just as long as we don't implement the file merge that works offline; then we won't need this 
	public Map<String, String> getTypeToVar() {
		return typeToVar;
	}
	
	// temporary, just as long as we don't implement the file merge that works offline; then we won't need this 
	public Map<EntityReference, String> getReferenceToVar() {
		return referenceToVar;
	}
	
	@Override
	protected void writeInstantiationManipulation(Appendable writer, InstantiationManipulation manipulation) throws IOException {
		EntityReference reference = (EntityReference) manipulation.getEntity();
		lastEntity = reference;
		writeReferenceExpression(writer, reference, ReferenceMode.instantiation);
	}
	
	@Override
	protected void writeAcquireManipulation(Appendable writer, AcquireManipulation manipulation) throws IOException {
		EntityReference reference = (EntityReference) manipulation.getEntity();
		lastEntity = reference;
		writeReferenceExpression(writer, reference, ReferenceMode.acquire);
	}
	
	@Override
	protected void writeDeleteManipulation(Appendable writer, DeleteManipulation manipulation) throws IOException {
		EntityReference reference = (EntityReference) manipulation.getEntity();
		writer.append("-");
		writeReferenceExpression(writer, reference, ReferenceMode.value);
	}
	
	private enum ReferenceMode {owner, value, instantiation, acquire}
	
	private void writeReferenceExpression(Appendable writer, EntityReference ref, ReferenceMode referenceMode) throws IOException {
		String varName = referenceToVar.get(ref);
		
		if (varName == null) {
			varName = newReferenceVarName();
			referenceToVar.put(ref, varName);
			
			switch (referenceMode) {
			case instantiation:
				writer.append(varName);
				writer.append('=');
				writeTypeReference(writer, ref.getTypeSignature());
				writer.append("()");
				break;
			case acquire:
				writer.append(varName);
				writer.append('=');
				writeTypeReference(writer, ref.getTypeSignature());
				writer.append("['");
				writer.append(String.valueOf(ref.getRefId()));
				writer.append("']");
				break;
			case owner:
				writer.append(varName);
				writer.append('=');
				writeTypeReference(writer, ref.getTypeSignature());
				writer.append("('");
				writer.append(String.valueOf(ref.getRefId()));
				writer.append("')\n");
				break;
			case value:
				writer.append('(');
				writer.append(varName);
				writer.append('=');
				writeTypeReference(writer, ref.getTypeSignature());
				writer.append("('");
				writer.append(String.valueOf(ref.getRefId()));
				writer.append("'))");
				break;
			}
		}
		else
			writer.append(varName);
	}

	private void writeTypeReference(Appendable writer, String type) throws IOException {
		String varName = typeToVar.get(type);
		
		if (varName == null) {
			varName = newTypeVarName(shorten(type));
			typeToVar.put(type, varName);
		
			writer.append('(');
			writer.append(varName);
			writer.append('=');
			writer.append(type);
			writer.append(')');
		}
		else {
			writer.append(varName);
		}
	}

	private String shorten(String type) {
		int index = type.lastIndexOf('.');
		
		if (index != -1)
			return type.substring(index + 1);
		else
			return type;
	}
	
	@Override
	protected void writeEntity(Appendable writer, GenericEntity entity) throws IOException {
		if (entity.type() == EnumReference.T) {
			EnumReference enumReference = (EnumReference)entity;
			writeEnum(writer, enumReference);
		}
		else {
			EntityReference entityReference = (EntityReference)entity;
			writeReferenceExpression(writer, entityReference, ReferenceMode.value);
		}
	}

	@Override
	protected void writeEnum(Appendable writer, Object enumValue) throws IOException {
		if (enumValue.getClass().isEnum()) {
			Enum<?> enumConstant = (Enum<?>)enumValue;
			writeTypeReference(writer, enumConstant.getDeclaringClass().getName());
			writer.append("::");
			writer.append(enumConstant.name());
		}
		else {
			EnumReference enumReference = (EnumReference)enumValue;
			String enumTypeSignature = enumReference.getTypeSignature();
			writeTypeReference(writer, enumTypeSignature);
			writer.append("::");
			writer.append(enumReference.getConstant());
		}
	}
	
	@Override
	protected void writePropertyManipulationStart(Appendable writer, PropertyManipulation manipulation) throws IOException {
		writePropertyManipulationStart(writer, manipulation, false);
	}

	@Override
	protected GenericModelType writePropertyManipulationStartAndReturnPropertyType(Appendable writer, PropertyManipulation manipulation)
			throws IOException {
		return writePropertyManipulationStart(writer, manipulation, true);
	}

	private GenericModelType writePropertyManipulationStart(Appendable writer, PropertyManipulation manipulation, boolean needsResult)
			throws IOException {
		EntityProperty owner = (EntityProperty) manipulation.getOwner();
		EntityReference reference = owner.getReference();

		if (isLastReference(reference)) {
			writeReferenceExpression(writer, reference, ReferenceMode.owner);
			lastEntity = reference;
		}

		writer.append('.');
		writer.append(owner.getPropertyName());

		if (!needsResult)
			return null;

		if (modelOracle == null)
			return BaseType.INSTANCE;
		else
			return resolveEntityPropertyType((EntityProperty) manipulation.getOwner());

	}
	
	private GenericModelType resolveEntityPropertyType(EntityProperty ep) {
		String typeSignature = ep.getReference().getTypeSignature();
		String propertyName = ep.getPropertyName();

		EntityTypeOracle entityTypeOracle = modelOracle.findEntityTypeOracle(typeSignature);
		if (entityTypeOracle == null)
			return BaseType.INSTANCE;
		
		PropertyOracle propertyOracle = entityTypeOracle.getProperty(propertyName);
		if (propertyOracle == null)
			return BaseType.INSTANCE;
		
		GmType propertyType = propertyOracle.asGmProperty().getType();

		switch (propertyType.typeKind()) {
			case LIST:
				return EssentialTypes.TYPE_LIST;
			case SET:
				return EssentialTypes.TYPE_SET;
			case MAP:
				return EssentialTypes.TYPE_MAP;
			default:
				return BaseType.INSTANCE;
		}
	}

	private static final EnumType SAMPLE_ENUM_TYPE = new EnumTypeImpl(ManipulationType.class);
	
	@Override
	protected GenericModelType resolveActualType(Object value) {
		if (value instanceof EntityReference)
			return GenericEntity.T;
		else if (value instanceof EnumReference)
			return SAMPLE_ENUM_TYPE;
		else if (value instanceof Set)
			return EssentialTypes.TYPE_SET;
		else if (value instanceof List)
			return EssentialTypes.TYPE_LIST;
		else if (value instanceof Map)
			return EssentialTypes.TYPE_MAP;
		else
			return BaseTypeImpl.INSTANCE.getActualType(value);
	}

}
