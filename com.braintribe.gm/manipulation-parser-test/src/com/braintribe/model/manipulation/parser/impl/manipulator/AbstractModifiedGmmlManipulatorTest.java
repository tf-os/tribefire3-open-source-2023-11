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
package com.braintribe.model.manipulation.parser.impl.manipulator;

import java.util.function.Function;

import com.braintribe.model.processing.manipulation.parser.api.GmmlManipulatorErrorHandler;
import com.braintribe.model.processing.manipulation.parser.impl.listener.error.LenientErrorHandler;

/**
 * To test the lenient mode, we record the manipulations, stringify them, modify the string with {@link #gmmlModifier}, and then apply the modified
 * string.
 * 
 * Ever test has the following structure:
 * <ul>
 * <li>Set the {@link #gmmlModifier} to cause some problem with the manipulations</li>
 * <li>Run the process (see {@link #recordStringifyAndApply}), which also applies the modifier set in previous step.</li>
 * <li>Assert that the result is correct.</li>
 * </ul>
 * 
 * @author peter.gazdik
 */
public abstract class AbstractModifiedGmmlManipulatorTest extends AbstractManipulatorTest {

	protected Function<String, String> gmmlModifier;

	@Override
	protected String processManipulationString(String string) {
		String s = gmmlModifier.apply(string);
		return s;
	}

	@Override
	protected GmmlManipulatorErrorHandler errorHandler() {
		return LenientErrorHandler.INSTANCE;
	}

}
