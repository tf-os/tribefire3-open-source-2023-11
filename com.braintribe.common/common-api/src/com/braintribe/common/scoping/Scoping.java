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
package com.braintribe.common.scoping;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.braintribe.common.lcd.function.CheckedRunnable;
import com.braintribe.common.lcd.function.CheckedSupplier;

public interface Scoping {
	<T> T call(Callable<T> callable) throws Exception;
	
	<T> T execute(Supplier<T> supplier);
	
	<T, E extends Throwable> T executeThrowing(CheckedSupplier<T, E> supplier) throws E;
	
	void run(Runnable runnable);
	
	<E extends Throwable> void runThrowing(CheckedRunnable<E> runnable) throws E;
	
	static Scoping with(Runnable before, Runnable after) {
		Objects.requireNonNull(before, "Argument before may not be null");
		Objects.requireNonNull(after, "Argument after may not be null");
		return new GenericScoping(before, after);
	}
}

class GenericScoping implements Scoping {
	private Runnable before;
	private Runnable after;
	
	public GenericScoping(Runnable before, Runnable after) {
		this.before = before;
		this.after = after;
	}

	@Override
	public <E extends Throwable> void runThrowing(CheckedRunnable<E> runnable) throws E {
		before.run();
		try {
			runnable.run();
		}
		finally {
			after.run();
		}
	}
	
	@Override
	public void run(Runnable runnable) {
		before.run();
		try {
			runnable.run();
		}
		finally {
			after.run();
		}
	}
	
	@Override
	public <T, E extends Throwable> T executeThrowing(CheckedSupplier<T, E> supplier) throws E {
		before.run();
		try {
			return supplier.get();
		}
		finally {
			after.run();
		}
	}
	
	@Override
	public <T> T execute(Supplier<T> supplier) {
		before.run();
		try {
			return supplier.get();
		}
		finally {
			after.run();
		}
	}
	
	@Override
	public <T> T call(Callable<T> callable) throws Exception {
		before.run();
		try {
			return callable.call();
		}
		finally {
			after.run();
		}
	}
}
