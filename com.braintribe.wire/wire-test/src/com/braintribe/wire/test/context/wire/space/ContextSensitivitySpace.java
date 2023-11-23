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
package com.braintribe.wire.test.context.wire.space;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;

import java.util.Set;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.test.context.wirable.Person;
import com.braintribe.wire.test.context.wirable.TestContext;
import com.braintribe.wire.test.context.wirable.TestLifecycleListener;
import com.braintribe.wire.test.context.wire.contract.ContextSensitivityContract;

@Managed
public class ContextSensitivitySpace implements ContextSensitivityContract {
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		configuration.addLifecycleListener(lifecycleListener());
	}
	
	@Override
	@Managed
	public TestLifecycleListener lifecycleListener() {
		TestLifecycleListener bean = new TestLifecycleListener();
		return bean;
	}
	
	@Override
	@Managed
	public Person person(final TestContext context) {
		Person bean = new Person();
		bean.setName(context.name());
		bean.setImportant(context.important());
		currentInstance().onDestroy(() -> System.out.println("person " + bean.getName() + " (id=" + System.identityHashCode(bean) + ") destroyed"));
		return bean;
	}
	
	@Override
	public Set<Object> destroyedBeans() {
		return lifecycleListener().getDestroyedBeans();
	}
	
	@Managed
	public Person p1() {
		Person bean = new Person();
		bean.setName("p1");
		bean.setPartner(p2());
		return bean;
	}
	
	@Managed
	public Person p2() {
		Person bean = new Person();
		bean.setName("p2");
		bean.setPartner(p1());
		return bean;
	}
}
