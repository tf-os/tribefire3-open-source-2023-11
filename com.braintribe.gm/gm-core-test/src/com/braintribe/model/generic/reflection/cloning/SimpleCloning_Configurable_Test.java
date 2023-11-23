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
package com.braintribe.model.generic.reflection.cloning;

import org.junit.Test;

import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.cloning.model.City;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.session.impl.notifying.BasicNotifyingGmSession;

/**
 * @author peter.gazdik
 */
public class SimpleCloning_Configurable_Test extends SimpleCloning_Base {

	@Test
	@Override
	public void simplyCopying() {
		cc = new ConfigurableCloningContext();

		runSimplyCopying();
	}

	@Test
	@Override
	public void copyOnASession() {
		GmSession session = new BasicNotifyingGmSession();

		cc = ConfigurableCloningContext.build().supplyRawCloneWith(session).done();

		runCopyOnASession(session);
	}

	@Test
	@Override
	public void doNotCopyIdStuff() {
		cc = ConfigurableCloningContext.build().skipIndentifying(true).skipGlobalId(true).done();

		runDoNotCopyIdStuff();
	}

	@Test
	@Override
	public void referenceOriginalPropertyValue() {
		cc = ConfigurableCloningContext.build().withAssociatedResolver(e -> e instanceof City ? e : null).done();

		runReferenceOriginalPropertyValue();
	}

	@Test
	@Override
	public void stringifyIdInPreProcess() {
		cc = ConfigurableCloningContext.build().withOriginPreProcessor(super::stringifyId).done();

		runStringifyIdInPreProcess();
	}

	@Test
	@Override
	public void stringifyIdInPostProcess() {
		cc = ConfigurableCloningContext.build().withClonedValuePostProcesor((type, o) -> o instanceof Long ? "" + o : o).done();

		runStringifyIdInPostProcess();
	}

}
