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
package com.braintribe.testing.internal.tribefire.tests;

import java.util.Collection;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ImpApiFactory;
import com.braintribe.testing.test.AbstractTest;

public abstract class AbstractTribefireQaTest extends AbstractTest {

	protected static ImpApiFactory apiFactory() {
		return ImpApiFactory.with();
	}

	protected String testId() {
		return "tfqa_" + this.getClass().getSimpleName() + "_";
	}

	protected String name(String suffix) {
		return testId() + suffix;
	}

	protected String nameWithTimestamp(String suffix) {
		return testId() + getTimeStamp() + suffix;
	}
	
	private String modelPrefix() {
		return "T" + testId(); // The model name needs to start with a capital letter. Hence the "T" to assure that regardless of testId
	}

	protected String modelName() {
		return groupId() + ":" + modelPrefix() + "Model";
	}

	protected String modelName(String infix) {
		return groupId() + ":" + modelPrefix() + infix + "Model";
	}

	protected String modelNameWithTimestamp() {
		return groupId() + ":" + modelPrefix() + getTimeStamp() + "Model";
	}

	protected String modelNameWithTimestamp(String infix) {
		return groupId() + ":" + modelPrefix() + infix + getTimeStamp() + "Model";
	}

	protected String groupId() {
		return this.getClass().getPackage().getName();
	}

	private String getTimeStamp() {
		return "" + System.currentTimeMillis();
	}

	/**
	 * erases all models with a name created by the modelNameX() methods undeploys and deletes all deployables with an externalId created by
	 * the nameX() methods
	 */
	public void eraseTestEntities() {
		eraseTestEntities(buildImp());
	}

	public void eraseTestEntities(ImpApi imp) {
		logger.info("###### Erasing test entities containing test id " + testId() + " ######");
		
		Collection<Deployable> testDeployables = imp.deployable().findAll("*" + testId() + "*");
		if (testDeployables.size() > 0) {
			logger.info("Deleting deployables " + testDeployables);
			imp.service().undeployRequest(testDeployables).call();
			imp.deployable().with(testDeployables).delete();
		}

		imp.model().allLike("*" + testId() + "*").deleteRecursively();
		imp.commit();
	}

	static ImpApi buildImp() {
		return apiFactory().build();
	}
}
