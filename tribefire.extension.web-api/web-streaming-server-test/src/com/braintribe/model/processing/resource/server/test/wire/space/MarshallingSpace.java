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
package com.braintribe.model.processing.resource.server.test.wire.space;

import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class MarshallingSpace implements WireSpace {

	@Managed
	public ConfigurableMarshallerRegistry registry() {

		BasicConfigurableMarshallerRegistry bean = new BasicConfigurableMarshallerRegistry();

		bean.registerMarshaller("application/xml", xmlMarshaller());
		bean.registerMarshaller("text/xml", xmlMarshaller());
		bean.registerMarshaller("gm/xml", xmlMarshaller());

		bean.registerMarshaller("application/gm", binMarshaller());
		bean.registerMarshaller("gm/bin", binMarshaller());

		bean.registerMarshaller("application/json", jsonMarshaller());
		bean.registerMarshaller("text/x-json", jsonMarshaller());
		bean.registerMarshaller("gm/json", jsonMarshaller());

		return bean;

	}

	@Managed
	public Marshaller xmlMarshaller() {
		StaxMarshaller bean = new StaxMarshaller();
		bean.setRequiredTypesReceiver(requiredTypeEnsurer());
		return bean;
	}

	@Managed
	public Marshaller jsonMarshaller() {
		JsonStreamMarshaller bean = new JsonStreamMarshaller();
		// TODO: Must have the required type receiver
		return bean;
	}

	@Managed
	public Marshaller binMarshaller() {
		Bin2Marshaller bean = new Bin2Marshaller();
		bean.setRequiredTypesReceiver(requiredTypeEnsurer());
		return bean;
	}

	protected Consumer<Set<String>> requiredTypeEnsurer() {
		return n -> {
			/* no ensurer */
		};
	}

}
