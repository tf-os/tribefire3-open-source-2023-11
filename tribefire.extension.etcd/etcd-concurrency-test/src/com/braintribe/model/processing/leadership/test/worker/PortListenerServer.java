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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.utils.DateTools;

public class PortListenerServer implements Runnable {

	protected boolean stop = false;
	protected CountDownLatch hasStopped = new CountDownLatch(1);
	protected int port = 2048;

	protected Exception exception = null;

	protected List<PortListener> communicators = new ArrayList<>();
	protected ReentrantLock commLock = new ReentrantLock();

	public PortListenerServer(int port) {
		this.port = port;
	}

	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [PortListenerServer]: " + text);
		System.out.flush();
	}

	@Override
	public void run() {

		try {
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(this.port);
				ss.setSoTimeout(1000);
				while (!this.stop) {

					try {
						Socket s = ss.accept();

						if (s != null) {
							commLock.lock();
							try {
								if (communicators.size() > 0) {
									s.close();
									throw new Exception("Received more than one parallel connection");
								} else {

									print("Received one socket connection. Starting port listener.");

									PortListener communicator = new PortListener(this, s);
									Thread t = Thread.ofVirtual().name("Communicator").start(communicator);
									this.communicators.add(communicator);

									print("Added communicator to list");
								}
							} finally {
								commLock.unlock();
							}
						}
					} catch (SocketTimeoutException ignore) { /* Ignore */
					}

				}
			} catch (Exception e) {
				this.exception = e;
				e.printStackTrace(System.out);
			} finally {
				print("Shutting down; closing server socket on port " + port);

				if (ss != null) {
					try {
						ss.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			print("Shutting down; informing " + this.communicators.size() + " communicators to close down");

			commLock.lock();
			try {
				for (PortListener c : this.communicators) {

					print("Shutting down; informing communicator to close down");

					c.close();
				}
				this.communicators.clear();
			} finally {
				commLock.unlock();
			}
		} finally {
			hasStopped.countDown();
		}
	}

	public void reset() {
		commLock.lock();
		try {
			for (PortListener c : this.communicators) {
				c.close();
			}
			this.communicators.clear();
		} finally {
			commLock.unlock();
		}
		this.exception = null;
		hasStopped = new CountDownLatch(1);
	}

	public void stopListening() {

		print("Got the command from the outside to shutdown");

		this.stop = true;
		try {
			if (!hasStopped.await(60000L, TimeUnit.MILLISECONDS)) {
				throw new RuntimeException("Timeout while waiting for stop.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted while waiting for stop.", e);
		}
	}
	public Exception getException() {
		return exception;
	}

	public void communicatorClosed(PortListener portCommunicator) {
		commLock.lock();
		try {
			this.communicators.remove(portCommunicator);
		} finally {
			commLock.unlock();
		}
	}

}
