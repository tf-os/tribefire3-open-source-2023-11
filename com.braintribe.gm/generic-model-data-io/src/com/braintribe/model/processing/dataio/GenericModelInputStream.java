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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.PlainList;
import com.braintribe.model.generic.collection.PlainMap;
import com.braintribe.model.generic.collection.PlainSet;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.session.GmSession;

public class GenericModelInputStream extends DataInputStream implements ObjectInput, SerializationCodes {
	private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private NullExpert nullExpert = new NullExpert();
	private ObjectExpert objectExpert = new ObjectExpert();

	private BooleanExpert booleanExpert = new BooleanExpert();
	private IntegerExpert integerExpert = new IntegerExpert();
	private LongExpert longExpert = new LongExpert();
	private FloatExpert floatExpert = new FloatExpert();
	private DoubleExpert doubleExpert = new DoubleExpert();
	private DecimalExpert decimalExpert = new DecimalExpert();
	private DateExpert dateExpert = new DateExpert();
	private StringExpert stringExpert = new StringExpert();
	private ClobExpert clobExpert = new ClobExpert();

	private EntityExpert entityExpert = new EntityExpert();
	private EnumExpert enumExpert = new EnumExpert();

	private ListExpert<Object> defaultListExpert;
	private MapExpert<Object, Object> defaultMapExpert;
	private SetExpert<Object> defaultSetExpert;

	private Map<CollectionType, InputExpert<?>> collectionExperts = new HashMap<CollectionType, InputExpert<?>>();

	private boolean enhanced = true;
	private boolean lenient = true;

	private int depth;

	private Map<Integer, GenericEntity> references = new HashMap<Integer, GenericEntity>();
	private List<Property> properties = new ArrayList<Property>();
	private List<EntityType<?>> types = new ArrayList<EntityType<?>>(128);
	private Consumer<Set<String>> requiredTypesReceiver;
	private boolean absentifyMissingProperties;
	private AbsenceInformation absenceInformation = GMF.absenceInformation();

	private GmSession session;

	public GenericModelInputStream(InputStream in, GmDeserializationOptions options) {
		this(in);
		this.session = options.getSession();
		this.requiredTypesReceiver = options.getRequiredTypesReceiver();
		this.absentifyMissingProperties = options.getAbsentifyMissingProperties();
	}

	public GenericModelInputStream(InputStream in, Consumer<Set<String>> requiredTypesReceiver, GmSession session) {
		this(in, requiredTypesReceiver);
		this.session = session;
	}

	public GenericModelInputStream(InputStream in, Consumer<Set<String>> requiredTypesReceiver, boolean absentifyMissingProperties,
			GmSession session) {
		this(in, requiredTypesReceiver, absentifyMissingProperties);
		this.session = session;
	}

	public GenericModelInputStream(InputStream in, Consumer<Set<String>> requiredTypesReceiver, boolean absentifyMissingProperties) {
		this(in, requiredTypesReceiver);
		this.absentifyMissingProperties = absentifyMissingProperties;
	}

	public GenericModelInputStream(InputStream in, Consumer<Set<String>> requiredTypesReceiver) {
		this(in);
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	public GenericModelInputStream(InputStream in) {
		super(in);

		BaseType baseType = typeReflection.getBaseType();

		CollectionType defaultListType = typeReflection.getCollectionType(List.class, new GenericModelType[] { baseType });
		CollectionType defaultSetType = typeReflection.getCollectionType(Set.class, new GenericModelType[] { baseType });
		CollectionType defaultMapType = typeReflection.getCollectionType(Map.class, new GenericModelType[] { baseType, baseType });

		defaultListExpert = new ListExpert<Object>(defaultListType, objectExpert);
		defaultMapExpert = new MapExpert<Object, Object>(defaultMapType, objectExpert, objectExpert);
		defaultSetExpert = new SetExpert<Object>(defaultSetType, objectExpert);
	}

	public AbsenceInformation getAbsenceInformation() {
		return absenceInformation;
	}

	protected InputExpert<?> getInputExpert(GenericModelType type) throws IOException {
		TypeCode typeCode = type.getTypeCode();

		switch (typeCode) {
			case objectType:
				return objectExpert;
			case entityType:
				return entityExpert;

			case enumType:
				return enumExpert;

			case booleanType:
				return booleanExpert;
			case integerType:
				return integerExpert;
			case longType:
				return longExpert;
			case floatType:
				return floatExpert;
			case doubleType:
				return doubleExpert;

			case stringType:
				return stringExpert;
			case dateType:
				return dateExpert;
			case decimalType:
				return decimalExpert;

			case listType:
				CollectionType listType = (CollectionType) type;
				InputExpert<?> listExpert = collectionExperts.get(listType);
				if (listExpert == null) {
					InputExpert<Object> elementExpert = getInputExpert(listType.getCollectionElementType()).cast();
					listExpert = new ListExpert<Object>(listType, elementExpert);
					collectionExperts.put(listType, listExpert);
				}

				return listExpert;

			case setType:
				CollectionType setType = (CollectionType) type;
				InputExpert<?> setExpert = collectionExperts.get(setType);
				if (setExpert == null) {
					InputExpert<Object> elementExpert = getInputExpert(setType.getCollectionElementType()).cast();
					setExpert = new SetExpert<Object>(setType, elementExpert);
					collectionExperts.put(setType, setExpert);
				}

				return setExpert;

			case mapType:
				CollectionType mapType = (CollectionType) type;
				InputExpert<?> mapExpert = collectionExperts.get(mapType);
				if (mapExpert == null) {
					InputExpert<Object> keyExpert = getInputExpert(mapType.getParameterization()[0]).cast();
					InputExpert<Object> valueExpert = getInputExpert(mapType.getParameterization()[1]).cast();
					mapExpert = new MapExpert<Object, Object>(mapType, keyExpert, valueExpert);
					collectionExperts.put(mapType, mapExpert);
				}

				return mapExpert;
		}

		throw new IOException("no expert found for type " + type);
	}

	protected InputExpert<?> getInputExpert(byte code) throws IOException {
		switch (code) {
			case CODE_NULL:
				return nullExpert;
			case CODE_BOOLEAN:
				return booleanExpert;
			case CODE_INTEGER:
				return integerExpert;
			case CODE_LONG:
				return longExpert;
			case CODE_FLOAT:
				return floatExpert;
			case CODE_DOUBLE:
				return doubleExpert;
			case CODE_DECIMAL:
				return decimalExpert;
			case CODE_DATE:
				return dateExpert;
			case CODE_STRING:
				return stringExpert;
			case CODE_ENTITY:
				return entityExpert;
			case CODE_ENUM:
				return enumExpert;
			case CODE_LIST:
				return defaultListExpert;
			case CODE_SET:
				return defaultSetExpert;
			case CODE_MAP:
				return defaultMapExpert;
			case CODE_CLOB:
				return clobExpert;
			default:
				throw new IOException("unsupported value code " + code);
		}
	}

	@Override
	public Object readObject() throws ClassNotFoundException, IOException {
		return objectExpert.readValue();
	}

	public GenericEntity readGmEntity() throws IOException {
		return entityExpert.readValue();
	}

	public GenericEntity readGmEntity(GenericEntity returnOnEndOfStream) throws IOException {
		return entityExpert.readValue(returnOnEndOfStream);
	}

	public Enum<?> readGmEnum() throws IOException {
		return enumExpert.readValue();
	}

	public Integer readGmInteger() throws IOException {
		return integerExpert.readValue();
	}

	public Long readGmLong() throws IOException {
		return longExpert.readValue();
	}

	public Float readGmFloat() throws IOException {
		return floatExpert.readValue();
	}

	public Double readGmDouble() throws IOException {
		return doubleExpert.readValue();
	}

	public BigDecimal readGmDecimal() throws IOException {
		return decimalExpert.readValue();
	}

	public Date readGmDate() throws IOException {
		return dateExpert.readValue();
	}

	public String readGmString() throws IOException {
		return stringExpert.readValue();
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> readGmList() throws IOException {
		return (List<E>) defaultListExpert.readValue();
	}

	@SuppressWarnings("unchecked")
	public <E> Set<E> readGmSet() throws IOException {
		return (Set<E>) defaultSetExpert.readValue();
	}

	@SuppressWarnings("unchecked")
	public <K, V> Map<K, V> readGmMap() throws IOException {
		return (Map<K, V>) defaultMapExpert.readValue();
	}

	protected Property readPropertyPayload(EntityType<?> entityType) throws IOException {
		String propertyName = readUTF();
		Property property = entityType.findProperty(propertyName);

		if (property == null && !lenient) {
			throw new IOException("unkown property " + entityType.getTypeSignature() + "." + propertyName);
		}

		return property;
	}

	protected Property readProperty(EntityType<?> entityType) throws IOException {
		byte code = readByte();

		switch (code) {
			case PROPERTY_NAME_PLAIN:
				return readPropertyPayload(entityType);
			case PROPERTY_NAME_DEF: {
				Property property = readPropertyPayload(entityType);
				properties.add(property);
				return property;
			}
			case PROPERTY_NAME_REF: {
				int index = readShort();
				return properties.get(index);
			}
			default:
				throw new IOException("unkown property code " + code);
		}
	}

	protected EntityType<?> readEntityType() throws IOException {
		byte code = readByte();

		switch (code) {
			case TYPE_SIG_PLAIN:
				return typeReflection.getEntityType(readUTF());
			case TYPE_SIG_DEF: {
				EntityType<?> entityType = typeReflection.getEntityType(readUTF());
				types.add(entityType);
				return entityType;
			}
			case TYPE_SIG_REF: {
				int index = readByte();
				return types.get(index);
			}
			default:
				throw new IOException("unkown type signature code " + code);
		}
	}

	protected abstract class InputExpert<T> {

		public abstract T readValue() throws IOException;
		public abstract T readValue(T returnOnEndOfStream) throws IOException;
		public abstract T readPayload() throws IOException;

		@SuppressWarnings("unchecked")
		public <V, I extends InputExpert<V>> I cast() {
			return (I) this;
		}
	}

	protected abstract class AbstractInputExpert<T> extends InputExpert<T> {
		private byte code;

		protected AbstractInputExpert(byte code) {
			super();
			this.code = code;
		}

		@Override
		public T readValue(T returnOnEndOfStream) throws IOException {
			int ch = in.read();

			if (ch < 0) {
				return returnOnEndOfStream;
			}

			byte readCode = (byte) (ch);
			return readValue(readCode);
		}

		@Override
		public T readValue() throws IOException {
			byte readCode = readByte();
			return readValue(readCode);
		}

		protected T readValue(byte readCode) throws IOException {
			if (code == readCode)
				return readPayload();
			else {
				switch (readCode) {
					case CODE_NULL:
						return null;
					case CODE_REQUIRED_TYPES:
						readRequiredTypes();
						return readValue();
					default:
						throw new IOException("Expected code " + code + " but found " + readCode);
				}
			}
		}
	}

	protected class NullExpert extends AbstractInputExpert<Void> {
		public NullExpert() {
			super(CODE_NULL);
		}

		@Override
		public Void readPayload() throws IOException {
			return null;
		}
	}

	protected class ObjectExpert extends InputExpert<Object> {
		@Override
		public Object readValue(Object returnOnEndOfStream) throws IOException {
			int ch = in.read();

			if (ch < 0) {
				return returnOnEndOfStream;
			}

			byte readCode = (byte) (ch);
			return readValue(readCode);
		}

		@Override
		public Object readValue() throws IOException {
			byte readCode = readByte();
			return readValue(readCode);
		}

		protected Object readValue(byte readCode) throws IOException {
			switch (readCode) {
				case CODE_NULL:
					return null;
				case CODE_REQUIRED_TYPES:
					readRequiredTypes();
					return readValue();
				default:
					InputExpert<Object> inputExpert = getInputExpert(readCode).cast();
					return inputExpert.readPayload();
			}
		}

		@Override
		public Object readPayload() throws IOException {
			throw new UnsupportedOperationException();
		}
	}

	protected class BooleanExpert extends AbstractInputExpert<Boolean> {

		public BooleanExpert() {
			super(CODE_BOOLEAN);
		}

		@Override
		public Boolean readPayload() throws IOException {
			return readBoolean();
		}
	}

	protected class IntegerExpert extends AbstractInputExpert<Integer> {
		public IntegerExpert() {
			super(CODE_INTEGER);
		}

		@Override
		public Integer readPayload() throws IOException {
			return readInt();
		}
	}

	protected class LongExpert extends AbstractInputExpert<Long> {
		public LongExpert() {
			super(CODE_LONG);
		}

		@Override
		public Long readPayload() throws IOException {
			return readLong();
		}
	}

	protected class FloatExpert extends AbstractInputExpert<Float> {
		public FloatExpert() {
			super(CODE_FLOAT);
		}

		@Override
		public Float readPayload() throws IOException {
			return readFloat();
		}
	}

	protected class DoubleExpert extends AbstractInputExpert<Double> {
		public DoubleExpert() {
			super(CODE_DOUBLE);
		}

		@Override
		public Double readPayload() throws IOException {
			return readDouble();
		}
	}

	protected class DecimalExpert extends AbstractInputExpert<BigDecimal> {
		public DecimalExpert() {
			super(CODE_DECIMAL);
		}

		@Override
		public BigDecimal readPayload() throws IOException {
			String encodedDecimal = readUTF();
			return new BigDecimal(encodedDecimal);
		}
	}

	protected class StringExpert extends AbstractInputExpert<String> {
		public StringExpert() {
			super(CODE_STRING);
		}
		@Override
		public String readValue(String returnOnEndOfStream) throws IOException {
			int ch = in.read();

			if (ch < 0) {
				return returnOnEndOfStream;
			}

			byte readCode = (byte) (ch);

			if (readCode == CODE_NULL) {
				return null;
			}
			if (readCode == SerializationCodes.CODE_STRING) {
				return readPayload();
			} else if (readCode == SerializationCodes.CODE_CLOB) {
				return readPayloadClob();
			} else {
				throw new IOException("Expected a String/Clob code but found " + readCode);
			}

		}
		@Override
		public String readValue() throws IOException {
			byte readCode = readByte();

			if (readCode == CODE_NULL) {
				return null;
			}
			if (readCode == SerializationCodes.CODE_STRING) {
				return readPayload();
			} else if (readCode == SerializationCodes.CODE_CLOB) {
				return readPayloadClob();
			} else {
				throw new IOException("Expected a String/Clob code but found " + readCode);
			}
		}
		@Override
		public String readPayload() throws IOException {
			return readUTF();
		}
		public String readPayloadClob() throws IOException {
			return readString();
		}
	}

	protected class ClobExpert extends AbstractInputExpert<String> {
		public ClobExpert() {
			super(CODE_CLOB);
		}
		@Override
		public String readPayload() throws IOException {
			return readString();
		}
	}

	protected class DateExpert extends AbstractInputExpert<Date> {
		public DateExpert() {
			super(CODE_DATE);
		}
		@Override
		public Date readPayload() throws IOException {
			long time = readLong();
			return new Date(time);
		}
	}

	protected abstract class ComplexValueExpert<E> extends AbstractInputExpert<E> {
		public ComplexValueExpert(byte code) {
			super(code);
		}

		@Override
		public final E readPayload() throws IOException {
			depth++;
			try {
				return readPayloadComplex();
			} finally {
				if (--depth == 0) {
					references.clear();
					properties.clear();
					types.clear();
					absenceInformation = null;
				}
			}
		}

		protected abstract E readPayloadComplex() throws IOException;
	}

	protected class EntityExpert extends ComplexValueExpert<GenericEntity> {
		public EntityExpert() {
			super(CODE_ENTITY);
		}

		@Override
		protected GenericEntity readPayloadComplex() throws IOException {
			byte code = readByte();

			switch (code) {
				case ENTITY_REF:
					return readReference();
				case ENTITY_DEF:
					return readDefinition();
				default:
					throw new IOException("unknown entity code " + code);
			}
		}

		protected GenericEntity readReference() throws IOException {
			int refId = readInt();
			GenericEntity entity = references.get(refId);

			if (entity == null)
				throw new IOException("invalid refId " + refId);
			else
				return entity;
		}

		protected GenericEntity readDefinition() throws IOException {
			Integer refId = readInt();
			EntityType<?> entityType = readEntityType();

			GenericEntity entity = session != null ? session.createRaw(entityType) : enhanced ? entityType.createRaw() : entityType.createPlainRaw();

			references.put(refId, entity);

			PropertyAbsenceHelper propertyAbsenceHelper = absentifyMissingProperties ? new ActivePropertyAbsenceHelper()
					: InactivePropertyAbsenceHelper.instance;

			propertyLoop: while (true) {
				// read property code
				byte code = readByte();

				switch (code) {
					case PROPERTY_DEFINED: {
						Property property = readProperty(entityType);
						if (property != null) {
							propertyAbsenceHelper.addPresent(property);
							GenericModelType propertyType = property.getType();
							InputExpert<Object> expert = getInputExpert(propertyType).cast();
							Object value = null;
							try {
								value = expert.readValue();
							} catch (Exception e) {
								throw new IOException("Could not read value for property " + property.getName(), e);
							}
							property.setDirectUnsafe(entity, value);
						} else {
							objectExpert.readValue();
						}
						break;
					}
					case PROPERTY_ABSENT: {
						Property property = readProperty(entityType);
						if (property != null) {
							propertyAbsenceHelper.addPresent(property);
							AbsenceInformation ai = (AbsenceInformation) entityExpert.readValue();
							if (enhanced) {
								property.setAbsenceInformation(entity, ai);
							}
						} else {
							objectExpert.readValue();
						}
						break;
					}
					case PROPERTY_TERMINATOR:
						break propertyLoop;
				}
			}

			propertyAbsenceHelper.ensureAbsenceInformation(entityType, entity);

			return entity;
		}
	}

	protected class EnumExpert extends AbstractInputExpert<Enum<?>> {
		public EnumExpert() {
			super(CODE_ENUM);
		}
		@Override
		public Enum<?> readPayload() throws IOException {
			String typeSignature = readUTF();
			String encodedValue = readUTF();
			EnumType enumType = typeReflection.getType(typeSignature);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Enum enumValue = enumType.getInstance(encodedValue);
			return enumValue;
		}

	}

	protected abstract class CollectionExpert<E, C extends Collection<E>> extends ComplexValueExpert<C> {
		private InputExpert<E> elementExpert;
		protected CollectionType collectionType;

		public CollectionExpert(CollectionType collectionType, byte code, InputExpert<E> elementExpert) {
			super(code);
			this.collectionType = collectionType;
			this.elementExpert = elementExpert;
		}

		protected abstract C createCollection(int size);

		@Override
		protected C readPayloadComplex() throws IOException {
			int size = readInt();
			C collection = createCollection(size);

			for (int i = 0; i < size; i++) {
				E element = elementExpert.readValue();
				collection.add(element);
			}
			return collection;
		}
	}

	protected class ListExpert<E> extends CollectionExpert<E, List<E>> {

		public ListExpert(CollectionType collectionType, InputExpert<E> elementExpert) {
			super(collectionType, CODE_LIST, elementExpert);
		}

		@Override
		protected List<E> createCollection(int size) {
			List<E> list = new ArrayList<E>(size);
			list = new PlainList<E>((ListType) collectionType, list);

			return list;
		}
	}

	protected class SetExpert<E> extends CollectionExpert<E, Set<E>> {

		public SetExpert(CollectionType collectionType, InputExpert<E> elementExpert) {
			super(collectionType, CODE_SET, elementExpert);
		}

		@Override
		protected Set<E> createCollection(int size) {
			Set<E> set = new HashSet<E>(size);
			set = new PlainSet<E>((SetType) collectionType, set);

			return set;
		}

	}

	protected class MapExpert<K, V> extends ComplexValueExpert<Map<K, V>> {
		private InputExpert<K> keyExpert;
		private InputExpert<V> valueExpert;
		CollectionType collectionType;

		public MapExpert(CollectionType collectionType, InputExpert<K> keyExpert, InputExpert<V> valueExpert) {
			super(CODE_MAP);
			this.keyExpert = keyExpert;
			this.valueExpert = valueExpert;
			this.collectionType = collectionType;
		}

		@Override
		protected Map<K, V> readPayloadComplex() throws IOException {
			int size = readInt();
			Map<K, V> map = new HashMap<K, V>(size);

			map = new PlainMap<K, V>((MapType) collectionType, map);

			for (int i = 0; i < size; i++) {
				K key = keyExpert.readValue();
				V value = valueExpert.readValue();
				map.put(key, value);
			}

			return map;
		}
	}

	public String readString() throws IOException {
		int len = readInt();
		byte[] buf = new byte[len];
		readFully(buf, 0, len);
		return new String(buf, "UTF-8");
	}

	public void readRequiredTypes() throws IOException {
		Set<String> typeSignatures = new HashSet<String>();
		int size = readInt();
		for (int i = 0; i < size; i++) {
			String typeSignature = readUTF();
			typeSignatures.add(typeSignature);
		}

		try {
			if (requiredTypesReceiver != null)
				requiredTypesReceiver.accept(typeSignatures);
		} catch (Exception e) {
			throw new IOException("error while propagating required types to the configured receiver", e);
		}
	}

	private static abstract class PropertyAbsenceHelper {
		public abstract void addPresent(Property property);
		public abstract void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity);
	}

	private class ActivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		private Set<Property> presentProperties = new HashSet<Property>();

		public ActivePropertyAbsenceHelper() {
			super();
		}

		@Override
		public void addPresent(Property property) {
			presentProperties.add(property);
		}

		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			List<Property> properties = entityType.getProperties();

			if (properties.size() != presentProperties.size()) {
				for (Property property : properties) {
					if (!presentProperties.contains(property)) {
						property.setAbsenceInformation(entity, absenceInformation);
					}
				}
			}

		}
	}

	private static class InactivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		public static InactivePropertyAbsenceHelper instance = new InactivePropertyAbsenceHelper();

		@Override
		public void addPresent(Property property) {
			// Intentionally left empty
		}

		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			// Intentionally left empty
		}
	}

}
