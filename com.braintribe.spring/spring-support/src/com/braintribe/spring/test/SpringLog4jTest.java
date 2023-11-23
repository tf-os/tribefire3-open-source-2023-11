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
package com.braintribe.spring.test;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.UrlResource;

public class SpringLog4jTest {
	public static void main(String[] args) {
		try {
			System.setProperty("org.apache.commons.logging.log", "org.apache.commons.logging.impl.Log4JLogger");
			XmlBeanFactory beanFactory = new XmlBeanFactory(
					new UrlResource(
							SpringLog4jTest.class.getResource("spring.xml")));
			
			Object object1 = beanFactory.getBean("test");
			System.out.println(object1);
			Object object2 = beanFactory.getBean("test2");
			System.out.println(object2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
