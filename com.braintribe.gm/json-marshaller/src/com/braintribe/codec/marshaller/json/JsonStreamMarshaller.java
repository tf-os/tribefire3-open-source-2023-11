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
package com.braintribe.codec.marshaller.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.DateDefaultZoneOption;
import com.braintribe.codec.marshaller.api.DateFormatOption;
import com.braintribe.codec.marshaller.api.DateLocaleOption;
import com.braintribe.codec.marshaller.api.EntityRecurrenceDepth;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmMarshallingOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.IdTypeSupplier;
import com.braintribe.codec.marshaller.api.IdentityManagementMode;
import com.braintribe.codec.marshaller.api.IdentityManagementModeOption;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.PropertyDeserializationTranslation;
import com.braintribe.codec.marshaller.api.PropertySerializationTranslation;
import com.braintribe.codec.marshaller.api.PropertyTypeInferenceOverride;
import com.braintribe.codec.marshaller.api.StringifyNumbersOption;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.collection.LinearCollectionBase;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.EssentialCollectionTypes;
import com.braintribe.model.generic.reflection.EssentialTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.LinearCollectionType;
import com.braintribe.model.generic.reflection.ListType;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SetType;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.utils.DateTools;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonStreamMarshaller implements CharacterMarshaller, HasStringCodec, GmCodec<Object, String> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(JsonStreamMarshaller.class);

	private boolean createEnhancedEntities = true;
	private boolean snakeCaseProperties = false;

	private static class EntityRegistration {
		public List<Consumer<GenericEntity>> consumers;
		public GenericEntity entity;

		public void set(GenericEntity entity) {
			this.entity = entity;

			if (consumers != null) {
				for (Consumer<GenericEntity> consumer : consumers) {
					consumer.accept(entity);
				}
			}
		}

		public void addConsumer(Consumer<GenericEntity> consumer) {
			if (consumers == null)
				consumers = new ArrayList<>();

			consumers.add(consumer);

			if (entity != null)
				consumer.accept(entity);
		}
	}

	@Configurable
	public void setAssignAbsenceInformationForMissingProperties(boolean assignAbsenceInformationForMissingProperties) {
		this.assignAbsenceInformationForMissingProperties = assignAbsenceInformationForMissingProperties;
	}

	@Configurable
	public void setCreateEnhancedEntities(boolean createEnhancedEntities) {
		this.createEnhancedEntities = createEnhancedEntities;
	}

	@Configurable
	public void setSnakeCaseProperties(boolean snakeCaseProperties) {
		this.snakeCaseProperties = snakeCaseProperties;
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.defaultOptions);
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.defaultOptions);
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, value, options);
			writer.flush();
		} catch (MarshallException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshallException("error while marshalling json", e);
		}
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		EncodingContext context = new EncodingContext(options);

		PrettinessSupport prettinessSupport = null;
		switch (options.outputPrettiness()) {
			case high:
				prettinessSupport = new HighPrettinessSupport();
				break;
			case low:
				prettinessSupport = new LowPrettinessSupport();
				break;
			case mid:
				prettinessSupport = new MidPrettinessSupport();
				break;
			case none:
			default:
				prettinessSupport = new NoPrettinessSupport();
				break;
		}

		GenericModelType contextType = BaseType.INSTANCE;
		if (options.getInferredRootType() != null) {
			contextType = options.getInferredRootType();
		}

		try {
			marshall(context, prettinessSupport, writer, contextType, value, 0, false);
		} catch (MarshallException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshallException("error while marshalling json", e);
		}
	}

	private static abstract class ContainerDecoder {
		public abstract ContainerDecoder arrayDecoder() throws Exception;
		public abstract ContainerDecoder objectDecoder() throws Exception;

		public abstract void setField(String name);
		public abstract void consumeInteger(Integer value);
		public abstract void consumeLong(Long value);
		public abstract void consumeFloat(Float value);
		public abstract void consumeDouble(Double value);
		public abstract void consumeDecimal(BigDecimal value);
		public abstract void consumeString(String value);
		public abstract void consumeBoolean(Boolean value);
		public abstract void consumeAssignable(GenericModelType type, Object value);

		public abstract void consumeDirect(GenericModelType type, Object value);
		public abstract void consumeDeferred(EntityRegistration registration);

		public void consumePotentiallyDeferred(Object v) {
			if (v != null) {
				if (v.getClass() == EntityRegistration.class) {
					consumeDeferred((EntityRegistration) v);
				} else {
					consumeAssignable(GMF.getTypeReflection().getType(v), v);
				}
			}
		}

		public abstract void close();

		public abstract GenericModelType getType();
		public abstract Object getValue();

		public abstract EntityRegistration isDeferred();
	}

	private abstract static class ValueDecoder extends ContainerDecoder {
		protected GenericModelType inferredType;
		protected DecodingContext context;
		protected boolean ignoreValue = false;
		protected boolean snakeCaseProperties = false;

		public ValueDecoder(DecodingContext context) {
			super();
			this.context = context;
			this.snakeCaseProperties = context.getSnakeCaseProperties();
		}

		@Override
		public EntityRegistration isDeferred() {
			return null;
		}

		protected boolean ignoreValue() {
			boolean ignoreValueThisTime = ignoreValue;

			ignoreValue = false;

			return ignoreValueThisTime;
		}

		protected String toCamelCase(String value, char delimiter) {
			String pascalCase = WordUtils.capitalizeFully(value, new char[] { delimiter }).replace(Character.toString(delimiter), "");
			return Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);
		}

		@Override
		public ContainerDecoder arrayDecoder() throws Exception {
			if (ignoreValue())
				return new NoopDecoder();

			switch (inferredType.getTypeCode()) {
				case objectType:
					return new TypedCollectionFromArrayDecoder(EssentialCollectionTypes.TYPE_LIST, context);
				case mapType:
					return new TypedMapFromArrayDecoder((MapType) inferredType, context);
				case setType:
					return new TypedCollectionFromArrayDecoder((SetType) inferredType, context);
				case listType:
					return new TypedCollectionFromArrayDecoder((ListType) inferredType, context);
				default:
					throw new IllegalStateException("json array literal is not assignable to: " + inferredType);
			}
		}

		@Override
		public ContainerDecoder objectDecoder() {
			if (ignoreValue())
				return new NoopDecoder();

			switch (inferredType.getTypeCode()) {
				case objectType:
				case mapType:
				case setType:
				case entityType:
				case dateType:
				case longType:
				case floatType:
				case doubleType:
				case decimalType:
				case enumType:
					return new GenericObjectDecoder(context, inferredType);
				default:
					throw new IllegalStateException("json object literal is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeInteger(Integer value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case integerType:
					consumeDirect(EssentialTypes.TYPE_INTEGER, value);
					break;
				case longType:
					consumeDirect(EssentialTypes.TYPE_LONG, value.longValue());
					break;
				case floatType:
					consumeDirect(EssentialTypes.TYPE_FLOAT, value.floatValue());
					break;
				case doubleType:
					consumeDirect(EssentialTypes.TYPE_DOUBLE, value.doubleValue());
					break;
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, BigDecimal.valueOf(value));
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeLong(Long value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case longType:
					consumeDirect(EssentialTypes.TYPE_LONG, value);
					break;
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, BigDecimal.valueOf(value));
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeFloat(Float value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case doubleType:
					consumeDirect(EssentialTypes.TYPE_DOUBLE, value.doubleValue());
					break;
				case floatType:
					consumeDirect(EssentialTypes.TYPE_FLOAT, value);
					break;
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, BigDecimal.valueOf(value));
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeDouble(Double value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case doubleType:
					consumeDirect(EssentialTypes.TYPE_DOUBLE, value);
					break;
				case floatType:
					consumeDirect(EssentialTypes.TYPE_FLOAT, value.floatValue());
					break;
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, BigDecimal.valueOf(value));
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeDecimal(BigDecimal value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, value);
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeBoolean(Boolean value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case booleanType:
					consumeDirect(EssentialTypes.TYPE_BOOLEAN, value);
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		@Override
		public void consumeString(String value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
				case stringType:
					consumeDirect(EssentialTypes.TYPE_STRING, value);
					break;
				case dateType:
					consumeDirect(EssentialTypes.TYPE_DATE, context.getDateCoding().decode(value));
					break;
				case longType:
					consumeDirect(EssentialTypes.TYPE_LONG, Long.parseLong(value));
					break;
				case decimalType:
					consumeDirect(EssentialTypes.TYPE_DECIMAL, new BigDecimal(value));
					break;
				case enumType:
					consumeDirect(inferredType, ((EnumType) inferredType).findEnumValue(value));
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		private void consumeMap(MapType mapType, Map<?, ?> value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
					consumeDirect(mapType, value);
					break;
				case entityType:
					consumeDirect(inferredType, convertToEntity((EntityType<?>) inferredType, (Map<String, ?>) value));
					break;
				case mapType:
					if (mapType == inferredType) {
						consumeDirect(mapType, value);
					} else {
						consumeDirect(inferredType, convertToMap((MapType) inferredType, value));
					}
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		private void consumeList(ListType listType, List<?> value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
					consumeDirect(listType, value);
					break;
				case mapType:
					consumeDirect(inferredType, convertToMap((MapType) inferredType, value));
					break;
				case setType:
				case listType:
					if (listType == inferredType) {
						consumeDirect(listType, value);
					} else {
						consumeDirect(inferredType, convertCollection((LinearCollectionType) inferredType, value));
					}
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		private void consumeSet(SetType setType, Set<?> value) {
			if (ignoreValue())
				return;

			switch (inferredType.getTypeCode()) {
				case objectType:
					consumeDirect(setType, value);
					break;
				case setType:
					if (setType == inferredType) {
						consumeDirect(setType, value);
					} else {
						consumeDirect(inferredType, convertCollection((LinearCollectionType) inferredType, value));
					}
					break;
				default:
					throw new IllegalStateException(value + " is not assignable to: " + inferredType);
			}
		}

		private Object convertCollection(LinearCollectionType targetType, Collection<?> source) {
			ContainerDecoder decoder = new TypedCollectionFromArrayDecoder(targetType, context);

			for (Object o : source) {
				decoder.consumePotentiallyDeferred(o);
			}

			decoder.close();
			return decoder.getValue();
		}

		private Object convertToEntity(EntityType<?> targetType, Map<String, ?> source) {
			ContainerDecoder decoder = new EntityDecoder(targetType, context);

			for (Map.Entry<String, ?> e : source.entrySet()) {
				Object v = e.getValue();
				decoder.setField(e.getKey());
				decoder.consumePotentiallyDeferred(v);
			}

			decoder.close();
			return decoder.getValue();
		}

		private Object convertToMap(MapType targetType, List<?> source) {
			ContainerDecoder decoder = new TypedMapFromArrayDecoder(targetType, context);

			for (Object o : source) {
				decoder.consumePotentiallyDeferred(o);
			}

			decoder.close();
			return decoder.getValue();
		}

		private Object convertToMap(MapType targetType, Map<?, ?> source) {
			ContainerDecoder decoder = new TypedMapFromArrayDecoder(targetType, context);

			for (Map.Entry<?, ?> e : source.entrySet()) {
				decoder.consumePotentiallyDeferred(e.getKey());
				decoder.consumePotentiallyDeferred(e.getValue());
			}

			decoder.close();
			return decoder.getValue();
		}

		@Override
		public void consumeAssignable(GenericModelType type, Object value) {
			if (ignoreValue())
				return;

			if (value == null) {
				consumeDirect(type, value);
				return;
			}

			switch (type.getTypeCode()) {
				case booleanType:
					consumeBoolean((Boolean) value);
					break;
				case decimalType:
					consumeDecimal((BigDecimal) value);
					break;
				case doubleType:
					consumeDouble((Double) value);
					break;
				case floatType:
					consumeFloat((Float) value);
					break;
				case longType:
					consumeLong((Long) value);
					break;
				case integerType:
					consumeInteger((Integer) value);
					break;
				case stringType:
					consumeString((String) value);
					break;
				case mapType:
					consumeMap((MapType) type, (Map<?, ?>) value);
					break;
				case setType:
					consumeSet((SetType) type, (Set<?>) value);
					break;
				case listType:
					consumeList((ListType) type, (List<?>) value);
					break;
				default:
					if (inferredType.isAssignableFrom(type)) {
						consumeDirect(type, value);
					} else {
						throw new IllegalStateException(value + " is not assignable to: " + inferredType);
					}
					break;
			}

		}

		@Override
		public void close() {
			// noop
		}

		@Override
		public void setField(String name) {
			throw new IllegalStateException(getClass() + " does not support field values.");
		}
	}

	private static class NoopDecoder extends ContainerDecoder {

		@Override
		public ContainerDecoder arrayDecoder() throws Exception {
			return this;
		}

		@Override
		public ContainerDecoder objectDecoder() throws Exception {
			return this;
		}

		@Override
		public void setField(String name) {
			// noop
		}

		@Override
		public void consumeInteger(Integer value) {
			// noop
		}

		@Override
		public void consumeLong(Long value) {
			// noop
		}

		@Override
		public void consumeFloat(Float value) {
			// noop
		}

		@Override
		public void consumeDouble(Double value) {
			// noop
		}

		@Override
		public void consumeDecimal(BigDecimal value) {
			// noop
		}

		@Override
		public void consumeString(String value) {
			// noop
		}

		@Override
		public void consumeBoolean(Boolean value) {
			// noop
		}

		@Override
		public void consumeAssignable(GenericModelType type, Object value) {
			// noop
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			// noop
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			// noop
		}

		@Override
		public void close() {
			// noop
		}

		@Override
		public GenericModelType getType() {
			return null;
		}

		@Override
		public Object getValue() {
			return null;
		}

		@Override
		public EntityRegistration isDeferred() {
			return null;
		}

	}

	private static class FieldValue {
		public String name;
		public GenericModelType type;
		public Object value;
		public FieldValue next;

		public FieldValue(String name, FieldValue next) {
			super();
			this.name = name;
			this.next = next;
		}
	}

	private static class StringMapEntryDecoder extends ValueDecoder {
		private final Map<Object, Object> map;
		private String key;
		private MapType mapType;

		public StringMapEntryDecoder(MapType mapType, DecodingContext context) {
			super(context);
			this.map = mapType.createPlain();
			this.inferredType = mapType.getValueType();
		}

		@Override
		public void setField(String name) {
			key = name;
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			map.put(key, value);
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			String deferredKey = key;
			map.put(deferredKey, registration);
			registration.addConsumer(e -> map.put(deferredKey, e));
		}

		@Override
		public GenericModelType getType() {
			return mapType;
		}

		@Override
		public Object getValue() {
			return map;
		}
	}

	private static class EntityDecoder extends ValueDecoder {
		private final EntityType<?> entityType;
		private final GenericEntity entity;
		private boolean idField;
		private String idValue;
		private String globalIdValue;
		private BiConsumer<GenericEntity, Object> valueSetter;
		private Property property;

		public EntityDecoder(EntityType<?> entityType, DecodingContext context) {
			super(context);
			this.entityType = entityType;
			this.entity = context.createRaw(entityType);
		}

		@Override
		public void setField(String name) {
			if ("_id".equals(name)) {
				idField = true;
				valueSetter = null;
				this.inferredType = EssentialTypes.TYPE_STRING;
				if (context.identityManagementMode == IdentityManagementMode.auto) {
					context.identityManagementMode = IdentityManagementMode._id;
				}
			} else if (name.charAt(0) == '?') {

				String realName = name.substring(1);
				BiFunction<EntityType<?>, String, Property> propertySupplier = context.options.findAttribute(PropertyDeserializationTranslation.class)
						.orElse(null);
				property = propertySupplier != null ? propertySupplier.apply(entityType, realName) : entityType.findProperty(realName);
				if (property == null && !context.isPropertyLenient()) {
					throw new NullPointerException(
							"The property " + realName + " (referenced as " + name + ") in the entity type " + entityType + " does not exist.");
				}
				if (property != null) {
					this.inferredType = AbsenceInformation.T;
					valueSetter = (e, v) -> property.setAbsenceInformation(e, (AbsenceInformation) v);
				} else {
					this.inferredType = null;
					this.ignoreValue = true;
				}

			} else {
				if (snakeCaseProperties) {
					name = toCamelCase(name, '_');
				}
				BiFunction<EntityType<?>, String, Property> propertySupplier = context.options.findAttribute(PropertyDeserializationTranslation.class)
						.orElse(null);
				property = propertySupplier != null ? propertySupplier.apply(entityType, name) : entityType.findProperty(name);
				if (property == null && !context.isPropertyLenient()) {
					throw new NullPointerException("The property " + name + " in the entity type " + entityType + " does not exist.");
				}
				if (property != null) {
					this.inferredType = context.getInferredPropertyType(entityType, property);

					GenericModelType propertyType = property.getType();

					if (propertyType != inferredType && propertyType.isCollection()) {
						Function<Object, Object> typeCaster = buildCollectionCaster(inferredType, propertyType);
						Property p = property;
						valueSetter = (e, v) -> p.set(e, typeCaster.apply(v));
					} else {
						valueSetter = property::set;
					}

					if (name.equals(GenericEntity.id)) {
						Function<String, GenericModelType> idTypeSupplier = context.options.findAttribute(IdTypeSupplier.class).orElse(null);
						if (idTypeSupplier != null) {
							GenericModelType idType = idTypeSupplier.apply(entityType.getTypeSignature());
							this.inferredType = idType;
						}
						if (context.identityManagementMode == IdentityManagementMode.auto) {
							context.identityManagementMode = IdentityManagementMode.id;
						}
					}
				} else {
					this.inferredType = null;
					this.ignoreValue = true;
				}
			}
		}

		private Function<Object, Object> buildCollectionCaster(GenericModelType source, GenericModelType target) {
			switch (target.getTypeCode()) {
				case listType:
				case setType:
					return c -> {
						LinearCollectionBase<Object> casted = ((LinearCollectionType) target).createPlain();
						casted.addAll((Collection<?>) c);
						return casted;
					};
				case mapType:
					return m -> {
						Map<Object, Object> casted = ((MapType) target).createPlain();
						casted.putAll((Map<?, ?>) m);
						return casted;
					};
				default:
					throw new IllegalStateException(
							"Cannot assign value of type " + source.getTypeSignature() + " to property with type " + target.getTypeSignature());
			}
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			if (ignoreValue())
				return;

			if (valueSetter != null && value != null) {
				boolean isIdProperty = property.getName().equals(GenericEntity.id);
				boolean isGlobalIdProperty = property.getName().equals(GenericEntity.globalId);

				if (context.identityManagementMode == IdentityManagementMode.id && (isIdProperty || isGlobalIdProperty)) {
					if (isIdProperty) {
						idValue = value.toString();
						EntityRegistration registeredEntity = context.lookupIdentity(entityType, idValue);
						if (registeredEntity.entity != null) {
							valueSetter.accept(registeredEntity.entity, value);
						} else {
							registeredEntity.set(entity);
							valueSetter.accept(entity, value);
						}
					} else if (isGlobalIdProperty) {
						globalIdValue = value.toString();
						EntityRegistration byGlobalId = context.lookupByGlobalId(entityType, globalIdValue);
						if (byGlobalId != null) {
							valueSetter.accept(byGlobalId.entity, value);
						} else {
							EntityRegistration registeredEntity = context.lookupIdentity(entityType, globalIdValue);
							if (registeredEntity.entity != null) {
								valueSetter.accept(registeredEntity.entity, value);
							} else {
								registeredEntity.set(entity);
								valueSetter.accept(entity, value);
							}
						}
					}
				} else {
					valueSetter.accept(entity, value);
				}
			} else if (idField) {
				idField = false;
				context.register(entity, (String) value);
			} else {
				// TODO: handle error or lenience
			}
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			if (valueSetter != null) {
				BiConsumer<GenericEntity, Object> deferredSetter = valueSetter;
				GenericEntity targetEntity = entity;
				registration.addConsumer(e -> deferredSetter.accept(targetEntity, e));
			}
		}

		@Override
		public GenericModelType getType() {
			return entityType;
		}

		@Override
		public Object getValue() {
			if (context.identityManagementMode.equals(IdentityManagementMode.id)) {
				GenericEntity e = context.lookupIdentity(entityType, idValue).entity;
				if (e != null)
					return e;
			}
			return entity;
		}

	}

	private static class EscapeDecoder extends ValueDecoder {
		private Object value;
		private GenericModelType type;
		private boolean isValueField;
		private final String encodingType;

		public EscapeDecoder(GenericModelType type, DecodingContext context, String encodingType) {
			super(context);
			this.inferredType = type;
			this.encodingType = encodingType;
		}

		@Override
		public void setField(String name) {
			isValueField = "value".equals(name);
		}

		@Override
		public ContainerDecoder arrayDecoder() throws Exception {
			switch (inferredType.getTypeCode()) {
				case objectType:
					return new TypedCollectionFromArrayDecoder(EssentialCollectionTypes.TYPE_LIST, context);
				case mapType:
					if (encodingType.equals("flatmap"))
						return new TypedMapFromArrayDecoder((MapType) inferredType, context);
					else
						return new TypedMapFromEntryArrayDecoder((MapType) inferredType, context);
				case setType:
					return new TypedCollectionFromArrayDecoder((SetType) inferredType, context);
				default:
					throw new IllegalStateException("json array literal is not assignable to: " + inferredType);
			}
		}

		@Override
		public ContainerDecoder objectDecoder() {
			return super.objectDecoder();
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			if (isValueField) {
				this.type = type;
				this.value = value;
			}
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public GenericModelType getType() {
			if (type == null)
				throw new IllegalStateException("no value parsed");

			return type;
		}
	}

	private static class BufferDecoder extends ValueDecoder {
		// linked list of recorded values
		private FieldValue fieldValue;
		private final Consumer<FieldValue> consumer;
		private static MapType mapType = GMF.getTypeReflection().getMapType(EssentialTypes.TYPE_STRING, BaseType.INSTANCE);
		private Map<Object, Object> map;

		public BufferDecoder(Consumer<FieldValue> consumer, DecodingContext context) {
			super(context);
			this.consumer = consumer;
			this.inferredType = BaseType.INSTANCE;
		}

		@Override
		public void setField(String name) {
			fieldValue = new FieldValue(name, fieldValue);
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			fieldValue.type = type;
			fieldValue.value = value;
			consumer.accept(fieldValue);
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			fieldValue.value = registration;
		}

		@Override
		public void close() {
			StringMapEntryDecoder decoder = new StringMapEntryDecoder(mapType, context);

			for (FieldValue currentFieldValue = fieldValue; currentFieldValue != null; currentFieldValue = currentFieldValue.next) {
				decoder.setField(currentFieldValue.name);
				decoder.consumePotentiallyDeferred(currentFieldValue.value);
			}
			this.map = (Map<Object, Object>) decoder.getValue();
		}

		@Override
		public GenericModelType getType() {
			return mapType;
		}

		@Override
		public Object getValue() {
			return map;
		}
	}

	private static class EntityReferenceDecoder extends ValueDecoder {
		private EntityRegistration registration;

		public EntityReferenceDecoder(DecodingContext context) {
			super(context);
			inferredType = BaseType.INSTANCE;
		}

		@Override
		public void setField(String name) {
			if (!name.equals("_ref"))
				throw new IllegalStateException("reference object literal can only have the property \"_ref\"");
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			if (value == null)
				throw new IllegalStateException("reference object literal must have a null value for the property \"_ref\"");

			registration = context.lookupEntity(value.toString());
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			throw new UnsupportedOperationException();
		}

		@Override
		public GenericModelType getType() {
			return registration.entity.entityType();
		}

		@Override
		public Object getValue() {
			return registration.entity;
		}

		@Override
		public EntityRegistration isDeferred() {
			if (registration.entity == null)
				return registration;
			else
				return null;
		}
	}

	private static class GenericObjectDecoder extends ContainerDecoder implements Consumer<FieldValue> {
		private final DecodingContext context;
		private ContainerDecoder delegate;
		private final GenericModelType inferredContainerType;

		public GenericObjectDecoder(DecodingContext context, GenericModelType inferredContainerType) {
			this.delegate = new BufferDecoder(this, context);
			this.context = context;
			this.inferredContainerType = inferredContainerType;
		}

		@Override
		public EntityRegistration isDeferred() {
			return delegate.isDeferred();
		}

		@Override
		public void setField(String name) {
			delegate.setField(name);
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			delegate.consumeDirect(type, value);
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			delegate.consumeDeferred(registration);
		}

		@Override
		public void accept(FieldValue fieldValue) {
			String name = fieldValue.name;

			if (name.charAt(0) == '_') {
				switch (name) {
					case "_type":
						onTypeReceived(fieldValue);
						return;
					case "_ref":
						onRef(fieldValue);
						return;
					default:
						break;
				}
			}
		}

		private void onRef(FieldValue fieldValue) {
			delegate = new EntityReferenceDecoder(context);
			transferToDelegate(fieldValue);
		}

		private void transferToDelegate(FieldValue fieldValue) {
			for (FieldValue currentFieldValue = fieldValue; currentFieldValue != null; currentFieldValue = currentFieldValue.next) {
				delegate.setField(currentFieldValue.name);
				delegate.consumeAssignable(currentFieldValue.type, currentFieldValue.value);
			}
		}

		private void onTypeReceived(FieldValue fieldValue) {
			GenericModelType type = null;
			String typeSignature = (String) fieldValue.value;
			switch (typeSignature) {
				case "map":
					if (inferredContainerType.getTypeCode() == TypeCode.mapType) {
						type = inferredContainerType;
					} else {
						type = EssentialCollectionTypes.TYPE_MAP;
					}
					break;
				case "flatmap":
					if (inferredContainerType.getTypeCode() == TypeCode.mapType) {
						type = inferredContainerType;
					} else {
						type = EssentialCollectionTypes.TYPE_MAP;
					}
					break;
				case "set":
					if (inferredContainerType.getTypeCode() == TypeCode.setType) {
						type = inferredContainerType;
					} else {
						type = EssentialCollectionTypes.TYPE_SET;
					}
					break;
				default:
					type = GMF.getTypeReflection().getType(typeSignature);
					break;
			}

			if (type.isEntity()) {
				delegate = new EntityDecoder((EntityType<?>) type, context);
			} else {
				delegate = new EscapeDecoder(type, context, typeSignature);
			}

			transferToDelegate(fieldValue.next);
		}

		@Override
		public GenericModelType getType() {
			return delegate.getType();
		}

		@Override
		public Object getValue() {
			return delegate.getValue();
		}

		@Override
		public ContainerDecoder arrayDecoder() throws Exception {
			return delegate.arrayDecoder();
		}

		@Override
		public ContainerDecoder objectDecoder() throws Exception {
			return delegate.objectDecoder();
		}

		@Override
		public void consumeInteger(Integer value) {
			delegate.consumeInteger(value);
		}

		@Override
		public void consumeLong(Long value) {
			delegate.consumeLong(value);
		}

		@Override
		public void consumeFloat(Float value) {
			delegate.consumeFloat(value);
		}

		@Override
		public void consumeDouble(Double value) {
			delegate.consumeDouble(value);
		}

		@Override
		public void consumeDecimal(BigDecimal value) {
			delegate.consumeDecimal(value);
		}

		@Override
		public void consumeString(String value) {
			delegate.consumeString(value);
		}

		@Override
		public void consumeBoolean(Boolean value) {
			delegate.consumeBoolean(value);
		}

		@Override
		public void consumeAssignable(GenericModelType type, Object value) {
			delegate.consumeAssignable(type, value);
		}

		@Override
		public void close() {
			delegate.close();
		}

	}

	private static class TypedCollectionFromArrayDecoder extends ValueDecoder {
		private final Collection<Object> collection;
		private final LinearCollectionType collectionType;

		public TypedCollectionFromArrayDecoder(LinearCollectionType collectionType, DecodingContext context) {
			super(context);
			this.inferredType = collectionType.getCollectionElementType();
			this.collection = collectionType.createPlain();
			this.collectionType = collectionType;
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			collection.add(value);
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			if (collectionType.getTypeCode() == TypeCode.listType) {
				List<Object> targetList = (List<Object>) collection;
				int index = targetList.size();
				collection.add(registration);
				registration.addConsumer(e -> {
					targetList.set(index, e);
				});
			} else {
				Collection<Object> targetCollection = collection;
				collection.add(registration);
				collection.remove(registration);
				registration.addConsumer(e -> {
					targetCollection.remove(registration);
					targetCollection.add(e);
				});
			}
		}

		@Override
		public Object getValue() {
			return collection;
		}

		@Override
		public GenericModelType getType() {
			return collectionType;
		}

	}

	private static class TypedMapFromArrayDecoder extends ValueDecoder {

		private final Map<Object, Object> map;
		private Object key;
		private boolean readValue;
		private final MapType mapType;

		public TypedMapFromArrayDecoder(MapType mapType, DecodingContext context) {
			super(context);
			this.mapType = mapType;
			this.inferredType = mapType.getKeyType();
			this.map = mapType.createPlain();
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			if (readValue) {
				map.put(key, value);

				if (key.getClass() == EntityRegistration.class) {
					EntityRegistration registration = (EntityRegistration) key;
					Map<Object, Object> targetMap = map;
					Object v = value;
					registration.addConsumer(e -> {
						targetMap.remove(registration);
						targetMap.put(e, v);
					});
				}

				inferredType = mapType.getKeyType();
				readValue = false;
			} else {
				key = value;
				inferredType = mapType.getValueType();
				readValue = true;
			}
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			if (readValue) {
				map.put(key, registration);

				if (key.getClass() == EntityRegistration.class) {
					EntityRegistration keyRegistration = (EntityRegistration) key;

					class DeferredEntry {
						GenericEntity keyEntity;
						GenericEntity valueEntity;
						Map<Object, Object> targetMap = map;
						Object removeKey = keyRegistration;

						void setKeyEntity(GenericEntity e) {
							keyEntity = e;
							validate();
						}

						void setValueEntity(GenericEntity e) {
							valueEntity = e;
							validate();
						}

						void validate() {
							if (keyEntity != null && valueEntity != null) {
								targetMap.remove(removeKey);
								targetMap.put(keyEntity, valueEntity);
							}
						}
					}

					DeferredEntry deferredEntry = new DeferredEntry();

					keyRegistration.addConsumer(deferredEntry::setKeyEntity);
					registration.addConsumer(deferredEntry::setValueEntity);
				} else {
					Map<Object, Object> targetMap = map;
					Object k = key;

					registration.addConsumer(e -> {
						targetMap.put(k, e);
					});
				}

				inferredType = mapType.getKeyType();
				readValue = false;
			} else {
				key = registration;
				inferredType = mapType.getValueType();
				readValue = true;
			}
		}

		@Override
		public GenericModelType getType() {
			return mapType;
		}

		@Override
		public Object getValue() {
			return map;
		}
	}

	private static class MapEntry {
		public Object key;
		public Object value;
		private EntityRegistration keyDeferring;
		private EntityRegistration valueDeferring;
		private BiConsumer<Object, Object> consumer;

		public void setKeyDeferring(EntityRegistration keyDeferring) {
			this.keyDeferring = keyDeferring;
		}

		public void setValueDeferring(EntityRegistration valueDeferring) {
			this.valueDeferring = valueDeferring;
		}

		public boolean isDeferred() {
			return keyDeferring != null && valueDeferring != null;
		}

		public void setConsumer(BiConsumer<Object, Object> consumer) {
			this.consumer = consumer;

			if (keyDeferring != null)
				keyDeferring.addConsumer(this::notifyDeferredKey);

			if (valueDeferring != null)
				valueDeferring.addConsumer(this::notifyDeferredValue);

		}

		private void notifyDeferredKey(GenericEntity entity) {
			keyDeferring = null;
			key = entity;
			notifyConsumerIfComplete();
		}

		private void notifyDeferredValue(GenericEntity entity) {
			valueDeferring = null;
			value = entity;
			notifyConsumerIfComplete();
		}

		private void notifyConsumerIfComplete() {
			if (!isDeferred()) {
				consumer.accept(key, value);
			}
		}
	}

	private static class MapEntryDecoder extends ValueDecoder {

		private final MapType mapType;

		public MapEntryDecoder(MapType mapType, DecodingContext context) {
			super(context);
			this.mapType = mapType;
		}

		private int state = 0;
		private final MapEntry entry = new MapEntry();

		@Override
		public void setField(String name) {
			if (name.equals("key")) {
				state = 1;
				inferredType = mapType.getKeyType();
			} else if (name.equals("value")) {
				state = 2;
				inferredType = mapType.getValueType();
			} else
				throw new IllegalStateException("expected key or value field but got: " + name);
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			switch (state) {
				case 1:
					entry.key = value;
					break;
				case 2:
					entry.value = value;
					break;
				default:
					throw new IllegalStateException("received a not key/value field value");
			}
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			switch (state) {
				case 1:
					entry.key = registration;
					entry.keyDeferring = registration;
					break;
				case 2:
					entry.value = registration;
					entry.valueDeferring = registration;
					break;
				default:
					throw new IllegalStateException("received a not key/value field value");
			}
		}

		@Override
		public GenericModelType getType() {
			return null;
		}

		@Override
		public Object getValue() {
			return entry;
		}
	}

	private static class TypedMapFromEntryArrayDecoder extends ContainerDecoder {

		private final Map<Object, Object> map;
		private final MapType mapType;
		private final DecodingContext context;

		public TypedMapFromEntryArrayDecoder(MapType mapType, DecodingContext context) {
			this.mapType = mapType;
			this.context = context;
			this.map = mapType.createPlain();
		}

		@Override
		public ContainerDecoder arrayDecoder() throws Exception {
			throw new IllegalStateException("map array should not contain non object literals");
		}

		@Override
		public ContainerDecoder objectDecoder() throws Exception {
			return new MapEntryDecoder(mapType, context);
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			throw new IllegalStateException("Unexpected call");
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			throw new IllegalStateException("Unexpected deferring");
		}

		@Override
		public GenericModelType getType() {
			return mapType;
		}

		@Override
		public Object getValue() {
			return map;
		}

		@Override
		public void setField(String name) {
			throw new IllegalStateException("Unexpected field " + name);
		}

		@Override
		public void consumeInteger(Integer value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeLong(Long value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeFloat(Float value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeDouble(Double value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeDecimal(BigDecimal value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeString(String value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeBoolean(Boolean value) {
			throw new IllegalStateException("Unexpected value " + value);
		}

		@Override
		public void consumeAssignable(GenericModelType type, Object value) {
			MapEntry entry = (MapEntry) value;

			if (entry.isDeferred()) {
				entry.setConsumer(map::put);
			} else {
				map.put(entry.key, entry.value);
			}
		}

		@Override
		public EntityRegistration isDeferred() {
			return null;
		}

		@Override
		public void close() {
		}
	}

	private static class TopDecoder extends ValueDecoder {
		private Object value;
		private GenericModelType type;

		public TopDecoder(GenericModelType inferredType, DecodingContext context) {
			super(context);
			this.inferredType = inferredType;
		}

		@Override
		public void consumeDirect(GenericModelType type, Object value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public void consumeDeferred(EntityRegistration registration) {
			throw new UnsupportedOperationException();
		}

		@Override
		public GenericModelType getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		JsonParser parser = null;
		try {
			JsonFactory factory = new JsonFactory();
			parser = factory.createParser(in);

			DecodingContext context = new DecodingContext(options, parser, createEnhancedEntities);
			context.setSnakeCaseProperties(snakeCaseProperties);
			return context.unmarshall();
		} catch (Exception e) {
			String location = getDiagnosticInformation(parser);
			throw new MarshallException("json codec error during unmarshalling" + location, e);
		}
	}

	protected String getDiagnosticInformation(JsonParser parser) {
		if (parser == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder(" [");

		JsonLocation currentLocation = parser.getCurrentLocation();
		if (currentLocation != null) {
			sb.append("current location: " + currentLocation.toString());
		}
		JsonLocation tokenLocation = parser.getTokenLocation();
		if (tokenLocation != null) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("token location: " + tokenLocation.toString());
		}
		try {
			String name = parser.getCurrentName();
			if (name != null) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append("name: " + name);
			}
		} catch (Exception ignore) {
			// Ignore
		}
		JsonToken currentToken = parser.getCurrentToken();
		if (currentToken != null) {
			String ct = currentToken.toString();
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("token: " + ct);
		}
		Object currentValue = parser.getCurrentValue();
		if (currentValue != null) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append("value: " + currentValue);
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		try {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createParser(reader);

			DecodingContext context = new DecodingContext(options, parser, createEnhancedEntities);
			context.setSnakeCaseProperties(snakeCaseProperties);
			return context.unmarshall();
		} catch (Exception e) {
			throw new MarshallException("json codec error during unmarshalling", e);
		}
	}

	@Override
	public Object decode(String encodedValue) throws CodecException {
		return decode(encodedValue, GmDeserializationOptions.defaultOptions);
	}

	@Override
	public Object decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		try {
			StringReader reader = new StringReader(encodedValue);
			return unmarshall(reader, options);
		} catch (MarshallException e) {
			throw new CodecException("error while unmarshalling", e);
		}
	}

	@Override
	public String encode(Object value) throws CodecException {
		return encode(value, GmSerializationOptions.defaultOptions);
	}

	@Override
	public String encode(Object value, GmSerializationOptions options) throws CodecException {
		StringWriter writer = new StringWriter();
		try {
			marshall(writer, value, options);
			return writer.toString();
		} catch (MarshallException e) {
			throw new CodecException("error while marshalling", e);
		}
	}

	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}

	@Override
	public GmCodec<Object, String> getStringCodec() {
		return this;
	}

	private static final char[] nullLiteral = "null".toCharArray();
	private static final char[] trueLiteral = "true".toCharArray();
	private static final char[] falseLiteral = "false".toCharArray();

	private static final char[] openTypedValue = "{\"value\":".toCharArray();
	private static final char[] openTypedQuotedValue = "{\"value\":\"".toCharArray();
	private static final char[] closeDouble = ", \"_type\":\"double\"}".toCharArray();
	private static final char[] closeFloat = ", \"_type\":\"float\"}".toCharArray();
	private static final char[] closeDate = "\", \"_type\":\"date\"}".toCharArray();
	private static final char[] closeDecimal = "\", \"_type\":\"decimal\"}".toCharArray();
	private static final char[] closeLong = "\", \"_type\":\"long\"}".toCharArray();
	private static final char[] midEnum = "\", \"_type\":\"".toCharArray();
	private static final char[] closeEnum = "\"}".toCharArray();
	private static final char[] emptyList = "[]".toCharArray();
	private static final char[] openSet = "{\"_type\": \"set\", \"value\":[".toCharArray();
	private static final char[] emptySet = "{\"_type\": \"set\", \"value\":[]}".toCharArray();
	private static final char[] openMap = "{\"_type\": \"map\", \"value\":[".toCharArray();
	private static final char[] openFlatMap = "{\"_type\": \"flatmap\", \"value\":[".toCharArray();
	private static final char[] emptyMap = "{\"_type\": \"map\", \"value\":[]}".toCharArray();
	private static final char[] emptyFlatMap = "{\"_type\": \"flatmap\", \"value\":[]}".toCharArray();
	private static final char[] closeTypedCollection = "]}".toCharArray();
	private static final char[] openEntry = "{\"key\":".toCharArray();
	private static final char[] midEntry = ", \"value\":".toCharArray();

	private static final boolean EnumTypeImpl = false;

	private boolean assignAbsenceInformationForMissingProperties;

	private void marshall(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer, GenericModelType contextType, Object value,
			int indent, boolean isIdentifier) throws MarshallException, IOException {
		if (value == null) {
			writer.write(nullLiteral);
			return;
		}

		boolean writeSimplifiedValues = context.writeSimplifiedValues();
		GenericModelType type = contextType;
		while (true) {
			switch (type.getTypeCode()) {
				// object type: retrieve the actual type and do another round of type detection here
				case objectType:
					GenericModelType actualType = type.getActualType(value);
					if (type.equals(actualType)) {
						throw new MarshallException("Object type should resolve to a concrete type.");
					}
					type = actualType;
					writeSimplifiedValues = false;
					if (isIdentifier && context.writeSimplifiedValues && type.isScalar()) {
						writeSimplifiedValues = true;
					} else if (!context.allowsTypeExplicitness()) {
						writeSimplifiedValues = true;
					}
					break;

				case booleanType:
					if ((Boolean) value)
						writer.write(trueLiteral);
					else
						writer.write(falseLiteral);
					return;
				case stringType:
					writer.write('"');
					writeEscaped(writer, (String) value);
					writer.write('"');
					return;
				case dateType:
					if (writeSimplifiedValues) {
						writer.write('"');
						writer.write(context.getDateCoding().encode((Date) value));
						writer.write('"');
					} else {
						writer.write(openTypedQuotedValue);
						writer.write(context.getDateCoding().encode((Date) value));
						writer.write(closeDate);
					}
					return;
				case integerType:
					writer.write(value.toString());
					return;
				case doubleType:
					if (writeSimplifiedValues) {
						writer.write(value.toString());
					} else {
						writer.write(openTypedValue);
						writer.write(value.toString());
						writer.write(closeDouble);
					}
					return;
				case floatType:
					if (writeSimplifiedValues) {
						writer.write(value.toString());
					} else {
						writer.write(openTypedValue);
						writer.write(value.toString());
						writer.write(closeFloat);
					}
					return;
				case longType:
					if (writeSimplifiedValues) {
						writeSimplifiedNumberType(context, writer, value);
					} else {
						writer.write(openTypedQuotedValue);
						writer.write(value.toString());
						writer.write(closeLong);
					}
					return;
				case decimalType:
					if (writeSimplifiedValues) {
						writeSimplifiedNumberType(context, writer, value);
					} else {
						writer.write(openTypedQuotedValue);
						writer.write(value.toString());
						writer.write(closeDecimal);
					}
					return;

				// custom types
				case entityType:
					marshallEntity(context, prettinessSupport, writer, (GenericEntity) value, indent, contextType);
					return;

				case enumType:
					if (writeSimplifiedValues) {
						writer.write('"');
						writer.write(value.toString());
						writer.write('"');
					} else {
						writer.write(openTypedQuotedValue);
						writer.write(value.toString());
						writer.write(midEnum);
						writer.write(type.getTypeSignature());
						writer.write(closeEnum);
					}
					return;

				// collections
				case listType: {
					Collection<?> collection = (Collection<?>) value;
					marshallCollection(context, prettinessSupport, writer, (ListType) type, collection, indent);
					return;
				}
				case setType: {
					Collection<?> collection = (Collection<?>) value;
					if (writeSimplifiedValues) {
						marshallCollection(context, prettinessSupport, writer, (SetType) type, collection, indent);
					} else {
						if (collection.isEmpty()) {
							writer.write(emptySet);
							return;
						}
						writer.write(openSet);
						int i = 0;
						int elementIndent = indent + 1;
						GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
						for (Object e : collection) {
							if (i > 0)
								writer.write(',');
							prettinessSupport.writeLinefeed(writer, elementIndent);

							marshall(context, prettinessSupport, writer, elementType, e, elementIndent, false);
							i++;
						}
						prettinessSupport.writeLinefeed(writer, indent);
						writer.write(closeTypedCollection);
					}
					return;
				}
				case mapType: {
					Map<?, ?> map = (Map<?, ?>) value;
					if (map.isEmpty()) {
						writer.write(emptyFlatMap);
						return;
					}

					int i = 0;
					int elementIndent = indent + 1;
					int subElementIndent = indent + 2;
					GenericModelType[] parameterization = ((CollectionType) type).getParameterization();
					GenericModelType keyType = parameterization[0];
					GenericModelType valueType = parameterization[1];
					boolean isStringKey = keyType == EssentialTypes.TYPE_STRING;
					boolean isEnumKey = keyType.isEnum();
					boolean writeSimpleFlatMap = !context.allowsTypeExplicitness() || isStringKey || (isEnumKey && writeSimplifiedValues);
					if (writeSimpleFlatMap) {
						writer.write("{");
					} else {
						writer.write(openFlatMap);
					}
					for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
						if (i > 0)
							writer.write(',');
						prettinessSupport.writeLinefeed(writer, elementIndent);
						marshall(context, prettinessSupport, writer, keyType, entry.getKey(), subElementIndent, false);
						if (writeSimpleFlatMap) {
							writer.write(":");
						} else {
							writer.write(",");
						}
						marshall(context, prettinessSupport, writer, valueType, entry.getValue(), subElementIndent, false);
						i++;
					}
					prettinessSupport.writeLinefeed(writer, indent);
					if (writeSimpleFlatMap) {
						writer.write("}".toCharArray());
					} else {
						writer.write(closeTypedCollection);
					}
					return;
				}
				default:
					break;
			}
		}

	}

	private void writeSimplifiedNumberType(EncodingContext context, Writer writer, Object value) throws IOException {
		if (context.stringifyNumbers) {
			writer.write('"');
			writer.write(value.toString());
			writer.write('"');
		} else {
			writer.write(value.toString());
		}
	}

	private void marshallCollection(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer, CollectionType collectionType,
			Collection<?> collection, int indent) throws IOException {
		if (collection.isEmpty()) {
			writer.write(emptyList);
			return;
		}
		writer.write('[');
		int i = 0;
		int elementIndent = indent + 1;
		GenericModelType elementType = collectionType.getCollectionElementType();
		for (Object e : collection) {
			if (i > 0)
				writer.write(',');
			prettinessSupport.writeLinefeed(writer, elementIndent);
			marshall(context, prettinessSupport, writer, elementType, e, elementIndent, false);
			i++;
		}
		prettinessSupport.writeLinefeed(writer, indent);
		writer.write(']');

	}

	private static final char[] openEntityRef = "{\"_ref\": \"".toCharArray();
	private static final char[] closeEntityRef = "\"}".toCharArray();
	private static final char[] openEntity = "{\"_type\": \"".toCharArray();
	private static final char[] openTypeFreeEntity = "{\"_id\": \"".toCharArray();
	private static final char[] openTypeFreeEntityNoId = "{".toCharArray();
	private static final char[] idPartEntity = "\", \"_id\": \"".toCharArray();
	private static final char[] partialPartEntity = "\", \"_partial\": \"".toCharArray();
	private static final char[] partialPartEntityNoId = "\"_partial\": \"\"".toCharArray();
	private static final char[] openEntityFinish = "\"".toCharArray();
	private static final char[] midProperty = "\": ".toCharArray();
	private static final char[] openAbsentProperty = "\"?".toCharArray();

	protected void marshallEntity(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer, GenericEntity entity, int indent,
			GenericModelType contextType) throws IOException, MarshallException {
		if (context.getEntityRecurrenceDepth() == 0) {
			marsallEntityWithZeroEntityRecurrenceDepth(context, prettinessSupport, writer, entity, indent, contextType);
		} else {
			marsallEntityWithEntityRecurrenceDepth(context, prettinessSupport, writer, entity, indent, contextType);
		}

	}

	private void marsallEntityWithEntityRecurrenceDepth(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer,
			GenericEntity entity, int indent, GenericModelType contextType) {

		if (context.isInRecurrence() || context.lookupId(entity) != null) {
			context.incrementCurrentRecurrenceDepth();
			try {
				_marshallEntity(context, prettinessSupport, writer, entity, indent, contextType);
			} finally {
				context.decrementCurrentRecurrenceDepth();
			}
		} else {
			context.register(entity);
			_marshallEntity(context, prettinessSupport, writer, entity, indent, contextType);
		}

	}

	private void _marshallEntity(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer, GenericEntity entity, int indent,
			GenericModelType contextType) {
		Set<GenericEntity> recursiveRecurrenceSet = context.getRecursiveRecurrenceSet();

		boolean nonRecursiveVisit = recursiveRecurrenceSet.add(entity);

		try {
			boolean onlyScalars = !nonRecursiveVisit || context.isRecurrenceMax();

			EntityType<?> type = entity.entityType();

			boolean skipType = !context.allowsTypeExplicitness() || (context.canSkipNonPolymorphicType() && contextType == entity.entityType());
			if (skipType) {
				writer.write(openTypeFreeEntityNoId);
			} else {
				writer.write(openEntity);
				// encode entity
				writer.write(type.getTypeSignature());
			}

			if (!skipType) {
				writer.write(openEntityFinish);
			}

			int propertyIndent = indent + 1;
			int i = 0;
			boolean dpa = context.useDirectPropertyAccess();
			boolean writeEmptyProperties = context.writeEmptyProperties();

			for (Property property : entity.entityType().getProperties()) {
				GenericModelType propertyType = property.getType();
				if (!propertyType.isScalar() && onlyScalars) {
					continue;
				}

				Object value = dpa ? property.getDirectUnsafe(entity) : property.get(entity);

				Function<Property, String> propertyNameSupplier = context.options.findAttribute(PropertySerializationTranslation.class).orElse(null);
				String propertyName = propertyNameSupplier != null ? propertyNameSupplier.apply(property) : property.getName();

				if (value == null) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					if (absenceInformation != null) {
						if (context.writeAbsenceProperties) {
							writer.write(',');
							prettinessSupport.writeLinefeed(writer, propertyIndent);
							writer.write(openAbsentProperty);
							writer.write(propertyName);
							writer.write(midProperty);
							marshallEntity(context, prettinessSupport, writer, absenceInformation, propertyIndent, AbsenceInformation.T);
							i++;
						}
						continue;
					} else {
						if (!writeEmptyProperties)
							continue;
					}
				} else {
					if (!writeEmptyProperties && propertyType.getTypeCode() != TypeCode.objectType && propertyType.isEmpty(value))
						continue;
				}

				if (!skipType && i == 0) {
					writer.write(',');
				} else if (i > 0) {
					writer.write(',');
				}

				prettinessSupport.writeLinefeed(writer, propertyIndent);
				writer.write('"');
				writer.write(propertyName);
				writer.write(midProperty);

				// GenericModelType actualType = GMF.getTypeReflection().getBaseType().getActualType(value);
				marshall(context, prettinessSupport, writer, propertyType, value, propertyIndent, property.isIdentifier());
				i++;
			}

			if (i > 0)
				prettinessSupport.writeLinefeed(writer, indent);

			writer.write('}');
		} catch (MarshallException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshallException("error while encoding entity", e);
		} finally {
			if (nonRecursiveVisit)
				recursiveRecurrenceSet.remove(entity);
		}
	}

	private void marsallEntityWithZeroEntityRecurrenceDepth(EncodingContext context, PrettinessSupport prettinessSupport, Writer writer,
			GenericEntity entity, int indent, GenericModelType contextType) throws IOException, MarshallException {
		int indentLimit = prettinessSupport.getMaxIndent() - 4;
		if (indent > indentLimit)
			indent = indentLimit;

		Integer refId = context.lookupId(entity);
		if (refId != null) {
			// encode reference
			writer.write(openEntityRef);
			writer.write(refId.toString());
			writer.write(closeEntityRef);
		} else {
			try {
				EntityType<?> type = entity.entityType();

				boolean skipType = !context.allowsTypeExplicitness() || (context.canSkipNonPolymorphicType() && contextType == entity.entityType());

				if (skipType) {
					writer.write(openTypeFreeEntity);
				} else {
					writer.write(openEntity);
					// encode entity
					writer.write(type.getTypeSignature());
					writer.write(idPartEntity);
				}

				refId = context.register(entity);
				writer.write(refId.toString());

				writer.write(openEntityFinish);

				int propertyIndent = indent + 1;
				int i = 0;
				boolean dpa = context.useDirectPropertyAccess();
				boolean writeEmptyProperties = context.writeEmptyProperties();

				for (Property property : type.getProperties()) {

					GenericModelType propertyType = property.getType();

					Object value = dpa ? property.getDirectUnsafe(entity) : property.get(entity);

					Function<Property, String> propertyNameSupplier = context.options.findAttribute(PropertySerializationTranslation.class)
							.orElse(null);
					String propertyName = propertyNameSupplier != null ? propertyNameSupplier.apply(property) : property.getName();

					if (value == null) {
						AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

						if (absenceInformation != null) {
							if (context.writeAbsenceProperties) {
								writer.write(',');
								prettinessSupport.writeLinefeed(writer, propertyIndent);
								writer.write(openAbsentProperty);
								writer.write(propertyName);
								writer.write(midProperty);
								marshallEntity(context, prettinessSupport, writer, absenceInformation, propertyIndent, AbsenceInformation.T);
								i++;
							}
							continue;
						} else {
							if (!writeEmptyProperties)
								continue;
						}
					} else {
						if (!writeEmptyProperties && propertyType.getTypeCode() != TypeCode.objectType && propertyType.isEmpty(value))
							continue;
					}

					writer.write(',');
					prettinessSupport.writeLinefeed(writer, propertyIndent);
					writer.write('"');
					writer.write(propertyName);
					writer.write(midProperty);

					// GenericModelType actualType = GMF.getTypeReflection().getBaseType().getActualType(value);
					marshall(context, prettinessSupport, writer, propertyType, value, propertyIndent, property.isIdentifier());
					i++;
				}

				if (i > 0)
					prettinessSupport.writeLinefeed(writer, indent);

				writer.write('}');
			} catch (MarshallException e) {
				throw e;
			} catch (Exception e) {
				throw new MarshallException("error while encoding entity", e);
			}
		}
	}

	public static String escape(String s) throws IOException {
		StringWriter writer = new StringWriter();
		writeEscaped(writer, s);
		return writer.toString();
	}

	private final static char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
	private static final char[][] ESCAPES = new char[128][];

	static {
		ESCAPES['"'] = "\\\"".toCharArray();
		ESCAPES['\\'] = "\\\\".toCharArray();
		ESCAPES['\t'] = "\\t".toCharArray();
		ESCAPES['\f'] = "\\f".toCharArray();
		ESCAPES['\n'] = "\\n".toCharArray();
		ESCAPES['\r'] = "\\r".toCharArray();

		for (int i = 0; i < 32; i++) {
			if (ESCAPES[i] == null)
				ESCAPES[i] = ("\\u00" + HEX_CHARS[i >> 4] + HEX_CHARS[i & 0xF]).toCharArray();
		}
	}

	public static void writeEscaped(Writer writer, String string) throws IOException {
		int len = string.length();
		int s = 0;
		int i = 0;
		char esc[] = null;
		for (; i < len; i++) {
			char c = string.charAt(i);

			if (c < 128) {
				esc = ESCAPES[c];
				if (esc != null) {
					writer.write(string, s, i - s);
					writer.write(esc);
					s = i + 1;
				}
			}
		}
		if (i > s) {
			if (s == 0)
				writer.write(string);
			else
				writer.write(string, s, i - s);
		}
	}

	private static DateCoding dateTimeFormatterFromOptions(GmMarshallingOptions options) {
		String datePattern = options.findOrNull(DateFormatOption.class);

		if (datePattern != null) {
			ZoneId zoneId = options.findAttribute(DateDefaultZoneOption.class).map(ZoneId::of).orElse(ZoneOffset.UTC);
			Locale locale = options.findAttribute(DateLocaleOption.class).map(Locale::forLanguageTag).orElse(Locale.US);

			return new DateCoding(datePattern, zoneId, locale);
		} else {
			return new DateCoding(DateTools.ISO8601_DATE_WITH_MS_FORMAT_AND_Z_OPTIONAL_TIME, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
		}
	}

	private static class CodingContext {
		private final DateCoding dateCoding;

		public CodingContext(GmMarshallingOptions options) {
			this.dateCoding = dateTimeFormatterFromOptions(options);
		}

		public DateCoding getDateCoding() {
			return dateCoding;
		}
	}

	private static class EncodingContext extends CodingContext {

		private final Map<GenericEntity, Integer> idByEntities = new HashMap<>();
		private final Set<GenericEntity> recursiveRecurrenceSet = new HashSet<>();
		private int idSequence = 0;
		private final boolean useDirectPropertyAccess;
		private final boolean writeEmptyProperties;
		private boolean canSkipNonPolymorphingType;
		private final TypeExplicitness typeExplicitness;
		private boolean writeSimplifiedValues;
		private Integer entityRecurrenceDepth = 0;
		private int currentRecurrenceDepth = 0;
		private boolean writeAbsenceProperties = true;
		private boolean stringifyNumbers = false;
		private final Consumer<? super GenericEntity> entityVisitor;
		private final GmSerializationOptions options;

		public EncodingContext(GmSerializationOptions options) {
			super(options);
			this.options = options;
			this.useDirectPropertyAccess = options.useDirectPropertyAccess();
			this.writeEmptyProperties = options.writeEmptyProperties();
			this.writeAbsenceProperties = options.writeAbsenceInformation();
			this.stringifyNumbers = options.findAttribute(StringifyNumbersOption.class).orElse(false);
			this.typeExplicitness = options.findOrDefault(TypeExplicitnessOption.class, TypeExplicitness.auto);

			switch (typeExplicitness) {
				case always:
					break;
				case auto:
				case entities:
					canSkipNonPolymorphingType = false;
					writeSimplifiedValues = true;
					break;
				case polymorphic:
				case never:
					canSkipNonPolymorphingType = true;
					writeSimplifiedValues = true;
					break;
				default:
					break;
			}

			this.entityRecurrenceDepth = options.findAttribute(EntityRecurrenceDepth.class).orElse(null);
			if (this.entityRecurrenceDepth == null) {
				this.entityRecurrenceDepth = 0;
			}

			this.entityVisitor = options.findAttribute(EntityVisitorOption.class).orElse(null);
		}

		public boolean allowsTypeExplicitness() {
			return typeExplicitness != TypeExplicitness.never;
		}

		public boolean canSkipNonPolymorphicType() {
			return canSkipNonPolymorphingType;
		}

		public boolean writeSimplifiedValues() {
			return writeSimplifiedValues;
		}

		public boolean useDirectPropertyAccess() {
			return useDirectPropertyAccess;
		}

		public boolean writeEmptyProperties() {
			return writeEmptyProperties;
		}

		public Integer register(GenericEntity entity) {
			return idByEntities.computeIfAbsent(entity, e -> {
				if (entityVisitor != null)
					entityVisitor.accept(e);
				return idSequence++;
			});
		}

		public Integer lookupId(GenericEntity entity) {
			return idByEntities.get(entity);
		}

		public Integer getEntityRecurrenceDepth() {
			return this.entityRecurrenceDepth;
		}

		public boolean isInRecurrence() {
			return this.currentRecurrenceDepth > 0;
		}

		public int incrementCurrentRecurrenceDepth() {
			return ++this.currentRecurrenceDepth;
		}

		public int decrementCurrentRecurrenceDepth() {
			return --this.currentRecurrenceDepth;
		}

		public Set<GenericEntity> getRecursiveRecurrenceSet() {
			return this.recursiveRecurrenceSet;
		}

		public boolean isRecurrenceMax() {
			if (this.entityRecurrenceDepth < 0) {
				return false;
			}
			return this.currentRecurrenceDepth >= this.entityRecurrenceDepth;
		}

	}

	private static class DecodingContext extends CodingContext {
		private final Map<String, EntityRegistration> entitiesById = new HashMap<>();
		private final Map<String, Map<String, EntityRegistration>> identityManagementRegister = new HashMap<>();
		private final AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
		private final boolean assignAbsenceInformation;
		private final boolean enhanced;
		private final GmSession session;
		private final GenericModelType inferredType;
		private final JsonParser parser;
		private final GmDeserializationOptions options;
		private IdentityManagementMode identityManagementMode;
		private final Consumer<? super GenericEntity> entityVisitor;
		private boolean snakeCaseProperties = false;
		private final BiFunction<EntityType<?>, Property, GenericModelType> propertyTypeInferenceOverride;

		public DecodingContext(GmDeserializationOptions options, JsonParser parser, boolean enhanced) {
			super(options);
			this.options = options;
			this.parser = parser;
			this.enhanced = enhanced;
			this.session = options.getSession();
			this.assignAbsenceInformation = options.getAbsentifyMissingProperties();
			this.inferredType = options.getInferredRootType();

			this.identityManagementMode = options.findOrDefault(IdentityManagementModeOption.class, IdentityManagementMode.auto);
			this.entityVisitor = options.findOrNull(EntityVisitorOption.class);
			this.propertyTypeInferenceOverride = options.findOrNull(PropertyTypeInferenceOverride.class);
		}

		public GenericModelType getInferredPropertyType(EntityType<?> entityType, Property property) {
			if (propertyTypeInferenceOverride != null) {
				GenericModelType type = propertyTypeInferenceOverride.apply(entityType, property);
				if (type != null)
					return type;
			}
			
			return property.getType();
		}

		public void setSnakeCaseProperties(boolean snakeCaseProperties) {
			this.snakeCaseProperties = snakeCaseProperties;
		}

		public boolean getSnakeCaseProperties() {
			return snakeCaseProperties;
		}

		public boolean getAssignAbsenceInformation() {
			return assignAbsenceInformation;
		}

		public void register(GenericEntity entity, String id) {
			lookupEntity(id).set(entity);
		}

		public AbsenceInformation getAbsenceInformationForMissingProperties() {
			return absenceInformationForMissingProperties;
		}

		public EntityRegistration lookupEntity(String id) {
			return entitiesById.computeIfAbsent(id, k -> new EntityRegistration());
		}

		public EntityRegistration lookupIdentity(GenericModelType modelType, String id) {
			String typeName = modelType.getTypeName();
			Map<String, EntityRegistration> registry = identityManagementRegister.computeIfAbsent(typeName, k -> new HashMap<>());
			return registry.computeIfAbsent(id, f -> new EntityRegistration());
		}

		public EntityRegistration lookupByGlobalId(GenericModelType modelType, String globalId) {
			String typeName = modelType.getTypeName();
			Map<String, EntityRegistration> registry = identityManagementRegister.get(typeName);
			if (registry != null && globalId != null) {
				Optional<EntityRegistration> result = registry.entrySet().stream()
						.filter(x -> (x.getValue() != null && x.getValue().entity != null && globalId.equals(x.getValue().entity.getGlobalId())))
						.map(x -> x.getValue()).findFirst();
				if (result.isPresent())
					return result.get();
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public <T extends GenericEntity> T createRaw(EntityType<?> entityType) {
			T entity = (T) (session != null ? session.createRaw(entityType) : enhanced ? entityType.createRaw() : entityType.createPlainRaw());

			if (assignAbsenceInformation) {
				entityType.getProperties().forEach(p -> p.setAbsenceInformation(entity, absenceInformationForMissingProperties));
			}

			if (entityVisitor != null)
				entityVisitor.accept(entity);

			return entity;
		}

		public boolean isSettingPropertiesDirect() {
			return session == null;
		}

		private boolean isPropertyLenient() {
			return options.getDecodingLenience() != null && options.getDecodingLenience().isPropertyLenient();
		}

		private Object unmarshall() throws Exception {
			JsonToken token = null;

			GenericModelType inferredRootType = options.getInferredRootType();
			if (inferredRootType == null)
				inferredRootType = BaseType.INSTANCE;

			TopDecoder topDecoder = new TopDecoder(inferredRootType, this);
			Deque<ContainerDecoder> stack = new ArrayDeque<>();
			ContainerDecoder decoder = topDecoder;

			while ((token = parser.nextValue()) != null) {

				String fieldName = parser.getCurrentName();

				switch (token) {
					case START_OBJECT:
						if (fieldName != null)
							decoder.setField(fieldName);
						ContainerDecoder objectDecoder = decoder.objectDecoder();
						stack.push(decoder);
						decoder = objectDecoder;
						break;

					case START_ARRAY:
						if (fieldName != null)
							decoder.setField(fieldName);
						ContainerDecoder arrayDecoder = decoder.arrayDecoder();
						stack.push(decoder);
						decoder = arrayDecoder;
						break;

					case END_ARRAY:
						ContainerDecoder closingArrayContainer = decoder;
						closingArrayContainer.close();
						decoder = stack.pop();
						decoder.consumeAssignable(closingArrayContainer.getType(), closingArrayContainer.getValue());
						break;
					case END_OBJECT:
						ContainerDecoder closingObjectContainer = decoder;
						closingObjectContainer.close();
						decoder = stack.pop();
						EntityRegistration deferred = closingObjectContainer.isDeferred();

						if (deferred != null)
							decoder.consumeDeferred(deferred);
						else {
							checkIfTypeIsMissing(closingObjectContainer, fieldName, stack, decoder);
							decoder.consumeAssignable(closingObjectContainer.getType(), closingObjectContainer.getValue());
						}
						break;

					case VALUE_NULL:
						if (fieldName != null)
							decoder.setField(fieldName);
						decoder.consumeDirect(BaseType.INSTANCE, null);
						break;

					case VALUE_NUMBER_FLOAT:
					case VALUE_NUMBER_INT:
						if (fieldName != null)
							decoder.setField(fieldName);
						if (GenericEntity.id.equals(fieldName)) {
							decoder.consumeLong(parser.getLongValue());
						} else {
							switch (parser.getNumberType()) {
								case BIG_DECIMAL:
									decoder.consumeDecimal(parser.getDecimalValue());
									break;
								case BIG_INTEGER:
									decoder.consumeDecimal(parser.getDecimalValue());
									break;
								case DOUBLE:
									decoder.consumeDouble(parser.getDoubleValue());
									break;
								case FLOAT:
									decoder.consumeFloat(parser.getFloatValue());
									break;
								case INT:
									decoder.consumeInteger(parser.getIntValue());
									break;
								case LONG:
									decoder.consumeLong(parser.getLongValue());
									break;
								default:
									break;
							}
						}
						break;

					case VALUE_STRING:
						if (fieldName != null)
							decoder.setField(fieldName);
						decoder.consumeString(parser.getText());
						break;
					case VALUE_TRUE:
						if (fieldName != null)
							decoder.setField(fieldName);
						decoder.consumeBoolean(Boolean.TRUE);
						break;
					case VALUE_FALSE:
						if (fieldName != null)
							decoder.setField(fieldName);
						decoder.consumeBoolean(Boolean.FALSE);
						break;

					default:
						break;
				}
			}

			return topDecoder.getValue();
		}

	}

	private static void checkIfTypeIsMissing(ContainerDecoder closingObjectContainer, String fieldName, Deque<ContainerDecoder> stack,
			ContainerDecoder lastDecoder) {
		if (closingObjectContainer instanceof GenericObjectDecoder) {
			GenericObjectDecoder genericObjectDecoder = (GenericObjectDecoder) closingObjectContainer;

			if (genericObjectDecoder.inferredContainerType.isEntity()) {
				EntityType<?> propertyType = (EntityType<?>) genericObjectDecoder.inferredContainerType;
				if (propertyType.isAbstract() && closingObjectContainer.getValue() instanceof Map) {
					String path = reverse(stack.stream()).filter(GenericObjectDecoder.class::isInstance).map(g -> (GenericObjectDecoder) g)
							.filter(g -> g.delegate != null).map(g -> g.delegate).filter(EntityDecoder.class::isInstance).map(g -> (EntityDecoder) g)
							.map(g -> {
								return "" + g.entityType.getTypeName() + ":" + g.property.getName();
							}).collect(Collectors.joining(" -> "));
					EntityDecoder ed = (EntityDecoder) ((GenericObjectDecoder) lastDecoder).delegate;

					path = path.concat((!path.isEmpty() ? " -> " : "") + ed.inferredType.getTypeName() + ":" + ed.property.getName());

					throw new MarshallException("_type is missing for property: " + fieldName + " with path [" + path + "]");
				}
			}
		}
	}

	public static <T> Stream<T> reverse(Stream<T> stream) {
		Deque<T> stack = new ArrayDeque<>();
		stream.forEach(stack::push);

		return stack.stream();
	}

	private static abstract class PropertyAbsenceHelper {

		public abstract void addPresent(Property property);
		public abstract void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity);
	}

	private static class ActivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		private final Set<Property> presentProperties = new HashSet<>();
		private final DecodingContext context;

		public ActivePropertyAbsenceHelper(DecodingContext context) {
			super();
			this.context = context;
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
						property.setAbsenceInformation(entity, context.getAbsenceInformationForMissingProperties());
					}
				}
			}

		}
	}

	private static class InactivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		public static InactivePropertyAbsenceHelper instance = new InactivePropertyAbsenceHelper();

		@Override
		public void addPresent(Property property) {
			// intentionally left empty
		}

		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			// intentionally left empty
		}
	}
}
