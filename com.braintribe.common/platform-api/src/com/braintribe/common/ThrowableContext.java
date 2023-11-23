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
package com.braintribe.common;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThrowableContext extends Throwable {

	private static final long serialVersionUID = 1L;

	private Deque<Entry> entries = new ArrayDeque<>();

	public void add(CallStackFrame frame, String msg) {
		Entry entry = new Entry(frame, msg);
		entries.addLast(entry);
	}

	public ThrowableContext() {
		super("", null, false, false);
	}

	@Override
	public String getMessage() {
		return Stream.concat(Stream.of("The following lines are showing context messages per stack frame:"), entries.stream().map(Entry::toString))
				.collect(Collectors.joining("\n\t\tat "));
	}

	public Collection<Entry> getEntries() {
		return entries;
	}

	public static class Entry {
		private CallStackFrame frame;
		private String msg;

		public Entry(CallStackFrame frame, String msg) {
			super();
			this.frame = frame;
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public CallStackFrame getFrame() {
			return frame;
		}

		@Override
		public String toString() {
			return frame.stackTraceElement.toString() + "[" + frame.stackIndex + "]: " + msg;
		}
	}
}
