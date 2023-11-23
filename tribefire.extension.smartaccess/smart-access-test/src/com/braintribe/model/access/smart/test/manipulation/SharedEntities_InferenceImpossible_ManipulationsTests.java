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
package com.braintribe.model.access.smart.test.manipulation;

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;
import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdB;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.shared.SharedFile;
import com.braintribe.model.processing.query.smart.test.model.shared.SharedFileDescriptor;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.utils.junit.core.rules.ThrowableChainRule;

/**
 * These tests check if the correct delegate is chosen.
 * 
 */
public class SharedEntities_InferenceImpossible_ManipulationsTests extends AbstractManipulationsTests {

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule(SmartAccessException.class);

	@Test
	public void explicitIncompatibleAssignment() throws Exception {
		SharedFile f1 = newSharedFile();
		f1.setPartition(accessIdA);

		SharedFile f2 = newSharedFile();
		f2.setPartition(accessIdB);

		f1.setParent(f2);

		commit();
	}

	@Test
	public void incompatibleDefaults() throws Exception {
		SharedFile f = newSharedFile();
		SharedFileDescriptor d = newSharedFileDescriptor();

		f.setFileDescriptor(d);
		commit();
	}

	// #####################################
	// ## . . . . . . HELPERS . . . . . . ##
	// #####################################

	protected SharedFile newSharedFile() {
		return newEntity(SharedFile.T);
	}

	protected SharedFileDescriptor newSharedFileDescriptor() {
		return newEntity(SharedFileDescriptor.T);
	}

}
