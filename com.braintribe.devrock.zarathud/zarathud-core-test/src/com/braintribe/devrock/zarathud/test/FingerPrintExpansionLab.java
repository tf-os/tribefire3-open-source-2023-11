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

public class FingerPrintExpansionLab extends AbstractFingerprintLab {
	
	@Test
	public void test() {
		String v1 = "group:com.braintribe.gm/artifact:gm-core-api/package:com.braintribe.gm/type:GenericEntity";
		String v2 = "property:myProperty";
		String v3 = v1 + "/" + v2;
		
		FingerPrint fp1 = FingerPrintExpert.build(v1);
		FingerPrint fp3 = FingerPrintExpert.build( v3);
		
		fp1 = FingerPrintExpert.attach(fp1, v2);
		
		testMatching(fp1, fp3, true);		
	}
	
}
