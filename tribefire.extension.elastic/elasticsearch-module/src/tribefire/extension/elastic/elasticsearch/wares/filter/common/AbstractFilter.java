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
/*
 * Copyright 2010-2011 Rajendra Patil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tribefire.extension.elastic.elasticsearch.wares.filter.common;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.braintribe.cfg.Configurable;

/**
 * Common AbstractFilter - infra filter code to be used by other filters through inheritance
 * <p/>
 * This is to have following infra init parameters to all the filter
 * <p/>
 * - ignoreURLPattern - to ignore the URLs matching this regex - acceptURLPattern - to process the URLs matching this
 * regex (ignore precedes) - ignoreMIMEPattern - to ignore if the response mime matches this regex - acceptMIMEPattern -
 * to process if the response mime matches this regex (ignore precedes) - ignoreUAPattern - to ignore if request user
 * agent name matches this regex - acceptUAPattern - to process if request user agent name matches this regex
 * <p/>
 * This filter implements IgnoreAcceptContext with the help of above init parameters and provides easy api for inherited
 * filters to know if given req/res to be ignored or processes.
 *
 * @version 1.0
 * @see IgnoreAcceptContext
 */
public abstract class AbstractFilter implements Filter, IgnoreAcceptContext {

	protected FilterConfig filterConfig;

	private String ignoreURLPattern;

	private String acceptURLPattern;

	private String ignoreMIMEPattern;

	private String acceptMIMEPattern;

	private String ignoreUAPattern;

	private String acceptUAPattern;

	private static final String INIT_PARAM_IGNORE_URL_PATTERN = "ignoreURLPattern";

	private static final String INIT_PARAM_ACCEPT_URL_PATTERN = "acceptURLPattern";

	private static final String INIT_PARAM_IGNORE_MIME_PATTERN = "ignoreMIMEPattern";

	private static final String INIT_PARAM_ACCEPT_MIME_PATTERN = "acceptMIMEPattern";

	private static final String INIT_PARAM_IGNORE_UA_PATTERN = "ignoreUAPattern";

	private static final String INIT_PARAM_ACCEPT_UA_PATTERN = "acceptUAPattern";

	@Override
	public void init(FilterConfig initFilterConfig) throws ServletException {
		this.filterConfig = initFilterConfig;
		if (this.ignoreURLPattern == null) {
			this.ignoreURLPattern = initFilterConfig.getInitParameter(INIT_PARAM_IGNORE_URL_PATTERN);
		}
		if (this.acceptURLPattern == null) {
			this.acceptURLPattern = initFilterConfig.getInitParameter(INIT_PARAM_ACCEPT_URL_PATTERN);
		}
		if (this.ignoreMIMEPattern == null) {
			this.ignoreMIMEPattern = initFilterConfig.getInitParameter(INIT_PARAM_IGNORE_MIME_PATTERN);
		}
		if (this.acceptMIMEPattern == null) {
			this.acceptMIMEPattern = initFilterConfig.getInitParameter(INIT_PARAM_ACCEPT_MIME_PATTERN);
		}
		if (this.ignoreUAPattern == null) {
			this.ignoreUAPattern = initFilterConfig.getInitParameter(INIT_PARAM_IGNORE_UA_PATTERN);
		}
		if (this.acceptUAPattern == null) {
			this.acceptUAPattern = initFilterConfig.getInitParameter(INIT_PARAM_ACCEPT_UA_PATTERN);
		}
	}

	private boolean isURLIgnored(String url) {
		return this.ignoreURLPattern != null && url != null && url.matches(ignoreURLPattern);
	}

	@Override
	public boolean isURLAccepted(String url) {
		return !this.isURLIgnored(url) && (this.acceptURLPattern == null || (url != null && url.matches(acceptURLPattern)));
	}

	private boolean isMIMEIgnored(String mimeType) {
		return this.ignoreMIMEPattern != null && mimeType != null && mimeType.matches(ignoreMIMEPattern);
	}

	@Override
	public boolean isMIMEAccepted(String mimeType) {
		return !this.isMIMEIgnored(mimeType) && (this.acceptMIMEPattern == null || (mimeType != null && mimeType.matches(acceptMIMEPattern)));
	}

	private boolean isUserAgentIgnored(String userAgent) {
		return this.ignoreUAPattern != null && userAgent != null && userAgent.matches(ignoreUAPattern);
	}

	@Override
	public boolean isUserAgentAccepted(String userAgent) {
		return !this.isUserAgentIgnored(userAgent) && (this.acceptUAPattern == null || (userAgent != null && userAgent.matches(acceptUAPattern)));
	}

	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	@Configurable
	public void setIgnoreURLPattern(String ignoreURLPattern) {
		this.ignoreURLPattern = ignoreURLPattern;
	}
	@Configurable
	public void setAcceptURLPattern(String acceptURLPattern) {
		this.acceptURLPattern = acceptURLPattern;
	}
	@Configurable
	public void setIgnoreMIMEPattern(String ignoreMIMEPattern) {
		this.ignoreMIMEPattern = ignoreMIMEPattern;
	}
	@Configurable
	public void setAcceptMIMEPattern(String acceptMIMEPattern) {
		this.acceptMIMEPattern = acceptMIMEPattern;
	}
	@Configurable
	public void setIgnoreUAPattern(String ignoreUAPattern) {
		this.ignoreUAPattern = ignoreUAPattern;
	}
	@Configurable
	public void setAcceptUAPattern(String acceptUAPattern) {
		this.acceptUAPattern = acceptUAPattern;
	}

}
