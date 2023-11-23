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
package tribefire.module.api;

import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public interface HardwiredConfigurationModelBinder {

	HardwiredConfigurationModelBinder dependency(Model model);

	/** Eager instantiation version of {@link #serviceProcessor(String, String, EntityType, Supplier)} */
	default <T extends ServiceRequest> HardwiredConfigurationModelBinder serviceProcessor( //
			String externalId, String name, EntityType<T> requestType, ServiceProcessor<? super T, ?> serviceProcessor) {
		return serviceProcessor(externalId, name, requestType, () -> serviceProcessor);
	}

	<T extends ServiceRequest> HardwiredConfigurationModelBinder serviceProcessor( //
			String externalId, String name, EntityType<T> requestType, Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier);

}
