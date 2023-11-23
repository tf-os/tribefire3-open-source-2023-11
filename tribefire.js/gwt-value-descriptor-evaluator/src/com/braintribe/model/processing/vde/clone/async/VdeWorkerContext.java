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
package com.braintribe.model.processing.vde.clone.async;

import java.util.function.Consumer;

import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * @author peter.gazdik
 */
/* package */ interface WorkerContext {

	int MAX_NUMBER_OF_OPS = 1000;

	void submit(Runnable task);
	int allowedNumberOfOps();
	void notifyNumberOfOps(int numberOfOps);

	default <T> AsyncCallback<T> submittingCallbackOf(Consumer<? super T> onSuccess, Consumer<Throwable> onFailure) {
		return AsyncCallback.of( //
				future -> submit(() -> onSuccess.accept(future)), //
				t -> submit(() -> onFailure.accept(t)));
	}

	default <T> AsyncCallback<T> submittingCallbackOf(AsyncCallback<? super T> callback) {
		return AsyncCallback.of( //
				future -> submit(() -> callback.onSuccess(future)), //
				t -> submit(() -> callback.onFailure(t)));
	}
}

/* package */ interface VdeWorkerContext extends WorkerContext {
	int MAX_NUMBER_OF_OPS = 1000;

	boolean evaluateVds();
	VdeWorkerContext nonVdEvaluatingContext();
}

/* package */ class VdYesWorkerContext<R> implements VdeWorkerContext, AsyncCallback<R> {
	private final AsyncCallback<? super R> callback;
	private final DeferredExecutor executor;

	private final int maxNumberOfOps;
	// pointer to the last job and also an indicator if there are jobs being processed - if null, no jobs are being processed
	private Job lastJob; 
	private VdeWorkerContext nonVdEvaluatingContext;

	private int currentNumberOfOps;

	public VdYesWorkerContext(AsyncCallback<? super R> callback, DeferredExecutor executor) {
		this.callback = callback;
		this.executor = executor;
		this.maxNumberOfOps = MAX_NUMBER_OF_OPS;
	}

	@Override
	public boolean evaluateVds() {
		return true;
	}

	@Override
	public int allowedNumberOfOps() {
		return Math.max(maxNumberOfOps - currentNumberOfOps, 0);
	}

	@Override
	public void notifyNumberOfOps(int numberOfOps) {
		currentNumberOfOps += numberOfOps;
	}

	@Override
	public VdeWorkerContext nonVdEvaluatingContext() {
		if (nonVdEvaluatingContext == null)
			nonVdEvaluatingContext = new VdNoWorkerContext(this);
		return nonVdEvaluatingContext;
	}

	@Override
	public void onSuccess(R future) {
		callback.onSuccess(future);
	}

	@Override
	public void onFailure(Throwable t) {
		callback.onFailure(t);
	}

	@Override
	public void submit(Runnable task) {
		Job job = new Job();
		job.task = task;

		boolean isWorking = lastJob != null;
		if (isWorking)
			lastJob.nextJob = job;

		lastJob = job;

		if (!isWorking)
			doAsync(job);
	}

	class Job {
		Runnable task;
		Job nextJob;

		public void run() {
			try {
				task.run();
			} catch (Throwable e) {
				callback.onFailure(e);
				nextJob = null;
			}
		}
	}

	private void doAsync(Job job) {
		executor.execute(() -> work(job));
	}

	private void work(Job job) {
		currentNumberOfOps = 0;
		while (job != null) {
			if (allowedNumberOfOps() == 0) {
				doAsync(job);
				return;
			}

			currentNumberOfOps++;
			job.run();
			job = job.nextJob;
		}
		// mark that no jobs are being processed so the next submit starts another working loop
		lastJob = null;
	}

}

/* package */ class VdNoWorkerContext implements VdeWorkerContext {

	private final VdeWorkerContext delegate;

	public VdNoWorkerContext(VdeWorkerContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean evaluateVds() {
		return false;
	}

	@Override
	public VdeWorkerContext nonVdEvaluatingContext() {
		return this;
	}

	@Override
	public void submit(Runnable task) {
		delegate.submit(task);
	}

	@Override
	public int allowedNumberOfOps() {
		return delegate.allowedNumberOfOps();
	}

	@Override
	public void notifyNumberOfOps(int numberOfOps) {
		delegate.notifyNumberOfOps(numberOfOps);
	}
}
