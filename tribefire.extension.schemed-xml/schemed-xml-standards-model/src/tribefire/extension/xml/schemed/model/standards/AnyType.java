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

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

  
/**
 * represents the XSD type 'anyType'
 * @author pit
 *
 */
public interface AnyType extends GenericEntity {

	EntityType<AnyType> T = EntityTypes.T(AnyType.class);
	
	String attributes = "attributes";
	String name = "name";
	String properties = "properties";
	String value = "value";

	 
	List<AnyAttribute> getAttributes();
	void setAttributes(List<AnyAttribute> value);
	

	String getName();
	void setName(String value);


	List<GenericEntity> getProperties();
	void setProperties(List<GenericEntity> value);

	
	String getValue();
	void setValue(String value);

}
