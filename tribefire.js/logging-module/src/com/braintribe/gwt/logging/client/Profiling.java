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
package com.braintribe.gwt.logging.client;



public class Profiling {
	private static boolean profilingEnabled = false;
	
	public static void setProfilingEnabled(boolean enabled) {
		profilingEnabled = enabled;
	}

	public static boolean isProfilingEnabled() {
		return profilingEnabled;
	}
	
	private static class ProfilingHandleImpl implements ProfilingHandle {
		private Class<?> clazz;
		private String hint;
		private long start = System.currentTimeMillis();
		private long end = 0;
		private int depth;
		private ProfilingHandleImpl predecessor;
		private boolean async;
		private boolean debug;
		
		public ProfilingHandleImpl(boolean async, ProfilingHandleImpl predecessor, Class<?> clazz, String hint, int depth, boolean debug) {
			super();
			this.clazz = clazz;
			this.hint = hint;
			this.depth = depth;
			this.predecessor = predecessor;
			this.async = async;
			this.debug = debug;
		}
		
		public boolean isAsync() {
			return async;
		}
		
		public boolean isDebug() {
			return debug;
		}
		
		public String getHint() {
			return hint;
		}
		
		public Class<?> getProfilingClass() {
			return clazz;
		}
		
		public long getDelta() {
			return end - start;
		}
		
		public int getDepth() {
			return depth;
		}
		
		public ProfilingHandleImpl getPredecessor() {
			return predecessor;
		}
		
		@Override
		public void stop() {
			end = System.currentTimeMillis();
			Profiling.stop(this);
		}
		
		@Override
		public void onStartedAsync() {
			startedAsync(this);
		}
	}
	
	private static ProfilingHandleImpl lastHandle = null;
	
	public static ProfilingHandle start(Object caller, String hint, boolean async) {
		return start(caller.getClass(), hint, async, false);
	}

	public static ProfilingHandle start(Object caller, String hint, boolean async, boolean debug) {
		return start(caller.getClass(), hint, async, debug);
	}

	private static final ProfilingHandle emptyProfilingHandle = new ProfilingHandle() {
		@Override
		public void stop() {
			// ignore since profiling is deactivated
		}

		@Override
		public void onStartedAsync() {
			// ignore since profiling is deactivated
		}
	};
	
	public static ProfilingHandle start(Class<?> clazz, String hint, boolean async) {
		return start(clazz, hint, async, false);
	}

	public static ProfilingHandle start(Class<?> clazz, String hint, boolean async, boolean debug) {
		if (profilingEnabled) {
			lastHandle = new ProfilingHandleImpl(async, lastHandle, clazz, hint, getLastDepth() + 1, debug);
			
//			logStart(lastHandle);
			return lastHandle;
		} else {
			return emptyProfilingHandle;
		}
	}
	
	private static int getLastDepth() {
		return lastHandle != null? lastHandle.getDepth(): 0;
	}
	
	private static void startedAsync(ProfilingHandleImpl handle) {
		if (lastHandle == handle) {
			while (lastHandle != null && lastHandle.isAsync()) {
				lastHandle = lastHandle.getPredecessor();
			}
		}
	}
	
	private static void stop(ProfilingHandleImpl handle) {
		lastHandle = handle.getPredecessor();
		logStop(handle);		
	}

//	private static void logStart(ProfilingHandleImpl handle) {
//		//log(handle, "started");
//	}
	
	private static void logStop(ProfilingHandleImpl handle) {
		String messageText = handle.getDelta() + "ms";
		log(handle, messageText);
	}
	
	private static void log(ProfilingHandleImpl handle, String messageText) {
		if (handle.isDebug() && !LogManager.isLevelEnabled(LogLevel.PROFILINGDEBUG))
			return;
		
		String classname = handle.getProfilingClass().getName();
		int index = classname.lastIndexOf('.');
		if (index != -1)
			classname = classname.substring(index + 1);

		String category = classname;
		String hint = handle.getHint();
		
		StringBuilder message = new StringBuilder();
		/*
		for (int i = 0; i < depth; i++)
			message.append("-> ");
		*/
		message.append(hint);
		message.append(": ");
		message.append(messageText);
		
		LogEvent event = new LogEvent(handle.isDebug() ? LogLevel.PROFILINGDEBUG : LogLevel.PROFILING, category, message.toString());
		LogManager.fireLogEvent(event);
	}
}
