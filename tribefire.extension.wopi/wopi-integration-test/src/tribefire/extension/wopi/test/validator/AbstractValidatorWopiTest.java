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
package tribefire.extension.wopi.test.validator;

import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.braintribe.utils.system.exec.CommandExecutionImpl;
import com.braintribe.utils.system.exec.ProcessTerminatorImpl;

import tribefire.extension.wopi.test.AbstractWopiTest;

/**
 * 
 *
 */
//@formatter:off
// get groups:
// -----------
// xmllint --xpath '/WopiValidation/TestGroup/@Name' TestCases.xml | tr ' ' '\n' | sed 's/Name="//g' | sed 's/"$//g'
// get tests per group:
// --------------------
// xmllint --xpath '/WopiValidation/TestGroup[@Name="FileVersion"]' TestCases.xml
// xmllint --xpath '/WopiValidation/TestGroup/TestCases/TestCase/@Name' TestCases.xml | tr ' ' '\n' |sed 's/Name="//g' | sed 's/"$//g' > /tmp/out.txt
//@formatter:on
@RunWith(Parameterized.class)
public abstract class AbstractValidatorWopiTest extends AbstractWopiTest {

	protected static CommandExecutionImpl commandExection;

	static {
		ProcessTerminatorImpl processTerminator = new ProcessTerminatorImpl();
		commandExection = new CommandExecutionImpl();
		commandExection.setProcessTerminator(processTerminator);

	}

	protected String testName;
	protected List<String> commands;

	public AbstractValidatorWopiTest(@SuppressWarnings("unused") String testGroup, String testName) {
		this.testName = testName;
	}

}
