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
package com.braintribe.model.access.security.manipulation.experts;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.SetTools.asSet;

import java.util.Set;

import org.junit.Before;

import com.braintribe.model.access.security.manipulation.ValidatorTestBase;
import com.braintribe.model.access.security.testdata.acl.AclFactory;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;

public abstract class AbstractAclIlsValidatorTest extends ValidatorTestBase {

	protected AclFactory acls;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		acls = new AclFactory(session);
		acls.setAssignedAclOps(asList(AclOperation.WRITE));
	}

	@Override
	protected final Set<? extends ManipulationSecurityExpert> manipulationSecurityExperts() {
		return asSet(new AclManipulationSecurityExpert());
	}

}
