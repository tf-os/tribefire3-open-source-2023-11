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
package com.braintribe.spring.support.converter;

import java.security.PrivateKey;
import java.util.function.Supplier;

import org.springframework.core.convert.converter.Converter;



public class PrivateKeyProviderConverter implements Converter<Supplier<PrivateKey>, PrivateKey> {

	@Override
	public PrivateKey convert(Supplier<PrivateKey> source) {
		try {
			return source.get();
		} catch (RuntimeException e) {
			throw new IllegalArgumentException( e);
		}
	}
	
	

}
