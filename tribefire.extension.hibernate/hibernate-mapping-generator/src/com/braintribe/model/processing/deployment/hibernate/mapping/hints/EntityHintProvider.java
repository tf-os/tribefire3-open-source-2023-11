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
package com.braintribe.model.processing.deployment.hibernate.mapping.hints;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.deployment.hibernate.mapping.exception.HbmXmlGeneratorException;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityHintProvider {

	private ObjectMapper mapper;
	private TypeReference<Map<String, EntityHint>> entityHintTypeRef;
	private Map<String, EntityHint> entityHints;
	private Map<String, MetaData> metaDataHints;

	private CharacterMarshaller marshaller;

	private static final Logger log = Logger.getLogger(EntityHintProvider.class);

	public EntityHintProvider(String typeHints, File typeHintsFile) {
		log.trace(() -> getClass().getName() + " instantiated");

		// hints as string are prioritized over hints as file in case both are given

		if (!StringTools.isBlank(typeHints)) {
			log.info("Loading hints from string (typeHints)");

			initialize();
			loadTypeHints(typeHints);

		} else if (typeHintsFile != null) {
			log.info("Loading hints from file (typeHintsFile): " + typeHintsFile.getAbsolutePath());

			initialize();
			loadTypeHintsFile(typeHintsFile);

		} else {
			log.debug("Neither typeHints nor typeHintsFile was provided.");
		}
	}

	public EntityHint provide(String typeSignature) {
		return entityHints != null ? entityHints.get(typeSignature) : null;
	}

	public Map<String, MetaData> getMetaDataHints() {
		return metaDataHints;
	}

	protected void initialize() {
		// This shit tells the mapper the desired type of the Json is Map<String, EntityHint> 
		entityHintTypeRef = new TypeReference<Map<String, EntityHint>>() {
			/* Internationally left blank */
		};
		mapper = new ObjectMapper();
		marshaller = new JsonStreamMarshaller();
		entityHints = newMap();
		metaDataHints = newMap();
	}

	private void loadTypeHints(String typeHints) {
		validateJson(typeHints);

		log.debug(() -> "Parsing typeHints JSON: " + typeHints);

		try {
			loadGmMetaDataHints(typeHints);
		} catch (Exception gmEx) {
			try {
				loadPojoHints(typeHints);
			} catch (Exception pojoEx) {
				throw createFinalParsingException(pojoEx, gmEx);
			}
		}
	}

	private void loadTypeHintsFile(File typeHintsFile) {
		validateJson(typeHintsFile);

		log.debug(() -> "Parsing typeHintsFile JSON: " + typeHintsFile);

		try {
			loadGmMetaDataHints(typeHintsFile);

		} catch (Exception gmEx) {
			try {
				loadPojoHints(typeHintsFile);

			} catch (Exception pojoEx) {
				throw createFinalParsingException(pojoEx, gmEx);
			}
		}
	}

	private void loadGmMetaDataHints(String hints) throws Exception {
		loadGmMetaDataHints(new StringReader(hints));
	}

	private void loadGmMetaDataHints(File hintsFile) throws Exception {
		loadGmMetaDataHints(new FileReader(hintsFile));
	}

	private void loadGmMetaDataHints(Reader reader) throws Exception {
		try {
			tryLoadingGmMetaDataHints(reader);
			log.debug("Loaded hints as generic model entities (meta data)");
			
		} finally {
			IOTools.closeCloseable(reader, log);
		}
	}

	private void tryLoadingGmMetaDataHints(Reader reader) {
		metaDataHints = (Map<String, MetaData>) marshaller.unmarshall(reader);
		if (metaDataHints.isEmpty())
			return;

		// This is some seriously retarded code that supports both MetaData and EntityHint class being encoded as JSON
		// The only way to find out is to try parsing it as MD and see what happens
		// FastJsonMarshaller - which was deprecated - would simply fail
		// JsonStreamMarshaller - which is now used instead - doesn't fail, but reads the JSON as Map<String, Map<...>>
		// So if the values in the metaDataHints are not in fact MetaData instances, we throw an exception, like it was before
		// whoever thought this is was smart is an idiot
		// The whole hint thing seems weird and maybe could be removed???
		Object first = first(metaDataHints.values());
		if (first instanceof MetaData)
			return;
		
		metaDataHints.clear();
		throw new RuntimeException("Given JSON is not GM compatible, thus you should try to decode it with ObjectMapper");
	}

	private void loadPojoHints(String hints) throws Exception {
		entityHints = mapper.readValue(hints, entityHintTypeRef);
		log.debug("Loaded hints as plain java objects");
	}

	private void loadPojoHints(File hintsFile) throws Exception {
		entityHints = mapper.readValue(hintsFile, entityHintTypeRef);
		log.debug("Loaded hints as plain java objects");
	}

	private void validateJson(final String json) {
		try {
			isValidJson(mapper.getFactory().createParser(json));
		} catch (Exception e) {
			throw new HbmXmlGeneratorException("The given string is not a valid JSON object", e);
		}
	}

	private void validateJson(final File json) {
		try {
			isValidJson(mapper.getFactory().createParser(json));
		} catch (Exception e) {
			throw new HbmXmlGeneratorException("The given file does not contain a valid JSON object", e);
		}
	}

	private static boolean isValidJson(final JsonParser parser) throws Exception {
		while (parser.nextToken() != null) {
			/* Internationally left blank */
		}
		return true;
	}

	private static HbmXmlGeneratorException createFinalParsingException(Exception pojoEx, Exception gmEx) {
		log.error("Failed to load hints as plain java objects", pojoEx);
		log.error("Failed to load hints as generic model entities", gmEx);

		HbmXmlGeneratorException resultEx = new HbmXmlGeneratorException("Given JSON couldn't be parsed neither as plain object (" + exDesc(pojoEx)
				+ ") nor generic entity (" + exDesc(gmEx) + ") representations. Check surpressed exceptions for details");

		resultEx.addSuppressed(gmEx);
		resultEx.addSuppressed(pojoEx);

		return resultEx;
	}

	private static String exDesc(Exception e) {
		return e.getClass().getName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
	}

}
