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
package com.braintribe.model.jvm.reflection;
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.mdec.ModelDeclaration;
import com.braintribe.model.generic.reflection.GenericModelException;

public class ModelDeclarationParser {
	
	private final static XMLInputFactory inputFactory;
	private final static Logger logger = Logger.getLogger(ModelDeclarationParser.class);
	
	static {
		inputFactory = XMLInputFactory.newInstance();

		boolean debug = logger.isDebugEnabled();
		try {
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
		} catch(Exception e) {
			if (debug) logger.debug("Could not set feature "+XMLInputFactory.SUPPORT_DTD+"=false", e);
		}

		try {
			inputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
		} catch(Exception e) {
			if (debug) logger.debug("Could not set feature javax.xml.stream.isSupportingExternalEntities=false", e);
		}
	}
	
	
	public static ModelDeclaration parse(InputStream in) {
		return parse(in, null);
	}
	
	public static ModelDeclaration parse(InputStream in, ModelDeclarationRegistry lookup) {
		
		try {
			ModelDeclaration cpDeclaration = ModelDeclaration.T.createPlain();

			List<ModelDeclaration> dependencies = new ArrayList<>();
			Set<String> types = new HashSet<>();
			Consumer<String> consumer = null;
			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
			try  {
				StringBuilder textBuilder = new StringBuilder();
				while(reader.hasNext()){
					reader.next();
					switch (reader.getEventType()) {
					case XMLStreamReader.START_ELEMENT:
						String tagName = reader.getLocalName();
						textBuilder.setLength(0);
						switch (tagName) {
						case "name": consumer = cpDeclaration::setName; break;
						case "groupId": consumer = cpDeclaration::setGroupId; break;
						case "artifactId": consumer = cpDeclaration::setArtifactId; break;
						case "version": consumer = cpDeclaration::setVersion; break;
						case "hash": consumer = cpDeclaration::setHash; break;
						
						case "dependencies": consumer = null; break;
						case "types": consumer = null; break;
						
						case "dependency":
							consumer = s -> {
								ModelDeclaration dependency = acquireDeclaration(lookup, s);
								dependencies.add(dependency);
							};
							break;
						case "type":
							consumer = s -> {
								types.add(s);
							};
							break;
						}
						
						break;
						
					case XMLStreamReader.END_ELEMENT:
						if (consumer != null) {
							consumer.accept(textBuilder.toString());
							consumer = null;
						}
						break;
						
					case XMLStreamReader.CHARACTERS:
					case XMLStreamReader.SPACE:
						textBuilder.append(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
						break;
					default:
						break;
					}
				}
			}
			finally {
				reader.close();
			}

			ModelDeclaration declaration = acquireDeclaration(lookup, cpDeclaration.getName());
			declaration.setGroupId(cpDeclaration.getGroupId());
			declaration.setArtifactId(cpDeclaration.getArtifactId());
			declaration.setVersion(cpDeclaration.getVersion());
			declaration.setHash(cpDeclaration.getHash());
			declaration.setDependencies(dependencies);
			declaration.setTypes(types);
			
			return declaration;
		} catch (Exception e) {
			throw new GenericModelException("error while parsing model declaration xml", e);
		}
	}

	private static ModelDeclaration acquireDeclaration(ModelDeclarationRegistry lookup, String name) {
		if (lookup != null) {
			return lookup.acquireModelDeclaration(name);
		}
		else {
			ModelDeclaration declaration = ModelDeclaration.T.create();
			declaration.setName(name);
			return declaration;
		}
	}

}
