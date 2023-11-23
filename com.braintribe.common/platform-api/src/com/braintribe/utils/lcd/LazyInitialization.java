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
package com.braintribe.utils.lcd;

/**
 * Convenient wrapper for code that should only be executed once.
 * <p>
 * This implementation is thread safe.
 *
 * @see #run()
 * @see LazyInitialized
 */
public class LazyInitialization {

	private LazyInitialized<Void> lazyInitialized;

	public LazyInitialization(Runnable runnable) {
		this.lazyInitialized = new LazyInitialized<>(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Runs the actual {@link Runnable} provided via constructor, but only on first invocation of this method. All subsequent invocations have no
	 * effect.
	 */
	public void run() {
		lazyInitialized.get();
	}

}
