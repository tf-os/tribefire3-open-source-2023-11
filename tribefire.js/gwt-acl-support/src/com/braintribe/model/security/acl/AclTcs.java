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
package com.braintribe.model.security.acl;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.acl.Acl;
import com.braintribe.model.acl.AclEntry;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;

/**
 * @author peter.gazdik
 */
public class AclTcs {

	// @formatter:off
	public static final TraversingCriterion TC_MATCHING_ACL_PROPS = TC.create() //
			.disjunction()
				.pattern()
					.typeCondition(TypeConditions.isAssignableTo(HasAcl.T))
					.disjunction()
						.property("owner")
						.property("acl")
					.close()
				.close()
				.pattern()
					.disjunction()
						.typeCondition(TypeConditions.isType(Acl.T))
						.typeCondition(TypeConditions.isAssignableTo(AclEntry.T))
					.close()
					.property()
				.close()
			.close() // disjunction
			.done();

	public static final TraversingCriterion HAS_ACL_TC = TC.create() //
										.negation() //
											.disjunction()
												.property(GenericEntity.id)
												.property(GenericEntity.partition)
												.criterion(TC_MATCHING_ACL_PROPS)
											.close()
										.done();

	public static final TraversingCriterion DEFAULT_TC_WITH_ACL = addAclEagerLoadingTo(
			TC.create().placeholder(IncrementalAccess.DEFAULT_TC_NAME).done());

	public static TraversingCriterion addAclEagerLoadingTo(TraversingCriterion tc) {
		return TC.create() //
			.conjunction()
				.criterion(tc)
				.negation() //
					.criterion(TC_MATCHING_ACL_PROPS)
				.close()
			.done();
	}
	// @formatter:on

}
