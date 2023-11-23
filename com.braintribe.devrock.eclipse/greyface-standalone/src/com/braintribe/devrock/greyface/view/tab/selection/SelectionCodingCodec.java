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
package com.braintribe.devrock.greyface.view.tab.selection;

import com.braintribe.build.artifact.name.NameParser;
import com.braintribe.cc.lcd.HashSupportWrapperCodec;
import com.braintribe.model.artifact.Solution;

/**
 * a codec for supporting the selection of solutions in the selection process,
 * making sure that only one instance of a solution is stored
 * @author pit
 *
 */
public class SelectionCodingCodec extends HashSupportWrapperCodec<Solution> {
	@Override
	protected boolean entityEquals(Solution arg0, Solution arg1) {
		String name1 = NameParser.buildName(arg0);
		String name2 = NameParser.buildName(arg1);
		return name1.equalsIgnoreCase(name2);
	}

	@Override
	protected int entityHashCode(Solution arg0) {
		return NameParser.buildName(arg0).hashCode();			
	}
}
