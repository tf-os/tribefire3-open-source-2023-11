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
import com.braintribe.model.resource.Resource;

/**
 * a {@link ReferencedSchema} is an external schema that needs to be incorporated in the translation 
 * @author pit
 *
 */
@Description("a ReferencedSchema is an external schema that needs to be incorporated in the translation")
public interface ReferencedSchema extends GenericEntity{
	
	final EntityType<ReferencedSchema> T = EntityTypes.T(ReferencedSchema.class);

	/** 
	 * @return - the URI of schema - as it is referenced by others
	 */
	@Description("the URI of schema - as it is referenced by others")
	@Alias("u")
	@Mandatory
	String getUri();
	void setUri(String key);
	
	/** 
	 * @return - {@link Resource} that contains the referenced schema
	 */
	@Description(" the Resource that contains the referenced schema")
	@Alias("s")
	@Mandatory
	Resource getSchema();
	void setSchema( Resource schema);
}
