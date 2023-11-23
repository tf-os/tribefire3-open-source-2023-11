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
package tribefire.extension.cache.integration.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.Undeploy;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import tribefire.extension.cache.CacheConstants;
import tribefire.extension.cache.model.deployment.service.CacheAspect;
import tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor;
import tribefire.extension.cache.model.deployment.service.cache2k.Cache2kCacheAspectConfiguration;
import tribefire.extension.cache.model.deployment.service.cache2k.EntryLogging;
import tribefire.extension.cache.model.deployment.service.cache2k.SimpleConstantExpiration;
import tribefire.extension.cache.model.deployment.service.demo.CacheDemoProcessor;
import tribefire.extension.cache.model.service.admin.CacheAdmin;
import tribefire.extension.cache.model.service.admin.CacheStatus;
import tribefire.extension.cache.model.service.admin.CacheStatusResult;
import tribefire.extension.cache.model.service.demo.CacheDemo;

public abstract class AbstractCacheTest extends AbstractTribefireQaTest {

	protected PersistenceGmSession cortexSession;

	private PersistenceGmSessionFactory sessionFactory;

	protected CacheAspect cacheAspect;
	protected CacheDemoProcessor cacheDemoProcessor;
	protected CacheAspectAdminServiceProcessor cacheAspectAdminServiceProcessor;
	protected String serviceModelGlobalId = "model:" + CacheConstants.SERVICE_MODEL_QUALIFIEDNAME;
	protected BasicModelMetaDataEditor serviceModelEditor;

	protected String tribefireCortexServiceModelGlobalId = "model:tribefire.cortex:tribefire-cortex-service-model";

	protected String processWithDemoCacheRequestGlobalId = "test.globalId.demo.cache";
	protected String processWithLocalCacheAdminGlobalId = "test.globalId.admin.cache";
	protected String aroundProcessWithCacheAspectGlobalId = "test.globalId.aspect.cache";

	// -----------------------------------------------------------------------
	// CLASS - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@BeforeClass
	public static void beforeClass() throws Exception {
		// nothing so far
	}

	@AfterClass
	public static void afterClass() throws Exception {
		// nothing so far
	}

	// -----------------------------------------------------------------------
	// TEST - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		sessionFactory = apiFactory().buildSessionFactory();
		cortexSession = sessionFactory.newSession("cortex");

		GmMetaModel serviceModel = cortexSession.query().findEntity(serviceModelGlobalId);
		serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected CacheStatusResult cacheStatus() {
		CacheStatus cacheStatus = CacheStatus.T.create();
		CacheStatusResult cacheStatusResult = cacheStatus.eval(cortexSession).get();
		return cacheStatusResult;
	}

	protected CacheAspect testCacheAspect() {
		cacheAspect = cortexSession.create(CacheAspect.T);
		cacheAspect.setAutoDeploy(true);
		cacheAspect.setName("TestCacheAspect");
		cacheAspect.setExternalId("cache.test.aspect");

		cacheAspect.setConfiguration(cache2kCacheAspectConfiguration());

		deploy(cacheAspect);

		return cacheAspect;
	}

	protected CacheDemoProcessor testCacheDemoProcessor() {
		cacheDemoProcessor = cortexSession.create(CacheDemoProcessor.T);
		cacheDemoProcessor.setAutoDeploy(true);
		cacheDemoProcessor.setName("TestCacheDemoProcessor");
		cacheDemoProcessor.setExternalId("cache.test.demo.service.processor");

		serviceModelEditor.onEntityType(CacheAdmin.T).removeMetaData((md) -> md.getGlobalId() == processWithDemoCacheRequestGlobalId);
		serviceModelEditor.onEntityType(CacheAdmin.T).removeMetaData((md) -> md.getGlobalId() == aroundProcessWithCacheAspectGlobalId);
		serviceModelEditor.onEntityType(CacheDemo.T).addMetaData(processWithDemoCacheRequest());
		serviceModelEditor.onEntityType(CacheDemo.T).addMetaData(aroundProcessWithCacheAspect());

		deploy(cacheDemoProcessor);

		return cacheDemoProcessor;
	}

	protected CacheAspectAdminServiceProcessor testCacheAdminServiceProcessor() {
		cacheAspectAdminServiceProcessor = cortexSession.create(CacheAspectAdminServiceProcessor.T);
		cacheAspectAdminServiceProcessor.setAutoDeploy(true);
		cacheAspectAdminServiceProcessor.setName("TestCacheAspectAdminServiceProcessor");
		cacheAspectAdminServiceProcessor.setExternalId("cache.aspect.admin.service.processor");

		serviceModelEditor.onEntityType(CacheAdmin.T).removeMetaData((md) -> md.getGlobalId() == processWithLocalCacheAdminGlobalId);
		serviceModelEditor.onEntityType(CacheAdmin.T).addMetaData(processWithLocalCacheAdmin());

		deploy(cacheAspectAdminServiceProcessor);

		return cacheAspectAdminServiceProcessor;
	}

	protected void deploy(Deployable... deployables) {
		cortexSession.commit();
		Set<String> externalIds = Arrays.stream(deployables).map(d -> d.getExternalId()).collect(Collectors.toSet());
		if (externalIds.isEmpty()) {
			return;
		}
		Deploy deploy = Deploy.T.create();
		deploy.setExternalIds(externalIds);
		deploy.eval(cortexSession).get();

		notifyChange();
	}

	protected void undeploy(Deployable... deployables) {
		cortexSession.commit();
		Set<String> externalIds = Arrays.stream(deployables).map(d -> d.getExternalId()).collect(Collectors.toSet());
		if (externalIds.isEmpty()) {
			return;
		}
		Undeploy undeploy = Undeploy.T.create();
		undeploy.setExternalIds(externalIds);
		undeploy.eval(cortexSession).get();

		notifyChange();
	}

	protected void deleteAll() {
		List<CacheAspect> cacheAspects = cortexSession.query().entities(EntityQueryBuilder.from(CacheAspect.T).done()).list();
		List<CacheAspectAdminServiceProcessor> cacheAspectAdminServiceProcessor = cortexSession.query()
				.entities(EntityQueryBuilder.from(CacheAspectAdminServiceProcessor.T).done()).list();
		List<CacheDemoProcessor> cacheDemoProcessor = cortexSession.query().entities(EntityQueryBuilder.from(CacheDemoProcessor.T).done()).list();

		undeploy(cacheAspects.stream().toArray(Deployable[]::new));
		undeploy(cacheAspectAdminServiceProcessor.stream().toArray(Deployable[]::new));
		undeploy(cacheDemoProcessor.stream().toArray(Deployable[]::new));

		cacheAspects.forEach(d -> cortexSession.deleteEntity(d, DeleteMode.dropReferences));
		cacheAspectAdminServiceProcessor.forEach(d -> cortexSession.deleteEntity(d, DeleteMode.dropReferences));
		cacheDemoProcessor.forEach(d -> cortexSession.deleteEntity(d, DeleteMode.dropReferences));

		Set<String> globalIds = new HashSet<>();
		globalIds.add(processWithDemoCacheRequestGlobalId);
		globalIds.add(processWithLocalCacheAdminGlobalId);
		globalIds.add(aroundProcessWithCacheAspectGlobalId);

		globalIds.forEach(globalId -> {
			GenericEntity entity = cortexSession.query().findEntity(globalId);

			if (entity != null) {
				cortexSession.deleteEntity(entity, DeleteMode.dropReferences);
			}
		});

		cortexSession.commit();
	}

	protected void notifyChange() {
		GmMetaModel tribefireCortexServiceModel = cortexSession.query().getEntity(tribefireCortexServiceModelGlobalId);

		Set<GmMetaModel> models = new HashSet<>();
		models.add(tribefireCortexServiceModel);

		models.forEach(model -> {
			NotifyModelChanged request = NotifyModelChanged.T.create();
			request.setModel(model);
			request.eval(cortexSession).get();
		});
	}

	// -----------------------------------------------------------------------
	// PRIVATE HELPER METHODS
	// -----------------------------------------------------------------------

	private Cache2kCacheAspectConfiguration cache2kCacheAspectConfiguration() {
		LogLevel logLevel = LogLevel.INFO;

		EntryLogging entryLogging = cortexSession.create(EntryLogging.T);
		entryLogging.setAsync(false);
		entryLogging.setLogLevel(logLevel);

		SimpleConstantExpiration expiration = cortexSession.create(SimpleConstantExpiration.T);

		Cache2kCacheAspectConfiguration bean = cortexSession.create(Cache2kCacheAspectConfiguration.T);

		bean.setExpiration(expiration);
		bean.setCreateEntryLogging(entryLogging);
		bean.setExpireEntryLogging(logLevel);
		bean.setRemoveEntryLogging(entryLogging);
		bean.setUpdateEntryLogging(entryLogging);

		return bean;
	}

	private ProcessWith processWithDemoCacheRequest() {
		Objects.requireNonNull(cacheDemoProcessor, "DemoProcessor must be set");
		ProcessWith bean = cortexSession.create(ProcessWith.T);
		bean.setGlobalId(processWithDemoCacheRequestGlobalId);
		bean.setProcessor(cacheDemoProcessor);
		return bean;
	}

	private ProcessWith processWithLocalCacheAdmin() {
		Objects.requireNonNull(cacheAspectAdminServiceProcessor, "AdminProcessor must be set");
		ProcessWith bean = cortexSession.create(ProcessWith.T);
		bean.setGlobalId(processWithLocalCacheAdminGlobalId);
		bean.setProcessor(cacheAspectAdminServiceProcessor);
		return bean;
	}

	private AroundProcessWith aroundProcessWithCacheAspect() {
		Objects.requireNonNull(cacheAspect, "CacheAspect must be set");
		AroundProcessWith bean = cortexSession.create(AroundProcessWith.T);
		bean.setGlobalId(aroundProcessWithCacheAspectGlobalId);
		bean.setProcessor(cacheAspect);
		return bean;
	}
}
