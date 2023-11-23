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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.braintribe.logging.Logger;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StringTools;

/**
 * <p>
 * Provides backward compatibility for non-json (custom format) based entity hints
 * 
 * @deprecated non-json (custom format) hints are no longer supported
 */
@Deprecated
public class EntityHintTextParser {
	
	private Map<String, EntityHint> entityHints = new HashMap<String, EntityHint>();
	
	private static Pattern typeDefPattern;
	
	private static final Logger log = Logger.getLogger(EntityHintTextParser.class);
	
	public Map<String, EntityHint> parse(String customFormatTypeHints) {
		String [] tokens = customFormatTypeHints.split(",");
		for (String token : tokens) {
			registerPropertyHint(token.trim());
		}
		return entityHints;
	}
	
	public Map<String, EntityHint> parse(File customFormatTypeHintsFile) {
		String contents = null;
		try {
			contents = IOTools.slurp(customFormatTypeHintsFile, "UTF-8");
		} catch (IOException e) {
			log.error("failed to read content of typeHintsFile [ "+customFormatTypeHintsFile+" ] due to: "+e.getMessage(), e);
		}
		String [] lines = contents.split("\n");
		for (String token : lines) {
			registerPropertyHint(token.trim());
		}
		
		return entityHints;
	}
	
	private void registerPropertyHint(String hintTuple) {
		
		String entityTypeSignature = null, propertyName = null;
		
		if (log.isDebugEnabled())
			log.debug("Start of tuple parsing: "+hintTuple);
		
		if (hintTuple == null || hintTuple.trim().isEmpty()) return;
		String[] tuple = hintTuple.split(":");
		if (tuple.length < 3) {
			log.warn("Illegal hint line, will be ignored: "+hintTuple);
			return;
		}
		
		entityTypeSignature = tuple[0];
		
		if (StringTools.isBlank(entityTypeSignature))
			return;
		
		if (entityHints.get(entityTypeSignature) == null) {
			EntityHint entityHint = new EntityHint();
			entityHint.properties = new HashMap<String, PropertyHint>();
			entityHints.put(entityTypeSignature, entityHint);
		}
		
		propertyName = tuple[1];
		
		PropertyHint propertyHint = entityHints.get(entityTypeSignature).properties.get(propertyName);
		if (propertyHint == null) {
			propertyHint = new PropertyHint();
			entityHints.get(entityTypeSignature).properties.put(propertyName, propertyHint);
		}
		
		enrichPropertyHint(propertyHint, tuple[2]);
		if (tuple.length > 3)
			enrichPropertyHint(propertyHint, tuple[3]);
	}
	
	
	private static void enrichPropertyHint(PropertyHint propertyHint, String typeDefinitionString) {
		
		if (StringTools.isBlank(typeDefinitionString))
			return;

	    Matcher m = getTypeDefPattern().matcher(typeDefinitionString.replaceAll("^\\[k\\]", ""));
		
	    if (!m.find()) {
	    	log.warn("The type definition string \""+typeDefinitionString+"\" did not match the expected pattern "+getTypeDefPattern().pattern());
	    	return;
	    }

		Long maxLength = 0L;
	    if (m.group(3) != null)
	    	maxLength = Long.parseLong(m.group(3));
	    
	    String type = m.group(1);
	    
	    if (typeDefinitionString.toLowerCase().startsWith("[k]")) {
	    	propertyHint.keyType = type;
	    	
	    	propertyHint.keyLength = maxLength;
	    } else {
	    	propertyHint.type = type;
	    	propertyHint.length = maxLength;
	    }
	}
	
	private static Pattern getTypeDefPattern() {
		
		if (typeDefPattern == null) {
			typeDefPattern = Pattern.compile("^(\\w+)(\\((\\d+)?\\))?$");
		}
		return typeDefPattern;
		
	}

}
