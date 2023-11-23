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
package com.braintribe.model.processing.resource.streaming.cache;

import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.resource.source.ConversionServiceSource;
import com.braintribe.model.resource.source.ResourceSource;


public class CacheKeyProvider implements Function<ResourceSource, String> {
	/**
	 * The logger.
	 */
	protected static Logger logger = Logger.getLogger(CacheKeyProvider.class);

	private Function<ConversionServiceSource, String> conversionServiceCacheKeyProvider = new ConversionServiceCacheKeyProvider();

	// **************************************************************************
	// Constructor
	// **************************************************************************

	/**
	 * Default constructor
	 */
	public CacheKeyProvider() {
	}

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************

	public void setConversionServiceCacheKeyProvider(
			Function<ConversionServiceSource, String> conversionServiceCacheKeyProvider) {
		this.conversionServiceCacheKeyProvider = conversionServiceCacheKeyProvider;
	}

	// **************************************************************************
	// Interface implementations
	// **************************************************************************

	/**
	 * @see Function#apply(java.lang.Object)
	 */
	@Override
	public String apply(ResourceSource source) throws RuntimeException {
		if (source instanceof ConversionServiceSource && conversionServiceCacheKeyProvider != null) {
			ConversionServiceSource conversionSource = (ConversionServiceSource) source;
			return conversionServiceCacheKeyProvider.apply(conversionSource);
		} else {
			return source.getId();
		}
	}

	public static String getBuildVersion() {
		return "$Build_Version$ $Id: CacheKeyProvider.java 102880 2018-01-18 11:36:53Z roman.kurmanowytsch $";
	}
}
