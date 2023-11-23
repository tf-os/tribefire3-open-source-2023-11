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
package com.braintribe.model.processing.session.api.resource;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

@JsType(namespace=GmCoreApiInteropNamespaces.resources)
@SuppressWarnings("unusable-by-js")
public interface ResourceUrlBuilder {

	/**
	 * <p>
	 * Determines the {@code download} parameter value.
	 */
	ResourceUrlBuilder download(boolean download);

	/**
	 * <p>
	 * Determines the {@code fileName} parameter value which can be used in both download and upload URLs.
	 */
	ResourceUrlBuilder fileName(String fileName);

	/**
	 * <p>
	 * Determines the {@code accessId} parameter value.
	 */
	ResourceUrlBuilder accessId(String accessId);

	/**
	 * <p>
	 * Determines the {@code sessionId} parameter value.
	 */
	ResourceUrlBuilder sessionId(String sessionId);

	/**
	 * <p>
	 * Determines the {@code entityType} of the ResourceSource.
	 */
	ResourceUrlBuilder sourceType(String sourceTypeSignature);

	/**
	 * <p>
	 * Determines the {@code useCase} parameter value.
	 */
	ResourceUrlBuilder useCase(String useCase);

	/**
	 * <p>
	 * Determines the {@code mimeType} parameter value.
	 */
	ResourceUrlBuilder mimeType(String mimeType);

	/**
	 * <p>
	 * Determines the {@code md5} parameter value.
	 */
	ResourceUrlBuilder md5(String md5);

	/**
	 * <p>
	 * Determines the {@code tags} parameter value.
	 */
	ResourceUrlBuilder tags(String tags);
	
	/**
	 * <p>
	 * Determines the {@code specification} parameter value.
	 */
	ResourceUrlBuilder specification(String specification);

	/**
	 * <p>
	 * Determines the base URL.
	 */
	ResourceUrlBuilder base(String baseUrl);

	String asString();

}
