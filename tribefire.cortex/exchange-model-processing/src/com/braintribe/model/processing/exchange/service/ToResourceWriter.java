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
package com.braintribe.model.processing.exchange.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchangeapi.EncodingType;
import com.braintribe.model.exchangeapi.WriteToResource;
import com.braintribe.model.exchangeapi.WriteToResourceResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.exchange.service.EncodingRegistry.EncodingRegistryEntry;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.stream.MemoryThresholdBuffer;

public class ToResourceWriter extends ServiceBase {
	private EncodingRegistry registry = EncodingRegistry.DEFAULT;
	private EncodingType defaultEncodingType = EncodingType.xml;

	private WriteToResource request;
	private PersistenceGmSession session;

	public ToResourceWriter(WriteToResource request, PersistenceGmSession session) {
		this.session = session;
		this.request = request;
	}

	@Configurable
	public void setRegistry(EncodingRegistry registry) {
		this.registry = registry;
	}

	public WriteToResourceResponse run() {

		Object assembly = request.getAssembly();
		if (assembly == null) {
			return createConfirmationResponse("No assembly specified.", Level.WARNING, WriteToResourceResponse.T);
		}

		EncodingType encodingType = request.getEncodingType();
		if (encodingType == null) {
			notifyInfo("No explicit encoding type given. Using default encoding type '" + defaultEncodingType.name() + "'.");
			encodingType = defaultEncodingType;
		} else {
			notifyInfo("Using encoding type '" + encodingType.name() + "'.");
		}
		EncodingRegistryEntry marshallerEntry = registry.findEntry(encodingType);
		if (marshallerEntry == null) {
			return createConfirmationResponse("No marshaller registered for type " + encodingType, Level.WARNING, WriteToResourceResponse.T);
		}

		Resource resource = marshall(marshallerEntry);

		if (resource == null) {
			return createConfirmationResponse("Could not encode assembly.\nPlease check the notifications for further details.", Level.WARNING,
					WriteToResourceResponse.T);
		}
		notifyResource(resource);
		WriteToResourceResponse response = createResponse("Successfully marshalled assembly to resource " + resource.getName(),
				WriteToResourceResponse.T);
		response.setResource(resource);

		return response;
	}

	private void notifyResource(Resource exportedResource) {
		addNotifications(Notifications.build().add().command().gotoModelPath("Resource").addElement(exportedResource).close().close().list());
	}

	private Resource marshall(EncodingRegistryEntry marshallerEntry) throws MarshallException {

		Object assembly = normalizeAssembly(request.getAssembly());
		if (assembly == null) {
			return null;
		}

		byte[] encodedAssembly = encodeAssembly(marshallerEntry, assembly);

		Resource resource = null;
		if (assembly instanceof ExchangePackage) {
			ExchangePackage exchangePackage = (ExchangePackage) assembly;
			String resourceName = Utils.buildResourceName(Utils.buildBaseName(exchangePackage), "zip");
			resource = buildZipResource(marshallerEntry, encodedAssembly, resource, resourceName);

		} else {
			String resourceName = Utils.buildResourceName(request.getResourceBaseName(), marshallerEntry.getExtension());
			resource = buildResource(resourceName, () -> new ByteArrayInputStream(encodedAssembly));
		}

		notifyInfo("Resource: " + resource.getName() + " created.");
		return resource;
	}

	private Object normalizeAssembly(Object assembly) {
		if (assembly instanceof Collection<?>) {
			Collection<?> collection = (Collection<?>) assembly;
			switch (collection.size()) {
				case 0:
					notifyWarning("Empty collection provided.");
					return null;
				case 1:
					assembly = collection.iterator().next();
			}
		}
		return assembly;
	}

	private Resource buildZipResource(EncodingRegistryEntry marshallerEntry, byte[] encodedAssembly, Resource resource, String resourceName) {
		MemoryThresholdBuffer buffer = new MemoryThresholdBuffer();
		try {

			String extension = marshallerEntry.getExtension();
			ZipContext zip = Archives.zip().add(Utils.addExtension(Utils.encodedAssemblyName, extension), new ByteArrayInputStream(encodedAssembly));

			if (request.getAddResourceBinaries()) {
				Set<Resource> resourcesToWrite = collectResources();
				if (!resourcesToWrite.isEmpty()) {
					for (Resource resourceToWrite : resourcesToWrite) {
						Resource sessionAttachedResource = session.query().findEntity(resourceToWrite.getGlobalId());
						InputStream stream = sessionAttachedResource.openStream();
						try {
							zip.add("resources/" + sessionAttachedResource.getId() + "/" + sessionAttachedResource.getName(), stream);
						} finally {
							stream.close();
						}
					}

					notifyInfo("Added " + resourcesToWrite.size() + " resources to exchange package.");
				}
			}

			zip.to(buffer).close();

			buffer.close();

			resource = buildResource(resourceName, () -> buffer.openInputStream(false));

		} catch (Exception e) {
			throw new MarshallException("Could not compress resource: " + resourceName, e);
		} finally {
			buffer.delete();
		}
		return resource;
	}

	private Set<Resource> collectResources() {
		Set<Resource> resources = new HashSet<>();
		BaseType.INSTANCE.traverse(request.getAssembly(), null, new EntityVisitor() {
			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				if (entity instanceof Resource) {
					resources.add((Resource) entity);
				}
			}
		});
		return resources;
	}

	private byte[] encodeAssembly(EncodingRegistryEntry marshallerEntry, Object assembly) {
		ByteArrayOutputStream encodingOs = new ByteArrayOutputStream();
		try {

			Marshaller marshaller = marshallerEntry.getMarshaller();

			//@formatter:off
			marshaller.marshall(encodingOs, assembly,
					GmSerializationOptions.deriveDefaults().stabilizeOrder(request.getStabilizeOrder())
							.outputPrettiness(request.getPrettyOutput() ? OutputPrettiness.high : OutputPrettiness.low)
							.writeEmptyProperties(request.getWriteEmptyProperties()).build());
			//@formatter:on

		} finally {
			IOTools.closeCloseable(encodingOs, "export xml outputstream", null);
		}

		notifyInfo("Assembly marshalled.");

		byte[] encodedAssembly = encodingOs.toByteArray();
		return encodedAssembly;
	}

	private Resource buildResource(String resourceName, InputStreamProvider resourceStreamProvider) {
		Resource resource = session.resources().create().name(resourceName).store(resourceStreamProvider);
		return resource;
	}

}
