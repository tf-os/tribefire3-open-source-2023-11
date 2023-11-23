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
 * a {@link BidirectionalLink} describes where a backlink should be added,
 * i.e. where a child should receive a property that points back to its parent. 
 * the backlink will automatically be set be the marshaller during read, yet not written during write.
 *  
 * @author pit
 *
 */
@Description("a {@link BidirectionalLink} describes where a backlink should be added, i.e. where a child should receive a property that points back to its parent. The backlink will automatically be set be the marshaller during read, yet not written during write.")
public interface BidirectionalLink extends GenericEntity {
	
	final EntityType<BidirectionalLink> T = EntityTypes.T(BidirectionalLink.class);

	/**
	 * defines that child that should get the back-linking property
	 * @return - the {@link SchemaAddress} that describes the point in the xsd
	 */
	@Description("the address of the element within the schema that should get the backlink")
	@Alias( "a")
	@Mandatory
	SchemaAddress getSchemaAddress();
	void setSchemaAddress( SchemaAddress address);
	
	/** 
	 * @return - property name of the backlink property to be added to child as defined in the {@link SchemaAddress}
	 */
	@Description("the name of property to contain the backlink")
	@Alias("b")
	@Mandatory
	String getBacklinkProperty();
	void setBacklinkProperty(String poperty);	
	
	
}
