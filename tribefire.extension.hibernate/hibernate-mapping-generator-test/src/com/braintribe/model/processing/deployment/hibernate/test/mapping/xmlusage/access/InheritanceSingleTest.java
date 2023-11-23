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
package com.braintribe.model.processing.deployment.hibernate.test.mapping.xmlusage.access;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class InheritanceSingleTest extends HibernateAccessBasedTest {
	
	@Override
	public Class<?>[] getTestEntityClasses() {
		return new Class<?>[] {
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Bank.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.CreditCard.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.DebitCard.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.PrePaidDebitCard.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.CardCompany.class,
			com.braintribe.model.processing.deployment.hibernate.testmodel.inheritance.single.Employee.class
		};
	}
	
	public static void main(String[] args) throws Exception {
		InheritanceSingleTest test = new InheritanceSingleTest();
		test.testPersistAndQuery();
	}

	@BeforeClass
	public static void start() throws Exception {
		initialize();
	}

	@AfterClass
	public static void stop() throws Exception {
		destroy();
	}

	@Override
	@Test
	public void testPersistAndQuery() throws Exception {
		super.testPersistAndQuery();
	}
	
}
