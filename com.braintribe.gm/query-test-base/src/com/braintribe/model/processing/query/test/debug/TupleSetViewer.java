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
package com.braintribe.model.processing.query.test.debug;

import com.braintribe.model.processing.query.tools.QueryPlanPrinter;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * textual visualization tool for tuple sets
 * 
 * @author pit
 * 
 */
public class TupleSetViewer {

	private static final int TEST_TITLE_WIDTH = 100;

	public static void view(String testName, TupleSet set) {
		printTestName(testName);
		System.out.println(QueryPlanPrinter.print(set));
		printTestBottomBorder();
	}

	protected static void printTestName(String testName) {
		int len = Math.max(TEST_TITLE_WIDTH - 2 - testName.length(), 20);
		String s = chain((len + 1) / 2, "*") + " " + testName + " " + chain(len / 2, "*");
		System.out.println(s);
	}

	protected static void printTestBottomBorder() {
		System.out.println(chain(TEST_TITLE_WIDTH, "*") + "\n");
	}

	protected static String chain(int count, String s) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < count; i++)
			result.append(s);

		return result.toString();
	}
}
