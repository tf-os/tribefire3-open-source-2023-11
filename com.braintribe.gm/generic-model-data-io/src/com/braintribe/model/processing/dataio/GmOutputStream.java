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
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.utils.IOTools;

/**
 * This outputstream serializes GenericModel assemblies to a binary format.
 * @author dirk.scheffler
 *
 */
public class GmOutputStream extends OutputStream implements ObjectOutput, GmSerializationCodes {
	
	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	private final EntityType<?> aiType = typeReflection.getEntityType(AbsenceInformation.class);
	
	private DataOutputStream dataOut;
	private boolean directPropertyAccess; 

	private final Map<Class<? extends Enum<?>>, EnumTypeRegistration> enumTypeRegistrations = new IdentityHashMap<Class<? extends Enum<?>>, EnumTypeRegistration>();
	private final Map<EntityType<?>, EntityTypeRegistration> entityTypeRegistrations = new IdentityHashMap<EntityType<?>, EntityTypeRegistration>();
	private final Map<GenericEntity, EntityRegistration> entityRegistrations = new IdentityHashMap<GenericEntity, EntityRegistration>();
	
	private final EntityRegistration anchorEntityRegistration = new EntityRegistration();
	private EntityRegistration lastEntityRegistration = anchorEntityRegistration;

	private Consumer<? super GenericEntity> entityVisitor;
	
	private static class EntityRegistration {
		public int entityIndex;
		public GenericEntity entity;
		public EntityTypeRegistration entityTypeRegistration;
		public EntityRegistration next;
	}

	private static class EntityTypeRegistration {
		public int instanceCount;
		public short[] propertyIndices;
		private short indexSequence;
		public int entityTypeIndex;
		public EntityType<?> entityType;
		
		public short propertyIndex(int originalPropertyIndex) {
			short value = propertyIndices[originalPropertyIndex];
			
			if (value == 0) {
				value = ++indexSequence;
				propertyIndices[originalPropertyIndex] = value; 
			}
			
			return (short)(value - 1);
		}
	}
	
	private static class EnumTypeRegistration {
		public Class<? extends Enum<?>> clazz;
		public short[] constantIndices;
		private short indexSequence;
		private int enumTypeIndex; 
		
		public short constantIndex(Enum<?> enumConstant) {
			int ordinal = enumConstant.ordinal();
			short value = constantIndices[ordinal];
			
			if (value == 0) {
				value = ++indexSequence;
				constantIndices[ordinal] = value; 
			}
			
			return (short)(value - 1);
		}
	}

	
	
	public GmOutputStream(OutputStream out) {
		this.dataOut = new DataOutputStream(out);
	}
	
	public GmOutputStream(OutputStream out, boolean useDirectPropertyAccess) {
		this.dataOut = new DataOutputStream(out);
		this.directPropertyAccess = useDirectPropertyAccess;
	}
	
	public GmOutputStream(OutputStream out, boolean useDirectPropertyAccess, Consumer<? super GenericEntity> entityVisitor) {
		this.entityVisitor = entityVisitor;
		this.dataOut = new DataOutputStream(out);
		this.directPropertyAccess = useDirectPropertyAccess;
	}

	@Override
	public void writeObject(Object object) throws IOException {
		writeObject(typeReflection.getBaseType(), object);
	}
	
	public void writeNull() throws IOException {
		dataOut.writeByte(CODE_NULL);
	}
	
	public void writeString(String string) throws IOException {
		
		byte buf[] = string.getBytes("UTF-8");
		dataOut.writeInt(buf.length);
		dataOut.write(buf);
	
//      TODO: this should be an optimization but it is not able to correctly encode for example smileys so
//      before reactivating (if at all) find out how UTF-8 is really working
		
//		int len = string.length();
//		byte bytearr[] = new byte[len*3];
//		int count = 0;
//		int i = 0;
//		char c;
//		char chars[] = string.toCharArray();
//		for (; i < len; i++) {
//			c = chars[i];
//			if ((c >= 0x0001) && (c <= 0x007F)) {
//				bytearr[count++] = (byte) c;
//
//			} else if (c > 0x07FF) {
//				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
//				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
//				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
//			} else {
//				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
//				bytearr[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
//			}
//		}
//		dataOut.writeInt(count);
//		dataOut.write(bytearr, 0, count);

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
	
	public void writeObject(GenericModelType type, Object value) throws IOException {
		ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
		
		try {
			// create bypass output stream to keep the sequence that required types come first
			DataOutputStream originalDataOut = dataOut;
			DataOutputStream bufferDataOut = new DataOutputStream(bufferOut);
			dataOut = bufferDataOut;
			
			// write the actual value into the bypass
			writeValue(type, value, (byte)0, false);
			
			// write pool
			writePool();
			
			dataOut = originalDataOut;
			
			// now that they are collected write requiredTypes block
			dataOut.writeByte(2);
			writeTypes();
			
			// write buffered data to the actual stream and pop the buffer stream to end the bypass
			bufferDataOut.flush();
			bufferDataOut.close();
			
			bufferOut.writeTo(dataOut);
			
			entityTypeRegistrations.clear();
			entityRegistrations.clear();
			enumTypeRegistrations.clear();
		}
		finally {
			IOTools.closeQuietly(bufferOut);
		}
	}
	private void writeTypes() throws IOException {
		// write entity types
		dataOut.writeInt(entityTypeRegistrations.size());
		for (EntityTypeRegistration entityTypeRegistration: entityTypeRegistrations.values()) {
			dataOut.writeShort(entityTypeRegistration.entityTypeIndex);
			writeUTF8ShortAndFast(entityTypeRegistration.entityType.getTypeSignature());
			// instance count
			dataOut.writeInt(entityTypeRegistration.instanceCount);
			// property count
			dataOut.writeShort(entityTypeRegistration.indexSequence);
			
			// properties
			List<Property> properties = entityTypeRegistration.entityType.getProperties();
			short[] propertyIndices = entityTypeRegistration.propertyIndices;
			int length = propertyIndices.length;
			for (int i = 0; i < length; i++) {
				int index = propertyIndices[i];
				
				if (index != 0) {
					dataOut.writeShort(--index);
					writeUTF8ShortAndFast(properties.get(i).getName());
				}
			}
		}
		
		// write enum types
		dataOut.writeInt(enumTypeRegistrations.size());
		
		for (EnumTypeRegistration enumTypeRegistration: enumTypeRegistrations.values()) {
			dataOut.writeShort(enumTypeRegistration.enumTypeIndex);
			writeUTF8ShortAndFast(enumTypeRegistration.clazz.getName());

			// constant count
			dataOut.writeShort(enumTypeRegistration.indexSequence);
			
			// constants
			Enum<?>[] constants = enumTypeRegistration.clazz.getEnumConstants();
			short[] constantIndices = enumTypeRegistration.constantIndices;
			int length = constantIndices.length;
			for (int i = 0; i < length; i++) {
				int index = constantIndices[i];
				
				if (index != 0) {
					dataOut.writeShort(--index);
					writeUTF8ShortAndFast(constants[i].name());
				}
			}
		}
	}
	
	private boolean writeValue(GenericModelType type, Object value, byte decorator, boolean skipEmpty) throws IOException {
		try {
			return tryWriteValue(type, value, decorator, skipEmpty);
		} catch (IOException e) {
			String maybeActualType = value == null ? "" : ", actual type: " + value.getClass().getName();
			throw Exceptions.contextualize(e, "Value: " + value + ", assumed type: " + type.getTypeSignature() + maybeActualType);
		}
	}
	
	private boolean tryWriteValue(GenericModelType type, Object value, byte decorator, boolean skipEmpty) throws IOException {
		if (value == null) {
			dataOut.writeByte(decorator | CODE_NULL);
			return true;
		}
		
		if (type.getTypeCode() == TypeCode.objectType) {
			type = type.getActualType(value);
			skipEmpty = false;
		}
		
		switch (type.getTypeCode()) {
			case booleanType:
				dataOut.writeByte(decorator | ((Boolean)value? CODE_TRUE: CODE_FALSE));
				return true;
				
			case dateType:
				dataOut.writeByte(decorator | CODE_DATE);
				dataOut.writeLong(((Date)value).getTime());
				return true;
				
			case decimalType:
				dataOut.writeByte(decorator | CODE_DECIMAL);
				writeUTF8ShortAndFast(value.toString());
				return true;
				
			case doubleType:
				dataOut.writeByte(decorator | CODE_DOUBLE);
				dataOut.writeDouble((Double)value);
				return true;
				
			case floatType:
				dataOut.writeByte(decorator | CODE_FLOAT);
				dataOut.writeFloat((Float)value);
				return true;
				
			case integerType:
				dataOut.writeByte(decorator | CODE_INTEGER);
				dataOut.writeInt((Integer)value);
				return true;
				
			case longType:
				dataOut.writeByte(decorator | CODE_LONG);
				dataOut.writeLong((Long)value);
				return true;
				
			case stringType:
				dataOut.writeByte(decorator | CODE_STRING);
				writeString((String)value);
				return true;
			
			// collection types
			case listType: 
			case setType: {
				Collection<?> collection = (Collection<?>)value;
				if (skipEmpty && collection.isEmpty()) 
					return false;
				byte collectionCode = type.getTypeCode() == TypeCode.listType ? CODE_LIST : CODE_SET;
				dataOut.writeByte(decorator | collectionCode);
				dataOut.writeInt(collection.size());
				GenericModelType elementType = ((CollectionType)type).getCollectionElementType();
				for (Object element: collection) 
					writeValue(elementType, element, (byte)0, false);
				return true;
			}
			case mapType: {
				Map<?, ?> map = (Map<?, ?>)value;
				if (skipEmpty && map.isEmpty()) 
					return false;

				dataOut.writeByte(decorator | CODE_MAP);
				dataOut.writeInt(map.size());
				GenericModelType[] parameterization = ((CollectionType)type).getParameterization();
				GenericModelType keyType = parameterization[0];
				GenericModelType valueType = parameterization[1];
				for (Map.Entry<?, ?> entry: map.entrySet()) {
					writeValue(keyType, entry.getKey(), (byte)0, false);
					writeValue(valueType, entry.getValue(), (byte)0, false);
				}
				return true;
			}
			// custom types
			case entityType:
				dataOut.writeByte(decorator | CODE_REF);
				EntityRegistration entityRegistration = acquireEntityRegistration((GenericEntity)value);
				dataOut.writeShort(entityRegistration.entityTypeRegistration.entityTypeIndex);
				dataOut.writeInt(entityRegistration.entityIndex);
				return true;
			case enumType:
				dataOut.writeByte(decorator | CODE_ENUM);
				EnumTypeRegistration enumTypeRegistration = acquireEnumTypeRegistration(value.getClass());
				dataOut.writeShort(enumTypeRegistration.enumTypeIndex);
				dataOut.writeShort(enumTypeRegistration.constantIndex((Enum<?>)value));
				return true;
			default:
				throw new IOException("unkown type " + type);
		}
	}
	
	private EntityRegistration acquireEntityRegistration(GenericEntity entity) {
		EntityRegistration entityRegistration = entityRegistrations.get(entity);
		
		if (entityRegistration == null) {
			if (entityVisitor != null)
				entityVisitor.accept(entity);
			
			EntityType<?> entityType = typeReflection.getType(entity);
			EntityTypeRegistration entityTypeRegistration = acquireEntityTypeRegistration(entityType);
			entityRegistration = new EntityRegistration();
			entityRegistration.entity = entity;
			entityRegistration.entityIndex = entityTypeRegistration.instanceCount++;
			entityRegistration.entityTypeRegistration = entityTypeRegistration;
			lastEntityRegistration = lastEntityRegistration.next = entityRegistration;
			entityRegistrations.put(entity, entityRegistration);
		}
		
		return entityRegistration;
	}

	private EntityTypeRegistration acquireEntityTypeRegistration(EntityType<?> entityType) {
		EntityTypeRegistration entityTypeRegistration = entityTypeRegistrations.get(entityType);
		
		if (entityTypeRegistration == null) {
			entityTypeRegistration = new EntityTypeRegistration();
			entityTypeRegistration.entityTypeIndex = entityTypeRegistrations.size();
			entityTypeRegistration.propertyIndices = new short[entityType.getProperties().size()];
			entityTypeRegistration.entityType = entityType;
			entityTypeRegistrations.put(entityType, entityTypeRegistration);
		}
		
		return entityTypeRegistration;
	}

	private EnumTypeRegistration acquireEnumTypeRegistration(Class<?> clazz) {
		EnumTypeRegistration enumTypeRegistration = enumTypeRegistrations.get(clazz);
		
		if (enumTypeRegistration == null) {
			enumTypeRegistration = new EnumTypeRegistration();
			enumTypeRegistration.clazz = (Class<? extends Enum<?>>)clazz;
			enumTypeRegistration.enumTypeIndex = enumTypeRegistrations.size();
			enumTypeRegistration.constantIndices = new short[clazz.getEnumConstants().length];
			enumTypeRegistrations.put((Class<? extends Enum<?>>) clazz, enumTypeRegistration);
		}
		
		return enumTypeRegistration;
	}

	
	private boolean isSimpleAbsenceInformation(AbsenceInformation absenceInformation) {
		return typeReflection.getType(absenceInformation) == aiType && absenceInformation.getSize() == null;
	}

	
	private void writePool() throws IOException {

		EntityRegistration entityRegistration = anchorEntityRegistration.next;
		
		int entitiesInCurrentBlockWritten = 0;
		int currentBlockSize = 0;
		int entitiesWrittenInTotal = 0;
		
		while (true) {

			// OK, this is a but tricky, so I'm gonna explain this a bit.
			// In #writeValue, the list of EntityTypeRegistration has been created
			// However, this list may not yet be complete. There are most likely
			// properties somewhere in the depths of the GenericEntity that have not
			// yet been registered. Hence, we have this endless loop to check whether
			// the list has been extended.
			// If there is a new entry in entityRegistrations, we write the currently
			// known size and write those new entries to the output stream.
			// After that, we check again.
			// On the receiver side, the GmInputStream reads entities until the next
			// block size is 0.
			
			if (entitiesInCurrentBlockWritten >= currentBlockSize) {
				entitiesInCurrentBlockWritten = 0;
				currentBlockSize = entityRegistrations.size() - entitiesWrittenInTotal;
				writeInt(currentBlockSize);
			}
			
			if (entityRegistration == null) {
				break;
			}
			
			entitiesInCurrentBlockWritten++;
			entitiesWrittenInTotal++;
	
			
			GenericEntity entity = entityRegistration.entity;
			EntityTypeRegistration entityTypeRegistration = entityRegistration.entityTypeRegistration;
			
			// write typed reference information
			writeShort(entityTypeRegistration.entityTypeIndex);
			writeInt(entityRegistration.entityIndex);
			
			// write properties
			List<Property> properties = entityTypeRegistration.entityType.getProperties();
			int propertyCount = properties.size();
			for (int i = 0; i < propertyCount; i++) {
				Property property = properties.get(i);
				
				Object value = directPropertyAccess? property.getDirectUnsafe(entity): property.get(entity);
				
				if (value == null) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);
					if (absenceInformation != null) {
						int propertyIndex = entityTypeRegistration.propertyIndex(i);

						if (propertyIndex < 15) {
							int adaptedPropertyIndex = propertyIndex + 1;
							byte decorator = (byte) (adaptedPropertyIndex << 4);
							writeByte(decorator);
						}
						else {
							writeByte(0);
							writeShort(propertyIndex);
						}
						
						
						if (isSimpleAbsenceInformation(absenceInformation))
							writeByte(0);
						else
							writeValue(aiType, absenceInformation, (byte)0, false);
					}
				}
				else {
					int propertyIndex = entityTypeRegistration.propertyIndex(i);

					//System.out.println("OUT: written property index "+propertyIndex+": "+property.getPropertyName()+" ["+property.getPropertyType().getTypeName()+"] = "+value);
					
					if (propertyIndex < 15) {
						int adaptedPropertyIndex = propertyIndex + 1;
						byte decorator = (byte) (adaptedPropertyIndex << 4);
						writeValue(property.getType(), value, decorator, true);
					}
					else {
						if (writeValue(property.getType(), value, (byte)0, true)) {
							writeShort(propertyIndex);
						}
					}
					
				}
			}
			
			// write terminator code
			writeByte(0xff);
			
			
			entityRegistration = entityRegistration.next;
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
