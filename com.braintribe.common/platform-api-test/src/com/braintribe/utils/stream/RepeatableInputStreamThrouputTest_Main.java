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
package com.braintribe.utils.stream;

import com.braintribe.utils.StringTools;
import com.braintribe.utils.stream.RepeatableInputStreamTest.TestInputStream;

/**
 * @author peter.gazdik
 */
public class RepeatableInputStreamThrouputTest_Main {

	public static void main(String[] args) throws Exception {

		int runs = 2;
		long t = 0;
		long readData = 0;
		int maxSourceThroughput = 100; // Mbit per second

		byte[] originalData = RepeatableInputStreamTest.randomData(1024 * 1024 * 20);

		for (int i = runs; i > 0; i--) {

			TestInputStream originalInput = new TestInputStream(originalData, maxSourceThroughput);

			RepeatableInputStream repeatableInput = new RepeatableInputStream(originalInput);

			try {
				long c = System.currentTimeMillis();
				readData = RepeatableInputStreamTest.readBuffered(repeatableInput);
				repeatableInput.close();
				t += System.currentTimeMillis() - c;
			} finally {
				repeatableInput.destroy();
			}

		}

		System.out.println("Consumption of " + StringTools.prettyPrintBytesDecimal(readData) + " bytes limited to " + maxSourceThroughput
				+ " Mbps took in average " + t / runs + "ms");

	}
}
