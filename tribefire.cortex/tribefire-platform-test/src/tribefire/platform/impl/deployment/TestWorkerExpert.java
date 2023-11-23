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
package tribefire.platform.impl.deployment;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.braintribe.cfg.LifecycleAware;

/**
 * @author christina.wilpernig
 */
public class TestWorkerExpert implements Callable<Void>, LifecycleAware {

	private Supplier<String> generator;
	private Future<?> future;
	private BlockingQueue<String> testQueue;

	public void setGenerator(Supplier<String> generator) {
		this.generator = generator;
	}

	@Override
	public void postConstruct() {
		future = Executors.newSingleThreadExecutor().submit(this);
	}

	@Override
	public void preDestroy() {
		future.cancel(true);
	}

	@Override
	public Void call() throws Exception {

		do {
			
			String generatorOutput = null;
			
			try {
				generatorOutput = generator.get();
			} catch (Exception e) {
				// noop
			}

			testQueue.offer((generatorOutput != null) ? generatorOutput : "test-failed");

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return null;
			}

		} while (true);

	}

	public void setTestQueue(BlockingQueue<String> testQueue) {
		this.testQueue = testQueue;
		
	}

}
