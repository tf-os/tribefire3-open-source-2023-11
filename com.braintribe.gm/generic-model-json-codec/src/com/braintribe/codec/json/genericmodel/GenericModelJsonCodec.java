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
package com.braintribe.codec.json.genericmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.json.genericmodel.util.TwoWayDateCodec;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.GmSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * JSON <-> GenericModel codec
 * 
 * @author dirk.scheffler
 */
public class GenericModelJsonCodec<T> implements GmCodec<T, JsonNode> {
	protected GenericModelTypeReflection genericModelTypeReflection = GMF.getTypeReflection();
	private GenericModelType type = genericModelTypeReflection.getBaseType();
	private final JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
	private final Codec<Date, String> dateCodec = new TwoWayDateCodec();

	private boolean writeAbsenceInformation = true;
	private boolean createEnhancedEntities = true;
	private boolean assignAbsenceInformationForMissingProperties;

	@Configurable
	public void setGenericModelTypeReflection(GenericModelTypeReflection genericModelTypeReflection) {
		this.genericModelTypeReflection = genericModelTypeReflection;
	}

	@Configurable
	public void setAssignAbsenceInformationForMissingProperties(boolean assignAbsenceInformatonForMissingProperties) {
		this.assignAbsenceInformationForMissingProperties = assignAbsenceInformatonForMissingProperties;
	}

	@Configurable
	public void setCreateEnhancedEntities(boolean createEnhancedEntities) {
		this.createEnhancedEntities = createEnhancedEntities;
	}

	@Configurable
	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}

	@Configurable
	public void setType(GenericModelType type) {
		this.type = type;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T decode(JsonNode jsonNode, GmDeserializationOptions options) throws CodecException {
		DecodingContext context = new DecodingContext(options, createEnhancedEntities);
		T value = (T) decodeJson(context, jsonNode);
		return value;
	}

	@Override
	public T decode(JsonNode jsonNode) throws CodecException {
		return decode(jsonNode,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	protected Object decodeJson(DecodingContext context, JsonNode jsonNode) throws CodecException {
		JsonNodeType nodeType = jsonNode.getNodeType();

		switch (nodeType) {
			case ARRAY:
				ArrayNode arrayNode = (ArrayNode) jsonNode;
				int size = arrayNode.size();
				List<Object> list = new ArrayList<Object>(size);
				for (int i = 0; i < size; i++) {
					list.add(decodeJson(context, arrayNode.get(i)));
				}
				return list;
			case BOOLEAN:
				return ((BooleanNode) jsonNode).booleanValue();
			case NULL:
				return null;
			case NUMBER:
				NumericNode numericNode = (NumericNode) jsonNode;
				switch (numericNode.numberType()) {
					case BIG_DECIMAL:
						return numericNode.decimalValue();
					case DOUBLE:
						return numericNode.doubleValue();
					case BIG_INTEGER:
						return numericNode.bigIntegerValue();
					case FLOAT:
						return numericNode.floatValue();
					case INT:
						return numericNode.intValue();
					case LONG:
						return numericNode.longValue();
					default:
						throw new CodecException("Unknown number type: " + numericNode.numberType());
				}
			case OBJECT:
				return decodeObjectNode(context, (ObjectNode) jsonNode);
			case STRING:
				return ((TextNode) jsonNode).textValue();
			default:
				throw new CodecException("unknown JSON type found " + nodeType.name());
		}
	}

	protected Object decodeObjectNode(DecodingContext context, ObjectNode objectNode) throws CodecException {
		JsonNode typeNode = objectNode.get("_type");

		if (typeNode == null) {
			JsonNode refNode = objectNode.get("_ref");

			if (refNode != null) {
				String refId = refNode.asText();
				GenericEntity referencedEntity = context.lookupEntity(refId);

				if (referencedEntity == null)
					throw new CodecException("referenced non existent entity with id " + refId);

				return referencedEntity;
			} else
				throw new CodecException("missing _type property for " + objectNode.toString());
		}

		String type = typeNode.asText();

		if (type.equals("set")) {
			ArrayNode arrayNode = (ArrayNode) extractValue(objectNode);
			Set<Object> set = new HashSet<Object>();

			int size = arrayNode.size();
			for (int i = 0; i < size; i++) {
				JsonNode elementNode = arrayNode.get(i);
				Object element = decodeJson(context, elementNode);
				set.add(element);
			}

			return set;
		} else if (type.equals("map")) {
			ArrayNode arrayNode = (ArrayNode) extractValue(objectNode);
			Map<Object, Object> map = new HashMap<Object, Object>();

			int size = arrayNode.size();
			for (int i = 0; i < size; i++) {
				JsonNode entryNode = arrayNode.get(i);
				JsonNode keyNode = entryNode.get("key");
				JsonNode valueNode = entryNode.get("value");
				Object key = decodeJson(context, keyNode);
				Object value = decodeJson(context, valueNode);
				map.put(key, value);
			}
			return map;
		} else {
			GenericModelType genericModelType = genericModelTypeReflection.getType(type);

			switch (genericModelType.getTypeCode()) {
				case entityType:
					EntityType<GenericEntity> entityType = genericModelType.cast();
					return decodeJson(context, objectNode, entityType);
				case enumType:
					String enumName = extractValue(objectNode).asText();
					return ((EnumType) genericModelType).getInstance(enumName);
				case dateType:
					String dateString = extractValue(objectNode).asText();
					return dateCodec.decode(dateString);
				case decimalType:
					String decimalString = extractValue(objectNode).asText();
					return new BigDecimal(decimalString);
				case doubleType:
					return extractValue(objectNode).asDouble();
				case floatType:
					return (float) extractValue(objectNode).asDouble();
				case longType:
					return extractValue(objectNode).asLong();
				case stringType:
					return extractValue(objectNode).asText();
				case booleanType:
					return extractValue(objectNode).asBoolean();
				case integerType:
					return extractValue(objectNode).asInt();
				default:
					throw new CodecException("problems to resolve the GenericModelType " + type);
			}
		}
	}

	protected JsonNode extractValue(ObjectNode objectNode) throws CodecException {
		JsonNode valueNode = objectNode.get("value");
		if (valueNode == null)
			throw new CodecException("missing value in typed object " + objectNode);
		return valueNode;
	}

	protected <G extends GenericEntity> G decodeJson(DecodingContext context, ObjectNode objectNode, EntityType<G> entityType) throws CodecException {
		try {
			G entity = context.createRaw(entityType);

			JsonNode idNode = objectNode.get("_id");

			if (idNode != null) {
				String id = idNode.asText();
				context.register(entity, id);
			}

			Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields();

			PropertyAbsenceHelper absenceHelper = context.getAssignAbsenceInformation() ? new ActivePropertyAbsenceHelper(context)
					: InactivePropertyAbsenceHelper.instance;

			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				String propertyName = entry.getKey();
				JsonNode propertyNode = entry.getValue();
				boolean propertyIsAbsent = false;
				// absent property?
				if (propertyName.charAt(0) == '?') {
					propertyName = propertyName.substring(1);
					propertyIsAbsent = true;
				}

				Property property = entityType.findProperty(propertyName);
				Object propertyValue = decodeJson(context, propertyNode);

				// tolerant behavior if property is missing in order to stay compatible between model versions
				if (property != null) {
					absenceHelper.addPresent(property);

					if (propertyIsAbsent) {
						property.setAbsenceInformation(entity, (AbsenceInformation) propertyValue);
					} else {
						property.setDirectUnsafe(entity, propertyValue);
					}
				}
			}

			absenceHelper.ensureAbsenceInformation(entityType, entity);
			return entity;
		} catch (Exception e) {
			throw new CodecException("error while decoding entity", e);
		}
	}

	private static abstract class PropertyAbsenceHelper {

		public abstract void addPresent(Property property);
		public abstract void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity);
	}

	private static class ActivePropertyAbsenceHelper extends PropertyAbsenceHelper {
		private final Set<Property> presentProperties = new HashSet<Property>();
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
			// noop
		}

		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			// noop
		}
	}

	@Override
	public JsonNode encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public JsonNode encode(T value, GmSerializationOptions options) throws CodecException {
		EncodingContext context = new EncodingContext();
		return encodeJsonAndConcretizeType(context, value, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getValueClass() {
		if (type != null) {
			return (Class<T>) type.getJavaType();
		} else
			return null;
	}

	protected JsonNode encodeJson(EncodingContext context, Object value) throws CodecException {
		return encodeJson(context, value, genericModelTypeReflection.getType(value));
	}

	protected JsonNode encodeJsonAndConcretizeType(EncodingContext context, Object value, GenericModelType type) throws CodecException {
		GenericModelType actualType = type.getActualType(value);
		return encodeJson(context, value, actualType);
	}

	protected JsonNode encodeJson(EncodingContext context, Object value, GenericModelType type) throws CodecException {
		if (value == null) {
			return nodeFactory.nullNode();
		} else {
			switch (type.getTypeCode()) {
				case booleanType:
					return nodeFactory.booleanNode((Boolean) value);
				case dateType:
					return typedNode(nodeFactory.textNode(dateCodec.encode((Date) value)), "date");
				case decimalType:
					return typedNode(nodeFactory.textNode(((BigDecimal) value).toString()), "decimal");
				case doubleType:
					return typedNode(nodeFactory.numberNode((Double) value), "double");
				case entityType:
					EntityType<GenericEntity> entityType = type.cast();
					return encodeJson(context, (GenericEntity) value, entityType);
				case enumType:
					return typedNode(nodeFactory.textNode(((Enum<?>) value).name()), type.getTypeSignature());
				case floatType:
					return typedNode(nodeFactory.numberNode((Float) value), "float");
				case integerType:
					return nodeFactory.numberNode((Integer) value);
				case listType:
					GenericModelType listElementType = ((CollectionType) type).getCollectionElementType();
					@SuppressWarnings("unchecked")
					List<Object> list = (List<Object>) value;
					ArrayNode listNode = nodeFactory.arrayNode();
					for (Object listElement : list) {
						listNode.add(encodeJsonAndConcretizeType(context, listElement, listElementType));
					}
					return listNode;
				case longType:
					return typedNode(nodeFactory.textNode(value.toString()), "long");
				case mapType:
					GenericModelType types[] = ((CollectionType) type).getParameterization();
					GenericModelType mapKeyType = types[0];
					GenericModelType mapValueType = types[1];
					@SuppressWarnings("unchecked")
					Map<Object, Object> map = (Map<Object, Object>) value;
					ArrayNode mapNode = nodeFactory.arrayNode();
					for (Map.Entry<Object, Object> entry : map.entrySet()) {
						Object mapKey = entry.getKey();
						Object mapValue = entry.getValue();
						ObjectNode entryNode = nodeFactory.objectNode();
						entryNode.set("key", encodeJsonAndConcretizeType(context, mapKey, mapKeyType));
						entryNode.set("value", encodeJsonAndConcretizeType(context, mapValue, mapValueType));
						mapNode.add(entryNode);
					}
					return typedNode(mapNode, "map");
				case objectType:
					return encodeJsonAndConcretizeType(context, value, type);
				case setType:
					GenericModelType setElementType = ((CollectionType) type).getCollectionElementType();
					@SuppressWarnings("unchecked")
					Set<Object> set = (Set<Object>) value;
					ArrayNode setListNode = nodeFactory.arrayNode();
					for (Object setElement : set) {
						setListNode.add(encodeJsonAndConcretizeType(context, setElement, setElementType));
					}
					return typedNode(setListNode, "set");
				case stringType:
					return nodeFactory.textNode((String) value);
				default:
					throw new CodecException("unsupported type " + type.getTypeSignature());
			}
		}
	}

	protected <G extends GenericEntity> JsonNode encodeJson(EncodingContext context, G entity, EntityType<G> type) throws CodecException {
		Integer refId = context.lookupId(entity);

		if (refId != null) {
			// encode reference
			ObjectNode jsonObject = nodeFactory.objectNode();
			jsonObject.set("_ref", nodeFactory.textNode(refId.toString()));
			return jsonObject;
		} else {
			try {
				// encode entity
				refId = context.register(entity);
				ObjectNode jsonObject = nodeFactory.objectNode();
				jsonObject.put("_id", refId.toString());

				jsonObject.put("_type", type.getTypeSignature());

				boolean partial = false;

				for (Property property : type.getProperties()) {

					String propertyName = property.getName();

					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					// Partial Representation needed here?
					if (absenceInformation != null) {
						if (writeAbsenceInformation) {
							EntityType<AbsenceInformation> aiType = absenceInformation.entityType();
							jsonObject.put('?' + propertyName, encodeJson(context, absenceInformation, aiType));
						}
						partial = true;
					} else {
						// normal value serializing here
						Object value = property.get(entity);
						JsonNode jsonNode = encodeJsonAndConcretizeType(context, value, property.getType());
						jsonObject.put(propertyName, jsonNode);
					}

				}

				if (partial)
					jsonObject.put("_partial", Boolean.TRUE.toString());

				return jsonObject;
			} catch (CodecException e) {
				throw e;
			} catch (Exception e) {
				throw new CodecException("error while encoding entity", e);
			}
		}
	}

	protected ObjectNode typedNode(JsonNode value, String type) {
		ObjectNode typedNode = nodeFactory.objectNode();
		typedNode.set("_type", nodeFactory.textNode(type));
		typedNode.set("value", value);
		return typedNode;
	}

	private static class EncodingContext {
		private final Map<GenericEntity, Integer> idByEntities = new HashMap<GenericEntity, Integer>();
		private int idSequence = 0;

		public Integer register(GenericEntity entity) {
			Integer id = idSequence++;
			idByEntities.put(entity, id);
			return id;
		}

		public Integer lookupId(GenericEntity entity) {
			return idByEntities.get(entity);
		}
	}

	private static class DecodingContext {
		private final Map<String, GenericEntity> entitiesById = new HashMap<String, GenericEntity>();
		private final AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
		private final boolean assignAbsenceInformation;
		private final boolean enhance;
		private final GmSession session;

		public DecodingContext(GmDeserializationOptions options, boolean enhance) {
			this.enhance = enhance;
			this.assignAbsenceInformation = options.getAbsentifyMissingProperties();
			this.session = options.getSession();
		}

		public boolean getAssignAbsenceInformation() {
			return assignAbsenceInformation;
		}

		public void register(GenericEntity entity, String id) {
			entitiesById.put(id, entity);
		}

		public AbsenceInformation getAbsenceInformationForMissingProperties() {
			return absenceInformationForMissingProperties;
		}

		@SuppressWarnings("unchecked")
		public <T extends GenericEntity> T lookupEntity(String id) {
			return (T) entitiesById.get(id);
		}

		@SuppressWarnings("unchecked")
		public <T extends GenericEntity> T createRaw(EntityType<?> entityType) {
			return (T) (session != null ? session.createRaw(entityType) : enhance ? entityType.createRaw() : entityType.createPlainRaw());
		}
	}

}
