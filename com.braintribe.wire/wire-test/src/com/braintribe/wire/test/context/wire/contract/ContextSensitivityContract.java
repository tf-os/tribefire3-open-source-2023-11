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
package com.braintribe.wire.test.context.wire.contract;

import java.util.Set;

import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.test.context.wirable.Person;
import com.braintribe.wire.test.context.wirable.TestContext;
import com.braintribe.wire.test.context.wirable.TestLifecycleListener;

public interface ContextSensitivityContract extends WireSpace {
	Person person(TestContext context);
	Set<Object> destroyedBeans();
	TestLifecycleListener lifecycleListener();
	Person p1();
}
