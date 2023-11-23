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
package com.braintribe.tribefire.jinni.wire.contract;

import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.JinniOptions;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.tribefire.jinni.support.request.alias.JinniAlias;
import com.braintribe.tribefire.jinni.support.request.history.JinniHistory;
import com.braintribe.wire.api.space.WireSpace;

public interface JinniContract extends WireSpace {

	Evaluator<ServiceRequest> evaluator();

	Map<String, EntityType<?>> shortcuts();

	Map<EntityType<?>, Function<JinniOptions, ? extends ServiceRequest>> defaultRequests();

	JinniHistory history();

	JinniAlias alias();

	ModelAccessory modelAccessory();

	MarshallerRegistry marshallerRegistry();

	YamlMarshaller yamlMarshaller();

}
