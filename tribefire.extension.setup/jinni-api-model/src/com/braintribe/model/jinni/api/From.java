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
package com.braintribe.model.jinni.api;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Abstract
public interface From extends GenericEntity {
	EntityType<From> T = EntityTypes.T(From.class);

	@Description("The mimetype defining the serialization format for the file input. "
			+ "Possible values: text/yaml, application/x-yaml, text/xml, application/json, gm/bin, gm/jse, gm/man")
	@Alias("m")
	@Initializer("'application/yaml'")
	@Mandatory
	String getMimeType();
	void setMimeType(String mimeType);
	
	@Description("Entities are created without initialized property default values to exactly reproduce a specific entity.")
	@Alias("r")
	boolean getReproduce();
	void setReproduce(boolean reproduce);
}
