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
package com.braintribe.logging;

import static java.util.Arrays.fill;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * <p>
 * Alters the name of the current {@link Thread} by appending a supplied {@code String}.
 * 
 * <p>
 * e.g.:
 * 
 * <pre>
 * threadRenamer.push(() -> "custom name");
 * try {
 * 	// Your code
 * } finally {
 * 	threadRenamer.pop();
 * }
 * </pre>
 * 
 */
public class ThreadRenamer {

	private static final Logger logger = Logger.getLogger(ThreadRenamer.class);
	
	private static AtomicLong threadIdCounter = new AtomicLong(0);
	
	// configurable
	private boolean oneLiner;
	private String oneLinerSeparator = "->";

	// cached
	// This is protected so that the ThreadRenamerTest can access it; not nice, but better than using reflection
	protected ThreadLocal<Stack<String>> nameStack; 

	// constants
	public static final ThreadRenamer NO_OP = new ThreadRenamer(false);
	private static final char tab = '\t';
	private static final char nl = '\n';

	public ThreadRenamer(boolean enabled) {
		this(enabled, true, null);
	}

	public ThreadRenamer(boolean enabled, boolean oneLiners, String oneLinerSeparator) {
		if (enabled) {
			this.nameStack = ThreadLocal.withInitial(Stack::new);
			this.oneLiner = oneLiners;
			if (oneLinerSeparator != null) {
				this.oneLinerSeparator = oneLinerSeparator;
			}
		}
	}

	public void push(Supplier<String> nameSupplier) {

		if (nameStack != null) {

			Thread thread = Thread.currentThread();

			nameStack.get().push(thread.getName());

			// We are expecting here to have a nameSupplier (i.e., non-null) that also returns a non-null value.
			// In any case, we would not want to have a problem here, would we?
			
			String suppliedName = null;
			if (nameSupplier != null) {
				suppliedName = nameSupplier.get();
			}
			if (suppliedName == null) {
				suppliedName = "ai"; //An acronym for absent information 
			}

			Long threadId = threadIdCounter.incrementAndGet();
			String threadIdString = Long.toString(threadId.longValue(), 36);
			String threadContextString = suppliedName + "#" + threadIdString; 
			
			logger.pushContext(threadContextString);
			thread.setName(newThreadName(thread.getName(), threadContextString));

		}

	}

	public void pop() {

		if (nameStack == null) {
			return;
		}

		Stack<String> names = nameStack.get();

		//Intentionally throwing a StackEmptyException so that we know that pop() was called more often than push()
		
		String previousName = names.pop();
		logger.popContext();
		
		if (names.isEmpty()) {
			nameStack.remove();
		}

		Thread.currentThread().setName(previousName);

	}

	protected String newThreadName(String originalName, String suppliedName) {
		if (oneLiner) {
			return originalName.concat(oneLinerSeparator).concat(suppliedName);
		} else {
			char[] tabs = new char[nameStack.get().size()];
			fill(tabs, tab);
			return new StringBuilder(originalName).append(nl).append(tabs).append(suppliedName).toString();
		}
	}

}
