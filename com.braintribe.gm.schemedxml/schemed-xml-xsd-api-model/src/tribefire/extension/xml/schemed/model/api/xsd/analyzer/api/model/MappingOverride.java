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
 * a mapping override overrides the automatic naming procedure in the analyzer.
 * 
 * @author pit
 *
 */
@Description("a mapping override overrides the automatic naming procedure in the analyer.")
public interface MappingOverride extends GenericEntity{

final EntityType<MappingOverride> T = EntityTypes.T(MappingOverride.class);

	/**
	 * identifies the type or element or attribute in the xsd.<br7>
	 * if the {@link SchemaAddress} has no value set in #SchemaAddress.element, then the type is changed,
	 * otherwise the resulting property (mapped either from element or attribute) is changed 
	 * @return address - the {@link SchemaAddress} to the element 
	 */
	@Description("identifies the type or element or attribute in the xsd. If the SchemaAddress has no value set in SchemaAddress.element, then the type is changed, otherwise the resulting property (mapped either from element or attribute) is changed")
	@Alias("a")
	@Mandatory
	SchemaAddress getSchemaAddress();
	void setSchemaAddress( SchemaAddress address);
	
	/**
	 * 
	 * @return- the name to use instead of the automatic map
	 */
	@Description("the name to use instead of the automatically generated name")
	@Alias("n")
	@Mandatory
	String getNameOverride();
	void setNameOverride( String name);	

}
