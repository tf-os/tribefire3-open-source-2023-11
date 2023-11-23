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
package com.braintribe.codec.marshaller.stax;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.EntityCollector;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.stabilization.FNVHash;
import com.braintribe.codec.marshaller.stabilization.MapKeyComparator;
import com.braintribe.codec.marshaller.stabilization.StabilizationComparators;
import com.braintribe.codec.marshaller.stabilization.SyntacticalElements;
import com.braintribe.codec.marshaller.stax.tree.AbsenceInformationNode;
import com.braintribe.codec.marshaller.stax.tree.DateNode;
import com.braintribe.codec.marshaller.stax.tree.DecimalNode;
import com.braintribe.codec.marshaller.stax.tree.DoubleNode;
import com.braintribe.codec.marshaller.stax.tree.EntityNode;
import com.braintribe.codec.marshaller.stax.tree.EntityReferenceNode;
import com.braintribe.codec.marshaller.stax.tree.EnumNode;
import com.braintribe.codec.marshaller.stax.tree.FloatNode;
import com.braintribe.codec.marshaller.stax.tree.IntegerNode;
import com.braintribe.codec.marshaller.stax.tree.ListNode;
import com.braintribe.codec.marshaller.stax.tree.LongNode;
import com.braintribe.codec.marshaller.stax.tree.MapNode;
import com.braintribe.codec.marshaller.stax.tree.SetNode;
import com.braintribe.codec.marshaller.stax.tree.StaxNodes;
import com.braintribe.codec.marshaller.stax.tree.StringNode;
import com.braintribe.codec.marshaller.stax.tree.ValueStaxNode;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.MapType;
import com.braintribe.model.generic.reflection.Property;

public class EncodingContext extends EntityCollector {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private final Map<GenericEntity, String> qualifiedIdByEntities = new HashMap<>();
	private final EntityQueueNode anchorNode = new EntityQueueNode();
	private EntityQueueNode lastNode = anchorNode;
	private final Set<GenericModelType> requiredGenericModelTypes = new HashSet<>();
	private final Map<GenericModelType, TypeInfo> requiredTypes = new HashMap<>();
	private final GmSerializationOptions options;
	private boolean writeAbsenceInformation = true;
	private final boolean stabilizeOrder;
	private final Map<GenericEntity, EntityInfo> entityInfos = new LinkedHashMap<>();
	private final Set<String> aliases = new HashSet<>();
	private final Consumer<? super GenericEntity> entityVisitor;

	public EncodingContext(GmSerializationOptions options) {
		this.options = options;
		this.stabilizeOrder = options.stabilizeOrder();
		this.entityVisitor = options.findOrNull(EntityVisitorOption.class);
		// setDirectPropertyAccess(true);
	}

	public boolean isStabilizingOrder() {
		return stabilizeOrder;
	}

	public boolean isSimpleAbsenceInformation(AbsenceInformation absenceInformation) {
		return absenceInformation.getSize() == null;
	}

	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}

	public void registerRequiredGenericModelType(GenericModelType requiredType) {
		requiredGenericModelTypes.add(requiredType);
	}

	@Override
	protected void add(Enum<?> constant, EnumType type) {
		registerRequiredType(type);
	}

	public Map<GenericEntity, EntityInfo> getEntityInfos() {
		return entityInfos;
	}

	@Override
	protected boolean add(GenericEntity entity, EntityType<?> type) {
		EntityInfo entityInfo = entityInfos.get(entity);

		if (entityInfo == null) {
			entityInfo = new EntityInfo();
			TypeInfo typeInfo = registerRequiredType(type);
			entityInfo.typeInfo = typeInfo;
			entityInfo.refId = typeInfo.nextId(entity);
			entityInfos.put(entity, entityInfo);
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings("unused")
	public EntityInfo lookupEntityInfo(GenericEntity entity) throws MarshallException {
		return entityInfos.get(entity);
	}

	private static String condensationOfTypeSignature(GenericModelType type) {
		String typeSignature = type.getTypeSignature();
		int i = FNVHash.hash32(typeSignature);
		int index = typeSignature.lastIndexOf('.');
		String shortName = typeSignature.substring(index + 1);
		return shortName + SyntacticalElements.hashDelimiter + asString(i);
	}

	private static char[] digits = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$@".toCharArray();

	private static String asString(int i) {
		char[] buf = new char[32];
		int charPos = 32;
		int radix = 1 << 6;
		int mask = radix - 1;
		do {
			buf[--charPos] = digits[i & mask];
			i >>>= 6;
		} while (i != 0);

		return new String(buf, charPos, (32 - charPos));
	}

	public TypeInfo registerRequiredType(GenericModelType type) {
		TypeInfo typeInfo = requiredTypes.get(type);

		if (typeInfo == null) {
			typeInfo = stabilizeOrder ? new StabilizedTypeInfo() : new TypeInfo();
			typeInfo.type = type;
			String alias = nextAlias(type);
			typeInfo.as = alias;
			typeInfo.alias = alias;
			requiredTypes.put(type, typeInfo);
		}

		return typeInfo;
	}

	private String nextAlias(GenericModelType type) {
		String alias = condensationOfTypeSignature(type);
		if (!aliases.add(alias)) {
			int i = 1;
			String alternativeAlias = null;
			do {
				alternativeAlias = alias + SyntacticalElements.hashDelimiter + i++;
			} while (!aliases.add(alternativeAlias));
			alias = alternativeAlias;
		}
		return alias;

		/* // take an alias from a sequence StringBuilder builder = new StringBuilder(); for (int i = 0; i < aliasDigitCount;
		 * i++) { builder.append(aliasDigits.charAt(aliasSequence[i])); }
		 * 
		 * for (int i = 0; i < aliasSequence.length; i++) { byte num = aliasSequence[i]; if (++num == aliasDigits.length()) {
		 * aliasSequence[i] = 0; } else { aliasSequence[i] = num; int countCandidate = i + 1; if (countCandidate >
		 * aliasDigitCount) aliasDigitCount = countCandidate; break; } }
		 * 
		 * return builder.toString(); } */
	}

	public Set<GenericModelType> getRequiredGenericModelTypes() {
		return requiredGenericModelTypes;
	}

	public EntityQueueNode getFirstNode() {
		return anchorNode.next;
	}

	@SuppressWarnings("unused")
	public String lookupQualifiedId(GenericEntity entity) throws MarshallException {
		String id = qualifiedIdByEntities.get(entity);

		if (id == null) {
			if (entityVisitor != null)
				entityVisitor.accept(entity);

			EntityType<?> entityType = entity.entityType();
			TypeInfo typeInfo = registerRequiredType(entityType);

			id = typeInfo.nextId(entity);
			qualifiedIdByEntities.put(entity, id);

			EntityQueueNode node = new EntityQueueNode();
			node.typeInfo = typeInfo;
			node.entity = entity;
			node.refId = id;

			lastNode.next = node;
			lastNode = node;
		}

		return id;
	}

	public EntityNode encodeEntity(EntityQueueNode entityQueueNode) throws MarshallException {
		try {
			GenericEntity entity = entityQueueNode.entity;

			TypeInfo typeInfo = entityQueueNode.typeInfo;
			String id = entityQueueNode.refId;

			EntityType<?> entityType = (EntityType<?>) typeInfo.type;

			List<Property> properties = entityType.getProperties();
			int propertyCount = properties.size();

			String propertyNames[] = new String[propertyCount];
			ValueStaxNode propertyValueNodes[] = new ValueStaxNode[propertyCount];

			boolean dap = options.useDirectPropertyAccess();

			int i = 0;
			for (Property property : properties) {
				Object value = dap ? property.getDirectUnsafe(entity) : property.get(entity);

				if (value == null) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					if (absenceInformation != null) {
						propertyNames[i] = property.getName();
						if (isSimpleAbsenceInformation(absenceInformation))
							propertyValueNodes[i] = StaxNodes.shortabsenceInformationNode;
						else
							propertyValueNodes[i] = new AbsenceInformationNode(lookupQualifiedId(absenceInformation));
						i++;
					}
				} else {
					ValueStaxNode valueNode = encodeValue(property.getType(), value, true);

					if (valueNode != null) {
						propertyValueNodes[i] = valueNode;
						propertyNames[i] = property.getName();
						i++;
					}
				}
			}

			return new EntityNode(entity, id, propertyNames, propertyValueNodes, i);

		} catch (GenericModelException e) {
			throw new MarshallException("error while encoding entity", e);
		}

	}

	public ValueStaxNode encodeValue(GenericModelType type, Object value, boolean returnNullForEmptyCollections) throws MarshallException {
		if (value == null)
			return StaxNodes.nullNode;

		switch (type.getTypeCode()) {
			// object type
			case objectType:
				return encodeValue(type.getActualType(value), value, false);

			// simple types
			case booleanType:
				return ((Boolean) value) ? StaxNodes.trueNode : StaxNodes.falseNode;
			case dateType:
				return new DateNode((Date) value);
			case decimalType:
				return new DecimalNode((BigDecimal) value);
			case doubleType:
				return new DoubleNode((Double) value);
			case floatType:
				return new FloatNode((Float) value);
			case integerType:
				return new IntegerNode((Integer) value);
			case longType:
				return new LongNode((Long) value);
			case stringType:
				return new StringNode((String) value);

			// collections
			case listType:
				return encodeList((CollectionType) type, (List<?>) value, returnNullForEmptyCollections);
			case setType:
				return encodeSet((CollectionType) type, (Set<?>) value, returnNullForEmptyCollections);
			case mapType:
				return encodeMap((MapType) type, (Map<?, ?>) value, returnNullForEmptyCollections);

			// custom types
			case entityType:
				return new EntityReferenceNode(lookupQualifiedId((GenericEntity) value));
			case enumType:
				return new EnumNode(registerRequiredType(typeReflection.getType((Enum<?>) value)), (Enum<?>) value);

			default:
				throw new MarshallException("unkown type: " + type);

		}
	}

	private ValueStaxNode encodeList(CollectionType type, List<?> list, boolean returnNullForEmptyCollections) throws MarshallException {
		int l = list.size();

		if (l == 0)
			return returnNullForEmptyCollections ? null : new ListNode(ValueStaxNode.EMPTY_NODES);

		ValueStaxNode elementNodes[] = new ValueStaxNode[l];
		GenericModelType elementType = type.getCollectionElementType();

		for (int i = 0; i < l; i++) {
			elementNodes[i] = encodeValue(elementType, list.get(i), returnNullForEmptyCollections);
		}

		return new ListNode(elementNodes);
	}

	private ValueStaxNode encodeSet(CollectionType type, Set<?> set, boolean returnNullForEmptyCollections) throws MarshallException {
		int l = set.size();

		if (l == 0)
			return returnNullForEmptyCollections ? null : new SetNode(ValueStaxNode.EMPTY_NODES);

		Collection<?> elements = set;

		if (stabilizeOrder) {
			List<Object> list = new ArrayList<>(elements);

			@SuppressWarnings("unchecked")
			Comparator<Object> comparator = (Comparator<Object>) StabilizationComparators.comparator(type.getCollectionElementType());
			Collections.sort(list, comparator);
			elements = list;
		}

		ValueStaxNode elementNodes[] = new ValueStaxNode[l];
		GenericModelType elementType = type.getCollectionElementType();

		int i = 0;
		for (Object value : elements) {
			elementNodes[i++] = encodeValue(elementType, value, returnNullForEmptyCollections);
		}

		return new SetNode(elementNodes);
	}

	private ValueStaxNode encodeMap(MapType type, Map<?, ?> map, boolean returnNullForEmptyCollections) throws MarshallException {
		int l = map.size();

		if (l == 0)
			return returnNullForEmptyCollections ? null : new MapNode(ValueStaxNode.EMPTY_NODES);

		@SuppressWarnings("unchecked")
		Map<Object, Object> castedMap = (Map<Object, Object>) map;
		Collection<? extends Map.Entry<Object, Object>> entries = castedMap.entrySet();

		if (stabilizeOrder) {
			List<Map.Entry<Object, Object>> list = new ArrayList<>(entries);
			Comparator<Object> comparator = (Comparator<Object>) StabilizationComparators.comparator(type.getKeyType());
			Collections.sort(list, new MapKeyComparator<>(comparator));

			entries = list;
		}

		ValueStaxNode elementNodes[] = new ValueStaxNode[l * 2];
		GenericModelType keyType = type.getKeyType();
		GenericModelType valueType = type.getValueType();

		int i = 0;
		for (Map.Entry<?, ?> entry : entries) {
			elementNodes[i++] = encodeValue(keyType, entry.getKey(), returnNullForEmptyCollections);
			elementNodes[i++] = encodeValue(valueType, entry.getValue(), returnNullForEmptyCollections);
		}

		return new MapNode(elementNodes);
	}

	public boolean shouldWriteAbsenceInformation() {
		return writeAbsenceInformation;
	}

	public Collection<TypeInfo> getRequiredTypeInfos() {
		return requiredTypes.values();
	}
}
