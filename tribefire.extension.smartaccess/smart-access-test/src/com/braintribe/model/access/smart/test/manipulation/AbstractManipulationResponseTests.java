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
package com.braintribe.model.access.smart.test.manipulation;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.access.smart.test.base.AccessSetup;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.SessionRunnable;

/**
 * 
 * @author peter.gazdik
 */
public class AbstractManipulationResponseTests extends AbstractManipulationsTests {

	protected ManipulationResponse configuredResponseA;
	protected ManipulationResponse configuredResponseB;

	@Override
	protected AccessSetup newAccessSetup() {
		return new SpecialAccessSetup(getSmartSetupProvider().setup());
	}

	class SpecialAccessSetup extends AccessSetup {

		public SpecialAccessSetup(SmartMappingSetup setup) {
			super(setup);
		}

		@Override
		protected SmoodAccess newSmoodAccess(final String... _partitions) {
			return new SmoodAccess() {

				@Override
				public synchronized ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest)
						throws ModelAccessException {

					ManipulationResponse response = super.applyManipulation(manipulationRequest);
					ManipulationResponse configuredResponse = _partitions[0].endsWith("A") ? configuredResponseA : configuredResponseB;

					return configuredResponse == null ? response : configuredResponse;
				}

			};
		}
	}

	protected void recordResponseA(SessionRunnable r) {
		configuredResponseA = recordResponse(setup.getAccessA(), r);
	}

	protected void recordResponseB(SessionRunnable r) {
		configuredResponseB = recordResponse(setup.getAccessB(), r);
	}

	private ManipulationResponse recordResponse(IncrementalAccess access, SessionRunnable r) {
		ManipulationDriver driver = new ManipulationDriver(access);
		Manipulation m = driver.dryRun(r);

		ManipulationResponse result = ManipulationResponse.T.create();
		result.setInducedManipulation(m);

		return result;
	}

}
