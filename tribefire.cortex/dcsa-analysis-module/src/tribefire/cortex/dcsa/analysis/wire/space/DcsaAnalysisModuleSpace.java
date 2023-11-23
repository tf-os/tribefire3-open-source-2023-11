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
package tribefire.cortex.dcsa.analysis.wire.space;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.dcsa.analysis.impl.CortexDcsaSsFiller;
import tribefire.cortex.model.api.dcsa.FillCortexDcsaSharedStorage;
import tribefire.module.wire.contract.HardwiredDeployablesContract;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class DcsaAnalysisModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	
	@Import
	private HardwiredDeployablesContract hardwiredDeployables;

	@Import
	private ResourceProcessingContract resourceProcessing;
	
	@Override
	public void bindHardwired() {
		hardwiredDeployables.bindOnExistingServiceDomain("cortex") //
				.serviceProcessor("cortexDcsaFiller", "Cortex DCSA SS Filler", FillCortexDcsaSharedStorage.T,
						(ctx, request) -> dcsaFiller().doIt(request));
	}

	private CortexDcsaSsFiller dcsaFiller() {
		CortexDcsaSsFiller bean = new CortexDcsaSsFiller();
		bean.setSharedStorageSupplier(tfPlatform.hardwiredDeployables().sharedStorageSupplier());
		bean.setMarshaller(new JsonStreamMarshaller());
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}
}
