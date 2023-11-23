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
package com.braintribe.model.generic.validation.log;

import java.util.ArrayList;
import java.util.HashMap;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.validation.ValidatorResult;

public class ValidationLog extends HashMap<GenericEntity, ArrayList<ValidatorResult>> {
	private static final long serialVersionUID = -3250735922573765286L;
	
	private LocalizedString name;
	private LocalizedString description;
	
	public void setName(LocalizedString name) {
		this.name = name;
	}
	
	public LocalizedString getName() {
		return name;
	}
	
	public void setDescription(LocalizedString description) {
		this.description = description;
	}
	
	public LocalizedString getDescription() {
		return description;
	}

}
