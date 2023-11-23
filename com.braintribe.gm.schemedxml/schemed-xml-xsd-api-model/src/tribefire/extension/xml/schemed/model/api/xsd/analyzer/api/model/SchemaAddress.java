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
package tribefire.extension.xml.schemed.model.api.xsd.analyzer.api.model;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a {@link SchemaAddress} identifies a point in a schema, i.e. a type and one of its properties. 
 * if only the parent part is declared, then a type is meant to be targeted, otherwise, 
 * it's the element aka property. If the parent's type null, then a top-level element is to 
 * be targeted. 
 * @author pit
 *
 */
@Description("a SchemaAddress identifies a point in a schema, i.e. a type and one of its properties. If only the parent part is declared, then a type is meant to be targeted, otherwise, it's the element aka property. If the parent's type null, then a top-level element is to be targeted.")
public interface SchemaAddress extends GenericEntity {
	
	final EntityType<SchemaAddress> T = EntityTypes.T(SchemaAddress.class);

	/**	
	 * @return - the name of the complex type that is to be addressed
	 */
	@Description("the name of the complex type that is to be addressed")
	@Alias("t")
	@Mandatory
	String getParent();
	void setParent(String parent);
	
	/**
	 * @return - the name of the property or attribute that is to be addressed (or null if only the type's targeted)
	 */
	@Description("the name of the property or attribute that is to be addressed (or null if only the type's targeted)")
	@Alias("e")
	String getElement();
	void setElement( String element);
}
