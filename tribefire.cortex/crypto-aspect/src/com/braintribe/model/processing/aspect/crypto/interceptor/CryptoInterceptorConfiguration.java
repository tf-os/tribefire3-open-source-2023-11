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
package com.braintribe.model.processing.aspect.crypto.interceptor;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.crypto.provider.CryptorProvider;

/**
 * <p>
 * A set of configurations for {@link CryptoInterceptor}(s).
 * 
 */
public class CryptoInterceptorConfiguration {

	private CryptorProvider<Cryptor, PropertyCrypting> cryptorProvider;
	private boolean cacheCryptorsPerContext = true;

	public CryptorProvider<Cryptor, PropertyCrypting> getCryptorProvider() {
		return cryptorProvider;
	}

	public void setCryptorProvider(CryptorProvider<Cryptor, PropertyCrypting> cryptorProvider) {
		this.cryptorProvider = cryptorProvider;
	}

	public boolean isCacheCryptorsPerContext() {
		return cacheCryptorsPerContext;
	}

	public void setCacheCryptorsPerContext(boolean cacheCryptorsPerContext) {
		this.cacheCryptorsPerContext = cacheCryptorsPerContext;
	}

}
