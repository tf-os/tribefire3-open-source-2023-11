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
package tribefire.extension.xml.schemed.model.standards;

import com.braintribe.model.generic.annotation.GlobalId;

@GlobalId("type:com.braintribe.gm.schemedxml.standards.AnyType")  
public interface AnyType extends com.braintribe.model.generic.GenericEntity {

	com.braintribe.model.generic.reflection.EntityType<AnyType> T = com.braintribe.model.generic.reflection.EntityTypes.T(AnyType.class);
	
	String attributes = "attributes";
	String name = "name";
	String properties = "properties";
	String value = "value";

	@GlobalId("property:com.braintribe.gm.schemedxml.standards.AnyAttribute/attributes")  
	java.util.List<tribefire.extension.xml.schemed.model.standards.AnyAttribute> getAttributes();
	void setAttributes(java.util.List<tribefire.extension.xml.schemed.model.standards.AnyAttribute> value);
	
	@GlobalId("property:com.braintribe.gm.schemedxml.standards.AnyAttribute/name")
	java.lang.String getName();
	void setName(java.lang.String value);

	@GlobalId("property:com.braintribe.gm.schemedxml.standards.AnyAttribute/properties")
	java.util.List<com.braintribe.model.generic.GenericEntity> getProperties();
	void setProperties(java.util.List<com.braintribe.model.generic.GenericEntity> value);

	@GlobalId("property:com.braintribe.gm.schemedxml.standards.AnyAttribute/value")
	java.lang.String getValue();
	void setValue(java.lang.String value);

}
