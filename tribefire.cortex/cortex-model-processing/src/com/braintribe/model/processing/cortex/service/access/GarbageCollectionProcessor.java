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
package com.braintribe.model.processing.cortex.service.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.utils.genericmodel.EntityTypeBasedEntitiesFinder;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortexapi.access.GarbageCollectionRequest;
import com.braintribe.model.cortexapi.access.GarbageCollectionResponse;
import com.braintribe.model.cortexapi.access.RunGarbageCollection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.cleanup.GarbageCollectionKind;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.garbagecollection.GarbageCollection;
import com.braintribe.model.processing.garbagecollection.GarbageCollectionMetaDataBasedEntitiesFinder;
import com.braintribe.model.processing.garbagecollection.GarbageCollectionReport;
import com.braintribe.model.processing.garbagecollection.GarbageCollectionReport.GarbageCollectionReportSettings;
import com.braintribe.model.processing.garbagecollection.SubsetConfiguration;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.genericmodel.GMCoreTools;

public class GarbageCollectionProcessor implements AccessRequestProcessor<GarbageCollectionRequest, GarbageCollectionResponse> {
	
	protected static Logger logger = Logger.getLogger(GarbageCollectionProcessor.class);

	private GarbageCollection garbageCollection;
	private PersistenceGmSessionFactory sessionFactory;

	private final AccessRequestProcessor<GarbageCollectionRequest, GarbageCollectionResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(RunGarbageCollection.T, this::runGarbageCollection);
	});
	
	@Required
	public void setGarbageCollection(GarbageCollection garbageCollection) {
		this.garbageCollection = garbageCollection;
	}

	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public GarbageCollectionResponse process(AccessRequestContext<GarbageCollectionRequest> context) {
		return dispatcher.process(context);
	}
	
	public GarbageCollectionResponse runGarbageCollection(AccessRequestContext<RunGarbageCollection> context) {
		RunGarbageCollection request = context.getRequest();

		if (request.getAccess() == null) {
			throw new IllegalArgumentException("Cannot perform garbage collection. No access specified!");
		}

		if (request.getAccess().getExternalId() == null) {
			throw new IllegalArgumentException(
					"Cannot perform garbage collection. Specified access has no externalId!");
		}

		String accessId = request.getAccess().getExternalId();

		logger.info(GMCoreTools.getDescription("Received garbage collection request for access " + accessId + ":",
				request));

		PersistenceGmSession actionSession = context.getSession();

		PersistenceGmSession garbageCollectionSession = null;
		try {
			garbageCollectionSession = this.sessionFactory.newSession(accessId);
		} catch (Exception e) {
			throw new ServiceProcessorException("Error while getting session for access "
					+ request.getAccess().getExternalId() + "!", e);
		}

		
		try {
			GarbageCollectionReport report = performGarbageCollection(garbageCollectionSession, request.getUseCases(),
					request.getTestModeEnabled());
			GarbageCollectionReportSettings reportSettings = new GarbageCollectionReportSettings();

			reportSettings.setListIndividualEntities(false);
			String detailedMessage = report.createReport(reportSettings);
			reportSettings.setListIndividualEntities(true);
			String reportFileContent = report.createReport(reportSettings);
			Resource reportResource = createGarbageCollectionReportResource(reportFileContent, actionSession);
			
			
			
			GarbageCollectionResponse response = GarbageCollectionResponse.T.create();
			response.setReport(reportResource);

			response.setNotifications(
				Notifications.build()
					.add()
						.message().info("Garbage collection performed on access: "+accessId,detailedMessage)
					.close()
					.add()
						.command().gotoModelPath("GC Report").addElement(reportResource).close()
					.close()
				.list()
			);
			
			return response;
			
		} catch (Exception e) {
			throw new ServiceProcessorException("Error while performing garbage collection on access " + accessId,e);
		}
	}
	
	private GarbageCollectionReport performGarbageCollection(PersistenceGmSession garbageCollectionSession,
			List<String> usesCases, boolean testModeEnabled) {
		GmMetaModel metaModel = garbageCollectionSession.getModelAccessory().getModel();
		List<SubsetConfiguration> subsetConfigurations = new ArrayList<SubsetConfiguration>();

		if (CommonTools.isEmpty(usesCases)) {
			usesCases = new ArrayList<String>();
			usesCases.add(null); // whole set
		}

		for (String useCase : usesCases) {
			// root types are the types from which GC should start its reachability walks
			final Set<String> rootTypes = GarbageCollectionMetaDataBasedEntitiesFinder.findEntityTypes(metaModel,
					GarbageCollectionKind.anchor, useCase);
			/*
			 * subset types include a) types that may be removed by GC (unless reachable from root types) and b) the
			 * root types themselves.
			 */
			final Set<String> subsetTypes = GarbageCollectionMetaDataBasedEntitiesFinder.findEntityTypes(metaModel,
					GarbageCollectionKind.collect, useCase);
			subsetTypes.addAll(rootTypes);

			String subsetId = useCase == null ? "<whole set>" : useCase;

			SubsetConfiguration subsetConfiguration = new SubsetConfiguration(subsetId,
					new EntityTypeBasedEntitiesFinder(rootTypes), new EntityTypeBasedEntitiesFinder(subsetTypes));
			subsetConfigurations.add(subsetConfiguration);
		}

		GarbageCollectionReport report = garbageCollection.performGarbageCollection(garbageCollectionSession,
				subsetConfigurations, testModeEnabled);
		return report;
	}

	private static Resource createGarbageCollectionReportResource(String reportFileContent, PersistenceGmSession resourceSession) {
		String resourceName = "GarbageCollection_" + DateTools.getTimestampNumber();
		//InputStream inputStream = StringTools.toInputStream(reportFileContent);
		return resourceSession.resources().create().name(resourceName).store(() -> StringTools.toInputStream(reportFileContent));
	}

}
