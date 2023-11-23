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
package com.braintribe.logging.ndc.mbean;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;

public class NdcMBeanImpl extends StandardMBean implements NdcMBean {

	public final ThreadLocal<Deque<String>> nestedDiagnosticContext = new ThreadLocal<Deque<String>>(); 
	public final ThreadLocal<Map<String,String>> mappedDiagnosticContext = new ThreadLocal<Map<String,String>>(); 

	protected NdcMBeanImpl() throws NotCompliantMBeanException {
		super(NdcMBean.class);
	}

	/**
	 * Returns the current thread's nested diagnostic context (which is basically a stack).
	 * 
	 * @return The thread's nested diagnostic context.
	 */
	public Deque<String> getOrCreateNestedDiagnosticContext(boolean createIfNotAvailable) {
		
		// Note: since we're using a ThreadLocal variable, synchronizing of this method is not necessary
		
		Deque<String> ndc = nestedDiagnosticContext.get();
		if ((ndc == null) && (createIfNotAvailable)) {
			ndc = new LinkedList<String>();
			nestedDiagnosticContext.set(ndc);
		}		
		return ndc;
	}
	
	@Override
	public void pushContext(String context) {
		Deque<String> ndc = getOrCreateNestedDiagnosticContext(true);
		ndc.push(context);
	}

	@Override
	public void popContext() {
		Deque<String> ndc = getOrCreateNestedDiagnosticContext(false);
		if (ndc == null) {
			//Nothing to do
			return;
		}
		if (ndc.size() > 0) {
			ndc.pop();
		}
		if (ndc.size() == 0) {
			nestedDiagnosticContext.remove();
		}
	}

	@Override
	public void removeContext() {
		nestedDiagnosticContext.remove();
	}

	@Override
	public Deque<String> getNdc() {
		return nestedDiagnosticContext.get();
	}
	
	
	
	
	
	/**
	 * Returns the current thread's nested mapped diagnostic context (which is basically a map).
	 * 
	 * @return The thread's nested mapped diagnostic context.
	 */
	public Map<String,String> getOrCreateMappedDiagnosticContext(boolean createIfNotAvailable) {
		
		// Note: since we're using a ThreadLocal variable, synchronizing of this method is not necessary
		
		Map<String,String> mdc = mappedDiagnosticContext.get();
		if ((mdc == null) && (createIfNotAvailable)) {
			mdc = new HashMap<String,String>();
			mappedDiagnosticContext.set(mdc);
		}		
		return mdc;
	}

	@Override
	public void clearMdc() {
		mappedDiagnosticContext.remove();
	}
	@Override
	public Object get(String key) {
		if (key == null) {
			return null;
		}
		Map<String,String> mdc = this.getOrCreateMappedDiagnosticContext(false);
		if (mdc == null) {
			return null;
		}
		return mdc.get(key);
	}
	@Override
	public Map<String,String> getMdc() {
		return mappedDiagnosticContext.get();
	}
	@Override
	public void put(String key, String value) {
		if (key == null) {
			return;
		}
		if (value == null) {
			value = "(null)";
		}
		Map<String,String> mdc = this.getOrCreateMappedDiagnosticContext(true);
		mdc.put(key, value);
	}
	@Override
	public void remove(String key) {
		if (key == null) {
			return;
		}
		Map<String,String> mdc = this.getOrCreateMappedDiagnosticContext(false);
		if (mdc == null) {
			return;
		}
		mdc.remove(key);
		if (mdc.isEmpty()) {
			this.mappedDiagnosticContext.remove();
		}
	}

	
	
}
