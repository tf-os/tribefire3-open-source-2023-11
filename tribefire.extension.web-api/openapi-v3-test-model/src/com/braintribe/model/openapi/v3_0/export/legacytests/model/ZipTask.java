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
package com.braintribe.model.openapi.v3_0.export.legacytests.model;

import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;

public interface ZipTask extends GenericEntity {

	final EntityType<ZipTask> T = EntityTypes.T(ZipTask.class);

	List<Resource> getInputResources();
	void setInputResources(List<Resource> inputResources);

	CallStreamCapture getCapture();
	void setCapture(CallStreamCapture capture);

	boolean getGenerateOutput();
	void setGenerateOutput(boolean generateOutput);

	String getName();
	void setName(String name);
}