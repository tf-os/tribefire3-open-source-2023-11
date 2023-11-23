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
package com.braintribe.wire.test.basic.space;

import com.braintribe.wire.api.WireException;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.example.BeanOriginManager;
import com.braintribe.wire.example.ExampleBean;
import com.braintribe.wire.example.Resource;
import com.braintribe.wire.test.basic.contract.MainContract;

@Managed
public class MainSpace implements MainContract {
	
	private String memberVariable = "initialized";
	
	@Import
	protected MetaSpace metaSpace;
	
	@Import
	public Space1 space1;
	
	@Import
	public Example2Space space2;
	
	public MainSpace() {
		System.out.println("Hello World");
	}
	
	@Override
	public String memberVariable() {
		return memberVariable;
	}
	
	@Override
	public BeanOriginManager beanOriginManager() {
		return metaSpace.beanOriginManager();
	}
	
	@Managed
	public ExampleBean bean1() {
		ExampleBean bean = new ExampleBean();
		bean.setBean(bean2());
		return bean;
	}
	
	@Managed
	public ExampleBean bean2() {
		ExampleBean bean = new ExampleBean();
		bean.setBean(bean1());
		return bean;
	}
	
	
	@Override
	public ExampleBean example() {
		return space1.exampleBean();
	}
	
	@Override
	public ExampleBean anotherExample() {
		return space2.anotherExampleBean();
	}
	
	@Override
	public Resource resource() {
		return space1.resource();
	}
	
	
	@Managed
	public String string() {
		String bean;
		
		try {
			bean = "Hallo";
			Integer.parseInt("1");
		}
		catch (Exception e) {
			throw new WireException("Hello World");
		}
		return bean;
	}
	
	@Managed(Scope.aggregate)
	public ExampleBean a1() {
		ExampleBean bean = new ExampleBean();
		bean.setBean(a2());
		return bean;
	}
	
	@Managed()
	public ExampleBean a1Holder() {
		ExampleBean bean = new ExampleBean();
		bean.setBean(a1());
		return bean;
	}
	
	@Managed(Scope.aggregate)
	public ExampleBean a2() {
		ExampleBean bean = new ExampleBean();
		bean.setBean(a1());
		return bean;
	}
	
	@Managed
	@Override
	public String qualification() {
		InstanceConfiguration instanceConfiguration = InstanceConfiguration.currentInstance();
		String bean = instanceConfiguration.qualification().space().getClass().getName() + "/" + instanceConfiguration.qualification().name();
		return bean;
	}
	
	@Override
	@Managed
	public String paramTest(Double param) {
		return "test-" + param;
	}
}
