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
package com.braintribe.gwt.customization.client.tests;

import com.braintribe.gwt.customization.client.tests.model.grindlebone.GbEntity;
import com.braintribe.model.generic.GmfException;

/**
 * @author peter.gazdik
 */
public class GrindleboneTest extends AbstractGwtTest {

	@Override
	protected void tryRun() throws GmfException {
		GbEntity gbEntity = GbEntity.T.create();
		gbEntity.setBooleanValue(true);
		gbEntity.setBooleanWrapper(true);
		gbEntity.setIntegerValue(23);
		gbEntity.setIntegerWrapper(23);
		gbEntity.setLongValue(4711L);
		gbEntity.setLongWrapper(4711L);
		gbEntity.setFloatValue((float)Math.PI);
		gbEntity.setFloatWrapper((float)Math.PI);
		gbEntity.setDoubleValue(Math.E);
		gbEntity.setDoubleWrapper(Math.E);
		gbEntity.setStringValue("laessig");

		Object objectValue = gbEntity.getStringValue();
		String stringValue = gbEntity.getStringValue();
		Class<?> class1 = objectValue.getClass();
		Class<?> class2 = stringValue.getClass();
		log(class1.toString());
		log(class2.toString());
		log(gbEntity.toString());
	}


}
