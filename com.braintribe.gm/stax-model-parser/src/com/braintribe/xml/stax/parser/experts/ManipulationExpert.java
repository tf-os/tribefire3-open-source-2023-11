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
package com.braintribe.xml.stax.parser.experts;

import java.util.UUID;

import org.xml.sax.Attributes;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.manipulation.parser.api.MutableGmmlParserConfiguration;
import com.braintribe.model.processing.manipulation.parser.impl.Gmml;
import com.braintribe.model.processing.manipulation.parser.impl.ManipulatorParser;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

public class ManipulationExpert extends AbstractContentExpert implements ContentExpert {
	private static MutableGmmlParserConfiguration configuration;
	
	private GenericEntity parentEntity;

	@Override
	public void startElement(ContentExpert parent, String uri, String localName, String qName, Attributes atts){
		parentEntity = parent.getInstance();
	
	}

	@Override
	public void endElement(ContentExpert parent, String uri, String localName, String qName) {
	
		// inject first line into expression 
		String expression = buffer.toString();
		String globalId = UUID.randomUUID().toString();
		parentEntity.setGlobalId(globalId);
		String injected = "$entity=($entityType=" + parentEntity.entityType().getTypeSignature() + ")('"+ globalId + "')"; 
		expression = injected + "\n" + expression;
		
		// setup session 
		BasicManagedGmSession session = new BasicManagedGmSession();
		session.attach(parentEntity);		

		// lazy create configuration 
		if (configuration == null) {
			configuration = Gmml.configuration();
			//configuration.setParseSingleBlock(true);
			configuration.setLenient(true);
		}
		// call parser 
		try {
			ManipulatorParser.parse(expression, session, configuration);
		} catch (Exception e) {
			throw new IllegalStateException("ManipulationParser cannot process entity ["+ qName + "]'s expression [" + expression + "]", e);
		}

	}

	@Override
	public void attach(ContentExpert child) {	
	}

	@Override
	public Object getPayload() {
		return null;
	}

	@Override
	public GenericEntity getInstance() {	
		return null;
	}

	@Override
	public EntityType<GenericEntity> getType() {
		return null;
	}
	
	


}
