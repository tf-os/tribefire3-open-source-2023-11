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
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.TypeLookup;
import com.braintribe.codec.marshaller.stabilization.EntityNodeComparator;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.codec.marshaller.stax.tree.EntityNode;
import com.braintribe.codec.marshaller.stax.tree.ValueStaxNode;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TypeCode;

public class StaxMarshaller implements CharacterMarshaller, GmCodec<Object, String>, HasStringCodec {

	private static Logger logger = Logger.getLogger(StaxMarshaller.class);

	// private boolean writeAbsenceInformation = true; // PGA: commenting out as unused
	boolean createEnhancedEntities = true;
	private boolean assignAbsenceInformationForMissingProperties;
	private Consumer<Set<String>> requiredTypesReceiver = null;
	private final DecoderFactoryContext decoderFactoryContext = new DecoderFactoryContext();

	private final static XMLInputFactory inputFactory;

	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = logger.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch (Exception e) {
			if (debug)
				logger.debug("Could not set feature " + XMLInputFactory.SUPPORT_DTD + "=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch (Exception e) {
			if (debug)
				logger.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
	}

	public static final StaxMarshaller defaultInstance = new StaxMarshaller();

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

	@Override
	public Object unmarshall(Reader reader) throws MarshallException {
		return unmarshall(reader,
				GmDeserializationOptions.deriveDefaults().absentifyMissingProperties(assignAbsenceInformationForMissingProperties).build());
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		if (in == null) {
			throw new MarshallException("The InputStream is null.");
		}
		try {
			XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
			return unmarshall(streamReader, options);
		} catch (XMLStreamException e) {
			throw new MarshallException(e);
		} catch (FactoryConfigurationError e) {
			throw new MarshallException(e);
		}
	}

	@Override
	public Object unmarshall(Reader reader, GmDeserializationOptions options) throws MarshallException {
		if (reader == null) {
			throw new MarshallException("The Reader is null.");
		}
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
		if (streamReader == null) {
			throw new MarshallException("The XMLStreamReader is null.");
		}

		DecoderFactoryContext factoryContext = options.findOrNull(TypeLookup.class) != null ? new DecoderFactoryContext() : decoderFactoryContext;

		try {
			DefaultHandlerImpl handler = new DefaultHandlerImpl(factoryContext, createEnhancedEntities, requiredTypesReceiver, options);

			handler.read(streamReader);

			Object value = handler.getValue();
			return value;
		} catch (Exception e) {
			throw new MarshallException("error while unmarshalling", e);
		}
	}

	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		if (out == null) {
			throw new MarshallException("The OutputStream is null.");
		}
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		if (out == null) {
			throw new MarshallException("The OutputStream is null.");
		}
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
		if (writer == null) {
			throw new MarshallException("The Writer is null.");
		}
		marshall(writer, encoding, value, GmSerializationOptions.deriveDefaults().build());
	}

	@Override
	public void marshall(Writer writer, Object value, GmSerializationOptions options) throws MarshallException {
		if (writer == null) {
			throw new MarshallException("The Writer is null.");
		}
		this.marshall(writer, "UTF-8", value, options);
	}

	public void marshall(Writer writer, String encoding, Object value, GmSerializationOptions options) throws MarshallException {
		if (writer == null) {
			throw new MarshallException("The Writer is null.");
		}
		try {
			writer.write("<?xml version='1.0' encoding='" + encoding + "'?>");
			writer.write("\n<?gm-xml version=\"4\"?>");
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
			writeGmData(context, writer, prettinessSupport, value);
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

	protected void writeGmData(EncodingContext context, Writer writer, PrettinessSupport prettinessSupport, Object rootValue)
			throws MarshallException, IOException {
		int indent = 0;
		prettinessSupport.writeLinefeed(writer, indent);
		writer.write("<gm-data>");

		ValueStaxNode valueNode = context.encodeValue(BaseType.INSTANCE, rootValue, false);

		EntityQueueNode entityQueueNode = context.getFirstNode();

		EntityNode firstNode = null;
		EntityNode lastNode = null;
		int entityInstanceCount = 0;
		while (entityQueueNode != null) {
			EntityNode entityNode = context.encodeEntity(entityQueueNode);
			if (firstNode == null) {
				firstNode = entityNode;
				lastNode = entityNode;
			} else {
				lastNode.next = entityNode;
				lastNode = entityNode;
			}
			entityQueueNode = entityQueueNode.next;
			entityInstanceCount++;
		}

		int childIndent = indent + 1;
		writeRequiredTypes(context, writer, prettinessSupport, childIndent);

		int valueIndent = indent + 2;
		prettinessSupport.writeLinefeed(writer, childIndent);
		writer.write("<root-value>");
		prettinessSupport.writeLinefeed(writer, valueIndent);
		valueNode.write(writer, prettinessSupport, valueIndent);
		prettinessSupport.writeLinefeed(writer, childIndent);
		writer.write("</root-value>");

		prettinessSupport.writeLinefeed(writer, childIndent);
		writer.write("<pool>");

		if (context.isStabilizingOrder()) {
			List<EntityNode> sortedNodes = new ArrayList<>(entityInstanceCount);
			EntityNode entityNode = firstNode;
			while (entityNode != null) {
				sortedNodes.add(entityNode);
				entityNode = entityNode.next;
			}

			Collections.sort(sortedNodes, EntityNodeComparator.INSTANCE);

			for (EntityNode outputEntityNode : sortedNodes) {
				outputEntityNode.write(writer, prettinessSupport, valueIndent);
			}

			prettinessSupport.writeLinefeed(writer, childIndent);
		} else {
			EntityNode entityNode = firstNode;
			while (entityNode != null) {
				entityNode.write(writer, prettinessSupport, valueIndent);
				entityNode = entityNode.next;
			}
			prettinessSupport.writeLinefeed(writer, childIndent);
		}
		writer.write("</pool>");

		prettinessSupport.writeLinefeed(writer, indent);
		writer.write("</gm-data>");
	}

	protected void writeRequiredTypes(EncodingContext context, Writer writer, PrettinessSupport prettinessSupport, int indent) throws IOException {
		prettinessSupport.writeLinefeed(writer, indent);
		writer.write("<required-types>");

		List<TypeInfo> typeInfos = new ArrayList<>(context.getRequiredTypeInfos());
		Collections.sort(typeInfos);
		int typeIndent = indent + 1;
		for (TypeInfo typeInfo : typeInfos) {
			GenericModelType type = typeInfo.type;

			prettinessSupport.writeLinefeed(writer, typeIndent);
			writer.write("<t alias='");
			writer.write(typeInfo.alias);

			if (type.getTypeCode() == TypeCode.entityType) {
				writer.write("' num='");
				writer.write(String.valueOf(typeInfo.getCount()));
			}
			writer.write("'>");
			writer.write(type.getTypeSignature());
			writer.write("</t>");
		}

		prettinessSupport.writeLinefeed(writer, indent);
		writer.write("</required-types>");
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
