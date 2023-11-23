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
package com.braintribe.web.multipart.test;

import com.braintribe.utils.IOTools;
import com.braintribe.utils.TimeTracker;

public class ArrayLab {
	public static void main(String[] args) {
		byte[] data = new byte[IOTools.SIZE_16K];
		byte[] data2 = new byte[IOTools.SIZE_16K];

		TimeTracker.start("unchecked array copy");

		for (int j = 0; j < 100000; j++) {
			if (data[0] == '\n') {
				System.out.println("schnucki");
			}

			System.arraycopy(data, 0, data2, 0, data.length);
		}

		TimeTracker.stopAndPrint("unchecked array copy");

		TimeTracker.start("checked");

		for (int j = 0; j < 100000; j++) {

			for (int i = 0; i < data.length; i++) {
				byte b = data[i];
				if (b == '\n') {
					System.out.println("found");
				} else {
					data2[i] = b;
				}
			}
			System.arraycopy(data, 0, data2, 0, data.length);
		}

		TimeTracker.stopAndPrint("checked");

		TimeTracker.start("unchecked");

		for (int j = 0; j < 100000; j++) {

			for (int i = 0; i < data.length; i++) {
				byte b = data[i];
				data2[i] = b;
			}
			// System.arraycopy(data, 0, data2, 0, data.length);
		}

		TimeTracker.stopAndPrint("unchecked");

	}
}
