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
package com.braintribe.xml.parser.experts;

import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.xml.parser.registry.ContentExpertFactory;

public class ComplexEntityExpertFactory implements ContentExpertFactory {
	protected EntityType<GenericEntity> type;
	protected String property;
	protected String signature;
	protected Map<String, Codec<GenericEntity, String>> codecs;
	
/*
	public ComplexEntityExpertFactory(EntityType<GenericEntity> type, String property) {
		this.type = type;
		this.property = property;
	}
*/	
	public ComplexEntityExpertFactory(String signature, String property) {
		this.type = GMF.getTypeReflection().getEntityType(signature);
		this.property = property;	
	}
	
	public ComplexEntityExpertFactory(String signature, String property, Map<String, Codec<GenericEntity, String>> codecs) {
		this.type = GMF.getTypeReflection().getEntityType(signature);
		this.property = property;
		this.codecs = codecs;
	}
	
	
	public ComplexEntityExpertFactory(String property) {
		this.type = null;
		this.property = property;
	}
	
	@Override
	public ContentExpert newInstance() {
		if (type != null) {
			return new ComplexEntityExpert(type, property, codecs);
		}				
		else if (signature != null) {
			return new ComplexEntityExpert(signature, property, codecs);
		}
		
		return null;
	}
}
