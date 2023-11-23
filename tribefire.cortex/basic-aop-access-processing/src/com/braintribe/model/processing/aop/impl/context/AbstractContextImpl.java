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
package com.braintribe.model.processing.aop.impl.context;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.context.Context;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author dirk, pit
 *
 */
public abstract class AbstractContextImpl<I> implements Context<I>{

	
	private static Logger log = Logger.getLogger(AbstractContextImpl.class);
	
	protected I request;

	private AccessJoinPoint joinPoint;	
	private static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();	
	private PersistenceGmSession userSession;
	private PersistenceGmSession systemSession;
	

	
	// **************************************************************************
	// Constructor
	// **************************************************************************

	/**
	 * Default constructor
	 */
	public AbstractContextImpl() {
	}

	// **************************************************************************
	// Getter/Setter
	// **************************************************************************
			
	
	@Override
	public I getRequest() {
		return request;
	}	
	public void setRequest( I request) {
		this.request = request;
	}

	@Override
	public AccessJoinPoint getJointPoint() {
		return joinPoint;
	}
	public void setJoinPoint(AccessJoinPoint joinPoint) {
		this.joinPoint = joinPoint;
	}
	
	public void setSession(PersistenceGmSession userSession) {
		this.userSession = userSession;
	}

	@Override
	public PersistenceGmSession getSession() {		
		return userSession;
	}
	
	public void setSystemSession(PersistenceGmSession systemSession) {
		this.systemSession = systemSession;
	}

	@Override
	public PersistenceGmSession getSystemSession() {		
		return systemSession;
	}
	
	protected void commitIfNecessary( PersistenceGmSession session) throws GmSessionException {
		if (session.getTransaction().hasManipulations()) {
			session.commit();
		}
	}
	
	public void commitIfNecessary() throws GmSessionException{
		try {
			commitIfNecessary(systemSession);
		} catch (GmSessionException e) {
			String msg = "cannot commit system session for joinPoint [" + joinPoint + "], advice [" + getAdvice() +"]";
			log.error( msg, e);
			throw e;
		}
		try {
			commitIfNecessary(userSession);
		} catch (GmSessionException e) {
			String msg = "cannot commit user session for joinPoint [" + joinPoint + "], advice [" + getAdvice() +"]";
			log.error( msg, e);
			throw e;
		}
	}

}
