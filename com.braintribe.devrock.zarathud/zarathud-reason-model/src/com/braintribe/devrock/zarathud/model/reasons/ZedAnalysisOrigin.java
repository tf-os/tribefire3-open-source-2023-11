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
package com.braintribe.devrock.zarathud.model.reasons;

import java.util.Date;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.zarathud.model.data.Artifact;

@Abstract
public interface ZedAnalysisOrigin extends Reason {
	
	EntityType<ZedAnalysisOrigin> T = EntityTypes.T(ZedAnalysisOrigin.class);
	
	String artifact = "artifact";
	String timestamp = "timestamp";

	Artifact getArtifact();
	void setArtifact(Artifact value);

	@Initializer("now()")
	Date getTimestamp();
	void setTimestamp(Date value);


}
