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
package tribefire.extension.appconfiguration.app_configuration.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.appconfiguration.app_configuration.test.wire.AppConfigurationTestWireModule;
import tribefire.extension.appconfiguration.app_configuration.test.wire.contract.AppConfigurationTestContract;

public abstract class AppConfigurationTestBase {

	protected static WireContext<AppConfigurationTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static AppConfigurationTestContract testContract;

	protected AppConfigurationTestBase() {
		// nothing to do
	}

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(AppConfigurationTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
	}

	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}

}
