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
package tribefire.extension.wopi.test;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.wopi.WopiQueryingUtil;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.service.WopiApp;
import com.braintribe.model.wopi.service.integration.AddDemoDocs;
import com.braintribe.model.wopi.service.integration.AddDemoDocsResult;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessions;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.CloseWopiSession;
import com.braintribe.model.wopi.service.integration.CloseWopiSessionResult;
import com.braintribe.model.wopi.service.integration.ExpireWopiSessions;
import com.braintribe.model.wopi.service.integration.ExpireWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.FindWopiSession;
import com.braintribe.model.wopi.service.integration.FindWopiSessionResult;
import com.braintribe.model.wopi.service.integration.GetWopiResource;
import com.braintribe.model.wopi.service.integration.GetWopiResourceResult;
import com.braintribe.model.wopi.service.integration.OpenWopiSession;
import com.braintribe.model.wopi.service.integration.OpenWopiSessionResult;
import com.braintribe.model.wopi.service.integration.RemoveAllWopiSessions;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocs;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocsResult;
import com.braintribe.model.wopi.service.integration.RemoveWopiSession;
import com.braintribe.model.wopi.service.integration.RemoveWopiSessionResult;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.stream.FileStreamProviders;

import tribefire.extension.wopi.WopiConstants;

/**
 * 
 *
 */
// TODO: test that tf-admin can access everything
public abstract class AbstractWopiTest extends AbstractTribefireQaTest implements WopiConstants {

	@Rule
	public TestName name = new TestName();

	protected ImpApi impApi;
	protected PersistenceGmSession session;
	protected PersistenceGmSession cortexSession;

	protected PersistenceGmSessionFactory sessionFactory;

	protected final String CORRELATION_ID_PREFIX = "testCorrelationId_";
	protected final String ALL_ROLE = "$all";
	protected final Set<String> ALLOWED_ROLES_ALL = asSet(ALL_ROLE);

	protected final String TEST_DOC_TAG = "testDocument";

	protected final File CSV = new File("res", "demo.csv");
	protected final File ODS = new File("res", "demo.ods");
	protected final File XLS = new File("res", "demo.xls");
	protected final File XLSB = new File("res", "demo.xlsb");
	protected final File XLSM = new File("res", "demo.xlsm");
	protected final File XLSX = new File("res", "demo.xlsx");

	protected final File ODP = new File("res", "demo.odp");
	protected final File POT = new File("res", "demo.pot");
	protected final File POTM = new File("res", "demo.potm");
	protected final File POTX = new File("res", "demo.potx");
	protected final File PPS = new File("res", "demo.pps");
	protected final File PPSM = new File("res", "demo.ppsm");
	protected final File PPSX = new File("res", "demo.ppsx");
	protected final File PPT = new File("res", "demo.ppt");
	protected final File PPTM = new File("res", "demo.pptm");
	protected final File PPTX = new File("res", "demo.pptx");

	protected final File VSD = new File("res", "demo.vsd");
	protected final File VSDM = new File("res", "demo.vsdm");
	protected final File VSDX = new File("res", "demo.vsdx");

	protected final File DOC = new File("res", "demo.doc");
	protected final File DOCM = new File("res", "demo.docm");
	protected final File DOCX = new File("res", "demo.docx");
	protected final File DOCX2 = new File("res", "demo2.docx");
	protected final File DOT = new File("res", "demo.dot");
	protected final File DOTM = new File("res", "demo.dotm");
	protected final File DOTX = new File("res", "demo.dotx");
	protected final File ODT = new File("res", "demo.odt");
	protected final File RTF = new File("res", "demo.rtf");

	protected final File NOT_SUPPORTED = new File("res", "notSupported.png");

	protected static final String WOPI_ACCESS_EXTERNALID = "collaborative-smood-access.default.wopi-templates-space.smood-access";

	protected final String CONTEXT_DEFAULT = "default";

	// -----------------------------------------------------------------------
	// CLASS - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		StopWatch stopWatch = new StopWatch();
		logger.info(() -> "--> START: '" + name.getMethodName() + "'");

		impApi = apiFactory().build();
		sessionFactory = apiFactory().buildSessionFactory();
		session = sessionFactory.newSession(WOPI_ACCESS_EXTERNALID);
		cortexSession = sessionFactory.newSession(WopiConstants.ACCESS_ID_CORTEX);
		stopWatch.intermediate("create Sessions");
		cleanupAccess();
		stopWatch.intermediate("cleanup Access");
		resetWopiApp();
		stopWatch.intermediate("reset WOPI App");

		logger.info(() -> "--> Finished PREPARING: '" + name.getMethodName() + "' in '" + stopWatch.toString() + "'");
	}

	@After
	public void after() throws Exception {
		logger.info(() -> "--> FINISHED: '" + name.getMethodName() + "'");
	}

	// -----------------------------------------------------------------------
	// TEST HELPER METHODS
	// -----------------------------------------------------------------------

	protected List<GenericEntity> genericEntities() {
		List<GenericEntity> genericEntities = session.query().entities(EntityQueryBuilder.from(GenericEntity.T).done()).list();
		return genericEntities;
	}

	protected void cleanupAccess() {
		List<GenericEntity> genericEntities = genericEntities();

		for (GenericEntity genericEntity : genericEntities) {
			session.deleteEntity(genericEntity, DeleteMode.dropReferences);
		}
		session.commit();
	}

	protected AddDemoDocsResult addDemoDocs() {
		AddDemoDocs request = AddDemoDocs.T.create();
		AddDemoDocsResult result = request.eval(session).get();
		return result;
	}

	protected RemoveDemoDocsResult removeDemoDocs() {
		RemoveDemoDocs request = RemoveDemoDocs.T.create();
		RemoveDemoDocsResult result = request.eval(session).get();
		return result;
	}

	protected OpenWopiSessionResult openWopiSessionCsvView() {
		return openWopiSession(CSV, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionOdsView() {
		return openWopiSession(ODS, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionXlsView() {
		return openWopiSession(XLS, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionXlsbView() {
		return openWopiSession(XLSB, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionXlsmView() {
		return openWopiSession(XLSM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionXlsxView() {
		return openWopiSession(XLSX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionOdsEdit() {
		return openWopiSession(ODS, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionXlsbEdit() {
		return openWopiSession(XLSB, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionXlsmEdit() {
		return openWopiSession(XLSM, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionXlsxEdit() {
		return openWopiSession(XLSX, DocumentMode.edit);
	}

	protected OpenWopiSessionResult openWopiSessionOdpView() {
		return openWopiSession(ODP, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPotView() {
		return openWopiSession(POT, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPotmView() {
		return openWopiSession(POTM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPotxView() {
		return openWopiSession(POTX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPpsView() {
		return openWopiSession(PPS, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPpsmView() {
		return openWopiSession(PPSM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPpsxView() {
		return openWopiSession(PPSX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPptView() {
		return openWopiSession(PPT, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPptmView() {
		return openWopiSession(PPTM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionPptxView() {
		return openWopiSession(PPTX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionOdpEdit() {
		return openWopiSession(ODP, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionPpsxEdit() {
		return openWopiSession(PPSX, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionPptxEdit() {
		return openWopiSession(PPTX, DocumentMode.edit);
	}

	protected OpenWopiSessionResult openWopiSessionVsdView() {
		return openWopiSession(VSD, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionVsdmView() {
		return openWopiSession(VSDM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionVsdxView() {
		return openWopiSession(VSDX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionVsdxEdit() {
		return openWopiSession(VSDX, DocumentMode.edit);
	}

	protected OpenWopiSessionResult openWopiSessionDocView() {
		return openWopiSession(DOC, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDocmView() {
		return openWopiSession(DOCM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDocxView() {
		return openWopiSession(DOCX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDotView() {
		return openWopiSession(DOT, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDotmView() {
		return openWopiSession(DOTM, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDotxView() {
		return openWopiSession(DOTX, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionOdtView() {
		return openWopiSession(ODT, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionRtfView() {
		return openWopiSession(RTF, DocumentMode.view);
	}
	protected OpenWopiSessionResult openWopiSessionDocmEdit() {
		return openWopiSession(DOCM, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionDocxEdit() {
		return openWopiSession(DOCX, DocumentMode.edit);
	}
	protected OpenWopiSessionResult openWopiSessionOdtEdit() {
		return openWopiSession(ODT, DocumentMode.edit);
	}

	protected OpenWopiSessionResult openWopiSession(String correlationId, File file, DocumentMode documentMode, Set<String> allowedRoles,
			boolean sendNotifications) {
		try (InputStream is = new FileInputStream(file)) {

			Resource resource = Resource.T.create();
			resource.setName(file.getName());
			resource.setTags(asSet(TEST_DOC_TAG));
			resource.assignTransientSource(FileStreamProviders.from(is));

			OpenWopiSession request = OpenWopiSession.T.create();
			request.setSendNotifications(sendNotifications);
			request.setCorrelationId(correlationId(correlationId));
			request.setResource(resource);
			request.setAllowedRoles(allowedRoles);
			request.setDocumentMode(documentMode);

			OpenWopiSessionResult result = request.eval(session).get();
			return result;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Could not OpenWopiSession");
		}
	}

	protected OpenWopiSessionResult openWopiSession(File file, DocumentMode documentMode, Set<String> allowedRoles) {
		return openWopiSession(file.getName(), file, documentMode, allowedRoles, false);
	}

	protected OpenWopiSessionResult openWopiSession(File file, DocumentMode documentMode) {
		return openWopiSession(file, documentMode, ALLOWED_ROLES_ALL);
	}

	protected FindWopiSessionResult findWopiSession(String correlationId, boolean includeResource) {
		FindWopiSession request = FindWopiSession.T.create();
		request.setCorrelationId(correlationId);
		request.setIncludeCurrentResource(includeResource);
		FindWopiSessionResult result = request.eval(session).get();
		return result;
	}

	protected GetWopiResourceResult getWopiResource(String correlationId) {
		GetWopiResource request = GetWopiResource.T.create();
		request.setCorrelationId(correlationId);
		GetWopiResourceResult result = request.eval(session).get();
		return result;
	}

	protected CloseWopiSessionResult closeWopiSession(String correlationId) {
		CloseWopiSession request = CloseWopiSession.T.create();
		WopiSession wopiSession = WopiSession.T.create();
		wopiSession.setCorrelationId(correlationId);
		request.setWopiSession(wopiSession);
		CloseWopiSessionResult result = request.eval(session).get();
		return result;
	}

	protected CloseAllWopiSessionsResult closeAllWopiSessions() {
		CloseAllWopiSessions request = CloseAllWopiSessions.T.create();
		CloseAllWopiSessionsResult result = request.eval(session).get();
		return result;
	}

	protected RemoveWopiSessionResult removeWopiSession(String correlationId) {
		RemoveWopiSession request = RemoveWopiSession.T.create();
		WopiSession wopiSession = WopiSession.T.create();
		wopiSession.setCorrelationId(correlationId);
		request.setWopiSession(wopiSession);
		RemoveWopiSessionResult result = request.eval(session).get();
		return result;
	}

	protected List<WopiSession> queryWopiSessions() {
		List<WopiSession> queryWopiSessions = WopiQueryingUtil.queryWopiSessions(session);
		return queryWopiSessions;
	}

	protected WopiSession queryWopiSession(String correlationId) {
		WopiSession wopiSession = WopiQueryingUtil.queryWopiSession(session, correlationId);
		return wopiSession;
	}

	protected ExpireWopiSessionsResult expireWopiSessions() {
		ExpireWopiSessions request = ExpireWopiSessions.T.create();
		ExpireWopiSessionsResult result = request.eval(session).get();
		return result;
	}

	/**
	 * simulates expiration of a session - use {@link AbstractWopiTest#resetWopiApp(long)} first to reset expiration
	 */
	protected void simulateExpireWopiSession() {
		// Simulate the {@link ExpireWopiSessionWorker}
		ExpireWopiSessionsResult expireWopiSessions = expireWopiSessions();
		assertThat(expireWopiSessions).isNotNull();
	}

	protected void simulateCleanupWopiSession() {
		// Simulate the {@link CleanupWopiSessionWorker}
		RemoveAllWopiSessions request = RemoveAllWopiSessions.T.create();
		request.setForceRemove(false);
		request.setContext(CONTEXT_DEFAULT);
		request.eval(session).get();
	}

	protected void removeallWopiSessions(boolean forceRemove, String context) {
		RemoveAllWopiSessions request = RemoveAllWopiSessions.T.create();
		request.setForceRemove(forceRemove);
		request.setContext(context);
		request.eval(session).get();
	}

	protected void assertWopiSessionExisting(String correlationId) {
		FindWopiSessionResult result = findWopiSession(correlationId, false);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNotNull();
		assertThat(result.getWopiSession().getCorrelationId()).isNotNull();
	}

	protected void assertOpenWopiSession(OpenWopiSessionResult result) {
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNotNull();
		assertThat(result.getWopiSession().getCorrelationId()).isEqualTo(result.getWopiSession().getCorrelationId());
		assertThat(genericEntities()).isNotEmpty();
	}

	protected void assertFailedOpenWopiSession(OpenWopiSessionResult result) {
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isEmpty();
	}

	protected void downloadCurrentResource(WopiSession wopiSession) {
		Resource currentResource = wopiSession.getCurrentResource();
		assertThat(currentResource).isNotNull();

		downloadResource(currentResource);
	}

	protected void downloadResource(Resource resource) {
		try {
			InputStream is = session.resources().openStream(resource);

			File file = newTempFile();
			IOTools.inputToFile(is, file);

			assertThat(file).isNotNull();
			assertThat(file.length()).isGreaterThan(0);
		} catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected String correlationId(String correlationId) {
		return CORRELATION_ID_PREFIX + correlationId;
	}

	protected void resetWopiApp() {
		resetWopiApp((long) WopiApp.T.getProperty(WopiApp.wopiSessionExpirationInMs).getInitializer());
	}

	protected void resetWopiApp(long wopiSessionExpirationInMs) {
		resetWopiApp(wopiSessionExpirationInMs, (String) WopiApp.T.getProperty(WopiApp.context).getInitializer());
	}

	protected void resetWopiApp(String context) {
		resetWopiApp((long) WopiApp.T.getProperty(WopiApp.wopiSessionExpirationInMs).getInitializer(), context);
	}

	protected void resetWopiApp(long wopiSessionExpirationInMs, String context) {
		WopiApp wopiApp = cortexSession.query().entities(EntityQueryBuilder.from(WopiApp.T).done()).unique();
		wopiApp.setWopiSessionExpirationInMs(wopiSessionExpirationInMs);
		wopiApp.setContext(context);
		cortexSession.commit();

		//@formatter:off
		List<Deployable> customDeployables = cortexSession.query().entities(EntityQueryBuilder
				.from(Deployable.T)
				.where()
					.negation()
					.disjunction()
						.property(Deployable.globalId).like("hardwired:*")
						.property(Deployable.globalId).like("default:*")
					.close()
				.done())
				.list();
		//@formatter:on

		Set<String> externalIds = customDeployables.stream().map(d -> d.getExternalId()).collect(Collectors.toSet());

		Redeploy redeploy = Redeploy.T.create();
		redeploy.setExternalIds(externalIds);
		redeploy.eval(cortexSession).get();

	}
}
