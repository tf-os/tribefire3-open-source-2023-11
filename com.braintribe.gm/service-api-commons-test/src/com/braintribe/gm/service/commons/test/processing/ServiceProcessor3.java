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
package com.braintribe.gm.service.commons.test.processing;

import java.io.UncheckedIOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.jar.JarException;

import com.braintribe.gm.service.commons.test.model.ServiceRequest3;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class ServiceProcessor3<P extends ServiceRequest3, R extends Number> extends AuthorizedServiceProcessorBase implements ServiceProcessor<P, R> {

	public static final Integer RETURN = ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static final Class<? extends Exception> EXCEPTION_TYPE = UncheckedIOException.class;
	
	@SuppressWarnings("unchecked")
	@Override
	public R process(ServiceRequestContext context, P parameter) throws UncheckedIOException {
		if (parameter.getForceException()) {
			throw new UncheckedIOException(new JarException("Enforced business exception for: " + parameter));
		}
		validate(context, parameter);
		return (R) RETURN;
	}

}