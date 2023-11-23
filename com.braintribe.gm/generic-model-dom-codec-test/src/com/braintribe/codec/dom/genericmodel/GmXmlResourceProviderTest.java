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
package com.braintribe.codec.dom.genericmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.utils.FileTools;

/**
 * Provides tests for {@link GmXmlResourceProviderTest}.
 * 
 * @author michael.lafite
 */
public class GmXmlResourceProviderTest {

	
	@Test @Category(KnownIssue.class)
	public void testReadMetaModel() throws RuntimeException {

		File metamodelFile = new File("res/metamodel.xml");
		URL metamodelURL = FileTools.toURL(metamodelFile);

		GmXmlResourceProvider<GmMetaModel> provider = new GmXmlResourceProvider<GmMetaModel>();
		provider.setResource(metamodelURL);
		GmMetaModel metamodel = provider.get();

		assertThat(metamodel).isNotNull();
	}

}
