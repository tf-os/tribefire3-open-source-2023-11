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
package com.braintribe.devrock.zed.api.comparison;

import com.braintribe.zarathud.model.data.Artifact;
import com.braintribe.zarathud.model.forensics.FingerPrint;

/**
 * compares two artifacts
 * 
 * @author pit
 */
public interface ZedComparison {

	/**
	 * compares two {@link Artifact} and creates {@link FingerPrint} for each comparison issues 
	 * @param base - the base {@link Artifact}
	 * @param toCompare - the {@link Artifact} to compare to
	 * @return - true if no differences were found, false if so. 
	 */
	boolean compare( Artifact base, Artifact toCompare);
	
	/**
	 * @return - the {@link ComparisonContext} created and filled-up during process 
	 */
	ComparisonContext getComparisonContext();
}
