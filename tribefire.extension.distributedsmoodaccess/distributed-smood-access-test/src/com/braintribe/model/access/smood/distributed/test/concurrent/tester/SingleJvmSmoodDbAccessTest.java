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
package com.braintribe.model.access.smood.distributed.test.concurrent.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.braintribe.model.access.smood.distributed.test.concurrent.worker.Worker;

public class SingleJvmSmoodDbAccessTest extends AbstractSmoodDbAccessTest {

	@Override
	protected void executeWorkers(int workerCountParam, int iterations) throws Exception {

		ExecutorService service = Executors.newFixedThreadPool(workerCountParam);
		List<Future<Void>> futures = new ArrayList<Future<Void>>();

		for (int i=0; i<workerCountParam; ++i) {
			Worker worker = super.factory.provideWorker();
			worker.setId(""+i);
			futures.add(service.submit(worker));
		}

		boolean allDone = false;
		while (!allDone) {
			allDone = true;
			for (Future<Void> f : futures) {
				if (!f.isDone()) {
					allDone = false;
					break;
				}
			}			
		}
		
		service.shutdown();
	}

}
