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
package com.braintribe.devrock.zarathud.test;

import org.junit.Test;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public class FingerPrintLab extends AbstractFingerprintLab {


	private void testConstruction( String v1) {
		FingerPrint fingerPrint = FingerPrintExpert.build( v1);
		String v2 = FingerPrintExpert.toString(fingerPrint); 
		testOnEquivalency(v1, v2);
	}

	@Test
	public void testConstruction() {	
		String v1 = "group:com.braintribe.gm/artifact:gm-core-api/type:com.braintribe.gm.GenericEntity/issue:MissingDependencies";
		testConstruction( v1);
		
		String v2 = "group:com.braintribe.gm/artifact:gm-core-api/type:com.braintribe.gm.GenericEntity/property:value/issue:MissingDependencies";
		testConstruction( v2);
		
		String v3 = "group:com.braintribe.gm/artifact:*/type:com.braintribe.gm.GenericEntity/property:value/issue:MissingDependencies";
		testConstruction( v3);
		
		String v4 = "group:com.braintribe.gm/artifact:*/type:com.braintribe.gm.GenericEntity/property:value/issue:MissingDependencies(com.braintribe.gm:gm-core-api)";
		testConstruction( v4);
		
	}

	

	@Test
	public void testSimpleMatching() {
		String v1 = "group:com.braintribe.gm/artifact:gm-core-api/type:com.braintribe.gm.GenericEntity/issue:MissingDependencies";				
		testMatching(v1, v1, true);
			
		String v2 = "group:com.braintribe.gm/artifact:gm-core-api/type:com.braintribe.gm.GenericEntity/issue:ExcessDependencies";
		testMatching( v1, v2, false);
		
		String v3 = "group:com.braintribe.gm";
		testMatching( v1, v3, true);
		
		String v4 = "property:myProperty";
		testMatching( v1, v4, false);
		
		String v5 = "property:*";
		testMatching( v1, v5, true);				
	}
	
	@Test
	public void testComplexMatching() {
		String v1 = "group:com.braintribe.gm/artifact:gm-core-api/type:com.braintribe.gm.GenericEntity/issue:MissingDependencies(com.braintribe.gm:gm-core-api,com.braintribe.common:platform-api)";
		String v2 = "issue:MissingDependencies(com.braintribe.gm:gm-core-api,com.braintribe.common:platform-api)";
		testMatching( v1, v2, true);
		
		String v3 = "issue:MissingDependencies(com.braintribe.gm:gm-core-api)";
		testMatching( v1, v3, true);
		
		String v4 = "issue:MissingDependencies(com.braintribe.devrock:malaclypse)";
		testMatching( v1, v4, false);
				
	}
}
