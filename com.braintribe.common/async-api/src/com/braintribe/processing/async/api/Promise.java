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
package com.braintribe.processing.async.api;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.braintribe.exception.CanceledException;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * The Promise acts a future value coming from another thread and overcomes the problems of the java.util.concurrent.Future which cannot be a {@link Supplier}
 * for its declared checked exceptions and also does not support asynchronous notification.
 * 
 * @author Dirk Scheffler
 */
@JsType(namespace = "$tf.async")
public interface Promise<T> extends Supplier<T> {
    /**
     * Returns {@code true} if this promise was canceled before it completed
     * normally which is equivalent to {@link #getState()} == {@link PromiseState#canceled}.
     *
     * In that case: 
     * 
     * Neither {@link #get()} nor {@link #getError()} will return meaning full results.
     * 
     * {@link #waitFor()} and {@link #waitFor(long, TimeUnit)} will be non blocking and return the state.
     */
    boolean isCanceled();

    /**
     * Returns {@code true} if this promise was completed successfully 
     * which is equivalent to {@link #getState()} == {@link PromiseState#done}.
     *
     * In that case:
     * 
     * {@link #get()} will be non blocking and return a meaning full result. 
     * 
     * {@link #waitFor()} and {@link #waitFor(long, TimeUnit)} will be non blocking and return the state.
     */
    boolean isDone();
    
    /**
     * Returns {@code true} if this promised was marked as failed with an exception 
     * which is equivalent to {@link #getState()} == {@link PromiseState#failed}.
     *
     * In that case:
     * 
     * {@link #get()} will be non blocking and throw the according exception directly 
     * if it was a non checked exception otherwise it will throw it wrapped in a {@link RuntimeException}.
     * 
     * {@link #getError()} will be non blocking and return the according exception directly.
     * 
     * {@link #waitFor()} and {@link #waitFor(long, TimeUnit)} will be non blocking and return the state.
     *  
     */
    boolean isFailed();
    
    /**
     * Returns {@code true} if this promised is still outstanding which is equivalent to {@link #getState()} == {@link PromiseState#failed}.
     * 
     * In that case 
     * 
     * {@link #get()}, {@link #getError()} and {@link #waitFor()} will block until the state changes from {@link PromiseState#outstanding} to something else
     * and then answer according to the achieved state.
     *
     */
    boolean isOutstanding();
    
    /**
     * Returns the current state of the promise in a non blocking way. The state can be:
     * 
     * <ul>
     * 	<li>{@link PromiseState#outstanding} -> {@link #isOutstanding()}</li>
     * 	<li>{@link PromiseState#done} -> {@link #isDone()}</li>
     * 	<li>{@link PromiseState#failed} -> {@link #isFailed()}</li>
     * 	<li>{@link PromiseState#canceled} -> {@link #isCanceled()}</li>
     * </ul>
     */
    PromiseState getState();
    
    /**
     * Subscribe an {@link AsyncCallback} to be notified about that the state of the Promise has change from {@link PromiseState#outstanding} to something else. If the
     * state in the moment of this has already left {@link PromiseState#outstanding} the callback will be immediately notified in a synchronous way. 
     * 
     * Depending on which state is or was reached the following method will be called:
     * 
     * <ul>
     * 	<li>{@link PromiseState#done} -> {@link AsyncCallback#onSuccess(Object)} with the result of the Promise</li>
     * 	<li>{@link PromiseState#failed} -> {@link AsyncCallback#onFailure(Throwable)} with the according exception</li>
     * 	<li>{@link PromiseState#canceled} -> {@link AsyncCallback#onFailure(Throwable)} with a {@link CanceledException#emptyInstance}</li>
     * </ul>
     */
	void get(AsyncCallback<? super T> l);
	
	/**
	 * Unsubscribe an {@link AsyncCallback} from the state notification.
	 */
	void remove(AsyncCallback<? super T> l);
	
	/**
	 * Blocks until the state of the Promise left {@link PromiseState#outstanding} and returns the achieved state.
	 * Therefore the returned state can be either of the one:
     * <ul>
     * 	<li>{@link PromiseState#done}</li>
     * 	<li>{@link PromiseState#failed}</li>
     * 	<li>{@link PromiseState#canceled}</li>
     * </ul>
	 */
	PromiseState waitFor();
	
	/**
	 * Blocks until the state of the Promise left {@link PromiseState#outstanding} or the given timeout was reached and returns the state.
	 * The returned state can be any of the {@link PromiseState} constants and in case the timeout was reached the state will be {@link PromiseState#outstanding}
	 */
	@JsIgnore
	PromiseState waitFor(long timeout, TimeUnit unit);
	
	/**
	 * Blocks until the state of the Promise left {@link PromiseState#outstanding} and answers depending on the achieved state:
	 * 
     * <ul>
     * 	<li>{@link PromiseState#done} -> returns the result of the Promise</li>
     * 	<li>{@link PromiseState#failed} -> throws the exception if being unchecked or throws the exception wrapped in a {@link RuntimeException}</li>
     * 	<li>{@link PromiseState#canceled} -> throws {@link CanceledException#emptyInstance}</li>
     * </ul>
	 */
	@Override
	T get();
	
	/**
	 * Blocks until the state of the Promise left {@link PromiseState#outstanding} and returns the exception if the reached state was {@link PromiseState#failed}
	 * otherwise null.
	 */
	Throwable getError();
}
