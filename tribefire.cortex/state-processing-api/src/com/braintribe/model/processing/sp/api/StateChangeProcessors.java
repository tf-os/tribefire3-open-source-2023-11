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
package com.braintribe.model.processing.sp.api;

import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

public abstract class StateChangeProcessors {
	private static StateChangeProcessorCapabilities defaultCapabilities = capabilities(true, true, true);
	private static StateChangeProcessorCapabilities processOnlyCapabilities = capabilities(false, false, true);
	private static StateChangeProcessorCapabilities beforeOnlyCapabilities = capabilities(true, false, false);
	private static StateChangeProcessorCapabilities afterOnlyCapabilities = capabilities(false, true, false);
	
	public static StateChangeProcessorCapabilities defaultCapabilities() {
		return defaultCapabilities;
	}
	public static StateChangeProcessorCapabilities afterOnlyCapabilities() {
		return afterOnlyCapabilities;
	}
	
	public static StateChangeProcessorCapabilities beforeOnlyCapabilities() {
		return beforeOnlyCapabilities;
	}
	
	public static StateChangeProcessorCapabilities processOnlyCapabilities() {
		return processOnlyCapabilities;
	}
	
	public static StateChangeProcessorCapabilities capabilities(boolean before, boolean after, boolean process) {
		StateChangeProcessorCapabilities capabilities = StateChangeProcessorCapabilities.T.create();
		capabilities.setBefore(before);
		capabilities.setAfter(after);
		capabilities.setProcess(process);
		
		return capabilities;
	}
}
