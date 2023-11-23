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
package tribefire.extension.spreadsheet.exchange_processing.test.wire.space;

import java.time.ZoneId;

import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.spreadsheet.exchange_processing.test.TestConstants;
import tribefire.extension.spreadsheet.exchange_processing.test.wire.contract.SpreadsheetExchangeProcessingTestContract;
import tribefire.extension.spreadsheet.model.exchange.api.request.SpreadsheetExchangeRequest;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnDatePatternMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnDateZoneMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnNameMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnRegexMapping;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetColumnTrimming;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetDataDelimiter;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetEntityContextLinking;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetIdentityProperty;
import tribefire.extension.spreadsheet.model.exchange.metadata.SpreadsheetRowNumProperty;
import tribefire.extension.spreadsheet.model.test.DateTestRecord;
import tribefire.extension.spreadsheet.model.test.Person;
import tribefire.extension.spreadsheet.model.test.PersonContext;
import tribefire.extension.spreadsheet.model.test.Record;
import tribefire.extension.spreadsheet.model.test.TestRecord;
import tribefire.extension.spreadsheet.processing.service.SpreadsheetExchangeProcessor;

@Managed
public class SpreadsheetExchangeProcessingTestSpace implements SpreadsheetExchangeProcessingTestContract {
	
	@Import
	private AccessProcessingConfigurationContract accessProcessingConfiguration;
	
	@Import
	private CommonAccessProcessingContract commonAccessProcessing;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		accessProcessingConfiguration.registerAccessConfigurer(this::configureAccesses);
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}
	
	private void configureAccesses(AccessProcessingConfiguration configuration) {
		configuration.registerAccess(TestConstants.ACCESS_IMPORT, configuredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_REGEX, regexConfiguredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_UPDATE, updateConfiguredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_SHARED, sharedConfiguredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_TRIMMING, trimmingConfiguredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_UPDATE_NULL_FORBIDDEN, updateNullForbiddenConfiguredModel());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT2, configuredModel2());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT3, configuredModel3());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_SIMPLE_CSV, configuredModelSimpleCsv());
		configuration.registerAccess(TestConstants.ACCESS_IMPORT_SIMPLE_XLSX, configuredModelSimpleXlsx());
		configuration.registerAccessRequestProcessor(SpreadsheetExchangeRequest.T, spreadsheetExchangeProcessor());
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
		// TODO register or remove interceptors and register tested service processors
		/*
			bean.registerInterceptor("someInterceptor");
			bean.removeInterceptor("someInterceptor");
			bean.register(SomeServiceRequest.T, someServiceProcessor());
		*/
	}
	
	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		// TODO add users IF your requests are to be authorized while testing
		// (make sure the 'auth' interceptor is not removed in that case in the 'configureServices' method)
		/* 
			User someUser = User.T.create();
			user.setId("someUserId");
			user.setName("someUserName");
			user.setPassword("somePassword");
	
			bean.addUser(someUser);
		*/
	}
	
	@Managed
	private GmMetaModel configuredModel() {
		GmMetaModel bean = GmMetaModel.T.create();

		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(GMF.getTypeReflection().getModel("tribefire.extension.spreadsheet:spreadsheet-exchange-test-model").getMetaModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(TestRecord.T).addPropertyMetaData(TestRecord.rowNum, SpreadsheetRowNumProperty.T.create());
		mdEditor.onEntityType(TestRecord.T).addMetaData(SpreadsheetDataDelimiter.create(";"));
		
		return bean;
	}
	
	@Managed
	private GmMetaModel configuredModelSimpleXlsx() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(Record.T).addPropertyMetaData(Record.dateValue, SpreadsheetColumnDateZoneMapping.create("UTC"));
		
		return bean;
	}
	
	@Managed
	private GmMetaModel updateConfiguredModel() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.birthDate, SpreadsheetColumnDatePatternMapping.create("dd.MM.yyyy"));
		
		SpreadsheetIdentityProperty idProperty = SpreadsheetIdentityProperty.T.create();
		idProperty.setNullIsIdentifier(true);
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.socialContractNumber, SpreadsheetIdentityProperty.T.create());
		
		return bean;
	}
	
	@Managed
	private GmMetaModel trimmingConfiguredModel() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.birthDate, SpreadsheetColumnDatePatternMapping.create("dd.MM.yyyy"));
		
		SpreadsheetColumnTrimming columnTrimming = SpreadsheetColumnTrimming.T.create();
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.hobby, columnTrimming);
		
		return bean;
	}

	
	@Managed
	private GmMetaModel sharedConfiguredModel() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(GMF.getTypeReflection().getModel("tribefire.extension.spreadsheet:spreadsheet-exchange-test-model").getMetaModel());
		
		
		ModelOracle modelOracle = new BasicModelOracle(bean);
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(PersonContext.T).addPropertyMetaData(TestRecord.rowNum, SpreadsheetRowNumProperty.T.create());
		mdEditor.onEntityType(TestRecord.T).addMetaData(SpreadsheetDataDelimiter.create(";"));
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.birthDate, SpreadsheetColumnDatePatternMapping.create("dd.MM.yyyy"));
		
		SpreadsheetEntityContextLinking linking = SpreadsheetEntityContextLinking.T.create();
		linking.setHashProperty(modelOracle.findEntityTypeOracle(Person.T).findProperty(Person.hash).asGmProperty());
		EntityTypeOracle personContextOracle = modelOracle.findEntityTypeOracle(PersonContext.T);
		linking.setLinkProperty(personContextOracle.findProperty(PersonContext.person).asGmProperty());
		linking.setType(personContextOracle.asGmEntityType());
		
		mdEditor.onEntityType(Person.T).addMetaData(linking);
		
		return bean;
	}
	
	@Managed
	private GmMetaModel updateNullForbiddenConfiguredModel() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.birthDate, SpreadsheetColumnDatePatternMapping.create("dd.MM.yyyy"));
		
		
		mdEditor.onEntityType(Person.T).addPropertyMetaData(Person.socialContractNumber, SpreadsheetIdentityProperty.T.create());
		
		return bean;
	}
	
	@Managed
	private GmMetaModel regexConfiguredModel() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		mdEditor.onEntityType(Record.T).addPropertyMetaData(Record.stringValue, SpreadsheetColumnRegexMapping.create("Hallo (.*)", "- $1 -"));
		
		return bean;
	}
	
	@Managed
	private GmMetaModel configuredModel2() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model2");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);

		mdEditor.onEntityType(DateTestRecord.T) //
			.addPropertyMetaData(DateTestRecord.policyStartDate, SpreadsheetColumnNameMapping.create("Policy_StartDate"));
		
		mdEditor.onEntityType(DateTestRecord.T) //
			.addPropertyMetaData(DateTestRecord.asAtDate, SpreadsheetColumnNameMapping.create("AsAtDate"));

		return bean;
	}
	
	@Managed
	private GmMetaModel configuredModel3() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model3");
		bean.getDependencies().add(configuredModel2());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		// 30/06/2020 0:00
		mdEditor.onEntityType(TestRecord.T) //
		.addPropertyMetaData(SpreadsheetColumnDatePatternMapping.create("d/M/yyyy H:m"));
		
		return bean;
	}
	
	@Managed
	private GmMetaModel configuredModelSimpleCsv() {
		GmMetaModel bean = GmMetaModel.T.create();
		
		bean.setName("tribefire.extension.spreadsheet:configured-spreadsheet-exchange-test-model3");
		bean.getDependencies().add(configuredModel());
		
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(bean);
		
		// 30/06/2020 0:00
		mdEditor.onEntityType(TestRecord.T) //
		.addPropertyMetaData(SpreadsheetColumnDatePatternMapping.create("yyyy-MM-dd"));
		
		return bean;
	}
	
	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
	
	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return commonAccessProcessing.sessionFactory();
	}
	
	private SpreadsheetExchangeProcessor spreadsheetExchangeProcessor() {
		SpreadsheetExchangeProcessor bean = new SpreadsheetExchangeProcessor();
		bean.setEngines(new PolymorphicDenotationMap<>());
		return bean;
	}
	
}
