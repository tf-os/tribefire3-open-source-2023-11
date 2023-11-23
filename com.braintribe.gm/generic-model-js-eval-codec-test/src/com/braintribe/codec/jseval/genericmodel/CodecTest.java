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
package com.braintribe.codec.jseval.genericmodel;

import java.util.Date;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.testing.test.AbstractTest;

public class CodecTest extends AbstractTest {
	@Test
	public void test() {
		AbsenceInformation ai = GMF.absenceInformation();
		ai.setSize(50);

		SomeEntity someEntity = SomeEntity.T.create();
		someEntity.entityType().getProperty("hint").setAbsenceInformation(someEntity, ai);
		someEntity.setName("noname");
		someEntity.setCreated(new Date());
		someEntity.setHint("don't look back");
		long s, e;

		s = System.currentTimeMillis();

		// JavaScriptPrototypes prototypes = new CondensedJavaScriptPrototypes();
		JavaScriptPrototypes prototypes = new PrettyJavaScriptPrototypes();
		GenericModelJsEvalCodec<SomeEntity> codec = new GenericModelJsEvalCodec<SomeEntity>();
		codec.setPrototypes(prototypes);
		// codec.setHostedMode(true);
		String js = codec.encode(someEntity);
		e = System.currentTimeMillis();
		logger.info(js);
		logger.info("full time: " + (e - s));

		Set<String> typeNames = JseUtil.extractTypes(js);
		logger.info("Type names: " + typeNames);
	}

	public static interface SomeEntity extends GenericEntity {
		final EntityType<SomeEntity> T = EntityTypes.T(SomeEntity.class);

		// @formatter:off
		Date getCreated();
		void setCreated(Date created);

		String getName();
		void setName(String name);

		String getHint();
		void setHint(String hint);
		// @formatter:on
	}
}
