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
package com.braintribe.model.processing.lock.etcd.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.lock.etcd.EtcdLockManager;
import com.braintribe.model.processing.lock.etcd.config.Configurator;
import com.braintribe.model.processing.lock.etcd.remote.JvmExecutor;
import com.braintribe.model.processing.lock.etcd.remote.RemoteProcess;
import com.braintribe.model.processing.lock.etcd.wire.contract.EtcdLockingTestContract;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;

@Category(SpecialEnvironment.class)
public class EtcdLockManagerTest {

	private static EtcdLockManager etcdLockManager;

	protected static EtcdLockingTestContract configuration = null;
	protected static Configurator configurator = null;

	public static final String IDENTIFIER = "someIdentifier";

	public static int DERBY_PORT = 1527;
	public static int ACTIVEMQ_PORT = 61636;

	protected static Worker activeMqWorker = null; 
	
	public final static long INTERVAL = 500L;
	public final static long LOCK_TIMEOUT = INTERVAL * 20;
	public final static long LOCK_TRY_WAIT = 100L;
	
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		
		configurator = new Configurator();
		configuration = configurator.getConfiguration();
		
		//Call this on startup so that the result is cached and does not interfere with other tests that rely on timing
		NetworkTools.getNetworkAddress().getHostAddress();
		
		etcdLockManager = configuration.lockManager();
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		
		if (configurator != null) {
			configurator.close();
		}
	}
	
	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT)+" [Master]: "+text);
	}

	// -----------------------------------------------------------------------
	// SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		cleanupEtcdLocks();
	}

	@After
	public void cleanupEtcdLocks() throws Exception {

		List<URI> endpointUris = configuration.endpointUrls().stream().map(u -> {
			try {
				return new URI(u);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		Client client = Client.builder().endpoints(endpointUris).build();
		KV kvClient = client.getKVClient();
		
		try {
			ByteSequence fromKey = ByteSequence.from("lock/", StandardCharsets.UTF_8);
			ByteSequence toKey = ByteSequence.from("locl/", StandardCharsets.UTF_8);

			GetOption getOption = GetOption.newBuilder().withRange(toKey).build();

			GetResponse response = kvClient.get(fromKey, getOption).get();

			List<KeyValue> kvs = response.getKvs();
			for (KeyValue kv : kvs) {
				ByteSequence delKey = kv.getKey();
				//System.out.println("Deleting key: "+delKey.toStringUtf8());
				kvClient.delete(delKey).get();
			}
			
		} finally {
			IOTools.closeCloseable(kvClient, null);
			IOTools.closeCloseable(client, null);
		}

	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------
	private Lock createLock() {
		return etcdLockManager.forIdentifier(IDENTIFIER).shared();
	}
	private Lock createLock(long lockTimeout) {
		return etcdLockManager.forIdentifier(IDENTIFIER).lockTtl(lockTimeout, TimeUnit.MILLISECONDS).shared();
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	//Manual test that might come in handy when you need to check something in etcd
	//Add a breakpoint in the System.out.println line to check etcd after a acquired lock.
	@Test
	public void testLockTtl() throws Exception {
		Lock lock = createLock(60000);
		lock.lock();
		try {
			System.out.println("Locked.");
		} finally {
			lock.unlock();
		}
	}
	
	@Test(timeout = 5000)
	public void testLockTryLock() throws Exception {

		Lock lock = createLock();
		lock.lock();
		lock.unlock();

		boolean tryLock = lock.tryLock();
		assertThat(tryLock).isTrue();
	}

	/**
	 * Simulate that a lock is stuck because of e.g. a crash; check if tryLock without parameter works
	 */
	@Test(timeout = 5000)
	public void testLockRecover1() throws Exception {

		Lock lock = createLock();
		lock.lock();

		boolean tryLock = lock.tryLock();
		assertThat(tryLock).isFalse();
	}

	/**
	 * Simulate that a lock is stuck because of e.g. a crash; there is a lock - check if tryLock with parameter works
	 */
	@Test(timeout = 5000)
	public void testLockRecover2() throws Exception {

		Lock lock = createLock();
		lock.lock();

		boolean tryLock = lock.tryLock(1, TimeUnit.SECONDS);
		assertThat(tryLock).isFalse();
	}

	@Test(timeout = 500000, expected = TimeoutException.class)
	public void testLockLock() throws Exception {

		final Lock lock = createLock();
		lock.lock();

		ExecutorService service = Executors.newFixedThreadPool(2);
		
		TimeLimiter timeLimiter = SimpleTimeLimiter.create(service);

		timeLimiter.callWithTimeout(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				lock.lock();
				return null;
			}
		}, 2, TimeUnit.SECONDS);
		
		service.shutdown();
	}

	@Test(timeout = 10000)//TODO: remove 00
	public void testLockHardUnlock() throws Exception {

		Lock lock = createLock(3000);
		lock.lock();

		boolean tryLock = lock.tryLock(1, TimeUnit.SECONDS);
		assertThat(tryLock).isFalse();

		Thread.sleep(3000);
		
		boolean tryLock2 = lock.tryLock();
		assertThat(tryLock2).isTrue();
		
		lock.unlock();
	}
	
	@Test
	@Category(Slow.class)
	public void testRemoteJvmsWithoutFailProbability() throws Exception {
		
		File tempFile = File.createTempFile("number", ".txt");
		FileTools.writeStringToFile(tempFile, "0");
		try {
			int worker = 10;
			int iterations = 10;
			
			List<RemoteProcess> remoteProcesses = JvmExecutor.executeWorkers(worker, 0, 60_000L, tempFile.getAbsolutePath(), iterations);
			
			for (RemoteProcess p : remoteProcesses) {
				p.getProcess().waitFor();
			}
			
			String content = FileTools.readStringFromFile(tempFile);
			print("Read content: "+content);
			int number = Integer.parseInt(content);
			int expected = worker*iterations;
			print("Read number: "+number+", Expecting "+expected);
			assertThat(number).isEqualTo(expected);
		} finally {
			FileTools.deleteFile(tempFile);
		}
		
	}

	@Test
	@Category(Slow.class)
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
			print("Read content: "+content);
			int number = Integer.parseInt(content);
			assertThat(number).isGreaterThanOrEqualTo(worker);
		} finally {
			FileTools.deleteFile(tempFile);
		}
		
	}
}
