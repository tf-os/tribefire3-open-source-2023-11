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
package com.braintribe.marshaller.impl.basic.test;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.model.generic.typecondition.TypeConditions.isKind;
import static com.braintribe.model.generic.typecondition.TypeConditions.orTc;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.collectionType;
import static com.braintribe.model.generic.typecondition.basic.TypeKind.entityType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.clone.AssemblyCloning;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.resource.Resource;

public class CloningTest {
	private Marshaller xmlMarshaller;

	@Test
	public void testCloning() throws Exception {
		Object assembly = loadAssembly();
		testCloningNew(assembly);
		testCloningOld(assembly);
		testCloningNew(assembly);
		testCloningOld(assembly);
		testCloningNew(assembly);
		testCloningOld(assembly);
	}

	public void testCloningOld(Object assembly) throws Exception {
		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(createTraversingCriterion());

		GMF.getTypeReflection().getBaseType().clone(assembly, matcher, StrategyOnCriterionMatch.partialize);

		long s = System.nanoTime();

		for (int i = 0; i < 10; i++) {
			GMF.getTypeReflection().getBaseType().clone(assembly, matcher, StrategyOnCriterionMatch.partialize);
		}

		double d = (System.nanoTime() - s) / 1000000.0;

		System.out.println("old cloning took " + d + "ms");

	}

	private Object loadAssembly() throws MarshallException, FileNotFoundException {
		File file = new File("current.xml");

		Object assembly1 = getXmlMarshaller().unmarshall(new FileInputStream(file));
		return assembly1;
	}

	public void testCloningNew(Object assembly) throws Exception {

		AssemblyCloning.builder().tc(createTraversingCriterion()).clone(assembly);

		long s = System.nanoTime();

		for (int i = 0; i < 10; i++) {
			AssemblyCloning.builder().directPropertyWrite(true).tc(createTraversingCriterion()).clone(assembly);
		}

		double d = (System.nanoTime() - s) / 1000000.0;

		System.out.println("new cloning took " + d + "ms");
	}

	private TraversingCriterion createTraversingCriterion() {
		//return TC.create().pattern().entity(GmEntityType.class.getName()).property("properties").close().done();
		//if (true) return null;
		// @formatter:off
		return TC.create()
				.disjunction()
					.pattern()
						.disjunction()
							.entity(GmEntityType.T)
							.entity(GmEnumType.T)
							.entity(GmMetaModel.T)
						.close()
						.property("artifactBinding")
					.close()
					.pattern()
						.entity(GmMetaModel.class)
						.property("dependencies")
					.close()
					.pattern()
						.typeCondition(isAssignableTo(GmType.T))
						.property("declaringModel")
					.close()
					.pattern()
						.entity(Resource.class)
						.property("resourceSource")
					.close()
					.pattern()
						.typeCondition(isAssignableTo(ChangeValueManipulation.T))
						.typeCondition(orTc(isKind(collectionType), isKind(entityType)))			
					.close()
				.close()
				.done();
	// @formatter:on
	}
	

	@Ignore
	private Marshaller getXmlMarshaller() {
		if (xmlMarshaller == null) {
			// xmlMarshaller = new XmlMarshaller();
			xmlMarshaller = StaxMarshaller.defaultInstance;
		}

		return xmlMarshaller;
	}

}
