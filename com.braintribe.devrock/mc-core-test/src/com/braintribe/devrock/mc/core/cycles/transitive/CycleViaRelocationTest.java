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
package com.braintribe.devrock.mc.core.cycles.transitive;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.artifact.analysis.AnalysisArtifactResolution;

/**
 * tests that the correct exception is thrown 
 * @author pit
 *
 */
public class CycleViaRelocationTest extends AbstractTransitiveCycleTest {
	protected static final String TERMINAL = "com.braintribe.devrock.test:terminal#1.0.1";
	
	@Override
	protected File archiveContentDirectory() {
		return new File( input, "cycleViaRelocation");
	}

	@Test
	public void runTest() {	
		boolean exceptionThrown = false;
		try {
			AnalysisArtifactResolution resolution = run( TERMINAL, standardResolutionContext);
			if (resolution.hasFailed()) {
				// TODO: validate RelocationCycle reason as root cause of the failure
				System.out.println(resolution.getFailure().stringify());
			}
			else {
				Assert.fail("Resolution was unexpectedly successful");
			}
		}
		catch (Exception e) {			
			e.printStackTrace();
			exceptionThrown = true;
		}
		Assert.assertTrue("unexpected exception thrown", !exceptionThrown);
	}
	
}
