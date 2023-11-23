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

import java.io.InputStream;
import java.io.Reader;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.EntityVisitorOption;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.TypeLookup;
import com.braintribe.codec.marshaller.stax.decoder.Decoder;
import com.braintribe.codec.marshaller.stax.decoder.RootDecoder;
import com.braintribe.codec.marshaller.stax.factory.DecoderFactoryContext;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.GmSession;

public class DefaultHandlerImpl extends DefaultHandler implements DecodingContext {

	private Decoder topDecoder;
	private final RootDecoder rootDecoder;
	private final DecoderFactoryContext decoderFactoryContext;
	private final Map<String, EntityRegistration> entitiesById = new HashMap<>();
	private final Map<String, TypeInfo4Read> typeInfoByAlias = new HashMap<>();
	private final Map<String, TypeInfo4Read> typeInfoByKey = new HashMap<>();

	private final AbsenceInformation absenceInformationForMissingProperties = GMF.absenceInformation();
	private int version = 1;
	private final GmDeserializationOptions options;

	protected boolean createEnhancedEntities = false;
	protected Consumer<Set<String>> requiredTypesReceiver;
	protected DecodingLenience decodingLenience;
	private final GmSession session;
	private Function<String, CustomType> typeLookup;
	private Consumer<? super GenericEntity> entityVisitor;

	public DefaultHandlerImpl(DecoderFactoryContext decoderFactoryContext, boolean createEnhancedEntities,
			Consumer<Set<String>> requiredTypesReceiver, GmDeserializationOptions options) {
		rootDecoder = new RootDecoder();
		topDecoder = rootDecoder;
		rootDecoder.decodingContext = this;
		this.decoderFactoryContext = decoderFactoryContext;
		this.createEnhancedEntities = createEnhancedEntities;
		this.requiredTypesReceiver = requiredTypesReceiver;
		this.session = options.getSession();
		this.options = options;
		DecodingLenience _decodingLenience = options.getDecodingLenience();
		if (_decodingLenience == null)
			_decodingLenience = new DecodingLenience(true);

		this.decodingLenience = _decodingLenience;

		typeLookup = options.findAttribute(TypeLookup.class).orElse(null);
		if (typeLookup == null)
			typeLookup = GMF.getTypeReflection()::findType;

		entityVisitor = options.findAttribute(EntityVisitorOption.class).orElse(null);
	}

	@Override
	public <T extends CustomType> T findType(String typeSignature) {
		return (T) typeLookup.apply(typeSignature);
	}

	public void read(InputStream in) throws XMLStreamException, FactoryConfigurationError, SAXException {
		XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(in);
		read(streamReader);
	}

	public void read(Reader reader) throws XMLStreamException, FactoryConfigurationError, SAXException {
		XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
		read(streamReader);
	}

	public void read(XMLStreamReader reader) throws XMLStreamException, FactoryConfigurationError, SAXException {
		XMLStreamReaderAttributes attributes = new XMLStreamReaderAttributes(reader);
		while (reader.hasNext()) {
			reader.next();
			switch (reader.getEventType()) {
				case XMLStreamReader.START_ELEMENT:
					try {
						String name = reader.getLocalName();
						// create new decoder which fits to the expectations of current topDecoder
						Decoder decoder = topDecoder.newDecoder(decoderFactoryContext, name, attributes);

						// initialize the decoder and push it on the stack
						decoder.decodingContext = this;
						decoder.parent = topDecoder;
						decoder.elementName = name;
						topDecoder = decoder;
						decoder.begin(attributes);
					} catch (Exception e) {
						throw new SAXException("error while resolving/creating decoder", e);
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					// pop from stack
					Decoder currentDecoder = topDecoder;

					try {
						currentDecoder.end();
					} catch (MarshallException e) {
						throw new SAXException("error in end element", e);
					}

					topDecoder = topDecoder.parent;

					// unlink to ease garbage collection
					currentDecoder.parent = null;
					break;
				case XMLStreamReader.CHARACTERS:
					topDecoder.appendCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				case XMLStreamReader.SPACE:
					topDecoder.appendCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
					break;
				case XMLStreamReader.PROCESSING_INSTRUCTION:
					processingInstruction(reader.getPITarget(), reader.getPIData());
					break;
			}
		}
	}

	@Override
	public void pushDelegateDecoder(Decoder decoder) {
		decoder.decodingContext = this;
		decoder.parent = topDecoder;
		decoder.elementName = topDecoder.elementName;
		topDecoder = decoder;
	}

	@Override
	public void popDelegateDecoder() {
		Decoder currentDecoder = topDecoder;
		topDecoder = topDecoder.parent;
		// unlink to ease garbage collection
		currentDecoder.parent = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		try {
			// create new decoder which fits to the expectations of current topDecoder
			Decoder decoder = topDecoder.newDecoder(decoderFactoryContext, qName, attributes);

			// initialize the decoder and push it on the stack
			decoder.decodingContext = this;
			decoder.parent = topDecoder;
			decoder.elementName = qName;
			topDecoder = decoder;
			decoder.begin(attributes);
		} catch (Exception e) {
			throw new SAXException("error while resolving/creating decoder", e);
		}
	}

	public Object getValue() {
		return rootDecoder.getValue();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// pop from stack
		Decoder currentDecoder = topDecoder;

		try {
			currentDecoder.end();
		} catch (MarshallException e) {
			throw new SAXException("error in end element", e);
		}

		topDecoder = topDecoder.parent;

		// unlink to ease garbage collection
		currentDecoder.parent = null;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		topDecoder.appendCharacters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		topDecoder.appendCharacters(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String rawData) throws SAXException {
		if (target.equals("gm-xml")) {
			Map<String, String> data = ProcessingInstructionParser.parseData(rawData);

			String encodedVersion = data.get("version");

			if (encodedVersion != null) {
				try {
					int _version = Integer.parseInt(encodedVersion);
					setVersion(_version);
				} catch (NumberFormatException e) {
					throw new SAXException("error while decoding gm-xml version", e);
				}
			}
		}

	}

	@Override
	public DecoderFactoryContext getDecoderFactoryContext() {
		return decoderFactoryContext;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public DateTimeFormatter getDateFormat() {
		return version > 2 ? DateFormats.dateFormat : DateFormats.legacyDateFormat;
	}

	@Override
	public PropertyAbsenceHelper providePropertyAbsenceHelper() {
		return options.getAbsentifyMissingProperties() ? new ActivePropertyAbsenceHelper(this) : InactivePropertyAbsenceHelper.instance;
	}

	@Override
	public AbsenceInformation getAbsenceInformationForMissingProperties() {
		return absenceInformationForMissingProperties;
	}

	@Override
	public GenericEntity lookupEntity(String ref) {
		EntityRegistration registration = entitiesById.get(ref);
		return registration != null ? registration.entity : null;
	}

	@Override
	public void registerTypeInfo(TypeInfo4Read typeInfo) {
		typeInfoByAlias.put(typeInfo.alias, typeInfo);
		typeInfoByKey.put(typeInfo.as, typeInfo);
	}

	@Override
	public TypeInfo4Read getTypeInfoByKey(String key) {
		return typeInfoByKey.get(key);
	}

	@Override
	public EntityRegistration acquireEntity(String ref) throws MarshallException {
		EntityRegistration registration = entitiesById.get(ref);

		if (registration == null) {
			int index = ref.indexOf('-', 1);
			if (index == -1) {
				throw new MarshallException("The reference " + ref + " is not a valid alias.");
			}
			String alias = ref.substring(0, index);
			TypeInfo4Read typeInfo = typeInfoByAlias.get(alias);

			if (typeInfo == null || typeInfo.type == null) {
				if (decodingLenience.isTypeLenient()) {
					registration = new EntityRegistration();
					entitiesById.put(ref, registration);
				} else
					throw new MarshallException("invalid ref id " + ref);
			} else {
				EntityType<?> entityType = (EntityType<?>) typeInfo.type;
				GenericEntity entity = session != null ? session.createRaw(entityType)
						: createEnhancedEntities ? entityType.createRaw() : entityType.createPlainRaw();
				registration = new EntityRegistration();
				registration.entity = entity;
				registration.typeInfo = typeInfo;
				entitiesById.put(ref, registration);

				if (entityVisitor != null)
					entityVisitor.accept(entity);
			}
		}

		return registration;
	}

	@Override
	public boolean isEnhanced() {
		return this.createEnhancedEntities;
	}

	@Override
	public void register(GenericEntity entity, String idString) throws MarshallException {

		if (entityVisitor != null)
			entityVisitor.accept(entity);

		if (idString == null || idString.isEmpty())
			return;

		EntityRegistration registration = new EntityRegistration();
		registration.entity = entity;

		EntityRegistration existingRegistration = entitiesById.put(idString, registration);

		if (existingRegistration != null) {
			EntityRegistrationListener existingListener = existingRegistration.listener;
			existingRegistration.listener = null;

			while (existingListener != null) {
				existingListener.onEntityRegistered(entity);
				existingListener = existingListener.successor;
			}
		}
	}

	@Override
	public Consumer<Set<String>> getRequiredTypesReceiver() {
		Consumer<Set<String>> receiver = this.options.getRequiredTypesReceiver();
		if (receiver != null) {
			return receiver;
		}
		return this.requiredTypesReceiver;
	}

	@Override
	public DecodingLenience getDecodingLenience() {
		return decodingLenience;
	}

	@Override
	public void addEntityRegistrationListener(String referenceId, EntityRegistrationListener listener) {

		EntityRegistration registration = new EntityRegistration();
		registration.listener = listener;

		EntityRegistration existingRegistration = entitiesById.put(referenceId, registration);

		if (existingRegistration != null) {
			EntityRegistrationListener existingListener = existingRegistration.listener;
			listener.successor = existingListener;
		}
	}

}
