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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.utils.DateTools;

public class StaxResearchMarshaller implements Marshaller, GmCodec<Object, String>, HasStringCodec {

	// private boolean writeAbsenceInformation = true; // PGA: commenting out as unused
	private boolean createEnhancedEntities = true;
	private boolean assignAbsenceInformationForMissingProperties;
	private Consumer<Set<String>> requiredTypesReceiver = null;
	private final DecoderFactoryContext decoderFactoryContext = new DecoderFactoryContext();

	private static XMLInputFactory inputFactory = XMLInputFactory.newInstance();

	public static final StaxResearchMarshaller defaultInstance = new StaxResearchMarshaller();

	@Configurable
	public void setRequiredTypesReceiver(Consumer<Set<String>> requiredTypesReceiver) {
		this.requiredTypesReceiver = requiredTypesReceiver;
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
	public void setWriteAbsenceInformation(@SuppressWarnings("unused") boolean writeAbsenceInformation) {
		// this.writeAbsenceInformation = writeAbsenceInformation;
	}

	@Override
	public Class<Object> getValueClass() {
		return Object.class;
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	public Object unmarshall(Reader reader) throws MarshallException {
		return unmarshall(reader,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		try {
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
			return unmarshall(streamReader, options);
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		} catch (FactoryConfigurationError e) {
			throw new MarshallException(e);
		}
	}

	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		try {
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(reader);
			return unmarshall(streamReader, options);
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		} catch (FactoryConfigurationError e) {
			throw new MarshallException(e);
		}
	}

	public Object unmarshall(XMLStreamReader streamReader, GmDeserializationOptions options) throws MarshallException {
		try {
			DefaultHandlerImpl handler = new DefaultHandlerImpl(this.decoderFactoryContext, this.createEnhancedEntities, this.requiredTypesReceiver,
					options);

			handler.read(streamReader);

			Object value = handler.getValue();
			return value;
		} catch (Exception e) {
			throw new MarshallException("error while unmarshalling", e);
		}
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			marshall(writer, "UTF-8", value, options);
			writer.flush();
		} catch (UnsupportedEncodingException e) {
			throw new MarshallException("error creating output stream", e);
		} catch (IOException e) {
			throw new MarshallException("IO error while marshalling", e);
		}
	}

	public void marshall(Writer writer, String encoding, Object value) throws MarshallException {
		marshall(writer, encoding, value, GmSerializationOptions.deriveDefaults().build());
	}

	public void marshall(Writer writer, String encoding, Object value, GmSerializationOptions options) throws MarshallException {
		try {
			writer.write("<?xml version='1.0' encoding='" + encoding + "'?>");
			writer.write("\n<?gm-xml version=\"4\"?>");
			EncodingContext context = new EncodingContext(options);
			writeGmData(context, writer, value);
		} catch (IOException | FactoryConfigurationError e) {
			throw new MarshallException("error while marshalling", e);
		}
	}

	@Override
	public Object decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		try {
			return unmarshall(new StringReader(encodedValue), options);
		} catch (Exception e) {
			throw new CodecException("error while unmarshalling", e);
		}
	}

	@Override
	public String encode(Object value, GmSerializationOptions options) throws CodecException {
		StringWriter writer = new StringWriter();
		try {
			marshall(writer, "UTF-8", value, options);
			writer.close();
			return writer.toString();
		} catch (Exception e) {
			throw new CodecException("error while marshalling", e);
		}
	}

	protected void writeGmData(EncodingContext context, Writer writer, Object rootValue) throws MarshallException, IOException {
		writer.write("\n<gm-data>");

		// preparation by a minimal traverse
		context.collect(rootValue);

		// required types
		writeRequiredTypes(context, writer);

		// root value
		writer.write("\n<root-value>");
		writer.write(linefeed, 0, 2);
		writeValue(context, writer, BaseType.INSTANCE, rootValue, 1);
		writer.write("\n</root-value>");

		// pool
		writer.write("\n<pool>");
		for (Map.Entry<GenericEntity, EntityInfo> entry : context.getEntityInfos().entrySet()) {
			writeEntity(context, writer, entry.getKey(), entry.getValue(), 0);
		}
		writer.write("\n</pool>");

		writer.write("\n</gm-data>");
	}

	protected void writeRequiredTypes(EncodingContext context, Writer writer) throws IOException {
		writer.write("\n<required-types>");

		List<TypeInfo> typeInfos = new ArrayList<>(context.getRequiredTypeInfos());
		Collections.sort(typeInfos);
		for (TypeInfo typeInfo : typeInfos) {
			GenericModelType type = typeInfo.type;

			writer.write("\n <t as='");
			writer.write(typeInfo.as);
			writer.write("' alias='");
			writer.write(typeInfo.alias);

			if (type.getTypeCode() == TypeCode.entityType) {
				writer.write("' num='");
				writer.write(String.valueOf(typeInfo.getCount()));
			}
			writer.write("'>");
			writer.write(type.getTypeSignature());
			writer.write("</t>");
		}

		writer.write("\n</required-types>");
	}

	private static final char[] emptyNullElement = "<n/>".toCharArray();
	private static final char[] propNullElementClose = "'/>".toCharArray();
	private static final char[] propNullElement = "<n p='".toCharArray();

	private static final char[] startPropIntegerElementClose = "'>".toCharArray();
	private static final char[] startPropIntegerElement = "<i p='".toCharArray();
	private static final char[] startIntegerElement = "<i>".toCharArray();
	private static final char[] endIntegerElement = "</i>".toCharArray();

	private static final char[] endStringElement = "</s>".toCharArray();
	private static final char[] startPropStringElementClose = "'>".toCharArray();
	private static final char[] startPropStringElement = "<s p='".toCharArray();
	private static final char[] startStringElement = "<s>".toCharArray();

	private static final char[] propTrueElementOpen = "<b p='".toCharArray();
	private static final char[] propTrueElementClose = "'>true</b>".toCharArray();
	private static final char[] trueElement = "<b>true</b>".toCharArray();

	private static final char[] propFalseElementOpen = "<b p='".toCharArray();
	private static final char[] propFalseElementClose = "'>false</b>".toCharArray();
	private static final char[] falseElement = "<b>false</b>".toCharArray();

	private static final char[] endDateElement = "</T>".toCharArray();
	private static final char[] startPropDateElementClose = "'>".toCharArray();
	private static final char[] startPropDateElement = "<T p='".toCharArray();
	private static final char[] startDateElement = "<T>".toCharArray();

	private static final char[] endDecimalElement = "</D>".toCharArray();
	private static final char[] startPropDecimalElementClose = "'>".toCharArray();
	private static final char[] startPropDecimalElement = "<D p='".toCharArray();
	private static final char[] startDecimalElement = "<D>".toCharArray();

	private static final char[] endDoubleElement = "</d>".toCharArray();
	private static final char[] startPropDoubleElementClose = "'>".toCharArray();
	private static final char[] startPropDoubleElement = "<d p='".toCharArray();
	private static final char[] startDoubleElement = "<d>".toCharArray();

	private static final char[] endFloatElement = "</f>".toCharArray();
	private static final char[] startPropFloatElementClose = "'>".toCharArray();
	private static final char[] startPropFloatElement = "<f p='".toCharArray();
	private static final char[] startFloatElement = "<f>".toCharArray();

	private static final char[] endLongElement = "</l>".toCharArray();
	private static final char[] startPropLongElementClose = "'>".toCharArray();
	private static final char[] startPropLongElement = "<l p='".toCharArray();
	private static final char[] startLongElement = "<l>".toCharArray();

	private static final char[] endRefElement = "</r>".toCharArray();
	private static final char[] startPropRefElementClose = "'>".toCharArray();
	private static final char[] startPropRefElement = "<r p='".toCharArray();
	private static final char[] startRefElement = "<r>".toCharArray();

	private static final char[] endEnumElement = "</e>".toCharArray();
	private static final char[] startPropEnumElementClose = "'>".toCharArray();
	private static final char[] startPropEnumElement = "<e p='".toCharArray();
	private static final char[] startEnumElement = "<e>".toCharArray();

	private static final char[] propEmptyAbsenceElementClose = "'/>".toCharArray();
	private static final char[] propAbsenceElementClose = "'>".toCharArray();
	private static final char[] propAbsenceElementOpen = "<a p='".toCharArray();
	private static final char[] endAbsenceElement = "</a>".toCharArray();

	private static final char[] endEmptyEntityElement = "/>".toCharArray();
	private static final char[] endEntityElement = "</E>".toCharArray();
	private static final char[] startEntityElement1 = "<E t='".toCharArray();
	private static final char[] startEntityElement2 = "' id='".toCharArray();
	private static final char[] startEntityElement3 = "'>".toCharArray();

	private static final char[] emptyListElement = "<L/>".toCharArray();
	private static final char[] endListElement = "</L>".toCharArray();
	private static final char[] startListElement = "<L>".toCharArray();
	private static final char[] endPropListElement = "</L>".toCharArray();
	private static final char[] startPropListElementClose = "'>".toCharArray();
	private static final char[] startPropListElement = "<L p='".toCharArray();

	private static final char[] emptySetElement = "<S/>".toCharArray();
	private static final char[] endSetElement = "</S>".toCharArray();
	private static final char[] startSetElement = "<S>".toCharArray();
	private static final char[] endPropSetElement = "</S>".toCharArray();
	private static final char[] startPropSetElementClose = "'>".toCharArray();
	private static final char[] startPropSetElement = "<S p='".toCharArray();

	private static final char[] emptyMapElement = "<M/>".toCharArray();
	private static final char[] endMapElement = "</M>".toCharArray();
	private static final char[] startEntryMapElement = "<m>".toCharArray();
	private static final char[] startMapElement = "<M>".toCharArray();
	private static final char[] endEntryMapElement = "</m>".toCharArray();
	private static final char[] startPropEntryMapElement = "<m>".toCharArray();
	private static final char[] startPropMapElementClose = "'>".toCharArray();
	private static final char[] startPropMapElement = "<M p='".toCharArray();

	private static final char[] linefeed = "\n          ".toCharArray();

	protected void writeValue(EncodingContext context, Writer writer, GenericModelType type, Object value, int indent)
			throws IOException, MarshallException {
		if (value == null) {
			writer.write(emptyNullElement);
			return;
		}

		while (true) {
			switch (type.getTypeCode()) {
				case objectType:
					type = type.getActualType(value);
					break;

				// simple types
				case stringType:
					writer.write(startStringElement);
					writeEscaped(writer, (String) value);
					writer.write(endStringElement);
					return;
				case integerType:
					writer.write(startIntegerElement);
					writer.write(value.toString());
					writer.write(endIntegerElement);
					return;
				case booleanType:
					if ((Boolean) value)
						writer.write(trueElement);
					else
						writer.write(falseElement);
					return;
				case dateType:
					writer.write(startDateElement);
					writer.write(DateTools.encode((Date) value, DateFormats.dateFormat));
					writer.write(endDateElement);
					return;
				case decimalType:
					writer.write(startDecimalElement);
					writer.write(value.toString());
					writer.write(endDecimalElement);
					return;
				case doubleType:
					writer.write(startDoubleElement);
					writer.write(value.toString());
					writer.write(endDoubleElement);
					return;
				case floatType:
					writer.write(startFloatElement);
					writer.write(value.toString());
					writer.write(endFloatElement);
					return;
				case longType:
					writer.write(startLongElement);
					writer.write(value.toString());
					writer.write(endLongElement);
					return;

				// custom types
				case entityType:
					writer.write(startRefElement);
					writer.write(context.lookupEntityInfo((GenericEntity) value).refId);
					// writer.write(context.getNextRefId());
					writer.write(endRefElement);
					return;
				case enumType:
					writer.write(startEnumElement);
					writer.write(context.registerRequiredType(type).as);
					writer.write('.');
					writer.write(value.toString());
					writer.write(endEnumElement);
					return;

				// collections
				case listType: {
					List<?> list = (List<?>) value;
					if (!list.isEmpty()) {
						writer.write(startListElement);
						GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
						for (Object element : list) {
							writer.write(linefeed, 0, indent + 2);
							writeValue(context, writer, elementType, element, indent + 1);
						}

						writer.write(linefeed, 0, indent + 1);
						writer.write(endListElement);
					} else
						writer.write(emptyListElement);
					return;
				}
				case setType: {
					Set<?> set = (Set<?>) value;
					if (!set.isEmpty()) {
						writer.write(startSetElement);
						GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
						for (Object element : set) {
							writer.write("\n ");
							writeValue(context, writer, elementType, element, indent + 1);
						}

						writer.write(linefeed, 0, indent + 1);
						writer.write(endSetElement);
					} else
						writer.write(emptySetElement);
					return;
				}
				case mapType: {
					Map<?, ?> map = (Map<?, ?>) value;
					if (!map.isEmpty()) {
						writer.write(startMapElement);
						GenericModelType[] parameterization = ((CollectionType) type).getParameterization();
						GenericModelType keyType = parameterization[0];
						GenericModelType valueType = parameterization[1];
						for (Map.Entry<?, ?> entry : map.entrySet()) {
							writer.write(linefeed, 0, indent + 2);
							writer.write(startEntryMapElement);
							writeValue(context, writer, keyType, entry.getKey(), indent + 2);
							writeValue(context, writer, valueType, entry.getValue(), indent + 2);
							writer.write(linefeed, 0, indent + 2);
							writer.write(endEntryMapElement);
						}
						writer.write(linefeed, 0, indent + 1);
						writer.write(endMapElement);
					} else
						writer.write(emptyMapElement);
					return;
				}
				default:
					throw new MarshallException("unsupported type " + type);
			}
		}
	}

	protected void writePropertyValue(EncodingContext context, Writer writer, GenericModelType type, String propertyName, Object value, int indent)
			throws IOException, MarshallException {
		if (value == null) {
			writer.write(linefeed, 0, indent + 1);
			writer.write(propNullElement);
			writer.write(propertyName);
			writer.write(propNullElementClose);
			return;
		}

		while (true) {
			switch (type.getTypeCode()) {
				case objectType:
					type = type.getActualType(value);
					break;

				// simple types
				case stringType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropStringElement);
					writer.write(propertyName);
					writer.write(startPropStringElementClose);
					writeEscaped(writer, (String) value);
					writer.write(endStringElement);
					return;
				case integerType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropIntegerElement);
					writer.write(propertyName);
					writer.write(startPropIntegerElementClose);
					writer.write(value.toString());
					writer.write(endIntegerElement);
					return;
				case booleanType:
					writer.write(linefeed, 0, indent + 1);
					if ((Boolean) value) {
						writer.write(propTrueElementOpen);
						writer.write(propertyName);
						writer.write(propTrueElementClose);
					} else {
						writer.write(propFalseElementOpen);
						writer.write(propertyName);
						writer.write(propFalseElementClose);
					}
					return;
				case dateType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropDateElement);
					writer.write(propertyName);
					writer.write(startPropDateElementClose);
					writer.write(DateTools.encode((Date) value, DateFormats.dateFormat));
					writer.write(endDateElement);
					return;
				case decimalType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropDecimalElement);
					writer.write(propertyName);
					writer.write(startPropDecimalElementClose);
					writer.write(value.toString());
					writer.write(endDecimalElement);
					return;
				case doubleType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropDoubleElement);
					writer.write(propertyName);
					writer.write(startPropDoubleElementClose);
					writer.write(value.toString());
					writer.write(endDoubleElement);
					return;
				case floatType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropFloatElement);
					writer.write(propertyName);
					writer.write(startPropFloatElementClose);
					writer.write(value.toString());
					writer.write(endFloatElement);
					return;
				case longType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropLongElement);
					writer.write(propertyName);
					writer.write(startPropLongElementClose);
					writer.write(value.toString());
					writer.write(endLongElement);
					return;

				// custom types
				case entityType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropRefElement);
					writer.write(propertyName);
					writer.write(startPropRefElementClose);
					writer.write(context.lookupEntityInfo((GenericEntity) value).refId);
					// writer.write(context.getNextRefId());
					writer.write(endRefElement);
					return;
				case enumType:
					writer.write(linefeed, 0, indent + 1);
					writer.write(startPropEnumElement);
					writer.write(propertyName);
					writer.write(startPropEnumElementClose);
					writer.write(context.registerRequiredType(type).as);
					writer.write('.');
					writer.write(value.toString());
					writer.write(endEnumElement);
					return;

				// collections
				case listType: {
					List<?> list = (List<?>) value;
					if (!list.isEmpty()) {
						writer.write(linefeed, 0, indent + 1);
						writer.write(startPropListElement);
						writer.write(propertyName);
						writer.write(startPropListElementClose);

						GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
						for (Object element : list) {
							writer.write(linefeed, 0, indent + 2);
							writeValue(context, writer, elementType, element, indent + 1);
						}

						writer.write(linefeed, 0, indent + 1);
						writer.write(endPropListElement);
					}
					return;
				}
				case setType: {
					Set<?> set = (Set<?>) value;
					if (!set.isEmpty()) {
						writer.write(linefeed, 0, indent + 1);
						writer.write(startPropSetElement);
						writer.write(propertyName);
						writer.write(startPropSetElementClose);

						GenericModelType elementType = ((CollectionType) type).getCollectionElementType();
						for (Object element : set) {
							writer.write(linefeed, 0, indent + 2);
							writeValue(context, writer, elementType, element, indent + 1);
						}

						writer.write(linefeed, 0, indent + 1);
						writer.write(endPropSetElement);
					}
					return;
				}
				case mapType: {
					Map<?, ?> map = (Map<?, ?>) value;
					if (!map.isEmpty()) {
						writer.write(linefeed, 0, indent + 1);
						writer.write(startPropMapElement);
						writer.write(propertyName);
						writer.write(startPropMapElementClose);

						GenericModelType[] parameterization = ((CollectionType) type).getParameterization();
						GenericModelType keyType = parameterization[0];
						GenericModelType valueType = parameterization[1];
						for (Map.Entry<?, ?> entry : map.entrySet()) {
							writer.write(linefeed, 0, indent + 2);
							writer.write(startPropEntryMapElement);
							writeValue(context, writer, keyType, entry.getKey(), indent + 2);
							writeValue(context, writer, valueType, entry.getValue(), indent + 2);
							writer.write(endEntryMapElement);
						}
						writer.write(linefeed, 0, indent + 1);
						writer.write(endMapElement);
					}
					return;
				}
				default:
					throw new MarshallException("unsupported type " + type);
			}
		}
	}

	public void writeEntity(EncodingContext context, Writer writer, GenericEntity entity, EntityInfo entityInfo, int indent)
			throws IOException, MarshallException {
		try {
			TypeInfo typeInfo = entityInfo.typeInfo;
			String id = entityInfo.refId;

			EntityType<?> entityType = (EntityType<?>) typeInfo.type;

			writer.write(linefeed, 0, indent + 1);
			writer.write(startEntityElement1);
			writer.write(typeInfo.as);
			writer.write(startEntityElement2);
			writer.write(id);
			writer.write(startEntityElement3);

			int count = 0;
			int propertyIndent = indent + 1;
			for (Property property : entityType.getProperties()) {
				Object value = property.get(entity);

				if (value == null) {
					AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

					if (absenceInformation != null) {
						writer.write(linefeed, 0, indent + 1);
						if (context.isSimpleAbsenceInformation(absenceInformation)) {
							writer.write(propAbsenceElementOpen);
							writer.write(property.getName());
							writer.write(propEmptyAbsenceElementClose);
						} else {
							writer.write(propAbsenceElementOpen);
							writer.write(property.getName());
							writer.write(propAbsenceElementClose);
							writer.write(context.lookupEntityInfo(absenceInformation).refId);
							writer.write(endAbsenceElement);
						}
						count++;
					}
				} else {
					writePropertyValue(context, writer, property.getType(), property.getName(), value, propertyIndent);
					count++;
				}
			}

			if (count > 0) {
				writer.write(linefeed, 0, indent + 1);
				writer.write(endEntityElement);
			} else
				writer.write(endEmptyEntityElement);

		} catch (GenericModelException e) {
			throw new MarshallException("error while encoding entity", e);
		}
	}

	private static final char[][] ESCAPES = new char[63][];

	static {
		ESCAPES['<'] = "&lt;".toCharArray();
		ESCAPES['>'] = "&gt;".toCharArray();
		ESCAPES['&'] = "&amp;".toCharArray();
		ESCAPES['\r'] = "&#13;".toCharArray();
	}

	public static void writeEscaped(Writer writer, String string) throws IOException {
		int len = string.length();
		int s = 0;
		int i = 0;
		char esc[] = null;
		for (; i < len; i++) {
			char c = string.charAt(i);

			if (c < 63) {
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

	@Override
	public GmCodec<Object, String> getStringCodec() {
		return this;
	}

	@Override
	public Object decode(String encodedValue) throws CodecException {
		return decode(encodedValue,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public String encode(Object value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}
}
