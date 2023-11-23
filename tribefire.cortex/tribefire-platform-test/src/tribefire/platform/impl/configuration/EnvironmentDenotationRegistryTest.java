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
package tribefire.platform.impl.configuration;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public class EnvironmentDenotationRegistryTest {

	@Test
	public void testRegisterLookup() {
		
		TestDenotationType registeredDenotationType = TestDenotationType.T.create();
		
		EnvironmentDenotationRegistry.getInstance().register("testRegisterLookup", registeredDenotationType);
		
		GenericEntity lookedUpDenotationType = EnvironmentDenotationRegistry.getInstance().lookup("testRegisterLookup");
		
		Assert.assertEquals("Unexpected denotation type fetched", registeredDenotationType, lookedUpDenotationType);
		
	}
	
	@Test
	public void testMultipleRegisterLookup() {
		
		int totalDenotationTypes = 100;
		
		GenericEntity[] registered = new GenericEntity[totalDenotationTypes];
		
		for (int i = 0; i < totalDenotationTypes; i++) {
			registered[i] = TestDenotationType.T.create();
		}
		
		for (int i = 0; i < totalDenotationTypes; i++) {
			EnvironmentDenotationRegistry.getInstance().register("testMultipleRegisterLookup"+i, registered[i]);
		}
		
		for (int i = 0; i < totalDenotationTypes; i++) {
			Assert.assertEquals("Unexpected denotation type fetched", registered[i], EnvironmentDenotationRegistry.getInstance().lookup("testMultipleRegisterLookup"+i));
		}
		
	}
	
	@Test
	public void testOverwriteRegister() {
		
		TestDenotationType registeredType1 = TestDenotationType.T.create();
		TestDenotationType registeredType2 = TestDenotationType.T.create();
		
		EnvironmentDenotationRegistry.getInstance().register("testOverwriteRegister", registeredType1);
		EnvironmentDenotationRegistry.getInstance().register("testOverwriteRegister", registeredType2);

		Assert.assertEquals("Unexpected denotation type fetched", registeredType2, EnvironmentDenotationRegistry.getInstance().lookup("testOverwriteRegister"));
		
	}
	
	@Test
	public void testNotFoundLookup() {

		GenericEntity nullDenotationType = EnvironmentDenotationRegistry.getInstance().lookup("FAKE");
		
		Assert.assertNull("Looking up for [ FAKE ] should have returned null", nullDenotationType);
		
	}
	
	public static interface TestDenotationType extends GenericEntity {
		
		EntityType<EnvironmentDenotationRegistryTest.TestDenotationType> T = EntityTypes
				.T(EnvironmentDenotationRegistryTest.TestDenotationType.class);
	}
	

}
