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

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.FileResource;

@Description("An instance of this type will be evaluated to the unmarshalled content read from the configured file.")
@PositionalArguments({ "file", "mimeType" })
public interface FromFile extends From {
	EntityType<FromFile> T = EntityTypes.T(FromFile.class);

	String vars = "hasVars";
	
	@Description("The file from which the content should be read")
	@Alias("f")
	@Mandatory
	FileResource getFile();
	void setFile(FileResource file);
	
	@Description("Support variables such as ${config.base} or ${env.YOUR_ENV_VAR} in a given yaml file.")
	@Alias("v")
	boolean getHasVars();
	void setHasVars(boolean hasVars);
}
