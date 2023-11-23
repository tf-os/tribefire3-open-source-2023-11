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
package com.braintribe.gm.service.access.api;

import java.util.function.BiFunction;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;

public interface AccessProcessingConfiguration {

	void registerAccess(IncrementalAccess access);

	void registerAccess(BiFunction<String, GmMetaModel, IncrementalAccess> accessFactory, String accessId, GmMetaModel model);

	SmoodAccess registerAccess(String accessId, GmMetaModel metaModel);

	<A extends AccessRequest> void registerAccessRequestProcessor(EntityType<A> requestType, AccessRequestProcessor<? super A, ?> processor);
}