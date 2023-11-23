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
package com.braintribe.test.multi.repositoryRoleLab;

import java.io.File;

import org.junit.BeforeClass;

public class SnapshotRoleTestLab extends AbstractRepositoryRoleLab {
	protected static File settings = new File( "res/repositoryRoleLab/contents/settings.snapshot.xml");
	
	@BeforeClass
	public static void before() {
		before( settings);
	}
	@Override
	protected String[] getResultsForRun() {	
		String [] result = new String[2];
		result [0] = "com.braintribe.test.dependencies.repositoryRoleTest:A#1.1-SNAPSHOT";
		result [1] = "com.braintribe.test.dependencies.repositoryRoleTest:B#1.1-SNAPSHOT";
		return result;
	}

}