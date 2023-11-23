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
package com.braintribe.artifacts.codebase.read;

import java.io.File;

import org.junit.BeforeClass;

public class GrpExpAgnosticReaderLab extends AbstractLocationAgnosticReaderLab {
	
	@BeforeClass
	public static void runbefore() {
		target = new File( contents, "grouping.expanded");
		targetGrpOneA = new File( target, "com/braintribe/grpOne/1.0/A/pom.xml");
		targetGrpOneB = new File( target, "com/braintribe/grpOne/1.0/B/pom.xml");
		targetGrpOneSubOneA = new File( target, "com/braintribe/grpOne/subOne/1.0/A/pom.xml");
		
	
		setupCodebase( EXPANDED_GROUP);
		AbstractLocationAgnosticReaderLab.runbefore();
	}

}
