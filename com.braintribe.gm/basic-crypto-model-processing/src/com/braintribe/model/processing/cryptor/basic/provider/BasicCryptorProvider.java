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
package com.braintribe.model.processing.cryptor.basic.provider;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.crypto.Cryptor;
import com.braintribe.logging.Logger;
import com.braintribe.model.crypto.configuration.CryptoConfiguration;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.crypto.factory.CryptorFactory;
import com.braintribe.model.processing.crypto.provider.CryptorProvider;
import com.braintribe.model.processing.crypto.provider.CryptorProviderException;

public class BasicCryptorProvider implements CryptorProvider<Cryptor, PropertyCrypting> {

	private static final Logger log = Logger.getLogger(BasicCryptorProvider.class);

	private GmExpertRegistry expertRegistry;

	/**
	 * <p>
	 * Sets the {@link GmExpertRegistry} used to fetch {@link CryptorFactory} experts based on the provided {@link PropertyCrypting} instances.
	 * 
	 * @param expertRegistry
	 *            the GmExpertRegistry to be set
	 */
	@Required
	@Configurable
	public void setExpertRegistry(GmExpertRegistry expertRegistry) {
		this.expertRegistry = expertRegistry;
	}

	@Override
	public Cryptor provideFor(PropertyCrypting propertyCrypting) throws CryptorProviderException {

		if (propertyCrypting == null) {
			throw new IllegalArgumentException(PropertyCrypting.class.getSimpleName() + " argument cannot be null");
		}

		CryptoConfiguration cryptoConfiguration = propertyCrypting.getCryptoConfiguration();

		if (cryptoConfiguration == null) {
			throw new IllegalStateException("The crypto configuration property of " + propertyCrypting.getClass().getName() + " cannot be null.");
		}

		Cryptor cryptor = provideForCryptoConfiguration(cryptoConfiguration);

		if (log.isTraceEnabled()) {
			log.trace("Providing [ " + cryptor + " ] based on given [ " + propertyCrypting + " ]");
		}

		return cryptor;
	}

	@Override
	public <R extends Cryptor> R provideFor(Class<R> cryptorType, PropertyCrypting propertyCrypting) throws CryptorProviderException {

		Cryptor cryptor = provideFor(propertyCrypting);
		
		if (cryptor == null) {
			return null;
		}
		
		if (cryptorType.isInstance(cryptor)) {
			return cryptorType.cast(cryptor);
		}
		
		if (log.isTraceEnabled()) {
			log.trace("Provided cryptor [ "+cryptor.getClass().getName()+" ] is not compatible with the required type [ "+cryptorType.getName()+" ]");
		}
		
		return null;
		
	}

	private Cryptor provideForCryptoConfiguration(CryptoConfiguration cryptoConfiguration) throws CryptorProviderException {

		CryptorFactory<CryptoConfiguration, Cryptor> cryptorFactory = null;

		try {
			cryptorFactory = expertRegistry.findExpert(CryptorFactory.class).forInstance(cryptoConfiguration);
		} catch (Exception e) {
			throw new CryptorProviderException("Failed to obtain a cryptor factory for the given configuration [" + cryptoConfiguration + "]", e);
		}

		if (cryptorFactory == null) {
			throw new CryptorProviderException("Failed to obtain a cryptor factory. Make sure the expert registry is properly configured to provide " + CryptorFactory.class.getName() + "(s) for the given denotation type: " + cryptoConfiguration);
		}

		Cryptor cryptor = null;

		try {
			cryptor = cryptorFactory.getCryptor(cryptoConfiguration);
		} catch (Exception e) {
			throw new CryptorProviderException("Failed to create a cryptor with factory " + cryptorFactory.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""), e);
		}

		if (cryptor == null) {
			throw new CryptorProviderException("Failed to create a cryptor. null was returned by the factory: " + cryptorFactory);
		}

		return cryptor;

	}

}
