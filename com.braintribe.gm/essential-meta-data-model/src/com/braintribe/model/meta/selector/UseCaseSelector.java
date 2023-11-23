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
package com.braintribe.model.meta.selector;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Selector for "use-case" (expressed by string value) for which meta data should be active. This may be used to
 * disambiguate the meaning of some meta data.
 * <p>
 * For example the visibility may be set for two different reasons (security, GUI), so if one wanted to just prohibit a
 * property from being visible in one GUI component, he would simply append this selector with value like "myGuiPanel".
 */

public interface UseCaseSelector extends MetaDataSelector {

	EntityType<UseCaseSelector> T = EntityTypes.T(UseCaseSelector.class);

	public String getUseCase();
	public void setUseCase(String useCase);

}
