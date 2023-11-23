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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;

public class GmInputStream extends DataInputStream implements ObjectInput, GmSerializationCodes {
	private static final int STRING_MAX_BUFFER_SIZE = 255;
	private static final int CONSTANTS_MAX_INITIAL_CAPACITY = 20;
	private static final int INSTANCES_MAX_INITIAL_CAPACITY = 20000;
	private static final int ENTITY_TYPES_MAX_INITIAL_CAPACITY = 1000;
	private static final int ENUM_TYPES_MAX_INITIAL_CAPACITY = 100;

	private final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final boolean enhanced = true;
	private boolean lenient = false;

	private Consumer<Set<String>> requiredTypesReceiver;
	private static AbsenceInformation absenceInformation = GMF.absenceInformation();

	private SafeArray<EnumTypePreparation> enumTypePreparations;
	private SafeArray<EntityTypePreparation> entityTypePreparations;
	private GmSession session;

	private final List<String> unknownTypeSignatures = new ArrayList<>();
	private Consumer<? super GenericEntity> entityVisitor;

	private final static Object missingInstance = new Object();
	private static final int PROPERTIES_MAX_INITIAL_CAPACITY = 100;

	public GmInputStream(InputStream in, GmDeserializationOptions options) {
		this(in);
		this.requiredTypesReceiver = options.getRequiredTypesReceiver();
		this.session = options.getSession();
		this.entityVisitor = options.findAttribute(EntityVisitorOption.class).orElse(null);
		DecodingLenience decodingLenience = options.getDecodingLenience();
		if (decodingLenience != null) {
			lenient = decodingLenience.isLenient();
		}
	}

	public GmInputStream(InputStream in, Consumer<Set<String>> requiredTypesReceiver) {
		this(in);
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	public GmInputStream(InputStream in) {
		super(in);
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		return readGmData();
	}

	public String readString() throws IOException {
		int len = readInt();
		byte[] buf = new byte[len];
		readFully(buf, 0, len);
		return new String(buf, StandardCharsets.UTF_8);
	}

	private <T> T readGmData(T returnOnEndOfStream) throws IOException {
		int version = read();

		if (version == -1)
			return returnOnEndOfStream;

		readTypes();
		Object rootValue = readRootValue();
		readPool();

		if (!lenient && !unknownTypeSignatures.isEmpty()) {
			throw new IOException("The following type signatures are unknown: " + unknownTypeSignatures);
		}

		@SuppressWarnings("unchecked")
		T returnValue = (T) rootValue;
		return returnValue;
	}

	private <T> T readGmData() throws IOException {
		return readGmData(null);
	}

	private static class EntityTypePreparation {
		public EntityType<?> entityType;
		public SafeArray<Property> properties;
		public SafeArray<String> propertyNames;
		public SafeArray<GenericEntity> instances;
		public String typeSignature;
		public int instanceCount;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(typeSignature);
			if (entityType != null) {
				sb.append(" (entity type: yes)");
			} else {
				sb.append(" (entity type: null)");
			}
			if (propertyNames != null) {
				sb.append(" (properties: ");
				sb.append(propertyNames.size());
				sb.append(')');
			}
			if (instances != null) {
				sb.append(" (instances: ");
				sb.append(instances.size());
				sb.append(')');
			}
			return sb.toString();
		}
	}

	private static class EnumTypePreparation {
		public SafeArray<Enum<?>> constants;
		public SafeArray<String> constantNames;
		public String typeSignature;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(typeSignature);
			if (constantNames != null) {
				sb.append(" (constantNames: ");
				sb.append(constantNames.size());
				sb.append(')');
			}
			return sb.toString();
		}
	}

	private void readTypes() throws IOException {
		Set<String> typeSignatures = new HashSet<>();

		// entity types
		int entityTypeCount = readInt();

		entityTypePreparations = new SafeArray<>(Math.min(entityTypeCount, ENTITY_TYPES_MAX_INITIAL_CAPACITY));

		for (int i = 0; i < entityTypeCount; i++) {
			short entityTypeIndex = readShort();
			String typeSignature = readUTF();

			typeSignatures.add(typeSignature);

			EntityTypePreparation preparation = new EntityTypePreparation();

			int instanceCount = readInt();
			preparation.instanceCount = instanceCount;
			preparation.instances = new SafeArray<>(Math.min(instanceCount, INSTANCES_MAX_INITIAL_CAPACITY));
			preparation.typeSignature = typeSignature;

			short propertyCount = readShort();

			int capacity = Math.min(propertyCount, PROPERTIES_MAX_INITIAL_CAPACITY);
			SafeArray<Property> properties = new SafeArray<>(capacity);
			SafeArray<String> propertyNames = new SafeArray<>(capacity);

			preparation.properties = properties;
			preparation.propertyNames = propertyNames;

			for (int c = 0; c < propertyCount; c++) {
				short index = readShort();
				String propertyName = readUTF();
				propertyNames.put(index, propertyName);
			}

			entityTypePreparations.put(entityTypeIndex, preparation);
		}

		// enum types
		int enumTypeCount = readInt();

		enumTypePreparations = new SafeArray<>(Math.min(enumTypeCount, ENUM_TYPES_MAX_INITIAL_CAPACITY));

		for (int i = 0; i < enumTypeCount; i++) {
			short enumTypeIndex = readShort();
			String typeSignature = readUTF();
			typeSignatures.add(typeSignature);

			EnumTypePreparation enumTypePreparation = new EnumTypePreparation();
			enumTypePreparation.typeSignature = typeSignature;

			short constantCount = readShort();

			int capacity = Math.min(constantCount, CONSTANTS_MAX_INITIAL_CAPACITY);
			enumTypePreparation.constants = new SafeArray<>(capacity);
			SafeArray<String> constantNames = new SafeArray<>(capacity);
			enumTypePreparation.constantNames = constantNames;

			for (int c = 0; c < constantCount; c++) {
				short index = readShort();
				constantNames.put(index, readUTF());
			}

			enumTypePreparations.put(enumTypeIndex, enumTypePreparation);
		}

		// notification of required types
		try {
			if (requiredTypesReceiver != null)
				requiredTypesReceiver.accept(typeSignatures);
		} catch (Exception e) {
			throw new IOException("error while propagating required types to the configured receiver", e);
		}

		// actually preparing entity types with lenient support if required
		for (EntityTypePreparation entityTypePreparation : entityTypePreparations) {
			EntityType<?> entityType = typeReflection.findType(entityTypePreparation.typeSignature);

			if (entityType != null) {
				entityTypePreparation.entityType = entityType;

				SafeArray<GenericEntity> instances = entityTypePreparation.instances;
				int len = entityTypePreparation.instanceCount;

				if (session != null) {
					for (int i = 0; i < len; i++)
						instances.put(i, session.createRaw(entityType));
				} else if (enhanced) {
					for (int i = 0; i < len; i++)
						instances.put(i, entityType.createRaw());
				} else {
					for (int i = 0; i < len; i++)
						instances.put(i, entityType.createPlainRaw());
				}

				int i = 0;
				SafeArray<Property> properties = entityTypePreparation.properties;
				for (String propertyName : entityTypePreparation.propertyNames) {
					Property prop = entityType.findProperty(propertyName);
					if (prop == null) {
						unknownTypeSignatures.add(entityTypePreparation.typeSignature.concat(".").concat(propertyName));
					}
					properties.put(i++, prop);
				}
			} else {
				unknownTypeSignatures.add(entityTypePreparation.typeSignature);
			}
		}

		// actually preparing enum types with lenient support if required
		for (EnumTypePreparation enumTypePrepration : enumTypePreparations) {
			EnumType enumType = typeReflection.findType(enumTypePrepration.typeSignature);

			if (enumType != null) {
				int i = 0;
				SafeArray<Enum<?>> constants = enumTypePrepration.constants;
				for (String constantName : enumTypePrepration.constantNames) {
					try {
						constants.put(i++, enumType.getEnumValue(constantName));
					} catch (Exception e) {
						unknownTypeSignatures.add(enumTypePrepration.typeSignature.concat(".").concat(constantName));
					}
				}
			} else {
				unknownTypeSignatures.add(enumTypePrepration.typeSignature);
			}
		}
	}

	private void readPool() throws IOException {

		while (true) {
			int entityCount = readInt();

			if (entityCount == 0) {
				break;
			}

			for (int i = 0; i < entityCount; i++) {
				short entityTypeIndex = readShort();
				int instanceIndex = readInt();

				EntityTypePreparation entityTypePreparation = entityTypePreparations.get(entityTypeIndex);

				SafeArray<Property> properties = entityTypePreparation.properties;

				GenericEntity entity = entityTypePreparation.instances.get(instanceIndex);

				if (entityVisitor != null)
					entityVisitor.accept(entity);

				while (true) {
					byte codeAndDecoration = readByte();

					byte code = (byte) (codeAndDecoration & 0xf);

					if (codeAndDecoration == (byte) 0xff) {
						break;
					} else {
						Object value = readValue(code);

						int propertyDecorator = (codeAndDecoration & 0xf0) >> 4;

						if (propertyDecorator == 0)
							propertyDecorator = readShort();
						else {
							propertyDecorator--;
						}

						Property property = properties.get(propertyDecorator);

						// System.out.println("IN: read property index "+propertyDecorator+": "+property.getPropertyName()+"
						// ["+property.getPropertyType().getTypeName()+"] = "+value);

						// null value means absence information is following directly
						if (value == null) {
							byte aiCode = readByte();
							AbsenceInformation absenceInformation = (AbsenceInformation) readValue(aiCode);
							if (absenceInformation == null)
								absenceInformation = GmInputStream.absenceInformation;
							if (property != null) {
								property.setAbsenceInformation(entity, absenceInformation);
							}
						} else {
							if (property != null && value != missingInstance) {
								property.setDirectUnsafe(entity, value);
							}
						}
					}
				}
			}
		}
	}

	private Object readRootValue() throws IOException {
		Object readValue = readValue(readByte());
		if (readValue == missingInstance) {
			return null;
		}
		return readValue;
	}

	private Object readValue(byte code) throws IOException {
		switch (code) {
			case CODE_NULL:
				return null;

			case CODE_FALSE:
				return false;
			case CODE_TRUE:
				return true;

			case CODE_INTEGER:
				return readInt();
			case CODE_LONG:
				return readLong();

			case CODE_FLOAT:
				return readFloat();
			case CODE_DOUBLE:
				return readDouble();
			case CODE_DECIMAL:
				return new BigDecimal(readUTF());

			case CODE_STRING:
				return readString();
			case CODE_DATE:
				return new Date(readLong());

			case CODE_REF: {
				short entityTypeIndex = readShort();
				int instanceIndex = readInt();
				GenericEntity genericEntity = entityTypePreparations.get(entityTypeIndex).instances.get(instanceIndex);
				if (genericEntity == null) {
					return missingInstance;
				}
				return genericEntity;
			}

			case CODE_ENUM: {
				short enumTypeIndex = readShort();
				short constantIndex = readShort();
				Enum<?> enum1 = enumTypePreparations.get(enumTypeIndex).constants.get(constantIndex);
				if (enum1 == null) {
					return missingInstance;
				}
				return enum1;
			}

			case CODE_LIST: {
				return fillCollection(size -> new ArrayList<>(size));
			}

			case CODE_SET: {
				return fillCollection(size -> new HashSet<>(size));
			}

			case CODE_MAP: {
				return fillMap();
			}

			default:
				throw new IOException("unsupported type code " + code);
		}
	}

	private Collection<Object> fillCollection(Function<Integer, Collection<Object>> collection) throws IOException {
		int size = readInt();
		Collection<Object> coll = collection.apply(size);
		for (int i = 0; i < size; i++) {
			Object value = readValue(readByte());
			if (value != missingInstance) {
				coll.add(value);
			}
		}
		return coll;
	}
	private Map<Object, Object> fillMap() throws IOException {
		int size = readInt();
		Map<Object, Object> map = new HashMap<>();
		for (int i = 0; i < size; i++) {
			Object key = readValue(readByte());
			if (key == missingInstance) {
				key = null;
			}
			Object value = readValue(readByte());
			if (value == missingInstance) {
				value = null;
			}
			if (key != null) {
				map.put(key, value);
			}
		}
		return map;
	}
}
