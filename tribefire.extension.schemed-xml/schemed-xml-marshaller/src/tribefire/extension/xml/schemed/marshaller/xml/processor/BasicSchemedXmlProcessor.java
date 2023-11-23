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
package tribefire.extension.xml.schemed.marshaller.xml.processor;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.model.generic.GenericEntity;

import tribefire.extension.xml.schemed.mapper.api.MapperInfoRegistry;
import tribefire.extension.xml.schemed.marshaller.xml.api.SchemedXmlProcessor;
import tribefire.extension.xml.schemed.marshaller.xml.options.IgnoreTags;
import tribefire.extension.xml.schemed.marshaller.xml.processor.commons.QNameWrapperCodec;

public class BasicSchemedXmlProcessor implements SchemedXmlProcessor {

	private MapperInfoRegistry registry;
	private Set<QName> tagsToIgnore = CodingSet.createHashSetBased(new QNameWrapperCodec());
	private boolean verbose;

	public BasicSchemedXmlProcessor(MapperInfoRegistry registry) {
		this.registry = registry;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * com.braintribe.marshaller.schemedXml.xml.marshaller.processor.MappingProcessor#read(javax.xml.stream.XMLStreamReader) */
	@Override
	public GenericEntity read(XMLStreamReader reader, GmDeserializationOptions options) throws XMLStreamException {
		if (options != null) {
			List<String> list = options.findOrNull(IgnoreTags.class);
			if (list != null) {
				for (String tag : list) {
					QName qname = new QName(tag);
					tagsToIgnore.add(qname);
				}
			}
		}

		SchemedXmlReader xmlReader = new SchemedXmlReader(registry);
		int depth = 0;

		while (reader.hasNext()) {
			switch (reader.getEventType()) {
				case XMLStreamConstants.START_ELEMENT:
					if (tagsToIgnore.contains(reader.getName())) {
						skip(reader);
					} else {
						xmlReader.processStartElement(reader, options);
						if (verbose)
							System.out.println(msgG(depth++, reader.getName()));
					}
					break;
				case XMLStreamConstants.END_ELEMENT: {
					xmlReader.processEndElement(reader, options);
					if (verbose)
						System.out.println(msgG(--depth, reader.getName()));
					break;
				}

				case XMLStreamConstants.CHARACTERS: {
					xmlReader.processCharacters(reader, options);
					if (verbose)
						System.out.println(msgG(depth, new QName("characters")));
					break;
				}
				case XMLStreamConstants.END_DOCUMENT: {
					return xmlReader.getRoot();
				}
				default:
					if (verbose)
						System.out.println(msgG(depth, new QName("type :" + reader.getEventType())));
					break;
			}
			reader.next();
		}

		// throw new IllegalStateException("no proper ending of document encountered");
		return xmlReader.getRoot();
	}

	private String msgG(int depth, QName name) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			buffer.append("\t");
		}
		buffer.append(name);
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.marshaller.schemedXml.xml.marshaller.processor.MappingProcessor#write(javax.xml.stream.
	 * XMLStreamWriter, com.braintribe.model.generic.GenericEntity) */
	@Override
	public void write(XMLStreamWriter writer, GenericEntity value, GmSerializationOptions options) throws XMLStreamException {
		SchemedXmlWriter xmlWriter = new SchemedXmlWriter(registry);

		writer.writeStartDocument();

		writer.writeProcessingInstruction("xml", "version='1.0' encoding='UTF-8'");

		xmlWriter.process(writer, value, options);

		writer.writeEndDocument();

		writer.flush();
		writer.close();
	}
	/**
	 * skip a tag - careful : this requires that the caller DOES call reader.next() before it actually starts processing,
	 * otherwise, this here will be called out of sequence and gobble-up any tags..
	 * 
	 * @param reader
	 * @throws XMLStreamException
	 */
	protected static void skip(XMLStreamReader reader) throws XMLStreamException {
		Stack<String> stack = new Stack<>();
		stack.push(reader.getLocalName());

		reader.next();
		while (reader.hasNext()) {
			int eventType = reader.getEventType();
			switch (eventType) {
				case XMLStreamConstants.START_ELEMENT: {
					stack.push(reader.getLocalName());
					break;
				}

				case XMLStreamConstants.END_ELEMENT: {
					stack.pop();
					if (stack.isEmpty())
						return;
				}
				default:
					break;
			}
			reader.next();
		}
	}
}
