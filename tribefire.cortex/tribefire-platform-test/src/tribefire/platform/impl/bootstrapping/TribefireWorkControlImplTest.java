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
package tribefire.platform.impl.bootstrapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.VerySlow;

@Category(VerySlow.class)
public class TribefireWorkControlImplTest {

	protected static ExecutorService service = Executors.newFixedThreadPool(100);

	@AfterClass
	public static void shutdown() throws Exception {
		service.shutdown();
	}

	//@Test
	public void testWorkPermission() throws Exception {


		int runs = 100;
		Random rnd = new Random();

		for (int k=0; k<runs; ++k) {
			
			TribefireWorkControlImpl.instance.permissionGranted = false;
			
			List<Future<Boolean>> futures = new ArrayList<>();
			
			int count = rnd.nextInt(50);
			for (int i=0; i<count; ++i) {
				Worker w = new Worker(i);
				Future<Boolean> submit = service.submit(w);
				futures.add(submit);
			}

			for (Future<Boolean> future : futures) {
				if (future.isDone()) {
					throw new Exception("One future is done.");
				}
			}
			Thread.sleep(2000L);
			
			TribefireWorkControlImpl.instance.giveWorkPermission();
			
			for (Future<Boolean> future : futures) {
				future.get(1000L, TimeUnit.MILLISECONDS);
			}
			
			System.out.println(""+count+" workers returned successfully true");

		}

		
	}
	
	//@Test
	public void testWorkPermissionWithDelay() throws Exception {


		int runs = 100;
		Random rnd = new Random();

		for (int k=0; k<runs; ++k) {
			
			TribefireWorkControlImpl.instance.permissionGranted = false;
			
			List<Future<Boolean>> futures = new ArrayList<>();
			
			int count = rnd.nextInt(50);
			long delay = rnd.nextInt(2000);
			
			PermissionGiver pg = new PermissionGiver(delay);
			service.submit(pg);
			
			for (int i=0; i<count; ++i) {
				Thread.sleep(50L);
				Worker w = new Worker(i);
				Future<Boolean> submit = service.submit(w);
				futures.add(submit);
			}

			Thread.sleep(2000L);
			
			for (Future<Boolean> future : futures) {
				future.get(1000L, TimeUnit.MILLISECONDS);
			}
			
			System.out.println(""+count+" workers returned successfully true");

		}

		
	}
	

}
