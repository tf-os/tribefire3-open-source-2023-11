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
package tribefire.extension.demo.processing;

import java.io.ByteArrayOutputStream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.codec.marshaller.api.CharacterMarshaller;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;

import tribefire.extension.demo.model.api.EntityMarshallingRequest;
import tribefire.extension.demo.model.api.EntityMarshallingResponse;
import tribefire.extension.demo.model.api.MarshallEntityToJson;
import tribefire.extension.demo.model.api.MarshallEntityToXml;
import tribefire.extension.demo.model.api.MarshallEntityToYaml;

public class EntityMarshallingProcessor extends AbstractDispatchingServiceProcessor<EntityMarshallingRequest, EntityMarshallingResponse>  {
	
	private CharacterMarshaller jsonMarshaller;
	private CharacterMarshaller xmlMarshaller;
	private CharacterMarshaller yamlMarshaller;
	
	@Configurable
	@Required
	public void setJsonMarshaller(CharacterMarshaller jsonMarshaller) {
		this.jsonMarshaller = jsonMarshaller;
	}

	@Configurable
	@Required
	public void setXmlMarshaller(CharacterMarshaller xmlMarshaller) {
		this.xmlMarshaller = xmlMarshaller;
	}

	@Configurable
	@Required
	public void setYamlMarshaller(CharacterMarshaller yamlMarshaller) {
		this.yamlMarshaller = yamlMarshaller;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<EntityMarshallingRequest, EntityMarshallingResponse> dispatching) {
		dispatching.register(MarshallEntityToJson.T, this::marshallEntityToJson);
		dispatching.register(MarshallEntityToXml.T, this::marshallEntityToXml);
		dispatching.register(MarshallEntityToYaml.T, this::marshallEntityToYaml);
		
	}
	
	private EntityMarshallingResponse marshallEntityToJson(@SuppressWarnings("unused") ServiceRequestContext context, MarshallEntityToJson request) {
		return marshall(request, jsonMarshaller);
	}

	private EntityMarshallingResponse marshallEntityToXml(@SuppressWarnings("unused") ServiceRequestContext context, MarshallEntityToXml request) {
		return marshall(request, xmlMarshaller);
	}
	
	private EntityMarshallingResponse marshallEntityToYaml(@SuppressWarnings("unused") ServiceRequestContext context, MarshallEntityToYaml request) {
		return marshall(request, yamlMarshaller);
	}
	
	private EntityMarshallingResponse marshall(EntityMarshallingRequest request, CharacterMarshaller marshaller) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		marshaller.marshall(stream, request.getEntity(), GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build());
		
		EntityMarshallingResponse response = EntityMarshallingResponse.T.create();
		response.setMarshalledEntity(stream.toString());
		
		return response;
	}

}
