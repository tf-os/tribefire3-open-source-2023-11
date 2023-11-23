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
package com.braintribe.devrock.zarathud.test.extraction.resolving;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.devrock.zarathud.model.ResolvingRunnerContext;
import com.braintribe.devrock.zarathud.model.context.ConsoleOutputVerbosity;
import com.braintribe.devrock.zarathud.runner.api.ZedWireRunner;
import com.braintribe.devrock.zarathud.runner.wire.ZedRunnerWireTerminalModule;
import com.braintribe.devrock.zarathud.runner.wire.contract.ZedRunnerContract;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(KnownIssue.class)
public class WiredExtraction {

	public void test(String terminal) {
		WireContext<ZedRunnerContract> wireContext = Wire.context( ZedRunnerWireTerminalModule.INSTANCE);
		
		ResolvingRunnerContext rrc = ResolvingRunnerContext.T.create();
		rrc.setTerminal( terminal);
		rrc.setConsoleOutputVerbosity( ConsoleOutputVerbosity.verbose);
		
		ZedWireRunner zedWireRunner = wireContext.contract().resolvingRunner( rrc);
		
		zedWireRunner.run();
	}
	
	@Test
	public void test() {
		test( "com.braintribe.devrock.test.zarathud:z-model-one#1.0.1-pc");
	}
	@Test
	public void testX() {
		test( "tribefire.cortex:tribefire-module-api#2.0.30");
	}
	
	@Test
	public void test_pull_parser() {
		test( "pull-parser:pull-parser#2");
	}
	
	@Test
	public void test_ravenhurst() {
		test( "com.braintribe.devrock:ravenhurst#1.0.50");
	}

	@Test
	public void test_acl() {
		test( "com.braintribe.gm:acl-support#1.0.6-pc");
	}
	
	@Test
	public void test_basic_access_adapters() {
		test( "com.braintribe.gm:basic-access-adapters#1.0.22-pc");
	}

	
}
