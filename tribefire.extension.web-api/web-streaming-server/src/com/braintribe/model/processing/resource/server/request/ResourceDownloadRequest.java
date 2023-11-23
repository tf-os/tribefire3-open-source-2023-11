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
package com.braintribe.model.processing.resource.server.request;

import java.util.Date;

/**
 * <p>
 * Type-safe holder of request parameters handled on download operations.
 * 
 */
public class ResourceDownloadRequest extends ResourceStreamingRequest {

	private String resourceId;
	private boolean noCache;
	private boolean download;
	private String ifNoneMatch;
	private Date ifModifiedSince;

	private long rangeStart = -1L;
	private long rangeEnd = -1L;
	
	public ResourceDownloadRequest() {
	}

	public long getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(long rangeStart) {
		this.rangeStart = rangeStart;
	}

	public long getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(long rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public boolean isNoCache() {
		return noCache;
	}

	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	/**
	 * @deprecated use {@link #getFileName()} instead.
	 */
	@Deprecated
	public String getDownloadName() {
		return getFileName();
	}

	/**
	 * @deprecated use {@link #setFileName(String)} instead.
	 */
	@Deprecated
	public void setDownloadName(String downloadName) {
		setFileName(downloadName);
	}

	public String getIfNoneMatch() {
		return ifNoneMatch;
	}

	public void setIfNoneMatch(String ifNoneMatch) {
		this.ifNoneMatch = ifNoneMatch;
	}

	public Date getIfModifiedSince() {
		return ifModifiedSince;
	}

	public void setIfModifiedSince(Date ifModifiedSince) {
		this.ifModifiedSince = ifModifiedSince;
	}

}
