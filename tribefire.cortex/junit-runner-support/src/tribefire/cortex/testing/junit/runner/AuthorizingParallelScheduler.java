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
package tribefire.cortex.testing.junit.runner;

import org.junit.runners.model.RunnerScheduler;

import com.braintribe.thread.api.ThreadContextScoping;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

import static java.util.concurrent.ForkJoinTask.inForkJoinPool;

/**
 * Encapsulates the singleton {@link ForkJoinPool} used by {@link AuthorizingParallelRunner} to execute test classes and test methods concurrently.
 *
 * @see <a href="https://github.com/MichaelTamm/junit-toolbox/blob/master/src/main/java/com/googlecode/junittoolbox/ParallelScheduler.java">JUnit
 *      Toolbox</a>
 */
class AuthorizingParallelScheduler implements RunnerScheduler {

	protected ThreadContextScoping tcs;
    static ForkJoinPool forkJoinPool = setUpForkJoinPool();

    static ForkJoinPool setUpForkJoinPool() {
        int numThreads;
        try {
            String configuredNumThreads = System.getProperty("maxParallelTestThreads");
            numThreads = Math.max(2, Integer.parseInt(configuredNumThreads));
        } catch (Exception ignored) {
            Runtime runtime = Runtime.getRuntime();
            numThreads = Math.max(2, runtime.availableProcessors());
        }
        ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = pool -> {
            if (pool.getPoolSize() >= pool.getParallelism()) {
                return null;
            } else {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("JUnit-" + thread.getName());
                return thread;
            }
        };
        return new ForkJoinPool(numThreads, threadFactory, null, false);
    }

    private final Deque<ForkJoinTask<?>> _asyncTasks = new LinkedList<>();
    private Runnable _lastScheduledChild;

    @Override
    public void schedule(Runnable childStatement) {
        if (_lastScheduledChild != null) {
            // Execute previously scheduled child asynchronously ...
            if (inForkJoinPool()) {
                _asyncTasks.addFirst(ForkJoinTask.adapt(_lastScheduledChild).fork());
            } else {
                _asyncTasks.addFirst(forkJoinPool.submit(_lastScheduledChild));
            }
        }
        // Note: We don't schedule the childStatement immediately here,
        // but remember it, so that we can synchronously execute the
        // last scheduled child in the finished method() -- this way,
        // the current thread does not immediately call join() in the
        // finished() method, which might block it ...
        if (tcs == null)
        	_lastScheduledChild = childStatement;
        else
        	_lastScheduledChild = tcs.bindContext(childStatement);
    }

    @Override
    public void finished() {
        RuntimeException me = new RuntimeException("One or more tests could not be run. See suppressed exceptions for details.");
        if (_lastScheduledChild != null) {
            if (inForkJoinPool()) {
                // Execute the last scheduled child in the current thread ...
                try { _lastScheduledChild.run(); } catch (Throwable t) { me.addSuppressed(t); }
            } else {
                // Submit the last scheduled child to the ForkJoinPool too,
                // because all tests should run in the worker threads ...
                _asyncTasks.addFirst(forkJoinPool.submit(_lastScheduledChild));
            }
            // Make sure all asynchronously executed children are done, before we return ...
            for (ForkJoinTask<?> task : _asyncTasks) {
                // Note: Because we have added all tasks via addFirst into _asyncTasks,
                // task.join() is able to steal tasks from other worker threads,
                // if there are tasks, which have not been started yet ...
                // from other worker threads ...
                try { task.join(); } catch (Throwable t) { me.addSuppressed(t); }
            }
            
            if (me.getSuppressed().length > 0)
            	throw me;
        }
    }
}