// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2022 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================

package com.braintribe.build.cmd.assets.impl;

import org.junit.Test;

import com.braintribe.model.platform.setup.api.IncrementRevisions;
import com.braintribe.testing.test.AbstractTest;

/**
 * Provides {@link IncrementRevisionsProcessor} tests.
 *
 * @author michael.lafite
 */
public class IncrementRevisionsProcessorTest extends AbstractTest {

	@Test
	public void testUpdateOldPoms() {
		String inputSubfolderName = "valid-old-poms";
		IncrementRevisions request = copyGroupFolderAndCreateRequest(inputSubfolderName);
		request.setDelta(10);
		IncrementRevisionsProcessor.process(request);
		assertThatActualMatchesExpectedFolder(inputSubfolderName);
	}

	@Test
	public void testUpdateNewPoms() {
		String inputSubfolderName = "valid-new-poms";
		IncrementRevisions request = copyGroupFolderAndCreateRequest(inputSubfolderName);
		request.setDelta(10);
		IncrementRevisionsProcessor.process(request);
		assertThatActualMatchesExpectedFolder(inputSubfolderName);
	}

	private IncrementRevisions copyGroupFolderAndCreateRequest(String inputSubfolderName) {
		return UpdateGroupVersionProcessorTest.copyGroupFolderAndCreateRequest(getClass(), IncrementRevisions.T, inputSubfolderName);
	}

	void assertThatActualMatchesExpectedFolder(String inputSubfolderName) {
		UpdateGroupVersionProcessorTest.assertThatActualMatchesExpectedFolder(getClass(), inputSubfolderName);
	}
}
