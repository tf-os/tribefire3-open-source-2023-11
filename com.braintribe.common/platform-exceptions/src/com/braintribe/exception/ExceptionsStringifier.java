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
package com.braintribe.exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.braintribe.exception.Exceptions.FrameInfo;

/**
 * Utility class to render a given Throwable into either a String or directly append it to
 * a provded {@link Appendable}.
 * <br><br>
 * Please note the output of this method dramatically differs from the classic output
 * created by {@link Throwable#printStackTrace()}. 
 * First, we have the root cause and all additional contexts that were accumulated by other exceptions on top of the exception stack trace. 
 * This should help (especially non-techies) to identify the cause for all the trouble.
 * <br><br>
 * The "... (x) more" output was replaced by a reference to the actual line the in the stack trace (who counts these lines anyway?) 
 * using a marker starting with "$". 
 * <br><br>
 * Note that the methods of these class are marked "protected". The main access point to these 
 * methods are {@link Exceptions#stringify(Throwable)} and {@link Exceptions#stringify(Throwable, Appendable)}.
 */
public class ExceptionsStringifier {

	private final static int STACK_HOOK_REFERENCE_INDEX = 1;
	private final static int EXCEPTION_INDEX = 2;
	private final static int DUPLICATE_MESSAGE_MAX_LENGTH = 255;

	protected static String stringify(Throwable t) {
		StringBuilder builder = new StringBuilder();
		stringify(t, builder);
		return builder.toString();
	}



	protected static void stringify(Throwable throwable, Appendable appendable) {

		if (throwable == null) {
			try {
				appendable.append("<null>");
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		
		ArrayList<ThrowableInformation> queue = new ArrayList<>();
		ThrowableInformation throwableInformation = new ThrowableInformation(throwable);
		queue.add(throwableInformation);
		int[] indices = new int[] {0,0,0};

		ArrayList<ThrowableInformation> renderingQueue = new ArrayList<>();
		Set<Throwable> visitedThrowables = new HashSet<>();
		boolean initialRun = true;

		while (!queue.isEmpty()) {

			throwableInformation = queue.remove(0);

			process(throwableInformation, queue, renderingQueue, indices, initialRun, visitedThrowables);
			
			initialRun = false;
		}

		int size = renderingQueue.size();
		for (int i=0; i<size; ++i) {
			throwableInformation = renderingQueue.get(i);
			try {
				throwableInformation.render(appendable);
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}



	private static void process(ThrowableInformation ti, List<ThrowableInformation> queue, List<ThrowableInformation> renderingQueue, int[] indices, boolean initialRun, Set<Throwable> visitedThrowables) {

		ArrayList<ThrowableInformation> throwableChain = new ArrayList<>();
		throwableChain.add(ti);
		Throwable t = ti.throwable;
		List<ThrowableInformation> wrappers = new ArrayList<>(5);
		List<ThrowableInformation> wrappersToBeNumbered = new ArrayList<>(15);
		List<ThrowableInformation> contextsToBeNumbered = new ArrayList<>(15);
		
		int contextBacktrackIndex = 1;
		while (t != null) {

			if (!visitedThrowables.add(t)) {
				//loop detected
				break;
			}
			
			Throwable[] suppressed = t.getSuppressed();
			int size = suppressed.length;
			for (int i=0; i<size; ++i) {
				Throwable s = suppressed[i];
				if (s.getClass() == ThrowableContext.class) {

					ThrowableInformation contextTi = new ThrowableInformation(s);
					contextTi.type = "Context";
					contextsToBeNumbered.add(contextTi);
					if (ti.contextEntries == null) {
						ti.contextEntries = new ArrayList<>(5);
					}
					ti.contextEntries.add(contextTi);
					contextTi.wrapperOfSuppressed = ti;
					contextTi.findCommonTraceElementsWithCause(ti);
					throwableChain.add(throwableChain.size()-contextBacktrackIndex, contextTi);
					contextBacktrackIndex++;

				} else {
					ThrowableInformation sTi = new ThrowableInformation(s);
					sTi.type = "--\nSuppressed";
					sTi.wrapperOfSuppressed = ti;
					
					sTi.findCommonTraceElementsWithCause(ti);
					queue.add(sTi);
				}
			}


			Throwable causedBy = t.getCause();
			if (causedBy != null) {
				ThrowableInformation causedByTi = new ThrowableInformation(causedBy);
				
				causedByTi.wrapperOfSuppressed = ti.wrapperOfSuppressed;
				
				wrappersToBeNumbered.add(0, ti);
				ti.type = "Wrapper";
				ti.cause = causedByTi;

				if (causedByTi.wrapperOfSuppressed != null) {
					ti.findCommonTraceElementsWithCause(causedByTi.wrapperOfSuppressed);
				} else {
					ti.findCommonTraceElementsWithCause(causedByTi);
				}
				throwableChain.add(causedByTi);
				wrappers.add(ti);
				ti = causedByTi;
			} else {
				if (!initialRun) {
					ti.type = "--\nSuppressed";
					if (ti.exceptionNumber == 0) {
						wrappersToBeNumbered.add(0, ti);
					}
					
					if (ti.wrapperOfSuppressed != null) {
						if (ti.wrapperOfSuppressed.suppressedTis == null) {
							ti.wrapperOfSuppressed.suppressedTis = new ArrayList<>(5);
						}
						ti.wrapperOfSuppressed.suppressedTis.add(ti);						
					}
					
				}
				ti.wrappers = wrappers;
				
				if (ti.wrapperOfSuppressed != null) {
					ti.findCommonTraceElementsWithCause(ti.wrapperOfSuppressed);
				}
				break;
			}

			t = causedBy;
		}
		
		int size = contextsToBeNumbered.size();
		for (int i=0; i<size; ++i) {
			ThrowableInformation tbn = contextsToBeNumbered.get(i);
			tbn.exceptionNumber = ++indices[EXCEPTION_INDEX];	
		}
		size = wrappersToBeNumbered.size();
		for (int i=0; i<size; ++i) {
			ThrowableInformation tbn = wrappersToBeNumbered.get(i);
			tbn.exceptionNumber = ++indices[EXCEPTION_INDEX];	
		}


		size = throwableChain.size();

		for (int i=size-1; i >= 0; --i) {
			ti = throwableChain.get(i);
			ti.findStackHook(indices);
			renderingQueue.add(ti);
		}

	}


	private static class ThrowableInformation {
		public Throwable throwable;
		public ThrowableInformation wrapperOfSuppressed;
		public ThrowableInformation cause;

		public StackTraceElement[] stackTraceElements;
		public int framesInCommon;
		public int uniqueFrames;
		public int stackElementReference;
		public List<ThrowableInformation> contextEntries;
		public List<ThrowableInformation> wrappers;
		public List<ThrowableInformation> suppressedTis;
		public int[] stackElementHooks;
		private int minFramesToShow = 0;
		public int exceptionNumber = 0;
		public String type = null;

		private ThrowableInformation(Throwable throwable) {
			this.throwable = throwable;
			stackTraceElements = throwable.getStackTrace();
			uniqueFrames = stackTraceElements.length;
		}

		@Override
		public String toString() {
			//To make life with the debugger easier
			return throwable.getClass().getName()+": "+throwable.getMessage()+" (type: "+type+"; suppressed: "+(suppressedTis != null ? suppressedTis.size() : 0)+")";
		}

		private void findStackHook(int[] indices) {
			if (framesInCommon == 0) {
				return;
			}
			ThrowableInformation currentCause = wrapperOfSuppressed != null ? wrapperOfSuppressed : cause;
			ThrowableInformation target = null;
			while(currentCause != null) {
				target = currentCause;
				if (currentCause.framesInCommon == 0) {
					break;
				}
				currentCause = currentCause.cause;
			}
			if (target != null) {
				stackElementReference = target.requestStackElementReference(this, indices);
			}
		}

		private int requestStackElementReference(ThrowableInformation wrapperTi, int[] indices) {

			StackTraceElement[] enclosingTrace = wrapperTi.stackTraceElements;
			int m = stackTraceElements.length - 1;
			int n = enclosingTrace.length - 1;
			while (m >= 0 && n >=0 && stackTraceElements[m].equals(enclosingTrace[n])) {
				m--; n--;
			}
			int hookIndex = m+1;
			if (hookIndex >= stackTraceElements.length) {
				return 0; //No hook possible
			}
			if (stackElementHooks == null) {
				stackElementHooks = new int[stackTraceElements.length];
			}
			if (stackElementHooks[hookIndex] == 0) {
				stackElementHooks[hookIndex] = ++indices[STACK_HOOK_REFERENCE_INDEX];
			}
			if (minFramesToShow < hookIndex) {
				minFramesToShow = hookIndex;
			}
			return stackElementHooks[hookIndex];
		}

		protected void findCommonTraceElementsWithCause(ThrowableInformation causeTi) {
			StackTraceElement[] enclosingTrace = causeTi.stackTraceElements;
			int m = stackTraceElements.length - 1;
			int n = enclosingTrace.length - 1;
			while (m >= 0 && n >=0 && stackTraceElements[m].equals(enclosingTrace[n])) {
				m--; n--;
			}
			framesInCommon = stackTraceElements.length - 1 - m;
			uniqueFrames = m+1;
		}

		protected void render(Appendable appendable) throws IOException {

			if (type != null) {
				appendable.append('\n');
				appendable.append(type);
				if (exceptionNumber != 0) {
					appendable.append(" #");
					appendable.append(Exceptions.intToString(exceptionNumber));
				} else {
					appendable.append(" #<no-reference>");
				}
				appendable.append(": ");
			} else {
				appendable.append("Root cause: ");
			}

			appendable.append(throwable.getClass().getName());
			String msg = throwable.getMessage();
			if (msg != null) {
				appendable.append(": ");
				if (exceptionNumber > 0) {
					//The full message is rendered with the root cause; no need to write everything again.
					int length = msg.length();
					if (length > DUPLICATE_MESSAGE_MAX_LENGTH) {
						appendable.append(msg, 0, DUPLICATE_MESSAGE_MAX_LENGTH);
						appendable.append("...");
					} else {
						appendable.append(msg);
					}
				} else {
					appendable.append(msg);
				}
			}
			int contextEntriesSize = contextEntries != null ? contextEntries.size() : 0;
			for (int i=0; i<contextEntriesSize; ++i) {
				ThrowableInformation e = contextEntries.get(i);
				appendable.append("\n\tContext #");
				appendable.append(Exceptions.intToString(e.exceptionNumber));
				String message = e.throwable.getMessage();
				if (message != null && !message.isEmpty()) {
					appendable.append(": ");
					appendable.append(message);
				}
			}
			int wrappersSize = wrappers != null ? wrappers.size() : 0;
			for (int i=wrappersSize-1; i>=0; --i) {
				ThrowableInformation wrapperTi = wrappers.get(i);
				Throwable t = wrapperTi.throwable;
				String message = t.getMessage();
				if (message != null && !message.isEmpty()) {
					appendable.append("\n\tWrapper");
					if (wrapperTi.exceptionNumber != -1) {
						appendable.append(" #");
						appendable.append(Exceptions.intToString(wrapperTi.exceptionNumber));
					}
					appendable.append(": ");
					appendable.append(t.getClass().getName());
					appendable.append(": ");
					appendable.append(message);
				}
			}
			int suppressedSize = suppressedTis != null ? suppressedTis.size() : 0; 
			if (suppressedSize > 0) {
				for (int i=0; i<suppressedSize; ++i) {
					ThrowableInformation sTi = suppressedTis.get(i);
					
					appendable.append("\n\tSuppressed #");
					appendable.append(Exceptions.intToString(sTi.exceptionNumber));
					appendable.append(": ");
					appendable.append(sTi.throwable.getClass().getName());
					String message = sTi.throwable.getMessage();
					if (message != null && !message.isEmpty()) {
						appendable.append(": ");
						appendable.append(message);
					}
				}
			}

			if (wrappersSize > 0 || contextEntriesSize > 0 || suppressedSize > 0) {
				appendable.append("\n\t--");				
			}
			int framesToShow = uniqueFrames;
			if (minFramesToShow > uniqueFrames) {
				framesToShow = minFramesToShow;
			}
			for (int i = 0; i < framesToShow; i++) {

				int hook = stackElementHooks != null ? stackElementHooks[i] : 0;
				if (hook != 0) {
					appendable.append("\n$");
					appendable.append(Exceptions.intToStringRadix36(hook));
					appendable.append("\tat ");
				} else {
					appendable.append("\n\tat ");
				}

				FrameInfo.renderStackTraceElement(appendable, stackTraceElements[i]);
			}
			if (framesInCommon > 0) {
				if (stackElementReference == 0) {
					appendable.append("\n\t... ");
					appendable.append(Exceptions.intToString(framesInCommon));
					appendable.append(" more");
				} else {
					appendable.append("\n\tat $");
					appendable.append(Exceptions.intToStringRadix36(stackElementReference));
				}
			}
		}
	}
}
