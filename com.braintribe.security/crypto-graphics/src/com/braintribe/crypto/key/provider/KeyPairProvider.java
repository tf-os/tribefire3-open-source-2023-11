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
package com.braintribe.crypto.key.provider;

import java.security.KeyPair;
import java.util.function.Supplier;

import com.braintribe.crypto.CryptoServiceException;
import com.braintribe.crypto.key.AsymmetricKeyGenerator;


/**
 * A {@link Supplier} compliant generator of {@link KeyPair}(s).
 * 
 */
public class KeyPairProvider implements Supplier<KeyPair> {
	
	private int length;
	private String generatorAlgo;
	private String randomAlgo;
	private String randomProvider;
	
	public void setLength(int length) {
		this.length = length;
	}

	public void setGeneratorAlgo(String generatorAlgo) {
		this.generatorAlgo = generatorAlgo;
	}

	public void setRandomAlgo(String randomAlgo) {
		this.randomAlgo = randomAlgo;
	}
	
	public void setRandomProvider(String randomProvider) {
		this.randomProvider = randomProvider;
	}
	

	@Override
	public KeyPair get() throws RuntimeException {
		
		if (length == 0) {
			throw new RuntimeException("No length was configured to this provider.");
		}
		
		try {
			AsymmetricKeyGenerator keyGen = null;
			
			if (this.generatorAlgo != null && this.randomAlgo != null && this.randomProvider != null) {
				keyGen = new AsymmetricKeyGenerator(generatorAlgo, randomAlgo, randomProvider, length);
			} else {
				keyGen = new AsymmetricKeyGenerator(length);
			}
			
			keyGen.generateKeyPair();
			
			return keyGen.getPair();
			
		} catch (CryptoServiceException e) {
			throw new RuntimeException("Failed to generate key pair: "+e.getMessage(), e);
		}
	}
	
}
