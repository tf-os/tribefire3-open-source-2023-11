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
package com.braintribe.devrock.zed.forensics.fingerprint.filter;

import java.util.function.Predicate;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintExpert;
import com.braintribe.zarathud.model.forensics.FingerPrint;

/**
 * filter to use the {@link FingerPrintExpert}'s matching feature within streams of *LOCK*.
 * Passed {@link FingerPrint} in constructor is used as *KEY*, i.e. must be either as qualified 
 * or less qualified than the *LOCK* in order to return *LOCK* 
 * 
 * @author pit
 *
 */
public class FingerPrintFilter implements Predicate<FingerPrint> {
	private FingerPrint key;	

	/**
	 * @param key - a {@link FingerPrint} instance to use as KEY
	 */
	public FingerPrintFilter( FingerPrint key) {
		this.key = key;
	}
	
	public FingerPrintFilter( FingerPrint key, String ... slotsToIgnore) {
		this.key = key;
	}
	
	
	/**
	 * @param expression - a {@link FingerPrint} in string notation to use as KEY
	 */
	public FingerPrintFilter( String expression) {
		this.key = FingerPrintExpert.build(expression);
	}
	
	@Override
	public boolean test(FingerPrint lock) {
		return FingerPrintExpert.matches(lock, key);
	}

	
}
