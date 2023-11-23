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
package com.braintribe.model.processing.leadership.test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.integration.etcd.supplier.ClientSupplier;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.CollectionTools2;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.lease.LeaseGrantResponse;

//Ignore, was just used for some curious tests

@Category(SpecialEnvironment.class)
public class NativeLockTest {

	@Test
	public void testLocking() throws Exception {

		ClientSupplier clientSupplier = new ClientSupplier(CollectionTools2.asList("http://localhost:2379"), null, null);
		Client client = clientSupplier.get();
		Lock lockClient = client.getLockClient();
		String lockId = "/lock-test";
		ByteSequence bsLockId = ByteSequence.from(lockId, StandardCharsets.UTF_8);


		ExecutorService service = Executors.newFixedThreadPool(5);
		try {

			Future<?> f1 = service.submit(() -> {
				
				LeaseGrantResponse leaseGrantResponse;
				try {
					leaseGrantResponse = client.getLeaseClient().grant(20).get();
				} catch (Exception e2) {
					throw new RuntimeException("Lease error 1", e2);
				}
				long leaseId = leaseGrantResponse.getID();
				
				System.out.println("Thread 1: Trying to get lock.");
				Instant start = NanoClock.INSTANCE.instant();
				try {
					lockClient.lock(bsLockId, leaseId).get();
				} catch (Exception e1) {
					throw new RuntimeException("Lock error 1", e1);
				}
				System.out.println("Thread 1: Got lock after "+StringTools.prettyPrintDuration(start, true, null));
				System.out.println("Thread 1: Keeping lock for 20 seconds.");
				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted");
				}
				System.out.println("Thread 1: Unlocking.");
				lockClient.unlock(bsLockId);
				System.out.println("Thread 1: Done.");
			});
			
			Future<?> f2 = service.submit(() -> {
				
				LeaseGrantResponse leaseGrantResponse;
				try {
					leaseGrantResponse = client.getLeaseClient().grant(20).get();
				} catch (Exception e2) {
					throw new RuntimeException("Lease error 2", e2);
				}
				long leaseId = leaseGrantResponse.getID();

				System.out.println("Thread 2: Waiting for 5 seconds.");
				try {
					Thread.sleep(5000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted");
				}
				System.out.println("Thread 2: Trying to get lock.");
				Instant start = NanoClock.INSTANCE.instant();
				try {
					lockClient.lock(bsLockId, leaseId).get();
				} catch (Exception e1) {
					throw new RuntimeException("Lock error 2", e1);
				}
				System.out.println("Thread 2: Got lock after "+StringTools.prettyPrintDuration(start, true, null));
				System.out.println("Thread 2: Keeping lock for 20 seconds.");
				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted");
				}
				System.out.println("Thread 2: Unlocking.");
				lockClient.unlock(bsLockId);
				System.out.println("Thread 2: Done.");
			});
			
			Future<?> f3 = service.submit(() -> {
				
				LeaseGrantResponse leaseGrantResponse;
				try {
					leaseGrantResponse = client.getLeaseClient().grant(20).get();
				} catch (Exception e2) {
					throw new RuntimeException("Lease error 3", e2);
				}
				long leaseId = leaseGrantResponse.getID();

				System.out.println("Thread 3: Waiting for 20 seconds.");
				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted");
				}
				System.out.println("Thread 3: Trying to get lock.");
				Instant start = NanoClock.INSTANCE.instant();
				try {
					lockClient.lock(bsLockId, leaseId).get();
				} catch (Exception e1) {
					throw new RuntimeException("Lock error 3", e1);
				}
				System.out.println("Thread 3: Got lock after "+StringTools.prettyPrintDuration(start, true, null));
				System.out.println("Thread 3: Keeping lock for 20 seconds.");
				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("Interrupted");
				}
				System.out.println("Thread 3: Unlocking.");
				lockClient.unlock(bsLockId);
				System.out.println("Thread 3: Done.");
			});
			
			List<Future<?>> futures = CollectionTools2.asList(f1, f2, f3);
			for (Future<?> f : futures) {
				f.get();
			}
			
		} finally {
			service.shutdown();
		}
	}
}
