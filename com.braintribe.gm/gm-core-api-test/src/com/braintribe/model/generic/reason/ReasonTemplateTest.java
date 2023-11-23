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
package com.braintribe.model.generic.reason;

import org.assertj.core.api.Assertions;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.model.generic.reason.model.TestReason;
import com.braintribe.model.generic.reason.model.TestSubject;

public class ReasonTemplateTest {

	//@Test
	public void testTemplateReason() throws Exception {
		TestSubject subject = TestSubject.T.create();
		subject.setName("Obelix");
		
		TestReason reason = Reasons.build(TestReason.T).enrich(r -> r.setSubject(subject)).toReason();
		
		Assertions.assertThat(reason.getText()).isEqualTo("You've got problems with subject Obelix");
	}
}
