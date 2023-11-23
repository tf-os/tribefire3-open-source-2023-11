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
package com.braintribe.model.processing.locking.db.test.remote;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;

public class LockingWorker implements Runnable {

	private final Locking locking;
	private final String workerId;
	private final int iterations;
	private ThreadCompleteListener listener;
	private int failProbability;
	private final File file;
	private final long maxWait;

	public static final String IDENTIFIER = "someIdentifier";
	public final static long INTERVAL = 500L;
	public final static long LOCK_TIMEOUT = INTERVAL * 20;
	public final static long LOCK_TRY_WAIT = 100L;

	public LockingWorker(Locking locking, String workerId, int iterations, File file, long maxWait) {
		this.locking = locking;
		this.workerId = workerId;
		this.file = file;
		this.iterations = iterations;
		this.maxWait = maxWait;
	}

	private void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Worker/" + workerId + "]: " + text);
		System.out.flush();
	}

	@Override
	public void run() {

		try {
			Lock lock = locking.forIdentifier(IDENTIFIER).readLock();

			long start = System.currentTimeMillis();
			int expectedNumber = -1;
			while (true) {

				boolean gotLock = false;
				try {
					gotLock = lock.tryLock(LOCK_TRY_WAIT, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					print("Got interrupted.");
					return;
				}
				if (gotLock) {

					print("Got lock");
					boolean doUnlock = true;

					try {
						for (int i = 0; i < iterations; ++i) {

							String content = FileTools.readStringFromFile(file);
							int number = Integer.parseInt(content);
							if (expectedNumber == -1) {
								expectedNumber = number;
							} else {
								if (number != expectedNumber) {
									FileTools.writeStringToFile(file, "Worker " + workerId + " expected " + expectedNumber + " but got " + number);
									print("Unexpected number: " + number + ", expected: " + expectedNumber);
									return;
								}
							}
							number++;
							expectedNumber = number;
							print("Writing " + number + " (iteration: " + i + ")");
							FileTools.writeStringToFile(file, "" + number);

							if (this.failProbability > 0) {
								int randomNumber = (new Random(System.currentTimeMillis())).nextInt(100);
								if (randomNumber < this.failProbability) {
									print("Quitting without notice; keeping lock to check timeout functionality");
									doUnlock = false;
									return;
								}
							}

							try {
								Thread.sleep(INTERVAL);
							} catch (InterruptedException e) {
								print("Got interrupted while waiting for the next run.");
								return;
							}
						}

					} finally {
						if (doUnlock) {
							print("Unlocking");
							lock.unlock();
						} else {
							print("Deliberately not unlocking. Let's see what happens.");
						}
					}
					return;
				} else {
					if ((System.currentTimeMillis() - start) > maxWait) {
						print("Waited too long for a lock. Aborting.");
						return;
					}
				}
			}

		} finally {
			if (listener != null) {
				listener.notifyOfThreadComplete();
			}
		}
	}
	public void setFailProbability(int failProbability) {
		this.failProbability = failProbability;
	}

	public void registerManger(ThreadCompleteListener listener) {
		this.listener = listener;
	}
}
