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
package tribefire.cortex.testrunner.api;

import java.util.Set;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Triggers test execution. Main implementation is in <tt>tribefire.cortex:test-runner-module</tt>. Returns a <tt>.zip</tt> file with collected test
 * results.
 *
 * @author Neidhart.Orlich
 *
 */
public interface RunTests extends AuthorizedRequest {

	final EntityType<RunTests> T = EntityTypes.T(RunTests.class);

	boolean getRunParallel();
	void setRunParallel(boolean runParallel);

	@Description("Prevents test runners to redirect system err/out to the report files. Usable for debugging, to keep output in the console.")
	boolean getKeepOriginalSysouts();
	void setKeepOriginalSysouts(boolean keepOriginalSysouts);

	@Description("If configured, only tests in given test class names are executed. Can be simple or full class name.")
	Set<String> getClassNames();
	void setClassNames(Set<String> classNames);

	@Override
	EvalContext<Resource> eval(Evaluator<ServiceRequest> evaluator);

}