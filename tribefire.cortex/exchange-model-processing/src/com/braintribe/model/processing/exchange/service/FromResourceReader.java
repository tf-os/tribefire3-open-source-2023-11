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

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.TypeLookup;
import com.braintribe.logging.Logger;
import com.braintribe.model.exchangeapi.EncodingType;
import com.braintribe.model.exchangeapi.ReadFromResource;
import com.braintribe.model.exchangeapi.ReadFromResourceResponse;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.CustomType;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.exchange.service.EncodingRegistry.EncodingRegistryEntry;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.archives.Archives;
import com.braintribe.utils.archives.ArchivesException;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.archives.zip.ZipContextEntry;

public class FromResourceReader extends ServiceBase {

	private static final Logger logger = Logger.getLogger(FromResourceReader.class);

	private EncodingRegistry registry = EncodingRegistry.DEFAULT;
	private ReadFromResource request;
	private int warnings = 0;

	private List<Resource> unzippedResources;

	public FromResourceReader(ReadFromResource request) {
		this.request = request;
	}

	public FromResourceReader(ReadFromResource request, List<Resource> unzippedResources) {
		this(request);
		this.unzippedResources = unzippedResources;
	}

	@Configurable
	public void setRegistry(EncodingRegistry registry) {
		this.registry = registry;
	}

	public ReadFromResourceResponse run() {

		Resource resource = request.getResource();

		if (resource == null) {
			return createConfirmationResponse("Please provide a resource.", Level.WARNING, ReadFromResourceResponse.T);
		}

		Object assembly = null;
		boolean success = false;

		if (isExchangePackage(resource)) {
			ZipContext zip = openZip(resource);
			if (zip != null) {
				try {

					ZipContextEntry assemblyEntry = findTypedEntry(zip, Utils.encodedAssemblyName);
					if (assemblyEntry == null) {
						return createConfirmationResponse("No encoded assembly entry found in package.", Level.WARNING, ReadFromResourceResponse.T);
					}
					String assemblyEntryName = assemblyEntry.getZipEntry().getName();
					EncodingRegistryEntry marshallerEntry = registry.findEntryByExtension(FileTools.getExtension(assemblyEntryName));
					if (marshallerEntry == null) {
						return createConfirmationResponse("No marshaller registered for entry " + assemblyEntryName, Level.WARNING,
								ReadFromResourceResponse.T);
					}

					assembly = unmarshall(marshallerEntry, assemblyEntry.getPayload());

					if (unzippedResources != null) {
						BaseType.INSTANCE.traverse(assembly, null, new EntityVisitor() {

							@Override
							protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
								String globalId = entity.getGlobalId();
								if (entity instanceof Resource && globalId != null) {

									Resource exportedResource = (Resource) entity;
									String resourceEntryName = "resources/" + globalId + "/" + exportedResource.getName();

									if (zip.get(resourceEntryName) != null) {

										Resource createdResource = Resource.createTransient(() -> {
											ZipContext packageZip = null;
											try {
												packageZip = Archives.zip().from(resource.openStream());
												ZipContextEntry resourceZipEntry = packageZip.getEntry(resourceEntryName);
												if (resourceZipEntry != null) {
													return resourceZipEntry.getPayload();
												}
												return null;
											} catch (ArchivesException e) {
												throw new RuntimeException("Error while reading packaged resource: " + globalId, e);
											} finally {
												// if (packageZip != null)
												// packageZip.close();
											}
										});
										createdResource.setGlobalId(globalId);
										unzippedResources.add(createdResource);
									}

								}

							}
						});
					}

					success = true;
				} catch (Exception e) {
					notifyError("Could not decode exchangepackage", e);
				} finally {
					zip.close();
				}
			}
		} else {

			EncodingType encodingType = request.getEncodingType();

			if (encodingType == null) {
				encodingType = autoDetectEncodingType(resource);
			} else {
				notifyInfo("Use encoding type '" + encodingType.name() + "' for resource '" + resource.getName() + "'");
			}
			if (encodingType == null) {
				return createConfirmationResponse(
						"Could not read resource!\\n" + "No encoding type could not be determined for resource " + resource.getName() + "'.",
						Level.WARNING, ReadFromResourceResponse.T);
			}

			EncodingRegistryEntry marshallerEntry = registry.findEntry(encodingType);
			if (marshallerEntry == null) {
				return createConfirmationResponse("Could not read resource!\n" + "No marshaller found for encoding type '" + encodingType.name()
						+ " determined for resource '" + resource.getName() + "'.", Level.WARNING, ReadFromResourceResponse.T);
			}

			InputStream resourceStream = resource.openStream();
			try {
				assembly = unmarshall(marshallerEntry, resourceStream);
				success = true;
			} catch (Exception e) {
				notifyError("Could not decode assembly with encoding type: " + encodingType, e);
			} finally {
				IOTools.closeCloseable(resourceStream, logger);
			}

		}

		if (!success) {
			return createConfirmationResponse("Could not read assembly from resource.\n See notifications for further details.", Level.WARNING,
					ReadFromResourceResponse.T);
		}

		notifyAssembly(assembly);
		ReadFromResourceResponse response = null;

		if (warnings > 0) {
			response = createConfirmationResponse(
					"Unmarshalled assembly from resource with " + warnings + " warnings.\n See notifications for further details", Level.WARNING,
					ReadFromResourceResponse.T);

		} else {
			response = createResponse("Successfully unmarshalled assembly from resource.", Level.INFO, ReadFromResourceResponse.T);
		}
		response.setAssembly(assembly);
		return response;

	}

	private ZipContext openZip(Resource resource) {
		try {
			return Archives.zip().from(resource.openStream());
		} catch (ArchivesException e) {
			notifyError("Could not extract exchangepackage", e);
			return null;
		}
	}

	private EncodingType autoDetectEncodingType(Resource resource) {
		notifyInfo("No encoding type given. Try autodetection for resource '" + resource.getName() + "'.");
		String resourceExtension = FileTools.getExtension(resource.getName());
		try {
			EncodingType encodingType = EncodingType.valueOf(resourceExtension);
			notifyInfo("Detected encoding type: '" + encodingType.name() + "' for resource '" + resource.getName() + "'.");
			return encodingType;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private Object unmarshall(EncodingRegistryEntry marshallerEntry, InputStream resourceStream) {
		Object assembly;

		//@formatter:off
		assembly = marshallerEntry.getMarshaller()
				.unmarshall(resourceStream,
						GmDeserializationOptions
						.deriveDefaults()
						.setDecodingLenience(new DecodingLenience(request.getLenient()))
						.set(TypeLookup.class, (s) -> {
									
									GenericModelType type = GMF.getTypeReflection().findType(s);
									if (type == null) {
										notifyWarning("Unknown type: "+s);
										warnings++;
									}
									return (CustomType)type;
									
								}).build());
		//@formatter:on
		if (warnings > 0) {
			notifyWarning("The assembly contains instances of " + warnings + " unknown types.");
		}
		return assembly;
	}

	private void notifyAssembly(Object assembly) {

		GenericModelType type = GMF.getTypeReflection().getType(assembly);
		addNotifications(Notifications.build().add().command().gotoModelPath("Decoded assembly").addElement(type.getTypeSignature(), assembly).close()
				.close().list());
	}

	private ZipContextEntry findTypedEntry(ZipContext zip, String baseName) {

		EncodingType encodingType = request.getEncodingType();
		if (encodingType != null) {
			return zip.getEntry(baseName + "." + encodingType);
		}

		notifyInfo("No encoding type given in decode request. Try autodetection.", logger);

		for (EncodingType type : EncodingType.values()) {
			ZipContextEntry assemblyEntry = zip.getEntry(baseName + "." + type);
			if (assemblyEntry != null) {
				return assemblyEntry;
			}
		}
		return null;
	}

	private boolean isExchangePackage(Resource resource) {
		return resource.getName().endsWith(".tfx.zip");
	}

	private void notifyError(String message, Exception e) {
		logger.error("Error in Decoder.", e);
		String errorMessage = e.getMessage();
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		if (rootCause != null) {
			errorMessage = rootCause.getMessage();
		}
		notifyError(message + ": " + errorMessage);
	}
}
