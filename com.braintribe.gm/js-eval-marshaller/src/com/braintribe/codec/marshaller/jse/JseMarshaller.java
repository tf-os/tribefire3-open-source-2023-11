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
package com.braintribe.codec.marshaller.jse;

import static com.braintribe.utils.lcd.CollectionTools2.acquireMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.jse.tree.JseCreate;
import com.braintribe.codec.marshaller.jse.tree.JseNode;
import com.braintribe.codec.marshaller.jse.tree.JsePoolAddress;
import com.braintribe.codec.marshaller.jse.tree.JsePropertyAbsenceAssignment;
import com.braintribe.codec.marshaller.jse.tree.JsePropertyAssignment;
import com.braintribe.codec.marshaller.jse.tree.JsePropertyStandardAbsenceAssignment;
import com.braintribe.codec.marshaller.jse.tree.JseResolveEntityType;
import com.braintribe.codec.marshaller.jse.tree.JseResolveEnum;
import com.braintribe.codec.marshaller.jse.tree.JseResolveEnumType;
import com.braintribe.codec.marshaller.jse.tree.JseResolveProperty;
import com.braintribe.codec.marshaller.jse.tree.JseReturn;
import com.braintribe.codec.marshaller.jse.tree.JseScript;
import com.braintribe.codec.marshaller.jse.tree.JseStatementSequence;
import com.braintribe.codec.marshaller.jse.tree.JseTmpVarAssignment;
import com.braintribe.codec.marshaller.jse.tree.value.JseDate;
import com.braintribe.codec.marshaller.jse.tree.value.JseDecimal;
import com.braintribe.codec.marshaller.jse.tree.value.JseDouble;
import com.braintribe.codec.marshaller.jse.tree.value.JseFloat;
import com.braintribe.codec.marshaller.jse.tree.value.JseHostedModeDate;
import com.braintribe.codec.marshaller.jse.tree.value.JseHostedModeLong;
import com.braintribe.codec.marshaller.jse.tree.value.JseInteger;
import com.braintribe.codec.marshaller.jse.tree.value.JseList;
import com.braintribe.codec.marshaller.jse.tree.value.JseLong;
import com.braintribe.codec.marshaller.jse.tree.value.JseMap;
import com.braintribe.codec.marshaller.jse.tree.value.JseSet;
import com.braintribe.codec.marshaller.jse.tree.value.JseString;
import com.braintribe.codec.marshaller.jse.tree.value.JseStringMap;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.utils.IOTools;

public class JseMarshaller implements CharacterMarshaller, HasStringCodec, GmCodec<Object, String> {
	private static final Logger logger = Logger.getLogger(JseMarshaller.class);
	private boolean hostedMode = false;

	public void setHostedMode(boolean hostedMode) {
		this.hostedMode = hostedMode;
	}

	@Override
	public void marshall(Writer writer, Object value) throws MarshallException {
		marshall(writer, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		EncodingContext encodingContext = new EncodingContext(hostedMode, writer, options);
		encodingContext.assemble(value);
	}

	@Override
	public Object unmarshall(Reader reader) throws MarshallException {
		throw new UnsupportedOperationException("no unmarshall supported");
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		throw new UnsupportedOperationException("no unmarshall supported");
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		throw new UnsupportedOperationException("no unmarshall supported");
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, value, options);
			writer.flush();
		} catch (Exception e) {
			throw new MarshallException("error while encoding value", e);
		}
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(in);
	}

	@Override
	public GmCodec<Object, String> getStringCodec() {
		return this;
	}

	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}

	@Override
	public String encode(Object value, GmSerializationOptions options) throws CodecException {
		return encode(value);
	}

	@Override
	public Object decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		return decode(encodedValue);
	}

	@Override
	public String encode(Object value) throws CodecException {
		StringWriter writer = null;
		try {
			writer = new StringWriter();
			marshall(writer, value);
			return writer.toString();
		} catch (MarshallException e) {
			throw new CodecException("error while encoding value", e);
		} finally {
			IOTools.closeCloseable(writer, logger);
		}
	}

	@Override
	public Object decode(String encodedValue) throws CodecException {
		throw new UnsupportedOperationException("no implemented");
	}

	private static class EntityListNode {
		public JseEntityTypePreparation preparation;
		public GenericEntity entity;
		public JsePoolAddress poolAddress;
		public EntityListNode successor;
	}

	static class EncodingContext extends JseEncodingContext {
		private static final GenericModelType baseType = GMF.getTypeReflection().getBaseType();
		private Map<EntityType<?>, JseEntityTypeRegistration> entityTypes = new HashMap<EntityType<?>, JseEntityTypeRegistration>();
		private Map<Object, JsePoolAddress> enums = new HashMap<Object, JsePoolAddress>();
		private Map<EnumType, JsePoolAddress> enumTypes = new HashMap<EnumType, JsePoolAddress>();
		private Map<Property, JsePoolAddress> properties = new HashMap<Property, JsePoolAddress>();
		private Map<JsePoolAddress, Map<Property, JsePoolAddress>> _properties = new HashMap<>();
		private Map<GenericEntity, JsePoolAddress> entities = new HashMap<GenericEntity, JsePoolAddress>();
		private EntityListNode anchorNode = new EntityListNode();
		private EntityListNode lastEntityNode = anchorNode;
		private boolean hostedMode;
		private PoolAddressSequence globalSequence = new PropertyPoolAddressSequence('P');
		private PoolAddressSequence typesSequence = globalSequence;
		private PoolAddressSequence propertiesSequence = globalSequence;
		private PoolAddressSequence constantsSequence = globalSequence;
		private PoolAddressSequence entitiesSequence = globalSequence;
		private Writer writer;
		private GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
		private Consumer<? super GenericEntity> entityVisitor;

		public EncodingContext(boolean hostedMode, Writer writer, GmSerializationOptions options) {
			super();
			this.writer = writer;
			this.hostedMode = hostedMode;
			this.entityVisitor = options.findOrNull(EntityVisitorOption.class);

			typesSection.append(JseNodeSingletons.beginTypesComment);
		}

		private JseScript typesSection = new JseScript();
		private JseStatementSequence propertiesSection = new JseStatementSequence();
		private JseStatementSequence enumSection = new JseStatementSequence();
		private JseStatementSequence instantiationSection = new JseStatementSequence();
		private JseScript assemblySection = new JseScript();

		public boolean isHostedMode() {
			return hostedMode;
		}

		private JseEntityTypeRegistration aquireEntityTypeAddress(EntityType<?> type) throws MarshallException {
			JseEntityTypeRegistration registration = entityTypes.get(type);
			if (registration == null) {
				JsePoolAddress poolAddress = typesSequence.createAddress();

				registration = new JseEntityTypeRegistration();
				registration.poolAddress = poolAddress;
				registration.preparation = new JseEntityTypePreparation(this, type, poolAddress);

				entityTypes.put(type, registration);

				typesSection.append(new JseResolveEntityType(poolAddress, new JseString(type.getTypeSignature())));
			}

			return registration;
		}

		private JsePoolAddress aquireEnumAddress(EnumType type, Object value) throws MarshallException {
			JsePoolAddress enumVarName = enums.get(value);
			if (enumVarName == null) {
				enumVarName = constantsSequence.createAddress();
				enums.put(value, enumVarName);

				JsePoolAddress typeAddress = aquireEnumTypeAddress(type);
				JseResolveEnum jseResolveEnum = new JseResolveEnum(enumVarName, typeAddress, value);

				enumSection.append(jseResolveEnum);
			}

			return enumVarName;
		}

		private JsePoolAddress aquireEnumTypeAddress(EnumType type) throws MarshallException {
			JsePoolAddress typeVarName = enumTypes.get(type);
			if (typeVarName == null) {
				typeVarName = typesSequence.createAddress();

				enumTypes.put(type, typeVarName);

				typesSection.append(new JseResolveEnumType(typeVarName, new JseString(type.getTypeSignature())));
			}

			return typeVarName;
		}

		/* (non-Javadoc)
		 * 
		 * @see com.braintribe.codec.marshaller.jse.JseEncodingContext#aquireEntityVarName(com.braintribe.model.generic.
		 * GenericEntity) */
		private JsePoolAddress aquireEntityAddress(GenericEntity entity) throws MarshallException {
			JsePoolAddress entityVarName = entities.get(entity);
			if (entityVarName == null) {
				if (entityVisitor != null)
					entityVisitor.accept(entity);
				JseEntityTypeRegistration entityTypeRegistration = aquireEntityTypeAddress(entity.entityType());

				entityVarName = entitiesSequence.createAddress();

				instantiationSection.append(new JseCreate(entityVarName, entityTypeRegistration.poolAddress));

				registerEntity(entity, entityVarName, entityTypeRegistration.preparation);
			}

			return entityVarName;
		}

		private void registerEntity(GenericEntity entity, JsePoolAddress entityVarName, JseEntityTypePreparation preparation) {
			entities.put(entity, entityVarName);

			EntityListNode node = new EntityListNode();
			node.poolAddress = entityVarName;
			node.entity = entity;
			node.preparation = preparation;

			lastEntityNode.successor = node;
			lastEntityNode = node;
		}

		@Override
		public JsePoolAddress aquirePropertyAddress(JsePoolAddress ownerTypeAddress, Property property) throws MarshallException {
			// JsePoolAddress propertyVarName = properties.get(property);
			JsePoolAddress propertyVarName = acquireMap(_properties, ownerTypeAddress).get(property);
			if (propertyVarName == null) {
				propertyVarName = propertiesSequence.createAddress();
				properties.put(property, propertyVarName);

				propertiesSection.append(new JseResolveProperty(propertyVarName, ownerTypeAddress, property.getName()));
			}

			return propertyVarName;
		}

		private void encodeEntityWiring(JseScript script, JsePoolAddress address, JseEntityTypePreparation preparation, GenericEntity entity)
				throws MarshallException {
			final JsePoolAddress entityVarName = aquireEntityAddress(entity);
			boolean first = true;
			for (JsePropertyPreparation propertyPreparation : preparation.preparations) {
				Property property = propertyPreparation.property;
				final AbsenceInformation ai = property.getAbsenceInformation(entity);

				if (ai != null) {
					if (first) {
						first = false;
						script.append(new JseTmpVarAssignment(entityVarName));
					}

					if (isStandardAbsenceInformation(ai)) {
						script.append(new JsePropertyStandardAbsenceAssignment(propertyPreparation.getPoolAddress()), false);
					} else {
						JsePoolAddress absenceInformationVarName = aquireEntityAddress(ai);
						script.append(new JsePropertyAbsenceAssignment(propertyPreparation.getPoolAddress(), absenceInformationVarName), false);
					}
				} else {
					Object value = property.get(entity);

					if (value != null) {
						JseNode valueNode = buildJseNode(property.getType(), value, true);

						if (valueNode != null) {
							if (first) {
								first = false;
								script.append(new JseTmpVarAssignment(entityVarName));
							}

							JseNode setPropertyNode = new JsePropertyAssignment(propertyPreparation.getPoolAddress(), valueNode);

							script.append(setPropertyNode, false);
						}
					}
				}

			}
		}

		private AbsenceInformation lastCheckedStandardAbsenceInformation;

		private boolean isStandardAbsenceInformation(AbsenceInformation ai) {
			if (lastCheckedStandardAbsenceInformation == ai)
				return true;
			else {
				if (typeReflection.getType(ai) == AbsenceInformation.T && ai.getSize() == null) {
					lastCheckedStandardAbsenceInformation = ai;
					return true;
				} else
					return false;
			}
		}

		public void assemble(Object value) throws MarshallException {
			JseScript valueSection = new JseScript();

			// actual value encoding
			valueSection.append(new JseReturn(buildJseNode(baseType, value, false)));

			EntityListNode node = anchorNode.successor;

			while (node != null) {
				GenericEntity entity = node.entity;
				encodeEntityWiring(assemblySection, node.poolAddress, node.preparation, entity);
				node = node.successor;
			}

			typesSection.append(JseNodeSingletons.endTypesComment);

			try {
				CountingWriter countingWriter = new CountingWriter(writer, 25000);

				countingWriter.write("//JSE version=4.0");
				// write scripts
				typesSection.write(countingWriter);
				propertiesSection.write(countingWriter);
				enumSection.write(countingWriter);
				instantiationSection.write(countingWriter);
				assemblySection.write(countingWriter);
				valueSection.write(countingWriter);

				List<Integer> splitPoints = countingWriter.getSplitPoints();
				int end = countingWriter.getCount();
				countingWriter.write("\n[");
				for (Integer i : splitPoints) {
					countingWriter.write(i.toString());
					countingWriter.write(',');
				}
				countingWriter.write(String.valueOf(end));
				countingWriter.write("];");
			} catch (Exception e) {
				throw new MarshallException("error while writing JseScript", e);
			}
		}

		private JseNode buildJseNode(GenericModelType type, Object value, boolean returnNullOnEmptyCollections) throws MarshallException {
			if (value == null)
				return JseNodeSingletons.nullValue;

			while (true) {
				switch (type.getTypeCode()) {
					// object type
					case objectType:
						type = baseType.getActualType(value);
						break;

					// simple types
					case stringType:
						return new JseString((String) value);
					case booleanType:
						return ((Boolean) value) ? JseNodeSingletons.trueValue : JseNodeSingletons.falseValue;
					case decimalType:
						return new JseDecimal(value);
					case doubleType:
						return new JseDouble(value);
					case floatType:
						return new JseFloat(value);
					case integerType:
						return new JseInteger(value);
					case longType:
						return hostedMode ? new JseHostedModeLong(value) : new JseLong((Long) value);
					case dateType:
						return hostedMode ? new JseHostedModeDate((Date) value) : new JseDate((Date) value);

					// collection types
					case setType:
						return buildSet((CollectionType) type, (Collection<?>) value, returnNullOnEmptyCollections);
					case listType:
						return buildList((CollectionType) type, (Collection<?>) value, returnNullOnEmptyCollections);
					case mapType:
						return buildMap((CollectionType) type, (Map<?, ?>) value, returnNullOnEmptyCollections);

					// custom types
					case entityType:
						return aquireEntityAddress((GenericEntity) value);
					case enumType:
						return aquireEnumAddress((EnumType) type, value);

					default:
						throw new IllegalArgumentException("unsupported type " + type);
				}
			}
		}

		private JseNode buildMap(CollectionType type, Map<?, ?> map, boolean returnNullOnEmptyCollection) throws MarshallException {
			if (returnNullOnEmptyCollection && map.isEmpty())
				return null;

			JseNode elements[] = new JseNode[map.size() * 2];
			GenericModelType[] parameterization = type.getParameterization();
			GenericModelType keyType = parameterization[0];
			GenericModelType valueType = parameterization[1];
			int i = 0;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				elements[i++] = buildJseNode(keyType, entry.getKey(), false);
				elements[i++] = buildJseNode(valueType, entry.getValue(), false);
			}

			if (keyType.getTypeCode() == TypeCode.stringType) {
				return new JseStringMap(elements);
			} else {
				return new JseMap(elements);
			}
		}

		private JseNode buildList(CollectionType type, Collection<?> value, boolean returnNullOnEmptyCollection) throws MarshallException {
			if (returnNullOnEmptyCollection && value.isEmpty())
				return null;

			JseNode elements[] = new JseNode[value.size()];
			GenericModelType elementType = type.getCollectionElementType();
			int i = 0;
			for (Object e : value) {
				elements[i++] = buildJseNode(elementType, e, false);
			}
			return new JseList(elements);
		}

		private JseNode buildSet(CollectionType type, Collection<?> value, boolean returnNullOnEmptyCollection) throws MarshallException {
			if (returnNullOnEmptyCollection && value.isEmpty())
				return null;

			JseNode elements[] = new JseNode[value.size()];
			GenericModelType elementType = type.getCollectionElementType();
			int i = 0;
			for (Object e : value) {
				elements[i++] = buildJseNode(elementType, e, false);
			}

			return new JseSet(elements);
		}
	}

}
