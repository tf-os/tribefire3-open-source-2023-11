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
package com.braintribe.model.processing.accessory.api;

import com.braintribe.model.meta.data.components.ModelExtension;
import com.braintribe.model.processing.accessory.impl.PlatformModelAccessoryFactory;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;

/**
 * Supplier of {@link PlatformModelEssentials} for given {@link #getForAccess(String, String, boolean) access},
 * {@link #getForServiceDomain(String, String, boolean) serviceDomain} or {@link #getForModelName(String, String) modelName}.
 * <p>
 * When resolving for access and serviceDomain, a boolean parameter called <tt>extended</tt> can be used to extend the actual models according to the
 * corresponding {@link ModelExtension} meta-data.
 * <p>
 * This is used as the supplier of model related data for the {@link PlatformModelAccessoryFactory}. The reason for the split in two layers is that
 * one can have different {@link CmdResolver meta data resolvers} for given model/access/serviceDomain, e.g. configured with different aspects like
 * user roles. Then, different {@link ModelAccessory}s are needed, but the model and {@link ModelOracle} can be shared. This supplier is responsible
 * for re-using these objects as much as possible, while the factory uses them for multiple model accessories.
 * 
 * @author peter.gazdik
 */
public interface PlatformModelEssentialsSupplier {

	PlatformModelEssentials getForAccess(String accessId, String perspective, boolean extended);

	PlatformModelEssentials getForServiceDomain(String serviceDomainId, String perspective, boolean extended);

	PlatformModelEssentials getForModelName(String modelName, String perspective);

}
