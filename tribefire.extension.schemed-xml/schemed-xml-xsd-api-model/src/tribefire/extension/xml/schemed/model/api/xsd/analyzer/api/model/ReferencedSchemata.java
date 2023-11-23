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

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * the {@link ReferencedSchemata} is the set of xsd to use 
 * 
 * @author pit
 *
 */
@Description("the ReferencedSchemata is the set of referenced xsd by the main xsd")
public interface ReferencedSchemata extends GenericEntity{

	final EntityType<ReferencedSchemata> T = EntityTypes.T(ReferencedSchemata.class);
	
	/**
	 * @return - the list of {@link ReferencedSchema}
	 */
	@Description("the list of ReferencedSchema")
	@Alias("s")
	@Mandatory
	Set<ReferencedSchema> getReferencedSchemata();
	void setReferencedSchemata(Set<ReferencedSchema> schemata);
	
	/**	  
	 * @return - true if they are to be loaded dynamically (from the internet for instance), false if the they exist as resources 
	 */
	@Description("true if they are to be loaded dynamically (from the internet for instance), false if the they exist as resources. default is false")
	@Alias("l")
	@Initializer("false")
	boolean getLoadOnRequirement();
	void setLoadOnRequirement( boolean loadOnRequirement);	

	

}
