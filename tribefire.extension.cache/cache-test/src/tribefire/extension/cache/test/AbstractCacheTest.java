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
package tribefire.extension.cache.test;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.extension.cache.test.wire.contract.AbstractCacheTestContract;

public abstract class AbstractCacheTest<S extends AbstractCacheTestContract> {

	public static final String URLENCODED = "application/x-www-form-urlencoded";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String APPLICATION_JSON = "application/json";

	protected abstract WireTerminalModule<S> module();

	protected WireContext<S> wireContext;
	protected S contract;
	protected Evaluator<ServiceRequest> evaluator;

	@Before
	public void initTest() {
		wireContext = Wire.context(module());
		contract = wireContext.contract();
		evaluator = contract.evaluator();
	}

	@After
	public void closeTest() {
		wireContext.shutdown();
	}

}
