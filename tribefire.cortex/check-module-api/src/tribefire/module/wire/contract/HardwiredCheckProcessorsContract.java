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
package tribefire.module.wire.contract;

import java.util.function.Supplier;

import com.braintribe.model.extensiondeployment.check.HardwiredCheckProcessor;
import com.braintribe.model.extensiondeployment.check.HardwiredParameterizedCheckProcessor;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.check.api.ParameterizedCheckProcessor;

/**
 * Offers methods for binding {@link CheckProcessor}s and {@link ParameterizedCheckProcessor}.
 * 
 * @see HardwiredDeployablesContract
 */
public interface HardwiredCheckProcessorsContract extends HardwiredDeployablesContract {

	default HardwiredCheckProcessor bindCheckProcessor(String externalId, String name, CheckProcessor checkProcessor) {
		return bindCheckProcessor(externalId, name, () -> checkProcessor);
	}

	HardwiredCheckProcessor bindCheckProcessor(String externalId, String name, Supplier<CheckProcessor> checkProcessorSupplier);

	default HardwiredParameterizedCheckProcessor bindParameterizedCheckProcessor( //
			String externalId, String name, ParameterizedCheckProcessor<?> parameterizedCheckProcessor) {
		return bindParameterizedCheckProcessor(externalId, name, () -> parameterizedCheckProcessor);
	}

	HardwiredParameterizedCheckProcessor bindParameterizedCheckProcessor( //
			String externalId, String name, Supplier<ParameterizedCheckProcessor<?>> parameterizedCheckProcessorSupplier);

}
