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
package tribefire.extension.cache.service;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

import tribefire.extension.cache.model.service.demo.CacheDemo;
import tribefire.extension.cache.model.service.demo.CacheDemoResult;

public class CacheDemoProcessor implements ServiceProcessor<CacheDemo, CacheDemoResult> {

	private static final Logger logger = Logger.getLogger(CacheDemoProcessor.class);

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public CacheDemoResult process(ServiceRequestContext requestContext, CacheDemo request) {
		logger.info(() -> "Executing '" + this.getClass().getSimpleName() + "' - " + request.type().getTypeName());

		CacheDemoResult result = CacheDemoResult.T.create();

		long durationInMs = request.getDurationInMs();
		boolean throwException = request.getThrowException();

		try {
			Thread.sleep(durationInMs);
		} catch (InterruptedException e) {
			throw Exceptions.unchecked(e, "Interrupted: '" + CacheDemoProcessor.class.getSimpleName() + "'");
		}

		if (throwException) {
			throw new RuntimeException("'" + CacheDemoProcessor.class.getSimpleName() + "' dummy exception");
		}

		String resultValue = request.getResultValue();
		if (resultValue != null) {
			result.setResultValue(resultValue);
		}

		return result;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

}
