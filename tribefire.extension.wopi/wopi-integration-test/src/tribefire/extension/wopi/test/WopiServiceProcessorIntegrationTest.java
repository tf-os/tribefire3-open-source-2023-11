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

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiStatus;
import com.braintribe.model.wopi.service.integration.AddDemoDocsResult;
import com.braintribe.model.wopi.service.integration.CloseAllWopiSessionsResult;
import com.braintribe.model.wopi.service.integration.CloseWopiSession;
import com.braintribe.model.wopi.service.integration.CloseWopiSessionResult;
import com.braintribe.model.wopi.service.integration.ExportWopiSession;
import com.braintribe.model.wopi.service.integration.ExportWopiSessionResult;
import com.braintribe.model.wopi.service.integration.FindWopiSession;
import com.braintribe.model.wopi.service.integration.FindWopiSessionResult;
import com.braintribe.model.wopi.service.integration.GetWopiResource;
import com.braintribe.model.wopi.service.integration.GetWopiResourceResult;
import com.braintribe.model.wopi.service.integration.OpenWopiSession;
import com.braintribe.model.wopi.service.integration.OpenWopiSessionResult;
import com.braintribe.model.wopi.service.integration.RemoveDemoDocsResult;
import com.braintribe.model.wopi.service.integration.RemoveWopiSession;
import com.braintribe.model.wopi.service.integration.RemoveWopiSessionResult;

/**
 * 
 *
 */
// TODO: add test with different context/user
// TODO: add test for creator of Resource
// TODO: test for first view, then edit mode and vice versa
// TODO: enable ignored tests
public class WopiServiceProcessorIntegrationTest extends AbstractWopiTest {

	// -----------------------------------------------------------------------
	// TESTS - OpenWopiSession
	// -----------------------------------------------------------------------

	@Test
	public void testOpenWopiSessionNoParameters() {

		OpenWopiSession request = OpenWopiSession.T.create();
		OpenWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();

	}
	@Test
	public void testOpenWopiSessionNoParametersCorrelationId() {
		OpenWopiSession request = OpenWopiSession.T.create();
		request.setCorrelationId(null);
		request.setResource(Resource.T.create());
		request.setAllowedRoles(ALLOWED_ROLES_ALL);
		request.setDocumentMode(DocumentMode.view);
		OpenWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}
	@Test
	public void testOpenWopiSessionNoParametersResource() {
		OpenWopiSession request = OpenWopiSession.T.create();
		request.setCorrelationId(CORRELATION_ID_PREFIX);
		request.setResource(null);
		request.setAllowedRoles(ALLOWED_ROLES_ALL);
		request.setDocumentMode(DocumentMode.view);
		OpenWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}
	@Test
	public void testOpenWopiSessionNoParametersAllowedRoles() {
		OpenWopiSession request = OpenWopiSession.T.create();
		request.setCorrelationId(CORRELATION_ID_PREFIX);
		request.setResource(Resource.T.create());
		request.setAllowedRoles(null);
		request.setDocumentMode(DocumentMode.view);
		OpenWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}
	@Test
	public void testOpenWopiSessionNoParametersDocumentMode() {
		OpenWopiSession request = OpenWopiSession.T.create();
		request.setCorrelationId(CORRELATION_ID_PREFIX);
		request.setResource(Resource.T.create());
		request.setAllowedRoles(ALLOWED_ROLES_ALL);
		request.setDocumentMode(null);
		OpenWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();

	}

	@Test
	@Ignore
	public void testOpenWopiSessionNotSupportedMimeType() {
		OpenWopiSessionResult result = openWopiSession(NOT_SUPPORTED, DocumentMode.view);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testOpenWopiSessionWrongModeDocEdit() {
		OpenWopiSessionResult result = openWopiSession(DOC, DocumentMode.edit);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testOpenWopiSessionWrongModeXlsEdit() {
		OpenWopiSessionResult result = openWopiSession(XLS, DocumentMode.edit);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testOpenWopiSessionWrongModePptEdit() {
		OpenWopiSessionResult result = openWopiSession(PPT, DocumentMode.edit);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(OpenWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testOpenWopiSession() {
		OpenWopiSessionResult result = openWopiSessionDocView();

		assertThat(result).isNotNull();
		assertThat(result.getWopiSession()).isNotNull();
		assertThat(result.getWopiSession().getCorrelationId()).isEqualTo(correlationId(DOC.getName()));
		assertThat(result.getWopiSession().getCreationDate()).isNotNull();
		assertThat(result.getWopiSession().getDocumentMode()).isEqualTo(DocumentMode.view);
		assertThat(result.getWopiSession().getStatus()).isEqualTo(WopiStatus.open);
		assertThat(result.getWopiSession().getTenant()).isEqualTo("default");
		assertThat(result.getWopiSession().getWopiUrl()).isNotEmpty();
		assertThat(result.getWopiSession().getAllowedRoles()).allMatch(r -> r.equals(ALL_ROLE));
		assertThat(result.getWopiSession().getBreadcrumbBrandName()).isEmpty();
		assertThat(result.getWopiSession().getBreadcrumbDocName()).isEmpty();
		assertThat(result.getWopiSession().getBreadcrumbFolderName()).isEmpty();
		assertThat(result.getWopiSession().getDisablePrint()).isFalse();
		assertThat(result.getWopiSession().getDisableTranslation()).isFalse();
		assertThat(result.getWopiSession().getLock()).isNull();
		assertThat(result.getWopiSession().getMaxVersions()).isGreaterThan(0);
		assertThat(result.getWopiSession().getShowBreadcrumbBrandName()).isFalse();
		assertThat(result.getWopiSession().getShowBreadcrumbDocName()).isTrue();
		assertThat(result.getWopiSession().getShowBreadcrumbFolderName()).isFalse();
		assertThat(result.getWopiSession().getShowUserFriendlyName()).isTrue();
		assertThat(result.getWopiSession().getWopiLockExpirationInMs()).isGreaterThan(0);
		assertThat(result.getWopiSession().getAccessTokens()).isEmpty();
		assertThat(result.getWopiSession().getResourceVersions()).isEmpty();

		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testOpenWopiSessionExistingOpen() {
		openWopiSessionDocView();
		OpenWopiSessionResult result = openWopiSessionDocView();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testOpenWopiSessionExistingExpired() {
		resetWopiApp(1);

		openWopiSessionDocView();

		simulateExpireWopiSession();

		OpenWopiSessionResult result = openWopiSessionDocView();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNotNull();
		assertThat(result.getNotifications()).hasSize(0);
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testOpenWopiSessionExistingClosed() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();

		simulateCleanupWopiSession();

		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testOpenWopiSessionDuplicate() {
		OpenWopiSessionResult result1 = openWopiSessionDocView();
		assertThat(result1).isNotNull();
		assertThat(result1.getFailure()).isNull();
		assertThat(result1.getWopiSession()).isNotNull();

		OpenWopiSessionResult result2 = openWopiSession(DOC.getName(), DOC, DocumentMode.view, ALLOWED_ROLES_ALL, true);
		assertThat(result2).isNotNull();
		assertThat(result2.getFailure()).isNotNull();
		assertThat(result2.getNotifications()).hasSize(1);
		assertThat(result2.getWopiSession()).isNull();
	}

	@Test
	@Ignore
	public void testOpenWopiSessionBigDocxView() {
		File file = TestFileCreator.createDocx(40);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionBigDocxEdit() {
		File file = TestFileCreator.createDocx(40);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionTooBigDocxView() {
		File file = TestFileCreator.createDocx(50);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertFailedOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionTooBigDocxEdit() {
		File file = TestFileCreator.createDocx(50);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertFailedOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionBigXlsxView() {
		File file = TestFileCreator.createDocx(4);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionBigXlsxEdit() {
		File file = TestFileCreator.createDocx(4);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionTooBigXlsxView() {
		File file = TestFileCreator.createXlsx(5);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertFailedOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionTooBigXlsxEdit() {
		File file = TestFileCreator.createXlsx(5);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertFailedOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionBigPptxView() {
		File file = TestFileCreator.createPptx(290);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionBigPptxEdit() {
		File file = TestFileCreator.createPptx(290);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionTooBigPptxView() {
		File file = TestFileCreator.createPptx(350);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.view);

		assertFailedOpenWopiSession(result);
	}

	@Test
	@Ignore
	public void testOpenWopiSessionTooBigPptxEdit() {
		File file = TestFileCreator.createPptx(350);
		OpenWopiSessionResult result = openWopiSession(file, DocumentMode.edit);

		assertFailedOpenWopiSession(result);
	}

	// -----------------------------------------------------------------------
	// TESTS - FindWopiSession
	// -----------------------------------------------------------------------

	@Test
	public void testFindWopiSessionNoParameters() {

		FindWopiSession request = FindWopiSession.T.create();
		FindWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(FindWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testFindWopiSession() {

		String correlationId = openWopiSessionDocView().getWopiSession().getCorrelationId();

		FindWopiSessionResult result = findWopiSession(correlationId, true);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNotNull();
		assertThat(result.getWopiSession().getCorrelationId()).isEqualTo(correlationId);
		assertThat(genericEntities()).isNotEmpty();
		downloadCurrentResource(result.getWopiSession());
	}

	@Test
	public void testFindWopiSessionNotExisting() {

		FindWopiSessionResult result = findWopiSession("definitelyNotExisting", false);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testFindWopiSessionNoPermissions() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		FindWopiSessionResult result = findWopiSession(correlationId, false);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testFindWopiSessionExpired() {
		resetWopiApp(1);

		String correlationId = openWopiSessionDocView().getWopiSession().getCorrelationId();

		simulateExpireWopiSession();

		FindWopiSessionResult result = findWopiSession(correlationId, false);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testFindWopiSessionClosed() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();

		FindWopiSessionResult result = findWopiSession(correlationId, false);
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getWopiSession()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	// ---------------------
	// FILE TYPES
	// ---------------------

	@Test
	public void testOpenWopiSessionCsvView() {
		OpenWopiSessionResult result = openWopiSessionCsvView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionOdsView() {
		OpenWopiSessionResult result = openWopiSessionOdsView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsView() {
		OpenWopiSessionResult result = openWopiSessionXlsView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsbView() {
		OpenWopiSessionResult result = openWopiSessionXlsbView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsmView() {
		OpenWopiSessionResult result = openWopiSessionXlsmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsxView() {
		OpenWopiSessionResult result = openWopiSessionXlsxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionOdsEdit() {
		OpenWopiSessionResult result = openWopiSessionOdsEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsbEdit() {
		OpenWopiSessionResult result = openWopiSessionXlsbEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsmEdit() {
		OpenWopiSessionResult result = openWopiSessionXlsmEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionXlsxEdit() {
		OpenWopiSessionResult result = openWopiSessionXlsxEdit();

		assertOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionOdpView() {
		OpenWopiSessionResult result = openWopiSessionOdpView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPotView() {
		OpenWopiSessionResult result = openWopiSessionPotView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPotmView() {
		OpenWopiSessionResult result = openWopiSessionPotmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPotxView() {
		OpenWopiSessionResult result = openWopiSessionPotxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPpsView() {
		OpenWopiSessionResult result = openWopiSessionPpsView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPpsmView() {
		OpenWopiSessionResult result = openWopiSessionPpsmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPpsxView() {
		OpenWopiSessionResult result = openWopiSessionPpsxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPptView() {
		OpenWopiSessionResult result = openWopiSessionPptView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPptmView() {
		OpenWopiSessionResult result = openWopiSessionPptmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPptxView() {
		OpenWopiSessionResult result = openWopiSessionPptxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionOdpEdit() {
		OpenWopiSessionResult result = openWopiSessionOdpEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPpsxEdit() {
		OpenWopiSessionResult result = openWopiSessionPpsxEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionPptxEdit() {
		OpenWopiSessionResult result = openWopiSessionPptxEdit();

		assertOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionVsdView() {
		OpenWopiSessionResult result = openWopiSessionVsdView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionVsdmView() {
		OpenWopiSessionResult result = openWopiSessionVsdmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionVsdxView() {
		OpenWopiSessionResult result = openWopiSessionVsdxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionVsdxEdit() {
		OpenWopiSessionResult result = openWopiSessionVsdxEdit();

		assertOpenWopiSession(result);
	}

	@Test
	public void testOpenWopiSessionDocView() {
		OpenWopiSessionResult result = openWopiSessionDocView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDocmView() {
		OpenWopiSessionResult result = openWopiSessionDocmView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDocxView() {
		OpenWopiSessionResult result = openWopiSessionDocxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDotView() {
		OpenWopiSessionResult result = openWopiSessionDotView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDotxView() {
		OpenWopiSessionResult result = openWopiSessionDotxView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionOdtView() {
		OpenWopiSessionResult result = openWopiSessionOdtView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionRtfView() {
		OpenWopiSessionResult result = openWopiSessionRtfView();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDocmEdit() {
		OpenWopiSessionResult result = openWopiSessionDocmEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionDocxEdit() {
		OpenWopiSessionResult result = openWopiSessionDocxEdit();

		assertOpenWopiSession(result);
	}
	@Test
	public void testOpenWopiSessionOdtEdit() {
		OpenWopiSessionResult result = openWopiSessionOdtEdit();

		assertOpenWopiSession(result);
	}

	// -----------------------------------------------------------------------
	// TESTS - GetWopiResource
	// -----------------------------------------------------------------------

	@Test
	public void testGetWopiResourceNoParameters() {

		GetWopiResource request = GetWopiResource.T.create();
		GetWopiResourceResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(GetWopiResourceResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testGetWopiResource() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		GetWopiResourceResult result = getWopiResource(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getCurrentResource()).isNotNull();
		assertThat(genericEntities()).isNotEmpty();
		downloadResource(result.getCurrentResource());
	}

	@Test
	public void testGetWopiResourceNotExisting() {
		GetWopiResourceResult result = getWopiResource("definitelyNotExisting");

		assertThat(result).isNotNull();
		assertThat(result.getCurrentResource()).isNull();
		assertThat(genericEntities()).isEmpty();

	}

	@Test
	public void testGetWopiResourceNoPermissions() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		GetWopiResourceResult result = getWopiResource(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getCurrentResource()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testGetWopiResourceExpired() {
		resetWopiApp(1);

		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		simulateExpireWopiSession();

		GetWopiResourceResult result = getWopiResource(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getCurrentResource()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testGetWopiResourceClosed() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();

		GetWopiResourceResult result = getWopiResource(correlationId);
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getCurrentResource()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - CloseWopiSession
	// -----------------------------------------------------------------------

	@Test
	public void testCloseWopiSessionNoParameters() {

		CloseWopiSession request = CloseWopiSession.T.create();
		CloseWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(CloseWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testCloseWopiSession() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		CloseWopiSessionResult result = closeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseWopiSessionNotExisting() {
		CloseWopiSessionResult result = closeWopiSession("definitelyNotExisting");

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testCloseWopiSessionNoPermissions() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		CloseWopiSessionResult result = closeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseWopiSessionExpired() {
		resetWopiApp(1);

		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		simulateExpireWopiSession();

		CloseWopiSessionResult result = closeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseWopiSessionClosed() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - RemoveWopiSession
	// -----------------------------------------------------------------------

	@Test
	public void testRemoveWopiSessionNoParameters() {
		RemoveWopiSession request = RemoveWopiSession.T.create();
		RemoveWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNotNull();
		assertThat(result).isInstanceOf(RemoveWopiSessionResult.class);
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testRemoveWopiSession() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		RemoveWopiSessionResult result = removeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(queryWopiSession(openWopiSessionResult.getWopiSession().getCorrelationId())).isNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testRemoveWopiSessionNotExisting() {
		RemoveWopiSessionResult result = removeWopiSession("definitelyNotExisting");

		assertThat(result).isNotNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testRemoveWopiSessionNoPermissions() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		RemoveWopiSessionResult result = removeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testRemoveWopiSessionExpired() {
		resetWopiApp(1);

		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		simulateExpireWopiSession();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		RemoveWopiSessionResult result = removeWopiSession(correlationId);

		assertThat(result).isNotNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testRemoveWopiSessionClosed() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		closeWopiSession(correlationId);

		assertThat(genericEntities()).isNotEmpty();

		removeWopiSession(correlationId);

		assertThat(genericEntities()).isEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - CloseAllWopiSessions
	// -----------------------------------------------------------------------

	@Test
	public void testCloseAllWopiSessions() {
		openWopiSessionDocView();

		CloseAllWopiSessionsResult result = closeAllWopiSessions();
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseAllWopiSessionsNoPermissions() {
		openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		CloseAllWopiSessionsResult result = closeAllWopiSessions();
		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseAllWopiSessionsExpired() {
		resetWopiApp(1);

		openWopiSessionDocView();

		simulateExpireWopiSession();

		CloseAllWopiSessionsResult result = closeAllWopiSessions();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(genericEntities()).isNotEmpty();
	}

	@Test
	public void testCloseAllWopiSessionsClosed() {
		openWopiSessionDocView();

		closeAllWopiSessions();

		assertThat(genericEntities()).isNotEmpty();

		closeAllWopiSessions();

		assertThat(genericEntities()).isNotEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - RemoveAllWopiSessions
	// -----------------------------------------------------------------------

	@Test
	public void testRemoveAllWopiSessionsNoForceDefaultContext() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		removeallWopiSessions(false, CONTEXT_DEFAULT);
		FindWopiSessionResult findWopiSession = findWopiSession(correlationId, true);
		assertThat(openWopiSessionResult).isNotNull();
		assertThat(findWopiSession).isNotNull();
		assertThat(findWopiSession.getFailure()).isNull();
		assertThat(findWopiSession.getWopiSession()).isNotNull();
		assertThat(findWopiSession.getWopiSession().getCorrelationId()).isEqualTo(correlationId);
		assertThat(genericEntities()).isNotEmpty();
		downloadCurrentResource(findWopiSession.getWopiSession());
	}

	@Test
	public void testRemoveAllWopiSessionsForceDefaultContext() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		removeallWopiSessions(true, CONTEXT_DEFAULT);
		FindWopiSessionResult findWopiSession = findWopiSession(correlationId, true);
		assertThat(openWopiSessionResult).isNotNull();
		assertThat(findWopiSession).isNotNull();
		assertThat(findWopiSession.getFailure()).isNull();
		assertThat(findWopiSession.getWopiSession()).isNull();
		assertThat(genericEntities()).isEmpty();
	}

	@Test
	public void testRemoveAllWopiSessionsNoForceOtherContext() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		removeallWopiSessions(false, "definitelyNotExisting");
		FindWopiSessionResult findWopiSession = findWopiSession(correlationId, true);
		assertThat(openWopiSessionResult).isNotNull();
		assertThat(findWopiSession).isNotNull();
		assertThat(findWopiSession.getFailure()).isNull();
		assertThat(findWopiSession.getWopiSession()).isNotNull();
		assertThat(findWopiSession.getWopiSession().getCorrelationId()).isEqualTo(correlationId);
		assertThat(genericEntities()).isNotEmpty();
		downloadCurrentResource(findWopiSession.getWopiSession());
	}

	@Test
	public void testRemoveAllWopiSessionsForceOtherContext() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSessionDocView();

		String correlationId = openWopiSessionResult.getWopiSession().getCorrelationId();

		removeallWopiSessions(true, "definitelyNotExisting");
		FindWopiSessionResult findWopiSession = findWopiSession(correlationId, true);
		assertThat(openWopiSessionResult).isNotNull();
		assertThat(findWopiSession).isNotNull();
		assertThat(findWopiSession.getFailure()).isNull();
		assertThat(findWopiSession.getWopiSession()).isNotNull();
		assertThat(findWopiSession.getWopiSession().getCorrelationId()).isEqualTo(correlationId);
		assertThat(genericEntities()).isNotEmpty();
		downloadCurrentResource(findWopiSession.getWopiSession());
	}

	@Test
	public void testRemoveAllWopiSessionsNoPermission() {
		OpenWopiSessionResult openWopiSessionResult = openWopiSession(DOC, DocumentMode.view, asSet("definitelyNotExisting"));

		removeallWopiSessions(true, CONTEXT_DEFAULT);
		assertThat(openWopiSessionResult).isNotNull();
		assertThat(genericEntities()).isNotEmpty(); // this includes the WopiSession created
	}

	// -----------------------------------------------------------------------
	// TESTS - ExportWopiSession
	// -----------------------------------------------------------------------

	@Test
	public void testExportWopiSession() {
		OpenWopiSessionResult openWopiSessionDocxEdit = openWopiSessionDocxEdit();

		ExportWopiSession request = ExportWopiSession.T.create();
		request.setWopiSessions(asSet(openWopiSessionDocxEdit.getWopiSession()));
		ExportWopiSessionResult result = request.eval(session).get();

		assertThat(result).isNotNull();
		assertThat(result.getFailure()).isNull();
		assertThat(result.getExportPackage()).isNotNull();
	}

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// TESTS - AddDemoDocs
	// -----------------------------------------------------------------------

	@Test
	public void testAddDemoDocs() {
		AddDemoDocsResult addDemoDocs = addDemoDocs();

		assertThat(addDemoDocs).isNotNull();
		assertThat(addDemoDocs.getWopiSessions().size()).isEqualTo(9);
		assertThat(genericEntities()).isNotEmpty();
	}

	// -----------------------------------------------------------------------
	// TESTS - RemoveDemoDocs
	// -----------------------------------------------------------------------

	@Test
	public void testRemoveDemoDocs() {
		addDemoDocs();
		RemoveDemoDocsResult removeDemoDocs = removeDemoDocs();

		assertThat(removeDemoDocs).isNotNull();
		assertThat(genericEntities()).isEmpty();
	}

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// OpenWopiDocument
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// DownloadCurrentResource
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// WopiHealthCheck
	// -----------------------------------------------------------------------

}
