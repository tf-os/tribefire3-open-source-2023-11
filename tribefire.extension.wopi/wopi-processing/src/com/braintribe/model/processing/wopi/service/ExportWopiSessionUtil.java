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
package com.braintribe.model.processing.wopi.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.braintribe.codec.CodecException;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.GmSerializationOptionsBuilder;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.generic.stream.StreamProviders;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.platformreflection.DiagnosticPackage;
import com.braintribe.model.platformreflection.request.GetDiagnosticPackage;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.archives.zip.ZipContext;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * This is more or less the same code used in ADx for exporting WopiSession
 * 
 *
 */
public class ExportWopiSessionUtil {

	private static final Logger logger = Logger.getLogger(WopiServiceProcessor.class);

	//@formatter:off
	private static Set<Property> ignoreOnEncoding = 
			CollectionTools2.asSet(
				ServiceRequest.T.getProperty("metaData"));
	//@formatter:on

	//@formatter:off
	private static GmSerializationOptions serializationOptions = 
			GmSerializationOptionsBuilder
				.defaults
				.outputPrettiness(OutputPrettiness.high)
				.useDirectPropertyAccess(false)
				.writeAbsenceInformation(false)
				.useDirectPropertyAccess(true)
				.writeEmptyProperties(false);
	//@formatter:on

	private static final String WOPI_SESSION = "wopiSession";
	private static final String WOPI_SESSION_EXPORT = "wopi-session-export";
	private static final String DIAGNOSTIC_PACKAGE = "diagnosticPackage";

	private static final String ERROR = "ERROR";
	private static final String TXT = "txt";
	private static final String JSON = "json";
	protected static final String ZIP = "zip";
	protected static final String APPLICATION_ZIP = "application/zip";

	protected static Resource export(List<WopiSession> wopiSessions, Marshaller marshaller, ZipContext exportZip, PersistenceGmSession session,
			boolean includeDiagnosticPackage, boolean includeCurrentResource, boolean includeResourceVersions,
			boolean includePostOpenResourceVersions) {
		if (includeDiagnosticPackage) {
			addDiagnosticPackage(session, exportZip);
		}

		wopiSessions.forEach(wopiSession -> {
			String encodedWopiSession = encode(wopiSession, marshaller, false, true);

			String wopiSessionFolder = WOPI_SESSION + "-" + wopiSession.getCorrelationId();
			// Add the serialized Job to the package
			addToZip(exportZip, wopiSessionFolder + "/" + WOPI_SESSION + "_" + wopiSession.correlationId + "." + JSON,
					() -> textInputStream(encodedWopiSession));

			if (includeCurrentResource) {

				Resource resource = wopiSession.getCurrentResource();
				addToZip(exportZip, wopiSessionFolder + "/CurrentResource/" + resource.getId() + "_" + resource.getName(),
						() -> openStream(session, resource));
			}

			if (includeResourceVersions) {
				List<Resource> resourceVersions = wopiSession.getResourceVersions();
				for (int i = 0; i < resourceVersions.size(); i++) {
					Resource resource = resourceVersions.get(i);
					addToZip(exportZip, wopiSessionFolder + "/ResourceVersions/" + i + "_" + resource.getId() + "_" + resource.getName(),
							() -> openStream(session, resource));
				}
			}

			if (includePostOpenResourceVersions) {
				List<Resource> postOpenResourceVersions = wopiSession.getPostOpenResourceVersions();
				for (int i = 0; i < postOpenResourceVersions.size(); i++) {
					Resource resource = postOpenResourceVersions.get(i);
					addToZip(exportZip, wopiSessionFolder + "/PostOpenResourceVersion/" + i + "_" + resource.getId() + "_" + resource.getName(),
							() -> openStream(session, resource));
				}
			}
		});

		Resource export = Resource.createTransient(StreamProviders.from(out -> writeAndCloseZip(exportZip, out, "WopiSession export")));

		String currentTs = DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT_2);

		export.setName(ExportWopiSessionUtil.WOPI_SESSION_EXPORT + "-" + currentTs + "." + ExportWopiSessionUtil.ZIP);
		export.setMimeType(ExportWopiSessionUtil.APPLICATION_ZIP);
		export.setCreated(new Date());

		String creator = "unknown";
		String userName = session.getSessionAuthorization().getUserName();
		if (StringTools.isBlank(userName)) {
			creator = userName;
		}
		export.setCreator(creator);

		return export;
	}

	// -----------------------------------------------------------------------
	// PRIVATE HELPER METHODS
	// -----------------------------------------------------------------------

	private static ZipContext addToZip(ZipContext zip, String name, InputStreamProvider isProvider) {
		return addToZip(zip, name, isProvider, true);
	}

	private static ZipContext addToZip(ZipContext zip, String name, InputStreamProvider isProvider, boolean lenient) {
		InputStream is = null;
		try {
			is = isProvider.openInputStream();

			if (is == null) {
				return zip;
			}
			return zip.add(name, is);
		} catch (Exception e) {
			if (lenient) {
				return addErroFileToZip(zip, name, e.getMessage());
			}
			throw Exceptions.unchecked(e, "Could not add :" + name + " to zip.");
		} finally {
			IOTools.closeCloseable(is, logger);
		}
	}

	private static String encode(Object value, Marshaller marshaller, boolean shallowfy, boolean resolveAbsence) {

		Object clonedValue = detach(value, shallowfy, resolveAbsence);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshall(baos, clonedValue, serializationOptions);
		try {
			return baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new CodecException("Unsupported encoding.", e);
		}
	}

	private static void addDiagnosticPackage(PersistenceGmSession session, ZipContext exportZip) {
		GetDiagnosticPackage request = GetDiagnosticPackage.T.create();
		try {
			DiagnosticPackage diagnosticPackage = request.eval(session).get();
			if (diagnosticPackage != null) {
				Resource diagnosticPackageResource = diagnosticPackage.getDiagnosticPackage();
				addToZip(exportZip, diagnosticPackageResource.getName(), diagnosticPackageResource::openStream);
			} else {
				logger.warn("No diagnoistic package returned.");
				addToZip(exportZip, ERROR + "_" + DIAGNOSTIC_PACKAGE + "." + TXT, () -> textInputStream("diagnostic package is not available!"));
			}
		} catch (Exception e) {
			logger.warn("Could not add diagnostic package to WopiSession export package.", e);
			addToZip(exportZip, ERROR + "_" + DIAGNOSTIC_PACKAGE + "." + TXT,
					() -> textInputStream("error while retrieving diagnostic package: " + e.getMessage()));
		}
	}

	private static InputStream openStream(PersistenceGmSession session, Resource resource) {
		try {
			return session.resources().openStream(resource);
		} catch (IOException e) {
			logger.error("Could not open stream for resource: " + resource, e);
			return null;
		}
	}

	private static ZipContext addErroFileToZip(ZipContext zip, String name, String errorMessage) {
		int idx = name.lastIndexOf('/');
		if (idx > 0) {
			String folder = name.substring(0, idx);
			String filename = name.substring(idx + 1);
			name = folder + "/" + "ERROR_" + filename + ".txt";
		} else {
			name = "ERROR_" + name + ".txt";
		}
		return addToZip(zip, name, () -> textInputStream(errorMessage));
	}

	private static ByteArrayInputStream textInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes());
	}

	private static Object detach(Object value, boolean shallowfy, boolean resolveAbsence) {
		Object clonedValue = GmBaseType.T.clone(new StandardCloningContext() {

			@Override
			public boolean isAbsenceResolvable(Property property, GenericEntity entity, AbsenceInformation absenceInformation) {
				return resolveAbsence;
			}

			@Override
			public <T> T getAssociated(GenericEntity entity) {
				GmSession session = entity.session();
				if (shallowfy && session != null && session instanceof PersistenceGmSession) {
					GenericEntity shallow = entity.entityType().create();
					shallow.setId(entity.getId());
					return (T) shallow;
				}
				return super.getAssociated(entity);
			}

			@Override
			public boolean canTransferPropertyValue(EntityType<? extends GenericEntity> entityType, Property property,
					GenericEntity instanceToBeCloned, GenericEntity clonedInstance, AbsenceInformation sourceAbsenceInformation) {
				return (ignoreOnEncoding.contains(property)) ? false : true;
			}

		}, value, StrategyOnCriterionMatch.skip);
		return clonedValue;
	}

	private static void writeAndCloseZip(ZipContext zip, OutputStream out, String context) {
		try {
			zip.to(out);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while creating zip for: " + context);
		} finally {
			zip.close();
		}
	}
}
