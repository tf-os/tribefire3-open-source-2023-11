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
package com.braintribe.model.processing.leadership.test.worker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.braintribe.utils.DateTools;

public class PortListener implements Runnable {

	protected boolean stop = false;
	protected Socket socket = null;
	protected InputStream inputStream = null;
	protected PortListenerServer portListener = null;
	protected CountDownLatch closed = new CountDownLatch(1);

	public PortListener(PortListenerServer portListener, Socket s) {
		this.portListener = portListener;
		this.socket = s;
	}

	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT)+" [PortListener]: "+text);
		System.out.flush();
	}


	@Override
	public void run() {

		try {
			try {

				print("Starting to listen on socket.");

				this.inputStream = this.socket.getInputStream();
				this.socket.setSoTimeout(2000);
				BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
				while (!this.stop) {
					try {
						String line = in.readLine();
						if (line != null) {
							if (line.equals("quit")) {
								break;
							}
							System.out.println(line);
						}
					} catch(SocketTimeoutException ignore) { /* Ignore */
					}
				}
			} catch(Exception e) {
				if (!this.socket.isClosed()) {
					e.printStackTrace(System.out);
				}
			}

			print("Informing port listener server that this communicator has closed down");

			this.closed();
		} finally {

			print("Closed");

			closed.countDown();
		}
	}

	private void closed() {
		try {
			if (this.inputStream != null) {
				this.inputStream.close();
			}
			if (this.socket != null) {
				this.socket.close();
			}
		} catch(Exception e) { /* Ignore */
		}
		this.portListener.communicatorClosed(this);
	}


	public void close() {
		this.stop = true;
		try {
			if (!closed.await(60000L, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Timeout while waiting for close.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting for close.", e);
		}
	}

}
