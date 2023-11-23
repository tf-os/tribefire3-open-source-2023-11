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
package tribefire.cortex.deployables.initializer.support.wire.contract;

import java.util.List;

import com.braintribe.model.cortex.aspect.FulltextAspect;
import com.braintribe.model.cortex.aspect.IdGeneratorAspect;
import com.braintribe.model.cortex.aspect.SecurityAspect;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.cortex.processorrules.BidiPropertyStateChangeProcessorRule;
import com.braintribe.model.cortex.processorrules.MetaDataStateChangeProcessorRule;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.wire.api.space.WireSpace;

public interface DefaultDeployablesContract extends WireSpace {

	List<Deployable> defaultDeployables();

	List<AccessAspect> defaultAspects();

	List<StateChangeProcessorRule> defaultStateChangeProcessorRules();

	StateProcessingAspect stateProcessingAspect();

	FulltextAspect fulltextAspect();

	SecurityAspect securityAspect();

	IdGeneratorAspect idGeneratorAspect();

	BidiPropertyStateChangeProcessorRule bidiPropertyProcessorRule();

	MetaDataStateChangeProcessorRule metaDataProcessorRule();

}
