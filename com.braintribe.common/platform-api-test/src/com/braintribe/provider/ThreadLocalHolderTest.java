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
package com.braintribe.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class ThreadLocalHolderTest {

	@Test
	public void simpleTest() {

		ThreadLocalHolder<String> holder = new ThreadLocalHolder<>();

		String value = UUID.randomUUID().toString();

		assertThat(holder.get()).isNull();

		holder.accept(value);

		assertThat(holder.get()).isEqualTo(value);

		holder.accept(null);

		assertThat(holder.get()).isNull();

		holder.accept(value);

		assertThat(holder.get()).isEqualTo(value);

		holder.release();

		assertThat(holder.get()).isNull();

	}

	@Test
	public void testMultithreaded() throws Exception {

		int workers = 10;
		int iterations = 100;
		ThreadLocalHolder<String> holder = new ThreadLocalHolder<>();

		ExecutorService service = Executors.newFixedThreadPool(workers);
		try {

			List<Future<?>> futures = new ArrayList<>();
			for (int i = 0; i < workers; ++i) {
				futures.add(service.submit(() -> {
					for (int j = 0; j < iterations; ++j) {

						String value = UUID.randomUUID().toString();
						holder.accept(value);
						try {
							Thread.sleep(1L);
						} catch (InterruptedException e) {
							return;
						}
						assertThat(holder.get()).isEqualTo(value);

					}
					holder.release();
				}));
			}

			for (Future<?> f : futures) {
				f.get();
			}

		} finally {
			service.shutdown();
		}

	}
}
