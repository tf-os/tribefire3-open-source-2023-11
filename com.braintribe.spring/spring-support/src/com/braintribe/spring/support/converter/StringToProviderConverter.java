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

import java.util.function.Supplier;

import org.springframework.core.convert.converter.Converter;

import com.braintribe.provider.Holder;

/**
 * A Spring {@link Converter} that converts a String to a {@link Supplier} of the same String.
 * 
 * 
 */
public class StringToProviderConverter implements Converter<String, Supplier<String>> {

	@Override
	public Supplier<String> convert(final String source) {
		if (source == null) {
			return null;
		}

		final Holder<String> holder = new Holder<String>();
		holder.accept(source);
		return holder;
	}
}
