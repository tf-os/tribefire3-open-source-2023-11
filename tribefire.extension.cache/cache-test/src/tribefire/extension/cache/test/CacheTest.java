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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.extension.cache.model.service.demo.CacheDemo;
import tribefire.extension.cache.model.service.demo.CacheDemoResult;
import tribefire.extension.cache.test.wire.CacheTestWireModule;
import tribefire.extension.cache.test.wire.contract.CacheTestContract;

//TODO: maybe delete it?
public class CacheTest extends AbstractCacheTest<CacheTestContract> {

	private static final String DEFAULT_TEST_DOMAIN = "test.domain2";

	@Override
	protected WireTerminalModule<CacheTestContract> module() {
		return CacheTestWireModule.INSTANCE;
	}

	@Before
	public void init() {
		// nothing so far
	}

	@Test
	@Ignore
	public void todoTest() {

		CacheDemo request = CacheDemo.T.create();

		Property property = CacheDemo.T.getProperty(CacheDemo.resultValue);
		property.setAbsenceInformation(request, GMF.absenceInformation());

		CacheDemoResult cacheDemoResult = request.eval(evaluator).get();

		System.out.println(cacheDemoResult);
	}

	@Test
	@Ignore
	public void test() {

	}

	// TODO: request with object
	// TODO: request empty
	// TODO: request with absence information

}
