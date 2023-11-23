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
package com.braintribe.swagger;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.swagger.v2_0.meta.SwaggerBasePathMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerConsumesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerHostMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerInfoMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerProducesMd;
import com.braintribe.model.swagger.v2_0.meta.SwaggerSchemesMd;


public class ImportSwaggerModelProcessorTest {
	
	private Smood smood;
	private final TestSessionFactory sessionFactory = new TestSessionFactory();
	
	@Before
	public void setup() {
		smood = new Smood(new ReentrantReadWriteLock());
		smood.setAccessId("test.access");
		sessionFactory.reset(smood);
	}

	@Test
	public void cloneTest() {
		GmMetaModel metaModel = GmMetaModel.T.create();
		metaModel.setName("MyMetaModel");
		metaModel.getMetaData().add(Description.T.create());
		
		GmMetaModel metaModel2 = GmMetaModel.T.create();
		metaModel2.setName("MyMetaModel2");
		metaModel2.getMetaData().add(Description.T.create());
		
		metaModel.getDependencies().add(metaModel2);
		
		GmMetaModel gmMetaModel = ConvertSwaggerModelProcessor.copyModelSkeleton(metaModel);
		Assertions.assertThat(gmMetaModel.getMetaData()).isEmpty();
		Assertions.assertThat(gmMetaModel.getDependencies()).hasSize(1);
	}
	
	@Test
	public void testImportSwaggerModel() throws GmSessionException {
		// when
		ImportSwaggerModelFromUrl request = getRequest("res/swagger-minimal.yaml");
		ImportSwaggerModelProcessor processor = new ImportSwaggerModelProcessor();
		ImportSwaggerModelResponse response = processor.importSwagger(sessionFactory.newSession("test.access"), request.getSwaggerUrl(), request.getImportOnlyDefinitions());
		
		// then
		MessageNotification notification = (MessageNotification) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:test-swagger-import-model", notification.getMessage());
		GmMetaModel model = response.getModel();
		assertEquals("1.0.0", model.getVersion());
		assertEquals("model:tribefire.extension.swagger:test-swagger-import-model", model.getGlobalId());
		Set<MetaData> metaDatas = model.getMetaData();
		assertThat(metaDatas).hasSize(8);
		metaDatas.forEach(md -> {
			if (md instanceof Name) {
				assertThat(((Name) md).getName().value("namels:TestSwaggerImportModel")).isEqualTo("Test Swagger Import");
			} else if (md instanceof Description) {
				assertThat(((Description) md).getDescription().value("descls:TestSwaggerImportModel")).isEqualTo("This is description");
			} else if (md instanceof SwaggerBasePathMd) {
				assertThat(((SwaggerBasePathMd) md).getBasePath()).isEqualTo("/api");
			} else if (md instanceof SwaggerHostMd) {
				assertThat(((SwaggerHostMd) md).getHost()).isEqualTo("tribefire-services.swagger.io");
			} else if (md instanceof SwaggerInfoMd) {
				assertThat(((SwaggerInfoMd) md).getDescription()).isEqualTo("This is description");
			} else if (md instanceof SwaggerSchemesMd) {
				assertThat(((SwaggerSchemesMd) md).getSchemes().get(0)).isEqualTo("http");
			} else if (md instanceof SwaggerConsumesMd) {
				assertThat(((SwaggerConsumesMd) md).getConsumes().get(0)).isEqualTo("application/json");
			} else if (md instanceof SwaggerProducesMd) {
				assertThat(((SwaggerProducesMd) md).getProduces().get(0)).isEqualTo("application/json");
			} else {
				fail("Unexpected metadata!");
			}
		});
		Set<GmType> types = model.getTypes();
		assertThat(types).hasSize(2);
		types.forEach(type -> {
			type.type();
			if (type instanceof GmEntityType) {
				GmEntityType gmEntityType = (GmEntityType) type;
				assertThat(gmEntityType.getGlobalId()).isEqualTo("type:testswaggerimportmodel.Task");
				List<GmProperty> properties = gmEntityType.getProperties();
				assertThat(properties).hasSize(3);
				Map<String, GmProperty> propertyMap = properties.stream().collect(Collectors.toMap(GmProperty::getName, Function.identity()));
				assertThat(propertyMap).containsKey("name");
				assertThat(propertyMap).containsKey("tag");
				assertThat(propertyMap).containsKey("enumType");
				
			} else if (type instanceof GmEnumType) {
				GmEnumType gmEnumType = (GmEnumType) type;
				List<GmEnumConstant> enumConstants = gmEnumType.getConstants();
				assertThat(enumConstants).hasSize(3);
				enumConstants.forEach(gmEnumConstant -> assertTrue(gmEnumConstant.getName().startsWith("enumvalue")));
			} else {
				fail("Unexpected GmType!");
			}
		});
	}
	
	private ImportSwaggerModelFromUrl getRequest(String path) throws GmSessionException {
		File testFile = new File(path);
		assertFilesExistAndAreFiles(testFile);
		ImportSwaggerModelFromUrl request = ImportSwaggerModelFromUrl.T.create();
		request.setSwaggerUrl(testFile.getPath());
		request.setDomainId("test.access");
		request.setImportOnlyDefinitions(false);
		return request;
	}
	
	private void assertFilesExistAndAreFiles(File file) {
		assertThat(file).exists();
		assertThat(file).isFile();
	}
	
	
}
