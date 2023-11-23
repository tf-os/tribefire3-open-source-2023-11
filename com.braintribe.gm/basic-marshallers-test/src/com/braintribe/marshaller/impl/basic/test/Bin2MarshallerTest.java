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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.marshaller.impl.basic.test.model.A;
import com.braintribe.marshaller.impl.basic.test.model.B;
import com.braintribe.marshaller.impl.basic.test.model.DemoAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileSystemSource;

public class Bin2MarshallerTest {

	@Test
	public void testBin2MarshallerAndUnmarshallerBinary() throws Exception {

		FileSystemSource resourceSource = FileSystemSource.T.create();
		resourceSource.setPath("my/resources");

		Resource resource = Resource.T.create();
		resource.setResourceSource(resourceSource);

		Bin2Marshaller marshaller = new Bin2Marshaller();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.marshall(baos, resource);

		ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
		Resource decodedResource = (Resource) marshaller.unmarshall(in);
		
		Assert.assertEquals(((FileSystemSource) resource.getResourceSource()).getPath(),
				((FileSystemSource) decodedResource.getResourceSource()).getPath());
	}

	@Test
	public void testBin2MarshallerAndUnmarshallerBase64() throws Exception {

		FileSystemSource resourceSource = FileSystemSource.T.create();
		resourceSource.setPath("my/resources");

		Resource resource = Resource.T.create();
		resource.setResourceSource(resourceSource);

		Bin2Marshaller marshaller = new Bin2Marshaller();

		String encodedRequest = marshaller.encode(resource);
		Resource decodedResource = (Resource) marshaller.decode(encodedRequest);

		Assert.assertEquals(((FileSystemSource) resource.getResourceSource()).getPath(),
				((FileSystemSource) decodedResource.getResourceSource()).getPath());
	}

	@Test
	public void testBin2MarsahllerAndUnmarshallerWithDeployables() throws Exception {
		DemoAccess source = DemoAccess.T.create();
		source.setExternalId("access.my");
		source.setName("My Access");
		source.setGlobalId("d5f20c04-e2da-4da9-9275-194df716c5cc");
		source.setPartition("cortex");
		// source.setInitDefaultPopulation(true);
		source.setMetaModel(GenericEntity.T.getModel().getMetaModel());

		Set<Class<? extends GenericEntity>> modelClazzes = new HashSet<>();
		modelClazzes.add(GenericEntity.class);
		modelClazzes.add(StandardIdentifiable.class);


		DemoAccess exportedSource = export(source);

		Bin2Marshaller marshaller = new Bin2Marshaller();

		String encodedRequest = marshaller.encode(exportedSource);

		DemoAccess target = (DemoAccess) marshaller.decode(encodedRequest);

		Assert.assertEquals(source.getExternalId(), target.getExternalId());
	}

	@Test
	public void testBin2MarsahllerAndUnmarshallerWithAiOnNull() throws Exception {
		A a = A.T.create();
		B b = B.T.create();

		a.setReference(b);
		a.setBoolean(true);

		// ((PartialRepresentation) a).markAsAbsent("reference", AbsenceInformation.T.create());

		Bin2Marshaller marshaller = new Bin2Marshaller();

		String encodedRequest = marshaller.encode(a);
		A target = (A) marshaller.decode(encodedRequest);

		Assert.assertNotNull(target);
	}

	/**
	 * creates a trimmed clone of the deployable that is easily transportable via rpc calls
	 * 
	 * @param deployable
	 *            The deployable
	 * @return A clone of the deployable
	 */
	protected DemoAccess export(DemoAccess deployable) {
		// @formatter:off
		TraversingCriterion tc = TC.create()
				.conjunction()
					.property()
					.typeCondition(orTc(isKind(collectionType), isKind(entityType)))			
					.negation()
						.pattern()
							.typeCondition(isAssignableTo(DemoAccess.T))
							.property("cartridge")
						.close()
				.close()
				.done();
		// @formatter:on

		StandardMatcher matcher = new StandardMatcher();
		matcher.setCriterion(tc);

		StandardCloningContext cloningContext = new StandardCloningContext();
		cloningContext.setAbsenceResolvable(true);
		cloningContext.setMatcher(matcher);

		DemoAccess exportedDeployable = deployable.entityType().clone(cloningContext, deployable, StrategyOnCriterionMatch.partialize);

		return exportedDeployable;
	}

}
