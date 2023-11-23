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
package com.braintribe.model.processing.cortex;

import com.braintribe.gm._ServicePersistenceModel_;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.processing.cortex.priming.CortexModelsPersistenceInitializer;

import tribefire.cortex._TribefirePackagedCortexModel_;
import tribefire.cortex._TribefirePackagedCortexServiceModel_;
import tribefire.cortex._TribefireSyncModel_;
import tribefire.cortex._WorkbenchModel_;

/**
 * Names for cortex-related models.
 * <p>
 * All these models have standard globalId, i.e. it can be derived from the model name here via {@link Model#modelGlobalId(String)}.
 * 
 * @author peter.gazdik
 */
public interface CortexModelNames {

	/** Container for all classpath models that should be put into cortex as data. */
	String TF_SYNC_MODEL_NAME = _TribefireSyncModel_.reflection.name();
	/** Contains all classpath models that should be included in the cortex' model. This is a subset of {@link #TF_SYNC_MODEL_NAME sync model}. */
	String TF_PACKAGED_CORTEX_MODEL_NAME = _TribefirePackagedCortexModel_.reflection.name();
	/** Actual cortex' model. This model is generated dynamically by {@link CortexModelsPersistenceInitializer} */
	String TF_CORTEX_MODEL_NAME = "tribefire.cortex:tribefire-cortex-model";

	/** Contains all cp models that should be included in the cortex' service model. This is a subset of {@link #TF_SYNC_MODEL_NAME sync model}. */
	String TF_PACKAGED_CORTEX_SERVICE_MODEL_NAME = _TribefirePackagedCortexServiceModel_.reflection.name();
	/** Actual cortex' service model. This model is generated dynamically by {@link CortexModelsPersistenceInitializer} */
	String TF_CORTEX_SERVICE_MODEL_NAME = "tribefire.cortex:tribefire-cortex-service-model";

	/** Contains all cp models that should be included in the cortex workbench model. This is a subset of {@link #TF_SYNC_MODEL_NAME sync model}. */
	String TF_PACKAGED_CORTEX_WORKBENCH_MODEL_NAME = _WorkbenchModel_.reflection.name();
	/** Actual cortex' workbench model. This model is generated dynamically by {@link CortexModelsPersistenceInitializer} */
	String TF_CORTEX_WORKBENCH_MODEL_NAME = "tribefire.cortex:tribefire-cortex-workbench-model";

	String TF_SERVICE_PERSISTENCE_MODEL_NAME = _ServicePersistenceModel_.reflection.name();

}
