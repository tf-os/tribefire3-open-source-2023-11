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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.IssueType;

/**
 * filter to {@link FingerPrint}s with one or more specific issue within streams. 
 * 
 * @author pit
 *
 */
public class FingerPrintRatingFilter implements Predicate<FingerPrint>{
	
	private List<String> codes = new ArrayList<>();
	
	/**
	 * @param codes - an array of {@link IssueType}s as {@link String}
	 */
	public FingerPrintRatingFilter(String ... codes) {
		if (codes != null) {
			this.codes.addAll( Arrays.asList( codes));
		}
	}
	
	
	/**
	 * @param codes - an array of {@link IssueType}s
	 */
	public FingerPrintRatingFilter(IssueType ... codes) {
		if (codes != null) {
			for (IssueType code : codes) {
				this.codes.add( code.name());			 
			}
		}
	}
	
	/**
	 * @param codes - an {@link List} of {@link IssueType}
	 */
	public FingerPrintRatingFilter(List<IssueType> codes) {		
		if (codes != null) {
			for (IssueType code : codes) {
				this.codes.add( code.name());			 
			}
		}
	}
	

	@Override
	public boolean test(FingerPrint t) {
		String code = t.getSlots().get("issue");		
		return codes.contains(code);
	}

	
}
