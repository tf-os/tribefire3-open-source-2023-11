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
package com.braintribe.devrock.zed.forensics.fingerprint.register;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.devrock.zed.forensics.fingerprint.FingerPrintCommons;
import com.braintribe.devrock.zed.forensics.fingerprint.filter.FingerPrintFilter;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.zarathud.model.data.ZedEntity;
import com.braintribe.zarathud.model.forensics.FingerPrint;
import com.braintribe.zarathud.model.forensics.findings.IssueType;

/**
 * contains collected {@link FingerPrint}, can be persisted and gives high-level access to the stored data  
 * @author pit
 *
 */
public class FingerPrintRegistry implements FingerPrintCommons {
	
	private List<FingerPrint> prints = new ArrayList<>();

	public List<FingerPrint> getPrints() {
		return prints;
	}
	public void setPrints(List<FingerPrint> prints) {
		this.prints = prints;
	}	
	
	/**
	 * finds whether a {@link FingerPrint} with the corresponding {@link ForensicsFindingCode} exist in the registry 
	 * @param code - the {@link IssueType} to look for amongst *all* stored prints
	 * @return - true if a (single) {@link FingerPrint} with the passed {@link ForensicsFindingCode} exists in the registry
	 */
	public boolean hasPrintOfIssue( IssueType code) {			
		return hasPrintOfIssue(prints, code);
	}
	
	 
	/**
	 * finds whether at least one {@link FingerPrint} in the registry matches the filter  
	 * @param filter - the filter to apply 
	 * @return - true if at least on FingerPrint matches 
	 */
	public boolean hasPrintOfIssue( Predicate<FingerPrint> filter) {
		return hasPrintOfIssue(prints, filter);
	}		
	
		
	/**
	 * @param filter - {@link Predicate} to filter {@link FingerPrint}, maybe an instance of {@link FingerPrintFilter}
	 * @return - a {@link List} of matching {@link FingerPrint}
	 */
	public List<FingerPrint> filter( Predicate<FingerPrint> filter) {
		return prints.stream().filter(filter).collect( Collectors.toList());
	}
		
	
	public Collection<GenericEntity> findMatchingSource( Collection<ZedEntity> population, FingerPrint fingerPrint) {
		return null;
	}
			
}
