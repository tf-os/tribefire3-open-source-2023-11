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
package com.braintribe.model.processing.web.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.rest.ParameterValue;
import com.braintribe.model.rest.RestRequest;
import com.braintribe.model.workbench.WorkbenchAction;

/**
 * Context of a REST request
 */
public interface RestRequestContext {
	
	/**
	 * Retrieves the business/data {@link PersistenceGmSession} associated with the REST request context
	 * @return
	 */
	PersistenceGmSession getSession() throws RestRequestException;
	
	/**
	 * Retrieves the workbench {@link PersistenceGmSession} associated with the REST request context
	 * @return
	 */
	PersistenceGmSession getWorkbenchSession() throws RestRequestException;
	
	/**
	 * Retrieves the {@link HttpServletRequest} associated with the REST request context
	 * @return
	 */
	HttpServletRequest getHttpServletRequest();
	
	/**
	 * Retrieves the {@link HttpServletResponse} associated with the REST request context
	 * @return
	 */
	HttpServletResponse getHttpServletResponse();

	/**
	 * return the normalized list on parameter value no matter if simple or multi
	 * @param value
	 * @return
	 */
	List<String> getValues(ParameterValue value);
	
	/**
	 * return the normalized first parameter value no matter if simple or multi
	 * @param value
	 * @return
	 */
	String getFirstValue(ParameterValue value);
	
	/**
	 * handlers can write their results conveniently with that method
	 * @param assembly
	 * @throws RestRequestException
	 */
	void writeResponse(Object assembly) throws RestRequestException;
	
	/**
	 * returns a TraversingCriterion configured for the given name
	 * @param name
	 * @return
	 */
	TraversingCriterion getNamedTraversingCriterion(String name);
	
	/**
	 * returns a WorkbenchAction configured for the given name
	 * @param name
	 * @return
	 */
	WorkbenchAction getNamedWorkbenchAction(String name);

	/**
	 * returns the optional default request which hosts default values that are being used when no explicit value was given in the url or form
	 * @return
	 */
	<R extends RestRequest> R getDefaultRequest();
	
	/**
	 * returns the Internet protocol address associated with the request.
	 */
	String getInternetAddress();
	
}
