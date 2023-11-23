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
package com.braintribe.model.processing.test.itw.speacialcase.invokevirtual;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.ImportantItwTestSuperType;

/**
 * 
 */
public class _InvokeVirtualTests extends ImportantItwTestSuperType {

	@Test
	public void testInvokeVirutalWithMultipleInheritance() {
		Sub sub = enhanced(Sub.class);

		sub.setProp1("val1");
		sub.setProp2("val2");

		Property p1 = typeReflection().getEntityType(Super1.class).getProperty("prop1");
		Property p2 = typeReflection().getEntityType(Super2.class).getProperty("prop2");

		assertThat((String) p1.get(sub)).isEqualTo("val1");
		assertThat((String) p2.get(sub)).isEqualTo("val2");
	}

	@Test
	public void testInvokeVirutalWithMultipleInheritance_AbsenceInformation() {
		NamedAbsenceInformation ai1 = NamedAbsenceInformation.T.create();
		NamedAbsenceInformation ai2 = NamedAbsenceInformation.T.create();

		ai1.setName("ai1");
		ai2.setName("ai2");

		Sub sub = enhanced(Sub.class);

		Property p1 = typeReflection().getEntityType(Super1.class).getProperty("prop1");
		Property p2 = typeReflection().getEntityType(Super2.class).getProperty("prop2");

		p1.setAbsenceInformation(sub, ai1);
		p2.setAbsenceInformation(sub, ai2);
		
		assertThat(p1.getAbsenceInformation(sub)).isSameAs(ai1);
		assertThat(p2.getAbsenceInformation(sub)).isSameAs(ai2);
	}

	
	@ToStringInformation("NamedAbsenceInformation(${name})")
	public static interface NamedAbsenceInformation extends AbsenceInformation {
		EntityType<_InvokeVirtualTests.NamedAbsenceInformation> T = EntityTypes.T(NamedAbsenceInformation.class);
		
		String getName();
		void setName(String value);
	}

	protected <T extends GenericEntity> T enhanced(Class<T> beanClass) {
		EntityType<GenericEntity> entityType = typeReflection().getEntityType(beanClass);
		return beanClass.cast(entityType.create());
	}

	private static GenericModelTypeReflection typeReflection() {
		return GMF.getTypeReflection();
	}
}
