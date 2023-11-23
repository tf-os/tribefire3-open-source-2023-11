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

import java.util.List;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * a {@link CollectionOverride} declares what multiple child should be a {@link Set} rather than a {@link List}<br/>
 * any element that is declared with maxOccurs="unbounded" (actually > 1) will be turned into a collection, default into a {@link List}
 * @author pit
 *
 */
@Description("a CollectionOverride declares what multiple child should be a Set rather than a List,  any element that is declared with maxOccurs=\"unbounded\" (actually > 1) will be turned into a collection, default into a List")
public interface CollectionOverride extends GenericEntity{

final EntityType<CollectionOverride> T = EntityTypes.T(CollectionOverride.class);

	/**
	 * 
	 * @return address - the {@link SchemaAddress} of the the element in the xsd that represents the collection
	 */
	@Description("the address of the element within the schema that is addressed")	
	@Alias("a")
	@Mandatory
	SchemaAddress getSchemaAddress();
	void setSchemaAddress( SchemaAddress address);
	
	/**
	 * @return - true if a {@link Set}, false if a {@link List}
	 */
	@Description("set to true if resulting collection should be a Set, false a List")
	@Alias("s")
	@Initializer("true")
	boolean getCollectionAsSet();
	void setCollectionAsSet( boolean asSet);

}
