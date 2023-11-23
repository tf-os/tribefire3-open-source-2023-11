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

/**
 * Interface to provide an API for filters to check if given req/res has to be ignore or processed.
 * <p/>
 * AbstractFilter implements it with the help of six init parameters.
 *
 * @see AbstractFilter for the implementation
 */
public interface IgnoreAcceptContext {

	/**
	 * @param URL
	 *            - request URL string without query parameters
	 * @return isAccepted - true if given URL has to be processed
	 */
	boolean isURLAccepted(String URL);

	/**
	 * @param mime
	 *            - contentType of the chained response
	 * @return isAccepted - true if it is to be processed by this filter
	 */

	boolean isMIMEAccepted(String mime);

	/**
	 * @param userAgent
	 *            - requesting user agent string
	 * @return isAccepted - true if request by given user agent has to be processed/filtered.
	 */

	boolean isUserAgentAccepted(String userAgent);

}
