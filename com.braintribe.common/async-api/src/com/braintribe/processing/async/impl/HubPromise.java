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
package com.braintribe.processing.async.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.braintribe.exception.CanceledException;
import com.braintribe.exception.Exceptions;
import com.braintribe.processing.async.api.AsyncCallback;
import com.braintribe.processing.async.api.Promise;
import com.braintribe.processing.async.api.PromiseState;

/**
 * The HubPromise is a hub in that sense that it is both an {@link AsyncCallback} as well as a {@link Promise} and therefore can link two threads independently.
 * Additionally to the {@link AsyncCallback} interface the HubPromise also serves as a {@link Consumer} to support standard Java functional interfaces.
 * The feeder of the {@link AsyncCallback} can be in one thread and the listener again in form of an {@link AsyncCallback} in another thread.
 * 
 * HubPromises uses states and monitors to be thread-safe and serve eager or late listenings.
 * 
 * @author Dirk Scheffler
 */
public class HubPromise<E> implements AsyncCallback<E>, Promise<E>, Consumer<E> {
	private volatile E result;
	private volatile Throwable error;

	private volatile PromiseState state;

	private final List<AsyncCallback<? super E>> waiters = new ArrayList<>();
    private Runnable interrupter;
    
	public HubPromise(E result) {
		this.result = result;
		this.state = PromiseState.done;
	}
	
	public HubPromise() {
		this.state = PromiseState.outstanding;
	}
	
	public void cancel() {
		synchronized (this) {
			if (state != PromiseState.outstanding)
				throw new IllegalStateException("future was already done");
			
			state = PromiseState.canceled;
		}
		
		notifyCallbacks(PromiseState.done);
		
		if (interrupter != null) interrupter.run();
	}

	@Override
	public E get() {
		switch (waitFor()) {
			case canceled: throw CanceledException.emptyInstance;
			case done: return result;
			case failed: throw Exceptions.unchecked(error, "error in promise");
			default:
				throw new IllegalStateException("unkown state  " + state);
		}
	}
	
	@Override
	public PromiseState waitFor() {
		return waitFor(0, null);
	}
	
	@Override
	public PromiseState waitFor(long timeout, TimeUnit unit) {
		if (state == PromiseState.outstanding) {
			MonitorCallback monitor = new MonitorCallback();
			
			get(monitor);
			
			synchronized (monitor) {
				if (state == PromiseState.outstanding) {
					long ms = timeout <= 0? 0:unit.toMillis(timeout);
					try {
						monitor.wait(ms);
					} catch (InterruptedException e) {
						// noop
					}
				}
			}
		}
		
		return state;
	}
	
	private static class MonitorCallback implements AsyncCallback<Object> {

		public MonitorCallback() {
			super();
		}

		@Override
		public synchronized void onSuccess(Object future) {
			notify();
		}

		@Override
		public synchronized void onFailure(Throwable t) {
			notify();
		}
		
	}
	
	@Override
	public boolean isCanceled() {
		return state == PromiseState.canceled;
	}
	
	@Override
	public boolean isDone() {
		return state != PromiseState.outstanding;
	}
	
	@Override
	public boolean isOutstanding() {
		return state == PromiseState.outstanding;
	}
	
	@Override
	public boolean isFailed() {
		return state == PromiseState.failed;
	}
	
    @Override
	public PromiseState getState() {
        return state;
    }
	
	public E getResult() {
		return result;
	}
	
	@Override
	public Throwable getError() {
		waitFor();
		return error;
	}
	
	@Override
	public void get(AsyncCallback<? super E> l) {
		boolean ready= false;
		
		synchronized (this) {
			if (!isOutstanding())
				ready = true;
			else
				addCallback(l);
		}
		
		if (ready) {
			notifyCallback(getState(), l);
		}
	}
	
	
    @Override
	public void remove(AsyncCallback<? super E> l) {
        synchronized (waiters) {
            waiters.remove(l);
        }
    }
    
    private void addCallback(AsyncCallback<? super E> l) {
    	synchronized (waiters) {
    		waiters.add(l);
    	}
    }
	
	private void notifyCallbacks(final PromiseState state) {
		AsyncCallback<? super E>[] waitersCopy = null;
		
		synchronized (waiters) {
			if (waiters==null || waiters.size()==0) return;
			
			waitersCopy = (AsyncCallback<? super E>[]) waiters.toArray(new AsyncCallback<?>[waiters.size()]);
		}
        
        for (final AsyncCallback<? super E> waiter:waitersCopy) {
            notifyCallback(state, waiter);
        }
	}

	private void notifyCallback(PromiseState state, AsyncCallback<? super E> waiter) {
		switch (state) {
		case done:
			waiter.onSuccess(getResult());
			break;

		case failed:
			waiter.onFailure(getError());
			break;

		case canceled:
			waiter.onFailure(CanceledException.emptyInstance);
			break;

		default:
			break;
		}
	}
    
	public void setInterrupter(Runnable interupt) {
		this.interrupter = interupt;
	}
	
	@Override
	public void onSuccess(E result) {
		synchronized (this) {
			if (state == PromiseState.canceled)
				return;

			if (state != PromiseState.outstanding)
				throw new IllegalStateException("future was already notified");
			
			this.result = result;
			this.state = PromiseState.done;
		}
		notifyCallbacks(PromiseState.done);
	}
	
	@Override
	public void onFailure(Throwable t) {
		synchronized (this) {
			if (state == PromiseState.canceled)
				return;
			
			if (state != PromiseState.outstanding)
				throw new IllegalStateException("future was already notified");
			
			this.error = t;
			state = PromiseState.failed;
		}
		notifyCallbacks(PromiseState.failed);
	}

	@Override
	public void accept(E result) {
		onSuccess(result);
	}

}
