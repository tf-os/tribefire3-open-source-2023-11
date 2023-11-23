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

import com.braintribe.model.resource.Resource;

/**
 * URL builder for upload and download of a {@link Resource}.
 */
public interface ResourceUrlBuilder {

	ResourceUrlBuilder download(boolean download);

	ResourceUrlBuilder fileName(String fileName);

	ResourceUrlBuilder accessId(String accessId);

	ResourceUrlBuilder sessionId(String sessionId);

	ResourceUrlBuilder sourceType(String sourceTypeSignature);

	ResourceUrlBuilder useCase(String useCase);

	ResourceUrlBuilder mimeType(String mimeType);

	ResourceUrlBuilder md5(String md5);

	ResourceUrlBuilder creator(String creator);

	ResourceUrlBuilder tags(String tags);

	ResourceUrlBuilder specification(String specification);

	/** Determines the base URL. */
	ResourceUrlBuilder base(String baseUrl);

	String asString();

	/**
	 * @deprecated Use {@link #download(boolean)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder forDownload(boolean download) {
		return download(download);
	}

	/**
	 * @deprecated Use {@link #fileName(String)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder withDownloadName(String downloadName) {
		return fileName(downloadName);
	}

	/**
	 * @deprecated Use {@link #fileName(String)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder withFileName(String fileName) {
		return fileName(fileName);
	}

	/**
	 * @deprecated Use {@link #accessId(String)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder forAccess(String accessId) {
		return accessId(accessId);
	}

	/**
	 * @deprecated Use {@link #sessionId(String)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder withSessionId(String sessionId) {
		return sessionId(sessionId);
	}

	/**
	 * @deprecated Use {@link #sourceType(String)} instead. This method will be removed in a future release.
	 */
	@Deprecated
	default ResourceUrlBuilder withSourceType(String sourceTypeSignature) {
		return sourceType(sourceTypeSignature);
	}

}
