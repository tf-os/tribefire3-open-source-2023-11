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
package com.braintribe.tribefire.jinni.wire.space;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.tribefire.jinni.wire.contract.HelpProcessorConfigurationContract;
import com.braintribe.tribefire.jinni.wire.contract.ServiceDomainContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class HelpProcessorConfigurationSpace implements HelpProcessorConfigurationContract {

	@Import
	private ServiceDomainContract serviceDomain;

	@Override
	public ModelAccessory modelAccessory() {
		return serviceDomain.modelAccessory();
	}

	@Override
	public EntityType<?> optionsType() {
		return null;
	}

}
