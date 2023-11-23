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
package com.braintribe.codec.marshaller.sax;

import static com.braintribe.model.generic.reflection.GmReflectionTools.isPartial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.utils.DateTools;

public class SaxMarshaller<T> implements Marshaller, GmCodec<T, String>, HasStringCodec {

	private static Logger logger = Logger.getLogger(SaxMarshaller.class);

	private GenericModelType type;
	private boolean writeAbsenceInformation = true;
	boolean createEnhancedEntities = true;
	private boolean assignAbsenceInformationForMissingProperties;
	boolean pretty = true;
	private boolean writeRequiredTypes = false;
	Consumer<Set<String>> requiredTypesReceiver = null;

	@Configurable
	public void setPretty(boolean pretty) {
		this.pretty = pretty;
	}

	@Configurable
	public void setWriteRequiredTypes(boolean writeRequiredTypes) {
		this.writeRequiredTypes = writeRequiredTypes;
	}

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
	public void setWriteAbsenceInformation(boolean writeAbsenceInformation) {
		this.writeAbsenceInformation = writeAbsenceInformation;
	}

	public void setType(GenericModelType type) {
		this.type = type;
	}

	public GenericModelType getType() {
		if (type == null) {
			type = BaseType.INSTANCE;
		}

		return type;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class<T> getValueClass() {
		return (Class) getType().getJavaType();
	}

	@Override
	public T unmarshall(InputStream in) throws MarshallException {
		return unmarshall(new InputSource(in),
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	public T unmarshall(Reader reader) throws MarshallException {
		return unmarshall(new InputSource(reader),
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public T unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(new InputSource(in), options);
	}

	public T unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		return unmarshall(new InputSource(reader), options);
	}

	public T unmarshall(InputSource is, GmDeserializationOptions options) throws MarshallException {
		try {
			DefaultHandlerImpl<T> handler = new DefaultHandlerImpl<>(this.createEnhancedEntities, this.requiredTypesReceiver, options);
			SAXParserFactory spf = SAXParserFactory.newInstance();

			setFeature(spf, "http://xml.org/sax/features/external-general-entities", false);
			setFeature(spf, "http://xml.org/sax/features/external-parameter-entities", false);
			setFeature(spf, "http://apache.org/xml/features/disallow-doctype-decl", true);

			spf.newSAXParser().parse(is, handler);
			@SuppressWarnings("unchecked")
			T value = (T) handler.getValue();
			return value;
		} catch (Exception e) {
			throw new MarshallException("error while unmarshalling", e);
		}
	}

	protected static void setFeature(SAXParserFactory spf, String key, boolean value) {
		try {
			spf.setFeature(key, value);
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("Could not set " + key + "=" + value, e);
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
		EncodingContext<T> context = new EncodingContext<>(this.pretty, writer, options);
		try {
			writeGmData(context, writer, encoding, value);
		} catch (IOException e) {
			throw new MarshallException("IO error while marshalling", e);
		}
	}

	@Override
	public T decode(String encodedValue, GmDeserializationOptions options) throws CodecException {
		try {
			return unmarshall(new StringReader(encodedValue), options);
		} catch (Exception e) {
			throw new CodecException("error while unmarshalling", e);
		}
	}

	@Override
	public String encode(T value, GmSerializationOptions options) throws CodecException {
		StringWriter writer = new StringWriter();
		try {
			marshall(writer, "UTF-8", value, options);
			writer.close();
			return writer.toString();
		} catch (Exception e) {
			throw new CodecException("error while marshalling", e);
		}
	}

	protected void writeGmData(EncodingContext<T> context, Writer writer, String encoding, Object rootValue) throws MarshallException, IOException {
		writer.write("<?xml version=\"1.0\" encoding=\"");
		writer.write(encoding);
		writer.write("\" standalone=\"no\"?>\n");
		writer.write("<?gm-xml version=\"3\"?>\n");
		writer.write("<gm-data>\n");

		context.pushIndentation();
		try {
			if (writeRequiredTypes) {
				StringWriter buffer = context.pushBuffer();
				try {
					writeRootValueAndPool(context, buffer, rootValue);
				} finally {
					context.popBuffer();
				}

				buffer.close();
				String bufferedText = buffer.toString();
				writeRequiredTypes(context, writer);
				writer.write(bufferedText);
			} else {
				writeRootValueAndPool(context, writer, rootValue);
			}
		} finally {
			context.popIndentation();
		}

		writer.write("</gm-data>\n");
	}

	protected void writeRequiredTypes(EncodingContext<T> context, Writer writer) throws IOException {
		Set<String> requiredTypes = context.getRequiredTypes();

		if (!requiredTypes.isEmpty()) {
			context.writeIndentation();
			writer.write("<required-types>\n");

			context.pushIndentation();
			try {
				for (String requiredType : requiredTypes) {
					context.writeIndentation();
					writer.write("<type>");
					writer.write(requiredType);
					writer.write("</type>\n");
				}
			} finally {
				context.popIndentation();
			}

			context.writeIndentation();
			writer.write("</required-types>\n");
		}
	}

	protected void writeRootValueAndPool(EncodingContext<T> context, Writer writer, Object rootValue) throws MarshallException, IOException {
		writeRootValue(context, writer, rootValue);
		writePool(context, writer);
	}

	protected void writeRootValue(EncodingContext<T> context, Writer writer, Object rootValue) throws MarshallException, IOException {
		context.writeIndentation();
		writer.write("<root-value>\n");

		context.pushIndentation();
		context.pushReferenceMode();
		try {
			write(context, writer, rootValue, getType());
		} finally {
			context.popReferenceMode();
			context.popIndentation();
		}

		context.writeIndentation();
		writer.write("</root-value>\n");
	}

	protected void writePool(EncodingContext<T> context, Writer writer) throws MarshallException, IOException {
		context.writeIndentation();
		writer.write("<pool>\n");

		context.pushIndentation();
		try {
			EntityQueueNode node = context.getFirstNode();

			while (node != null) {
				writeEntity(context, writer, node.entity, node.entityType);
				node = node.next;
			}
		} finally {
			context.popIndentation();
		}

		context.writeIndentation();
		writer.write("</pool>\n");
	}

	protected void write(EncodingContext<T> context, Writer writer, Object value, GenericModelType type) throws MarshallException, IOException {
		if (value == null) {
			context.writeIndentation();
			writer.write("<null/>\n");
			return;
		}

		switch (type.getTypeCode()) {
			// base type
			case objectType:
				write(context, writer, value, type.getActualType(value));
				break;

			// scalar types
			case dateType:
				String text = DateTools.encode((Date) value, DateFormats.dateFormat); // Expecting object of type Date
				writeScalarValue(context, writer, type.getTypeName(), text, false);
				break;
			case stringType:
				writeScalarValue(context, writer, type.getTypeName(), value, true);
				break;
			case booleanType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
				writeScalarValue(context, writer, type.getTypeName(), value, false);
				break;

			// collections
			case listType:
			case setType:
				writeCollectionValue(context, writer, (CollectionType) type, (Collection<?>) value);
				break;
			case mapType:
				writeMapValue(context, writer, (CollectionType) type, (Map<?, ?>) value);
				break;

			// custom types
			case entityType:
				writeEntity(context, writer, (GenericEntity) value, type.getActualType(value).<EntityType<?>> cast());
				break;

			case enumType:
				context.writeIndentation();
				writer.write("<enum type=\"");

				String typeSignature = type.getTypeSignature();

				if (writeRequiredTypes)
					context.registerRequiredType(typeSignature);

				writer.write(typeSignature);
				writer.write("\">");
				writer.write(value.toString());
				writer.write("</enum>\n");
				break;
			default:
				throw new MarshallException("unsupported GenericModelType " + type.getClass());
		}
	}

	/**
	 * @throws MarshallException
	 *             Thrown when the marshalling was not successful.
	 */
	protected void writeScalarValue(EncodingContext<T> context, Writer writer, String tagName, Object value, boolean escape)
			throws MarshallException, IOException {
		context.writeIndentation();
		writer.write('<');
		writer.write(tagName);
		writer.write('>');
		if (escape)
			writeEscaped(writer, value.toString());
		else
			writer.write(value.toString());
		writer.write("</");
		writer.write(tagName);
		writer.write(">\n");
	}

	private static void writeEscaped(Writer writer, String s) throws IOException {
		int c = s.length();

		for (int i = 0; i < c; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '&':
					writer.write("&amp;");
					break;
				case '<':
					writer.write("&lt;");
					break;
				case '>':
					writer.write("&gt;");
					break;
				case '\r':
					writer.write("&#13;");
					break;
				default:
					writer.write(ch);
			}
		}
	}

	protected void writeCollectionValue(EncodingContext<T> context, Writer writer, CollectionType collectionType, Collection<?> collection)
			throws MarshallException, IOException {
		context.writeIndentation();
		writer.write('<');
		writer.write(collectionType.getTypeName());
		writer.write(">\n");
		GenericModelType elementType = collectionType.getCollectionElementType();

		context.pushIndentation();
		try {
			for (Object value : collection) {
				write(context, writer, value, elementType);
			}
		} finally {
			context.popIndentation();
		}

		context.writeIndentation();
		writer.write("</");
		writer.write(collectionType.getTypeName());
		writer.write(">\n");
	}

	protected void writeMapValue(EncodingContext<T> context, Writer writer, CollectionType collectionType, Map<?, ?> map)
			throws MarshallException, IOException {
		context.writeIndentation();
		writer.write("<map>\n");
		GenericModelType[] parameterization = collectionType.getParameterization();
		GenericModelType keyType = parameterization[0];
		GenericModelType valueType = parameterization[1];

		context.pushIndentation();
		try {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				context.writeIndentation();
				writer.write("<entry>\n");

				context.pushIndentation();
				try {
					context.writeIndentation();
					writer.write("<key>\n");
					write(context, writer, entry.getKey(), keyType);
					context.writeIndentation();
					writer.write("</key>\n");

					context.writeIndentation();
					writer.write("<value>\n");
					write(context, writer, entry.getValue(), valueType);
					context.writeIndentation();
					writer.write("</value>\n");
				} finally {
					context.popIndentation();
				}

				context.writeIndentation();
				writer.write("</entry>\n");
			}
		} finally {
			context.popIndentation();
		}
		context.writeIndentation();
		writer.write("</map>\n");
	}

	protected void writeEntity(EncodingContext<T> context, Writer writer, GenericEntity entity, EntityType<?> entityType)
			throws MarshallException, IOException {
		Integer refId = context.lookupId(entity, entityType);

		if (context.isReferenceMode()) {
			context.writeIndentation();
			writer.write("<entity ref=\"");
			writer.write(refId.toString());
			writer.write("\"/>\n");
		} else {
			context.pushReferenceMode();
			try {
				boolean partial = entity instanceof EnhancedEntity ? isPartial(entity) : false;

				// encode entity
				context.writeIndentation();
				writer.write("<entity id=\"");
				writer.write(refId.toString());
				writer.write("\" type=\"");
				String entityTypeName = entityType.getTypeSignature();
				if (writeRequiredTypes)
					context.registerRequiredType(entityTypeName);
				writer.write(entityTypeName);
				if (partial)
					writer.write("\" partial=\"true");
				writer.write("\">\n");

				context.pushIndentation();
				try {

					for (Property property : entityType.getProperties()) {

						String propertyName = property.getName();
						Object value = property.get(entity);
						GenericModelType propertyType = property.getType();

						AbsenceInformation absenceInformation = property.getAbsenceInformation(entity);

						// Partial Representation needed here?
						if (absenceInformation != null && writeAbsenceInformation) {
							context.writeIndentation();
							writer.write("<property name=\"");
							writer.write(propertyName);
							writer.write("\" absent=\"true\">\n");

							context.pushIndentation();
							try {
								write(context, writer, absenceInformation, AbsenceInformation.T);
							} finally {
								context.popIndentation();
							}
							context.writeIndentation();
							writer.write("</property>\n");
						} else {
							context.writeIndentation();
							writer.write("<property name=\"");
							writer.write(propertyName);
							writer.write("\">\n");

							context.pushIndentation();
							try {
								write(context, writer, value, propertyType);
							} finally {
								context.popIndentation();
							}
							context.writeIndentation();
							writer.write("</property>\n");
						}
					}
				} finally {
					context.popIndentation();
				}

				context.writeIndentation();
				writer.write("</entity>\n");

			} catch (GenericModelException e) {
				throw new MarshallException("error while encoding entity", e);
			} finally {
				context.popReferenceMode();
			}
		}
	}

	@Override
	public GmCodec<Object, String> getStringCodec() {
		@SuppressWarnings("unchecked")
		GmCodec<Object, String> stringCodec = (GmCodec<Object, String>) this;
		return stringCodec;
	}

	@Override
	public T decode(String encodedValue) throws CodecException {
		return decode(encodedValue,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public String encode(T value) throws CodecException {
		return encode(value, GmSerializationOptions.deriveDefaults().build());
	}
}
