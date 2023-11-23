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
package com.braintribe.web.impl.registry;

import java.io.File;

import com.braintribe.web.api.registry.MultipartConfig;

public class ConfigurableMultipartConfig implements MultipartConfig {

	protected int fileSizeThreshold = 0;
	protected File location;
	protected long maxFileSize = -1;
	protected long maxRequestSize = -1;

	@Override
	public int getFileSizeThreshold() {
		return fileSizeThreshold;
	}

	public void setFileSizeThreshold(int fileSizeThreshold) {
		this.fileSizeThreshold = fileSizeThreshold;
	}

	@Override
	public File getLocation() {
		return location;
	}

	public void setLocation(File location) {
		this.location = location;
	}

	@Override
	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	@Override
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	public void setMaxRequestSize(long maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	/* builder methods */

	public MultipartConfig fileSizeThreshold(int _fileSizeThreshold) {
		this.fileSizeThreshold = _fileSizeThreshold;
		return this;
	}

	public MultipartConfig lLocation(File _location) {
		this.location = _location;
		return this;
	}

	public MultipartConfig maxFileSize(long _maxFileSize) {
		this.maxFileSize = _maxFileSize;
		return this;
	}

	public MultipartConfig maxRequestSize(long _maxRequestSize) {
		this.maxRequestSize = _maxRequestSize;
		return this;
	}

	/* // builder methods */

}
