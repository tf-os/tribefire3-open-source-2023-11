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
package com.braintribe.model.processing.accessory.impl;

import org.junit.After;

import com.braintribe.model.processing.accessory.test.cortex.MaTestConstants;
import com.braintribe.model.processing.accessory.test.wire.contract.MaTestContract;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.NotifiableModelAccessoryFactory;

/**
 * 
 */
public class AbstractPlatformModelAccessoryFactoryTest implements MaTestConstants {

	protected final MaTestContract contract = MaTestContract.theContract();

	protected NotifiableModelAccessoryFactory maf;
	protected ModelAccessory cortexMa;
	protected ModelAccessory cortexSdMa;
	protected ModelAccessory customAccessMa;
	protected ModelAccessory customAccessSdMa;
	protected ModelAccessory customSdMa;
	protected ModelAccessory emptySdMa;
	protected ModelAccessory customModelMa;

	@After
	public void cleanup() {
		contract.cortexCsaDu().cleanup();
	}

}
