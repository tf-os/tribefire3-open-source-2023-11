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
package com.braintribe.model.processing.dataio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;

/**
 * This outputstream serializes GenericModel assemblies to a binary format.
 * @author dirk.scheffler
 *
 */
public class GenericModelOutputStream extends OutputStream implements ObjectOutput, SerializationCodes {
	
	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private int depth;
	private Map<GenericEntity, Integer> referenceIds = new HashMap<GenericEntity, Integer>();
	private Map<EntityType<?>, Byte> typeIds = new HashMap<EntityType<?>, Byte>();
	private Map<Property, Short> propertyIds = new HashMap<Property, Short>();
	private int referenceIdSequence = 9;
	private short propertyIdSequence;
	private byte typeIdSequence;
	
	private NullExpert nullExpert = new NullExpert();
	private ObjectExpert objectExpert = new ObjectExpert();
	private EntityExpert entityExpert = new EntityExpert();
	private EnumExpert enumExpert = new EnumExpert();
	private StringExpert stringExpert = new StringExpert();
	private DateExpert dateExpert = new DateExpert();
	private DecimalExpert decimalExpert = new DecimalExpert();
	private BooleanExpert booleanExpert = new BooleanExpert();
	private IntegerExpert integerExpert = new IntegerExpert();
	private LongExpert longExpert = new LongExpert();
	private FloatExpert floatExpert = new FloatExpert();
	private DoubleExpert doubleExpert = new DoubleExpert();
	private MapExpert<Object, Object> defaultMapExpert = new MapExpert<Object, Object>(objectExpert, objectExpert);
	private SetExpert<Object> defaultSetExpert = new SetExpert<Object>(objectExpert);
	private ListExpert<Object> defaultListExpert = new ListExpert<Object>(objectExpert);
	
	private Map<CollectionType, OutputExpert<?>> collectionExperts = new HashMap<CollectionType, GenericModelOutputStream.OutputExpert<?>>();
	private Map<Class<?>, OutputExpert<?>> simpleExperts = new HashMap<Class<?>, GenericModelOutputStream.OutputExpert<?>>();
	private Set<String> requiredTypes = null;
	
	private DataOutputStream dataOut;
	private boolean writeRequiredTypes;
	
	public GenericModelOutputStream(OutputStream out) {
		this(out, false);
	}
	
	public GenericModelOutputStream(OutputStream out, boolean writeRequiredTypes) {
		this.dataOut = new DataOutputStream(out);
		this.writeRequiredTypes = writeRequiredTypes;
		if (writeRequiredTypes)
			requiredTypes = new HashSet<String>();
		
		BaseType baseType = typeReflection.getBaseType();
		CollectionType defaultListType = typeReflection.getCollectionType(List.class, new GenericModelType[]{baseType});
		CollectionType defaultSetType = typeReflection.getCollectionType(Set.class, new GenericModelType[]{baseType});
		CollectionType defaultMapType = typeReflection.getCollectionType(Map.class, new GenericModelType[]{baseType, baseType});
		collectionExperts.put(defaultListType, defaultListExpert);
		collectionExperts.put(defaultSetType, defaultSetExpert);
		collectionExperts.put(defaultMapType, defaultMapExpert);
		
		simpleExperts.put(String.class, stringExpert);
		simpleExperts.put(Boolean.class, booleanExpert);
		simpleExperts.put(Integer.class, integerExpert);
		simpleExperts.put(Long.class, longExpert);
		simpleExperts.put(Float.class, floatExpert);
		simpleExperts.put(Double.class, doubleExpert);
		simpleExperts.put(BigDecimal.class, decimalExpert);
		simpleExperts.put(Date.class, dateExpert);
	}
	
	protected OutputExpert<?> getOutputExpert(Object value) throws IOException {
		if (value == null)
			return nullExpert;
		
		Class<?> valueClass = value.getClass();
		if (valueClass.isEnum()) {
			return enumExpert;
		}
		else if (value instanceof GenericEntity) {
			return entityExpert;
		}
		else if (value instanceof Set<?>) {
			return defaultSetExpert;
		}
		else if (value instanceof List<?>) {
			return defaultListExpert;
		}
		else if (value instanceof Map<?, ?>) {
			return defaultMapExpert;
		}
		else if (value instanceof Date) {
			return simpleExperts.get(Date.class);
		}
		else {
			OutputExpert<?> outputExpert = simpleExperts.get(valueClass);
			if (outputExpert != null)
				return outputExpert;
			else
				throw new IOException("unsupported value type " + valueClass);
		}
	}
	
	protected OutputExpert<?> getOutputExpert(GenericModelType type) throws IOException {
		TypeCode typeCode = type.getTypeCode();
		
		switch (typeCode) {
		case objectType: return objectExpert;
		case entityType: return entityExpert;
		
		case enumType: return enumExpert;
		
		case booleanType: return booleanExpert;
		case integerType: return integerExpert;
		case longType: return longExpert;
		case floatType: return floatExpert;
		case doubleType: return doubleExpert;
		
		case stringType: return stringExpert;
		case dateType: return dateExpert;
		case decimalType: return decimalExpert;
		
		case listType:
			CollectionType listType = (CollectionType)type;
			OutputExpert<?> listExpert = collectionExperts.get(listType);
			if (listExpert == null) {
				OutputExpert<Object> elementExpert = getOutputExpert(listType.getCollectionElementType()).cast();
				listExpert = new ListExpert<Object>(elementExpert);
				collectionExperts.put(listType, listExpert);
			}
			
			return listExpert;
			
		case setType:
			CollectionType setType = (CollectionType)type;
			OutputExpert<?> setExpert = collectionExperts.get(setType);
			if (setExpert == null) {
				OutputExpert<Object> elementExpert = getOutputExpert(setType.getCollectionElementType()).cast();
				setExpert = new SetExpert<Object>(elementExpert);
				collectionExperts.put(setType, setExpert);
			}
			
			return setExpert;
			
		case mapType: 
			CollectionType mapType = (CollectionType)type;
			OutputExpert<?> mapExpert = collectionExperts.get(mapType);
			if (mapExpert == null) {
				OutputExpert<Object> keyExpert = getOutputExpert(mapType.getParameterization()[0]).cast();
				OutputExpert<Object> valueExpert = getOutputExpert(mapType.getParameterization()[1]).cast();
				mapExpert = new MapExpert<Object, Object>(keyExpert, valueExpert);
				collectionExperts.put(mapType, mapExpert);
			}
			
			return mapExpert;
		}
		
		throw new IOException("no expert found for type " + type);
	}
	
	@Override
	public void writeObject(Object object) throws IOException {
		objectExpert.writeValue(object);
	}
	
	public void writeNull() throws IOException {
		writeByte(CODE_NULL);
	}
	
	public void writeString(String string) throws IOException {
		byte buf[] = string.getBytes("UTF-8");
		writeInt(buf.length);
		write(buf);
	}
	
	public void writeGmEntity(GenericEntity entity) throws IOException {
		entityExpert.writeNullSafe(entity);
	}

	protected void writeGmList(List<?> list) throws IOException {
		OutputExpert<Object> expert = getOutputExpert(typeReflection.getCollectionType(List.class, new GenericModelType[]{typeReflection.getBaseType()})).cast();
		expert.writeNullSafe(list);
	}
	
	protected void writeGmSet(Set<?> set) throws IOException {
		OutputExpert<Object> expert = getOutputExpert(typeReflection.getCollectionType(Set.class, new GenericModelType[]{typeReflection.getBaseType()})).cast();
		expert.writeNullSafe(set);
	}
	
	protected void writeGmMap(Map<?,?> map) throws IOException {
		BaseType baseType = typeReflection.getBaseType();
		OutputExpert<Object> expert = getOutputExpert(typeReflection.getCollectionType(Map.class, new GenericModelType[]{baseType, baseType})).cast();
		expert.writeNullSafe(map);
	}
	
	public void writeGmEnum(Enum<?> enumValue) throws IOException {
		enumExpert.writeNullSafe(enumValue);
	}
	
	public void writeGmBoolean(Boolean booleanValue) throws IOException {
		booleanExpert.writeNullSafe(booleanValue);
	}
	
	public void writeGmInteger(Integer integerValue) throws IOException {
		integerExpert.writeNullSafe(integerValue);
	}
	
	public void writeGmLong(Long longValue) throws IOException {
		longExpert.writeNullSafe(longValue);
	}
	
	public void writeGmFloat(Float floatValue) throws IOException {
		floatExpert.writeNullSafe(floatValue);
	}
	
	public void writeGmDouble(Double doubleValue) throws IOException {
		doubleExpert.writeNullSafe(doubleValue);
	}
	
	public void writeGmDecimal(BigDecimal decimalValue) throws IOException {
		decimalExpert.writeNullSafe(decimalValue);
	}
	
	public void writeGmString(String stringValue) throws IOException {
		stringExpert.writeNullSafe(stringValue);
	}
	
	public void writeGmDate(Date dateValue) throws IOException {
		dateExpert.writeNullSafe(dateValue);
	}
	
	protected abstract class OutputExpert<T> {
		abstract void writeNullSafe(T value) throws IOException;
		abstract void writeValue(T value) throws IOException;
		protected abstract boolean hasTypeSignaturePotential();
		@SuppressWarnings("unchecked")
		public <O extends OutputExpert<?>> O cast() {
			return (O)this;
		}
	}
	
	protected class ObjectExpert extends NullSafeExpert<Object> {
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return true;
		}
		
		@Override
		public void writeValue(Object value) throws IOException {
			OutputExpert<Object> outputExpert = getOutputExpert(value).cast();
			outputExpert.writeValue(value);
		}
	}
	
	protected class NullExpert extends OutputExpert<Void> {
		@Override
		void writeValue(Void value) throws IOException {
			writeNull();
		}
		
		@Override
		void writeNullSafe(Void value) throws IOException {
			writeNull();
		}
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return false;
		}
	}
	
	protected abstract class NullSafeExpert<T> extends OutputExpert<T> {
		@Override
		public void writeNullSafe(T value) throws IOException {
			if (value == null)
				writeNull();
			else
				writeValue(value);
		}
	}
	
	protected abstract class ComplexValueExpert<T> extends NullSafeExpert<T> {
		protected void increaseDepth() {
			//Intentionally left empty			
		}
		
		@Override
		public final void writeValue(T value) throws IOException {
			// write complex header on depth 0
			ByteArrayOutputStream bufferOut = null;
			DataOutputStream originalDataOut = null;
			DataOutputStream bufferDataOut = null;
			
			if (depth == 0 && writeRequiredTypes && hasTypeSignaturePotential()) {
				bufferOut = new ByteArrayOutputStream();
				originalDataOut = dataOut;
				bufferDataOut = new DataOutputStream(bufferOut);
				dataOut = bufferDataOut;
			}
			depth++;
			try {
				writeComplex(value);
			}
			finally {
				if (--depth == 0) {
					// integrate buffer if given
					if (bufferOut != null) {
						dataOut = originalDataOut;
						// write requiredTypes block
						writeRequiredTypes();
						// write buffered data to the actual stream and pop the buffer stream
						bufferDataOut.flush();
						bufferDataOut.close();
						byte data[] = bufferOut.toByteArray();
						dataOut.write(data);
						requiredTypes.clear();
					}
					referenceIds.clear();
					propertyIds.clear();
					typeIds.clear();
					referenceIdSequence = 0;
				}
			}
		}
		
		private void writeRequiredTypes() throws IOException {
			int size = requiredTypes.size();
			if (size > 0) {
				writeByte(CODE_REQUIRED_TYPES);
				writeInt(size);
				for (String requiredType: requiredTypes) {
					writeUTF(requiredType);
				}
			}
		}

		protected abstract void writeComplex(T value) throws IOException;
	}
	
	protected void writePropertyName(Property property) throws IOException {
		Short propertyId = propertyIds.get(property);
		
		if (propertyId == null) {
			if (propertyIdSequence > Short.MAX_VALUE) {
				writeByte(PROPERTY_NAME_PLAIN);
				writeUTF(property.getName());
			}
			else {
				propertyId = propertyIdSequence++;
				propertyIds.put(property, propertyId);
				writeByte(PROPERTY_NAME_DEF);
				writeUTF(property.getName());
			}
		}
		else {
			writeByte(PROPERTY_NAME_REF);
			writeShort(propertyId);
		}
	}
	
	protected void writeTypeSignature(EntityType<?> entityType) throws IOException {
		Byte typeId = typeIds.get(entityType);
		
		if (typeId == null) {
			if (typeIdSequence > Byte.MAX_VALUE) {
				writeByte(TYPE_SIG_PLAIN);
				writeUTF(entityType.getTypeSignature());
			}
			else {
				typeId = typeIdSequence++;
				typeIds.put(entityType, typeId);
				writeByte(TYPE_SIG_DEF);
				writeUTF(entityType.getTypeSignature());
			}
		}
		else {
			writeByte(TYPE_SIG_REF);
			writeByte(typeId);
		}
	}
	
	protected class EntityExpert extends ComplexValueExpert<GenericEntity> {
		@Override
		public void writeComplex(GenericEntity entity) throws IOException {
			writeByte(CODE_ENTITY);
			EntityType<?> entityType = entity.entityType();
			
			Integer refId = referenceIds.get(entity);
			
			if (refId == null) {
				// writing an entity
				refId = referenceIdSequence++;
				referenceIds.put(entity, refId);
				
				// type signature
				if (writeRequiredTypes)
					requiredTypes.add(entityType.getTypeSignature());
				
				writeByte(ENTITY_DEF);
				writeInt(refId);
				writeTypeSignature(entityType);
				
				EnhancedEntity enhancedEntity = entity instanceof EnhancedEntity? (EnhancedEntity)entity: null;
				
				// write properties
				for (Property property: entityType.getProperties()) {
					GenericModelType propertyType = property.getType();
					
					AbsenceInformation ai = null;
					
					if (enhancedEntity != null) {
						ai = property.getAbsenceInformation(enhancedEntity);
					}
					
					byte propertyCode = PROPERTY_DEFINED;
					OutputExpert<Object> outputExpert = null;
					Object outputValue = null;
					
					if (ai != null) {
						propertyCode = PROPERTY_ABSENT;
						outputValue = ai;
						outputExpert = entityExpert.cast();
					}
					else {
						outputValue = property.get(entity);
						
						// skip null values
						if (outputValue == null)
							continue;
						
						outputExpert = getOutputExpert(propertyType).cast();
					}
					
					writeByte(propertyCode);
					writePropertyName(property);
					outputExpert.writeValue(outputValue);
				}
				
				// write property terminator
				writeByte(PROPERTY_TERMINATOR);
			}
			else {
				// writing a reference for a entity serialized already in the current top level write scope
				writeByte(ENTITY_REF);
				writeInt(refId);
			}
		}
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return true;
		}
	}
	
	protected class EnumExpert extends NullSafeExpert<Enum<?>> {
		@Override
		public void writeValue(Enum<?> value) throws IOException {
			writeByte(CODE_ENUM);
			String typeSignature = value.getClass().getName();
			if (writeRequiredTypes)
				requiredTypes.add(typeSignature);
			
			writeUTF(typeSignature);
			writeUTF(value.name());
		}
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return true;
		}
	}
	
	protected abstract class NonCustomScalarExpert<T> extends NullSafeExpert<T> {
		@Override
		protected boolean hasTypeSignaturePotential() {
			return false;
		}
	}
	
	protected class StringExpert extends NonCustomScalarExpert<String> {
		@Override
		public void writeValue(String value) throws IOException {
			writeByte(CODE_CLOB);
			writeString(value);
		}
	}
	
	protected class BooleanExpert extends NonCustomScalarExpert<Boolean> {
		@Override
		public void writeValue(Boolean value) throws IOException {
			writeByte(CODE_BOOLEAN);
			writeBoolean(value);
		}
	}
	
	protected class IntegerExpert extends NonCustomScalarExpert<Integer> {
		@Override
		public void writeValue(Integer value) throws IOException {
			writeByte(CODE_INTEGER);
			writeInt(value);
		}
	}
	
	protected class LongExpert extends NonCustomScalarExpert<Long> {
		@Override
		public void writeValue(Long value) throws IOException {
			writeByte(CODE_LONG);
			writeLong(value);
		}
	}
	
	protected class FloatExpert extends NonCustomScalarExpert<Float> {
		@Override
		public void writeValue(Float value) throws IOException {
			writeByte(CODE_FLOAT);
			writeFloat(value);
		}
	}
	
	protected class DoubleExpert extends NonCustomScalarExpert<Double> {
		@Override
		public void writeValue(Double value) throws IOException {
			writeByte(CODE_DOUBLE);
			writeDouble(value);
		}
	}
	
	protected class DecimalExpert extends NonCustomScalarExpert<BigDecimal> {
		@Override
		public void writeValue(BigDecimal value) throws IOException {
			writeByte(CODE_DECIMAL);
			writeUTF(value.toString());
		}
	}
	
	protected class DateExpert extends NonCustomScalarExpert<Date> {
		@Override
		public void writeValue(Date value) throws IOException {
			writeByte(CODE_DATE);
			long time = value.getTime(); 
			writeLong(time);
		}
	}
	
	protected abstract class CollectionExpert<E> extends ComplexValueExpert<Collection<E>> {
		private OutputExpert<E> elementExpert;
		private byte code;
		
		public CollectionExpert(byte code, OutputExpert<E> elementExpert) {
			super();
			this.code = code;
			this.elementExpert = elementExpert;
		}
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return elementExpert.hasTypeSignaturePotential();
		}

		@Override
		protected void writeComplex(Collection<E> collection) throws IOException {
			writeByte(code);
			int size = collection.size();
			writeInt(size);
			for (E element: collection) {
				elementExpert.writeNullSafe(element);
			}
		}
	}

	protected class ListExpert<E> extends CollectionExpert<E> {
		public ListExpert(OutputExpert<E> elementExpert) {
			super(CODE_LIST, elementExpert);
		}
	}
	
	protected class SetExpert<E> extends CollectionExpert<E> {

		public SetExpert(OutputExpert<E> elementExpert) {
			super(CODE_SET, elementExpert);
		}
	}
	
	protected class MapExpert<K, V> extends ComplexValueExpert<Map<K,V>> {
		private OutputExpert<K> keyExpert;
		private OutputExpert<V> valueExpert;

		public MapExpert(OutputExpert<K> keyExpert, OutputExpert<V> valueExpert) {
			super();
			this.keyExpert = keyExpert;
			this.valueExpert = valueExpert;
		}

		@Override
		protected void writeComplex(Map<K,V> value) throws IOException {
			writeByte(CODE_MAP);
			int size = value.size();
			writeInt(size);
			for (Map.Entry<K, V> entry: value.entrySet()) {
				keyExpert.writeValue(entry.getKey());
				valueExpert.writeValue(entry.getValue());
			}
		}
		
		@Override
		protected boolean hasTypeSignaturePotential() {
			return keyExpert.hasTypeSignaturePotential() || valueExpert.hasTypeSignaturePotential();
		}
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		dataOut.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		dataOut.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		dataOut.writeShort(v);
		
	}

	@Override
	public void writeChar(int v) throws IOException {
		dataOut.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		dataOut.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		dataOut.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		dataOut.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		dataOut.writeDouble(v);
		
	}

	@Override
	public void writeBytes(String s) throws IOException {
		dataOut.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		dataOut.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		dataOut.writeUTF(s);
	}

	@Override
	public void write(int b) throws IOException {
		dataOut.write(b);
	}
	
	@Override
	public void close() throws IOException {
		dataOut.close();
	}
	
	@Override
	public void flush() throws IOException {
		dataOut.flush();
	}
}
