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
package tribefire.extension.xml.schemed.marshaller.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;

import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerMarshallResponse;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallRequest;
import tribefire.extension.xml.schemed.model.api.xml.marshaller.api.model.SchemedXmlMarshallerUnmarshallResponse;

/**
 * a processor handle the two features, in & out
 * 
 * @author pit
 *
 */
public class SchemedXmlMarshallingRequestProcessor {

	private SchemedXmlMarshaller marshaller;
	private GmMetaModel mappingModel;
	private ReentrantLock lock = new ReentrantLock();

	private synchronized void initialize(GmMetaModel model) {
		if (mappingModel == null || mappingModel != model) {
			lock.lock();
			try {
				if (mappingModel == null) {
					mappingModel = model;
					marshaller = new SchemedXmlMarshaller();
					marshaller.setMappingMetaModel(mappingModel);
				} else if (mappingModel != model) {
					mappingModel = model;
					marshaller = new SchemedXmlMarshaller();
					marshaller.setMappingMetaModel(mappingModel);
				}
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * processor for unmarshalling request
	 * 
	 * @param request
	 *            - {@link SchemedXmlMarshallerUnmarshallRequest}
	 * @return - {@link SchemedXmlMarshallerUnmarshallResponse}
	 */
	public SchemedXmlMarshallerUnmarshallResponse process(SchemedXmlMarshallerUnmarshallRequest request) {

		initialize(request.getMappingModel());

		DecodingLenience lenience = new DecodingLenience();
		lenience.setLenient(request.getIsLenient());
		GenericEntity result = (GenericEntity) marshaller.unmarshall(request.getXml().openStream(),
				GmDeserializationOptions.deriveDefaults().setDecodingLenience(lenience).build());

		SchemedXmlMarshallerUnmarshallResponse response = SchemedXmlMarshallerUnmarshallResponse.T.create();
		response.setAssembly(result);

		return response;
	}

	/**
	 * processor for the marshalling request
	 * 
	 * @param request
	 *            - the {@link SchemedXmlMarshallerMarshallRequest}
	 * @return - the {@link SchemedXmlMarshallerMarshallResponse}
	 */
	public SchemedXmlMarshallerMarshallResponse process(SchemedXmlMarshallerMarshallRequest request) {
		SchemedXmlMarshallerMarshallResponse response = SchemedXmlMarshallerMarshallResponse.T.create();
		initialize(request.getMappingModel());
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			marshaller.marshall(out, request.getAssembly());
			response.setExpression(new String(out.toByteArray()));
		} catch (IOException e) {
			throw new SchemedXmlMarshallingException(e);
		}

		return response;
	}
}
