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
package tribefire.extension.demo.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.extension.demo.test.wire.DemoTestWireModule;
import tribefire.extension.demo.test.wire.contract.DemoTestContract;


public abstract class DemoTestBase {
	
	protected static WireContext<DemoTestContract> context;
	protected static Evaluator<ServiceRequest> evaluator;
	protected static DemoTestContract testContract;
	protected static PersistenceGmSession testAccessSession;
	
	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(DemoTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
		testAccessSession = testContract.sessionFactory().newSession("test.access");
	}
	
	@AfterClass
	public static void afterClass() {
		context.shutdown();
	}
	
}
