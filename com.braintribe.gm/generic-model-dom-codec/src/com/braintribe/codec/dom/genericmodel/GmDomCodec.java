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
package com.braintribe.codec.dom.genericmodel;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.xml.XmlTools;

public class GmDomCodec<T> implements GmCodec<T, Document> {
	private static DateTimeFormatter legacyDateFormat = DateTools.LEGACY_DATETIME_FORMAT;
	private static DateTimeFormatter dateFormat = DateTools.ISO8601_DATE_WITH_MS_FORMAT; // DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	private GenericModelType type;
	protected GenericModelTypeReflection genericModelTypeReflection = GMF.getTypeReflection();
	private static DocumentBuilder documentBuilder = null;
	private static Map<String, Decoder> decoderMap = new HashMap<>();

	private boolean writeAbsenceInformation = true;
	private boolean createEnhancedEntities = true;
	private boolean assignAbsenceInformationForMissingProperties;
	private Integer encodingVersion = null;
	private Consumer<Set<String>> requiredTypesReceiver;

	private static EntityDecoder entityDecoder = new EntityDecoder();

	static {
		decoderMap.put("boolean", new BooleanDecoder());
		decoderMap.put("string", new StringDecoder());
		decoderMap.put("integer", new IntegerDecoder());
		decoderMap.put("long", new LongDecoder());
		decoderMap.put("float", new FloatDecoder());
		decoderMap.put("double", new DoubleDecoder());
		decoderMap.put("decimal", new DecimalDecoder());
		decoderMap.put("date", new DateDecoder());
		decoderMap.put("entity", entityDecoder);
		decoderMap.put("enum", new EnumDecoder());
		decoderMap.put("list", new ListDecoder());
		decoderMap.put("set", new SetDecoder());
		decoderMap.put("map", new MapDecoder());
		decoderMap.put("null", new NullDecoder());
	}

	protected static DocumentBuilder getDocumentBuilder() throws CodecException {
		if (documentBuilder == null) {
			try {
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new CodecException("error while retrieving DocumentBuilder", e);
			}
		}

		return documentBuilder;
	}

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
	}

	@Configurable
	public void setEncodingVersion(Integer encodingVersion) {
		this.encodingVersion = encodingVersion;
	}

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

	public GenericModelType getType() {
		if (type == null) {
			type = genericModelTypeReflection.getBaseType();
		}

		return type;
	}

	protected void decodeRequiredTypes(Element gmDataElement) throws CodecException {
		Element requiredTypesElement = XmlTools.getElementByPath(gmDataElement, "required-types");

		if (requiredTypesElement != null && requiredTypesReceiver != null) {
			Set<String> typeSignatures = new HashSet<>();
			Node child = requiredTypesElement.getFirstChild();

			while (child != null) {
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) child;
					if (childElement.getTagName().equals("type")) {
						String typeSignature = childElement.getTextContent();
						typeSignatures.add(typeSignature);
					}
				}

				child = child.getNextSibling();
			}

			if (typeSignatures.size() > 0)
				try {
					requiredTypesReceiver.accept(typeSignatures);
				} catch (Exception e) {
					throw new CodecException("error while propagating required types to the configured receiver", e);
				}
		}

	}

	@Override
	public T decode(Document document, GmDeserializationOptions options) throws CodecException {
		int version = getGmXmlVersion(document);
		DateTimeFormatter useDateFormat = version > 2 ? dateFormat : legacyDateFormat;
		DecodingContextImpl context = new DecodingContextImpl(genericModelTypeReflection, useDateFormat, options.getAbsentifyMissingProperties());

		Element rootValueElement = document.getDocumentElement();

		if (version > 2) {
			Element gmDataElement = document.getDocumentElement();

			decodeRequiredTypes(gmDataElement);

			Element rootElement = XmlTools.getElementByPath(gmDataElement, "root-value");
			if (rootElement == null)
				throw new CodecException("missing /root-value element");

			rootValueElement = getFirstChildElement(rootElement);

			if (rootValueElement == null)
				throw new CodecException("missing a gm value element below the /root-value element");

			Element poolElement = XmlTools.getElementByPath(gmDataElement, "pool");

			if (poolElement == null)
				throw new CodecException("missing /pool element");

			context.setPoolElement(poolElement);
		}

		@SuppressWarnings("unchecked")
		T result = (T) context.decode(rootValueElement);

		return result;
	}

	protected int getGmXmlVersion(Document document) throws CodecException {
		Node node = document.getFirstChild();

		while (node != null) {
			if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				ProcessingInstruction pi = (ProcessingInstruction) node;
				String target = pi.getTarget();

				if (target.equals("gm-xml")) {
					String rawDdata = pi.getData();
					Map<String, String> data = ProcessingInstructionParser.parseData(rawDdata);

					String encodedVersion = data.get("version");

					if (encodedVersion != null) {
						try {
							int version = Integer.parseInt(encodedVersion);
							return version;
						} catch (NumberFormatException e) {
							throw new CodecException("error while decoding gm-xml version", e);
						}
					}
				}
			}
			node = node.getNextSibling();
		}

		return 1;
	}

	@Override
	public Document encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public T decode(Document encodedValue) throws CodecException {
		return decode(encodedValue,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public Document encode(T value, GmSerializationOptions options) throws CodecException {
		EncodingContext context = new EncodingContext();
		Document document = getDocumentBuilder().newDocument();
		context.setDocument(document);

		int version = encodingVersion != null ? encodingVersion : GmXml.defaultVersion;

		ProcessingInstruction gmXmlPi = document.createProcessingInstruction("gm-xml", "version=\"" + version + "\"");
		document.appendChild(gmXmlPi);

		Node rootValueNode = document;
		Element requiredTypesElement = null;

		if (version > 2) {
			Element gmDataElement = document.createElement("gm-data");
			requiredTypesElement = document.createElement("required-types");
			Element rootValueElement = document.createElement("root-value");
			Element poolElement = document.createElement("pool");

			gmDataElement.appendChild(requiredTypesElement);
			gmDataElement.appendChild(rootValueElement);
			gmDataElement.appendChild(poolElement);

			document.appendChild(gmDataElement);

			rootValueNode = rootValueElement;

			context.setDateFormat(dateFormat);
			context.setPoolElement(poolElement);
		} else {
			context.setDateFormat(legacyDateFormat);
		}

		if (requiredTypesElement != null) {
			for (GenericModelType requiredType : context.getRequiredTypes()) {
				Element typeElement = document.createElement("type");
				Text text = document.createTextNode(requiredType.getTypeSignature());
				typeElement.appendChild(text);
				requiredTypesElement.appendChild(typeElement);
			}
		}

		rootValueNode.appendChild(encode(context, value, getType()));

		return document;
	}

	protected Element encode(EncodingContext context, Object value, GenericModelType type) throws CodecException {
		if (value == null) {
			return context.getDocument().createElement("null");
		}

		switch (type.getTypeCode()) {
			// base type
			case objectType:
				return encode(context, value, type.getActualType(value));

			// scalar types
			case dateType:
				return encodeScalarValue(context, type.getTypeName(), DateTools.encode((Date) value, context.getDateFormat()));
			case stringType:
			case booleanType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
				return encodeScalarValue(context, type.getTypeName(), value);

			// collections
			case listType:
			case setType:
				return encodeCollectionValue(context, (CollectionType) type, (Collection<?>) value);
			case mapType:
				return encodeMapValue(context, (CollectionType) type, (Map<?, ?>) value);

			// custom types
			case entityType:
				return encodeEntity(context, (GenericEntity) value, type.getActualType(value).<EntityType<?>> cast());

			case enumType:
				context.registerRequiredType(type);
				Element enumElement = encodeScalarValue(context, "enum", value);
				enumElement.setAttribute("type", type.getTypeSignature());
				return enumElement;
			default:
				throw new CodecException("unsupported GenericModelType " + type.getClass());
		}
	}

	protected Element encodeScalarValue(EncodingContext context, String tagName, Object value) {
		Document document = context.getDocument();
		Text text = document.createTextNode(value.toString());
		Element containerElement = document.createElement(tagName);
		containerElement.appendChild(text);
		return containerElement;
	}

	protected Element encodeCollectionValue(EncodingContext context, CollectionType collectionType, Collection<?> collection) throws CodecException {
		Document document = context.getDocument();
		Element containerElement = document.createElement(collectionType.getTypeName());
		GenericModelType elementType = collectionType.getCollectionElementType();

		for (Object value : collection) {
			containerElement.appendChild(encode(context, value, elementType));
		}

		return containerElement;
	}

	protected Element encodeMapValue(EncodingContext context, CollectionType collectionType, Map<?, ?> map) throws CodecException {
		Document document = context.getDocument();
		Element containerElement = document.createElement(collectionType.getTypeName());
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Element entryElement = document.createElement("entry");
			Element keyElement = document.createElement("key");
			Element valueElement = document.createElement("value");
			keyElement.appendChild(encode(context, entry.getKey(), keyType));
			valueElement.appendChild(encode(context, entry.getValue(), valueType));
			entryElement.appendChild(keyElement);
			entryElement.appendChild(valueElement);
			containerElement.appendChild(entryElement);
		}

		return containerElement;
	}

	protected Element encodeEntity(EncodingContext context, GenericEntity entity, EntityType<?> entityType) throws CodecException {
		Document document = context.getDocument();

		Integer refId = context.lookupId(entity);

		if (refId != null) {
			// encode reference
			Element element = document.createElement("entity");
			element.setAttribute("ref", refId.toString());
			return element;
		} else {
			try {
				// encode entity
				context.registerRequiredType(entityType);
				refId = context.register(entity);
				Element element = document.createElement("entity");
				element.setAttribute("id", refId.toString());
				element.setAttribute("type", entityType.getTypeSignature());

				boolean partial = false;

				for (Property property : entityType.getProperties()) {

					String propertyName = property.getName();
					Object value = property.get(entity);
					GenericModelType propertyType = property.getType();

					Element propertyElement = document.createElement("property");
					propertyElement.setAttribute("name", propertyName);

					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					// Partial Representation needed here?
					if (absenceInformation != null && writeAbsenceInformation) {
						// stop serializing of value here and deliver only AbsenceInformation
						partial = true;
						propertyElement.setAttribute("absent", Boolean.TRUE.toString());

						Element childElement = encode(context, absenceInformation, context.getAbsenceInformationType());
						propertyElement.appendChild(childElement);
					} else {
						// normal value serializing here
						Element propertyValueElement = encode(context, value, propertyType);
						propertyElement.appendChild(propertyValueElement);
					}

					element.appendChild(propertyElement);
				}

				if (partial)
					element.setAttribute("partial", Boolean.TRUE.toString());

				Element poolElement = context.getPoolElement();

				// store real entities only in the entities element if present and return only entity ref elements in that case
				if (poolElement != null) {
					poolElement.appendChild(element);

					element = document.createElement("entity");
					element.setAttribute("ref", refId.toString());
				}

				return element;
			} catch (GenericModelException e) {
				throw new CodecException("error while encoding entity", e);
			}
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<T> getValueClass() {
		return (Class) getType().getJavaType();
	}

	private class EncodingContext {
		private final Map<GenericEntity, Integer> idByEntities = new HashMap<>();
		private int idSequence = 0;
		private Document document;
		private Element poolElement;
		private EntityType<AbsenceInformation> absenceInformationType;
		private DateTimeFormatter _dateFormat;
		private final Set<GenericModelType> requiredTypes = new HashSet<>();

		public void setDateFormat(DateTimeFormatter dateFormat) {
			this._dateFormat = dateFormat;
		}

		public void setPoolElement(Element poolElement) {
			this.poolElement = poolElement;
		}

		public Element getPoolElement() {
			return poolElement;
		}

		public Integer register(GenericEntity entity) {
			Integer id = idSequence++;
			idByEntities.put(entity, id);
			return id;
		}

		public void registerRequiredType(GenericModelType type) {
			requiredTypes.add(type);
		}

		public Set<GenericModelType> getRequiredTypes() {
			return requiredTypes;
		}

		public void setDocument(Document document) {
			this.document = document;
		}

		public DateTimeFormatter getDateFormat() {
			return _dateFormat;
		}

		public Integer lookupId(GenericEntity entity) {
			return idByEntities.get(entity);
		}

		public Document getDocument() {
			return document;
		}

		public EntityType<AbsenceInformation> getAbsenceInformationType() {
			if (absenceInformationType == null) {
				absenceInformationType = genericModelTypeReflection.getEntityType(AbsenceInformation.class);
			}

			return absenceInformationType;
		}
	}

	private static abstract class DecodingContext {
		public abstract void register(GenericEntity entity, String id);
		public abstract GenericEntity lookupEntity(String id) throws CodecException;
		public abstract Object decode(Element element) throws CodecException;
		public abstract GenericModelTypeReflection getTypeReflection();
		public abstract PropertyAbsenceHelper providePropertyAbsenceHelper();
		public abstract AbsenceInformation getAbsenceInformationForMissingProperties();
		public abstract DateTimeFormatter getDateFormat();
		public abstract boolean isEnhanced();
		public abstract boolean writeAbsenceInformation();
	}

	private class DecodingContextImpl extends DecodingContext {
		private final Map<String, GenericEntity> entitiesById = new HashMap<>();
		private Map<String, Element> elementPool;
		private Element poolElement;
		private final GenericModelTypeReflection typeReflection;
		private AbsenceInformation absenceInformationForMissingProperties;
		private final boolean _assignAbsenceInformationForMissingProperties;
		private final DateTimeFormatter _dateFormat;

		public DecodingContextImpl(GenericModelTypeReflection typeReflection, DateTimeFormatter dateFormat,
				boolean assignAbsenceInformationForMissingProperties) {
			super();
			this.typeReflection = typeReflection;
			this._dateFormat = dateFormat;
			this._assignAbsenceInformationForMissingProperties = assignAbsenceInformationForMissingProperties;
		}

		@Override
		public boolean writeAbsenceInformation() {
			return writeAbsenceInformation;
		}

		public void setPoolElement(Element poolElement) {
			this.poolElement = poolElement;
		}

		/* (non-Javadoc)
		 * 
		 * @see com.braintribe.codec.dom.genericmodel.consolidation.DecodingContext#register(com.braintribe.model.generic.
		 * GenericEntity, java.lang.String) */
		@Override
		public void register(GenericEntity entity, String id) {
			entitiesById.put(id, entity);
		}

		/* (non-Javadoc)
		 * 
		 * @see com.braintribe.codec.dom.genericmodel.consolidation.DecodingContext#lookupEntity(java.lang.String) */
		@Override
		public GenericEntity lookupEntity(String id) throws CodecException {
			GenericEntity entity = entitiesById.get(id);

			if (entity == null) {
				Element element = getElementPool().get(id);
				if (element != null) {
					entity = entityDecoder.decodeElement(this, element);
				} else
					throw new CodecException("no entity element with id " + id + " found");
			}

			return entity;
		}

		@Override
		public boolean isEnhanced() {
			return createEnhancedEntities;
		}

		@SuppressWarnings("unused")
		protected Map<String, Element> getElementPool() throws CodecException {
			if (elementPool == null) {
				elementPool = new HashMap<>();
				if (poolElement != null) {
					NodeList entityElements = poolElement.getElementsByTagName("entity");
					for (int i = 0; i < entityElements.getLength(); i++) {
						Element element = (Element) entityElements.item(i);
						String id = element.getAttribute("id");
						if (id != null && !id.isEmpty()) {
							elementPool.put(id, element);
						}
					}
				}
			}
			return elementPool;
		}

		@Override
		public Object decode(Element element) throws CodecException {
			String tagName = element.getTagName();
			Decoder decoder = decoderMap.get(tagName);
			if (decoder == null)
				throw new CodecException("unkown element type " + tagName);
			return decoder.decodeElement(this, element);
		}

		@Override
		public GenericModelTypeReflection getTypeReflection() {
			return typeReflection;
		}

		@Override
		public PropertyAbsenceHelper providePropertyAbsenceHelper() {
			return _assignAbsenceInformationForMissingProperties ? new ActivePropertyAbsenceHelper(this) : InactivePropertyAbsenceHelper.instance;
		}

		@Override
		public AbsenceInformation getAbsenceInformationForMissingProperties() {
			if (absenceInformationForMissingProperties == null) {
				absenceInformationForMissingProperties = GMF.absenceInformation();
			}

			return absenceInformationForMissingProperties;
		}

		@Override
		public DateTimeFormatter getDateFormat() {
			return _dateFormat;
		}

	}

	interface Decoder {
		Object decodeElement(DecodingContext context, Element element) throws CodecException;

	}

	private abstract static class TextContentDecoder implements Decoder {
		@Override
		public Object decodeElement(DecodingContext context, Element element) throws CodecException {
			Node node = element.getFirstChild();

			String s = "";

			while (node != null) {
				if (node.getNodeType() == Node.TEXT_NODE) {
					Text text = (Text) node;
					s = text.getData();
					break;
				}
				node = node.getNextSibling();
			}

			try {
				return decodeString(context, element, s);
			} catch (Exception e) {
				throw new CodecException("error while decoding string", e);
			}
		}

		protected abstract Object decodeString(DecodingContext context, Element element, String s) throws CodecException;
	}

	private static class EntityDecoder implements Decoder {
		@Override
		public GenericEntity decodeElement(DecodingContext context, Element element) throws CodecException {
			String ref = element.getAttribute("ref");
			if (ref != null && ref.length() > 0) {
				GenericEntity referencedEntity = context.lookupEntity(ref);
				return referencedEntity;
			} else {
				try {
					GenericEntity entity = null;

					String typeName = element.getAttribute("type");

					if (typeName == null || typeName.length() == 0)
						throw new CodecException("missing type attribute");

					GenericModelTypeReflection typeReflection = context.getTypeReflection();
					EntityType<GenericEntity> type = typeReflection.getEntityType(typeName);

					entity = context.isEnhanced() ? type.create() : type.createPlain();

					String idString = element.getAttribute("id");

					if (idString != null && idString.length() > 0) {
						context.register(entity, idString);
					}

					PropertyAbsenceHelper absenceHelper = context.providePropertyAbsenceHelper();

					Node childNode = element.getFirstChild();

					while (childNode != null) {
						if (childNode.getNodeType() == Node.ELEMENT_NODE) {
							Element propertyElement = (Element) childNode;
							String propertyName = propertyElement.getAttribute("name");

							Property property = type.findProperty(propertyName);

							if (property != null) {
								absenceHelper.addPresent(property);
							}

							Element valueElement = getFirstChildElement(propertyElement);

							boolean absent = Boolean.TRUE.toString().equals(propertyElement.getAttribute("absent"));

							if (absent) {
								AbsenceInformation absenceInformation = (AbsenceInformation) entityDecoder.decodeElement(context, valueElement);
								if (property != null)
									property.setAbsenceInformation(entity, absenceInformation);
							} else {
								Object value = context.decode(valueElement);
								if (property != null)
									property.set(entity, value);
							}
						}
						childNode = childNode.getNextSibling();
					}

					absenceHelper.ensureAbsenceInformation(type, entity);

					return entity;
				} catch (Exception e) {
					throw new CodecException("error while decoding entity", e);
				}
			}

		}
	}

	private static class EnumDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) throws CodecException {
			String typeSignature = element.getAttribute("type");
			EnumType enumType = GMF.getTypeReflection().getType(typeSignature);
			return enumType.getInstance(s);
		}
	}

	private static class StringDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return s;
		}
	}

	private static class DateDecoder extends TextContentDecoder {

		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return DateTools.decode(s, context.getDateFormat());
		}
	}

	private static class IntegerDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return Integer.valueOf(s);
		}
	}

	private static class LongDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return Long.valueOf(s);
		}
	}

	private static class FloatDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return Float.valueOf(s);
		}
	}

	private static class DoubleDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return Double.valueOf(s);
		}
	}

	private static class DecimalDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return new BigDecimal(s);
		}
	}

	private static class BooleanDecoder extends TextContentDecoder {
		@Override
		protected Object decodeString(DecodingContext context, Element element, String s) {
			return Boolean.valueOf(s);
		}
	}

	private static class NullDecoder implements Decoder {
		@Override
		public Object decodeElement(DecodingContext context, Element element) {
			return null;
		}
	}

	private static abstract class CollectionDecoder implements Decoder {
		protected abstract Collection<Object> createCollection();
		@Override
		public Object decodeElement(DecodingContext context, Element element) throws CodecException {
			Collection<Object> collection = createCollection();

			Node childNode = element.getFirstChild();

			while (childNode != null) {
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element childElement = (Element) childNode;
					collection.add(context.decode(childElement));
				}
				childNode = childNode.getNextSibling();
			}

			return collection;
		}
	}

	private static class ListDecoder extends CollectionDecoder {
		@Override
		protected Collection<Object> createCollection() {
			return new ArrayList<>();
		}
	}

	private static class SetDecoder extends CollectionDecoder {
		@Override
		protected Collection<Object> createCollection() {
			return new HashSet<>();
		}
	}

	private static class MapDecoder implements Decoder {
		@Override
		public Object decodeElement(DecodingContext context, Element element) throws CodecException {
			Node childNode = element.getFirstChild();
			Map<Object, Object> map = new HashMap<>();
			while (childNode != null) {
				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					Element entryElement = (Element) childNode;

					Node entryChildNode = entryElement.getFirstChild();
					Element keyElement = null;
					Element valueElement = null;
					while (entryChildNode != null) {
						if (entryChildNode.getNodeType() == Node.ELEMENT_NODE) {
							Element entryChildElement = (Element) entryChildNode;
							if (entryChildElement.getTagName().equals("key")) {
								keyElement = getFirstChildElement(entryChildElement);
							} else if (entryChildElement.getTagName().equals("value")) {
								valueElement = getFirstChildElement(entryChildElement);
							}
						}
						entryChildNode = entryChildNode.getNextSibling();
					}

					Object key = context.decode(keyElement);
					Object value = context.decode(valueElement);

					map.put(key, value);
				}
				childNode = childNode.getNextSibling();
			}

			return map;
		}
	}

	protected static Element getFirstChildElement(Element element) {
		Node childNode = element.getFirstChild();
		while (childNode != null) {
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) childNode;
			}
			childNode = childNode.getNextSibling();
		}
		return null;
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
			// noop
		}

		@Override
		public void ensureAbsenceInformation(EntityType<?> entityType, GenericEntity entity) {
			// noop
		}
	}

}
