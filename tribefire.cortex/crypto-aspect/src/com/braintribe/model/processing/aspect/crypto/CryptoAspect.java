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
package com.braintribe.model.processing.aspect.crypto;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.crypto.Cryptor;
import com.braintribe.logging.Logger;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessAspectRuntimeException;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aspect.crypto.interceptor.CryptoInterceptorConfiguration;
import com.braintribe.model.processing.aspect.crypto.interceptor.manipulation.CryptoManipulationInterceptor;
import com.braintribe.model.processing.crypto.provider.CryptorProvider;

/**
 * <p>
 * An {@link AccessAspect} which provides encryption and hashing capabilities to access(es).
 * 
 */
public class CryptoAspect implements AccessAspect {

	private static final Logger log = Logger.getLogger(CryptoAspect.class);

	private CryptorProvider<Cryptor, PropertyCrypting> cryptorProvider;
	private boolean cacheCryptorsPerContext = true;

	@Required
	@Configurable
	public void setCryptorProvider(CryptorProvider<Cryptor, PropertyCrypting> cryptorProvider) {
		this.cryptorProvider = cryptorProvider;
	}

	@Configurable
	public void setCacheCryptorsPerContext(boolean cacheCryptorsPerContext) {
		this.cacheCryptorsPerContext = cacheCryptorsPerContext;
	}

	@Override
	public void configurePointCuts(PointCutConfigurationContext context) throws AccessAspectRuntimeException {

		if (log.isTraceEnabled()) {
			log.trace("Configuring " + this.getClass().getSimpleName() + " point cuts");
		}

		try {

			CryptoInterceptorConfiguration cryptoInterceptorConfiguration = new CryptoInterceptorConfiguration();
			cryptoInterceptorConfiguration.setCryptorProvider(cryptorProvider);
			cryptoInterceptorConfiguration.setCacheCryptorsPerContext(cacheCryptorsPerContext);

			context.addPointCutBinding(AccessJoinPoint.applyManipulation, new CryptoManipulationInterceptor(cryptoInterceptorConfiguration));

			if (log.isDebugEnabled()) {
				log.debug("Configured " + this.getClass().getSimpleName() + " point cuts");
			}

		} catch (Exception e) {
			throw new AccessAspectRuntimeException("Failed to configure " + this.getClass().getSimpleName() + " point cuts" + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

	}

}
