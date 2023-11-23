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
package com.braintribe.devrock.zed.forensics.fingerprint;

import java.util.Collection;
import java.util.function.Predicate;

import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.IssueType;

/**
 * 
 * @author pit
 */
public interface FingerPrintCommons {

	/**
	 * finds whether a {@link FingerPrint} with the corresponding {@link ForensicsFindingCode} exist in the passed collection
	 * @param fingerprints - the {@link FingerPrint}s to traverse 
	 * @param code - the {@link IssueType} to look for 
	 * @return - true if a (single) {@link FingerPrint} with the passed {@link ForensicsFindingCode} exists in the fingerprints passed
	 */
	default boolean hasPrintOfIssue( Collection<FingerPrint> fingerprints, IssueType code) {
		Predicate<FingerPrint> filter = new FingerPrintFilter( "issue:" + code.name());
		return fingerprints.stream().filter(filter).findAny().isPresent();
	}
	
	/**
	 * finds whether at least one {@link FingerPrint} in the collection matches the filter
	 * @param fingerprints - the {@link FingerPrint}s to traverse
	 * @param filter - the filter to apply 
	 * @return - true if at least on FingerPrint matches
	 */
	default boolean hasPrintOfIssue( Collection<FingerPrint> fingerprints, Predicate<FingerPrint> filter) {			
		return fingerprints.stream().filter(filter).findAny().isPresent();
	}
	
	
}
