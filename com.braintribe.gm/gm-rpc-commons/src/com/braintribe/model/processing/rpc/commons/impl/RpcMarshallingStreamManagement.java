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
package com.braintribe.model.processing.rpc.commons.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.source.TransientSource;

public class RpcMarshallingStreamManagement {
	protected List<CallStreamCapture> callStreamCaptures = new ArrayList<>();
	protected List<TransientSource> transientSources = new ArrayList<TransientSource>();

	public RpcMarshallingStreamManagement() {
	}
	
	public Consumer<GenericEntity> getMarshallingVisitor() {
		return entity -> {
			// TODO: Optimize transient check when Peter finished his transient property reflection
			if (entity instanceof TransientSource) {
				transientSources.add((TransientSource) entity);
			}
			else if (entity instanceof CallStreamCapture) {
				callStreamCaptures.add((CallStreamCapture) entity);
			}
		};
	}
	
	public List<CallStreamCapture> getCallStreamCaptures() {
		return callStreamCaptures;
	}
	
	public List<TransientSource> getTransientSources() {
		return transientSources;
	}
	
}
