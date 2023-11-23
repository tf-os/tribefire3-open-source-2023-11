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
package com.braintribe.model.processing.query.test.shortening;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.shortening.SmartShortening;
import com.braintribe.model.processing.query.test.shortening.model.AmbiguiousName;
import com.braintribe.model.processing.query.test.shortening.model.ShorteningTestModel;

/**
 * @author peter.gazdik
 */
public class SmartShorteningTests {

	private final SmartShortening signatureExpert = new SmartShortening(getModelOracle());

	@Test
	public void ambiguousName() throws Exception {
		String s1 = signatureExpert.shorten(AmbiguiousName.T.getTypeSignature());
		String s2 = signatureExpert.shorten(com.braintribe.model.processing.query.test.shortening.model.sub.AmbiguiousName.T.getTypeSignature());

		assertThat(s1).isEqualTo("model.AmbiguiousName");
		assertThat(s2).isEqualTo("sub.AmbiguiousName");
	}
	
	@Test
	public void expandAmbiguousShortennedName() throws Exception {
		String s1 = signatureExpert.expand("model.AmbiguiousName");
		String s2 = signatureExpert.expand("sub.AmbiguiousName");
		
		assertThat(s1).isEqualTo("com.braintribe.model.processing.query.test.shortening.model.AmbiguiousName");
		assertThat(s2).isEqualTo("com.braintribe.model.processing.query.test.shortening.model.sub.AmbiguiousName");
	}

	public static ModelOracle getModelOracle() {
		return new BasicModelOracle(ShorteningTestModel.raw());
	}

}
