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
package com.braintribe.model.access.collaboration.distributed.api.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * Base type for {@link CsaOperation}s related to {@link Resource}s stored in the CSA.
 * 
 * @author peter.gazdik
 */
@Abstract
public interface CsaBinaryOperation extends CsaOperation {

	EntityType<CsaBinaryOperation> T = EntityTypes.T(CsaBinaryOperation.class);

	String resourceRelativePath = "resourceRelativePath";

	/** The relative path under which the actual binary data can be located, typically as a file in the file system. */
	String getResourceRelativePath();
	void setResourceRelativePath(String resourceRelativePath);

}
