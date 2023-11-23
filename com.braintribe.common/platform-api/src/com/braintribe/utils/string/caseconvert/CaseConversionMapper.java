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
package com.braintribe.utils.string.caseconvert;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author peter.gazdik
 */
public interface CaseConversionMapper extends CaseConversionJoiner {

	/**
	 * A helper method to apply mapping conditionally without breaking the fluent API.
	 * <p>
	 * If <tt>true</tt> is passed, this method returns this mapper, so the overall effect is if is this "when" method wasn't even called.
	 * <p>
	 * If <tt>false</tt> is passed , this method returns a special mapper which ignores the next method, so the overall effect is as
	 * if this "when" method and the subsequent mapping method weren't even called.
	 */
	
	CaseConversionMapper when(boolean condition);

	CaseConversionJoiner uncapitalizeAll();

	CaseConversionJoiner capitalizeAll();

	CaseConversionJoiner uncapitalizeFirst();

	CaseConversionJoiner capitalizeAllButFirst();

	CaseConversionJoiner toLowerCase();
	
	CaseConversionJoiner toUpperCase();

	CaseConversionJoiner map(Function<? super String, String> mapping);

	Stream<String> asStream();	

}
