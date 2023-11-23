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
package com.braintribe.gwt.customization.client.cmd;

import java.util.function.Function;

import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.override.GmCustomTypeOverride;

/**
 * @author peter.gazdik
 */
public interface GmMappers {

	Function<GmType, String> typeToSignature = gmType -> gmType.getTypeSignature();
	Function<GmCustomTypeOverride, String> typeOverrideToSignature = typeOverride -> typeOverride.addressedType().getTypeSignature();

}
