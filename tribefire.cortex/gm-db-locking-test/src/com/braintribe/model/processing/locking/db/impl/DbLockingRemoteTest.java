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
package com.braintribe.model.processing.locking.db.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.db.DbVendor;
import com.braintribe.model.processing.locking.db.test.remote.JvmExecutor;
import com.braintribe.model.processing.locking.db.test.remote.RemoteProcess;
import com.braintribe.model.processing.locking.db.test.wire.contract.DbLockingTestContract;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;

/**
 * Tests for {@link DbLocking}
 * 
 * NOTE: I have no idea what this is testing, it's been copied from the old tests for DbLockManager and worked right off the bat.
 * 
 * @see DbLockingTestContract
 */
public class DbLockingRemoteTest extends AbstractDbLockingTestBase {

	public static final String IDENTIFIER = "someIdentifier";

	public DbLockingRemoteTest(DbVendor vendor) {
		super(vendor);
	}

	private final static long INTERVAL = 500L;
	private final static long LOCK_TIMEOUT = INTERVAL * 20;

	// ###############################################
	// ## . . . . . . . . . Tests . . . . . . . . . ##
	// ###############################################

	@Test
	@Category(VerySlow.class)
	public void testRemoteJvmsWithoutFailProbability() throws Exception {

		File tempFile = File.createTempFile("number", ".txt");
		FileTools.writeStringToFile(tempFile, "0");
		try {
			int worker = 2;
			int iterations = 10;

			List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(worker, 0, 60_000L, tempFile.getAbsolutePath(), iterations);

			for (RemoteProcess p : remoteProcesses) {
				p.getProcess().waitFor();
			}

			String content = FileTools.readStringFromFile(tempFile);
			print("Read content: " + content);
			int number = Integer.parseInt(content);
			int expected = worker * iterations;
			print("Read number: " + number + ", Expecting " + expected);
			assertThat(number).isEqualTo(expected);
		} finally {
			FileTools.deleteFile(tempFile);
		}

	}

	@Test
	@Category(VerySlow.class)
	public void testRemoteJvmsWithFailProbability() throws Exception {

		File tempFile = File.createTempFile("number", ".txt");
		FileTools.writeStringToFile(tempFile, "0");
		try {
			int worker = 10;
			int iterations = 10;

			long wait = (worker * iterations * INTERVAL) + (LOCK_TIMEOUT * worker);

			List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(worker, 10, wait, tempFile.getAbsolutePath(), iterations);

			for (RemoteProcess p : remoteProcesses) {
				p.getProcess().waitFor();
			}

			String content = FileTools.readStringFromFile(tempFile);
			print("Read content: " + content);
			int number = Integer.parseInt(content);
			assertThat(number).isGreaterThanOrEqualTo(worker);
		} finally {
			FileTools.deleteFile(tempFile);
		}

	}

	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Main]: " + text);
	}

}
