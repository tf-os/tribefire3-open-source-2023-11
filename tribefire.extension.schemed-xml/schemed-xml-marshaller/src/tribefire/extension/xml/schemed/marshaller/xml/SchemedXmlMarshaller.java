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
package tribefire.extension.xml.schemed.marshaller.xml;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.xml.schemed.mapper.api.MapperInfoRegistry;
import tribefire.extension.xml.schemed.mapper.structure.BasicMapperInfoRegistry;
import tribefire.extension.xml.schemed.marshaller.xml.api.SchemedXmlProcessor;
import tribefire.extension.xml.schemed.marshaller.xml.processor.BasicSchemedXmlProcessor;
import tribefire.extension.xml.schemed.marshaller.xsd.IndentingXmlStreamWriter;

/**
 * @author pit
 *
 */
public class SchemedXmlMarshaller implements Marshaller {
	private static Logger log = Logger.getLogger(SchemedXmlMarshaller.class);
	private static XMLInputFactory inputFactory;
	private static XMLOutputFactory outputFactory;
	private boolean indenting = true;
	private GmMetaModel mappingMetaModel;
	private MapperInfoRegistry mappingRegistry;

	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = log.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch (Exception e) {
			if (debug)
				log.debug("Could not set feature " + XMLInputFactory.SUPPORT_DTD + "=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch (Exception e) {
			if (debug)
				log.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}

		outputFactory = XMLOutputFactory.newInstance();
	}

	@Configurable
	@Required
	public void setMappingMetaModel(GmMetaModel mappingMetaModel) {
		this.mappingMetaModel = mappingMetaModel;
		mappingRegistry = null;
	}

	@Configurable
	public void setMappingRegistry(MapperInfoRegistry mappingRegistry) {
		this.mappingRegistry = mappingRegistry;
	}

	private Object schemedMarshallerInitializingMonitor = new Object();

	/**
	 * initialize the {@link MappingRegistry} from the mapping {@link GmMetaModel}
	 */
	private void initialize() {
		if (mappingRegistry != null)
			return;

		synchronized (schemedMarshallerInitializingMonitor) {
			if (mappingRegistry != null)
				return;

			BasicMapperInfoRegistry infoRegistry = new BasicMapperInfoRegistry();
			infoRegistry.initializeWithMappingMetaModel(mappingMetaModel);
			mappingRegistry = infoRegistry;
		}
	}

	@Override
	public Object unmarshall(InputStream in) throws MarshallException {
		return unmarshall(in, GmDeserializationOptions.deriveDefaults().build());
	}

	@Override
	public Object unmarshall(InputStream in, GmDeserializationOptions options) throws MarshallException {
		XMLStreamReader reader;
		try {
			reader = inputFactory.createXMLStreamReader(in);
			return unmarshall(options, reader);
		} catch (Exception e) {
			throw new MarshallException(e);
		}
	}

	/**
	 * unmarshalls the {@link GenericEntity} (actually a {@link GenericEntity} from the XML
	 * 
	 * @param reader
	 *            - the {@link XMLStreamReader}
	 * @return - the {@link GenericEntity} contained
	 * @throws XMLStreamException
	 *             - if anything goes wrong
	 */
	private GenericEntity unmarshall(GmDeserializationOptions options, XMLStreamReader reader) throws XMLStreamException {
		initialize();
		SchemedXmlProcessor processor = new BasicSchemedXmlProcessor(mappingRegistry);
		GenericEntity entity = processor.read(reader, options);
		return entity;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.codec.marshaller.api.Marshaller#marshall(java.io.OutputStream, java.lang.Object) */
	@Override
	public void marshall(OutputStream out, Object value) throws MarshallException {
		marshall(out, value, GmSerializationOptions.deriveDefaults().build());
	}

	/* (non-Javadoc)
	 * 
	 * @see com.braintribe.codec.marshaller.api.Marshaller#marshall(java.io.OutputStream, java.lang.Object,
	 * com.braintribe.codec.marshaller.api.GmSerializationOptions) */
	@Override
	public void marshall(OutputStream out, Object value, GmSerializationOptions options) throws MarshallException {
		XMLStreamWriter writer;
		try {
			writer = !indenting ? outputFactory.createXMLStreamWriter(out, "UTF-8") : new IndentingXmlStreamWriter(out, "UTF-8");
			marshall(options, writer, value);
		} catch (Exception e) {
			throw new MarshallException(e);
		}
	}

	/**
	 * actual writer work horse
	 * 
	 * @param writer
	 *            - a instance of the {@link XMLStreamWriter}
	 * @param value
	 *            - the {@link Object} to write, acutally a {@link GenericEntity} is expected
	 * @throws XMLStreamException
	 */
	public void marshall(GmSerializationOptions options, XMLStreamWriter writer, Object value) throws XMLStreamException {
		initialize();
		SchemedXmlProcessor processor = new BasicSchemedXmlProcessor(mappingRegistry);
		processor.write(writer, (GenericEntity) value, options);
	}

}
