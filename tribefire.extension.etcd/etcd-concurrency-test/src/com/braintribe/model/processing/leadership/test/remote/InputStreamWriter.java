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
package com.braintribe.model.processing.leadership.test.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamWriter extends Thread {

	/**
	 * Stream being read
	 */
	protected final InputStream stream;

	public InputStreamWriter(InputStream stream) {
		this.stream = stream;
	}

	/**
	 * Stream the data.
	 */
	@Override
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			try {
				char[] buffer = new char[4096];
				int n = 0;
				while (-1 != (n = br.read(buffer))) {
					System.out.print(new String(buffer, 0, n));
					System.out.flush();
				}			
			} finally {
				br.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

}
