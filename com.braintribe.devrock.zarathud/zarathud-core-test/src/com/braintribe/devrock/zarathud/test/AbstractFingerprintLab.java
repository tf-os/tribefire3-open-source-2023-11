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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.forensics.FingerPrint;

public abstract class AbstractFingerprintLab {
	/**
	 * @param v1 - first string representation
	 * @param v2 - second string representation 
	 * @return - true if they match
	 */
	protected boolean testOnEquivalency( String v1, String v2) {
		List<String> tokens1 = Arrays.asList(v1.split( "/"));
		List<String> tokens2 = Arrays.asList(v2.split( "/"));
		
		for (String token : tokens1) {
			// value exists like that -> ok
			if (tokens2.contains( token))
				continue;
			// value doesn't exist, but key is a wild card -> ok
			if (token.endsWith( ":*"))
				continue;
			// neither -> fail
			Assert.fail("expression [" + v2 + "] is not compatible with expected [" + v1 + "]");
			return false;
		}
		return true;
	}
	
	protected void testMatching( String v1, String v2, boolean matchExpected) {
		FingerPrint lock = FingerPrintExpert.build(v1);
		FingerPrint key = FingerPrintExpert.build( v2);			
		testMatching( lock, key, matchExpected);
	}
	
	protected void testMatching( FingerPrint lock, FingerPrint key, boolean matchExpected) {
		boolean matches = FingerPrintExpert.matches(lock, key);
		if (matchExpected) {
			Assert.assertTrue( "key [" + key.toString() + "] doesn't match lock [" + lock.toString() + "]", matches);				
		}
		else {
			Assert.assertTrue( "key [" + key.toString() + "] matches lock [" + lock.toString() + "]", !matches);
		}		
	}

}
