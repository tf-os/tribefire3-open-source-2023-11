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
package com.braintribe.wire.test.basic;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Button;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.example.AnnoBean;
import com.braintribe.wire.example.ExampleBean;
import com.braintribe.wire.example.Resource;
import com.braintribe.wire.test.basic.contract.IntermediateConstructionContract;
import com.braintribe.wire.test.basic.contract.MainContract;
import com.braintribe.wire.test.basic.space.MainSpace;


public class BasicTest {
	/**
	 * tests the following features
	 * <ul>
	 * 	<li>bean space contract resolution</li>
	 * 	<li>cyclic bean space import</li>
	 * 	<li>cyclic singleton scoped bean reference</li>
	 * </ul>
	 */
	
	public boolean isX(String name) {
		return true;
	}
	
	public boolean isY(String name) {
		return true;
	}
	
	@Test
	public void basicFunctionality() throws RuntimeException {

		WireContext<MainContract> context = Wire
				.context(MainContract.class)
				.bindContracts("com.braintribe.wire.test.basic")
				.build();
		
		
		ExampleBean exampleBean = context.contract().bean1();
		Resource resourceDirect = context.contract().resource();
		ExampleBean exampleBeanWithResource = context.contract().example();
		
		Resource resource = exampleBeanWithResource.getResourceProvider().get();
		
		ExampleBean exampleBean1 = context.contract().example();
		
		ExampleBean exampleBean2 = exampleBean1.getBean();
		ExampleBean exampleBean1Candidate = exampleBean2.getBean();
		
		AnnoBean annoBean1 = exampleBeanWithResource.getAnnoBean();
		AnnoBean annoBean2 = exampleBeanWithResource.getDeferredBean();
		
		InstanceHolder beanHolder = context.contract().beanOriginManager().resolveBeanHolder(exampleBean);
		System.out.println(beanHolder.space().getClass());
		System.out.println(beanHolder.name());
		
		ExampleBean anotherExampleBean = context.contract().anotherExample();
		
		ExampleBean a1 = context.contract().a1();
		assertThat(a1).isSameAs(a1.getBean().getBean());
		
		// check cyclic reference
		assertThat(exampleBean1).isSameAs(exampleBean1Candidate);

		// check BeanSpace implementation inheritance
		assertThat(exampleBean1.getSomething()).isNotNull();
		assertThat(exampleBean2.getSomething()).isNotNull();
		assertThat(exampleBean2.getSomething()).isNotSameAs(exampleBean1.getSomething());
		assertThat(context.contract().qualification()).as("InstanceConfiguration qualification did not work").isEqualTo(MainSpace.class.getName() + "/qualification");
		
		String s1 = context.contract().paramTest(2D);
		String s2 = context.contract().paramTest(2D);
		String s3 = context.contract().paramTest(3D);
		
		assertThat(s1).isEqualTo("test-2.0");
		assertThat(s2).isEqualTo("test-2.0");
		assertThat(s1).isSameAs(s2);
		assertThat(s3).isEqualTo("test-3.0");
		
		// assertThat(context.contract().memberVariable()).isEqualTo("initialized");
		context.shutdown();
	}
	
	@Test
	public void aggregateScope() throws RuntimeException {
		
		List<Object> destroyedInstances = new ArrayList<>();
		
		LifecycleListener lifecycleListener = new LifecycleListener() {
			
			@Override
			public void onPreDestroy(InstanceHolder instanceHolder, Object instance) {
				destroyedInstances.add(instance);
			}
			
			@Override
			public void onPostConstruct(InstanceHolder instanceHolder, Object instance) {
				// TODO Auto-generated method stub
				
			}
		};
		
		try (WireContext<MainContract> context = Wire
				.context(MainContract.class)
				.bindContracts("com.braintribe.wire.test.basic")
				.lifecycleListener(lifecycleListener)
				.build()) {
			
			ExampleBean a1Holder = context.contract().a1Holder();
			ExampleBean a1 = a1Holder.getBean();
			assertThat(a1).isSameAs(a1.getBean().getBean());
			
		}
		
		System.out.println(destroyedInstances);
		
	}
	
	@Test
	public void intermediateConstructionTest() throws Exception {
		
		WireContext<IntermediateConstructionContract> context = Wire
				.context(IntermediateConstructionContract.class)
				.bindContracts("com.braintribe.wire.test.basic")
				.build();

		IntermediateConstructionContract contract = context.contract();
		ExampleBean button = contract.button();
		System.out.println("Done: "+button);
		
	}

}
