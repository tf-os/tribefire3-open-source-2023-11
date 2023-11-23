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
package com.braintribe.devrock.templates.model.artifact;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Creates a js-library artifact.")
public interface CreateJsLibrary extends CreateArtifact {

	EntityType<CreateJsLibrary> T = EntityTypes.T(CreateJsLibrary.class);
	
	@Initializer("'vscode'")
	@Override
	String getIde();

	@Description("Specifies whether or not this library is an asset - if set to 'true' according asset.man file will be projected.")
	@Alias("a")
	boolean getAsset();
	void setAsset(boolean asset);
	
	@Override
	default String template() {
		return "com.braintribe.devrock.templates:js-library-template#2.0";
	}
	
}
