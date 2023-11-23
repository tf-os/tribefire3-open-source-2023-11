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
package com.braintribe.model.processing.vde.evaluator.api;

import com.braintribe.processing.async.api.AsyncCallback;

/**
 * This is the context that handles all the VDE related actions.
 * 
 * It provides evaluation methods and aspect adjustment methods.
 * 
 */
public interface VdeContext {

	/**
	 * Evaluation method for value descriptor, invokes @
	 * {@link #evaluate(Object, boolean) } with a false
	 * 
	 * @param object
	 *            The value descriptor that will be evaluated.
	 * @return The evaluated object
	 * 
	 */
	<T> T evaluate(Object object) throws VdeRuntimeException;
	
	/**
	 * Evaluation method for value descriptor, invokes @
	 * {@link #evaluate(Object, boolean) } with a false
	 * 
	 * @param object
	 *            The value descriptor that will be evaluated.
	 * @param callback the callback to which either the result or an exception is being asynchronously notified
	 */
	<T> void evaluate(Object object, AsyncCallback<T> callback);

	/**
	 * The evaluation method which evaluates a value descriptor and stores the
	 * result in a cache when non-volatile. If a non value descriptor is
	 * provided it will be returned as is.
	 * 
	 * @param object
	 *            The value descriptor that will be evaluated.
	 * @param volatileEvaluation
	 *            boolean that indicates if this evaluation is volatile
	 * @param callback the callback to which either the result or an exception is being asynchronously notified
	 */
	<T> void evaluate(Object object, boolean volatileEvaluation, AsyncCallback<T> callback);
	/**
	 * The evaluation method which evaluates a value descriptor and stores the
	 * result in a cache when non-volatile. If a non value descriptor is
	 * provided it will be returned as is.
	 * 
	 * @param object
	 *            The value descriptor that will be evaluated.
	 * @param volatileEvaluation
	 *            boolean that indicates if this evaluation is volatile
	 * @return The evaluated object
	 */
	<T> T evaluate(Object object, boolean volatileEvaluation) throws VdeRuntimeException;

	/**
	 * Retrieves a value for given aspect. The value may be <tt>null</tt>.
	 * 
	 * @param aspect
	 *            The aspect itself.
	 * @return The value associated with the aspect
	 */
	<T, A extends VdeContextAspect<T>> T get(Class<A> aspect);

	/**
	 * Adds a value for an {@link VdeContextAspect}
	 */
	<T, A extends VdeContextAspect<? super T>> void put(Class<A> aspect, T value);

	void setVdeRegistry(VdeRegistry registry);

	VdeRegistry getVdeRegistry();

	void setEvaluationMode(VdeEvaluationMode mode);

	VdeEvaluationMode getEvaluationMode();
}
