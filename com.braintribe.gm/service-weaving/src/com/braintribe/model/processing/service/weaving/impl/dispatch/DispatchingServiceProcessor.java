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
package com.braintribe.model.processing.service.weaving.impl.dispatch;
import java.util.IdentityHashMap;
import java.util.Objects;

import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * DispatchingServiceProcessor is a convenience and performance optimizing class for the dispatching of polymorphic
 * types deriving from {@link ServiceRequest} to concrete handler methods. 
 * </p>
 * <p>
 * It uses byte code weaving in order to create the fastest possible way
 * to call the actual handler methods being found in any derivate of itself or in the passed service interface class.
 * </p>
 * <p>
 * The class is threadsafe and uses synchronization only where absolutely neccessary in order to make dispatching very
 * straight forward and fast. As in internal mapping principle the {@link IdentityHashMap} is used for the fastest possible dynamic mapping.
 * </p>
 * <p>
 * Handler methods have to follow the following method signature:<br/><br/>
 * 
 * <code>
 * ReturnType yourMethodName({@link ServiceRequestContext} requestContext, CustomRequest request) throws {@link ServiceProcessorException}
 * </code>
 * </p>
 * <p>
 * Handler methods will handle sub types of the request type from the signature if no better concrete method could do so. If a request type
 * is not directly or polymorphically handled by some method a {@link UnsupportedRequestTypeException} will be thrown 
 * when {@link #process(ServiceRequestContext, ServiceRequest)} is being called. 
 * </p>
 * 
 * 
 * @author dirk.scheffler
 * @deprecated because of the obscure magic implicit dispatch mapping this solution is considered problematic. 
 * Use {@link AbstractDispatchingServiceProcessor} instead  
 */
@Deprecated
public class DispatchingServiceProcessor<P extends ServiceRequest, R> implements ServiceProcessor<P, R> {

	private DispatchMap dispatchMap;
	private Object delegate;
	
	/**
	 * Default constructor for subclasses which assumes that the DispatchingServiceProcessor itself is the holder
	 * of the actual handler methods.
	 * 
	 * Its usage is not encouraged as we want to enforce specification interfaces for dispatching 
	 * and therefore do not longer allow for class based method definitions
	 */
	protected DispatchingServiceProcessor() {
		init(getClass().asSubclass(Object.class), this);
	}

	/**
	 * Constructor for combinatoric usage or for subclasses. 
	 * @param interfaceClass a specific class or interface that lists the methods to be considered as handler methods as long as they fit the signature pattern
	 * @param delegate the instance fitting to the interfaceClass that actually can handle the calls
	 */
	public <I> DispatchingServiceProcessor(Class<I> interfaceClass, I delegate) {
		if (!interfaceClass.isInterface())
			throw new IllegalArgumentException("interfaceClass must be an interface");

		init(interfaceClass, delegate);
	}
	
	/**
	 * Constructor for combinatoric usage or for subclasses. 
	 * @param interfaceClass a specific class or interface that lists the methods to be considered as handler methods as long as they fit the signature pattern
	 */
	protected DispatchingServiceProcessor(Class<?> interfaceClass) {
		// strange cast is needed because a type does not have an automatic generic type parameter of itself
		init((Class<Object>)interfaceClass, this);
	}
	
	private <I> void init(Class<I> interfaceClass, I delegate) {
		Objects.requireNonNull(delegate, "delegate must not be null");
		
		this.delegate = delegate;
		dispatchMap = DispatchMap.mapFor(interfaceClass);
	}
	
	/**
	 * dispatches the request handling to a fitting handler method which may handle a super class of the request class or directly this class.
	 */
	@Override
	public R process(ServiceRequestContext requestContext, P request) throws ServiceProcessorException {
		@SuppressWarnings("unchecked")
		R result = (R)dispatchMap.acquireHandler(request.entityType()).process(delegate, requestContext, request);
		return result;
	}
}
