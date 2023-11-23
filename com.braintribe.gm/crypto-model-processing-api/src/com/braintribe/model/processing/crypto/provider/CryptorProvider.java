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
package com.braintribe.model.processing.crypto.provider;

import com.braintribe.crypto.Cryptor;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;

/**
 * <p>
 * Provides {@link Cryptor} instances based on {@link PropertyCrypting} instances.
 * 
 *
 * @param <O> The common super-type of the {@link Cryptor} to be provided.
 * @param <I> The {@link PropertyCrypting} for which relevant {@link Cryptor} must be provided
 */
public interface CryptorProvider<O extends Cryptor, I extends PropertyCrypting> {

	O provideFor(I propertyCrypting) throws CryptorProviderException;

	<R extends O> R provideFor(Class<R> cryptorType, I propertyCrypting) throws CryptorProviderException;
	
}
