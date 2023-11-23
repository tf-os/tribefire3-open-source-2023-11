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
package tribefire.cortex.assets.workbench.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.meta.DefaultView;
import com.braintribe.model.workbench.meta.QueryString;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.workbench.initializer.wire.contract.WorkbenchInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class WorkbenchInitializerSpace extends AbstractInitializerSpace implements WorkbenchInitializerContract {

	@Override
	public void initialize() {
		folder_1();
		folder_19();
		folder_41();
		folder_45();
		folder_46();
		folder_47();
		folder_52();
		folder_54();
		folder_56();
		folder_57();
		folder_68();
		folder_69();
		workbenchPerspective_1();
		workbenchPerspective_2();
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2fd58709-5af2-4a23-976e-d97608b57132");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "d0e7612f-f928-42c1-9aff-1561e71d1b98");
		bean.setDisplayName(localizedString_1());
		bean.setName("$instantiateEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "629e5ac0-83a9-4f92-84fc-68a4575f53f0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New ")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "54dd7753-5eaa-4c40-a617-afe04d9bf19b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Record")));
		return bean;
	}

	// Managed
	private Template template_1() {
		Template bean = session().createRaw(Template.T, "a141f4d2-3d20-4b01-a2f3-2b6845da7649");
		bean.setPrototype(entityQuery_1());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_1() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "e608cf44-eb77-4fbe-b0c8-a3a8b2582272");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchAction");
		bean.setRestriction(restriction_1());
		return bean;
	}

	// Managed
	private Restriction restriction_1() {
		Restriction bean = session().createRaw(Restriction.T, "2e49b1ce-30ab-4f74-844d-a1cb21c3a4eb");
		bean.setCondition(valueComparison_1());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_1() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "4ca1652a-c6f0-49b4-887d-7d54f664999c");
		bean.setLeftOperand(propertyOperand_1());
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_1() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "b668a699-6691-40e1-ae14-059c0ebaa6cf");
		bean.setPropertyName("inplaceContextCriterion");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "77559be7-f141-4d2d-a4da-2524281c98e6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "37419ca9-f41a-43a6-b7f3-2f687edd61cb");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 50, 23, 57));
		bean.setFileSize(593l);
		bean.setMd5("9c42d0906a62a1f06f3964486683cb3e");
		bean.setMimeType("image/png");
		bean.setName("wb_home_16x16.png");
		bean.setResourceSource(moduleSource_1());
		bean.setSpecification(rasterImageSpecification_2());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e0fd2931-4447-412d-84a6-07a5dcc79531");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/50/95355802-3054-43c8-8ae4-b487a772a3d2");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "24d91789-ce48-4b72-ace4-ff9de7f5dba6");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_2() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "c004446c-90a9-4819-81c8-dcf70a9f2ba8");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchConfiguration");
		return bean;
	}

	// Managed
	private Restriction restriction_2() {
		Restriction bean = session().createRaw(Restriction.T, "77ff6d51-5601-4a4d-81d7-21f580e06dd3");
		bean.setCondition(valueComparison_2());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_2() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "0c132b79-3246-4be3-b97f-cea07087a7e6");
		bean.setLeftOperand(propertyOperand_2());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("headerbar");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_2() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "306cb35d-3531-499d-93f0-c6ed012c11cd");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "da93041c-eef7-4750-8d66-884183a332ed");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_1() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "2be1ca6f-6c4b-438e-9fee-e19dfb00f6e8");
		bean.setDisplayName(localizedString_5());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b3c7b225-747c-446b-820a-fc51c8e46993");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images Query")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_1() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "deb90632-578e-4295-bfa6-8e26a6e80530");
		bean.setTemplate(template_2());
		return bean;
	}

	// Managed
	private Template template_2() {
		Template bean = session().createRaw(Template.T, "cd010450-379e-4a2a-a181-aec7dcae8633");
		bean.setMetaData(Sets.set(defaultView_1(), queryString_1()));
		bean.setPrototype(entityQuery_3());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private DefaultView defaultView_1() {
		DefaultView bean = session().createRaw(DefaultView.T, "bfcc67ba-63c0-4a32-8b67-dade4ba451d7");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_1() {
		QueryString bean = session().createRaw(QueryString.T, "64141014-0db0-43d1-a672-f61839b2cafe");
		bean.setValue("from WorkbenchPerspective where name = 'headerbar'");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_3() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "e7c26053-0b07-4bfc-afab-92b371678915");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_2());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_1() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "5bf35231-3d0e-4152-9f39-fd665f7815a1");
		bean.setDirection(OrderingDirection.ascending);
		bean.setOrderBy(propertyOperand_3());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_3() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "d3acbbfe-d9ae-4bb5-83bd-4bbda408382d");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c020b23d-f423-498b-a007-02058a229c67");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "List View")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_7() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c9bf5abd-bfe0-4c33-b54c-37e3a67bbee9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actionbar")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_9() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "af495ad3-17ad-47c9-bfb8-7936ed30416c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2116/01/badeccbb-6125-4126-9e1f-e9998a6e78f8");
		return bean;
	}

	// Managed
	private DefaultView defaultView_2() {
		DefaultView bean = session().createRaw(DefaultView.T, "a61c6829-8072-4b1f-83ea-54f63c99e46f");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_8() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9b27e4f5-164a-4230-bff3-a38db5817d7f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Folders")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_12() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "561ec85b-1bf4-4d22-a5fa-151121025392");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2116/01/0cd1cd64-ce95-4581-8773-b2e980251f48");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_9() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2e67dd91-86ac-450f-a082-ccbdea1fd01e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_2() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "ea66bae0-4c8a-4c24-a0d6-e54ca114faf5");
		bean.setName("Images Icon");
		bean.setRepresentations(Sets.set(resource_6(), resource_7()));
		return bean;
	}

	// Managed
	private Resource resource_6() {
		Resource bean = session().createRaw(Resource.T, "aa981201-086d-43d7-9e03-7d1eb0d3cb6a");
		bean.setCreated(newGmtDate(2014, 10, 21, 15, 0, 54, 198));
		bean.setFileSize(19748l);
		bean.setMd5("7c374e1d9c4be34abe8c570aaa6dd1d5");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 16x16_03.png");
		bean.setResourceSource(moduleSource_15());
		bean.setSpecification(rasterImageSpecification_7());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_15() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a438f205-369d-4e39-81a5-2a15ad92aae5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2116/00/5a015814-adf7-4cd6-8c6f-85faf037f12c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_7() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d67ef6b8-b6a8-4d18-ba25-1cc3175b6fd8");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_7() {
		Resource bean = session().createRaw(Resource.T, "661b23dd-93b1-4dca-ab3e-ab625566d34d");
		bean.setCreated(newGmtDate(2014, 10, 21, 15, 1, 18, 318));
		bean.setFileSize(20309l);
		bean.setMd5("32a5620c7e8a63aa1e39491e294cce08");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 64x64_03.png");
		bean.setResourceSource(moduleSource_12());
		bean.setSpecification(rasterImageSpecification_8());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_8() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "530195b1-9547-4bd9-b89f-9c104cebaa72");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_10() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "62fdc214-d0a0-4ab4-ba11-63ef9ed84a65");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_11() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "38881d1e-fb94-4c9f-89d3-cf7f339278d8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_9() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d2f706af-8195-4e83-9ccb-a47141c128af");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_16() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d6b22f21-f54b-4554-92e2-d523f53a1c23");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/57/22c318a2-1ccb-40ef-bb2d-bc4a09df31d3");
		return bean;
	}

	// Managed
	private Template template_3() {
		Template bean = session().createRaw(Template.T, "dbf4d103-f30b-43b8-bcbf-187032fc89ba");
		bean.setPrototype(entityQuery_5());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_5() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "91feded8-7121-4976-a523-c8b013600877");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchAction");
		bean.setRestriction(restriction_4());
		return bean;
	}

	// Managed
	private Restriction restriction_4() {
		Restriction bean = session().createRaw(Restriction.T, "de9b99df-35d1-457e-82ce-4a3abde93b71");
		bean.setCondition(valueComparison_4());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_4() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "5cdf1804-5ce4-41fd-8463-820a55b23ca7");
		bean.setLeftOperand(propertyOperand_5());
		bean.setOperator(Operator.notEqual);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_5() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e3279ef9-3a08-4312-a790-b2b06d39d162");
		bean.setPropertyName("inplaceContextCriterion");
		return bean;
	}

	@Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "c96c9c45-83dc-4ebd-8ea7-70b8334edacb");
		bean.setDisplayName(localizedString_2());
		bean.setName("$recordTemplateScript");
		bean.setParent(folder_3());
		return bean;
	}

	@Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "5f974336-3ade-45bc-8937-eff1dd45446c");
		bean.setDisplayName(localizedString_12());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_4(), folder_5(), folder_6(), folder_7(), folder_8(), folder_9(), folder_10(), folder_11(), folder_12(),
				folder_13(), folder_14(), folder_15(), folder_16(), folder_2()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_12() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "136e1f37-bddd-4d94-8d07-8c80d64ce67e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action bar")));
		return bean;
	}

	// Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "600df7ea-569f-454c-a3fb-a6841e24be83");
		bean.setDisplayName(localizedString_13());
		bean.setName("$exchangeContentView");
		bean.setParent(folder_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_13() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "af3c4d78-847d-45c0-80fe-3cfe5cde6a89");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange View")));
		return bean;
	}

	// Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "6911d411-f339-426d-9397-7eabd770b610");
		bean.setDisplayName(localizedString_14());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_14() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bc6cc310-4b5f-4ecc-b53b-94c95d4f2c5f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "c20b4041-3aa1-4ac4-b8e1-2f534409fc5d");
		bean.setDisplayName(localizedString_15());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_15() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ace8b83f-1c93-4aa3-888d-b122a0f2bd6b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "7150332b-9526-4b5a-a3a8-851a3ed554cb");
		bean.setDisplayName(localizedString_16());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_16() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "91d2b018-4259-461a-9e7f-9c9cb2ce7839");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete ")));
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "1cb9111c-01c6-4cf5-a5a4-960b2bb486f5");
		bean.setDisplayName(localizedString_17());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_17() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b3478135-80b2-41d1-98c1-77f952035c0d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "33e61240-2fae-42c3-b6b7-cee6c65b5ca3");
		bean.setDisplayName(localizedString_11());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "09456949-40cf-48dd-9d27-32ec8e510fe5");
		bean.setDisplayName(localizedString_18());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_18() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "77052912-9cdd-4971-8ed4-77bdc7b4826c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "6dd70043-1de1-4c40-8ebd-0e2614a3d7e1");
		bean.setDisplayName(localizedString_19());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_19() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "df04916d-3652-4e86-9e4e-215fb3404934");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "4e26d9d8-9d0f-4e27-a2e6-8bd4038c5308");
		bean.setDisplayName(localizedString_20());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_20() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e968aca2-31cd-4faf-aaa4-d19d24427aeb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "daefc1ed-e2df-4c90-83a8-6a9640ba0905");
		bean.setDisplayName(localizedString_21());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_21() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c49151c7-73b7-4552-8bf7-91b67d5f245f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "6de6738f-3ca9-417c-a0b3-6aaf4a0df121");
		bean.setDisplayName(localizedString_3());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "238dc1ed-b76d-4df5-8a8e-0ac34e3e46a1");
		bean.setDisplayName(localizedString_22());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_22() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5913d25e-55a3-4224-af78-98f492aed844");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	@Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "f35cc7cd-86c5-4258-9c2f-03a0e3c89697");
		bean.setDisplayName(localizedString_23());
		bean.setName("$addToClipboard");
		bean.setParent(folder_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c91ce268-4216-4b8b-8f9c-be07ac679298");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add To Clipboard")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e6264c13-48f1-4e1e-8ea9-c6f98137b9d2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Query")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_3() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "79b8acfc-9888-487d-a4d9-535db3e69386");
		bean.setRepresentations(Sets.set(resource_9(), resource_8()));
		return bean;
	}

	// Managed
	private Resource resource_8() {
		Resource bean = session().createRaw(Resource.T, "7c5cd310-054c-4a41-af6e-0883d6757a4a");
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 24, 8, 71));
		bean.setFileSize(1210l);
		bean.setMd5("b599c004747fbdd06bc59da58b058edb");
		bean.setMimeType("image/png");
		bean.setName("view_32x32.png");
		bean.setResourceSource(moduleSource_17());
		bean.setSpecification(rasterImageSpecification_10());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_17() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a691d043-60d0-4b65-8bd1-643d6738a545");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/24/578f7746-da0b-43ca-8134-4b9eac27401c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_10() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ae71609b-89a1-4795-be91-2247d25ea226");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_9() {
		Resource bean = session().createRaw(Resource.T, "692b2f44-40c4-47c3-8ee3-f796696da49b");
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 23, 58, 195));
		bean.setFileSize(477l);
		bean.setMd5("068351a0336c6cfae580e1a64828cd8e");
		bean.setMimeType("image/png");
		bean.setName("view_16x16.png");
		bean.setResourceSource(moduleSource_18());
		bean.setSpecification(rasterImageSpecification_11());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_18() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "720a02e6-ee81-4936-bf8d-8fec051015d7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/23/c6643a1d-63ee-42bb-a8b4-712e58a53011");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_11() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4a1f46c6-ec12-49b5-87b1-2bcaae2f8efd");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_19() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0ee85805-7531-44a9-92c0-0accd8058e11");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/57/0d6ce771-726a-4089-ac68-50b706ddf302");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b10b46df-5ebc-4f66-80ff-4fb4dcd6904f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_4() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "1faa6965-df82-4c91-8866-17f9a5dc2920");
		bean.setName("Actionbar Icon");
		bean.setRepresentations(Sets.set(resource_14(), resource_13(), resource_11(), resource_12()));
		return bean;
	}

	// Managed
	private Resource resource_11() {
		Resource bean = session().createRaw(Resource.T, "9c6fa941-ee6a-4552-b2f3-96afdf4d4b56");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 55, 30, 629));
		bean.setFileSize(845l);
		bean.setMd5("586860c39f2bffaf7e9a3a1967dbdaad");
		bean.setMimeType("image/png");
		bean.setName("wb_actionbar_24x24.png");
		bean.setResourceSource(moduleSource_21());
		bean.setSpecification(rasterImageSpecification_14());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_21() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cd2580b1-2e4c-47de-af77-c0bb2fa09f3f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/55/e7156431-0f7c-41e1-ba7e-4f79f86a0747");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_14() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5d57cde3-dc0c-45dc-92a4-de71454645fb");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_12() {
		Resource bean = session().createRaw(Resource.T, "4de93028-7674-4864-82e1-c3ad552d4ebc");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 55, 30, 625));
		bean.setFileSize(517l);
		bean.setMd5("998666992633140cbee3d62d3f11e688");
		bean.setMimeType("image/png");
		bean.setName("wb_actionbar_16x16.png");
		bean.setResourceSource(moduleSource_22());
		bean.setSpecification(rasterImageSpecification_1());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_22() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3a8fa550-0648-410e-abd2-1b88b60a5ad3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/55/0dab0396-b0e7-45f3-9d4b-30830ac948f1");
		return bean;
	}

	// Managed
	private Resource resource_13() {
		Resource bean = session().createRaw(Resource.T, "6d6cfe8a-c55c-4f64-889d-eb484fdae355");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 55, 30, 634));
		bean.setFileSize(3557l);
		bean.setMd5("32abef375a3b62de8f41ddaa08f15ef1");
		bean.setMimeType("image/png");
		bean.setName("wb_actionbar_64x64.png");
		bean.setResourceSource(moduleSource_23());
		bean.setSpecification(rasterImageSpecification_15());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_23() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ecace9a7-7b59-4d1c-b149-261b7fe6c0bb");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/55/8048177b-9eae-4e57-96a0-d1077eb761df");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_15() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ae118716-608e-4e37-9aed-52a3b6983fc4");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_14() {
		Resource bean = session().createRaw(Resource.T, "bc3e06a5-de8a-49ab-b46b-ab3ebd60bcca");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 55, 30, 632));
		bean.setFileSize(1199l);
		bean.setMd5("255737e6019325e20f6034db3f15da71");
		bean.setMimeType("image/png");
		bean.setName("wb_actionbar_32x32.png");
		bean.setResourceSource(moduleSource_24());
		bean.setSpecification(rasterImageSpecification_16());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_24() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d53731da-158f-4cad-938d-a6a866a3cb06");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/55/74a4e3b3-1de2-4c71-8fa5-57cb4cc90481");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_16() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "7c8dbf14-b80e-49bf-913e-f2bd54ea1432");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_6() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9bf297c7-f636-47a9-bfbb-e1b67dfed880");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_6() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "6001df75-3e21-4c22-90ad-61460fb2c2eb");
		bean.setEntityTypeSignature("com.braintribe.model.folder.Folder");
		bean.setOrdering(simpleOrdering_1());
		bean.setRestriction(restriction_5());
		return bean;
	}

	// Managed
	private Restriction restriction_5() {
		Restriction bean = session().createRaw(Restriction.T, "4073e6a0-e531-4137-9f8b-bc5328986f6a");
		bean.setCondition(valueComparison_5());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_5() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "015f842e-856c-4560-b22f-f7208f1da80b");
		bean.setLeftOperand(propertyOperand_7());
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_7() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "dc9dc283-3fed-4aab-9701-82fb729bf6dd");
		bean.setPropertyName("parent");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "06ed705b-5d6b-4bc9-86a3-6baece72d2fe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	// Managed
	private Template template_4() {
		Template bean = session().createRaw(Template.T, "b38a0f86-ee14-4bec-ba0d-5cc7b4a0b4fa");
		bean.setMetaData(Sets.set(queryString_2(), defaultView_3()));
		bean.setPrototype(entityQuery_7());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private DefaultView defaultView_3() {
		DefaultView bean = session().createRaw(DefaultView.T, "c8cf0aeb-46d8-4b92-8cdb-0238b7b430d9");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_2() {
		QueryString bean = session().createRaw(QueryString.T, "6f9b300c-66b7-4752-bb8e-d5c2ed713c3b");
		bean.setValue("from WorkbenchPerspective where name = 'global-actionbar'");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_7() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "4be734cd-f152-428a-a1cc-aace426b1999");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_6());
		return bean;
	}

	// Managed
	private Restriction restriction_6() {
		Restriction bean = session().createRaw(Restriction.T, "78e24f48-1738-4994-9eaf-2f1b45b499bb");
		bean.setCondition(valueComparison_6());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_6() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "f93c2b88-70e8-497a-a73c-dfae6c120679");
		bean.setLeftOperand(propertyOperand_8());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("global-actionbar");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_8() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e03ac6ca-a06c-4d82-8390-a13af36f4697");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fb0dab41-f33a-482a-ab62-280424ba8827");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "GME Configuration")));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "2c9c2d92-efcf-456c-b5c5-c0b5b4c337ba");
		bean.setDisplayName(localizedString_28());
		bean.setName("$detailsPanelVisibility");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "01ca2ce7-1df9-49f2-8aa5-0612f69e4338");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7d1b2411-1dd5-49bf-aeda-ccee5ad84b4b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New ")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_31() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ce35b063-4cfc-47d6-b108-362c8c70dfdb");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/24/83d1e649-5e2e-4b2a-a1ba-d26180a81921");
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "f60ae8ae-18a5-4e01-b975-299c130964cc");
		bean.setDisplayName(localizedString_30());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6d4230b4-573d-47f1-86cf-b36214688e03");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete ")));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_2() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "272c9c9c-4031-42ef-8d46-1b4708a3343c");
		bean.setDisplayName(localizedString_31());
		bean.setTypeSignature("com.braintribe.model.workbench.WorkbenchAction");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bfa759df-2d3f-4294-be10-c72f4ee29321");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actions Query")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_2() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "ce8af444-703a-4f7e-85b1-121d4b86be74");
		bean.setTemplate(template_5());
		return bean;
	}

	// Managed
	private Template template_5() {
		Template bean = session().createRaw(Template.T, "5334f031-0448-4787-a281-5de1ec8786a0");
		bean.setMetaData(Sets.set(defaultView_4(), queryString_3()));
		bean.setPrototype(entityQuery_2());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private DefaultView defaultView_4() {
		DefaultView bean = session().createRaw(DefaultView.T, "60bfe5d8-4090-4f8c-a03c-013b084d8cac");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_3() {
		QueryString bean = session().createRaw(QueryString.T, "525c3775-3bcc-4fd6-8014-d6b8af2ee5c1");
		bean.setValue("from WorkbenchConfiguration c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_27() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0729fa53-1758-45eb-b35c-d11845d40a1d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_28() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "124cfa87-8162-4854-9884-8a484fe733bd");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "33fb538b-3023-476b-b76a-65ee23afc5a6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Folders")));
		return bean;
	}

	@Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "5bb9cdf1-4ed4-447c-a844-a48f395a0827");
		bean.setContent(templateQueryAction_3());
		bean.setDisplayName(localizedString_33());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("Top-level Folders");
		bean.setParent(folder_20());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_3() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "ffeaac99-d74b-4f57-b85e-2202e3ea1293");
		bean.setTemplate(template_6());
		return bean;
	}

	// Managed
	private Template template_6() {
		Template bean = session().createRaw(Template.T, "df5394bd-ff44-4042-b59f-0816bf7aae15");
		bean.setMetaData(Sets.set(queryString_4(), defaultView_5()));
		bean.setPrototype(entityQuery_6());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_4() {
		QueryString bean = session().createRaw(QueryString.T, "25cc5361-5441-4e56-8420-cec197164ea0");
		bean.setValue("from Folder f where f.parent = null order by f.name");
		return bean;
	}

	// Managed
	private DefaultView defaultView_5() {
		DefaultView bean = session().createRaw(DefaultView.T, "bd044d57-c4a9-40c4-8ea5-93933d969fde");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3c2a15fc-c161-4ef8-bc45-806ecbe57b85");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Top-level Folders")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_5() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "8a4d0b7c-ea05-4536-8a06-812f1da0a6dc");
		bean.setName("Folder Icon");
		bean.setRepresentations(Sets.set(resource_21(), resource_19(), resource_20(), resource_22()));
		return bean;
	}

	// Managed
	private Resource resource_19() {
		Resource bean = session().createRaw(Resource.T, "20328242-36fe-43a1-8000-a1df1122ed2e");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 57, 34, 511));
		bean.setFileSize(1715l);
		bean.setMd5("d61cdb108089961fdc50216c0cfab5de");
		bean.setMimeType("image/png");
		bean.setName("wb_allfolder_64x64.png");
		bean.setResourceSource(moduleSource_16());
		bean.setSpecification(rasterImageSpecification_29());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_29() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a2a16dd0-f312-433e-8a0e-d23a1164ce84");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_20() {
		Resource bean = session().createRaw(Resource.T, "82a10853-d5e5-49cb-a398-39cbe337d06c");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 57, 34, 505));
		bean.setFileSize(627l);
		bean.setMd5("165259246f1bba5ff502d00991f00bf2");
		bean.setMimeType("image/png");
		bean.setName("wb_allfolder_24x24.png");
		bean.setResourceSource(moduleSource_19());
		bean.setSpecification(rasterImageSpecification_30());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_30() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "163963c8-abcd-4f0f-83ba-0d9139f35da5");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_21() {
		Resource bean = session().createRaw(Resource.T, "cb6f01bf-14e7-4c1d-9daa-eab7c961131c");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 57, 34, 502));
		bean.setFileSize(436l);
		bean.setMd5("6c7f4d52220997e31939cf00d4231848");
		bean.setMimeType("image/png");
		bean.setName("wb_allfolder_16x16.png");
		bean.setResourceSource(moduleSource_35());
		bean.setSpecification(rasterImageSpecification_28());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_35() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "646787af-50e2-4aed-a69e-a999e04d366f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/57/779e2bd3-da90-4d59-a976-23100b0e24fb");
		return bean;
	}

	// Managed
	private Resource resource_22() {
		Resource bean = session().createRaw(Resource.T, "fedc9387-ddf9-41f6-85f7-a2270e1c7a0f");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 57, 34, 508));
		bean.setFileSize(861l);
		bean.setMd5("eeecbeae0df7105945bb63986a54bb6e");
		bean.setMimeType("image/png");
		bean.setName("wb_allfolder_32x32.png");
		bean.setResourceSource(moduleSource_36());
		bean.setSpecification(rasterImageSpecification_31());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_36() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a793b6cf-1075-4028-a903-0eaee03adeb2");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/57/cc622acb-08d0-40c2-ba66-196b632844fd");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_31() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3f0ea873-b8d5-46b6-8b4f-dbd8aa2788cb");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	@Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "fafa0c94-2016-478f-b4f9-afc83be2f545");
		bean.setDisplayName(localizedString_27());
		bean.setName("GME");
		bean.setParent(folder_21());
		bean.setSubFolders(Lists.list(folder_32(), folder_39()));
		return bean;
	}

	@Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "34127265-6d74-47b7-aef6-b953352ed841");
		bean.setDisplayName(localizedString_34());
		bean.setName("WorkbenchConfiguration");
		bean.setParent(folder_22());
		bean.setSubFolders(Lists.list(folder_20(), folder_27(), folder_29()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_34() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e0b37c70-0e2a-4506-b063-03e3d867e4a2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Workbench Administration")));
		return bean;
	}

	@Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "eead198a-6285-476b-9026-7628cb79c180");
		bean.setDisplayName(localizedString_35());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_21(), folder_23()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_35() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a19902d3-770d-4e94-850d-c5a42924a15b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "59f42abe-e78c-416f-befe-2ffc50db322e");
		bean.setDisplayName(localizedString_36());
		bean.setName("System");
		bean.setParent(folder_22());
		bean.setSubFolders(Lists.list(folder_24()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "76592460-4d89-4915-b8ce-a20874aaef97");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System")));
		return bean;
	}

	@Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "ad14374e-9fa9-4209-8e62-a07ecf1fdb8a");
		bean.setDisplayName(localizedString_37());
		bean.setName("Resources");
		bean.setParent(folder_23());
		bean.setSubFolders(Lists.list(folder_25(), folder_26()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "05d88a6d-7dcd-47a7-860e-49111715eb8f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resources")));
		return bean;
	}

	@Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "96c3e80c-7c2c-447a-a65a-ad4bc61990e7");
		bean.setContent(simpleQueryAction_1());
		bean.setDisplayName(localizedString_38());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("Images");
		bean.setParent(folder_24());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_38() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8f29d749-30a1-4d7e-983e-90d8f23e4a15");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "eabfdbbb-10ea-4983-a1f3-d38f50c471f1");
		bean.setContent(simpleQueryAction_3());
		bean.setDisplayName(localizedString_40());
		bean.setIcon(adaptiveIcon_6());
		bean.setName("Files");
		bean.setParent(folder_24());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_3() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "c6b628b3-1503-40aa-9547-31c58804c429");
		bean.setDisplayName(localizedString_39());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_39() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "124854b3-bce0-46c2-a41a-db6d47356bb3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2c23fe16-beaa-4733-ab40-b15d4e207791");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_6() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "05eb33fb-3974-47f8-a0b7-bdc84b72f681");
		bean.setName("Files Icon");
		bean.setRepresentations(Sets.set(resource_24(), resource_23()));
		return bean;
	}

	// Managed
	private Resource resource_23() {
		Resource bean = session().createRaw(Resource.T, "3d9a43d1-3c5a-4164-bbcf-ef27ca587bf4");
		bean.setCreated(newGmtDate(2014, 10, 21, 15, 1, 18, 311));
		bean.setFileSize(20079l);
		bean.setMd5("6da736391282db589f9420551cb34cc5");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 64x64_01.png");
		bean.setResourceSource(moduleSource_9());
		bean.setSpecification(rasterImageSpecification_32());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_32() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b4601cc9-ed2b-4311-b9ad-e9295ea22b2c");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_24() {
		Resource bean = session().createRaw(Resource.T, "7b218640-6bde-4c7d-ba9f-1e59d1156223");
		bean.setCreated(newGmtDate(2014, 10, 21, 15, 0, 54, 187));
		bean.setFileSize(19781l);
		bean.setMd5("047840a3e9eebb505ffb24021396da12");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 16x16_01.png");
		bean.setResourceSource(moduleSource_37());
		bean.setSpecification(rasterImageSpecification_33());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_37() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e2d8788a-ed39-4abf-944d-ccce4436b28d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2116/00/37ef7b24-6115-4371-8c11-196cae04e2fd");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_33() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c8c6efc3-e1b6-4d7e-a922-24d0c928692c");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "37888b83-9325-4b02-b695-06ddd17ad0ca");
		bean.setDisplayName(localizedString_8());
		bean.setName("Folders");
		bean.setSubFolders(Lists.list(folder_28(), folder_19()));
		return bean;
	}

	@Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "5a839b92-9c09-4f29-8fd2-7fd50aabbd05");
		bean.setContent(templateQueryAction_4());
		bean.setDisplayName(localizedString_32());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("AllFolders");
		bean.setParent(folder_20());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_4() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "5183ce79-60ee-4e66-920b-fa44d0c213a9");
		bean.setTemplate(template_7());
		return bean;
	}

	// Managed
	private Template template_7() {
		Template bean = session().createRaw(Template.T, "5354a402-35a0-42a1-b691-1f05edcc17b5");
		bean.setMetaData(Sets.set(defaultView_6(), queryString_5()));
		bean.setPrototype(entityQuery_9());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_5() {
		QueryString bean = session().createRaw(QueryString.T, "0a86824f-2032-46f5-b820-328c256763cc");
		bean.setValue("from Folder f order by f.name");
		return bean;
	}

	// Managed
	private DefaultView defaultView_6() {
		DefaultView bean = session().createRaw(DefaultView.T, "38df5caf-0a34-4620-8716-4d65d7182a33");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_9() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "f901c091-520b-46b7-9f48-e182ebb1e798");
		bean.setEntityTypeSignature("com.braintribe.model.folder.Folder");
		bean.setOrdering(simpleOrdering_2());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_2() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "8a515ee3-cdd8-49da-8bdf-35f9759a5e58");
		bean.setDirection(OrderingDirection.ascending);
		bean.setOrderBy(propertyOperand_10());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_10() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e455d976-8a10-4771-b4d5-a5c9ab385770");
		bean.setPropertyName("name");
		return bean;
	}

	@Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "8c539de6-b7bc-4cd3-971a-e6d200a8fceb");
		bean.setContent(simpleQueryAction_2());
		bean.setDisplayName(localizedString_41());
		bean.setIcon(adaptiveIcon_7());
		bean.setName("Actions");
		bean.setSubFolders(Lists.list(folder_30(), folder_31()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3ad860df-2fc0-42ca-b8a2-c35eb5fa4f04");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actions")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_7() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e00f3ae2-125e-4c0f-a61b-2b330a736b37");
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "0338b6de-9cb7-4e6d-b3f5-7927c4c54735");
		bean.setContent(templateQueryAction_5());
		bean.setDisplayName(localizedString_43());
		bean.setIcon(adaptiveIcon_8());
		bean.setName("ContextSensitive");
		bean.setParent(folder_29());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_5() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "62bf748d-febf-4eff-953f-89ed56ca0c2b");
		bean.setDisplayName(localizedString_42());
		bean.setTemplate(template_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "95c20994-cd15-4bad-b4a5-6a7ec8c7343c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Context Sensitive Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d0f031bb-6cc6-49cc-b70b-5a4961b0127d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Context Sensitive")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_8() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "4644e285-71da-4cef-9f4b-0511438cc85c");
		bean.setName("Action Bar Icon");
		bean.setRepresentations(Sets.set(resource_25(), resource_26()));
		return bean;
	}

	// Managed
	private Resource resource_25() {
		Resource bean = session().createRaw(Resource.T, "fe48919a-5c22-4cc9-b3b5-bf8bd3d118c0");
		bean.setCreated(newGmtDate(2014, 10, 21, 18, 24, 37, 239));
		bean.setFileSize(19992l);
		bean.setMd5("a6f856c78cec2320e9b2412b61726e3a");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 16x16_08.png");
		bean.setResourceSource(moduleSource_38());
		bean.setSpecification(rasterImageSpecification_27());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_38() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "74110131-f167-45de-8327-0ec5980cb29c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2119/24/c9fddcb3-b903-403a-bd0a-576baae07558");
		return bean;
	}

	// Managed
	private Resource resource_26() {
		Resource bean = session().createRaw(Resource.T, "2a35ac2c-bcb7-4507-80dd-291db6aa295f");
		bean.setCreated(newGmtDate(2014, 10, 21, 18, 24, 37, 293));
		bean.setFileSize(21490l);
		bean.setMd5("38b953378f8166e7ef07e88c6f084f4e");
		bean.setMimeType("image/png");
		bean.setName("tf controlcenter 64x64_08.png");
		bean.setResourceSource(moduleSource_39());
		bean.setSpecification(rasterImageSpecification_9());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_39() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4b3210e0-fb38-4ed6-af49-2a25e7e279a2");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2119/24/c08dda7b-21df-4a83-b973-4620b48c0855");
		return bean;
	}

	@Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "37f5f5c8-05eb-419c-9792-2bbceb7fb2f3");
		bean.setContent(templateQueryAction_6());
		bean.setDisplayName(localizedString_44());
		bean.setIcon(adaptiveIcon_8());
		bean.setName("Global");
		bean.setParent(folder_29());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_6() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "3f33a1dd-2925-432e-8454-374cf3fb1c9f");
		bean.setDisplayName(localizedString_24());
		bean.setTemplate(template_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a132b7f0-c25d-434d-8b00-31f41287df93");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global")));
		return bean;
	}

	// Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "da7d6733-9b9e-4de6-bb44-ef322d568950");
		bean.setContent(templateQueryAction_7());
		bean.setDisplayName(localizedString_45());
		bean.setName("Perspectives");
		bean.setParent(folder_20());
		bean.setSubFolders(Lists.list(folder_33(), folder_34(), folder_35(), folder_36(), folder_37(), folder_38()));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_7() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "91f5e41c-0e2d-4cd0-8445-f7bd15352036");
		bean.setTemplate(template_8());
		return bean;
	}

	// Managed
	private Template template_8() {
		Template bean = session().createRaw(Template.T, "00702bb3-8e6b-4f8e-b69f-a33866eec852");
		bean.setMetaData(Sets.set(defaultView_7(), queryString_6()));
		bean.setPrototype(entityQuery_10());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private DefaultView defaultView_7() {
		DefaultView bean = session().createRaw(DefaultView.T, "0f709767-9010-4880-b3d1-08ff18458a75");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_6() {
		QueryString bean = session().createRaw(QueryString.T, "8a87cbb0-0fe0-4133-9b5f-e24529ad1dc9");
		bean.setValue("from WorkbenchPerspective p order by p.name");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_10() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "836970da-2497-4de0-bb99-03134a61c649");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setOrdering(simpleOrdering_3());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_3() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "7a3f52b2-c503-42a3-9d85-46bd0cc99b6b");
		bean.setDirection(OrderingDirection.ascending);
		bean.setOrderBy(propertyOperand_11());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_11() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "33aec4cc-5e82-41d0-91c0-412fa0d444d1");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_45() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6894432c-ee93-4342-943f-ab34383cfacf");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Perspectives")));
		return bean;
	}

	@Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "59bf13a6-eb21-4fc7-beac-7ed4851dee58");
		bean.setContent(templateQueryAction_8());
		bean.setDisplayName(localizedString_47());
		bean.setIcon(adaptiveIcon_9());
		bean.setName("EntryPointFolders");
		bean.setParent(folder_20());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_8() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "c6dd0e77-2c22-490a-a06a-3f60394b071c");
		bean.setDisplayName(localizedString_46());
		bean.setTemplate(template_9());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_46() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "df210a2f-e3ea-477e-95e8-37dca2897cd7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points Query")));
		return bean;
	}

	// Managed
	private Template template_9() {
		Template bean = session().createRaw(Template.T, "7a4e501f-9bc8-488b-874d-8d9d94946591");
		bean.setMetaData(Sets.set(queryString_7()));
		bean.setPrototype(entityQuery_11());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_7() {
		QueryString bean = session().createRaw(QueryString.T, "eef282d3-cf61-4fcc-87de-eaf6b951a9e7");
		bean.setValue("from WorkbenchPerspective where name = 'root'");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_11() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "1cfffbb1-189f-43e1-8002-40d71bd7d5fe");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_8());
		return bean;
	}

	// Managed
	private Restriction restriction_8() {
		Restriction bean = session().createRaw(Restriction.T, "32eb60a7-0c52-4df7-8380-a296b28d7a7f");
		bean.setCondition(valueComparison_8());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_8() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "ea415305-a791-4135-896f-6978a3cef710");
		bean.setLeftOperand(propertyOperand_12());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("root");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_12() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "26ecc817-74c5-4281-859a-9528118081fb");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_47() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "08b388b8-c3aa-46c6-a01a-0419712ec820");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_9() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "24bd048a-ca3c-4b5e-87fa-49883c0188e7");
		bean.setRepresentations(Sets.set(resource_28(), resource_27(), resource_29(), resource_30()));
		return bean;
	}

	// Managed
	private Resource resource_27() {
		Resource bean = session().createRaw(Resource.T, "669dc9d9-c96b-41ce-9241-04d9d2cdfbda");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 53, 35, 356));
		bean.setFileSize(2618l);
		bean.setMd5("1daa120c5b5b868079b3f33a92b1b29e");
		bean.setMimeType("image/png");
		bean.setName("wb_entrypoints_64x64.png");
		bean.setResourceSource(moduleSource_40());
		bean.setSpecification(rasterImageSpecification_34());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_40() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "75462553-4c74-4413-bc4d-17dd38b20d84");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/53/bcbe9ecb-4ed6-4f1c-934c-367b81710022");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_34() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "925e418d-1e59-4f18-89a4-a1a918bc2384");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_28() {
		Resource bean = session().createRaw(Resource.T, "bad56d6d-0102-4c8f-ac6c-77c6964b4551");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 53, 35, 346));
		bean.setFileSize(522l);
		bean.setMd5("ed7526a3931c632645c9661ea1760937");
		bean.setMimeType("image/png");
		bean.setName("wb_entrypoints_16x16.png");
		bean.setResourceSource(moduleSource_41());
		bean.setSpecification(rasterImageSpecification_35());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_41() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d8ca8873-f67f-4ad4-9de3-ffbd30e2e6c5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/53/d7b0784c-a787-4c0f-9e0a-416ff477dca8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_35() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "7e74a838-05b6-4f5c-b2f1-545eb513ddde");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_29() {
		Resource bean = session().createRaw(Resource.T, "df619e2b-935d-4354-a12f-74b5367c9390");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 53, 35, 353));
		bean.setFileSize(1127l);
		bean.setMd5("5a23ac215e04b05d4761f9d00176cddb");
		bean.setMimeType("image/png");
		bean.setName("wb_entrypoints_32x32.png");
		bean.setResourceSource(moduleSource_42());
		bean.setSpecification(rasterImageSpecification_36());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_42() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1952b74e-79ef-440f-98a6-7213e86026fb");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/53/a06d0d8e-1a1b-4528-965e-1cc633a34510");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_36() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2a658233-18bc-4d18-9670-4a6c297f317e");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_30() {
		Resource bean = session().createRaw(Resource.T, "e5ca647a-6061-45e4-a887-81c3ad3bda61");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 53, 35, 349));
		bean.setFileSize(812l);
		bean.setMd5("729083ffd59ce10efbacdc76804316aa");
		bean.setMimeType("image/png");
		bean.setName("wb_entrypoints_24x24.png");
		bean.setResourceSource(moduleSource_43());
		bean.setSpecification(rasterImageSpecification_37());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_43() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "088a6615-7f93-430a-bf05-f37eb73361a5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/53/409a453b-00e0-4fd8-a613-a4489f4a23fe");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_37() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "9be7268b-8d1f-4651-8a1c-94ffd2fb5e12");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "a3b9d0f1-e4d6-4f8c-9521-cc53ec42aa75");
		bean.setContent(templateQueryAction_9());
		bean.setDisplayName(localizedString_49());
		bean.setIcon(adaptiveIcon_10());
		bean.setName("HomeScreen");
		bean.setParent(folder_20());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_9() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "b46bf0ad-235a-424c-b8f4-d49b3dc256fb");
		bean.setDisplayName(localizedString_48());
		bean.setTemplate(template_10());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_48() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7a7f4d33-6a1d-4637-81e1-a36c4d58fe6b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Screen Query")));
		return bean;
	}

	// Managed
	private Template template_10() {
		Template bean = session().createRaw(Template.T, "89f37335-9d70-4dbf-aab8-e066df429246");
		bean.setPrototype(entityQuery_12());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_12() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "b89b27c1-1e77-456e-a487-2bcc890f9af7");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_9());
		return bean;
	}

	// Managed
	private Restriction restriction_9() {
		Restriction bean = session().createRaw(Restriction.T, "4ceb2f3a-38f2-4974-b3ac-b53c0f775c9e");
		bean.setCondition(valueComparison_9());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_9() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "7ff61f91-19f9-446c-8e87-3fa77cde7a27");
		bean.setLeftOperand(propertyOperand_13());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("homeFolder");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_13() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9a090d1d-16b8-4c4c-bd26-049807041473");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_49() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e9b15ed1-838d-40c2-b16a-027520026fff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Screen")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_10() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "8be27e4d-441c-4bc2-b9bf-ca6383a8cf63");
		bean.setName("Home Folder Icon");
		bean.setRepresentations(Sets.set(resource_32(), resource_33(), resource_1(), resource_31()));
		return bean;
	}

	// Managed
	private Resource resource_31() {
		Resource bean = session().createRaw(Resource.T, "3fca1bf5-c7ba-4cb3-a7bd-bf400b819db1");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 50, 23, 64));
		bean.setFileSize(1217l);
		bean.setMd5("8b637e13ca79d58b2dfc91c12739ef2e");
		bean.setMimeType("image/png");
		bean.setName("wb_home_32x32.png");
		bean.setResourceSource(moduleSource_44());
		bean.setSpecification(rasterImageSpecification_38());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_44() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "291a0a4d-0438-4d92-b02b-855e0373e647");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/50/f9d90e57-b18f-454c-847c-a5ec88ebd214");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_38() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4cd07ca3-3c3c-4c42-8100-c8d6a2d3455f");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_32() {
		Resource bean = session().createRaw(Resource.T, "b3f65572-a020-4c2b-8aeb-62e906701a9c");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 50, 23, 69));
		bean.setFileSize(2645l);
		bean.setMd5("2b32b14e80215d1f8946effadcc91ed0");
		bean.setMimeType("image/png");
		bean.setName("wb_home_64x64.png");
		bean.setResourceSource(moduleSource_45());
		bean.setSpecification(rasterImageSpecification_39());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_45() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0e607865-006e-40c3-a911-a8e1133619af");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/50/4eaa4e84-3e92-4334-991e-ba6948471c13");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_39() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "04759a73-3c1e-4e93-98a6-ee34de5cff51");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_33() {
		Resource bean = session().createRaw(Resource.T, "1685b0d9-896c-4af5-acd5-2d7c8ff3c771");
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 50, 23, 61));
		bean.setFileSize(943l);
		bean.setMd5("94fe9f8a8bf51f8f763018b22b3171b0");
		bean.setMimeType("image/png");
		bean.setName("wb_home_24x24.png");
		bean.setResourceSource(moduleSource_46());
		bean.setSpecification(rasterImageSpecification_40());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_46() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b42da660-8c88-4fd9-b0a9-def20552aa73");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/50/5b89539e-efaf-446f-b7fc-388906d3df71");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_40() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0de67dc2-170c-407e-9663-05b64b292ec1");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "8c3759be-9f39-46f6-b6d8-8ea480deb4b4");
		bean.setContent(templateQueryAction_10());
		bean.setDisplayName(localizedString_52());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Actionbar");
		bean.setParent(folder_20());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_10() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "31735e73-a301-40ae-ae6c-4cae7ce6e078");
		bean.setDisplayName(localizedString_50());
		bean.setTemplate(template_11());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_50() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d6f1abb2-3068-40d7-90b5-475bec066ce6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar Query")));
		return bean;
	}

	// Managed
	private Template template_11() {
		Template bean = session().createRaw(Template.T, "1d32a1af-29bf-4990-9e51-4feb22cbf3f3");
		bean.setMetaData(Sets.set(queryString_8()));
		bean.setName(localizedString_51());
		bean.setPrototype(entityQuery_13());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_8() {
		QueryString bean = session().createRaw(QueryString.T, "eab84720-3641-43da-9cec-744c6e58555e");
		bean.setValue("from WorkbenchPerspective where name = 'actionbar'");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_51() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bde08873-0918-4a59-aa97-53fcbda0a5cc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actionbar Template Query")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_13() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "4af8d1c6-8789-4f7c-b859-f0a6f27fc50f");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_10());
		return bean;
	}

	// Managed
	private Restriction restriction_10() {
		Restriction bean = session().createRaw(Restriction.T, "ed3ce7d9-0b89-4f19-b7a7-c362bd7b7101");
		bean.setCondition(valueComparison_10());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_10() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "bfc17658-04d4-4786-8546-5a7361136eb5");
		bean.setLeftOperand(propertyOperand_6());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_52() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cef25bdf-121c-4dca-9004-879c1cebe2e6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar")));
		return bean;
	}

	// Managed
	private Folder folder_36() {
		Folder bean = session().createRaw(Folder.T, "53ed7d0b-5881-47a9-979d-3c93596ad2aa");
		bean.setContent(templateQueryAction_11());
		bean.setDisplayName(localizedString_53());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Global Action Bar");
		bean.setParent(folder_20());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_11() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "8488b061-57e6-4f08-8c94-601d8a0d82b6");
		bean.setTemplate(template_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_53() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5dd75e89-8c57-43d6-90fb-69de11e6f877");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Action Bar")));
		return bean;
	}

	// Managed
	private Folder folder_37() {
		Folder bean = session().createRaw(Folder.T, "032449d0-5213-464c-a425-6a791dfa4b06");
		bean.setContent(templateQueryAction_1());
		bean.setDisplayName(localizedString_54());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Header Bar");
		bean.setParent(folder_20());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_54() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8bfbbbee-6720-4200-9e72-6f9c99439bd6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Header Bar")));
		return bean;
	}

	// Managed
	private Folder folder_38() {
		Folder bean = session().createRaw(Folder.T, "9ebe926d-7268-45eb-9ee1-ebfa4591e9a0");
		bean.setContent(templateQueryAction_12());
		bean.setDisplayName(localizedString_55());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Tab Action Bar");
		bean.setParent(folder_20());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_12() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "e4e25145-77b1-409c-874a-63cd214069c2");
		bean.setTemplate(template_12());
		return bean;
	}

	// Managed
	private Template template_12() {
		Template bean = session().createRaw(Template.T, "0e29b96a-fb3f-4bce-aa99-05f3033753fe");
		bean.setMetaData(Sets.set(queryString_9(), defaultView_2()));
		bean.setPrototype(entityQuery_14());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_9() {
		QueryString bean = session().createRaw(QueryString.T, "4f501245-a064-4cb9-b264-b69b56d0bd74");
		bean.setValue("from WorkbenchPerspective where name = 'tab-actionbar'");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_14() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "fd0468ca-9b87-437a-bd69-088ae5c9fd56");
		bean.setEntityTypeSignature("com.braintribe.model.workbench.WorkbenchPerspective");
		bean.setRestriction(restriction_11());
		return bean;
	}

	// Managed
	private Restriction restriction_11() {
		Restriction bean = session().createRaw(Restriction.T, "10a72d3d-b1e6-4117-a3c9-36425ff05202");
		bean.setCondition(valueComparison_11());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_11() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "dbc9da83-0671-47f5-b310-c45276562161");
		bean.setLeftOperand(propertyOperand_14());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("tab-actionbar");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_14() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "d0df3156-3ec8-42e3-b260-0aa194614224");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_55() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "509866c1-9041-4087-9ad3-9e0a6b6f7c13");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tab Action Bar")));
		return bean;
	}

	@Managed
	private Folder folder_39() {
		Folder bean = session().createRaw(Folder.T, "4e548abd-b279-4ae6-9723-0229531ed56c");
		bean.setDisplayName(localizedString_56());
		bean.setName("Styling");
		bean.setSubFolders(Lists.list(folder_40()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_56() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d1e735f3-4f38-4924-a440-56864e8b3677");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Styling")));
		return bean;
	}

	// Managed
	private Folder folder_40() {
		Folder bean = session().createRaw(Folder.T, "f307756d-74b2-41d8-91a8-5a583b44e945");
		bean.setContent(templateQueryAction_2());
		bean.setDisplayName(localizedString_57());
		bean.setName("Configuration");
		bean.setParent(folder_39());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_57() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c7ba271e-928e-4e5a-8412-50d84573fd4b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configuration")));
		return bean;
	}

	// Managed
	private Folder folder_41() {
		Folder bean = session().createRaw(Folder.T, "a3b59f67-5f01-487e-8e90-a6e2f013667d");
		bean.setDisplayName(localizedString_4());
		bean.setName("$detailsPanelVisibility");
		return bean;
	}

	// Managed
	private Folder folder_42() {
		Folder bean = session().createRaw(Folder.T, "d940c7f4-4aee-46f5-bf6b-42633ad868be");
		bean.setDisplayName(localizedString_58());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_58() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d4d8d6b8-cab2-48d3-8556-0b043c2dd985");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Change")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_59() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "82d4af7c-38f6-440d-93b3-1033a6c2c0de");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Record")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "01e21ad9-dc46-4a31-b23d-cb5eb7488e2b");
		bean.setDisplayName(localizedString_60());
		bean.setFolders(Lists.list(folder_28(), folder_33(), folder_34(), folder_35(), folder_31(), folder_25()));
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_60() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "74656f78-5e55-4e98-a4eb-00f7029e7ec7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_61() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "809cae75-87a2-48dd-b661-2531ea608f37");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "List View")));
		return bean;
	}

	// Managed
	private Folder folder_43() {
		Folder bean = session().createRaw(Folder.T, "1e2ef49e-4f2c-4ea6-ba54-acbbfc8faab7");
		bean.setDisplayName(localizedString_62());
		bean.setName("$condenseEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_62() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6ac9a8b1-476a-4442-b816-9e78fd8aed1c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_63() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3bd33552-0f1a-45c8-ad20-fed28fb8fc9f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private Folder folder_44() {
		Folder bean = session().createRaw(Folder.T, "c5e48785-7967-4cbd-af71-0a0952f7aa15");
		bean.setDisplayName(localizedString_64());
		bean.setName("$exchangeContentView");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_64() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c48bba6a-df4f-4d14-bb69-e82668518d40");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange View")));
		return bean;
	}

	// Managed
	private Folder folder_45() {
		Folder bean = session().createRaw(Folder.T, "f24fe516-ed97-4fa2-b8bd-ad470680c6f7");
		bean.setDisplayName(localizedString_65());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("View");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_65() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "eb6f96cb-3211-4e47-a1ed-ef42cfcca51c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private Folder folder_46() {
		Folder bean = session().createRaw(Folder.T, "1799c0aa-ee1d-4830-84ed-650beca65e3b");
		bean.setDisplayName(localizedString_66());
		bean.setName("$condenseEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_66() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e94d1922-7fee-4797-baf4-0c36ff47290a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Condensation")));
		return bean;
	}

	// Managed
	private Folder folder_47() {
		Folder bean = session().createRaw(Folder.T, "b071628f-81be-4964-998d-d6c89b1e0beb");
		bean.setDisplayName(localizedString_61());
		bean.setName("$displayMode");
		return bean;
	}

	// Managed
	private Folder folder_48() {
		Folder bean = session().createRaw(Folder.T, "e58ebc00-b94a-44f3-a18d-57b89ac5aaca");
		bean.setDisplayName(localizedString_59());
		bean.setName("$recordTemplateScript");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_69() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "077ea185-43c1-47f2-89ab-771229fe203a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private Folder folder_49() {
		Folder bean = session().createRaw(Folder.T, "3c66c654-80d3-475e-81dd-68820b1c52c7");
		bean.setDisplayName(localizedString_10());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_70() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2560c634-2f1c-4738-9771-42eaedc01824");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open Modeller")));
		return bean;
	}

	// Managed
	private Folder folder_50() {
		Folder bean = session().createRaw(Folder.T, "caee3c6d-fb36-4b5b-8c57-ca39aef77974");
		bean.setDisplayName(localizedString_69());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private Folder folder_51() {
		Folder bean = session().createRaw(Folder.T, "6d600ba9-365c-41f3-9b2b-d54161517064");
		bean.setDisplayName(localizedString_70());
		bean.setName("$openModel");
		return bean;
	}

	// Managed
	private Folder folder_52() {
		Folder bean = session().createRaw(Folder.T, "da6e9e35-1938-4625-b8f1-272eb9038434");
		bean.setDisplayName(localizedString_71());
		bean.setName("$openGmeForAccessInNewTab");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_71() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "97014c5b-277a-4653-82d8-38405fa3537e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	// Managed
	private Folder folder_53() {
		Folder bean = session().createRaw(Folder.T, "f1b944b8-eec7-4a19-b19d-333a631371ed");
		bean.setDisplayName(localizedString_25());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_56() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ef36ab8e-edeb-4e06-bb11-ef4881aef6bb");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_54() {
		Folder bean = session().createRaw(Folder.T, "39231eef-cb71-44ad-a30e-beaff424d1b9");
		bean.setDisplayName(localizedString_72());
		bean.setIcon(adaptiveIcon_11());
		bean.setName("MoreActions");
		bean.setSubFolders(Lists.list(folder_16(), folder_2()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_72() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c1be24b8-0e62-44e9-bd88-4fdbf795c763");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "More Actions")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_11() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e7dfc85d-50c3-482f-b0ac-b94f1a0468c7");
		bean.setRepresentations(Sets.set(resource_41(), resource_40()));
		return bean;
	}

	// Managed
	private Resource resource_40() {
		Resource bean = session().createRaw(Resource.T, "6052ad29-1162-4ee3-870b-050fbd984096");
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 24, 40, 822));
		bean.setFileSize(1102l);
		bean.setMd5("5fd2881619e89494c8c2cfbacf577457");
		bean.setMimeType("image/png");
		bean.setName("more_32x32.png");
		bean.setResourceSource(moduleSource_63());
		bean.setSpecification(rasterImageSpecification_58());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_63() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "feda38ae-c29e-46e8-bddb-46a733b4782d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/24/ddddf1d5-08a6-48a2-8439-784c13c960c0");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_58() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "765499b6-f13d-4e54-8436-acc546daea82");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_41() {
		Resource bean = session().createRaw(Resource.T, "75986ca5-d000-4957-8e45-7b74bf78071b");
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 24, 35, 199));
		bean.setFileSize(487l);
		bean.setMd5("7c932faa5ed19e59da4108a195131f7c");
		bean.setMimeType("image/png");
		bean.setName("more_16x16.png");
		bean.setResourceSource(moduleSource_31());
		bean.setSpecification(rasterImageSpecification_56());
		return bean;
	}

	// Managed
	private Folder folder_55() {
		Folder bean = session().createRaw(Folder.T, "7ba0df0c-354a-46b1-9bb8-29338200ef8a");
		bean.setDisplayName(localizedString_73());
		bean.setName("$openGmeForAccessInNewTab");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_73() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d14fa1a1-1f1f-4ce2-b76a-f8052161a467");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_74() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8e96e9da-80a7-4d2d-a42c-46be408a0cdc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actionbar")));
		return bean;
	}

	// Managed
	private Folder folder_56() {
		Folder bean = session().createRaw(Folder.T, "6e273572-389f-449f-aa79-beb6fe1e4759");
		bean.setDisplayName(localizedString_75());
		bean.setName("$openModel");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_75() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "45568f1c-e123-4a8f-a680-be5e64138f92");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open Modeller")));
		return bean;
	}

	// Managed
	private Folder folder_57() {
		Folder bean = session().createRaw(Folder.T, "bd062650-6b92-4ff9-8012-fe2ee26a89ed");
		bean.setDisplayName(localizedString_7());
		bean.setName("actionbar2");
		bean.setSubFolders(Lists.list(folder_58(), folder_59(), folder_60(), folder_18(), folder_42(), folder_61(), folder_62(), folder_50(),
				folder_49(), folder_53(), folder_44(), folder_63(), folder_43(), folder_64(), folder_65(), folder_55(), folder_66(), folder_17(),
				folder_67(), folder_48(), folder_51()));
		return bean;
	}

	// Managed
	private Folder folder_58() {
		Folder bean = session().createRaw(Folder.T, "8e8108a2-19dd-44dc-991b-271c6ac3f3b5");
		bean.setDisplayName(localizedString_76());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_76() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9266eef8-02df-4c50-9a10-39fa2612255b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private Folder folder_59() {
		Folder bean = session().createRaw(Folder.T, "db3cb810-cacc-4ebf-a06b-af46a804f73b");
		bean.setDisplayName(localizedString_9());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private Folder folder_60() {
		Folder bean = session().createRaw(Folder.T, "bb8dd2e4-ac45-4c27-9a84-ebb924c8a8d6");
		bean.setDisplayName(localizedString_29());
		bean.setName("$instantiateEntity");
		return bean;
	}

	// Managed
	private Folder folder_61() {
		Folder bean = session().createRaw(Folder.T, "4190d824-84be-4872-b874-4f5ffd599c27");
		bean.setDisplayName(localizedString_77());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_77() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "835ceb35-04fb-4f45-8648-00927e863338");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_62() {
		Folder bean = session().createRaw(Folder.T, "cb9acef9-ad3a-4d25-9a6a-db495bbe40d7");
		bean.setDisplayName(localizedString_63());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private Folder folder_63() {
		Folder bean = session().createRaw(Folder.T, "7f46d1fa-4907-4066-ad98-7daf0e4b94d3");
		bean.setDisplayName(localizedString_78());
		bean.setName("$uncondenseLocal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_78() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0a93ea94-fea0-46d6-95a9-0c93ab7cc587");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Hide Details Here")));
		return bean;
	}

	// Managed
	private Folder folder_64() {
		Folder bean = session().createRaw(Folder.T, "c8829c59-efcf-4c5e-939d-36bb9be7efab");
		bean.setDisplayName(localizedString_79());
		bean.setName("$addToClipboard");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_79() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f72a17a2-f03e-433b-9515-cc94eb0cf789");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add To Clipboard")));
		return bean;
	}

	// Managed
	private Folder folder_65() {
		Folder bean = session().createRaw(Folder.T, "8bbf1b14-171e-4cb7-b91d-b792e05799b1");
		bean.setDisplayName(localizedString_6());
		bean.setName("$displayMode");
		return bean;
	}

	// Managed
	private Folder folder_66() {
		Folder bean = session().createRaw(Folder.T, "3d6f6d8a-1db4-4cfd-8485-5b96e750d565");
		bean.setDisplayName(localizedString_80());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_80() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6919c0ea-c64f-4fa6-8b1d-591f68c94180");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private Folder folder_67() {
		Folder bean = session().createRaw(Folder.T, "2dfe987e-209f-486c-a6d7-60b4a7ab9a50");
		bean.setDisplayName(localizedString_81());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_81() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ebb0af83-fd4b-4a43-8349-d3ce7275250d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download Resource")));
		return bean;
	}

	// Managed
	private Folder folder_68() {
		Folder bean = session().createRaw(Folder.T, "79d959b6-0cf6-4b9f-81fe-fd5391d8a245");
		bean.setDisplayName(localizedString_82());
		bean.setName("$uncondenseLocal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_82() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "68fb7182-129a-44c2-8c95-9d37372355df");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Uncondense")));
		return bean;
	}

	// Managed
	private Folder folder_69() {
		Folder bean = session().createRaw(Folder.T, "dd4c88e5-cf54-4a44-8d9b-bbf15d430c7a");
		bean.setDisplayName(localizedString_74());
		bean.setIcon(adaptiveIcon_9());
		bean.setName("Actionbar");
		bean.setParent(folder_21());
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "8a9932fb-b59f-45b1-862c-8482deb8729e");
		bean.setDisplayName(localizedString_26());
		bean.setFolders(Lists.list(folder_22()));
		bean.setName("root");
		return bean;
	}

	private Date newGmtDate(int year, int month, int day, int hours, int minutes, int seconds, int millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);
		calendar.set(Calendar.MILLISECOND, millis);
		return calendar.getTime();
	}

}