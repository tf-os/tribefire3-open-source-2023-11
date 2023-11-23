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
package com.braintribe.model.cortexapi.access;

import java.util.List;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.cleanup.GarbageCollection;
import com.braintribe.model.service.api.ServiceRequest;

public interface RunGarbageCollection extends GarbageCollectionRequest {
	
	EntityType<RunGarbageCollection> T = EntityTypes.T(RunGarbageCollection.class);
	
	
	/**
	 * See {@link #setAccess(IncrementalAccess)}
	 */
	IncrementalAccess getAccess();

	/**
	 * The access on which the garbage collection will be performed.
	 */
	void setAccess(IncrementalAccess access);

	/**
	 * see {@link #setUseCases(List)}
	 */
	List<String> getUseCases();

	/**
	 * An optional list of use cases. If set, the garbage collection will use a use case selector to retrieve the
	 * {@link GarbageCollection} metadata.
	 */
	void setUseCases(List<String> useCases);

	/**
	 * See {@link #getTestModeEnabled()}
	 */
	@Initializer("true")
	boolean getTestModeEnabled();

	/**
	 * If enabled, the garbage collection will be run and a report will be created, but nothing will actually be
	 * deleted.
	 */
	void setTestModeEnabled(boolean testModeEnabled);
	
	
	
	@Override
	EvalContext<? extends GarbageCollectionResponse> eval(Evaluator<ServiceRequest> evaluator);

}
