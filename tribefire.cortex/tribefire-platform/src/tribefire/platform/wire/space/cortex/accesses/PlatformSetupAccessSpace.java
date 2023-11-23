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
package tribefire.platform.wire.space.cortex.accesses;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.platformsetup.PlatformSetupStateChangeProcessor;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.impl.setup.PlatformSetupSupplier;
import tribefire.platform.wire.space.cortex.accesses.TribefireProductModels.TribefireProductModel;
import tribefire.platform.wire.space.cortex.deployment.StateChangeProcessorsSpace;

@Managed
public class PlatformSetupAccessSpace extends CollaborativeSystemAccessSpaceBase {

	private static final String id = TribefireConstants.ACCESS_SETUP;
	private static final String name = "Platform Setup";

	private static final TribefireProductModel model = TribefireProductModels.platformSetupAccessModel;
	private static final String modelName = model.modelName;
	private static final String serviceModelName = "tribefire.cortex:platform-setup-service-model";

	@Import
	private StateChangeProcessorsSpace stateChangeProcessors;

	// @formatter:off
	@Override public String id() { return id; }
	@Override public String name() { return name; }
	@Override public String modelName() { return modelName; }
	@Override public String serviceModelName() { return serviceModelName; }
	// @formatter:on

	@Override
	protected List<AccessAspect> aopAspects() {
		return list( //
				aspects.security(), //
				aspects.fulltext(), //
				stateProcessingAspect() //
		);
	}

	/* IMPORTANT: This should not be @Managed, the returned bean is already managed. */
	@Override
	public CollaborativeSmoodAccess access() {
		return systemAccessCommons.collaborativeSmoodAccess(id());
	}

	@Override
	protected List<StateChangeProcessorRule> stateChangeProcessorRules() {
		return list( //
				stateChangeProcessors.bidiProperty(), //
				new PlatformSetupStateChangeProcessor() //
		);
	}

	@Override
	public HardwiredAccessSpaceBase workbenchAccessSpace() {
		return systemAccesses.platformSetupWorkbench();
	}

	@Managed
	public PlatformSetupSupplier platformSetupSupplier() {
		PlatformSetupSupplier bean = new PlatformSetupSupplier();
		bean.setSetupSessionFactory(sessionProvider());
		return bean;
	}

}
