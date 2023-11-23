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
package com.braintribe.swagger.test;

/*
@Category(TribefireServices.class)
public class ImportSwaggerModelProcessorTest extends AbstractSwaggerImportTest {

	@Test
	public void testDeployablesDeployed() {
		logger.info("Making sure that all expected deployables are there and deployed...");
		GenericDeployablesPresentTest test = new GenericDeployablesPresentTest(globalSessionFactory);
		test.assertThatDeployableIsPresentAndDeployed("swaggerImportProcessor.serviceProcessor", ImportSwaggerModelProcessor.T);
		logger.info("Test finished successfully!");
	}
	
	@Test
	public void testImportFixedSwagger() throws GmSessionException, IOException {
		PersistenceGmSession session = globalImp.session();
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(getResourceRequest("res/adidasproductapi_fixed.yaml")).get();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();
		MessageWithCommand notification = (MessageWithCommand) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:draft-product-service-model", notification.getMessage());
		
		GenericEntity draftProductServiceModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:draft-product-service-model");
		GenericEntity draftProductServiceApiModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:draft-product-service-api-model");
		GenericEntity colorway = cortexSession.findEntityByGlobalId("type:draftproductservicemodel.AdidasComProductArticle.api.Colorway");
		GenericEntity conversionIDs = cortexSession.findEntityByGlobalId("type:draftproductservicemodel.AdidasComProductSKU.api.ConversionIDs");
		GenericEntity assetTypeEnum = cortexSession.findEntityByGlobalId("type:draftproductservicemodel.AdidasComProductAsset.api.AssetTypeEnum");
		GenericEntity adidasComProductSKU = cortexSession.findEntityByGlobalId("type:draftproductservicemodel.AdidasComProductSKU");
		
		assertNotNull(draftProductServiceModel);
		assertNotNull(draftProductServiceApiModel);
		assertNotNull(colorway);
		assertNotNull(conversionIDs);
		assertNotNull(assetTypeEnum);
		assertNotNull(adidasComProductSKU);
		
		QueryHelper queryHelper = new QueryHelper(session);

		List<GmEntityType> gmEntityTypes = queryHelper.entitiesWithPropertyLike(GmEntityType.T, "typeSignature", "draftproductservicemodel.AdidasComProductSKU");
		GmEntityType sku = gmEntityTypes.get(0);
		sku.getMetaData().forEach(md -> {
			if (md instanceof Name) {
				Name name = (Name) md;
				assertEquals("SKU", name.getName().value());
			}
			if (md instanceof SwaggerExampleMd) {
				SwaggerExampleMd example = (SwaggerExampleMd) md;
				assertEquals("{EAN=4058026055128, UPC=885591427616, srcSizeCD3=380, srcSizeScale=T4, conversionIDs=[{saleSizeConvID=D, saleSizeScale=8C, saleSizeAbbr=27}]}", example.getExample());
			}
		});
		sku.getProperties().forEach(property -> {
			switch(property.getName()) {
				case "EAN": 
				case "UPC":
				case "srcSizeCD3":
				case "srcSizeScale":
				case "srcSizeDescription":
					assertTrue(property.getType() instanceof GmStringType);	
					break;
				case "conversionIDs":
					assertEquals(property.getType().getTypeSignature(),"set<draftproductservicemodel.AdidasComProductSKU.api.ConversionIDs>");
					break;
				default:
					fail("Undefined property.");
			}
		});
		
		cortexSession.deleteEntity(draftProductServiceModel);
		cortexSession.deleteEntity(draftProductServiceApiModel);
		cortexSession.commit();
	}
	
	@Test
	public void testImportInvalidSwagger() throws GmSessionException, IOException {
		PersistenceGmSession session = globalImp.session();
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(getResourceRequest("res/adidasproductapi_invalid.yaml")).get();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();
		MessageNotification notification = (MessageNotification) response.getNotifications().get(0);
		assertEquals("Unable to read content.  It may be invalid JSON or YAML.", notification.getMessage());
		
		GenericEntity draftProductServiceModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:DraftProductServiceModel");
		GenericEntity draftProductServiceApiModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:DraftProductServiceApiModel");
		assertNull(draftProductServiceModel);
		assertNull(draftProductServiceApiModel);
	}
	
	@Category(Online.class)
	@Test
	public void testImportSwaggerFromUrl() throws GmSessionException {
		PersistenceGmSession session = globalImp.session();
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(getUrlRequest("http://petstore.swagger.io/v2/swagger.json")).get();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();
		MessageWithCommand notification = (MessageWithCommand) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:swagger-petstore-model", notification.getMessage());
		
		GenericEntity swaggerPetstoreModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-model");
		GenericEntity swaggerPetstoreAPIModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-api-model");
		
		assertNotNull(swaggerPetstoreModel);
		assertNotNull(swaggerPetstoreAPIModel);
		
		cortexSession.deleteEntity(swaggerPetstoreModel);
		cortexSession.deleteEntity(swaggerPetstoreAPIModel);
		cortexSession.commit();
	}
	
	@Category(Online.class)
	@Test
	public void testExportSwagger() throws GmSessionException {
		ImportSwaggerModelFromUrl request = ImportSwaggerModelFromUrl.T.create();
		request.setSwaggerUrl("http://petstore.swagger.io/v2/swagger.json");
		request.setImportOnlyDefinitions(true);
		
		PersistenceGmSession session = globalImp.session();
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(request).get();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();
		MessageWithCommand notification = (MessageWithCommand) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:swagger-petstore-model", notification.getMessage());
		
		ExportSwaggerFromModelName exportRequest = ExportSwaggerFromModelName.T.create();
		exportRequest.setModelName("swagger-petstore-model");
		ExportSwaggerModelResponse exportResponse = (ExportSwaggerModelResponse) session.eval(exportRequest).get();
		assertNotNull(exportResponse.getResource());
		
		GenericEntity swaggerPetstoreModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-model");
		GenericEntity swaggerPetstoreAPIModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-api-model");
		
		assertNotNull(swaggerPetstoreModel);
		assertNull(swaggerPetstoreAPIModel);
		
		cortexSession.deleteEntity(swaggerPetstoreModel);
		cortexSession.commit();
	}
	
	@Test
	public void testImportSwaggerFromEmptyUrl() throws GmSessionException {
		PersistenceGmSession session = globalImp.session();
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(getUrlRequest("")).get();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();
		MessageNotification notification = (MessageNotification) response.getNotifications().get(0);
		assertEquals("Cannot import swagger model from empty or null URL!", notification.getMessage());
		
		GenericEntity swaggerPetstoreModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-model");
		GenericEntity swaggerPetstoreAPIModel = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-api-model");
		
		assertNull(swaggerPetstoreModel);
		assertNull(swaggerPetstoreAPIModel);
		
	}
	
	@Test
	public void testDefaultModelNameFromUrlRequest() throws GmSessionException, IOException {
		PersistenceGmSession session = globalImp.session();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();

		ImportSwaggerModelFromUrl request = ImportSwaggerModelFromUrl.T.create();
		request.setSwaggerUrl("http://petstore.swagger.io/v2/swagger.json");
		request.setImportOnlyDefinitions(true);
		
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(request).get();
		MessageWithCommand notification = (MessageWithCommand) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:swagger-petstore-model", notification.getMessage());
		
		ImportSwaggerModelFromUrl request2 = ImportSwaggerModelFromUrl.T.create();
		request2.setSwaggerUrl("http://petstore.swagger.io/v2/swagger.json");
		request2.setImportOnlyDefinitions(true);
		request2.setNamespace("swagger default:name");
		
		
		ImportSwaggerModelResponse response2 = (ImportSwaggerModelResponse) session.eval(request2).get();
		MessageWithCommand notification2 = (MessageWithCommand) response2.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: swagger-default:name-model", notification2.getMessage());
		
			
		GmMetaModel swagger_petstore_model = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:swagger-petstore-model");
		assertNotNull(swagger_petstore_model);
		
		GmMetaModel swagger_default_name_model = cortexSession.findEntityByGlobalId("model:swagger-default:name-model");
		assertNotNull(swagger_default_name_model);
		
		assertEquals(swagger_petstore_model.getTypes().size(), swagger_default_name_model.getTypes().size());
		
		cortexSession.deleteEntity(swagger_petstore_model);
		cortexSession.deleteEntity(swagger_default_name_model);
		cortexSession.commit();
		
	}
	
	@Test
	public void testDefaultModelNameFromResourceRequest() throws GmSessionException, IOException {
		PersistenceGmSession session = globalImp.session();
		PersistenceGmSession cortexSession = globalImp.switchToAccess(CORTEX_ACCESS_ID).session();

		ImportSwaggerModelFromResource request = getResourceRequest("res/adidasproductapi_fixed.yaml");
		request.setImportOnlyDefinitions(true);
		
		ImportSwaggerModelResponse response = (ImportSwaggerModelResponse) session.eval(request).get();
		MessageWithCommand notification = (MessageWithCommand) response.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: tribefire.extension.swagger:draft-product-service-model", notification.getMessage());
		
		ImportSwaggerModelFromResource request2 = getResourceRequest("res/adidasproductapi_fixed.yaml");
		request2.setImportOnlyDefinitions(true);
		request2.setNamespace("adidas default:name");
		
		ImportSwaggerModelResponse response2 = (ImportSwaggerModelResponse) session.eval(request2).get();
		MessageWithCommand notification2 = (MessageWithCommand) response2.getNotifications().get(0);
		assertEquals("Imported Model from Swagger definition as: adidas-default:name-model", notification2.getMessage());
		
			
		GmMetaModel draft_product_service_model = cortexSession.findEntityByGlobalId("model:tribefire.extension.swagger:draft-product-service-model");
		assertNotNull(draft_product_service_model);
		
		GmMetaModel adidas_default_name_model = cortexSession.findEntityByGlobalId("model:adidas-default:name-model");
		assertNotNull(adidas_default_name_model);
		
		assertEquals(draft_product_service_model.getTypes().size(), adidas_default_name_model.getTypes().size());
		
		cortexSession.deleteEntity(draft_product_service_model);
		cortexSession.deleteEntity(adidas_default_name_model);
		cortexSession.commit();
		
	}
	
	private ImportSwaggerModelFromResource getResourceRequest(String path) throws GmSessionException, IOException {
		Resource resource = Resource.T.create();
		File inputFile = new File(path);
		InputStream inputStream = FileUtils.openInputStream(inputFile);
		GeneralGmUtils gmUtils = new GeneralGmUtils(globalImp.session());
		resource = gmUtils.createResource("adidasTestModel", inputStream); 
		ImportSwaggerModelFromResource request = ImportSwaggerModelFromResource.T.create();
		request.setSwaggerResource(resource);
		request.setImportOnlyDefinitions(false);
		return request;
	}
	
	private ImportSwaggerModelFromUrl getUrlRequest(String url) {
		ImportSwaggerModelFromUrl request = ImportSwaggerModelFromUrl.T.create();
		request.setSwaggerUrl(url);
		request.setImportOnlyDefinitions(false);
		return request;
	}
	
}
*/