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
package tribefire.extension.okta.processing.base;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import tribefire.extension.okta.processing.base.wire.OktaProcessingTestWireModule;
import tribefire.extension.okta.processing.base.wire.contract.OktaProcessingTestContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

public abstract class OktaProcessingTestBase {

	protected static WireContext<OktaProcessingTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static OktaProcessingTestContract testContract;

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(OktaProcessingTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
	}

	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}

}
