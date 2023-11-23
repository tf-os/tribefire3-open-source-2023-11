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
package com.braintribe.gwt.customization.client.tests.model.basic.proto.mi;

import com.braintribe.gwt.customization.client.tests.model.basic.non_dynamic.CI1;
import com.braintribe.gwt.customization.client.tests.model.basic.non_dynamic.CI1_COPY;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


public interface RI_CI_CI_COPY extends CI1, CI1_COPY {

	
	EntityType<RI_CI_CI_COPY> T = EntityTypes.T(RI_CI_CI_COPY.class);
	
}
