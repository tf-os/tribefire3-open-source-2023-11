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
package com.braintribe.testing.junit.rules;

// MLA: deactivated because of the Javadoc note regarding JUnit 4.7+, see below. This should be checked before
// uncommenting.
//// ============================================================================
//// Braintribe IT-Technologies GmbH - www.braintribe.com
//// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2015 - All Rights Reserved
//// It is strictly forbidden to copy, modify, distribute or use this code without written permission
//// To this file the Braintribe License Agreement applies.
//// ============================================================================
//
// package com.braintribe.utils.junit.core.rules;
//
// import java.util.concurrent.CountDownLatch;
//
// import org.junit.Before;
// import org.junit.rules.MethodRule;
// import org.junit.runners.model.FrameworkMethod;
// import org.junit.runners.model.Statement;
//
/// **
// * Similar to {@link LoopRule}, but the test is run concurrently. Source mostly copied from
// * <a href="http://blog.mycila.com/2009/11/writing-your-own-junit-extensions-using.html">this</a>.
// *
// * If the evaluation of the underlying {@link Statement} ends with an exception for any of the threads, this exception
// * is then re-thrown after all threads are finished. If more threads end with an exception, the rule tries to re-throw
// * the first exception that occurred (since that may the most helpful one for finding the bug), but there is no
// * guarantee of that. All unexpected exceptions should be printed to standard error output, so this is not such a big
// * deal anyway.
// *
// * Note that one may use the {@link org.junit.Test#expected()} parameter only iff all threads are expected to throw a
// * given exception. This exception would be processed inside the {@linkplain Statement} evaluation (i.e. inside the
// * method {@link Statement#evaluate()} and would therefore not be re-thrown. However, if some of the threads did not
// * throw given exception, {@linkplain Statement} evaluation would end with {@link AssertionError} which would then be
// * re-thrown, causing the test to fail.
// *
// * Note regarding {@link Before} annotation: This is executed by each thread, so beware of that. UPDATE: Recently I
// read
// * somewhere, that the behavior of this annotation is different for higher versions of junit (4.7+), so if someone
// wants
// * to update to a newer version of junit, it would also be good to create a new version of JUnitTools.
// */
// public class ConcurrentRule implements MethodRule {
// private final int threadsCount;
//
// public ConcurrentRule(final int threadsCount) {
// if (threadsCount < 1) {
// throw new IllegalArgumentException("Number of threads must be a positive number!");
// }
//
// this.threadsCount = threadsCount;
// }
//
// @Override
// public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
// return new Statement() {
//
// private volatile Throwable throwable = null;
//
// @Override
// public void evaluate() throws Throwable {
// final int nThreads = getThreadsCount();
//
// final String name = method.getName();
// final Thread[] threads = new Thread[nThreads];
// final CountDownLatch go = new CountDownLatch(1);
//
// for (int i = 0; i < nThreads; i++) {
// threads[i] = new Thread(new Runnable() {
// @Override
// public void run() {
// try {
// go.await();
// base.evaluate();
//
// } catch (final InterruptedException e) {
// Thread.currentThread().interrupt();
//
// } catch (final Throwable t) {
// // MLA: suppress unqualified access (not sure how to fix it in this special case)
// /*
// * PGA @MLA: Just curious, why did you add the t2 variable?
// *
// * Also, 'unqualified access'? I think your settings are way too strict... BTW, you
// * could fix this by changing the more outer class (Statement) into a "Local Class" (by
// * giving it a name), but that would make the code much more ugly/confusing IMHO.
// */
// Throwable t2 = throwable;
// if (t2 == null) {
// notifyException(t);
// }
//
// rethrow(t);
// }
// }
//
// private void rethrow(final Throwable t) {
// if (t instanceof RuntimeException) {
// throw (RuntimeException) t;
// }
//
// if (t instanceof Error) {
// throw (Error) t;
// }
//
// final RuntimeException r = new RuntimeException(t.getMessage(), t);
// r.setStackTrace(t.getStackTrace());
// throw r;
// }
//
// }, name + "_Thread_" + i);
// threads[i].start();
// }
//
// go.countDown();
//
// for (final Thread t : threads) {
// t.join();
// }
//
// if (this.throwable != null) {
// throw this.throwable;
// }
// }
//
// private synchronized void notifyException(final Throwable t) {
// if (this.throwable == null) {
// this.throwable = t;
// }
// }
//
// private int getThreadsCount() {
// final Concurrent concurrent = method.getAnnotation(Concurrent.class);
//
// final int nThreads = concurrent == null ? ConcurrentRule.this.threadsCount : concurrent.value();
//
// if (nThreads < 1) {
// throw new IllegalArgumentException("Number of threads must be a positive number!");
// }
//
// return nThreads;
// }
//
// };
//
// }
//
// }
