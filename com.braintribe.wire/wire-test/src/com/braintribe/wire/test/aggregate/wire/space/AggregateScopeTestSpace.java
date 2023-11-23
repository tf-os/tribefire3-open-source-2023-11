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
package com.braintribe.wire.test.aggregate.wire.space;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.test.aggregate.payload.TestContext;
import com.braintribe.wire.test.aggregate.payload.TestNode;
import com.braintribe.wire.test.aggregate.wire.LifecycleMonitor;
import com.braintribe.wire.test.aggregate.wire.contract.AggregateScopeTestContract;

@Managed
public class AggregateScopeTestSpace implements AggregateScopeTestContract {
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		configuration.addLifecycleListener(monitor());
	}
	
	@Managed @Override
	public TestNode root(TestContext context) {
		TestNode bean = new TestNode("root");
		bean.setNext(sub("sub"));
		bean.setAltNext(sub("sub"));
		bean.setExtraNext(sub("subextra"));
		return bean;
	}
	
	@Managed(Scope.caller)
	private TestNode sub(String name) {
		TestNode bean = new TestNode(name);
		bean.setNext(sub1());
		return bean;
	}
	
	@Override
	@Managed(Scope.caller)
	public TestNode sub1() {
		TestNode bean = new TestNode("sub1");
		bean.setNext(sub2());
		return bean;
	}
	
	@Managed(Scope.caller)
	private TestNode sub2() {
		TestNode bean = new TestNode("sub2");
		bean.setNext(sub1());
		return bean;
	}
	
	@Override
	@Managed
	public LifecycleMonitor monitor() {
		LifecycleMonitor bean = new LifecycleMonitor();
		return bean;
	}
}
