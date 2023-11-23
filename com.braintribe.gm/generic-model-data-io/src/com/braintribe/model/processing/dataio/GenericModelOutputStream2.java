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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.IOTools;

/**
 * This outputstream serializes GenericModel assemblies to a binary format.
 * @author dirk.scheffler
 *
 */
public class GenericModelOutputStream2 extends OutputStream implements ObjectOutput, SerializationCodes {
	
	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private Map<GenericEntity, Integer> referenceIds = new HashMap<GenericEntity, Integer>();
	private Map<EntityType<?>, TypeRegistration> typeRegistrations = new HashMap<>();
	private Map<Property, Short> propertyIds = new HashMap<Property, Short>();
	private int referenceIdSequence = 0;
	private int propertyIdSequence;
	private short typeIdSequence;
	
	private Set<String> requiredTypes = null;
	
	private DataOutputStream dataOut;
	private boolean writeRequiredTypes;
	
	public GenericModelOutputStream2(OutputStream out) {
		this(out, false);
	}
	
	public GenericModelOutputStream2(OutputStream out, boolean writeRequiredTypes) {
		this.dataOut = new DataOutputStream(out);
		this.writeRequiredTypes = writeRequiredTypes;
		if (writeRequiredTypes)
			requiredTypes = new HashSet<String>();
	}
	
	@Override
	public void writeObject(Object object) throws IOException {
		writeRootValue(typeReflection.getBaseType(), object);
	}
	
	public void writeNull() throws IOException {
		dataOut.writeByte(CODE_NULL);
	}
	
	public void writeString(String string) throws IOException {
		
		/*
		byte buf[] = string.getBytes("UTF-8");
		dataOut.writeInt(buf.length);
		dataOut.write(buf);
		*/
		
		int len = string.length();
		byte bytearr[] = new byte[len*3];
		int count = 0;
		int i = 0;
		char c;
		char chars[] = string.toCharArray();
		for (; i < len; i++) {
			c = chars[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte) c;

			} else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			} else {
				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		dataOut.writeInt(count);
		dataOut.write(bytearr, 0, count);

	}
	
	
	public void writeUTF8ShortAndFast(String identifier) throws IOException {
		int len = identifier.length();
		byte bytearr[] = new byte[len*3];
		int count = 0;
		int i = 0;
		char c;
		char chars[] = identifier.toCharArray();
		for (; i < len; i++) {
			c = chars[i];
			if ((c >= 0x0001) && (c <= 0x007F)) {
				bytearr[count++] = (byte) c;

			} else if (c > 0x07FF) {
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			} else {
				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		dataOut.writeShort(count);
		dataOut.write(bytearr, 0, count);
	}
	
	public void writeStringNew(String str) throws IOException {
	        int strlen = str.length();
	        int utflen = 0;
	        int c, count = 0;

	        /* use charAt instead of copying String to char array */
	        for (int i = 0; i < strlen; i++) {
	            c = str.charAt(i);
	            if ((c >= 0x0001) && (c <= 0x007F)) {
	                utflen++;
	            } else if (c > 0x07FF) {
	                utflen += 3;
	            } else {
	                utflen += 2;
	            }
	        }
	        
	        writeInt(utflen);

	        byte[] bytearr = new byte[utflen];

	        int i=0;
	        for (i=0; i<strlen; i++) {
	           c = str.charAt(i);
	           if (!((c >= 0x0001) && (c <= 0x007F))) break;
	           bytearr[count++] = (byte) c;
	        }

	        for (;i < strlen; i++){
	            c = str.charAt(i);
	            if ((c >= 0x0001) && (c <= 0x007F)) {
	                bytearr[count++] = (byte) c;

	            } else if (c > 0x07FF) {
	                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
	                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
	                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	            } else {
	                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
	                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
	            }
	        }
	        write(bytearr, 0, utflen);
	}
	
	public void writeGmEntity(GenericEntity entity) throws IOException {
		writeRootValue(entity.entityType(), entity);
	}

	public void writeGmEnum(Enum<?> enumValue) throws IOException {
		writeValue(typeReflection.getType(enumValue), enumValue);
	}
	
	public void writeGmBoolean(Boolean booleanValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_BOOLEAN, booleanValue);
	}
	
	public void writeGmInteger(Integer integerValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_INTEGER, integerValue);
	}
	
	public void writeGmLong(Long longValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_LONG, longValue);
	}
	
	public void writeGmFloat(Float floatValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_FLOAT, floatValue);
	}
	
	public void writeGmDouble(Double doubleValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_DOUBLE, doubleValue);
	}
	
	public void writeGmDecimal(BigDecimal decimalValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_DECIMAL, decimalValue);
	}
	
	public void writeGmString(String stringValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_STRING, stringValue);
	}
	
	public void writeGmDate(Date dateValue) throws IOException {
		writeValue(GenericModelTypeReflection.TYPE_DATE, dateValue);
	}
	
	private void writeRequiredTypes() throws IOException {
		int size = requiredTypes.size();
		if (size > 0) {
			dataOut.writeByte(CODE_REQUIRED_TYPES);
			dataOut.writeInt(size);
			for (String requiredType: requiredTypes) {
				//dataOut.writeUTF(requiredType);
				writeUTF8ShortAndFast(requiredType);
			}
		}
	}
	
	public void writeRootValue(GenericModelType type, Object value) throws IOException {
		if (writeRequiredTypes && canContainCustomTypes(type, value)) {
			ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
			
			try {
				// create bypass output stream to keep the sequence that required types come first
				DataOutputStream originalDataOut = dataOut;
				DataOutputStream bufferDataOut = new DataOutputStream(bufferOut);
				dataOut = bufferDataOut;
				
				// write the actual value into the bypass
				writeValue(type, value);
				
				dataOut = originalDataOut;
				
				// now that they are collected write requiredTypes block 
				writeRequiredTypes();
				
				// write buffered data to the actual stream and pop the buffer stream to end the bypass
				bufferDataOut.flush();
				bufferDataOut.close();
				
				bufferOut.writeTo(dataOut);
				
				/* last line was the following style to be blamed:
				byte data[] = bufferOut.toByteArray();
				bufferOut.writeTo(dataOut);
				dataOut.write(data);
				*/
				
				requiredTypes.clear();
				referenceIds.clear();
				propertyIds.clear();
				typeRegistrations.clear();
				referenceIdSequence = 0;
			}
			finally {
				IOTools.closeQuietly(bufferOut);
			}
		}
		else {
			writeValue(type, value);
		}
	}
	
	private boolean canContainCustomTypes(GenericModelType type, Object value) {
		while (true) {
			switch (type.getTypeCode()) {
			case objectType:
				if (value == null) 
					return true;
					
				type = type.getActualType(value);
				break;
	
			case entityType: 
			case enumType: 
				return true;
				
			case listType:
			case mapType:
			case setType:
				for (GenericModelType subType: ((CollectionType)type).getParameterization()) {
					if (canContainCustomTypes(subType, null))
						return true;
				}
				return false;
				
			default:
				return false;
			}
		}
	}

	private void writeValue(GenericModelType type, Object value) throws IOException {
		if (value == null) {
			dataOut.writeByte(CODE_NULL);
			return;
		}
		
		while (true) {
			switch (type.getTypeCode()) {
			case objectType:
				type = type.getActualType(value);
				break;
				
			case booleanType:
				dataOut.writeByte(CODE_BOOLEAN);
				dataOut.writeBoolean((Boolean)value);
				return;
				
			case dateType:
				dataOut.writeByte(CODE_DATE);
				dataOut.writeLong(((Date)value).getTime());
				return;
				
			case decimalType:
				dataOut.writeByte(CODE_DECIMAL);
				//dataOut.writeUTF(value.toString());
				writeUTF8ShortAndFast(value.toString());
				return;
				
			case doubleType:
				dataOut.writeByte(CODE_DOUBLE);
				dataOut.writeDouble((Double)value);
				return;
				
			case floatType:
				dataOut.writeByte(CODE_FLOAT);
				dataOut.writeDouble((Float)value);
				return;
				
			case integerType:
				dataOut.writeByte(CODE_INTEGER);
				dataOut.writeInt((Integer)value);
				return;
				
			case longType:
				dataOut.writeByte(CODE_LONG);
				dataOut.writeLong((Long)value);
				return;
				
			case stringType:
				dataOut.writeByte(CODE_CLOB);
				writeString((String)value);
				return;
			
			// collection types
			case listType:
				List<?> list = (List<?>)value;
				dataOut.writeByte(CODE_LIST);
				dataOut.writeInt(list.size());
				GenericModelType listElementType = ((CollectionType)type).getCollectionElementType();
				for (Object element: list) 
					writeValue(listElementType, element);
				return;
				
			case setType:
				Set<?> set = (Set<?>)value;
				dataOut.writeByte(CODE_SET);
				dataOut.writeInt(set.size());
				GenericModelType setElementType = ((CollectionType)type).getCollectionElementType();
				for (Object element: set) 
					writeValue(setElementType, element);
				return;
				
			case mapType:
				Map<?, ?> map = (Map<?, ?>)value;
				dataOut.writeByte(CODE_MAP);
				dataOut.writeInt(map.size());
				GenericModelType[] parameterization = ((CollectionType)type).getParameterization();
				GenericModelType keyType = parameterization[0];
				GenericModelType valueType = parameterization[1];
				for (Map.Entry<?, ?> entry: map.entrySet()) {
					writeValue(keyType, entry.getKey());
					writeValue(valueType, entry.getValue());
				}
				return;
				
			// custom types
			case entityType:
				writeEntity((GenericEntity)value);
				return;
			case enumType:
				dataOut.writeByte(CODE_ENUM);
				String typeSignature = value.getClass().getName();
				if (writeRequiredTypes)
					requiredTypes.add(typeSignature);
				
				/*dataOut.writeUTF(typeSignature);
				dataOut.writeUTF(value.toString());*/
				writeUTF8ShortAndFast(typeSignature);
				writeUTF8ShortAndFast(value.toString());
				return;
			default:
				throw new IOException("unkown type " + type);
			}
		}
	}
	
	private void writeEntity(GenericEntity entity) throws IOException {
		writeByte(CODE_ENTITY);
		
		Integer refId = referenceIds.get(entity);
		
		if (refId == null) {
			EntityType<?> entityType = entity.entityType();
			// writing an entity
			refId = referenceIdSequence++;
			referenceIds.put(entity, refId);
			
			// type signature
			if (writeRequiredTypes)
				requiredTypes.add(entityType.getTypeSignature());
			
			dataOut.writeByte(ENTITY_DEF);
			dataOut.writeInt(refId);
			Short[] propertyIds = writeTypeSignature(entityType).propertyIds;
			
			// write properties
			List<Property> properties = entityType.getProperties();
			int propertyCount = properties.size();
			for (int i = 0; i < propertyCount; i++) {
				Property property = properties.get(i);
				Object propertyValue = property.get(entity);
				
				if (propertyValue == null) {
					AbsenceInformation ai = property.getAbsenceInformation(entity);
					
					if (ai != null) {
						dataOut.writeByte(PROPERTY_ABSENT);
						writePropertyName(property, propertyIds, i);
						writeEntity(ai);
					}
				}
				else {
					dataOut.writeByte(PROPERTY_DEFINED);
					writePropertyName(property, propertyIds, i);
					writeValue(property.getType(), propertyValue);
				}
			}
			
			// write property terminator
			dataOut.writeByte(PROPERTY_TERMINATOR);
		}
		else {
			// writing a reference for a entity serialized already in the current top level write scope
			dataOut.writeByte(ENTITY_REF);
			dataOut.writeInt(refId);
		}
	}

	private void writePropertyName(Property property, Short[] propertyIds, int index) throws IOException {
		Short propertyId = propertyIds[index];
		
		if (propertyId == null) {
			if (propertyIdSequence > 0xffff) {
				dataOut.writeByte(PROPERTY_NAME_PLAIN);
				//dataOut.writeUTF(property.getPropertyName());
				writeUTF8ShortAndFast(property.getName());
			}
			else {
				propertyId = (short)propertyIdSequence++;
				propertyIds[index] = propertyId;
				dataOut.writeByte(PROPERTY_NAME_DEF);
				//dataOut.writeUTF(property.getPropertyName());
				writeUTF8ShortAndFast(property.getName());
			}
		}
		else {
			dataOut.writeByte(PROPERTY_NAME_REF);
			dataOut.writeShort(propertyId);
		}
	}

	/*
	private void writeTypeSignature(EntityType<?> entityType) throws IOException {
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
	*/
	
	private TypeRegistration writeTypeSignature(EntityType<?> entityType) throws IOException {
		TypeRegistration typeRegistration = typeRegistrations.get(entityType);
		
		if (typeRegistration == null) {
			typeRegistration = new TypeRegistration();
			typeRegistration.propertyIds = new Short[entityType.getProperties().size()];
			typeRegistrations.put(entityType, typeRegistration);
			
			if (typeIdSequence < 0x100) {
				typeRegistration.typeId = (byte)typeIdSequence++;
				dataOut.writeByte(TYPE_SIG_DEF);
				//dataOut.writeUTF(entityType.getTypeSignature());
				writeUTF8ShortAndFast(entityType.getTypeSignature());
				return typeRegistration;
			}
		}
		
		Byte typeId = typeRegistration.typeId;
		if (typeId != null) {
			dataOut.writeByte(TYPE_SIG_REF);
			dataOut.writeByte(typeId);
		}
		else {
			dataOut.writeByte(TYPE_SIG_PLAIN);
			//dataOut.writeUTF(entityType.getTypeSignature());
			writeUTF8ShortAndFast(entityType.getTypeSignature());
		}
		
		return typeRegistration;
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
	
	private static class TypeRegistration {
		public Byte typeId;
		public Short propertyIds[];
	}
}
