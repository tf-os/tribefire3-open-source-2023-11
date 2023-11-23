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
import com.braintribe.model.processing.aop.api.aspect.Advice;
import com.braintribe.model.processing.aop.api.context.BeforeContext;

public class BeforeContextImpl<I,O> extends AbstractContextImpl<I> implements BeforeContext<I,O>{
	
	private static Logger log = Logger.getLogger(BeforeContextImpl.class);
	
	private O response;
	private boolean skipped = false;
	private boolean overridden = false;
	
	@Override
	public void skip(O response) {
		this.response = response;
		skipped = true;
		log.info("Skip execution of joint point ["+ getJointPoint()+"], advice [" + getAdvice() + "]");
	}

	public O getResponse() {
		return response;
	}
	
	public boolean isSkipped() {
		return skipped;
	}
	
	@Override
	public void overrideRequest(I request) {
		overridden = true;
		setRequest( request);		
	}

	@Override
	public Advice getAdvice() {		
		return Advice.before;
	}
	
	public boolean isOverridden() {
		return overridden;
	}
	
	
	
	
	
}
