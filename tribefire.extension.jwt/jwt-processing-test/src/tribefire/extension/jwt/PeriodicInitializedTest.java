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
package tribefire.extension.jwt;

import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InternalError;

import tribefire.extension.jwt.processing.PeriodicInitialized;

public class PeriodicInitializedTest {

	@Test
	public void testPeriodicInitialized() {
		NumberSupplier numberSupplier = new NumberSupplier();
		PeriodicInitialized<Integer> holder = new PeriodicInitialized<>(() -> Maybe.complete(numberSupplier.get()));
		holder.setUpdateIntervalInMs(10);

		// calling stuff once to initiate classloading to have proper timing afterwards
		Assertions.assertThat(holder.get().get()).isEqualTo(0);
		numberSupplier.reset();
		holder.reset();

		Assertions.assertThat(holder.get().get()).isEqualTo(0);
		Assertions.assertThat(holder.get().get()).isEqualTo(0);
		Assertions.assertThat(holder.get().get()).isEqualTo(0);

		sleep(11);

		Assertions.assertThat(holder.get().get()).isEqualTo(1);
		Assertions.assertThat(holder.get().get()).isEqualTo(1);
		Assertions.assertThat(holder.get().get()).isEqualTo(1);

		sleep(11);

		Assertions.assertThat(holder.get().get()).isEqualTo(2);
		Assertions.assertThat(holder.get().get()).isEqualTo(2);
		Assertions.assertThat(holder.get().get()).isEqualTo(2);
	}

	@Test
	public void testPeriodicInitializedReasoning() {
		FailingSupplier numberSupplier = new FailingSupplier();
		PeriodicInitialized<Integer> holder = new PeriodicInitialized<>(numberSupplier);
		holder.setUpdateIntervalInMs(10_000);

		Assertions.assertThat(holder.get().whyUnsatisfied().getText()).isEqualTo("0");
		Assertions.assertThat(holder.get().whyUnsatisfied().getText()).isEqualTo("1");
		Assertions.assertThat(holder.get().whyUnsatisfied().getText()).isEqualTo("2");
	}

	private void sleep(long ms) {
		long s = System.nanoTime() / 1_000_000;

		while (true) {
			long c = System.nanoTime() / 1_000_000;

			if (c - s >= ms)
				return;
		}
	}

	class NumberSupplier implements Supplier<Integer> {
		private int number = 0;

		@Override
		public Integer get() {
			return number++;
		}

		public void reset() {
			number = 0;
		}
	}

	class FailingSupplier implements Supplier<Maybe<Integer>> {
		private int number = 0;

		@Override
		public Maybe<Integer> get() {
			return Reasons.build(InternalError.T).text(String.valueOf(number++)).toMaybe();
		}
	}
}
