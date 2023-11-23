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
package tribefire.cortex.assets.cortex_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.cortexapi.access.ConfigureWorkbench;
import com.braintribe.model.cortexapi.access.ExplorerStyle;
import com.braintribe.model.cortexapi.access.RunGarbageCollection;
import com.braintribe.model.cortexapi.access.SetupAspects;
import com.braintribe.model.cortexapi.access.SetupWorkbench;
import com.braintribe.model.cortexapi.connection.CreateModelFromDbSchema;
import com.braintribe.model.cortexapi.connection.SynchronizeDbSchema;
import com.braintribe.model.cortexapi.connection.SynchronizeModelWithDbSchema;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnection;
import com.braintribe.model.cortexapi.connection.TestDatabaseConnections;
import com.braintribe.model.cortexapi.model.AddDependencies;
import com.braintribe.model.cortexapi.model.AddToCortexModel;
import com.braintribe.model.cortexapi.model.CreateModel;
import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.cortexapi.model.ValidateModel;
import com.braintribe.model.cortexapi.workbench.CreateServiceRequestTemplate;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.DeployWithDeployables;
import com.braintribe.model.deploymentapi.request.DeploymentMode;
import com.braintribe.model.deploymentapi.request.RedeployWithDeployables;
import com.braintribe.model.deploymentapi.request.UndeployWithDeployables;
import com.braintribe.model.deploymentreflection.request.GetDeploymentStatusNotification;
import com.braintribe.model.exchangeapi.EncodingType;
import com.braintribe.model.exchangeapi.Export;
import com.braintribe.model.exchangeapi.ExportAndWriteToResource;
import com.braintribe.model.exchangeapi.ExportDescriptor;
import com.braintribe.model.exchangeapi.ReadFromResource;
import com.braintribe.model.exchangeapi.ReadFromResourceAndImport;
import com.braintribe.model.exchangeapi.WriteToResource;
import com.braintribe.model.exchangeapi.predicate.DefaultPredicate;
import com.braintribe.model.exchangeapi.supplier.StaticSupplier;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.pr.criteria.ComparisonOperator;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.NegationCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeStrategy;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.logic.TypeConditionConjunction;
import com.braintribe.model.generic.typecondition.logic.TypeConditionNegation;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateInstantiationServiceRequestAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.meta.DefaultView;
import com.braintribe.model.workbench.meta.QueryString;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.cortex_wb.initializer.wire.contract.CortexWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.cortex.model.check.CheckCoverage;
import tribefire.module.model.resource.ModuleSource;

@Managed
@SuppressWarnings("deprecation")
public class CortexWbInitializerSpace extends AbstractInitializerSpace implements CortexWbInitializerContract {

	@Override
	public void initialize() {
		folder_1();
		folder_34();
		folder_78();
		folder_83();
		folder_84();
		folder_85();
		folder_87();
		folder_90();
		folder_91();
		folder_96();
		folder_100();
		folder_102();
		folder_105();
		folder_106();
		folder_116();
		folder_118();
		folder_119();
		folder_120();
		folder_121();
		folder_124();
		folder_125();
		folder_126();
		folder_128();
		folder_129();
		folder_132();
		folder_133();
		folder_134();
		folder_140();
		folder_142();
		folder_143();
		folder_144();
		folder_149();
		workbenchPerspective_1();
		workbenchPerspective_2();
		workbenchPerspective_3();
		workbenchPerspective_4();
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "9a4a1ac1-5435-4556-ad09-07757bf1529e");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "0822d563-7476-4565-bd97-1dfb167c3043");
		bean.setName("ExportExchangePackage Icon");
		bean.setRepresentations(Sets.set(resource_4(), resource_2(), resource_3()));
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "7567d6d4-02cd-4cc5-9627-268a71861171");
		bean.setFileSize(3289l);
		bean.setCreated(newGmtDate(2015, 4, 30, 16, 14, 1, 590));
		bean.setName("ExportPackage_64x64.png");
		bean.setSpecification(rasterImageSpecification_5());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_2());
		bean.setMd5("98f4cd52cee8f5c6a12a0cf985a709c1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f27928ac-090c-4887-a2c1-24010dd36bf8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3018/14/7ed7bf1d-b56b-4849-8947-164208c00d65");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_5() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "84bf1dac-c98b-4101-b1b7-f2e755eac335");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_3() {
		Resource bean = session().createRaw(Resource.T, "76cd8595-202e-4950-81bc-25009408f5be");
		bean.setFileSize(1382l);
		bean.setCreated(newGmtDate(2015, 4, 30, 16, 14, 1, 585));
		bean.setName("ExportPackage_32x32.png");
		bean.setSpecification(rasterImageSpecification_6());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_3());
		bean.setMd5("073e6557bc6547f60c8e9dc33ee2bdae");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_3() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "fa86204a-f295-442e-8e9f-869b60845909");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3018/14/1b3f63ad-05c9-4923-8c62-34301fcdb850");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_6() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d872de5c-781a-4b01-a270-28f785af6465");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_4() {
		Resource bean = session().createRaw(Resource.T, "9eb0ee4a-680a-4c08-adab-73407c5eb52c");
		bean.setFileSize(634l);
		bean.setCreated(newGmtDate(2015, 4, 30, 16, 14, 1, 565));
		bean.setName("ExportPackage_16x16.png");
		bean.setSpecification(rasterImageSpecification_7());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_4());
		bean.setMd5("da84e78e2b0b64cbe172363ba497a451");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_4() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4a074c26-e4bb-4281-8295-415b1be15f88");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3018/14/ec082a5d-38b7-45a1-b8f9-338ac634c1ef");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_7() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "015bda9e-80d3-4411-bcbf-b1c923a89f4e");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Variable variable_2() {
		Variable bean = session().createRaw(Variable.T, "67fd8c1f-91b6-4517-b44f-a5a3d4264326");
		bean.setTypeSignature("string");
		bean.setDefaultValue("custom.model");
		bean.setName("Group Id");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_1() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "715b62de-5c28-4ce7-b0a4-3fbea235cf47");
		bean.setOrderBy(propertyOperand_2());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_2() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "50ed8116-12b4-40c3-b175-0e4720fd712a");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_9() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2ca3c1e9-e1ad-430c-8b99-10a5e825e996");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_1() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "64bbf163-d02e-4597-a263-9fd02f5913cc");
		return bean;
	}

	// Managed
	private Resource resource_5() {
		Resource bean = session().createRaw(Resource.T, "ed16e536-df00-4e3a-926a-30aaa976348e");
		bean.setFileSize(1527l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 52, 25, 359));
		bean.setName("Condensation_64x64.png");
		bean.setSpecification(rasterImageSpecification_10());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_5());
		bean.setMd5("bdda528fea06f1c1d754244d5c55962e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_5() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "80b733b2-c999-4625-ae9f-3d6fa5abe7f4");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/52/323a1920-ef7c-49ad-a648-20ba6227e2e7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_10() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "966d3f33-6dac-442a-afa8-3b70a9d7fda0");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private QueryString queryString_1() {
		QueryString bean = session().createRaw(QueryString.T, "e73a3531-4707-49b9-a0b0-59b263b4bde5");
		bean.setValue("from HardwiredAccess order by name");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_7() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5e1793c5-454f-4427-90a8-552c5923de85");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/37/5e39a611-4c1b-4406-b7ce-c88694abd3d1");
		return bean;
	}

	// Managed
	private Priority priority_1() {
		Priority bean = session().createRaw(Priority.T, "7eb975be-1bf6-4d4f-bc4f-3ece13ec60c7");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.93d);
		return bean;
	}

	@Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "7b9c77a7-70dc-4707-8d7b-adc071bbc734");
		bean.setParent(folder_2());
		bean.setSubFolders(Lists.list(folder_28(), folder_29(), folder_30(), folder_31(), folder_32()));
		bean.setDisplayName(localizedString_6());
		bean.setName("Processing");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d48083da-60bf-40fd-bf5f-d307e0e0410e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Processing")));
		return bean;
	}

	@Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "0ac46814-7a79-4af7-bd81-8a43671fbc14");
		bean.setParent(folder_3());
		bean.setSubFolders(Lists.list(folder_12(), folder_17(), folder_1(), folder_19()));
		bean.setDisplayName(localizedString_7());
		bean.setName("Smart Enterprise Information");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_7() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5fd04f74-6e84-44a9-9fe1-332041743370");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Smart Enterprise Information")));
		return bean;
	}

	@Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "d4e3def1-10f4-44d0-8c41-8a382509c83b");
		bean.setSubFolders(Lists.list(folder_2(), folder_4()));
		bean.setDisplayName(localizedString_8());
		bean.setName("root");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_8() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "353eb6e2-55d2-4742-a5f7-87880ce6241c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "3d95781c-dafc-4b6a-90a1-3ddca7e45ef5");
		bean.setParent(folder_3());
		bean.setSubFolders(Lists.list(folder_166(), folder_168(), folder_173(), folder_5(), folder_8()));
		bean.setDisplayName(localizedString_9());
		bean.setName("System");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_9() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d60272e3-6d98-43f3-8740-40daa2aa7f11");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System")));
		return bean;
	}

	@Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "0ab9e8b8-969a-4cdf-891c-a91c3572f269");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_6()));
		bean.setDisplayName(localizedString_10());
		bean.setName("Cartridges");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_10() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b854495f-22f2-4216-9e5f-e10402cee051");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Cartridges")));
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "08dbfc2c-19f4-4b02-9dea-7e0b2777c30a");
		bean.setParent(folder_5());
		bean.setDisplayName(localizedString_12());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("ShowCartriges");
		bean.setContent(simpleQueryAction_1());
		return bean;
	}

	@Managed
	private SimpleQueryAction simpleQueryAction_1() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "ac94a9de-f8e5-4486-bde5-a6ea2eb2ca93");
		bean.setTypeSignature("com.braintribe.model.deployment.Cartridge");
		bean.setDisplayName(localizedString_11());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_11() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f2bf55b4-042f-4d03-96e5-1117067991b4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Cartridges Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_12() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1680bb73-5c60-4910-96c3-b7c938fda1c4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show All")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_2() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "4153ef05-a015-4ccd-a1d4-0c1e40336e00");
		bean.setRepresentations(Sets.set(resource_9(), resource_8(), resource_10(), resource_7()));
		return bean;
	}

	// Managed
	private Resource resource_7() {
		Resource bean = session().createRaw(Resource.T, "4c3fc4ad-6000-45ba-b1d6-346c535a245c");
		bean.setFileSize(957l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 42, 177));
		bean.setName("Cartridges_32x32.png");
		bean.setSpecification(rasterImageSpecification_13());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_9());
		bean.setMd5("1ddbac16736a2cb6d008c140e50e3e5c");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_9() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "998c53d9-372e-4286-9af2-50851be03827");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/857c0d61-a970-47e4-b999-4b91e914739c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_13() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f5509c25-dbb5-4f70-a68e-ad281518c75a");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_8() {
		Resource bean = session().createRaw(Resource.T, "475d0ec6-a639-46de-8b8f-5a88305f0524");
		bean.setFileSize(21310l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 45, 50, 219));
		bean.setName("Cartridges_64x64.png");
		bean.setSpecification(rasterImageSpecification_14());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_10());
		bean.setMd5("c0cf5809b26110d213fb6e6fde745d54");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_10() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bc128482-85d8-42a6-a6b7-c09b0904e2f7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/45/1bc65ce1-4322-4eb2-a280-e00a59995cb9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_14() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4d7c6797-959c-4ef3-b01c-1ffc7c7e83b2");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_9() {
		Resource bean = session().createRaw(Resource.T, "e0db43f1-7b4d-47d1-99de-67e7628deebd");
		bean.setFileSize(448l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 42, 171));
		bean.setName("Cartridges_16x16.png");
		bean.setSpecification(rasterImageSpecification_15());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_11());
		bean.setMd5("c704dd266e8864cf3c087f7477f351e9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_11() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7555dc48-8cc4-4f79-b584-7447d4f77205");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/e85f2f81-41e4-4a5c-a80e-231e7bf27c82");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_15() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8974ba17-3da6-4d0b-9224-15cd5b819715");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_10() {
		Resource bean = session().createRaw(Resource.T, "45105958-5499-4977-86f7-7e3e9e001ef5");
		bean.setFileSize(703l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 42, 174));
		bean.setName("Cartridges_24x24.png");
		bean.setSpecification(rasterImageSpecification_16());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_12());
		bean.setMd5("f6980dfffcacaa652f9e5d15e051e1bb");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_12() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "dc9c4068-3894-4724-9545-a0ef10739c9a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/7e81d79c-8dd0-43a2-b30f-ca1d7c48ab53");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_16() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "223b67ea-ddb9-4109-ac08-094028cb9b8d");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "be37578b-47a9-478c-84c4-a2a702c306d9");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_9(), folder_10(), folder_161(), folder_11()));
		bean.setDisplayName(localizedString_17());
		bean.setName("Resources");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_17() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5055e635-2069-41bd-9e06-49db519be1ed");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resources")));
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "cfeb763f-db3a-448c-85d8-fc3f713e0b6c");
		bean.setParent(folder_8());
		bean.setDisplayName(localizedString_19());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("Images");
		bean.setContent(templateQueryAction_1());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_1() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "027773cc-38bd-42b2-a9e6-96c795255cb7");
		bean.setTemplate(template_2());
		bean.setDisplayName(localizedString_18());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_18() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fb9df7cf-0911-4302-8dfc-bf07110fc2dc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images Query")));
		return bean;
	}

	// Managed
	private Template template_2() {
		Template bean = session().createRaw(Template.T, "9a56c128-46f9-4f22-8615-359ebda63549");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_1());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_1() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "7552bb11-075b-4c00-8a27-a47bcf77127b");
		bean.setRestriction(restriction_1());
		bean.setEntityTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private Restriction restriction_1() {
		Restriction bean = session().createRaw(Restriction.T, "fc607d9a-a6ed-4761-bc30-a5071c39d224");
		bean.setCondition(valueComparison_3());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_3() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "f1357e84-635a-409c-8086-99c463609d4b");
		bean.setLeftOperand(propertyOperand_5());
		bean.setRightOperand("image*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_5() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "455b1a3e-f510-428c-b28f-81131d3cc449");
		bean.setPropertyName("mimeType");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_19() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "70f243ef-76d7-472b-a835-67011ceb61d7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_5() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "3f645f67-c620-4bdc-a5bf-0442c6d9b57b");
		bean.setName("Images Icon");
		bean.setRepresentations(Sets.set(resource_19(), resource_21(), resource_20(), resource_18()));
		return bean;
	}

	// Managed
	private Resource resource_18() {
		Resource bean = session().createRaw(Resource.T, "7e6f3f62-8212-41f6-9da1-c4cc57727268");
		bean.setFileSize(611l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 30, 52, 933));
		bean.setName("Image_32x32.png");
		bean.setSpecification(rasterImageSpecification_24());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_20());
		bean.setMd5("399397ff68aaa4e9bfc0ecd66fb3d5ee");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_20() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "91615cee-e317-4f46-a0fe-0ecf2c0af860");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/30/3a7557a9-4bcf-42bc-9285-2df166af4072");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_24() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "68cc197a-a94b-4b2d-9ad5-d0ba28913fa7");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_19() {
		Resource bean = session().createRaw(Resource.T, "a862dd3b-a474-42e6-97ca-78afed8ae067");
		bean.setFileSize(1301l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 30, 52, 936));
		bean.setName("Image_64x64.png");
		bean.setSpecification(rasterImageSpecification_25());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_21());
		bean.setMd5("7aa095fe0ecce7b44e731da00eda3a3f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_21() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a3a36529-fbcd-4bc8-88e4-0a40897b057d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/30/f7bc3774-fefe-4e7a-98c8-f4afec4e3499");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_25() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b216331d-5c65-4f33-9187-ed717c37cfc6");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_20() {
		Resource bean = session().createRaw(Resource.T, "32f4d1d6-25cd-4504-9076-a0591190fab0");
		bean.setFileSize(468l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 30, 52, 931));
		bean.setName("Image_24x24.png");
		bean.setSpecification(rasterImageSpecification_26());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_22());
		bean.setMd5("360498ea2ef2bad1a5f62b270988ceaf");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_22() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "42451d21-06f9-4fe4-802d-17c343272a51");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/30/484858a5-8f64-4508-934e-17e411e4f8ad");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_26() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8b9b9047-96e9-4925-8796-e06380f32021");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_21() {
		Resource bean = session().createRaw(Resource.T, "be731b19-45d8-45de-8bcc-07225c69cd82");
		bean.setFileSize(343l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 30, 52, 927));
		bean.setName("Image_16x16.png");
		bean.setSpecification(rasterImageSpecification_27());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_23());
		bean.setMd5("9dbb3e1d784eff84edaf22a3e6a62d86");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_23() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "faf9de55-80e9-4e2b-ae7a-d6a14e4bc12a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/30/c66d939f-3149-4101-a822-2265dfd06b84");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_27() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "434a044b-81c3-491d-8e56-23209afb8912");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "754daa50-f151-4dcf-97e4-a3b4eb1de7bc");
		bean.setParent(folder_8());
		bean.setDisplayName(localizedString_21());
		bean.setIcon(adaptiveIcon_6());
		bean.setName("Files");
		bean.setContent(simpleQueryAction_2());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_2() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "f3ea411c-ba67-4de8-8513-8f8f474b7b67");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		bean.setDisplayName(localizedString_20());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_20() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7d4ff43a-36b3-4a2a-a4ce-60ecba227d06");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_21() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "99d86b21-7d1a-4e36-b275-b2c919e4b397");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_6() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e9701255-ecd7-49b8-aa9e-b86272a7744a");
		bean.setName("Files Icon");
		bean.setRepresentations(Sets.set(resource_24(), resource_22(), resource_25(), resource_23()));
		return bean;
	}

	// Managed
	private Resource resource_22() {
		Resource bean = session().createRaw(Resource.T, "7cd7d237-feb7-4689-91e9-7106ce751214");
		bean.setFileSize(310l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 31, 36, 141));
		bean.setName("Files_16x16.png");
		bean.setSpecification(rasterImageSpecification_28());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_24());
		bean.setMd5("fb63c47fe7ab05eda28aaae72e892775");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_24() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4b277307-bc4b-445c-819e-f96a9859e27b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/31/9ca520ea-3358-454c-b0e9-ac98d0703c39");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_28() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "13b2693e-dbe2-423b-bfe9-9ac820698d7b");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_23() {
		Resource bean = session().createRaw(Resource.T, "cf4507c3-989f-49d9-9e58-0250acad3e1a");
		bean.setFileSize(401l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 31, 36, 145));
		bean.setName("Files_24x24.png");
		bean.setSpecification(rasterImageSpecification_29());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_25());
		bean.setMd5("cb5c22359b4e52be16d0ba189479b873");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_25() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "664e095d-f919-48d8-8bcb-9670214d5b76");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/31/9efef30b-4f0f-4c82-a551-77d4c1e3d487");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_29() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4b754c14-ba66-4c1d-96bb-1d4ac8d4c64c");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_24() {
		Resource bean = session().createRaw(Resource.T, "5b3f3547-9a8b-4741-80fe-1ceb3a7cba59");
		bean.setFileSize(949l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 31, 36, 150));
		bean.setName("Files_64x64.png");
		bean.setSpecification(rasterImageSpecification_30());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_26());
		bean.setMd5("bf7dcfe6a27fe311db96d5eb87cfa3e2");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_26() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f0ff4ea0-1367-41de-a253-fd676d3db46e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/31/76afe080-c52c-48ac-81f5-e34999163633");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_30() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "bb17b7ff-703a-4108-866c-f0d1a8d511ba");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_25() {
		Resource bean = session().createRaw(Resource.T, "c7968d34-caa0-4e97-a228-d9133b802cea");
		bean.setFileSize(490l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 31, 36, 147));
		bean.setName("Files_32x32.png");
		bean.setSpecification(rasterImageSpecification_31());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_27());
		bean.setMd5("9a71b9cfe4300ebb6b4b5dcd620135cf");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_27() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "93a80d15-bb96-4870-b609-a1fc3b41800d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/31/82ac6fde-83dc-46cb-8faf-f2dacfbda996");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_31() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b9305590-e2e1-4e27-b93c-0d0631bf915d");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "f2651fdd-3944-4f84-8223-916d8babe7da");
		bean.setParent(folder_8());
		bean.setDisplayName(localizedString_23());
		bean.setIcon(adaptiveIcon_7());
		bean.setName("PropertyGroups");
		bean.setContent(simpleQueryAction_3());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_3() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "45c12713-bb82-4f15-85b8-68afae2a498f");
		bean.setTypeSignature("com.braintribe.model.meta.data.display.Group");
		bean.setDisplayName(localizedString_22());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_22() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "20e3ecca-2e6e-4c85-83cf-90cb1fe26236");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Property Groups Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "905bcaa0-43a2-400a-9314-fa7cb934bc30");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Property Groups")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_7() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "520ff271-165f-41e8-bbad-9931a15a42d4");
		bean.setRepresentations(Sets.set(resource_26(), resource_27(), resource_29(), resource_28()));
		return bean;
	}

	// Managed
	private Resource resource_26() {
		Resource bean = session().createRaw(Resource.T, "e51df6f8-b9e9-4a26-9b8b-9b8f58841f23");
		bean.setFileSize(613l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 13, 615));
		bean.setName("Groups_24x24.png");
		bean.setSpecification(rasterImageSpecification_32());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_28());
		bean.setMd5("0bcc5572179fc42b2fd8a432d1048a8d");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_28() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "aef719f3-00d2-4115-8cfa-0282d3992335");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/397f934e-ff74-4372-b2b6-c08c1c8bb3a3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_32() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "20810ce3-6dde-4d95-8e6f-869023cdc0e1");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_27() {
		Resource bean = session().createRaw(Resource.T, "00b3c9ae-195b-4016-93c1-75e1b17446ee");
		bean.setFileSize(409l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 13, 611));
		bean.setName("Groups_16x16.png");
		bean.setSpecification(rasterImageSpecification_33());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_29());
		bean.setMd5("2f15139b6df98ffa740b05319eeb3ce7");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_29() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1d54dc85-ae4c-4a4e-baa9-ebd5a0f25050");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/194cdddb-2495-41aa-a221-dd6b4fc6e1e7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_33() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "04c346c9-60e3-49b2-adbd-cec245af22c8");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_28() {
		Resource bean = session().createRaw(Resource.T, "a05c6a85-7189-4d58-993f-d95e40521a89");
		bean.setFileSize(825l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 13, 619));
		bean.setName("Groups_32x32.png");
		bean.setSpecification(rasterImageSpecification_34());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_30());
		bean.setMd5("81fc43201b81e3e8f6d5980381dbc06f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_30() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c7a49231-841d-4b95-bda9-321a2c4e695e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/ca750f32-3bcd-40a3-94d2-df64d36d4fa6");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_34() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5f10baef-1bd5-44e6-81fe-2bf2bbe7a755");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_29() {
		Resource bean = session().createRaw(Resource.T, "cd563977-8dc6-4834-bda7-73af011f4f9b");
		bean.setFileSize(1815l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 13, 622));
		bean.setName("Groups_64x64.png");
		bean.setSpecification(rasterImageSpecification_35());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_31());
		bean.setMd5("b185a2c0c1856935801e22945cdce834");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_31() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e96a7d67-f3d6-44fd-8127-94ad1072e49b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/cec40b17-80a9-41a0-9384-4a3664ad8799");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_35() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "556c2c1b-0c16-431e-b10e-8ff2749d567f");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	@Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "53fd35b6-3317-4109-9a10-34f8b343ee26");
		bean.setParent(folder_2());
		bean.setSubFolders(Lists.list(folder_13(), folder_14(), folder_15(), folder_16()));
		bean.setDisplayName(localizedString_24());
		bean.setName("Modeling");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2c6d6b11-3832-4a80-b86b-531a9e350575");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modeling")));
		return bean;
	}

	@Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "5e277a83-62c7-46e1-982c-3d0375c1fa0a");
		bean.setParent(folder_12());
		bean.setDisplayName(localizedString_26());
		bean.setIcon(adaptiveIcon_8());
		bean.setName("CustomModels");
		bean.setContent(templateQueryAction_2());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_2() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "5dcbe10f-2ab5-4e4b-80b5-1656b91063dd");
		bean.setTemplate(template_3());
		bean.setDisplayName(localizedString_25());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f333a45b-d4c7-4254-b25e-b23675aedf04");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Models Query")));
		return bean;
	}

	// Managed
	private Template template_3() {
		Template bean = session().createRaw(Template.T, "819188b2-1cc6-4553-9333-a288af023056");
		bean.setMetaData(Sets.set(queryString_2()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_36());
		return bean;
	}

	// Managed
	private QueryString queryString_2() {
		QueryString bean = session().createRaw(QueryString.T, "2dd80676-c4b5-4c4e-a57f-292b432e7fa3");
		bean.setValue("from GmMetaModel m where not (m.name ilike 'tribefire.cortex*' or m.name ilike 'com.braintribe*') order by m.name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fe3a1cbc-09e8-40de-9042-113d504c664b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Models")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_8() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "abe777c3-e190-450c-8120-3b02226b371a");
		bean.setName("Distributed Models Icon");
		bean.setRepresentations(Sets.set(resource_32(), resource_30(), resource_33(), resource_31()));
		return bean;
	}

	// Managed
	private Resource resource_30() {
		Resource bean = session().createRaw(Resource.T, "a05a3cb7-fcd1-4910-b66e-49ec8f3ac0ff");
		bean.setFileSize(21565l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 43, 44, 214));
		bean.setName("CustomModels_64x64.png");
		bean.setSpecification(rasterImageSpecification_36());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_32());
		bean.setMd5("7f3b80dbaa1ffdca56ad904a1ae29fbe");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_32() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "64e49324-7acf-460d-8475-0582c815b053");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/43/f38ae0ae-808e-4ec4-998d-3e97640c9de7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_36() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3647baf3-9e33-4bd5-82dc-cd78e46c8ea0");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_31() {
		Resource bean = session().createRaw(Resource.T, "4428a7d6-26df-4600-884b-3f9393e93d0f");
		bean.setFileSize(1012l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 22, 48, 338));
		bean.setName("CustomModels_32x32.png");
		bean.setSpecification(rasterImageSpecification_37());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_33());
		bean.setMd5("71d742a33a368514aca210603ec6c5f5");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_33() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "199c2cc8-206c-447a-9a2e-8ee54beed46f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/22/7aaba34f-dffc-40eb-bcfb-6cf82e785d92");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_37() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0339303c-2a51-4951-9916-35cd712c3b97");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_32() {
		Resource bean = session().createRaw(Resource.T, "4adcbc76-8855-449a-8fd2-498d9557bfff");
		bean.setFileSize(728l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 22, 48, 334));
		bean.setName("CustomModels_24x24.png");
		bean.setSpecification(rasterImageSpecification_38());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_34());
		bean.setMd5("fcc97f0906a4d352101e890e80186a06");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_34() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8e73a351-a74e-4b37-b9ae-1683e5fde565");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/22/78f517d6-2d67-46e8-a78a-11b1fdd13e25");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_38() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1f20d015-0b16-4193-bf1c-061bc7087475");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_33() {
		Resource bean = session().createRaw(Resource.T, "433ad621-4c06-496a-844b-ea46bb134fef");
		bean.setFileSize(464l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 22, 48, 329));
		bean.setName("CustomModels_16x16.png");
		bean.setSpecification(rasterImageSpecification_39());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_35());
		bean.setMd5("7ed1171f7d1a1b24814380d91d6e4bfc");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_35() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4b451275-8737-426b-add2-d8616f1fd805");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/22/d9350908-1468-459c-8536-0d262f2e750a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_39() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e47df978-1e9f-4db2-8a6f-c38884db0baa");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "6aadf300-9eb3-4887-b127-a8e0ec6b1604");
		bean.setParent(folder_12());
		bean.setDisplayName(localizedString_28());
		bean.setIcon(adaptiveIcon_9());
		bean.setName("BaseModels");
		bean.setContent(templateQueryAction_3());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_3() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "b6fe308a-75c6-405f-a2fc-41091509a581");
		bean.setTemplate(template_4());
		bean.setDisplayName(localizedString_27());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "323fba7e-5819-471c-9f82-70c7050bcca1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Base Models Query")));
		return bean;
	}

	// Managed
	private Template template_4() {
		Template bean = session().createRaw(Template.T, "d8d25572-0526-4d80-9fca-208af8a766e3");
		bean.setMetaData(Sets.set(queryString_3()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_35());
		return bean;
	}

	// Managed
	private QueryString queryString_3() {
		QueryString bean = session().createRaw(QueryString.T, "b619e5bb-34a8-4499-9eaf-d7c9f3f43dce");
		bean.setValue("from GmMetaModel m where (m.name ilike 'tribefire.cortex*' or m.name ilike 'com.braintribe*') order by m.name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "13390925-2621-47df-82e2-4ff0e11fad7c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Base Models")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_9() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "8103857c-fbda-429d-8bc7-f9ca4d1a9da9");
		bean.setName("Custom Models Icon");
		bean.setRepresentations(Sets.set(resource_34(), resource_36(), resource_37(), resource_35()));
		return bean;
	}

	// Managed
	private Resource resource_34() {
		Resource bean = session().createRaw(Resource.T, "93cc6c8d-1ce6-4bfb-be23-1023b67008a0");
		bean.setFileSize(943l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 11, 51, 77));
		bean.setName("BaseModels_32x32.png");
		bean.setSpecification(rasterImageSpecification_40());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_36());
		bean.setMd5("922b9d2e57d6fb4c06b2edd819280df1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_36() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "013c783c-45c6-406b-839e-c51955ec6a13");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/11/4bcb3c58-2f4b-486f-a68c-b4de4c4b83e8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_40() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8bedeb97-d674-4aeb-98c7-7f43fad6263c");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_35() {
		Resource bean = session().createRaw(Resource.T, "8a3135b2-cc23-49c6-a30a-73a9ce4e4bcb");
		bean.setFileSize(472l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 11, 51, 70));
		bean.setName("BaseModels_16x16.png");
		bean.setSpecification(rasterImageSpecification_41());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_37());
		bean.setMd5("8a610e5a0795d8f27c8de3893068e3d4");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_37() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "87af558f-4d78-4fac-9ba3-7c0a51d8430c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/11/78aa1682-35aa-49ab-ac14-29206e0f9eb9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_41() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8ef609b7-59fb-40af-ae7d-2dd44b2d0f53");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_36() {
		Resource bean = session().createRaw(Resource.T, "937f0bac-3ee1-42c0-b842-19bc6865ede4");
		bean.setFileSize(21311l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 44, 25, 959));
		bean.setName("BaseModels_64x64.png");
		bean.setSpecification(rasterImageSpecification_42());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_38());
		bean.setMd5("9a1bf0c27fbbd0b162f15e9dd30d4a71");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_38() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bdcd4f1c-1b87-4ed3-b107-91cb6a813af1");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/44/27a714ab-5e7f-4852-9950-5d934f005df7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_42() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "060f50f9-3429-46c4-bbc8-2536a929ac36");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_37() {
		Resource bean = session().createRaw(Resource.T, "b50fa7dd-4620-4b34-8806-4f850720ee4e");
		bean.setFileSize(684l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 11, 51, 74));
		bean.setName("BaseModels_24x24.png");
		bean.setSpecification(rasterImageSpecification_43());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_39());
		bean.setMd5("0c319cab7637e0d67f692a42fc5184ac");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_39() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4dda2cad-1677-419e-866c-ebd523cc66f7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/11/34cc667f-bd80-4708-8665-97cef360c9dc");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_43() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5774794d-954a-401f-b08f-2bfef7b84de1");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "bce18ba0-1e36-470f-b859-6ddd5429af7f");
		bean.setParent(folder_12());
		bean.setDisplayName(localizedString_29());
		bean.setIcon(adaptiveIcon_10());
		bean.setName("EntityTypes");
		bean.setContent(templateQueryAction_4());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_4() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "2312d081-39cc-466d-9523-2433dd19f822");
		bean.setTemplate(template_5());
		bean.setDisplayName(localizedString_502());
		return bean;
	}

	// Managed
	private Template template_5() {
		Template bean = session().createRaw(Template.T, "89566cbe-0bcd-4799-9982-bf6e7fc17c23");
		bean.setMetaData(Sets.set(defaultView_1(), queryString_4()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_4());
		return bean;
	}

	// Managed
	private QueryString queryString_4() {
		QueryString bean = session().createRaw(QueryString.T, "8fa0796e-9821-4f6f-9099-8e98463f960b");
		bean.setValue("from GmEntityType t order by 't.typeSignature'");
		return bean;
	}

	// Managed
	private DefaultView defaultView_1() {
		DefaultView bean = session().createRaw(DefaultView.T, "10ac3354-1978-4912-9307-0b908558b792");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_4() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "5cf00ed7-bb74-49ef-a899-3430d9e61cd1");
		bean.setOrdering(simpleOrdering_4());
		bean.setEntityTypeSignature("com.braintribe.model.meta.GmEntityType");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_4() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "06168801-2340-4982-9624-3a84187ed70e");
		bean.setOrderBy("t.typeSignature");
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "603167df-e7a3-41ab-8425-fc1ef08d6863");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entity Types")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_10() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "1c8963fd-0098-4b5e-8593-f27f9fb350ff");
		bean.setName("Entity Types Icon");
		bean.setRepresentations(Sets.set(resource_40(), resource_41(), resource_39(), resource_38()));
		return bean;
	}

	// Managed
	private Resource resource_38() {
		Resource bean = session().createRaw(Resource.T, "41f1c746-e533-49c9-a579-056892e15b0d");
		bean.setFileSize(488l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 13, 50, 430));
		bean.setName("EntityTypes_32x32.png");
		bean.setSpecification(rasterImageSpecification_44());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_40());
		bean.setMd5("9fd7388b5c9b4404244f99d076ac0667");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_40() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "aa88b5ed-60ef-4fd4-a0ad-08c126932130");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/13/24bed29e-1e03-4f52-b102-3dc3973d1368");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_44() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4308155a-ffda-41b2-a35c-bd68c99ef3ce");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_39() {
		Resource bean = session().createRaw(Resource.T, "b97dfaf7-07bf-4168-aff6-91df96468ddf");
		bean.setFileSize(20594l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 44, 47, 97));
		bean.setName("EntityTypes_64x64.png");
		bean.setSpecification(rasterImageSpecification_45());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_41());
		bean.setMd5("c87b71cc2e2c17cc9d6a1c185259659c");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_41() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ca68fd43-1ce9-41d7-b785-39dc0102a396");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/44/d226ffc3-00a4-4166-b72f-6c8f9d9138eb");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_45() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "195ef852-82da-46f4-a0a7-3a979b205248");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_40() {
		Resource bean = session().createRaw(Resource.T, "afc604a4-dd35-42b4-8e18-0e04d06c7f8e");
		bean.setFileSize(375l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 13, 50, 427));
		bean.setName("EntityTypes_24x24.png");
		bean.setSpecification(rasterImageSpecification_46());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_42());
		bean.setMd5("e3ff719307d8afd06a739f8405c8851b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_42() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "68b74e41-7f70-4953-83f0-5fc029c70e5d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/13/19ac7a92-77f8-41a9-a9be-cabeac9be619");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_46() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "81e6e5ef-3c70-4561-aae2-f1b37b8a6e52");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_41() {
		Resource bean = session().createRaw(Resource.T, "3a8bab63-f072-4a64-9bbd-f00bbc086a70");
		bean.setFileSize(268l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 13, 50, 423));
		bean.setName("EntityTypes_16x16.png");
		bean.setSpecification(rasterImageSpecification_47());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_43());
		bean.setMd5("f6d862b60d44b5c84f9e0d28b93c945e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_43() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "64bfa9cb-78e8-4ab0-8956-479516253f65");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/13/60684628-1ffd-4cba-8005-39cf51cae2ba");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_47() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "42eace73-e760-4dc6-ab2d-17e244e1fd3f");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "62e1a7cb-3afd-48f8-9c5c-3c066d369f43");
		bean.setParent(folder_12());
		bean.setDisplayName(localizedString_30());
		bean.setIcon(adaptiveIcon_11());
		bean.setName("EnumTypes");
		bean.setContent(templateQueryAction_5());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_5() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "eab832c9-4fbb-4c1d-b5a2-08afcb16e94f");
		bean.setTemplate(template_6());
		bean.setDisplayName(localizedString_501());
		return bean;
	}

	// Managed
	private Template template_6() {
		Template bean = session().createRaw(Template.T, "135f10f4-d476-4702-b814-a82eb851239f");
		bean.setMetaData(Sets.set(defaultView_2(), queryString_5()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_5());
		return bean;
	}

	// Managed
	private QueryString queryString_5() {
		QueryString bean = session().createRaw(QueryString.T, "13972c1b-a920-4606-8096-f31392af7c05");
		bean.setValue("from GmEnumType t order by 't.typeSignature'");
		return bean;
	}

	// Managed
	private DefaultView defaultView_2() {
		DefaultView bean = session().createRaw(DefaultView.T, "150c6384-fb3f-4012-ab13-2cd0901a03bc");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_5() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "2610efff-4c41-4bbd-b764-2ddc1cb2ed5f");
		bean.setOrdering(simpleOrdering_5());
		bean.setEntityTypeSignature("com.braintribe.model.meta.GmEnumType");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_5() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "2cf640fe-302a-43e3-918b-33c8256c4560");
		bean.setOrderBy("t.typeSignature");
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "225a0e9b-00f0-497e-991e-91e1262bc6ea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Enum Types")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_11() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "0decdb0d-be16-4f30-ae54-7b60c7421063");
		bean.setName("Enum Types Icon");
		bean.setRepresentations(Sets.set(resource_42(), resource_43(), resource_44(), resource_45()));
		return bean;
	}

	// Managed
	private Resource resource_42() {
		Resource bean = session().createRaw(Resource.T, "340d8a4a-c20c-41b4-9df9-f1b73dbc4e66");
		bean.setFileSize(268l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 14, 21, 174));
		bean.setName("EnumTypr_16x16.png");
		bean.setSpecification(rasterImageSpecification_48());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_44());
		bean.setMd5("33bad3a09e529db0fc0ea1071b674a36");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_44() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "22265044-96df-4172-bbdb-f65589cf3876");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/14/23a64c19-a285-4c20-a0b8-af26534424f9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_48() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "766d1101-26d4-423c-a1e7-416edc32dd76");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_43() {
		Resource bean = session().createRaw(Resource.T, "18d103f8-fc93-468a-a529-505608008818");
		bean.setFileSize(492l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 14, 21, 181));
		bean.setName("EnumTypr_32x32.png");
		bean.setSpecification(rasterImageSpecification_49());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_45());
		bean.setMd5("a9b61cacf629d1d44842ec587529a1d9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_45() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "6266cac9-3487-4379-995c-bb1df62b46a4");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/14/f1d9c1cc-21b4-4dfc-bf03-a5110c33dd05");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_49() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b9fd6802-64d7-4f53-befa-e1dfed79e9b4");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_44() {
		Resource bean = session().createRaw(Resource.T, "be1c322e-bfce-4f8d-8cd4-b5d38e992641");
		bean.setFileSize(20394l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 45, 4, 446));
		bean.setName("EnumTypes_64x64.png");
		bean.setSpecification(rasterImageSpecification_50());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_46());
		bean.setMd5("ade73e859cf2184dff7a1ac023713b33");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_46() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ab90c0c2-9e08-4a64-b08f-8943fa7b824a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/45/e749e092-e803-4a82-adf0-fec70a7fb01b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_50() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5628bd7f-4edd-4e3e-9faf-ebb0570d759f");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_45() {
		Resource bean = session().createRaw(Resource.T, "6b49924d-7b75-4cd7-ade6-b101ce29dc00");
		bean.setFileSize(391l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 14, 21, 179));
		bean.setName("EnumTypr_24x24.png");
		bean.setSpecification(rasterImageSpecification_51());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_47());
		bean.setMd5("7e6ff8951f934f38c4690a3991ee84fc");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_47() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "147f3ba2-7390-4d0a-9aab-261fba9bbad7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/14/2093dfda-7c02-4e00-a09f-dfd9b742f1de");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_51() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "302e5841-ebec-4d93-a220-08510d7eca5e");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "13055364-aec1-4fda-81fb-fb5c617d9efd");
		bean.setParent(folder_2());
		bean.setSubFolders(Lists.list(folder_18(), folder_24(), folder_25(), folder_26(), folder_27()));
		bean.setDisplayName(localizedString_31());
		bean.setName("Integration");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e53d3d8c-c18c-48f0-b422-4bfd31e5d014");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Virtualization")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "1260db27-58b8-4806-a345-d70d8ea14e4f");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_32());
		bean.setIcon(adaptiveIcon_12());
		bean.setName("Custom Deployables");
		bean.setContent(templateQueryAction_6());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_6() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "917b899f-1513-49a8-a469-5285c9d3ea4b");
		bean.setTemplate(template_7());
		bean.setDisplayName(localizedString_500());
		return bean;
	}

	// Managed
	private Template template_7() {
		Template bean = session().createRaw(Template.T, "6544360a-e221-4dfe-bfa3-9681123ecbf2");
		bean.setMetaData(Sets.set(defaultView_3(), queryString_6()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_6());
		return bean;
	}

	// Managed
	private DefaultView defaultView_3() {
		DefaultView bean = session().createRaw(DefaultView.T, "507169b8-5b0c-4cc8-8a05-06ad1932edb2");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_6() {
		QueryString bean = session().createRaw(QueryString.T, "ff6d5063-b66d-438e-938b-08032757bc50");
		bean.setValue("from Deployable d where not (d.globalId like 'hardwired:*' or d.globalId like 'default:*') order by d.name ");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_6() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "60b7ffea-3af5-470b-95cf-e473bbe7797f");
		bean.setOrdering(simpleOrdering_6());
		bean.setRestriction(restriction_4());
		bean.setEntityTypeSignature("com.braintribe.model.deployment.Deployable");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_6() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "bd70a836-a79e-4496-861b-77a32fc17ed1");
		bean.setOrderBy(propertyOperand_16());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_16() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "c6406473-3e83-416d-a3ea-cc7278644ecb");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_4() {
		Restriction bean = session().createRaw(Restriction.T, "9e6b2356-6151-42a7-af7d-16a04c587d8a");
		bean.setCondition(negation_3());
		return bean;
	}

	// Managed
	private Negation negation_3() {
		Negation bean = session().createRaw(Negation.T, "38cf0310-8020-4cd2-b60a-cce0fe4bb9ad");
		bean.setOperand(disjunction_4());
		return bean;
	}

	// Managed
	private Disjunction disjunction_4() {
		Disjunction bean = session().createRaw(Disjunction.T, "8261cf6e-824a-4369-afdb-50c6645d6c01");
		bean.setOperands(Lists.list(valueComparison_12(), valueComparison_13()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_12() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "c6e22b48-21e6-4839-99d7-4c1bb05f6888");
		bean.setLeftOperand(propertyOperand_17());
		bean.setRightOperand("hardwired:*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_17() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "161340d8-666c-4f4d-b222-d58b115f1951");
		bean.setPropertyName("globalId");
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_13() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "b16db777-0afe-477a-adb1-687ab92ceaa3");
		bean.setLeftOperand(propertyOperand_18());
		bean.setRightOperand("default:*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_18() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "20dbbd44-7445-400b-b928-95264f14c849");
		bean.setPropertyName("globalId");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f11ef5fd-9986-4b40-a7e8-e12a9c53ac13");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Deployables")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_12() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "588b1660-d42c-4049-92ef-6fd8f94d9259");
		bean.setName("Deploy Icon");
		bean.setRepresentations(Sets.set(resource_46(), resource_47()));
		return bean;
	}

	// Managed
	private Resource resource_46() {
		Resource bean = session().createRaw(Resource.T, "3f138cdd-96d5-4fb3-800f-b663febecc62");
		bean.setFileSize(973l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 33, 59, 485));
		bean.setName("deploy_32x32 copy.png");
		bean.setSpecification(rasterImageSpecification_52());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_48());
		bean.setMd5("ca7160d3f2294bd752369d60c146f661");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_48() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1a7a58c3-c2a6-4492-9446-f4a7902c6bbe");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/33/e0adc2f8-2785-4ba3-a672-234c957b48f5");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_52() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "00d89b51-0086-474a-912c-a3b1ae1188fd");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_47() {
		Resource bean = session().createRaw(Resource.T, "d8cc86f3-4abb-47cf-be79-ff0f25f96aaa");
		bean.setFileSize(840l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 33, 59, 481));
		bean.setName("deploy_16x16.png");
		bean.setSpecification(rasterImageSpecification_53());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_49());
		bean.setMd5("3591de95c620ef1a01c8a7d00b98a08e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_49() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "762d32fc-c599-4af7-962b-15eb8c30e47d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/33/a1ab3463-52dc-4364-a13d-fb1ea257bb26");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_53() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a8d6b445-93e1-47b5-9854-d974f9310044");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "3b33f47f-3590-4e58-bb58-9bd7080998d1");
		bean.setParent(folder_2());
		bean.setSubFolders(Lists.list(folder_20(), folder_21(), folder_22(), folder_23()));
		bean.setDisplayName(localizedString_33());
		bean.setName("Other Extensions");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "52b8535d-923e-460b-9bde-00501ea9fa39");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Other Extensions")));
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "6c697813-83f3-43b1-be20-e9084a829673");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_34());
		bean.setIcon(adaptiveIcon_13());
		bean.setName("Apps");
		bean.setContent(templateQueryAction_7());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_7() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "eb84c004-cfc4-4c67-b7c7-f9a4467bcf10");
		bean.setTemplate(template_8());
		bean.setDisplayName(localizedString_496());
		return bean;
	}

	// Managed
	private Template template_8() {
		Template bean = session().createRaw(Template.T, "9d47eacb-8da8-4ae6-867b-2b59fce1f2c8");
		bean.setMetaData(Sets.set(queryString_7(), defaultView_4()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_7());
		return bean;
	}

	// Managed
	private DefaultView defaultView_4() {
		DefaultView bean = session().createRaw(DefaultView.T, "c49425e2-dc4e-4d45-9f87-f718c6a7b79c");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_7() {
		QueryString bean = session().createRaw(QueryString.T, "8caee12a-1b57-4d71-bd09-473cd67351ab");
		bean.setValue("from WebTerminal t order by t.name");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_7() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "99e3c2fb-ecb4-4f55-ae2f-d784f80a31fa");
		bean.setOrdering(simpleOrdering_1());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.WebTerminal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_34() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5d1bf210-7aff-4f39-b785-285f80e9a487");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Apps")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_13() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "1870a01b-8612-4ea5-8cd1-c052ea944f7e");
		bean.setName("Apps Icon");
		bean.setRepresentations(Sets.set(resource_48(), resource_49(), resource_50(), resource_51()));
		return bean;
	}

	// Managed
	private Resource resource_48() {
		Resource bean = session().createRaw(Resource.T, "fc06edb2-6d4a-41e2-96a4-878ee7ed997e");
		bean.setFileSize(640l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 24, 33, 421));
		bean.setName("Apps_24x24.png");
		bean.setSpecification(rasterImageSpecification_54());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_50());
		bean.setMd5("06dd1f7dea47e993867c66081daa7ab4");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_50() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ba1d6067-a654-443b-a692-ec9f055c6098");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/24/08f45a21-cd2f-4536-af6f-51e343107a03");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_54() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2db44e61-79a9-4de6-9798-8ff44dc2152a");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_49() {
		Resource bean = session().createRaw(Resource.T, "fde9b982-5b88-454e-bbfb-5e5e1b89011e");
		bean.setFileSize(804l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 24, 33, 424));
		bean.setName("Apps_32x32.png");
		bean.setSpecification(rasterImageSpecification_55());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_51());
		bean.setMd5("93b2fe01df02fad49da4005681d6b2cd");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_51() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7b708ff0-4770-4531-996e-783ff13e22ae");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/24/85c7ccf3-7844-470e-bc69-3ee24ec0fd86");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_55() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1a5b8c02-0906-430b-9553-1920718e0cfa");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_50() {
		Resource bean = session().createRaw(Resource.T, "49728268-f22e-4019-bc13-2a92583833ad");
		bean.setFileSize(401l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 24, 33, 417));
		bean.setName("Apps_16x16.png");
		bean.setSpecification(rasterImageSpecification_56());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_52());
		bean.setMd5("7d0369cb6d42725c9c9dec1a355faaaa");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_52() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "16da6bf6-5ad6-4aca-b1bf-8b318c4ce691");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/24/7346f3fc-f039-4534-a98a-ca6e4b0e5362");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_56() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e7884124-5ba1-450d-8c68-936f8cbca508");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_51() {
		Resource bean = session().createRaw(Resource.T, "22afb586-e2f5-4e0f-a0c4-467fefa44c91");
		bean.setFileSize(1781l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 24, 33, 426));
		bean.setName("Apps_64x64.png");
		bean.setSpecification(rasterImageSpecification_57());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_53());
		bean.setMd5("fbf45b60390787af04e6187c1a15e6c2");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_53() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4cd06202-1f99-4744-b360-cf9a883936c2");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/24/dff080be-39ea-4717-b1d3-5776f72a5b61");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_57() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "64cf3047-b8b2-49c7-8463-bf8a589984bc");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "ce3bcd59-ebfc-4542-802f-8e9d9f524cf4");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_35());
		bean.setIcon(adaptiveIcon_14());
		bean.setName("Service Processors");
		bean.setContent(templateQueryAction_8());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_8() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "2817bbfa-c142-475d-ab3c-670394eb97c2");
		bean.setTemplate(template_9());
		bean.setDisplayName(localizedString_497());
		return bean;
	}

	// Managed
	private Template template_9() {
		Template bean = session().createRaw(Template.T, "f518ab8d-e9b0-4363-aed8-8893cf88dc11");
		bean.setMetaData(Sets.set(defaultView_5(), queryString_8()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_8());
		return bean;
	}

	// Managed
	private DefaultView defaultView_5() {
		DefaultView bean = session().createRaw(DefaultView.T, "a611fc6a-f391-4caa-8c5b-f4d1d06c0f3d");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_8() {
		QueryString bean = session().createRaw(QueryString.T, "341fab50-28b2-40f5-a45b-aa845469c469");
		bean.setValue("from ServiceProcessor p order by p.name");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_8() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "6dd00b28-7bda-4564-9377-85b3df73ab2d");
		bean.setOrdering(simpleOrdering_7());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.ServiceProcessor");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_7() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "041ba522-3fa0-4f47-be5c-b34fbec57cb6");
		bean.setOrderBy(propertyOperand_19());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_19() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9f085428-22ed-44ad-9e53-a9e6f5c027f7");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_35() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e0854532-4740-47f8-bb24-d7b725709ec5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Service Processors")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_14() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "4ee05285-9f08-4fb6-9508-3ce867fcf6bd");
		bean.setName("Action Processors Icon");
		bean.setRepresentations(Sets.set(resource_52(), resource_53(), resource_55(), resource_54()));
		return bean;
	}

	// Managed
	private Resource resource_52() {
		Resource bean = session().createRaw(Resource.T, "4b8b0196-d77a-45be-b83f-71f4aba534cf");
		bean.setFileSize(1576l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 4, 308));
		bean.setName("ActionProcessor_32x32.png");
		bean.setSpecification(rasterImageSpecification_58());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_54());
		bean.setMd5("9efb4275726841eafb097947be06e49a");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_54() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "302fab5d-bf07-4e82-abb6-853d1c4dbfd2");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/a0c3f17e-c6e6-4b1b-9b42-5f2303a71e3a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_58() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "fc70c6e0-1839-4e7c-b29f-c5caecfe3075");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_53() {
		Resource bean = session().createRaw(Resource.T, "c766c036-5a13-4bcd-90e7-3cec40060133");
		bean.setFileSize(679l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 4, 303));
		bean.setName("ActionProcessor_16x16.png");
		bean.setSpecification(rasterImageSpecification_59());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_55());
		bean.setMd5("3e7f36ac1b92d19aaa113a5df42dfa7d");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_55() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ffbe0059-d6bd-4b9a-b1c0-24fd8a0ed578");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/5acd8a51-d60d-4155-94ef-691a9aca195b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_59() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "051207ee-8149-4675-afbe-1e456faf2dd8");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_54() {
		Resource bean = session().createRaw(Resource.T, "accfcb1d-3f62-4593-87c2-e1cfeca396ce");
		bean.setFileSize(1110l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 4, 306));
		bean.setName("ActionProcessor_24x24.png");
		bean.setSpecification(rasterImageSpecification_60());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_56());
		bean.setMd5("4dd2537286398637e979fc627b7d2908");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_56() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "93895de8-dc7c-408c-b6d5-46a083a4f381");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/28617fd5-4e0d-4b88-bad5-6d1bbee9ef98");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_60() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "292b3946-da76-4d1b-bab8-6fbdbc95f6fb");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_55() {
		Resource bean = session().createRaw(Resource.T, "27295e01-9e42-4ec9-988d-873792fcf4e3");
		bean.setFileSize(4466l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 26, 4, 312));
		bean.setName("ActionProcessor_64x64.png");
		bean.setSpecification(rasterImageSpecification_61());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_57());
		bean.setMd5("e8ed1230bc6afec7da1e1ec67ddf55fa");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_57() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3a32cfe4-5518-4146-be11-80387dc269a6");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/26/e00adb15-8e52-440b-940d-28fbef75fe87");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_61() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2f751f5c-fc65-4623-81e3-574e3b566e95");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "431a4737-0c6f-4dea-aa55-42605369c437");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_37());
		bean.setIcon(adaptiveIcon_15());
		bean.setName("StateChangeProcessors");
		bean.setContent(simpleQueryAction_4());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_4() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "620e7c84-913f-4d09-b4de-85cc4f94d086");
		bean.setTypeSignature("com.braintribe.model.extensiondeployment.StateChangeProcessor");
		bean.setDisplayName(localizedString_36());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "69841932-adeb-46c1-993a-401dabd83dd6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "State Change Processors Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a4ee64de-2e8c-44a2-b56f-8bcf43d3aabe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "State Change Processors")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_15() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e692999c-63a4-4b84-9635-961b13f9689f");
		bean.setName("StateChangeProcessor Icon");
		bean.setRepresentations(Sets.set(resource_58(), resource_56(), resource_59(), resource_57()));
		return bean;
	}

	// Managed
	private Resource resource_56() {
		Resource bean = session().createRaw(Resource.T, "095c253b-9f5b-46b3-9d53-9156bdb10dc1");
		bean.setFileSize(667l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 25, 22, 706));
		bean.setName("StateChangeProcessors_16x16.png");
		bean.setSpecification(rasterImageSpecification_62());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_58());
		bean.setMd5("58a7822c290bd526285c799ab3cca6c5");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_58() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1c484977-ef5f-4fea-bab7-1d32ff8a50c6");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/25/e10158b6-f9d5-4629-a167-8c0c9981b82b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_62() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2d65f348-de99-45a1-b1a3-c327a0801559");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_57() {
		Resource bean = session().createRaw(Resource.T, "1f9711f3-fec3-4ed3-952b-a5ccf6aec431");
		bean.setFileSize(1112l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 25, 22, 710));
		bean.setName("StateChangeProcessors_24x24.png");
		bean.setSpecification(rasterImageSpecification_63());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_59());
		bean.setMd5("23b520c12e457e12824d53c092e6b414");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_59() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cd98a247-a8f0-4a6d-9cc2-f2fcb1a45663");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/25/5640c523-5829-42f4-81f9-1d0a638fe220");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_63() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d90c98e4-fb91-47a6-a15b-9f4c1162dcaa");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_58() {
		Resource bean = session().createRaw(Resource.T, "de0cd943-6857-4070-85ba-d29f10f247c5");
		bean.setFileSize(1616l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 25, 22, 714));
		bean.setName("StateChangeProcessors_32x32.png");
		bean.setSpecification(rasterImageSpecification_64());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_60());
		bean.setMd5("6ff19a03ac98b8c0267eb6be558369fc");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_60() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "26551483-7a7d-4e7e-95f8-aa7ba4a70b73");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/25/62f5bf85-6dac-4b78-bc90-2908931311b4");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_64() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c10cc40a-7ba0-4d8c-9276-fe8bce313d77");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_59() {
		Resource bean = session().createRaw(Resource.T, "ded694cf-35aa-42ac-9e0c-7a2a6671798b");
		bean.setFileSize(4623l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 25, 22, 716));
		bean.setName("StateChangeProcessors_64x64.png");
		bean.setSpecification(rasterImageSpecification_65());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_61());
		bean.setMd5("82fa544f1912806e28a665c6de9e777c");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_61() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ed96e386-97f8-4724-8631-690f36098053");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/25/3f78b580-c010-435d-871e-1b3e5a6ba68a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_65() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b0917ad8-1851-44ce-a7c0-fc1ffcc957b5");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "cd8b72fb-d695-40f5-afe7-76b3bbc7739f");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_41());
		bean.setIcon(adaptiveIcon_16());
		bean.setName("ResouceStreamer");
		bean.setContent(simpleQueryAction_6());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_6() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "f653492e-ec76-4beb-a8ea-aa599a67a895");
		bean.setTypeSignature("com.braintribe.model.extensiondeployment.ResourceStreamer");
		bean.setDisplayName(localizedString_40());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "313d1ea7-1a78-472a-bbfc-856e47ba4188");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource Streamer Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f3129719-d415-4bb1-a40c-4fd1ccaf9f1b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource Streamer")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_16() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "f61d4e71-ffc9-4d7e-b6e9-5d7a990c9e10");
		bean.setName("StreamerIcon");
		bean.setRepresentations(Sets.set(resource_63(), resource_61(), resource_62(), resource_60()));
		return bean;
	}

	// Managed
	private Resource resource_60() {
		Resource bean = session().createRaw(Resource.T, "994f3b2e-af3c-4038-8439-c454b042db5b");
		bean.setFileSize(20090l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 48, 43, 135));
		bean.setName("Streamer_24x24.png");
		bean.setSpecification(rasterImageSpecification_66());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_62());
		bean.setMd5("cbb8acde01f16e91892591a041c99096");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_62() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7c0fe757-2fe5-4c72-8cfd-024e64a3c962");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/48/e72ed726-28b6-40ff-ae95-105ef96463aa");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_66() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1c9523a5-9d56-4d12-b257-33b4c00d9a31");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_61() {
		Resource bean = session().createRaw(Resource.T, "10f2ef0d-7144-4001-a209-f836906ba799");
		bean.setFileSize(21184l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 48, 43, 123));
		bean.setName("Streamer_64x64.png");
		bean.setSpecification(rasterImageSpecification_67());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_63());
		bean.setMd5("6c8cc83e162fa19420279e06b9e48cf7");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_63() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0cc14599-47ad-414a-9a79-8ca7c17d4a07");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/48/46e4dfc9-bbf6-4feb-95e2-ea9630dbadd1");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_67() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5f3c969d-17b2-438e-bbc1-b85ed4d2819c");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_62() {
		Resource bean = session().createRaw(Resource.T, "5cb2020c-59f0-4ef4-bcd3-b02456743092");
		bean.setFileSize(20283l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 48, 43, 129));
		bean.setName("Streamer_32x32.png");
		bean.setSpecification(rasterImageSpecification_68());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_64());
		bean.setMd5("40bf30eb8c8ad67fff5341dff0d99079");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_64() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c2ad3a62-32f1-48cb-87ce-f173357060dc");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/48/ee944d23-3f73-4b81-84a3-dc99d95cb82b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_68() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "119f4c6e-5c6b-4427-965d-2dd3eab21e4b");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_63() {
		Resource bean = session().createRaw(Resource.T, "6f466036-bfc7-44a0-acad-0e239f9f6bea");
		bean.setFileSize(19886l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 48, 43, 139));
		bean.setName("Streamer_16x16.png");
		bean.setSpecification(rasterImageSpecification_69());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_65());
		bean.setMd5("942b6046cb8c97c44a1d2f4e73627436");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_65() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "290d4d23-a95b-409c-be74-9581df457c80");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/48/44b694d0-fba8-4179-a4ab-73b2de3b12df");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_69() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "eb237eb9-a07c-4bb4-92d0-9041e91b71f6");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "cc4f0b81-d1d2-41d2-b94c-e4ca9c900219");
		bean.setParent(folder_19());
		bean.setDisplayName(localizedString_42());
		bean.setIcon(adaptiveIcon_12());
		bean.setName("SystemDeployables");
		bean.setContent(templateQueryAction_9());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_9() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "c02b1dec-1aad-4577-b9e0-797c28b63b6f");
		bean.setTemplate(template_10());
		bean.setDisplayName(localizedString_499());
		return bean;
	}

	// Managed
	private Template template_10() {
		Template bean = session().createRaw(Template.T, "ee918111-eb1f-4bf5-a1df-eec711548130");
		bean.setMetaData(Sets.set(queryString_9(), defaultView_6()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_9());
		return bean;
	}

	// Managed
	private QueryString queryString_9() {
		QueryString bean = session().createRaw(QueryString.T, "340e6d85-7ec4-4784-944c-94da07feb372");
		bean.setValue("from Deployable d where d.globalId like 'hardwired:*' or d.globalId like 'default:*' order by d.name ");
		return bean;
	}

	// Managed
	private DefaultView defaultView_6() {
		DefaultView bean = session().createRaw(DefaultView.T, "14bff0a2-0fee-45f6-8c0c-ce6420b47e9a");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_9() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "60a0f1f1-6b6e-4c9e-b20f-be861ed87c6f");
		bean.setOrdering(simpleOrdering_8());
		bean.setRestriction(restriction_5());
		bean.setEntityTypeSignature("com.braintribe.model.deployment.Deployable");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_8() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "64f72e27-db2e-45b7-ab38-884635dc28fc");
		bean.setOrderBy(propertyOperand_20());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_20() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "3d10e4cd-9489-4f8f-b218-98a7e108c575");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_5() {
		Restriction bean = session().createRaw(Restriction.T, "e033e6a5-d544-46b4-9cd9-7a095bf45020");
		bean.setCondition(disjunction_5());
		return bean;
	}

	// Managed
	private Disjunction disjunction_5() {
		Disjunction bean = session().createRaw(Disjunction.T, "d022d4a7-b31d-43ce-96a0-883618b3427f");
		bean.setOperands(Lists.list(valueComparison_14(), valueComparison_15()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_14() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "a35972d1-201d-4b90-8b88-65afa74eb0fd");
		bean.setLeftOperand(propertyOperand_21());
		bean.setRightOperand("hardwired:*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_21() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "c5369d5b-0aa9-44f7-bd2f-e3dd7766f548");
		bean.setPropertyName("globalId");
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_15() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "920dab00-b42e-467d-8633-5f3e29f9a2c9");
		bean.setLeftOperand(propertyOperand_22());
		bean.setRightOperand("default:*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_22() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e52d72c3-5149-4555-b295-90d222495623");
		bean.setPropertyName("globalId");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1089cede-9f26-490a-b074-299a9501c2a8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System Deployables")));
		return bean;
	}

	@Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "fc3550bc-7ed8-48de-8b7d-2057b95db5f6");
		bean.setParent(folder_17());
		bean.setDisplayName(localizedString_44());
		bean.setIcon(adaptiveIcon_17());
		bean.setName("CustomAccesses");
		bean.setContent(templateQueryAction_10());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_10() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "45d9418e-51c7-405e-b99e-d5b30cd68a55");
		bean.setTemplate(template_11());
		bean.setDisplayName(localizedString_43());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "10bb9363-0a21-41f0-9cd2-3695adcfd180");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Accesses Query")));
		return bean;
	}

	// Managed
	private Template template_11() {
		Template bean = session().createRaw(Template.T, "6a9e72e3-19ac-4bf9-ab72-c2c07080ec32");
		bean.setMetaData(Sets.set(queryString_10()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_42());
		return bean;
	}

	// Managed
	private QueryString queryString_10() {
		QueryString bean = session().createRaw(QueryString.T, "1b6e5c54-ca25-4f4e-be4e-bc999e59abef");
		bean.setValue(
				"from IncrementalAccess a where not (typeSignature(a) like 'com.braintribe.model.accessdeployment.Hardwired*') order by a.name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3b3b7c51-d5cd-4619-af76-ba1ffd3563a7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Accesses")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_17() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "bafbd1a6-5194-4904-b804-76cfc4d25292");
		bean.setName("Custom Accesses Icon");
		bean.setRepresentations(Sets.set(resource_66(), resource_64(), resource_65(), resource_67()));
		return bean;
	}

	// Managed
	private Resource resource_64() {
		Resource bean = session().createRaw(Resource.T, "eb7e6564-ea17-4c8d-8700-923da0776029");
		bean.setFileSize(390l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 27, 55, 553));
		bean.setName("CustomAccesses_16x16.png");
		bean.setSpecification(rasterImageSpecification_70());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_66());
		bean.setMd5("ea38dafcaeb1965454161f0b716bdff8");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_66() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3f8901da-c975-4974-b108-1eedefedcd57");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/27/887c37a7-a2f0-434b-bb5f-252f02d5f99c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_70() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "6e08ece0-35ba-46bb-926e-eb3ca6d83f72");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_65() {
		Resource bean = session().createRaw(Resource.T, "6e666c8a-06a1-488d-a035-de76df759823");
		bean.setFileSize(526l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 27, 55, 558));
		bean.setName("CustomAccesses_24x24.png");
		bean.setSpecification(rasterImageSpecification_71());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_67());
		bean.setMd5("f02af2f4e44d551c2dc8f8a094e354a1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_67() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "9c924fd8-398f-40ba-ae95-088fd1f9cb94");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/27/8696e09c-ffbe-4b7b-b30b-1aae0485de67");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_71() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "69a75a87-bf93-4158-950b-b73831e91c62");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_66() {
		Resource bean = session().createRaw(Resource.T, "3c60cd56-63ef-456c-b09f-dffab41ae641");
		bean.setFileSize(716l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 27, 55, 561));
		bean.setName("CustomAccesses_32x32.png");
		bean.setSpecification(rasterImageSpecification_72());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_68());
		bean.setMd5("677693263af2e3e74e5df027291cb098");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_68() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f88e5606-74ec-414d-8a8b-1098a58d542d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/27/148aeafc-d66a-4478-9364-02040b1a46cd");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_72() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b5347d2e-692f-4992-8c76-65a0b4b3bce9");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_67() {
		Resource bean = session().createRaw(Resource.T, "cb12a57f-47a0-4487-969c-b2e0188445e0");
		bean.setFileSize(20580l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 47, 43, 92));
		bean.setName("CustomAccesses_64x64.png");
		bean.setSpecification(rasterImageSpecification_73());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_69());
		bean.setMd5("39735dcf7edf13e37519515b9ee94f74");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_69() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7212dff7-4060-44f1-bd80-3c8727ed7105");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/47/de29cae0-33ac-42b2-b7f9-78ade657c601");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_73() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "22d01021-2e41-4107-bd51-3f3f52635cab");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	@Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "914a25bb-ebf5-41a6-a4d5-e2377d84737c");
		bean.setParent(folder_17());
		bean.setDisplayName(localizedString_46());
		bean.setIcon(adaptiveIcon_18());
		bean.setName("SystemAccesses");
		bean.setContent(templateQueryAction_11());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_11() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "f3dcb1c8-4e6a-411f-b1a7-a6e1a4247eac");
		bean.setTemplate(template_12());
		bean.setDisplayName(localizedString_45());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_45() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "012c0af7-b475-41bd-a894-78b02a0f384a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System Accesses Query")));
		return bean;
	}

	// Managed
	private Template template_12() {
		Template bean = session().createRaw(Template.T, "2c88ee3d-4b01-44a8-9811-06554a47121e");
		bean.setMetaData(Sets.set(queryString_1()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_11());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_11() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "06231096-461e-498e-a042-738ad0ca5761");
		bean.setOrdering(simpleOrdering_10());
		bean.setEntityTypeSignature("com.braintribe.model.accessdeployment.HardwiredAccess");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_10() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "c3bd9aff-c1d5-48c6-b909-ea36098c1eec");
		bean.setOrderBy(propertyOperand_25());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_25() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "bacd9f59-5290-4bed-9577-dcdc887a4308");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_46() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a061c716-67c1-4eb8-a533-ca34c8d21fd9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System Accesses")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_18() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "1132fb30-a190-4941-a425-0ea551aa22cb");
		bean.setName("System Accesses Icon");
		bean.setRepresentations(Sets.set(resource_70(), resource_69(), resource_71(), resource_68()));
		return bean;
	}

	// Managed
	private Resource resource_68() {
		Resource bean = session().createRaw(Resource.T, "b9ab25bc-ca49-4898-ac97-ccb96cdabb4e");
		bean.setFileSize(21363l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 47, 0, 319));
		bean.setName("SystemAccesses_64x64.png");
		bean.setSpecification(rasterImageSpecification_74());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_70());
		bean.setMd5("1ae3a1d27c4c9ad6d1e9206377cdef85");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_70() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "95928b48-3f9a-4d3f-bdbe-be39f66a1cdf");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/47/ad6b7fa4-586e-49c6-a02f-2d18151b522b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_74() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "fe8c180e-1e1c-4856-8d56-b049950e5112");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_69() {
		Resource bean = session().createRaw(Resource.T, "fcf6595b-5100-4cf6-b14c-ceb46805ceec");
		bean.setFileSize(978l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 28, 36, 489));
		bean.setName("SystemAccesses_32x32.png");
		bean.setSpecification(rasterImageSpecification_75());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_71());
		bean.setMd5("feed1c56cc098228dc459b56363a97f1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_71() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "121ea689-4e6b-4234-bd2b-aed19f610759");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/28/700ee1e6-3cbd-431b-8404-087baf8db02b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_75() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "486878db-381b-476c-99d8-c7ca2501145c");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_70() {
		Resource bean = session().createRaw(Resource.T, "363e914d-bfa9-455e-a870-ed5803619760");
		bean.setFileSize(685l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 28, 36, 485));
		bean.setName("SystemAccesses_24x24.png");
		bean.setSpecification(rasterImageSpecification_76());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_72());
		bean.setMd5("d08643f0b492778b8d89db3b28872b79");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_72() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5bd09567-db52-4272-90fb-e932643ac0fa");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/28/0e452c74-2993-438a-a1cf-7fdcfa29945f");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_76() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3e3d3cbe-2639-43f5-8984-4f1507feb443");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_71() {
		Resource bean = session().createRaw(Resource.T, "be6bfed1-94ee-434a-bd34-f233cd7bf738");
		bean.setFileSize(414l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 28, 36, 482));
		bean.setName("SystemAccesses_16x16.png");
		bean.setSpecification(rasterImageSpecification_77());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_73());
		bean.setMd5("0fa2539fa1bfe655a715969f265590e8");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_73() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c7c973d3-aea5-4648-b219-f111c8beca9c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/28/ea9f01f8-ca27-4fbb-87cf-f18ee8b84d5f");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_77() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e6312733-a5e8-4e0a-ae0e-99e7d4238180");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "0f513b3c-df67-49b3-8c7a-77f775903abc");
		bean.setParent(folder_17());
		bean.setDisplayName(localizedString_47());
		bean.setIcon(adaptiveIcon_19());
		bean.setName("Connections");
		bean.setContent(templateQueryAction_12());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_12() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "f100d461-ed15-41ea-89b1-6ffa7d8ce2f0");
		bean.setTemplate(template_13());
		bean.setDisplayName(localizedString_498());
		return bean;
	}

	// Managed
	private Template template_13() {
		Template bean = session().createRaw(Template.T, "a64402d0-ad8c-466c-a2e8-02edf4bb85ae");
		bean.setMetaData(Sets.set(defaultView_7(), queryString_11()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_12());
		return bean;
	}

	// Managed
	private QueryString queryString_11() {
		QueryString bean = session().createRaw(QueryString.T, "f54b8464-5041-43c4-b4f1-02d7c24e5855");
		bean.setValue("from Connector c order by c.name");
		return bean;
	}

	// Managed
	private DefaultView defaultView_7() {
		DefaultView bean = session().createRaw(DefaultView.T, "72cb4fed-0ef7-428d-b694-566c9e2d98db");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_12() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "74b24cf9-4af5-4e83-a7c0-61571e770fd1");
		bean.setOrdering(simpleOrdering_11());
		bean.setEntityTypeSignature("com.braintribe.model.deployment.connector.Connector");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_11() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "5ac65a4d-cbc7-4e58-8425-6623b919cb79");
		bean.setOrderBy(propertyOperand_26());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_26() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "21eb7b4d-b309-493b-8387-b756cff8bee9");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_47() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d5dc2cbb-a8ed-42fb-915a-dc2ddb027535");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connections")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_19() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e5094caf-5e60-4121-8fc2-3a4f82a882d7");
		bean.setRepresentations(Sets.set(resource_72(), resource_74(), resource_73(), resource_75()));
		return bean;
	}

	// Managed
	private Resource resource_72() {
		Resource bean = session().createRaw(Resource.T, "ffaa7f18-dc0f-49ab-8c1f-7ee973c4f7ad");
		bean.setFileSize(20754l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 46, 19, 994));
		bean.setName("Connections_64x64.png");
		bean.setSpecification(rasterImageSpecification_78());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_74());
		bean.setMd5("df12a3c1ac9f84b001c512da6f80b8b5");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_74() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "9b1904be-6d5a-4ab9-8312-1463c1f98983");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/46/758fd990-3954-4689-8d50-e1b2929a7904");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_78() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0cd006b4-c694-4e5d-b077-f392a65922e7");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_73() {
		Resource bean = session().createRaw(Resource.T, "6f62cc07-f19a-4b0b-ae0f-c33387f114e7");
		bean.setFileSize(583l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 29, 54, 432));
		bean.setName("Connections_32x32.png");
		bean.setSpecification(rasterImageSpecification_79());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_75());
		bean.setMd5("193f4a8bc594c46df8e01f87814e222d");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_75() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0fc2fb3e-dc5f-4227-b8df-90474aafd93c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/29/bede4aec-fe21-4acd-b414-9b299a7e7cd3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_79() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "85ef6c2c-ae90-410a-8aa8-10d48ac80825");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_74() {
		Resource bean = session().createRaw(Resource.T, "d5b3aad4-0a20-46b3-9ead-3f28210398ae");
		bean.setFileSize(444l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 29, 54, 429));
		bean.setName("Connections_24x24.png");
		bean.setSpecification(rasterImageSpecification_80());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_76());
		bean.setMd5("efab0f104436c582ae6daba32865dbf9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_76() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "04663f79-a063-4d7b-a943-e4a740121d2f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/29/63033188-ca46-4c6a-9438-55005165ec88");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_80() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "fdf3c828-83f1-4e8d-a89f-eb332d468179");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_75() {
		Resource bean = session().createRaw(Resource.T, "5a68f11a-c860-4478-b957-80c84b63e19a");
		bean.setFileSize(326l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 29, 54, 424));
		bean.setName("Connections_16x16.png");
		bean.setSpecification(rasterImageSpecification_81());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_77());
		bean.setMd5("f69987399c5e03406c560cc347deea13");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_77() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4e590c69-cdb9-49de-8366-4a9e03861075");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/29/fdd1c8c3-7cdd-454f-8496-46e6d8e7d248");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_81() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "db786293-6576-41b3-84c2-a3baca1f966e");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "113ff445-5d35-4a04-ba9a-15a355579dc6");
		bean.setParent(folder_1());
		bean.setDisplayName(localizedString_48());
		bean.setIcon(adaptiveIcon_20());
		bean.setName("Engines");
		bean.setContent(templateQueryAction_13());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_13() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "d068b594-87c8-4319-a12a-fae531643c5c");
		bean.setTemplate(template_14());
		bean.setDisplayName(localizedString_491());
		return bean;
	}

	// Managed
	private Template template_14() {
		Template bean = session().createRaw(Template.T, "e1bc1327-dc76-4072-97bf-eb0521b4d142");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_13());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_13() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "1c123c69-0290-4e4e-bfed-145125220995");
		bean.setOrdering(simpleOrdering_12());
		bean.setRestriction(restriction_7());
		bean.setEntityTypeSignature("com.braintribe.model.processdefinition.ProcessingEngine");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_12() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "4c978bd2-0d06-4abd-b502-a2518e8c29a5");
		bean.setOrderBy(propertyOperand_27());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_27() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "269a7e29-656c-435f-8c6d-3ad6cce50bce");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Restriction restriction_7() {
		Restriction bean = session().createRaw(Restriction.T, "1c0d88a1-bf80-4e2e-972a-e325463ae5db");
		bean.setCondition(fulltextComparison_1());
		bean.setPaging(paging_1());
		return bean;
	}

	// Managed
	private FulltextComparison fulltextComparison_1() {
		FulltextComparison bean = session().createRaw(FulltextComparison.T, "62ad516f-d350-4641-9135-b23962bbab35");
		bean.setText("");
		return bean;
	}

	// Managed
	private Paging paging_1() {
		Paging bean = session().createRaw(Paging.T, "27a24a1e-804e-4fce-a451-ff9eea2fdbdb");
		bean.setPageSize(10);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_48() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ebd8ba8d-a553-4083-bb7e-5388b174d5c4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Engines")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_20() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "8e624fc9-66ab-4694-847d-3279c7bfa9ab");
		bean.setName("EngineIcon");
		bean.setRepresentations(Sets.set(resource_79(), resource_77(), resource_78(), resource_76()));
		return bean;
	}

	// Managed
	private Resource resource_76() {
		Resource bean = session().createRaw(Resource.T, "3e6e0745-a618-4c20-b60c-acfcbc95e7ac");
		bean.setFileSize(4755l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 34, 9, 981));
		bean.setName("tribefire_Icons_04_64x64_ALL-41_64x64.png");
		bean.setSpecification(rasterImageSpecification_2());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_78());
		bean.setMd5("8f98569ec6f7cb25baef5b43e0312fa6");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_78() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c3ae419c-61f2-43b8-a68b-02d41c4a1372");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/34/6c7dafab-a3e4-4e61-9508-d9b643d1a3db");
		return bean;
	}

	// Managed
	private Resource resource_77() {
		Resource bean = session().createRaw(Resource.T, "4c4eb6e2-06c9-4fa4-b593-9c5d60931231");
		bean.setFileSize(1165l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 34, 9, 969));
		bean.setName("tribefire_Icons_04_64x64_ALL-41_24x24.png");
		bean.setSpecification(rasterImageSpecification_82());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_79());
		bean.setMd5("95b4e2e4df6e6f0c39aa699644e67acb");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_79() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b40169e7-2fca-425e-9790-0badc7d7951b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/34/8208c2ad-2314-4a0b-9b10-94ff267b89b0");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_82() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "aae1bb14-64c8-49b1-9305-054413c6a42e");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_78() {
		Resource bean = session().createRaw(Resource.T, "7452f425-946b-45b5-9109-5e2168f4ff76");
		bean.setFileSize(1687l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 34, 9, 972));
		bean.setName("tribefire_Icons_04_64x64_ALL-41_32x32.png");
		bean.setSpecification(rasterImageSpecification_83());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_80());
		bean.setMd5("c4abdc72aa5b1ba9feb928c437241d2f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_80() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bfaac5c0-24fb-434f-8782-175158ac47c0");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/34/e2033e56-73fd-4f74-aa85-08b9daae746e");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_83() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2aa82c1a-985c-4be3-abe5-d72b76bd739d");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_79() {
		Resource bean = session().createRaw(Resource.T, "3d03c90d-391f-480d-bd71-a1f64989c08a");
		bean.setFileSize(682l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 34, 9, 951));
		bean.setName("tribefire_Icons_04_64x64_ALL-41_16x16.png");
		bean.setSpecification(rasterImageSpecification_84());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_81());
		bean.setMd5("6f375bb250c38787cd06b149710851de");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_81() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "95faf065-261c-4c22-9fe9-a076ad130850");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/34/d4c10e9e-8c18-486d-bd3b-dc6bb226408e");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_84() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d3418504-5688-4106-80b3-e628247e5963");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "1460769f-87a7-4fcd-bc6c-f86bc0fff14f");
		bean.setParent(folder_1());
		bean.setDisplayName(localizedString_49());
		bean.setIcon(adaptiveIcon_21());
		bean.setName("Definitions");
		bean.setContent(templateQueryAction_14());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_14() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "e1b2bf6e-4271-4d0a-8806-96467c42e407");
		bean.setTemplate(template_15());
		bean.setDisplayName(localizedString_492());
		return bean;
	}

	// Managed
	private Template template_15() {
		Template bean = session().createRaw(Template.T, "e8f58c3d-e917-40af-96a4-11d16241e109");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_14());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_14() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "15723961-6f37-4a0d-ab73-3d60b59cc851");
		bean.setOrdering(simpleOrdering_13());
		bean.setRestriction(restriction_8());
		bean.setEntityTypeSignature("com.braintribe.model.processdefinition.ProcessDefinition");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_13() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "853b4384-f9dc-412d-8808-1c25afda2fb0");
		bean.setOrderBy(propertyOperand_28());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_28() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9e207c2c-cfa9-4446-a7d0-a1ca4b8b8ccc");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Restriction restriction_8() {
		Restriction bean = session().createRaw(Restriction.T, "c0dbb10b-ce3e-481a-a535-4a4499a78193");
		bean.setCondition(fulltextComparison_2());
		bean.setPaging(paging_2());
		return bean;
	}

	// Managed
	private FulltextComparison fulltextComparison_2() {
		FulltextComparison bean = session().createRaw(FulltextComparison.T, "091a3a81-c25d-4955-a074-ee16798a30e7");
		bean.setText("");
		return bean;
	}

	// Managed
	private Paging paging_2() {
		Paging bean = session().createRaw(Paging.T, "031e33ba-4b02-45cb-af93-2a687c206af2");
		bean.setPageSize(10);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_49() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6ff28f75-1d8c-4eab-832d-4df7a0e6cb99");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Definitions")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_21() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "3691dc8b-6ecc-45ed-92ba-fbabf9312382");
		bean.setName("ProcessDefinitionIcon");
		bean.setRepresentations(Sets.set(resource_80(), resource_83(), resource_81(), resource_82()));
		return bean;
	}

	// Managed
	private Resource resource_80() {
		Resource bean = session().createRaw(Resource.T, "94209926-6eab-4db1-b352-ac61b16c4740");
		bean.setFileSize(663l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 39, 35, 490));
		bean.setName("processDef_16x16.png");
		bean.setSpecification(rasterImageSpecification_85());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_82());
		bean.setMd5("5f5f29dbf8c53e29adb110863e0eaca8");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_82() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "830594c8-5f3b-4736-a336-b64469b554dc");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/39/6241b5a8-686a-4d3b-aac0-e56f2f5cee75");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_85() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d36f925d-ef02-402a-8398-81043fabccea");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_81() {
		Resource bean = session().createRaw(Resource.T, "4830dadb-9b8b-499b-b8dc-d9465c12dcef");
		bean.setFileSize(1796l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 39, 35, 497));
		bean.setName("processDef_32x32.png");
		bean.setSpecification(rasterImageSpecification_86());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_83());
		bean.setMd5("964fa0a917b2ac53bc3520d53a76d383");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_83() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7414cf0e-905d-4a11-8bcf-3466e128ab2f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/39/859d2607-76cb-4952-8981-83a405b0318d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_86() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e62700bd-ec2f-4948-b9b2-6c03b2bf7787");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_82() {
		Resource bean = session().createRaw(Resource.T, "48ff5e7d-6050-4797-8ad2-081e363d9975");
		bean.setFileSize(5148l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 39, 35, 500));
		bean.setName("processDef_64x64.png");
		bean.setSpecification(rasterImageSpecification_87());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_84());
		bean.setMd5("227aa23c646d7e05ee4c14a9ec93e68e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_84() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "32d4a7e1-69c9-412c-8451-4810c9349de2");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/39/00a3775a-777e-44c6-bbfd-937e126500ad");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_87() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "329368cd-17bc-4760-a8fc-f013f4eac3ba");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_83() {
		Resource bean = session().createRaw(Resource.T, "852752d2-e69e-4139-9278-1f89ba7741f1");
		bean.setFileSize(1223l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 39, 35, 494));
		bean.setName("processDef_24x24.png");
		bean.setSpecification(rasterImageSpecification_88());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_85());
		bean.setMd5("5aa63965bfd58fc56b3a158f855dcc09");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_85() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "93689c2b-f67b-48cb-b82c-4dc483697211");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/39/27d0f578-e022-4d2a-872b-a6888b9cac90");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_88() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "bac00847-1c13-4c6e-abed-13a372618e6f");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "65db72d6-371a-43e8-b1b6-7acdb13521f4");
		bean.setParent(folder_1());
		bean.setDisplayName(localizedString_50());
		bean.setIcon(adaptiveIcon_22());
		bean.setName("Transition Processors");
		bean.setContent(templateQueryAction_15());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_15() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "60609653-be3b-40e3-add1-1d48e54ec3a5");
		bean.setTemplate(template_16());
		bean.setDisplayName(localizedString_493());
		return bean;
	}

	// Managed
	private Template template_16() {
		Template bean = session().createRaw(Template.T, "6910da66-aba5-4e09-967b-334bcf63afa7");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_15());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_15() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "5ce61fc1-42c5-4277-bce0-e2fff0fa5716");
		bean.setOrdering(simpleOrdering_14());
		bean.setRestriction(restriction_9());
		bean.setEntityTypeSignature("com.braintribe.model.processdefinition.TransitionProcessor");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_14() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "22fedbaa-ba93-49c2-8385-58450b998759");
		bean.setOrderBy(propertyOperand_29());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_29() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "dad2efb5-d2c6-4e38-92ee-b68165f1c4aa");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Restriction restriction_9() {
		Restriction bean = session().createRaw(Restriction.T, "cd9c24ae-50b5-4054-873b-53f5beac3d00");
		bean.setCondition(fulltextComparison_3());
		bean.setPaging(paging_3());
		return bean;
	}

	// Managed
	private FulltextComparison fulltextComparison_3() {
		FulltextComparison bean = session().createRaw(FulltextComparison.T, "92f9f6fa-3236-4a5a-b092-0a64979dfb39");
		bean.setText("");
		return bean;
	}

	// Managed
	private Paging paging_3() {
		Paging bean = session().createRaw(Paging.T, "5026e817-0067-4249-b7d4-36f2d8d77d60");
		bean.setPageSize(10);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_50() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f6540abf-0ff2-4b77-8480-2c104b6ed83c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transition Processors")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_22() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "c2f6f3d9-7f45-43fd-84e2-3cdbe2136ed0");
		bean.setName("TransitionProcessorIcon");
		bean.setRepresentations(Sets.set(resource_85(), resource_84(), resource_87(), resource_86()));
		return bean;
	}

	// Managed
	private Resource resource_84() {
		Resource bean = session().createRaw(Resource.T, "2f71c65f-2dab-45ea-91e6-6b99635ba995");
		bean.setFileSize(923l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 42, 24, 158));
		bean.setName("se_transitionproc_24x24.png");
		bean.setSpecification(rasterImageSpecification_89());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_86());
		bean.setMd5("0274aa6af297ce98f775ba541c298cf0");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_86() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "03f3e8be-560d-42f7-a08f-867e0e62b89e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/42/3c69d90c-47c7-4124-a8e3-1a939c121f41");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_89() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d72a2403-8a71-496f-b9d2-bbab5bbb1db3");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_85() {
		Resource bean = session().createRaw(Resource.T, "517d9e61-b1f0-47b6-9aa8-9b55d4b9b02d");
		bean.setFileSize(3465l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 42, 24, 166));
		bean.setName("se_transitionproc_64x64.png");
		bean.setSpecification(rasterImageSpecification_90());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_87());
		bean.setMd5("450ca8b7f77a16759d54ee1a109e1b87");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_87() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c7151d73-b578-4049-8d3e-76e4fee94dd7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/42/408fb582-983d-4527-b139-060608166e8c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_90() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "14ec1e82-55bd-4d07-a914-a4213f66754b");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_86() {
		Resource bean = session().createRaw(Resource.T, "30ac5da5-ae25-44d4-98a0-2169bd409b51");
		bean.setFileSize(546l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 42, 24, 155));
		bean.setName("se_transitionproc_16x16.png");
		bean.setSpecification(rasterImageSpecification_91());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_88());
		bean.setMd5("d1b0ac53d2f40fadff43f5b1c79de204");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_88() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7a24d775-2758-4886-ae3d-fe54154d5a37");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/42/1bd42f38-0c37-42e5-9874-a91effac6dda");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_91() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "355d2c70-efaa-480c-b670-87f7cef428ff");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_87() {
		Resource bean = session().createRaw(Resource.T, "c9c1f928-562d-4f4e-93df-fea7479a8162");
		bean.setFileSize(1348l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 42, 24, 162));
		bean.setName("se_transitionproc_32x32.png");
		bean.setSpecification(rasterImageSpecification_92());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_89());
		bean.setMd5("1e533f5250805d43076994ef64b4f683");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_89() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5252677a-12cf-41b9-a009-f094e4eba7b9");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/42/58a8ab73-276f-4763-9858-88da05a4c900");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_92() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "7511a8db-7757-4ed3-ad96-044913b8326b");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "7f4ab9e0-782c-4720-8c48-5eea0c9ebdb7");
		bean.setParent(folder_1());
		bean.setDisplayName(localizedString_51());
		bean.setIcon(adaptiveIcon_23());
		bean.setName("Conditions");
		bean.setContent(templateQueryAction_16());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_16() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "2b166991-86a5-420f-8b08-1a135a33009a");
		bean.setTemplate(template_17());
		bean.setDisplayName(localizedString_494());
		return bean;
	}

	// Managed
	private Template template_17() {
		Template bean = session().createRaw(Template.T, "8ee7ed05-9299-4cb8-8f14-f9911cdcadff");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_16());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_16() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "cda90408-2b31-4d65-ab3e-784ddb8078f3");
		bean.setOrdering(simpleOrdering_15());
		bean.setRestriction(restriction_10());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.Condition");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_15() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "e40c0c59-6a55-4350-8cfb-e8d0c67b3791");
		bean.setOrderBy(propertyOperand_30());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_30() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "13d89441-a6c4-4425-a0f3-656ea116473f");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Restriction restriction_10() {
		Restriction bean = session().createRaw(Restriction.T, "95ed9e14-4aee-43fb-98d3-75dc68316372");
		bean.setCondition(fulltextComparison_4());
		bean.setPaging(paging_4());
		return bean;
	}

	// Managed
	private FulltextComparison fulltextComparison_4() {
		FulltextComparison bean = session().createRaw(FulltextComparison.T, "1315d06e-17a7-45f3-8c32-7f511786185a");
		bean.setText("");
		return bean;
	}

	// Managed
	private Paging paging_4() {
		Paging bean = session().createRaw(Paging.T, "7e305608-dfb7-45de-9652-32c2463cb32f");
		bean.setPageSize(10);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_51() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "01dbffac-cf7e-43f3-9f55-8afcc2751904");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Conditions")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_23() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7be7cf82-7701-4eba-83e6-53dc4cb0be08");
		bean.setName("ConditionIcon");
		bean.setRepresentations(Sets.set(resource_90(), resource_88(), resource_89(), resource_91()));
		return bean;
	}

	// Managed
	private Resource resource_88() {
		Resource bean = session().createRaw(Resource.T, "ec68ac10-91d8-4c1f-84e9-0bd232ef98ae");
		bean.setFileSize(3987l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 43, 10, 687));
		bean.setName("se_condition_64x64.png");
		bean.setSpecification(rasterImageSpecification_93());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_90());
		bean.setMd5("7d929bbfebba1c4ec61db9229b710a19");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_90() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3dc76183-e447-4f19-bd19-6cd5fd37867e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/43/03330138-3938-4e38-bcfb-8cb7c603a9d2");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_93() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "6f7ef536-8a87-4d8a-b7eb-998cfa36fba7");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_89() {
		Resource bean = session().createRaw(Resource.T, "dc910ddf-6d62-4772-ac6b-4e4aa2752970");
		bean.setFileSize(973l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 43, 10, 677));
		bean.setName("se_condition_24x24.png");
		bean.setSpecification(rasterImageSpecification_94());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_91());
		bean.setMd5("debf69e1aa03400dbdc75b659412cb9e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_91() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "921abc37-2eda-4b1e-b6aa-277b62b2f4a5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/43/352b9c45-c80e-4d7e-a3b1-37ade6c86b69");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_94() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "94be4f23-a132-4700-9d18-209cf7a7e9fc");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_90() {
		Resource bean = session().createRaw(Resource.T, "abbc5f2b-3c09-4d6e-bf4f-7a7d39b1b97f");
		bean.setFileSize(603l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 43, 10, 672));
		bean.setName("se_condition_16x16.png");
		bean.setSpecification(rasterImageSpecification_95());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_92());
		bean.setMd5("33d4276877fb22b4f4d4bd545d2dabb9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_92() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3aad0bb4-0c48-4105-8dd9-fcc7f71305f8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/43/34f8a3eb-eeda-451b-872d-00182ca69842");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_95() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0d0c1935-85f9-4b02-a556-d3958705fc7b");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_91() {
		Resource bean = session().createRaw(Resource.T, "37ff205f-96e8-4713-8eb6-b87a651c73c3");
		bean.setFileSize(1439l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 43, 10, 682));
		bean.setName("se_condition_32x32.png");
		bean.setSpecification(rasterImageSpecification_96());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_93());
		bean.setMd5("f4addbf05c9001a4ce2d83a99131071f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_93() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "6b96c981-b717-4b5b-b440-3baf751b62d3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/43/b08e1137-7a03-413b-999d-a9acf17952d8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_96() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c50a810a-89e3-4d61-ae38-3a77cd947132");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	@Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "afdd2822-56ff-4311-a925-20617df9286c");
		bean.setParent(folder_1());
		bean.setDisplayName(localizedString_52());
		bean.setIcon(adaptiveIcon_24());
		bean.setName("Workers");
		bean.setContent(templateQueryAction_17());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_17() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "0ef48c78-41eb-47ed-8d33-c0a394152edb");
		bean.setTemplate(template_18());
		bean.setDisplayName(localizedString_495());
		return bean;
	}

	// Managed
	private Template template_18() {
		Template bean = session().createRaw(Template.T, "4c582a75-fd79-4487-b3a5-f67f600ffaa8");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_17());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_17() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "55e62d9b-f0b7-44a0-9cd9-e9a284b197ac");
		bean.setOrdering(simpleOrdering_16());
		bean.setRestriction(restriction_11());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.Worker");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_16() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "c32b5b46-7d6e-4147-8932-5b6b0fe28593");
		bean.setOrderBy(propertyOperand_31());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_31() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "d5a8f2e4-a798-4c37-9f23-7888ae560e06");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Restriction restriction_11() {
		Restriction bean = session().createRaw(Restriction.T, "af36b722-3333-43ce-95db-c40e4775d469");
		bean.setCondition(fulltextComparison_5());
		bean.setPaging(paging_5());
		return bean;
	}

	// Managed
	private FulltextComparison fulltextComparison_5() {
		FulltextComparison bean = session().createRaw(FulltextComparison.T, "1f3e0c6b-cd00-4d7d-bbf0-51c016a401a4");
		bean.setText("");
		return bean;
	}

	// Managed
	private Paging paging_5() {
		Paging bean = session().createRaw(Paging.T, "6dc3f2b9-6dd4-4292-a3c1-021ac6dbaa7e");
		bean.setPageSize(10);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_52() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c8576112-15d4-4dda-b719-16c98aa9945a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Workers")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_24() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "1c575cc7-5e8b-438a-94cd-321e7e1b4823");
		bean.setName("WorkerIcon");
		bean.setRepresentations(Sets.set(resource_94(), resource_96(), resource_93(), resource_95(), resource_92()));
		return bean;
	}

	// Managed
	private Resource resource_92() {
		Resource bean = session().createRaw(Resource.T, "670f5dd4-e3fb-4e83-bdbe-ed59a7b0899f");
		bean.setFileSize(684l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 31, 50, 340));
		bean.setName("Worker_24x24.png");
		bean.setSpecification(rasterImageSpecification_97());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_94());
		bean.setMd5("62a02700586e02610bf9e919acba8172");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_94() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "efb31df2-192b-4049-88b2-64fa36dddb06");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/31/8cdefffc-b779-4e2f-bd77-88c65f9267f0");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_97() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a6c289b8-9cf6-4c80-a060-39abb04f289f");
		bean.setPageCount(1);
		bean.setPixelHeight(25);
		bean.setPixelWidth(25);
		return bean;
	}

	// Managed
	private Resource resource_93() {
		Resource bean = session().createRaw(Resource.T, "d94f42c6-7f6f-457c-9b75-a9dc5dfe58a3");
		bean.setFileSize(628l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 44, 22, 916));
		bean.setName("se_worker_16x16.png");
		bean.setSpecification(rasterImageSpecification_98());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_95());
		bean.setMd5("8a77335d0ea975c23ef5600fef525686");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_95() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "dda2d912-0f79-4555-a18e-ee1b1313fc1a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/44/b0cbe386-cdca-4ed9-8d48-5e7924cba941");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_98() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a6257b4d-8346-4fcb-9262-64ec88307988");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_94() {
		Resource bean = session().createRaw(Resource.T, "727a0b73-a88d-471e-b9d6-41a20b8ad7a3");
		bean.setFileSize(1557l);
		bean.setCreated(newGmtDate(2015, 4, 26, 15, 44, 22, 919));
		bean.setName("se_worker_32x32.png");
		bean.setSpecification(rasterImageSpecification_99());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_96());
		bean.setMd5("01d8c876ef7c86830cf8c1b3f56517db");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_96() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f6d22f46-26a2-4b73-ae63-9a04d946398c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2617/44/3f16fed2-5a45-4598-87bb-2caec0643825");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_99() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "cac60f2e-f7dd-474e-bd6e-75e8c300433d");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_95() {
		Resource bean = session().createRaw(Resource.T, "cec608aa-b18d-41bb-a432-87e998319cb0");
		bean.setFileSize(1466l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 31, 50, 334));
		bean.setName("Worker_64x64.png");
		bean.setSpecification(rasterImageSpecification_100());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_97());
		bean.setMd5("c5ae431ad13d683691ff75b4f6dcb49b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_97() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1d894700-6e8a-486f-8d40-82283ea46a40");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/31/6848e247-e63b-4f99-9fef-388cb905e118");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_100() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c4644808-77aa-4665-a45b-26e74bd46a96");
		bean.setPageCount(1);
		bean.setPixelHeight(65);
		bean.setPixelWidth(66);
		return bean;
	}

	// Managed
	private Resource resource_96() {
		Resource bean = session().createRaw(Resource.T, "4e00cb2e-b32a-4afa-9076-86fb1db7c871");
		bean.setFileSize(17153l);
		bean.setCreated(newGmtDate(2015, 3, 24, 7, 31, 50, 330));
		bean.setName("Worker_512x512.png");
		bean.setSpecification(rasterImageSpecification_101());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_98());
		bean.setMd5("91ce1074ae40d27d23b8748a50ad7282");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_98() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b00f7160-0180-4947-9715-72a316d37b0a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1504/2409/31/eb1250bf-4363-4947-9767-51fae40e3f75");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_101() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3e98f089-8db2-4825-a71b-1c8d0abbe8b8");
		bean.setPageCount(1);
		bean.setPixelHeight(512);
		bean.setPixelWidth(512);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_101() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "66760e3c-5a41-4cf0-804e-87d423038436");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/51/82f90d0a-458e-495c-99ff-202c998cd089");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_7() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "8838d828-d96c-4fab-8ec4-6fe35d3c22a3");
		bean.setOwner(localEntityProperty_5());
		bean.setNewValue(variable_6());
		return bean;
	}

	// Managed
	private Variable variable_6() {
		Variable bean = session().createRaw(Variable.T, "05c64cce-3a5d-49ef-986c-5339df4bcd69");
		bean.setLocalizedName(localizedString_56());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("ignoreUnsupportedTables");
		bean.setDescription(localizedString_55());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_55() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e1c2210e-ef95-429e-8931-8031d0727cfe");
		bean.setLocalizedValues(
				Maps.map(Maps.entry("default", "If set to false model types will be created for unsupported tables but declared as unmapped.")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_56() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e9a05a40-5b75-404f-84ce-7f0b5828d1ed");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ignore Unsupported Tables")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_5() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "07a6670e-3cef-4501-8e32-5a20fd7742b0");
		bean.setPropertyName("ignoreUnsupportedTables");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	@Managed
	private CreateModelFromDbSchema createModelFromDbSchema_1() {
		CreateModelFromDbSchema bean = session().createRaw(CreateModelFromDbSchema.T, "985c45da-ab69-4186-a455-17818bde4010");
		bean.setIgnoreUnsupportedTables(Boolean.TRUE);
		bean.setGroupId("custom.model");
		bean.setVersion("1.0");
		bean.setResolveRelationships(Boolean.TRUE);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_103() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "882fb735-4892-441e-9b95-a12735243235");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/1210/07/8db8ffb0-aba7-42f2-8acb-6471de993a74");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_102() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "951cb505-f2db-4b6b-ab13-d80cb0dfa27e");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "ccd090da-2e4a-4f97-8766-ee85f2f8b27a");
		bean.setDisplayName(localizedString_57());
		bean.setName("Extensions");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_57() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b7a8ce59-e567-4daf-94d1-a2ac8c1d4c57");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Extensions")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_1() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "39aa1fdb-8325-4ee6-93fb-c9b49f2c23a5");
		bean.setTypeCondition(isAssignableTo_1());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_1() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "df07021a-f8ee-420f-8a01-fb03340ccef6");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_3() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "7aa9cb2b-c9e3-4d8a-8737-0394add83036");
		bean.setTemplate(template_19());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_58());
		bean.setIcon(adaptiveIcon_12());
		bean.setInplaceContextCriterion(conjunctionCriterion_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_58() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b8e9ca3a-bed9-48c9-90ce-e76cd3302d9b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Redeploy")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_1() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "51a6a698-f5d4-4fdd-ba4b-52826fa08157");
		bean.setCriteria(Lists.list(entityCriterion_2(), negationCriterion_1(), valueConditionCriterion_2()));
		return bean;
	}

	@Managed
	private EntityCriterion entityCriterion_2() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "4094b42c-6c6b-446d-afa2-78b58c764f5c");
		bean.setTypeSignature("com.braintribe.model.deployment.Deployable");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	@Managed
	private NegationCriterion negationCriterion_1() {
		NegationCriterion bean = session().createRaw(NegationCriterion.T, "2838d0f8-9e29-48ec-8dae-240101bbdc80");
		bean.setCriterion(typeConditionCriterion_2());
		return bean;
	}

	@Managed
	private TypeConditionCriterion typeConditionCriterion_2() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "464692c3-54c6-4668-9492-823dce651594");
		bean.setTypeCondition(isAssignableTo_2());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_2() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "bb7ab91d-ba23-4d52-bf87-8f9bcb6bada1");
		bean.setTypeSignature("com.braintribe.model.deployment.HardwiredDeployable");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_2() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "55aec4ce-dcc6-44b6-8082-59322903db47");
		bean.setPropertyPath("deploymentStatus");
		bean.setOperand(DeploymentStatus.undeployed);
		bean.setOperator(ComparisonOperator.notEqual);
		return bean;
	}

	// Managed
	private Template template_19() {
		Template bean = session().createRaw(Template.T, "c906da3a-b57a-4608-b901-d0c95b6e1f8a");
		bean.setName(localizedString_59());
		bean.setPrototypeTypeSignature("com.braintribe.model.deploymentapi.request.RedeployWithDeployables");
		bean.setPrototype(redeployWithDeployables_1());
		bean.setTechnicalName("RedeployWithDeployablesTemplate");
		bean.setScript(changeValueManipulation_10());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_59() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0ae82647-9740-439a-9127-ddfeb5c6346b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "RedeployWithDeployables Template")));
		return bean;
	}

	@Managed
	private RedeployWithDeployables redeployWithDeployables_1() {
		RedeployWithDeployables bean = session().createRaw(RedeployWithDeployables.T, "1dcfbb25-cb61-4838-91ea-e7bd8ee57cdb");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_10() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "492ebe76-a7e4-4287-b7c4-2dfa0bdb7635");
		bean.setOwner(localEntityProperty_8());
		bean.setNewValue(variable_9());
		return bean;
	}

	// Managed
	private Variable variable_9() {
		Variable bean = session().createRaw(Variable.T, "6207917a-3410-42a7-b52a-d6ad55f418e3");
		bean.setTypeSignature("set<com.braintribe.model.deployment.Deployable>");
		bean.setName("Deployables");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_8() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "f6c6edfe-9455-4d28-a9ea-8f849cceb979");
		bean.setPropertyName("deployables");
		bean.setEntity(redeployWithDeployables_1());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_4() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "3bea7fc2-5895-4603-8826-7acaacd10dbd");
		bean.setTemplate(template_20());
		bean.setDisplayName(localizedString_60());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_60() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "91a8c3e9-41a2-4601-a121-7a6b3402734f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Garbage Collection Action")));
		return bean;
	}

	@Managed
	private Template template_20() {
		Template bean = session().createRaw(Template.T, "0cfa1599-6b98-4388-a53d-38b0e32be001");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_2()));
		bean.setName(localizedString_62());
		bean.setPrototype(runGarbageCollection_1());
		bean.setTechnicalName("RunGarbageCollection");
		bean.setScript(compoundManipulation_2());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_2() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "16b8ddcb-91f5-496f-b887-12873649a396");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_10());
		return bean;
	}

	@Managed
	private Hidden hidden_1() {
		Hidden bean = session().createRaw(Hidden.T, "478c7cee-070e-4b1b-81a1-1f9127692a58");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_1());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_1() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "32bfba56-d5c6-4c79-aeda-614392b8b044");
		bean.setPropertyPath("access");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	@Managed
	private Variable variable_10() {
		Variable bean = session().createRaw(Variable.T, "e2a0bda7-c4e4-4d90-9c0a-f55a2f95a500");
		bean.setLocalizedName(localizedString_61());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_61() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ae9a6aee-60d2-4b10-b1a4-d1b797aade8a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_62() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "00dcdc73-f4c3-4f25-9f64-af99fa0394c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Garbage Collection")));
		return bean;
	}

	@Managed
	private RunGarbageCollection runGarbageCollection_1() {
		RunGarbageCollection bean = session().createRaw(RunGarbageCollection.T, "684b3204-91c4-4b98-9492-a05bd9f36db5");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_2() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "1cac2029-cf87-452d-831a-5d0cfb4bef77");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_11(), changeValueManipulation_12(), changeValueManipulation_13()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_11() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "7592c6f0-7dc9-4688-8ba4-866162908daa");
		bean.setOwner(localEntityProperty_9());
		bean.setNewValue(variable_10());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_9() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "dec3d0e5-3515-44df-82f8-d7085b7e4ebb");
		bean.setPropertyName("access");
		bean.setEntity(runGarbageCollection_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_12() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "da05f0ba-09a9-4aef-b538-7611f6e490fc");
		bean.setOwner(localEntityProperty_10());
		bean.setNewValue(variable_11());
		return bean;
	}

	// Managed
	private Variable variable_11() {
		Variable bean = session().createRaw(Variable.T, "d7e057cf-380f-4d6b-beae-2953428bf291");
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("Test Mode");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_10() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "cdad9e63-b57d-414f-8657-22a3c984921e");
		bean.setPropertyName("testModeEnabled");
		bean.setEntity(runGarbageCollection_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_13() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "776bff65-ed53-40a7-8d51-d2c9796cc901");
		bean.setOwner(localEntityProperty_11());
		bean.setNewValue(variable_12());
		return bean;
	}

	// Managed
	private Variable variable_12() {
		Variable bean = session().createRaw(Variable.T, "9bf732b4-5a9b-47d6-8547-9439b13991a6");
		bean.setTypeSignature("list<string>");
		bean.setName("Use Cases");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_11() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6cf35314-3f75-407c-b531-bfefcd2218c4");
		bean.setPropertyName("useCases");
		bean.setEntity(runGarbageCollection_1());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_104() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "7da7a828-063a-4c25-ab7a-181c7d532af3");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_105() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "6479b104-b62b-4ec4-bc43-7f42ca299b6f");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_14() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "adc2e627-72a8-4566-b683-9f7853b4d69b");
		bean.setOwner(localEntityProperty_12());
		bean.setNewValue(variable_13());
		return bean;
	}

	// Managed
	private Variable variable_13() {
		Variable bean = session().createRaw(Variable.T, "73a2801a-521d-4817-9f1a-837fe8c094cb");
		bean.setTypeSignature("string");
		bean.setName("actionName");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_12() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bedc9171-17b3-4854-90d6-fb75c11184ca");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_2());
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_2() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "88c4bec9-7335-4efa-8a18-772365d9235a");
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_25() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "6ffafff2-7f90-4b4e-b273-a19c59219b51");
		bean.setName("Validate Icon");
		bean.setRepresentations(Sets.set(resource_99(), resource_100(), resource_101(), resource_98()));
		return bean;
	}

	// Managed
	private Resource resource_98() {
		Resource bean = session().createRaw(Resource.T, "16f14f97-761b-4556-b16c-617c13fc81d7");
		bean.setFileSize(1583l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 37, 58, 681));
		bean.setName("tribefire_Icons_04_64x64_ALL-74_24x24.png");
		bean.setSpecification(rasterImageSpecification_106());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_106());
		bean.setMd5("d35d526017e536595c2f1e6cd22be911");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_106() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "73518232-3292-44f7-b76f-c02318fbdbf6");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/37/99508532-b1f0-4438-b79f-2e7f272795ca");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_106() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e9d30337-84ef-494e-ab34-d27fa6b1429e");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_99() {
		Resource bean = session().createRaw(Resource.T, "14e6f1c7-676d-4baf-b285-978dd65ebfaa");
		bean.setFileSize(1291l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 37, 58, 686));
		bean.setName("tribefire_Icons_04_64x64_ALL-74_32x32.png");
		bean.setSpecification(rasterImageSpecification_107());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_107());
		bean.setMd5("6f21e154d6b67df203ab2cfa050fda53");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_107() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "de265c3d-d3c1-4a29-ba7f-a1afd767c1e0");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/37/5aa76948-caea-4906-a682-0f5a7cf0cfd7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_107() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4feaca03-49c8-4b27-9e0c-4a0c380b8a46");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_100() {
		Resource bean = session().createRaw(Resource.T, "52bc511f-befb-453f-8cc0-173ae6f83e17");
		bean.setFileSize(3281l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 37, 58, 693));
		bean.setName("tribefire_Icons_04_64x64_ALL-74_64x64.png");
		bean.setSpecification(rasterImageSpecification_9());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_108());
		bean.setMd5("7f20114b4481be696d30002b2b2c7eb1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_108() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a28aed65-fce0-41f6-8515-08aec286fb3a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/37/1b14438a-af23-4ec6-b672-aac29f7b1634");
		return bean;
	}

	// Managed
	private Resource resource_101() {
		Resource bean = session().createRaw(Resource.T, "520a11a7-7af6-4865-b8d2-a24a0355a299");
		bean.setFileSize(1101l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 37, 58, 671));
		bean.setName("tribefire_Icons_04_64x64_ALL-74_16x16.png");
		bean.setSpecification(rasterImageSpecification_108());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_7());
		bean.setMd5("175d85dafa03b95b713525cef58d66d4");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_108() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1448ce82-9f04-4afb-ac81-5efb061ee243");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_63() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1ec6c0a8-0269-4e54-ac90-d0e9a5d0b09c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy via Service")));
		return bean;
	}

	// Managed
	private Resource resource_102() {
		Resource bean = session().createRaw(Resource.T, "3fc16bbb-8e11-4bdc-a51c-090d386f1f31");
		bean.setFileSize(2773l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 51, 19, 336));
		bean.setName("tribefire_Icons_04_64x64_ALL-48_64x64.png");
		bean.setSpecification(rasterImageSpecification_110());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_101());
		bean.setMd5("a2994bf64ec22638b5ce6f40c141bfb7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_110() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5558d174-9b5e-4a9f-9f3e-96ccf852d2fe");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_65() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "71682921-b21a-4c05-b6d0-79a6489b8fa1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ensure Workbench")));
		return bean;
	}

	@Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "178cd517-13cb-4c94-8a7f-26bc4dd90ef9");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_66());
		bean.setName("$openGmeForAccessWebTerminalInNewTab");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_66() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7a7ecf6a-113a-4a3a-a277-eca8f5ce81af");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	@Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "7898a5a9-1568-4855-8ea9-d5a50f0a6f6e");
		bean.setSubFolders(Lists.list(folder_36(), folder_37(), folder_38(), folder_39(), folder_40(), folder_41(), folder_42(), folder_43(),
				folder_44(), folder_45(), folder_46(), folder_47(), folder_48(), folder_49(), folder_50(), folder_51(), folder_52(), folder_53(),
				folder_55(), folder_68(), folder_69(), folder_70(), folder_71(), folder_72(), folder_73(), folder_74(), folder_34(), folder_157(),
				folder_159(), folder_153(), folder_160(), folder_56()));
		bean.setDisplayName(localizedString_67());
		bean.setIcon(adaptiveIcon_26());
		bean.setName("actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_67() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2b686aaa-afe1-4d48-bd3d-299656a67eb1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actionbar")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_26() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7f599779-d906-48a4-b765-f7789f58789f");
		bean.setName("Synchronize Icon");
		bean.setRepresentations(Sets.set(resource_104(), resource_103(), resource_102()));
		return bean;
	}

	// Managed
	private Resource resource_103() {
		Resource bean = session().createRaw(Resource.T, "56ffd7af-19fe-4272-a2c9-2b473dfc4986");
		bean.setFileSize(1873l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 51, 19, 332));
		bean.setName("tribefire_Icons_04_64x64_ALL-48_32x32.png");
		bean.setSpecification(rasterImageSpecification_111());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_109());
		bean.setMd5("74e93c6a5499f81b1936031a2d78c92d");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_109() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f55d108b-3b47-454e-8f24-a0c5c6feba3b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/51/d5383185-9a9a-4407-9d5e-7319b8553cc3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_111() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "95025163-017b-4346-9398-3306a8010643");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_104() {
		Resource bean = session().createRaw(Resource.T, "52245fae-43b7-495b-bdb5-74cde09a1a71");
		bean.setFileSize(1046l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 51, 19, 321));
		bean.setName("tribefire_Icons_04_64x64_ALL-48_16x16.png");
		bean.setSpecification(rasterImageSpecification_112());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_110());
		bean.setMd5("d4c64ed90e855c254f05cb2b80dad81b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_110() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "2009debe-3f06-4d89-9466-22bcb613c707");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/51/6a547efe-56cd-4276-a4e2-37fdc18173f4");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_112() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "34697179-5124-4f10-a578-499cccb00cfa");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_36() {
		Folder bean = session().createRaw(Folder.T, "c3ca4ebe-0ddb-41ac-ac60-a82951230ae5");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_68());
		bean.setName("$exchangeContentView");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_68() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "58fb4246-446c-4c51-979b-bd13a0032a0a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private Folder folder_37() {
		Folder bean = session().createRaw(Folder.T, "8ea3978b-124b-423f-acc6-475d733aa778");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_69());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_69() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e97e8042-3108-4a40-8846-d65e21472c4d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private Folder folder_38() {
		Folder bean = session().createRaw(Folder.T, "b56bfe72-2f45-4b9f-9245-9a74b1a52cc5");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_70());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_70() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7373889d-b800-47dd-8aef-bb816720f2dd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private Folder folder_39() {
		Folder bean = session().createRaw(Folder.T, "a5952b1b-5a5b-46cd-aeae-818d571b7e2e");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_71());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_71() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5c9ff120-2d53-4352-bc0a-e4916787f173");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete ")));
		return bean;
	}

	// Managed
	private Folder folder_40() {
		Folder bean = session().createRaw(Folder.T, "807c5fcb-3911-4ed1-8a67-2eda44084d25");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_72());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_72() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "902604ed-0585-49ca-b438-39abf62a809f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private Folder folder_41() {
		Folder bean = session().createRaw(Folder.T, "707ad08f-c5d4-4056-837f-9864d17d25f3");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_73());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_73() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cb6c63b7-9be2-4dec-b60a-225e53c23949");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_42() {
		Folder bean = session().createRaw(Folder.T, "815502bc-835f-4f02-b615-b4a44056dee9");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_74());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_74() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6a657a0d-b166-4140-8ff4-e8cc90c8ac18");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private Folder folder_43() {
		Folder bean = session().createRaw(Folder.T, "763f6f0c-6d55-49a3-95f8-cbe6824c3ae9");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_75());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_75() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "86d74d37-fb44-4858-a83b-de1e0c15404b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private Folder folder_44() {
		Folder bean = session().createRaw(Folder.T, "c77710cb-56ef-45d4-93c6-9a0b1ab20462");
		bean.setDisplayName(localizedString_76());
		bean.setName("$addMetaDataEditorAction");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_76() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1483e88d-b1ab-4e44-aabb-a012791e2eab");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add Metadata")));
		return bean;
	}

	// Managed
	private Folder folder_45() {
		Folder bean = session().createRaw(Folder.T, "1312dcc3-24a9-4606-ae0f-d5ba93e111c3");
		bean.setDisplayName(localizedString_77());
		bean.setName("$removeMetaDataEditorAction");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_77() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3addfaa2-1f20-4b39-93aa-743abee9507c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove Metadata")));
		return bean;
	}

	// Managed
	private Folder folder_46() {
		Folder bean = session().createRaw(Folder.T, "3392cb97-0b49-48f9-8b81-78e9a4a02e62");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_78());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_78() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "15bbd053-c562-4d66-b49b-30f2190ace06");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_47() {
		Folder bean = session().createRaw(Folder.T, "79b27ccf-38cb-444a-919c-717e9276c73c");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_79());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_79() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c280cd43-d216-4b40-8959-62415e2d136f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private Folder folder_48() {
		Folder bean = session().createRaw(Folder.T, "96396d78-c5eb-49f9-8b9e-b390f3c597c8");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_80());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_80() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "673e7874-c706-4f89-95ad-17ee6056ffc0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private Folder folder_49() {
		Folder bean = session().createRaw(Folder.T, "c531aa2f-3123-4fb2-aa87-54b53d337939");
		bean.setDisplayName(localizedString_81());
		bean.setIcon(adaptiveIcon_26());
		bean.setName("$refreshMetaDataEditorAction");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_81() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b468c621-0e95-496f-8098-ea4fd8c69148");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh Resolver")));
		return bean;
	}

	// Managed
	private Folder folder_50() {
		Folder bean = session().createRaw(Folder.T, "4f79fb34-76cf-4547-b8c1-008dc09d0fae");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_82());
		bean.setName("$addToClipboard");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_82() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "83567a07-7832-4384-9c44-c86610669c8b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add To Clipboard")));
		return bean;
	}

	// Managed
	private Folder folder_51() {
		Folder bean = session().createRaw(Folder.T, "87eae8a4-f164-4499-ad03-3d83b7a71875");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_83());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_83() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "131a3d76-ce88-4b82-aa90-b346f3a00fc7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private Folder folder_52() {
		Folder bean = session().createRaw(Folder.T, "08bbde97-d24d-4aaf-afde-701f1a78ac61");
		bean.setDisplayName(localizedString_84());
		bean.setIcon(adaptiveIcon_27());
		bean.setName("$executeServiceRequest");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_84() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4887ded4-92b1-4d2c-8065-3985c92bc71f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Execute Service")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_27() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "2d00fc9f-fd74-456f-8d0d-7de775ba91b4");
		bean.setName("Run Icon");
		bean.setRepresentations(Sets.set(resource_106(), resource_105()));
		return bean;
	}

	// Managed
	private Resource resource_105() {
		Resource bean = session().createRaw(Resource.T, "e0a5253d-8188-4b5d-bab7-6dbdd2d6dff8");
		bean.setFileSize(346l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 49, 10, 580));
		bean.setName("right_16x16.png");
		bean.setSpecification(rasterImageSpecification_113());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_111());
		bean.setMd5("5194afa6471a42e57f5beb12242c6768");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_111() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "2afed7b9-b0bc-4c8e-8cfc-0ca70187a013");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/49/57dea10a-b5fa-4f17-9e37-0caf3677501a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_113() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "abdfcb4d-7907-4b85-bc26-81eb6fc1d2b1");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_106() {
		Resource bean = session().createRaw(Resource.T, "5241f2a6-2355-4cf6-8c89-55d6334bcb42");
		bean.setFileSize(551l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 49, 10, 586));
		bean.setName("right_32x32.png");
		bean.setSpecification(rasterImageSpecification_114());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_112());
		bean.setMd5("5478336eb1ce80dbe3fcce9688d18738");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_112() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e21adf6a-cfaa-4e0f-972f-b837b6c208df");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/49/6002abcc-1606-48ed-936d-c270ef9e4dae");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_114() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "dea9244e-6f89-4229-b346-a222a320bd0d");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_53() {
		Folder bean = session().createRaw(Folder.T, "3cfa5099-9fb5-4342-ace0-fa26716b105f");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_87());
		bean.setIcon(adaptiveIcon_28());
		bean.setName("Save to Workbench");
		bean.setContent(templateServiceRequestAction_5());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_5() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "25e04e9e-3b85-4df2-8650-1b6463894693");
		bean.setTemplate(template_21());
		bean.setDisplayName(localizedString_85());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_85() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f075faec-9e84-4fe6-9d35-af993508ee25");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Save to Workbench")));
		return bean;
	}

	// Managed
	private Template template_21() {
		Template bean = session().createRaw(Template.T, "63d3898c-ba8a-4d27-a926-7544de7de81e");
		bean.setName(localizedString_86());
		bean.setPrototype(createServiceRequestTemplate_14());
		bean.setTechnicalName("CreateServiceRequestTemplate");
		bean.setScript(compoundManipulation_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_86() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b2ba0a8c-c625-411e-9f77-3176763daa0b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Save to Workbench Template")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_3() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "f89c740b-8438-4456-8a74-1805a97015a2");
		bean.setCompoundManipulationList(
				Lists.list(changeValueManipulation_15(), changeValueManipulation_16(), changeValueManipulation_17(), changeValueManipulation_18(),
						changeValueManipulation_19(), changeValueManipulation_20(), changeValueManipulation_21(), changeValueManipulation_281()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_15() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "ef33a009-2311-4526-926c-28bdf5e8b183");
		bean.setOwner(localEntityProperty_14());
		bean.setNewValue(variable_14());
		return bean;
	}

	// Managed
	private Variable variable_14() {
		Variable bean = session().createRaw(Variable.T, "b232f66f-63c9-485a-8143-824d94b0d66d");
		bean.setTypeSignature("string");
		bean.setName("Action Name");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_14() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "ff61c82d-064c-4036-a386-d1c2dcf5d99e");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_16() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "9a9d0fc4-bd1c-4adf-af0f-c39fc036d439");
		bean.setOwner(localEntityProperty_15());
		bean.setNewValue(variable_15());
		return bean;
	}

	// Managed
	private Variable variable_15() {
		Variable bean = session().createRaw(Variable.T, "13e4b6c5-9fb6-465f-9e4e-c890d72de1c8");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("Criterion Type");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_15() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "77f81f9f-20f0-490b-8577-0e8206ccbe77");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_17() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "15e4ded1-a925-4dfc-a938-34f0ba4805f7");
		bean.setOwner(localEntityProperty_16());
		bean.setNewValue(variable_16());
		return bean;
	}

	// Managed
	private Variable variable_16() {
		Variable bean = session().createRaw(Variable.T, "982f91b9-0f7c-41c1-99bb-f73cb0103068");
		bean.setTypeSignature("string");
		bean.setDefaultValue("actionbar/more");
		bean.setName("Folder Path");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_16() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "502444e0-3b5d-4549-b186-1a95b585928b");
		bean.setPropertyName("folderPath");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_18() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "cb57b972-dea9-4090-9abf-dbc51b1fad1e");
		bean.setOwner(localEntityProperty_17());
		bean.setNewValue(variable_17());
		return bean;
	}

	// Managed
	private Variable variable_17() {
		Variable bean = session().createRaw(Variable.T, "f4e889e8-27fe-4a66-84a6-87e1d5a3f4aa");
		bean.setTypeSignature("set<string>");
		bean.setName("Ignore Properties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_17() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "56aba793-2e31-4164-8ea0-a0e12eb649b2");
		bean.setPropertyName("ignoreProperties");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_19() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "82517e66-673e-4b85-ab27-5c0d45e337f1");
		bean.setOwner(localEntityProperty_18());
		bean.setNewValue(variable_18());
		return bean;
	}

	// Managed
	private Variable variable_18() {
		Variable bean = session().createRaw(Variable.T, "3e615bcc-8cb5-455a-9632-19d2c0ecdc00");
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("Ignore Standard Properties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_18() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a1621d48-5a3e-4e16-bc87-31a360c244ed");
		bean.setPropertyName("ignoreStandardProperties");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_20() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "fdad5973-2d95-42e9-b34c-d4649b889054");
		bean.setOwner(localEntityProperty_19());
		bean.setNewValue(variable_19());
		return bean;
	}

	// Managed
	private Variable variable_19() {
		Variable bean = session().createRaw(Variable.T, "b70bb906-5399-4745-81a9-5f23b7d0bc3b");
		bean.setTypeSignature("boolean");
		bean.setName("Multi Selection Support");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_19() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "7021420b-412b-4ccc-aefb-bcc6cf796d6e");
		bean.setPropertyName("multiSelectionSupport");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_21() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a75204e7-e6c3-444e-b703-72a7609d08c1");
		bean.setOwner(localEntityProperty_20());
		bean.setNewValue(variable_20());
		return bean;
	}

	// Managed
	private Variable variable_20() {
		Variable bean = session().createRaw(Variable.T, "2a48f2b9-8199-41fe-a659-37a810203c7b");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("Template Request");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_20() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "cbcaf1fe-738b-4cea-a990-8503d6824e42");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_87() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "16d25568-20d5-487b-b409-136f919bd1aa");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Save to Workbench")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_28() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7da31d7d-e3e4-4335-834b-e243e0d07bec");
		bean.setName("Workbench Icon");
		bean.setRepresentations(Sets.set(resource_108(), resource_109(), resource_107()));
		return bean;
	}

	// Managed
	private Resource resource_107() {
		Resource bean = session().createRaw(Resource.T, "c629239c-215b-4a6b-9dd6-cea0a586c9ca");
		bean.setFileSize(1268l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 53, 27, 204));
		bean.setName("WorkbenchAccess_32x32.png");
		bean.setSpecification(rasterImageSpecification_115());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_113());
		bean.setMd5("7432100eac5a1d8c82c24efcab9613b1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_113() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5428311d-ba31-4c94-80c9-5197407e6ceb");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/53/61d7be71-f7e1-4761-8103-99a05959df49");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_115() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "584e368f-4d48-428b-9797-d92aec276dbb");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_108() {
		Resource bean = session().createRaw(Resource.T, "9cf929a4-aaef-4854-9274-51a1d40037c7");
		bean.setFileSize(631l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 53, 27, 195));
		bean.setName("WorkbenchAccess_16x16.png");
		bean.setSpecification(rasterImageSpecification_116());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_114());
		bean.setMd5("3ec437eb296fb7036a451bd06fdf1ca9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_114() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f3776a1f-5607-4c23-8b2c-b4c14acb8e27");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/53/e7b6de9b-ec6b-4b25-a0a8-b62a66cd471c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_116() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "24de6bab-bca1-4a0c-8fa3-1e227873a7a0");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_109() {
		Resource bean = session().createRaw(Resource.T, "36732fb8-522d-4638-9eb5-266ec55f6a11");
		bean.setFileSize(3142l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 53, 27, 208));
		bean.setName("WorkbenchAccess_64x64.png");
		bean.setSpecification(rasterImageSpecification_102());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_115());
		bean.setMd5("4322a15cab1413df52c9a0df0966c29b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_115() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a4231381-5f81-4e8b-a2ff-fe3a78c45ced");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/53/7d4f9dd0-02ce-4545-b755-60fcce66c0f7");
		return bean;
	}

	// Managed
	private Folder folder_55() {
		Folder bean = session().createRaw(Folder.T, "be3e6884-8702-4897-80d1-26d8259659cc");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_93());
		bean.setIcon(adaptiveIcon_12());
		bean.setName("Deploy");
		bean.setContent(templateServiceRequestAction_7());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_7() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "51777270-de54-46c4-aa2b-77a7393ccad4");
		bean.setTemplate(template_23());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_91());
		bean.setIcon(adaptiveIcon_12());
		bean.setInplaceContextCriterion(conjunctionCriterion_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_91() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ef8e0b68-36f1-4b79-b5e4-f53d0c692374");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "DeployViaServiceRequest")));
		return bean;
	}

	@Managed
	private ConjunctionCriterion conjunctionCriterion_2() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "e42d4e19-92b4-491e-8e17-c5430c8d21b6");
		bean.setCriteria(Lists.list(entityCriterion_2(), negationCriterion_1(), valueConditionCriterion_3()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_3() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "0fe669bf-8d41-43ab-bb89-6f584071e536");
		bean.setPropertyPath("deploymentStatus");
		bean.setOperand(DeploymentStatus.undeployed);
		bean.setOperator(ComparisonOperator.equal);
		return bean;
	}

	// Managed
	private Template template_23() {
		Template bean = session().createRaw(Template.T, "863d6c92-7b10-4a71-bbfc-e944747b9564");
		bean.setPrototype(deployWithDeployables_1());
		bean.setScript(changeValueManipulation_23());
		return bean;
	}

	@Managed
	private DeployWithDeployables deployWithDeployables_1() {
		DeployWithDeployables bean = session().createRaw(DeployWithDeployables.T, "9d5a7d28-afac-4187-a839-d762e0fd2f5f");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_23() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "77f2b190-c531-465f-b4a1-c6d86cc28b4c");
		bean.setOwner(localEntityProperty_22());
		bean.setNewValue(variable_22());
		return bean;
	}

	// Managed
	private Variable variable_22() {
		Variable bean = session().createRaw(Variable.T, "5334ca64-717c-48d8-bfbb-cf7dbce804da");
		bean.setTypeSignature("set<com.braintribe.model.deployment.Deployable>");
		bean.setName("Deployables");
		bean.setDescription(localizedString_92());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_92() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7cb654be-e2b7-4d02-8654-c5ad4de0b704");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "v_deployables_0")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_22() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "c93b92cf-2398-4834-b16a-2d9e25c97b41");
		bean.setPropertyName("deployables");
		bean.setEntity(deployWithDeployables_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_93() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a0156d06-6a4f-4d09-98b2-8617d725f142");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy")));
		return bean;
	}

	@Managed
	private Folder folder_56() {
		Folder bean = session().createRaw(Folder.T, "1aed696a-edf1-4f64-b4a0-138800dde6b9");
		bean.setParent(folder_35());
		bean.setSubFolders(Lists.list(folder_57(), folder_58(), folder_60(), folder_61(), folder_63(), folder_64(), folder_66(),
				folder_67(), folder_65(), folder_164(), folder_165()));
		bean.setDisplayName(localizedString_94());
		bean.setIcon(adaptiveIcon_29());
		bean.setName("More");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_94() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c6273720-22cc-490f-8574-78a048da9bdd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "More")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_29() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "a510afde-f9a5-46f5-b546-4c9a30e1a863");
		bean.setName("More Icon");
		bean.setRepresentations(Sets.set(resource_110(), resource_111(), resource_112(), resource_113()));
		return bean;
	}

	// Managed
	private Resource resource_110() {
		Resource bean = session().createRaw(Resource.T, "56af58ef-0076-4666-b43c-3bae93caa090");
		bean.setFileSize(658l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 27, 18, 209));
		bean.setName("tribefire_Icons_04_64x64_More-16_16x16.png");
		bean.setSpecification(rasterImageSpecification_117());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_116());
		bean.setMd5("ac22b8fbe347dbc52005bcf0109649a3");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_116() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "6969529a-b5fa-42c5-9b42-00df4fe6cbc3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/27/071b3212-99f2-4c01-98cb-915ab278b016");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_117() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a435b6a1-27f2-4757-9b85-dd807283ad6e");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_111() {
		Resource bean = session().createRaw(Resource.T, "fa9c46a0-77ec-427d-8cd0-20c956cdf412");
		bean.setFileSize(868l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 27, 18, 221));
		bean.setName("tribefire_Icons_04_64x64_More-16_32x32.png");
		bean.setSpecification(rasterImageSpecification_118());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_117());
		bean.setMd5("c80ed0c59acb287a499accf0ee510aac");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_117() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "9d01783f-e019-409a-815c-d7e830e87187");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/27/9fa6f140-210d-469b-ae96-ff58fe5d8595");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_118() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "dab55612-d46e-4262-b0d3-737b4ef4a52a");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_112() {
		Resource bean = session().createRaw(Resource.T, "974873db-faa5-42fc-9e4d-b650731537d9");
		bean.setFileSize(1101l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 27, 18, 227));
		bean.setName("tribefire_Icons_04_64x64_More-16_64x64.png");
		bean.setSpecification(rasterImageSpecification_105());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_118());
		bean.setMd5("44db36bd0bd9aa488b9bd4dc6394cd0f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_118() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "23e24ba1-ffbf-4726-a644-a0f2b68a1fc8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/27/2cea9fbb-928e-4866-91d0-3edc5a4f5f30");
		return bean;
	}

	// Managed
	private Resource resource_113() {
		Resource bean = session().createRaw(Resource.T, "8c7943a4-ffb5-4b24-8c32-2472068be944");
		bean.setFileSize(766l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 27, 18, 216));
		bean.setName("tribefire_Icons_04_64x64_More-16_24x24.png");
		bean.setSpecification(rasterImageSpecification_119());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_119());
		bean.setMd5("8dcf0e926f085d13587bd936b9a255e0");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_119() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8f468eda-9fa3-4cac-85f9-d71d5b57ae6a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/27/d09db09f-1e49-409c-8377-9b9821713864");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_119() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "eb53ee98-1398-486f-9fde-ebe9871d3353");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Folder folder_57() {
		Folder bean = session().createRaw(Folder.T, "e4b65a5e-1bd2-423f-a50c-e23b8b523069");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_97());
		bean.setIcon(adaptiveIcon_30());
		bean.setName("Add Dependencies");
		bean.setContent(templateServiceRequestAction_8());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_8() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "d282f826-25dc-4e17-9359-7fc85bed83fd");
		bean.setTemplate(template_24());
		bean.setDisplayName(localizedString_95());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_95() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "28d3b8eb-3fff-4760-b329-6cd7c79baa8c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add Dependencies Action")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_5() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "411717c0-8421-448c-8eaf-e5f6a109a8c1");
		bean.setTypeCondition(isAssignableTo_4());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_4() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "1c4e7fea-675f-4261-bd0d-aebf359d8acc");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_24() {
		Template bean = session().createRaw(Template.T, "78b7ebd6-8ce2-491e-bdba-d6df195829ad");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_29()));
		bean.setName(localizedString_96());
		bean.setPrototype(addDependencies_1());
		bean.setTechnicalName("AddDependenciesTemplate");
		bean.setScript(compoundManipulation_6());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_96() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ce36b961-9a9e-44d1-8ad9-11b209466f71");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add Dependencies")));
		return bean;
	}

	@Managed
	private AddDependencies addDependencies_1() {
		AddDependencies bean = session().createRaw(AddDependencies.T, "597fb3f8-75c3-4c27-8e95-fd9ebc842540");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_6() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "81ac4f32-4258-4cbb-8683-ad55a68a8608");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_24(), changeValueManipulation_25()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_24() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "20f1f4d6-269d-4743-bee3-2812ef1515a0");
		bean.setOwner(localEntityProperty_23());
		bean.setNewValue(variable_23());
		return bean;
	}

	@Managed
	private Variable variable_23() {
		Variable bean = session().createRaw(Variable.T, "78d51bd4-d8a5-44e8-8f50-5e1a11513a2a");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		bean.setName("Model");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_23() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "1dcc75e8-2825-4ffa-8782-3170ce41ba4f");
		bean.setPropertyName("model");
		bean.setEntity(addDependencies_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_25() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "fef7376a-6d97-4f6e-998a-af5b7c39610c");
		bean.setOwner(localEntityProperty_24());
		bean.setNewValue(variable_24());
		return bean;
	}

	// Managed
	private Variable variable_24() {
		Variable bean = session().createRaw(Variable.T, "900a7ec8-fa38-4023-912a-89e58ba49745");
		bean.setTypeSignature("list<com.braintribe.model.meta.GmMetaModel>");
		bean.setName("Dependencies");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_24() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "78fdea9f-631a-44bf-aef4-bb4c8321ede0");
		bean.setPropertyName("dependencies");
		bean.setEntity(addDependencies_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_97() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8a0fd0b9-f340-441c-a291-3ee0db797827");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add Dependencies")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_30() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "15261311-0901-43dd-8af1-7cc14f975eca");
		bean.setName("Merge Icon");
		bean.setRepresentations(Sets.set(resource_117(), resource_114(), resource_116(), resource_115()));
		return bean;
	}

	// Managed
	private Resource resource_114() {
		Resource bean = session().createRaw(Resource.T, "eecf96b7-3bab-46e8-8481-b3700e58ff1c");
		bean.setFileSize(1238l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 52, 18, 838));
		bean.setName("MergeModels_24x24.png");
		bean.setSpecification(rasterImageSpecification_120());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_120());
		bean.setMd5("ce2fdc89dad2017d59280e8f4377c434");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_120() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "07e0b3d6-d34d-4062-9a78-a46f880a1d94");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/52/93bd32c6-12c7-4b86-9c1a-d0d59438e347");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_120() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "191f9417-d527-4bdb-8a58-8e3429a1fbd0");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_115() {
		Resource bean = session().createRaw(Resource.T, "83717cf4-e220-40c2-a146-a39e1e888783");
		bean.setFileSize(713l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 52, 18, 833));
		bean.setName("MergeModels_16x16.png");
		bean.setSpecification(rasterImageSpecification_104());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_121());
		bean.setMd5("f90b53e805a2c2ba78561e8dfc17e1ec");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_121() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4ecb5d94-e0a2-483a-9e37-9ef77d9d21dd");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/52/b1077c4a-b557-42b9-84b1-caefe5533ec6");
		return bean;
	}

	// Managed
	private Resource resource_116() {
		Resource bean = session().createRaw(Resource.T, "13b218b7-ec5d-4f77-b56f-2d8e6fb14344");
		bean.setFileSize(4650l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 52, 18, 847));
		bean.setName("MergeModels_64x64.png");
		bean.setSpecification(rasterImageSpecification_121());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_122());
		bean.setMd5("0e22a9ad06700620501b14a00acf0ead");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_122() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "231fb13f-fa57-46c3-9b65-492566eb0cd7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/52/cfffb150-1d8a-4eae-bd98-9837ea86422c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_121() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "51f77e79-c026-493d-8b2e-468d6fe6c916");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_117() {
		Resource bean = session().createRaw(Resource.T, "63487cd5-ea0a-4037-a308-9569871c8b6f");
		bean.setFileSize(1832l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 52, 18, 842));
		bean.setName("MergeModels_32x32.png");
		bean.setSpecification(rasterImageSpecification_122());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_123());
		bean.setMd5("ec19fb8f201b450dd5c8a3f9abc625a5");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_123() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ea2c6f64-30db-41c5-a5da-6a5b4767b68a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/52/0610533c-0556-4c60-982e-8d2b2f0a7a67");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_122() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4b602dd6-1412-40e4-bbcb-8d37a72be44c");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_58() {
		Folder bean = session().createRaw(Folder.T, "4b4dac9b-3b0a-4c0f-829b-6950347fffac");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_100());
		bean.setIcon(adaptiveIcon_25());
		bean.setName("Validate");
		bean.setContent(templateServiceRequestAction_9());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_9() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "c162f39a-cd45-4075-affa-67e4647b2679");
		bean.setTemplate(template_25());
		bean.setDisplayName(localizedString_98());
		bean.setInplaceContextCriterion(typeConditionCriterion_6());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_98() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "629fdf16-cba5-4ad2-81d2-4dbdb827002e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Validate")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_6() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "e54946ad-f447-4c47-a87a-de2a7e6672be");
		bean.setTypeCondition(isAssignableTo_5());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_5() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "707f7f8e-c4a7-4c98-9314-48c4d94e50ea");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_25() {
		Template bean = session().createRaw(Template.T, "16df0b46-6b99-4688-93e9-8ef63d9e55a6");
		bean.setName(localizedString_99());
		bean.setPrototype(validateModel_2());
		bean.setTechnicalName("ValidateModel");
		bean.setScript(changeValueManipulation_26());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_99() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d6714160-563e-4271-891f-04d4b0794cd5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Validate Template")));
		return bean;
	}

	@Managed
	private ValidateModel validateModel_2() {
		ValidateModel bean = session().createRaw(ValidateModel.T, "43bd07d6-9a6d-4fc4-a848-963e77ed34d6");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_26() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "18fbb1bc-2474-451a-a8c3-34e601b8c567");
		bean.setOwner(localEntityProperty_25());
		bean.setNewValue(variable_25());
		return bean;
	}

	// Managed
	private Variable variable_25() {
		Variable bean = session().createRaw(Variable.T, "2e16ed3f-06d1-4254-b25c-694f3e7e643f");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		bean.setName("Model");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_25() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "3487d79a-d03e-4721-8371-ed37950e55a9");
		bean.setPropertyName("model");
		bean.setEntity(validateModel_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_100() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c6e18c7b-8314-49e2-9d87-819adadfe86c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Validate")));
		return bean;
	}

	// Managed
	private Folder folder_60() {
		Folder bean = session().createRaw(Folder.T, "e7dc4123-e0b1-4c5a-9bb5-bb00b203f43b");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_106());
		bean.setIcon(adaptiveIcon_26());
		bean.setName("Notify Change");
		bean.setContent(templateServiceRequestAction_11());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_11() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "40c7384b-ed16-418b-b63e-fa9b4e235a4d");
		bean.setTemplate(template_27());
		bean.setDisplayName(localizedString_104());
		bean.setInplaceContextCriterion(typeConditionCriterion_8());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_104() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2dba44d3-d00d-4a4b-aadf-c5370cf2035d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notify Change")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_8() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "02c18198-eccb-48e0-a6c7-b6ee771f0320");
		bean.setTypeCondition(isAssignableTo_7());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_7() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "ad6bc1bd-7313-4ccf-8805-25444eadcc76");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_27() {
		Template bean = session().createRaw(Template.T, "2a8c0aef-0225-454a-ab10-8c4fe1c0cad7");
		bean.setName(localizedString_105());
		bean.setPrototype(notifyModelChanged_1());
		bean.setTechnicalName("NotifyModelChanged");
		bean.setScript(changeValueManipulation_28());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_105() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c6802b38-6d3e-4933-b802-493d9b6f865b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notify Change Template")));
		return bean;
	}

	@Managed
	private NotifyModelChanged notifyModelChanged_1() {
		NotifyModelChanged bean = session().createRaw(NotifyModelChanged.T, "faf93312-cdab-4538-8e38-b6585eff1844");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_28() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "433ed659-a62f-49b1-aeec-110c8e05f202");
		bean.setOwner(localEntityProperty_27());
		bean.setNewValue(variable_27());
		return bean;
	}

	// Managed
	private Variable variable_27() {
		Variable bean = session().createRaw(Variable.T, "ce305736-91f1-4938-8abf-f8f0674a0db3");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		bean.setName("Model");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_27() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "87004c0c-3f42-46ce-8c93-48093dd91c1e");
		bean.setPropertyName("model");
		bean.setEntity(notifyModelChanged_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_106() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4fd4669a-955f-4dff-8572-b9cea26f1074");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notify Change")));
		return bean;
	}

	@Managed
	private Folder folder_61() {
		Folder bean = session().createRaw(Folder.T, "3dac34d4-a62a-4546-a00b-8ffe14be8143");
		bean.setParent(folder_62());
		bean.setDisplayName(localizedString_110());
		bean.setIcon(adaptiveIcon_32());
		bean.setName("Setup Aspects");
		bean.setContent(templateServiceRequestAction_12());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_12() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "29934fed-1691-4b39-8348-d6c1225c6697");
		bean.setTemplate(template_28());
		bean.setDisplayName(localizedString_107());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_107() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a2cc5d65-9699-42c2-a54f-a72978483510");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Aspects Action")));
		return bean;
	}

	@Managed
	private ConjunctionCriterion conjunctionCriterion_3() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "8b72f1b9-029c-41d6-896b-6e02b0cb8c53");
		bean.setCriteria(Lists.list(typeConditionCriterion_9(), negationCriterion_1()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_9() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "9c4a8ad1-5c68-459f-9eb4-585bc8772de3");
		bean.setTypeCondition(isAssignableTo_8());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_8() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "b7c75876-0d7d-41ae-ae59-77b144ff3108");
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		return bean;
	}

	// Managed
	private Template template_28() {
		Template bean = session().createRaw(Template.T, "e7167bc8-6788-4815-a952-4b9e0595f41c");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_3()));
		bean.setName(localizedString_109());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.access.SetupAspects");
		bean.setPrototype(setupAspects_1());
		bean.setTechnicalName("SetupAspectsTemplate");
		bean.setScript(compoundManipulation_10());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_3() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "c5e38f31-a8ae-49bc-9c0b-522a260964c3");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_28());
		return bean;
	}

	@Managed
	private Variable variable_28() {
		Variable bean = session().createRaw(Variable.T, "d257c625-3275-4206-b6b9-f59a86baa600");
		bean.setLocalizedName(localizedString_108());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_108() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "381ada7d-6d02-427e-80e1-4dc46638bf8c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_109() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c2656b47-3503-4b6f-9884-df9869793d94");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Aspects")));
		return bean;
	}

	@Managed
	private SetupAspects setupAspects_1() {
		SetupAspects bean = session().createRaw(SetupAspects.T, "3986d0e3-929f-48d5-b04a-d04301aff23e");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_10() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "450551e7-95a7-46ac-828d-d8f7b1ed58be");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_29(), changeValueManipulation_30()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_29() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4a10f5b9-eeaf-4168-b00d-6661a1858eba");
		bean.setOwner(localEntityProperty_28());
		bean.setNewValue(variable_28());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_28() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "af69bbf9-5c33-4b09-9d97-a7af757a7ef5");
		bean.setPropertyName("access");
		bean.setEntity(setupAspects_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_30() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "331d7d60-954d-4c6e-8a83-635c77809fef");
		bean.setOwner(localEntityProperty_29());
		bean.setNewValue(variable_29());
		return bean;
	}

	// Managed
	private Variable variable_29() {
		Variable bean = session().createRaw(Variable.T, "21dd3991-4948-45b3-b216-44fed0f00351");
		bean.setTypeSignature("boolean");
		bean.setName("Reset To Default");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_29() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "640cc6c2-42ba-4784-ae9b-52b25384d3a1");
		bean.setPropertyName("resetToDefault");
		bean.setEntity(setupAspects_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_110() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d81e4d9e-4e27-4c3a-8944-de0788c10541");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Aspects")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_32() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "29d88726-df58-45eb-9f7c-36fa400e143a");
		bean.setName("Configurator Icon");
		bean.setRepresentations(Sets.set(resource_122(), resource_123(), resource_124()));
		return bean;
	}

	// Managed
	private Resource resource_122() {
		Resource bean = session().createRaw(Resource.T, "084add08-ccf6-404e-89b7-ff3bd0bf5025");
		bean.setFileSize(505l);
		bean.setCreated(newGmtDate(2015, 4, 24, 20, 7, 38, 312));
		bean.setName("settings_16x16.png");
		bean.setSpecification(rasterImageSpecification_127());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_128());
		bean.setMd5("f255f2f29a56af5dfa341e27e64dcbe9");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_128() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ec762eed-c9e1-4286-8084-72944426aa5f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2422/07/f22fdb98-a90f-45b4-8682-b805a5ef2d7b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_127() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "20013a1f-e06c-4d7b-9428-e1e69355df69");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_123() {
		Resource bean = session().createRaw(Resource.T, "5c6605a3-27a0-422d-8e7c-2bd46da46217");
		bean.setFileSize(1363l);
		bean.setCreated(newGmtDate(2015, 4, 24, 20, 7, 38, 333));
		bean.setName("tribefire_Icons_04_64x64_ALL-01_32x32.png");
		bean.setSpecification(rasterImageSpecification_128());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_129());
		bean.setMd5("a1ed0cefbdf8b0431beb707f3ea5c44b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_129() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "956b406e-0e10-416b-acb3-89c660612a32");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2422/07/f647a69e-f864-4644-8163-8f2b0926b967");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_128() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f94d929a-0166-495b-8984-89d40f0ee2a5");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_124() {
		Resource bean = session().createRaw(Resource.T, "e8f275c7-fd24-4bb7-8256-293e2c169fac");
		bean.setFileSize(3634l);
		bean.setCreated(newGmtDate(2015, 4, 24, 20, 7, 38, 337));
		bean.setName("tribefire_Icons_04_64x64_ALL-01_64x64.png");
		bean.setSpecification(rasterImageSpecification_129());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_130());
		bean.setMd5("1fa3f4ca05759104ff15645f7bdcaa3f");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_130() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cdba9f68-419e-4cc8-bef2-1e7486725656");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2422/07/6e87eb91-54cd-4bbb-89e9-656056537769");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_129() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b2ac14b9-b488-461b-9387-83a4e542560b");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	@Managed
	private Folder folder_62() {
		Folder bean = session().createRaw(Folder.T, "cda91ded-4f71-4009-8e81-771cea0b71f2");
		bean.setSubFolders(Lists.list(folder_61(), folder_63(), folder_64(), folder_65()));
		bean.setDisplayName(localizedString_111());
		bean.setIcon(adaptiveIcon_32());
		bean.setName("setup");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_111() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "60c22e57-dd6e-4d18-8aa4-365e2050b8b2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup")));
		return bean;
	}

	@Managed
	private Folder folder_63() {
		Folder bean = session().createRaw(Folder.T, "4212e96f-142a-4262-b9e4-bbbe65d541b0");
		bean.setParent(folder_62());
		bean.setDisplayName(localizedString_115());
		bean.setIcon(adaptiveIcon_32());
		bean.setName("Setup Workbench");
		bean.setContent(templateServiceRequestAction_13());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_13() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "1564075b-90ce-4bd3-bd5c-7b8476efe60e");
		bean.setTemplate(template_29());
		bean.setDisplayName(localizedString_112());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_112() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c93f25b8-60dc-4d99-b815-a1132e430f8a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Workbench")));
		return bean;
	}

	// Managed
	private Template template_29() {
		Template bean = session().createRaw(Template.T, "0a9a5e0a-d993-4bf0-9011-9a01f5288d36");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_4()));
		bean.setName(localizedString_114());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.access.SetupWorkbench");
		bean.setPrototype(setupWorkbench_1());
		bean.setTechnicalName("SetupWorkbenchTemplate");
		bean.setScript(compoundManipulation_11());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_4() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "fe55389a-5794-4e6a-b9d0-2449258a007e");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_30());
		return bean;
	}

	@Managed
	private Variable variable_30() {
		Variable bean = session().createRaw(Variable.T, "34894aac-c6c4-4eb6-826f-b53cafb7a20a");
		bean.setLocalizedName(localizedString_113());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_113() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "edaab256-c87a-403a-80ff-994fef8df27d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_114() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "955b02f6-a647-41cb-98bf-7955804a6cd1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Workbench")));
		return bean;
	}

	@Managed
	private SetupWorkbench setupWorkbench_1() {
		SetupWorkbench bean = session().createRaw(SetupWorkbench.T, "512b5df8-4ea7-467a-b362-7f580e6bf7eb");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_11() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "8d67b749-73ec-4df2-9424-ce556857d502");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_31(), changeValueManipulation_32(), changeValueManipulation_33()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_31() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "f3c1390a-32f2-47de-a1c9-7b40e5832380");
		bean.setOwner(localEntityProperty_30());
		bean.setNewValue(variable_30());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_30() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0037da70-4b2c-4d4d-a671-7d12e0bc9216");
		bean.setPropertyName("access");
		bean.setEntity(setupWorkbench_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_32() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "303f4cbd-2f19-44df-87e6-658c6a56b8fa");
		bean.setOwner(localEntityProperty_31());
		bean.setNewValue(variable_31());
		return bean;
	}

	// Managed
	private Variable variable_31() {
		Variable bean = session().createRaw(Variable.T, "2a45bb4b-44d2-4bb2-bea7-88a3b7a212f0");
		bean.setTypeSignature("boolean");
		bean.setName("Reset Existing Access");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_31() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "28365d63-9d5c-41cc-990a-09304f25c883");
		bean.setPropertyName("resetExistingAccess");
		bean.setEntity(setupWorkbench_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_33() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3e4e9084-10dc-4680-bf2f-fc431d88dea9");
		bean.setOwner(localEntityProperty_32());
		bean.setNewValue(variable_32());
		return bean;
	}

	// Managed
	private Variable variable_32() {
		Variable bean = session().createRaw(Variable.T, "9c6ec48e-1778-4726-b610-bbbdad0c6720");
		bean.setTypeSignature("boolean");
		bean.setName("Reset Existing Model");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_32() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b5d74687-ee20-4794-9a75-4e292c3ac1f5");
		bean.setPropertyName("resetExistingModel");
		bean.setEntity(setupWorkbench_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_115() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c2e5db94-4566-426c-964f-9e67c73275cf");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Setup Workbench")));
		return bean;
	}

	@Managed
	private Folder folder_64() {
		Folder bean = session().createRaw(Folder.T, "29ed642e-a5ce-4e64-9ff5-1ef54b8ee893");
		bean.setParent(folder_62());
		bean.setDisplayName(localizedString_119());
		bean.setIcon(adaptiveIcon_28());
		bean.setName("Configure Workbench");
		bean.setContent(templateServiceRequestAction_14());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_14() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "ac3de242-37a9-4c45-b4d5-6635a9d51010");
		bean.setTemplate(template_30());
		bean.setDisplayName(localizedString_116());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_116() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8c221df1-d76f-4573-a6ad-2f7d1ebd7c1a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configure Workbench Action")));
		return bean;
	}

	// Managed
	private Template template_30() {
		Template bean = session().createRaw(Template.T, "3e23d448-5cdd-4272-8b65-408cfbf1e9da");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_5()));
		bean.setName(localizedString_118());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.access.ConfigureWorkbench");
		bean.setPrototype(configureWorkbench_1());
		bean.setTechnicalName("ConfigureWorkbenchTemplate");
		bean.setScript(compoundManipulation_12());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_5() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "837e7c6f-f5f4-4978-b6c2-c486ff077396");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_33());
		return bean;
	}

	@Managed
	private Variable variable_33() {
		Variable bean = session().createRaw(Variable.T, "9fba6894-f708-4f4d-add2-336a77467310");
		bean.setLocalizedName(localizedString_117());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_117() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f661c164-606c-43d6-9299-dd0b2d8da137");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_118() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "43e66116-ff5b-48fd-9715-0bfd1849d40d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configure Workbench ")));
		return bean;
	}

	@Managed
	private ConfigureWorkbench configureWorkbench_1() {
		ConfigureWorkbench bean = session().createRaw(ConfigureWorkbench.T, "9a334f2f-135f-441f-91d8-4659371b54c4");
		bean.setEnsureStandardFolders(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_12() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "5c03fe76-e406-427a-831c-d92c03621cb8");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_34(), changeValueManipulation_35(), changeValueManipulation_36()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_34() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "d1b30054-e3f9-4dcd-916b-2dfc39c38972");
		bean.setOwner(localEntityProperty_33());
		bean.setNewValue(variable_33());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_33() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bf388c0e-9cc5-49ec-bbc1-3b6c828aff01");
		bean.setPropertyName("access");
		bean.setEntity(configureWorkbench_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_35() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1699f73e-49f8-4bb2-a335-40e73167b39b");
		bean.setOwner(localEntityProperty_34());
		bean.setNewValue(variable_34());
		return bean;
	}

	// Managed
	private Variable variable_34() {
		Variable bean = session().createRaw(Variable.T, "96a015e2-de69-4a3f-a517-832808a4215f");
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("Ensure Standard Folders");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_34() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "449612d2-c982-4b5f-9be0-27aca64ea558");
		bean.setPropertyName("ensureStandardFolders");
		bean.setEntity(configureWorkbench_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_36() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "50d30c30-6b33-431a-ae1c-e38936d63a0d");
		bean.setOwner(localEntityProperty_35());
		bean.setNewValue(variable_35());
		return bean;
	}

	// Managed
	private Variable variable_35() {
		Variable bean = session().createRaw(Variable.T, "fb91601a-97a0-49c7-8843-a85977a0565d");
		bean.setTypeSignature("com.braintribe.model.cortexapi.access.ExplorerStyle");
		bean.setDefaultValue(ExplorerStyle.tribefireOrange);
		bean.setName("Explorer Style");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_35() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "72d15346-5025-4ceb-8e0e-699dd95a3a2b");
		bean.setPropertyName("explorerStyle");
		bean.setEntity(configureWorkbench_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_119() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8ae2213f-cf4c-42e9-b2f8-ea490c070f52");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configure Workbench")));
		return bean;
	}

	@Managed
	private Folder folder_65() {
		Folder bean = session().createRaw(Folder.T, "afa67bb8-9546-49ae-bcd7-6d09f49617dd");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_121());
		bean.setIcon(adaptiveIcon_33());
		bean.setName("Garbage Collection");
		bean.setContent(templateServiceRequestAction_15());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_15() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "fb767fe5-cc2d-4c31-9dee-363eb916ab3c");
		bean.setTemplate(template_20());
		bean.setDisplayName(localizedString_120());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_120() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4454d67d-a5ca-4aaf-98b1-3b16d58b808f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Garbage Collection")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_4() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "0e53e23c-c271-4a24-888b-6f710e2c25a4");
		bean.setCriteria(Lists.list(typeConditionCriterion_10(), negationCriterion_1()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_10() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "94cd2bb8-4014-4503-9d19-bdd397e2b921");
		bean.setTypeCondition(isAssignableTo_9());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_9() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "b6979ea3-047d-4135-be86-b08f8aa3af93");
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_121() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1e48cb38-aeb5-4685-afed-bbc5bf9ca5a5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Garbage Collection")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_33() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "9dac6168-f8ef-450e-9d6f-73d64d33458a");
		bean.setName("GC Icon");
		bean.setRepresentations(Sets.set(resource_127(), resource_125(), resource_126()));
		return bean;
	}

	// Managed
	private Resource resource_125() {
		Resource bean = session().createRaw(Resource.T, "ebde4822-cd47-41f9-a1cf-9bf97a5e0d1a");
		bean.setFileSize(442l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 53, 30, 871));
		bean.setName("remove_16x16.png");
		bean.setSpecification(rasterImageSpecification_130());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_131());
		bean.setMd5("921e9b91b17468c5c699bd9e7aee2b34");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_131() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "09667f2a-9aa9-46fe-839d-27f804a1a7e8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/53/0e6d6215-36a4-4521-b126-aae3e737a03a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_130() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "84e5e6bb-012e-4e56-bf15-8999e4358446");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_126() {
		Resource bean = session().createRaw(Resource.T, "8c117b81-6acc-49e0-8684-7dc4454aefb7");
		bean.setFileSize(2122l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 53, 30, 886));
		bean.setName("tribefire_Icons_04_64x64_ALL-03_64x64.png");
		bean.setSpecification(rasterImageSpecification_131());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_132());
		bean.setMd5("03ab87647aaa3b13c3e55d4b2c62cee7");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_132() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f56e46bd-7ecf-4c16-b7db-bf51052b1c2b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/53/fbfcc8bb-8418-41f8-8fc2-79b2ce207691");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_131() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b5162dba-7111-4406-83bb-c2ec60c0e1c8");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_127() {
		Resource bean = session().createRaw(Resource.T, "e23985a7-14d3-4bfa-ab92-500378b18487");
		bean.setFileSize(904l);
		bean.setCreated(newGmtDate(2015, 4, 24, 19, 53, 30, 878));
		bean.setName("remove_32x32.png");
		bean.setSpecification(rasterImageSpecification_132());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_133());
		bean.setMd5("1c6ff9371ea2d690225047a937e99943");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_133() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e4d451c8-7ada-4a02-95cd-404fcecd3ea7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2421/53/f830e45c-cc61-49ec-b8e6-7f142f5264da");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_132() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "15d92b1f-0806-4a0a-a18b-c003fdf62f26");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_66() {
		Folder bean = session().createRaw(Folder.T, "47c2fac6-c675-47bb-be03-9288464963b5");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_127());
		bean.setIcon(adaptiveIcon_26());
		bean.setName("Synchronize Model with DB");
		bean.setContent(templateServiceRequestAction_16());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_16() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "2373e852-947b-44a0-9efb-2e479c34a535");
		bean.setTemplate(template_31());
		bean.setDisplayName(localizedString_122());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_122() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a167f81d-41e1-4f10-b38c-7fc97320ad86");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Synchronize Model with DB Schema")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_5() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "297d212d-027c-4e2d-9350-7945067d6218");
		bean.setCriteria(Lists.list(typeConditionCriterion_11(), valueConditionCriterion_4()));
		return bean;
	}

	@Managed
	private TypeConditionCriterion typeConditionCriterion_11() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "2bd469e9-5715-4836-875e-8ae11f376162");
		bean.setTypeCondition(isAssignableTo_10());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_10() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "563b1302-93b1-48f3-95e0-8bea396b8b42");
		bean.setTypeSignature("com.braintribe.model.accessdeployment.hibernate.HibernateAccess");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_4() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "aa596ada-762f-45f2-8eb9-05a2d8ec8910");
		bean.setPropertyPath("metaModel");
		bean.setOperator(ComparisonOperator.notEqual);
		return bean;
	}

	// Managed
	private Template template_31() {
		Template bean = session().createRaw(Template.T, "3ec6c4d2-8dd4-4b49-8a18-b15119545fdc");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_6()));
		bean.setName(localizedString_124());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.connection.SynchronizeModelWithDbSchema");
		bean.setPrototype(synchronizeModelWithDbSchema_1());
		bean.setTechnicalName("SynchronizeModelWithDbSchemaTemplate");
		bean.setScript(compoundManipulation_13());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_6() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "9a1786b5-c920-42fd-bd98-d0c94b6a4edd");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_36());
		return bean;
	}

	@Managed
	private Variable variable_36() {
		Variable bean = session().createRaw(Variable.T, "abd84d40-6ba7-4b84-8a9c-ff52be0bb27e");
		bean.setLocalizedName(localizedString_123());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.hibernate.HibernateAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_123() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9503b7ba-de28-4893-8721-78089266b3f4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_124() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "76606366-5efd-448e-b8aa-7a3983c9a898");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Synchronize Model With Db Schema")));
		return bean;
	}

	@Managed
	private SynchronizeModelWithDbSchema synchronizeModelWithDbSchema_1() {
		SynchronizeModelWithDbSchema bean = session().createRaw(SynchronizeModelWithDbSchema.T, "a1007b40-eb2a-4bc1-8c10-a5371a6fb350");
		bean.setIgnoreUnsupportedTables(Boolean.TRUE);
		bean.setResolveRelationships(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_13() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "8972f17c-1717-4668-b077-29869ae442fa");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_37(), changeValueManipulation_38(), changeValueManipulation_39()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_37() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a429e5dc-77db-4217-a9b3-97a723fa6749");
		bean.setOwner(localEntityProperty_36());
		bean.setNewValue(variable_36());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_36() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "cae1a183-9cdf-46d3-8165-762b8892e188");
		bean.setPropertyName("access");
		bean.setEntity(synchronizeModelWithDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_38() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3f7b4b48-d48e-45d9-8116-bf1afe84e816");
		bean.setOwner(localEntityProperty_37());
		bean.setNewValue(variable_37());
		return bean;
	}

	// Managed
	private Variable variable_37() {
		Variable bean = session().createRaw(Variable.T, "b99eabfd-cf12-4038-a064-de0cdb63b250");
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("Ignore Unsupported Tables");
		bean.setDescription(localizedString_125());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_125() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "44a1c822-2276-4ac2-8956-a57c772cf091");
		bean.setLocalizedValues(
				Maps.map(Maps.entry("default", "If set to false model types will be created for unsupported tables but declared as unmapped.")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_37() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "044a7b85-8fc2-42a3-a9e2-7bf963c91987");
		bean.setPropertyName("ignoreUnsupportedTables");
		bean.setEntity(synchronizeModelWithDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_39() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "423b20e5-09e2-4bea-9fee-c982b19ef12e");
		bean.setOwner(localEntityProperty_38());
		bean.setNewValue(variable_38());
		return bean;
	}

	// Managed
	private Variable variable_38() {
		Variable bean = session().createRaw(Variable.T, "e1a46cfb-b096-47cf-8767-31e3119dbda1");
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("Resolve Relationships");
		bean.setDescription(localizedString_126());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_126() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "53c0be6e-e9e4-4581-acbf-527a32b9ad19");
		bean.setLocalizedValues(
				Maps.map(Maps.entry("default", "If set to true relationship constraints between tables will be taken into account.")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_38() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "af72f6a0-bc3f-4300-a28b-a6493e46a2ec");
		bean.setPropertyName("resolveRelationships");
		bean.setEntity(synchronizeModelWithDbSchema_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_127() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a51fe2f7-bb33-4ee5-8cf2-8ac091e7dbfc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Synchronize Model with DB")));
		return bean;
	}

	// Managed
	private Folder folder_67() {
		Folder bean = session().createRaw(Folder.T, "45cd1ba3-2bc9-4627-b025-0c8e61cc6b3c");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_133());
		bean.setIcon(adaptiveIcon_34());
		bean.setName("Create Model from DB");
		bean.setContent(templateServiceRequestAction_17());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_17() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "59fe5539-578e-4f46-9671-fc303cd27e96");
		bean.setTemplate(template_32());
		bean.setDisplayName(localizedString_128());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_6());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_128() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d798634f-905a-4cf6-968b-6658260e4a45");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Model from DB")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_6() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "bda6a837-5a3c-4a74-b29e-ea3849991e40");
		bean.setCriteria(Lists.list(typeConditionCriterion_11(), valueConditionCriterion_5()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_5() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "f621a143-de59-4f78-9034-f15f633b421b");
		bean.setPropertyPath("metaModel");
		bean.setOperator(ComparisonOperator.equal);
		return bean;
	}

	// Managed
	private Template template_32() {
		Template bean = session().createRaw(Template.T, "a5dec584-bcbf-4ada-8c87-ca65b729d22c");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_7(), dynamicPropertyMetaDataAssignment_11(),
				dynamicPropertyMetaDataAssignment_10(), dynamicPropertyMetaDataAssignment_8(), dynamicPropertyMetaDataAssignment_9()));
		bean.setName(localizedString_130());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.connection.CreateModelFromDbSchema");
		bean.setPrototype(createModelFromDbSchema_1());
		bean.setTechnicalName("CreateModelFromDbSchemaTemplate");
		bean.setScript(compoundManipulation_14());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_7() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "79b93e10-4d95-4a01-b3f4-143ec3ed8bd2");
		bean.setMetaData(Sets.set(priority_2()));
		bean.setVariable(variable_2());
		return bean;
	}

	// Managed
	private Priority priority_2() {
		Priority bean = session().createRaw(Priority.T, "d2347a2f-810b-4565-9bb2-c3bfb4f9f2b0");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.92d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_8() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "385b5a7c-a458-40fe-adba-9b435cb0a2ab");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_39());
		return bean;
	}

	@Managed
	private Variable variable_39() {
		Variable bean = session().createRaw(Variable.T, "88a7778a-6813-47fd-ad09-c1a260915d6e");
		bean.setLocalizedName(localizedString_129());
		bean.setTypeSignature("com.braintribe.model.accessdeployment.hibernate.HibernateAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_129() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "61d7d706-bde4-45e2-ab6c-c583645c6b5d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Access")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_9() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "2ccb6ff1-c06a-46f5-a697-31390576926c");
		bean.setMetaData(Sets.set(priority_3()));
		bean.setVariable(variable_40());
		return bean;
	}

	// Managed
	private Priority priority_3() {
		Priority bean = session().createRaw(Priority.T, "ab4fa11e-14f2-4a35-bb7b-7f3d4087ded4");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.9d);
		return bean;
	}

	@Managed
	private Variable variable_40() {
		Variable bean = session().createRaw(Variable.T, "efc30c3d-377f-448b-860c-96cc35f7627b");
		bean.setTypeSignature("list<com.braintribe.model.meta.GmMetaModel>");
		bean.setName("Dependencies");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_10() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "9be83252-93f3-4e65-a644-aa182bd1b8b0");
		bean.setMetaData(Sets.set(priority_4()));
		bean.setVariable(variable_41());
		return bean;
	}

	// Managed
	private Priority priority_4() {
		Priority bean = session().createRaw(Priority.T, "5b28d2b9-e04b-454e-b2dc-759992143c7a");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.91d);
		return bean;
	}

	@Managed
	private Variable variable_41() {
		Variable bean = session().createRaw(Variable.T, "aaace993-5c68-40e7-a05f-43dbfad14781");
		bean.setTypeSignature("string");
		bean.setDefaultValue("1.0");
		bean.setName("Version");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_11() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "aa5b49be-31dc-4467-9ff8-4ccd2a7febce");
		bean.setMetaData(Sets.set(priority_1()));
		bean.setVariable(variable_42());
		return bean;
	}

	@Managed
	private Variable variable_42() {
		Variable bean = session().createRaw(Variable.T, "444f11bf-a8d2-4086-807c-5826950f0ca5");
		bean.setTypeSignature("string");
		bean.setName("Name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_130() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9d21293d-e683-4f71-97c5-9d80f163a5f5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Model From Db Schema")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_14() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "c62ba99e-c564-41b7-9d14-a9c6c02e5946");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_40(), changeValueManipulation_41(), changeValueManipulation_42(),
				changeValueManipulation_7(), changeValueManipulation_43(), changeValueManipulation_44(), changeValueManipulation_45()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_40() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "afbd8046-be4e-4907-8b56-873ebb68f757");
		bean.setOwner(localEntityProperty_39());
		bean.setNewValue(variable_39());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_39() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "c12331ff-68eb-4fe1-9dbd-287636ee76a3");
		bean.setPropertyName("access");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_41() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "71730a1a-90dc-4d30-98dc-c04a1d54338f");
		bean.setOwner(localEntityProperty_40());
		bean.setNewValue(variable_40());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_40() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "c153f7d7-0120-4ca9-9b07-af117a20fbc5");
		bean.setPropertyName("dependencies");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_42() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "caf375b3-e229-43ef-baaf-35cafdee1162");
		bean.setOwner(localEntityProperty_41());
		bean.setNewValue(variable_2());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_41() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0e012fc6-6ccf-42a2-b0d9-9712a6abce5f");
		bean.setPropertyName("groupId");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_43() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4ff9064e-d603-4aa7-80ca-44dbd86c2d81");
		bean.setOwner(localEntityProperty_42());
		bean.setNewValue(variable_42());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_42() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "e67b668f-6644-45c8-a593-59241b8efaa2");
		bean.setPropertyName("name");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_44() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "57750fd6-7302-4e4a-a42e-86d3434e8a2c");
		bean.setOwner(localEntityProperty_43());
		bean.setNewValue(variable_43());
		return bean;
	}

	// Managed
	private Variable variable_43() {
		Variable bean = session().createRaw(Variable.T, "331b2c3e-3378-463b-af4a-330d3d9de7ad");
		bean.setLocalizedName(localizedString_132());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("resolveRelationships");
		bean.setDescription(localizedString_131());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_131() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2f683988-9b01-47da-9358-4de7a7bd2958");
		bean.setLocalizedValues(
				Maps.map(Maps.entry("default", "If set to true relationship constraints between tables will be taken into account.")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_132() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4dc31044-a6cd-47d5-8737-8443155b59c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resolve Relationships")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_43() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "70b271f0-8def-4daa-8cb7-6b21cfa374b8");
		bean.setPropertyName("resolveRelationships");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_45() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "2eced5e2-be61-4697-b516-f39bbaad1f0b");
		bean.setOwner(localEntityProperty_44());
		bean.setNewValue(variable_41());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_44() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bad32259-4828-469c-b2fd-f44543314490");
		bean.setPropertyName("version");
		bean.setEntity(createModelFromDbSchema_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_133() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "85a25491-40ad-43ff-95ec-a41019e39e33");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Model from DB")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_34() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "2d29f006-5af8-4faf-a8d7-b1463c0aee5d");
		bean.setName("Create Icon");
		bean.setRepresentations(Sets.set(resource_128(), resource_129(), resource_130()));
		return bean;
	}

	// Managed
	private Resource resource_128() {
		Resource bean = session().createRaw(Resource.T, "e1071f3a-8943-41dc-ab27-a78751645860");
		bean.setFileSize(2295l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 50, 42, 467));
		bean.setName("CreateModel_64x64.png");
		bean.setSpecification(rasterImageSpecification_133());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_134());
		bean.setMd5("e7073f27c421875d9cfceebecb8c7cd5");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_134() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e34022e8-89ec-4337-8428-70351ce732e1");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/50/d266b795-a30a-4b9c-88d7-5c9a12070e2d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_133() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "def3fd8e-7ba0-4e13-a1d0-dcdec9250185");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_129() {
		Resource bean = session().createRaw(Resource.T, "004d79a3-e1d3-42cf-a5c2-84a3f0938ff4");
		bean.setFileSize(961l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 50, 42, 463));
		bean.setName("CreateModel_32x32.png");
		bean.setSpecification(rasterImageSpecification_134());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_135());
		bean.setMd5("bd6d394799d0a66e32ee6622a3e32935");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_135() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5960b2b7-657f-4cb9-bfd7-5a56ce6d97be");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/50/61b3e7c9-a980-4dc3-87a9-f90c92fda811");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_134() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b21a22c6-fcf2-4301-bdf7-78c35d433fdf");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_130() {
		Resource bean = session().createRaw(Resource.T, "d46afa46-3f7a-49b6-b9fd-6bbcdae66a14");
		bean.setFileSize(407l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 50, 42, 445));
		bean.setName("CreateModel_16x16.png");
		bean.setSpecification(rasterImageSpecification_135());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_136());
		bean.setMd5("f5e5534342e90ffc5bbbc46d8db491fc");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_136() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "2738dfab-0973-4a70-9a3a-6b5d8e99fa54");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/50/e67e7986-c83b-4a83-939b-96cd042bc8b6");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_135() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1594da86-2d4a-4ae3-a7f2-8450526ad62e");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_68() {
		Folder bean = session().createRaw(Folder.T, "34aa8bc3-907d-422e-9815-5d59f7f2f5e4");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_134());
		bean.setIcon(adaptiveIcon_12());
		bean.setName("Redeploy");
		bean.setContent(templateServiceRequestAction_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_134() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a10fb9d2-3d14-40fa-a3a7-ebbf37df09f5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Redeploy")));
		return bean;
	}

	// Managed
	private Folder folder_69() {
		Folder bean = session().createRaw(Folder.T, "fce89f40-20f3-42dd-a775-eb8577de758f");
		bean.setDisplayName(localizedString_137());
		bean.setIcon(adaptiveIcon_35());
		bean.setName("Undeploy");
		bean.setContent(templateServiceRequestAction_18());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_18() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "87754fe6-9216-449c-93f2-d60fb18649dc");
		bean.setTemplate(template_33());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_135());
		bean.setInplaceContextCriterion(conjunctionCriterion_7());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_135() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c43752a7-19f7-499b-a495-ae5851763a4b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "UndeployTemplate")));
		return bean;
	}

	@Managed
	private ConjunctionCriterion conjunctionCriterion_7() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "d1f0046c-268a-4f9c-9441-6764ff092368");
		bean.setCriteria(Lists.list(entityCriterion_2(), negationCriterion_1(), valueConditionCriterion_6()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_6() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "7d2890e0-bb74-4905-9309-d271dc7a5321");
		bean.setPropertyPath("deploymentStatus");
		bean.setOperand(DeploymentStatus.undeployed);
		bean.setOperator(ComparisonOperator.notEqual);
		return bean;
	}

	// Managed
	private Template template_33() {
		Template bean = session().createRaw(Template.T, "1a94a89d-75cb-4d93-a173-436bdea114f4");
		bean.setName(localizedString_136());
		bean.setPrototype(undeployWithDeployables_1());
		bean.setScript(changeValueManipulation_46());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_136() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2c6b0fe4-60c8-4c24-9fcf-1eef473c4a89");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "UndeployTemplate")));
		return bean;
	}

	@Managed
	private UndeployWithDeployables undeployWithDeployables_1() {
		UndeployWithDeployables bean = session().createRaw(UndeployWithDeployables.T, "e7aeb241-c5b3-48f0-89da-a343584e18ec");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_46() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3bb5adad-da28-4a07-b475-8321d7b36290");
		bean.setOwner(localEntityProperty_45());
		bean.setNewValue(variable_44());
		return bean;
	}

	// Managed
	private Variable variable_44() {
		Variable bean = session().createRaw(Variable.T, "19fb1f7b-fec4-4268-a082-60e594b83ea4");
		bean.setTypeSignature("set<com.braintribe.model.deployment.Deployable>");
		bean.setName("Deployables");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_45() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "5529d0bc-ce20-4644-a7e8-22dd170ede7c");
		bean.setPropertyName("deployables");
		bean.setEntity(undeployWithDeployables_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_137() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "eaf6022d-e982-492f-a57d-96ec07ab16b6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Undeploy")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_35() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "2d0383b6-bb91-444d-a2e3-fcea921cfde3");
		bean.setName("Undeploy Icon");
		bean.setRepresentations(Sets.set(resource_132(), resource_131()));
		return bean;
	}

	// Managed
	private Resource resource_131() {
		Resource bean = session().createRaw(Resource.T, "ac6e0914-c989-485e-9bb2-62ffd94c17cd");
		bean.setFileSize(979l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 34, 28, 872));
		bean.setName("undeploy_32x32.png");
		bean.setSpecification(rasterImageSpecification_136());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_137());
		bean.setMd5("1fe68031dbb917bc862f0fd8a2f22481");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_137() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4df805c2-8dbd-4946-8f5d-6b2f3567da3a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/34/80553ab8-f6b5-4b71-af28-f6865c0de14a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_136() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e6087e3c-97f2-447d-ac7c-7478897cb780");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_132() {
		Resource bean = session().createRaw(Resource.T, "805cf216-7e19-4065-928c-c4b147889fa2");
		bean.setFileSize(828l);
		bean.setCreated(newGmtDate(2015, 4, 24, 21, 34, 28, 864));
		bean.setName("undeploy_16x16.png");
		bean.setSpecification(rasterImageSpecification_137());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_138());
		bean.setMd5("8081191974a0c07c20134453e95769e1");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_138() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bd67eac0-0bf8-4cdb-ae63-9270da666c07");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2423/34/e7cd3526-8f28-42ab-a0db-93e0effaee37");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_137() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8e1ac9a9-2a07-4678-b9aa-ce638b9842e0");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_70() {
		Folder bean = session().createRaw(Folder.T, "868ecde0-a63b-46ce-8b3f-64d50c7fee29");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_140());
		bean.setIcon(adaptiveIcon_34());
		bean.setName("New Model");
		bean.setContent(templateServiceRequestAction_19());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_19() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "1724a749-5c71-46b7-8d65-cbe8e5c9613b");
		bean.setTemplate(template_34());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_138());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_12());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_138() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d321cd72-9b3e-4c7b-86b6-4c9b8c1dc287");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New Model")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_12() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "4b48cada-a327-41bc-b76e-f8977ed409a9");
		bean.setTypeCondition(isAssignableTo_11());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_11() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "56bab579-00fa-4583-813e-79708a1348c7");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_34() {
		Template bean = session().createRaw(Template.T, "76b12d53-3e16-4348-830f-07733bd3503a");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_12()));
		bean.setName(localizedString_139());
		bean.setPrototype(createModel_1());
		bean.setTechnicalName("CreateModel");
		bean.setScript(compoundManipulation_15());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_12() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "a129797d-cfd4-4aa9-b9b0-13cf0545f3cc");
		bean.setMetaData(Sets.set(mandatory_1(), priority_5()));
		bean.setVariable(variable_45());
		return bean;
	}

	@Managed
	private Priority priority_5() {
		Priority bean = session().createRaw(Priority.T, "45d65eb2-cf7f-4956-94e3-3a9a4e166502");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	@Managed
	private Mandatory mandatory_1() {
		Mandatory bean = session().createRaw(Mandatory.T, "ed7a74a6-4f53-4d08-9559-ed6503ccb0e2");
		bean.setInherited(Boolean.TRUE);
		return bean;
	}

	@Managed
	private Variable variable_45() {
		Variable bean = session().createRaw(Variable.T, "1975d2cc-10c1-45fc-8d0b-ecc8ddaaed96");
		bean.setLocalizedName(localizedString_510());
		bean.setTypeSignature("string");
		bean.setName("name");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_139() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "00d62379-7c9d-4f0a-9d85-c3ae9a172e04");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New Model")));
		return bean;
	}

	@Managed
	private CreateModel createModel_1() {
		CreateModel bean = session().createRaw(CreateModel.T, "9bd0337f-0957-4b17-8605-bd4c24b73dbe");
		bean.setGroupId("custom.model");
		bean.setVersion("1.0");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_15() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "7e9f53b7-d734-48d7-85b2-9a245724c03d");
		bean.setCompoundManipulationList(
				Lists.list(changeValueManipulation_47(), changeValueManipulation_48(), changeValueManipulation_49(), changeValueManipulation_50()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_47() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "757bd023-2b80-4d06-bdb3-e322447d6bc0");
		bean.setOwner(localEntityProperty_46());
		bean.setNewValue(variable_46());
		return bean;
	}

	// Managed
	private Variable variable_46() {
		Variable bean = session().createRaw(Variable.T, "a2c17b4a-93b8-45a8-a54a-77597b95e22e");
		bean.setTypeSignature("list<com.braintribe.model.meta.GmMetaModel>");
		bean.setName("Dependencies");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_46() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "17a16a48-e751-4bae-937d-4f07b5719988");
		bean.setPropertyName("dependencies");
		bean.setEntity(createModel_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_48() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "375155f9-0f97-4e9d-855d-eb191fade988");
		bean.setOwner(localEntityProperty_47());
		bean.setNewValue(variable_47());
		return bean;
	}

	// Managed
	private Variable variable_47() {
		Variable bean = session().createRaw(Variable.T, "bb58153d-a48c-4f01-b175-2ab55276a4f6");
		bean.setTypeSignature("string");
		bean.setDefaultValue("custom.model");
		bean.setName("Group Id");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_47() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "5803eef4-eab3-46ea-a7c2-32853ec735ec");
		bean.setPropertyName("groupId");
		bean.setEntity(createModel_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_49() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1de73382-673b-4a8e-bd0f-f75ea7722d21");
		bean.setOwner(localEntityProperty_48());
		bean.setNewValue(variable_45());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_48() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b1c662fb-fb53-48cb-82e2-aa1d60c0bc7d");
		bean.setPropertyName("name");
		bean.setEntity(createModel_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_50() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "f2a19837-c7f9-4cf4-a733-5292302f1e56");
		bean.setOwner(localEntityProperty_49());
		bean.setNewValue(variable_48());
		return bean;
	}

	// Managed
	private Variable variable_48() {
		Variable bean = session().createRaw(Variable.T, "6b94da73-cad7-47fb-8e67-ccf73bddd54c");
		bean.setTypeSignature("string");
		bean.setDefaultValue("1.0");
		bean.setName("Version");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_49() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "c6e8f1be-c6f0-4280-bb95-64711b6a001c");
		bean.setPropertyName("version");
		bean.setEntity(createModel_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_140() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bd8a95bb-f161-479a-9f2b-5774e04a949a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New Model")));
		return bean;
	}

	// Managed
	private Folder folder_71() {
		Folder bean = session().createRaw(Folder.T, "9d5c1b3b-7561-4965-b2ac-85ffacec6a3a");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_143());
		bean.setIcon(adaptiveIcon_36());
		bean.setName("Test Connection");
		bean.setContent(templateServiceRequestAction_20());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_20() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "3614e051-f6ea-438f-b9b7-7e7cb83cb894");
		bean.setTemplate(template_35());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_141());
		bean.setInplaceContextCriterion(typeConditionCriterion_13());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_141() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4260a5c7-17dd-46bc-9c16-0b321e3f946d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_13() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "69a4b6c7-404f-446e-ab01-d41172b07e78");
		bean.setTypeCondition(isAssignableTo_12());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_12() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "34ad0a91-45c8-4ba2-a46d-af7f1df2a5d7");
		bean.setTypeSignature("com.braintribe.model.deployment.database.pool.DatabaseConnectionPool");
		return bean;
	}

	// Managed
	private Template template_35() {
		Template bean = session().createRaw(Template.T, "54508276-1823-4a87-93a2-fe7376c484c8");
		bean.setName(localizedString_142());
		bean.setPrototype(testDatabaseConnections_1());
		bean.setTechnicalName("TestDatabaseConnections");
		bean.setScript(changeValueManipulation_51());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_142() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d62dab74-7038-4723-92c1-847394557e59");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection Template")));
		return bean;
	}

	@Managed
	private TestDatabaseConnections testDatabaseConnections_1() {
		TestDatabaseConnections bean = session().createRaw(TestDatabaseConnections.T, "ed2d412d-0f85-448b-bfe1-d2a67bce010c");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_51() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "b5d43b47-3515-44c7-b497-994378dcf395");
		bean.setOwner(localEntityProperty_50());
		bean.setNewValue(variable_49());
		return bean;
	}

	// Managed
	private Variable variable_49() {
		Variable bean = session().createRaw(Variable.T, "42e2c1c4-aced-41f7-a90a-c0de17b2b1d2");
		bean.setTypeSignature("list<com.braintribe.model.deployment.database.pool.DatabaseConnectionPool>");
		bean.setName("Connection Pools");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_50() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "4f18c0da-fac2-494f-b1ca-b11153bb2b61");
		bean.setPropertyName("connectionPools");
		bean.setEntity(testDatabaseConnections_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_143() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8c7df542-1b34-4c26-a5a6-ec61f05d02b3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_36() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "6e5bdf7c-9a07-48bf-ba5d-dd851600ad14");
		bean.setName("Test Icon");
		bean.setRepresentations(Sets.set(resource_134(), resource_135(), resource_133()));
		return bean;
	}

	// Managed
	private Resource resource_133() {
		Resource bean = session().createRaw(Resource.T, "c5ad3ba9-ac52-4add-ba28-fd66c0c49725");
		bean.setFileSize(1399l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 51, 11, 992));
		bean.setName("ConnectionTest_64x64.png");
		bean.setSpecification(rasterImageSpecification_138());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_139());
		bean.setMd5("fd27633b4aac7a1d8b48cfc8fb0f2d8d");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_139() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e4df374f-7332-48ad-b938-686b6b874095");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/51/9e1ba6b2-96df-4286-86b9-b1f650573fe3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_138() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "760be6a4-30ef-4fde-892f-155f87fae560");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_134() {
		Resource bean = session().createRaw(Resource.T, "5443f25a-dcf7-4912-9f70-f348f4438c6f");
		bean.setFileSize(319l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 51, 11, 979));
		bean.setName("ConnectionTest_16x16.png");
		bean.setSpecification(rasterImageSpecification_139());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_140());
		bean.setMd5("081667818533f8106dba45f573f1abc4");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_140() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b9be428f-9310-4004-bb53-c4e24cf2c737");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/51/354a4bad-cc14-4b20-a2a7-9eebf5531b59");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_139() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "7b030d79-68a2-45f8-8e9d-44d6c0dba382");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_135() {
		Resource bean = session().createRaw(Resource.T, "1390a13e-7f14-429e-ab9e-a77a427d53b2");
		bean.setFileSize(611l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 51, 11, 989));
		bean.setName("ConnectionTest_32x32.png");
		bean.setSpecification(rasterImageSpecification_140());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_141());
		bean.setMd5("5c138a7f312e278e3af38fce68f492a8");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_141() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "eb3de948-bfa0-4328-97be-da80bfd3295d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/51/bcf1b605-49ab-4c4b-9418-54e50078c0f0");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_140() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "cffb4b17-0d7a-4645-9c14-f5944c227759");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_72() {
		Folder bean = session().createRaw(Folder.T, "9a7df307-6dfc-4f7e-bd50-61548f465123");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_146());
		bean.setIcon(adaptiveIcon_26());
		bean.setName("Synchronize DB Schema");
		bean.setContent(templateServiceRequestAction_21());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_21() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "4e7dd377-85db-492e-983f-f9ed59114324");
		bean.setTemplate(template_36());
		bean.setDisplayName(localizedString_144());
		bean.setInplaceContextCriterion(typeConditionCriterion_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_144() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0f50bb79-23c6-4dc5-9b4c-8feb01f3e23a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Synchronize DB Schema")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_14() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "2873b555-1d79-4daa-9af6-4bda4836358a");
		bean.setTypeCondition(isAssignableTo_13());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_13() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "36c05e26-d2b2-4ed0-b01c-edd1346828f3");
		bean.setTypeSignature("com.braintribe.model.deployment.database.pool.DatabaseConnectionPool");
		return bean;
	}

	// Managed
	private Template template_36() {
		Template bean = session().createRaw(Template.T, "d2ed3d8b-c641-480e-af02-28473f8b539d");
		bean.setName(localizedString_145());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.connection.SynchronizeDbSchema");
		bean.setPrototype(synchronizeDbSchema_1());
		bean.setTechnicalName("SynchronizeDbSchemaTemplate");
		bean.setScript(changeValueManipulation_52());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_145() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3272f77c-29fe-41da-a21d-22301481a439");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "SynchronizeDbSchema Template")));
		return bean;
	}

	@Managed
	private SynchronizeDbSchema synchronizeDbSchema_1() {
		SynchronizeDbSchema bean = session().createRaw(SynchronizeDbSchema.T, "abd7efa5-9048-4681-8507-24990bdefc5b");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_52() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a545ab91-c6db-4c14-bf9d-2643be020f13");
		bean.setOwner(localEntityProperty_51());
		bean.setNewValue(variable_50());
		return bean;
	}

	// Managed
	private Variable variable_50() {
		Variable bean = session().createRaw(Variable.T, "f05f8ef5-4e93-4218-b9a9-5621b87b83bc");
		bean.setTypeSignature("com.braintribe.model.deployment.database.pool.DatabaseConnectionPool");
		bean.setName("Connection Pool");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_51() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "42d7e8a4-ddeb-43d0-b126-772752668864");
		bean.setPropertyName("connectionPool");
		bean.setEntity(synchronizeDbSchema_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_146() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a364ae25-fb6a-44fc-ab5d-8f0e069b98ff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Synchronize DB Schema")));
		return bean;
	}

	// Managed
	private Folder folder_73() {
		Folder bean = session().createRaw(Folder.T, "f3306dee-0207-43ee-88ce-49377eaa454f");
		bean.setDisplayName(localizedString_147());
		bean.setIcon(adaptiveIcon_33());
		bean.setName("Garbage Collection (for HardwiredAccesses)");
		bean.setContent(templateServiceRequestAction_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_147() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1acc3e58-c32a-4984-8b02-1c89795f96d9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Garbage Collection")));
		return bean;
	}

	// Managed
	private Folder folder_74() {
		Folder bean = session().createRaw(Folder.T, "03140cf6-0cf5-4b15-b245-00e7399641b0");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_148());
		bean.setName("$openGmeForAccessInNewTab");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_148() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "abc091ef-132c-43f5-b82a-b81841d4a969");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	// Managed
	private Variable variable_51() {
		Variable bean = session().createRaw(Variable.T, "0cbd7b14-36f6-45bf-99f6-a76a3a566091");
		bean.setTypeSignature("string");
		bean.setName("actionName");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_22() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "8c5ee19c-bb77-4b7c-af8b-ddafa58667fa");
		bean.setTemplate(template_37());
		bean.setDisplayName(localizedString_150());
		bean.setInplaceContextCriterion(entityCriterion_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_150() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0014c416-0f1c-41d2-a565-eab5105a38b1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template Again")));
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_3() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "e1bb45a6-7893-4f1c-8bce-34b86e869d3a");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private Template template_37() {
		Template bean = session().createRaw(Template.T, "f29689b5-f0df-4533-bb6a-2394b5388ef5");
		bean.setName(localizedString_151());
		bean.setPrototype(createServiceRequestTemplate_3());
		bean.setScript(compoundManipulation_18());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_151() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1da10e58-b2c3-443f-b75a-e0bcca245b75");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template Again-Template")));
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_3() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "12df893f-67dc-4823-a7ef-4b76996215c0");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_18() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "46077b4c-4578-4b89-b213-08d169b7c21c");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_53(), changeValueManipulation_54(), changeValueManipulation_55(),
				changeValueManipulation_56(), changeValueManipulation_57()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_53() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4c70bd84-749c-4457-81f9-02da24bfcafc");
		bean.setOwner(localEntityProperty_53());
		bean.setNewValue(variable_51());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_53() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "59fde2fc-25d0-4950-a79e-eff7efe28a05");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_3());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_54() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "b5ceb8d8-d7a6-4727-bb0c-80f7630cc259");
		bean.setOwner(localEntityProperty_54());
		bean.setNewValue(variable_52());
		return bean;
	}

	// Managed
	private Variable variable_52() {
		Variable bean = session().createRaw(Variable.T, "78bef5f9-cdb5-4bf2-8243-b9cc94a951b1");
		bean.setTypeSignature("boolean");
		bean.setName("addToActionBar");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_54() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "f87185ad-ff0e-454a-ad37-e700dd96c3bf");
		bean.setPropertyName("addToActionBar");
		bean.setEntity(createServiceRequestTemplate_3());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_55() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "403cab99-4416-463e-83b8-ad40f8e91d13");
		bean.setOwner(localEntityProperty_55());
		bean.setNewValue(variable_53());
		return bean;
	}

	// Managed
	private Variable variable_53() {
		Variable bean = session().createRaw(Variable.T, "052a6738-2675-458b-9989-15baa4d4ba9e");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("criterionType");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_55() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "adfb4fc8-ce11-4bc6-bf96-6dd83963e9d8");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_3());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_56() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "e00e6835-4591-432c-b301-6d10b12ceeaf");
		bean.setOwner(localEntityProperty_56());
		bean.setNewValue(variable_54());
		return bean;
	}

	// Managed
	private Variable variable_54() {
		Variable bean = session().createRaw(Variable.T, "ae82454e-9d59-4dbe-ad60-3e2d17805c48");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("templateRequest");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_56() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "2a52bfb1-ea5f-4114-a150-ce2de986f0b3");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_3());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_57() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c0312ef4-78fe-4570-8033-a8d644c94041");
		bean.setOwner(localEntityProperty_57());
		bean.setNewValue(variable_55());
		return bean;
	}

	// Managed
	private Variable variable_55() {
		Variable bean = session().createRaw(Variable.T, "cd3786e9-b31d-40cc-8b2b-7212ec8de4b3");
		bean.setTypeSignature("set<string>");
		bean.setName("variableProperties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_57() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "761ad77e-dfb9-46dd-b893-d63d12024898");
		bean.setPropertyName("variableProperties");
		bean.setEntity(createServiceRequestTemplate_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_166() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "88a60fb6-da69-49d9-90b7-b87eb771ba54");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Cartridges")));
		return bean;
	}

	@Managed
	private Folder folder_78() {
		Folder bean = session().createRaw(Folder.T, "aa9eb80c-53f0-4ede-bbcf-c34f6dfee264");
		bean.setParent(folder_79());
		bean.setDisplayName(localizedString_180());
		bean.setIcon(adaptiveIcon_37());
		bean.setName("ExcelDataImports");
		bean.setContent(simpleQueryAction_7());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_7() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "721df6ef-9299-49ed-851e-bc38219eb23f");
		bean.setTypeSignature("com.braintribe.model.ieaction.excel.ExcelSheetImport");
		bean.setDisplayName(localizedString_179());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_179() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d4dee1f0-616e-4670-a1c0-09f622393473");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Excel Data Imports Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_180() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ec3dfadb-062a-424b-8c3e-f9c88c9b5a22");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Excel Data Imports")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_37() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "374f595b-6a71-4448-b080-f617baee4cfa");
		bean.setRepresentations(Sets.set(resource_139(), resource_140(), resource_141(), resource_142()));
		return bean;
	}

	// Managed
	private Resource resource_139() {
		Resource bean = session().createRaw(Resource.T, "3d27d220-9fc1-40d4-94f7-186fe8c11824");
		bean.setFileSize(690l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 52, 927));
		bean.setName("Excel_32x32.png");
		bean.setSpecification(rasterImageSpecification_154());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_151());
		bean.setMd5("46cd44b553b59e43a15df7c8368a7f46");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_151() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ee1f06cf-9297-4391-bb9f-c4a193555f0c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/a8f4f367-43b7-4cec-acfd-44f640b3b570");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_154() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "77f3ea51-dd3b-44cf-a8d2-ab89e5093499");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_140() {
		Resource bean = session().createRaw(Resource.T, "5d7d6c49-f943-493c-8513-b0298ce0aa40");
		bean.setFileSize(1426l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 52, 930));
		bean.setName("Excel_64x64.png");
		bean.setSpecification(rasterImageSpecification_155());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_152());
		bean.setMd5("b4668b9d386e284fa045e8d96c699a5e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_152() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bbaaf785-828d-4dd3-9308-a6bdc01c2fd0");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/2375a29d-4b7c-4717-a257-b374061ec44d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_155() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8072d81f-5510-4bf9-8b7a-501332ba5254");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_141() {
		Resource bean = session().createRaw(Resource.T, "59e7fda4-42b0-4ef2-ac8c-372c17d3cbd7");
		bean.setFileSize(381l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 52, 921));
		bean.setName("Excel_16x16.png");
		bean.setSpecification(rasterImageSpecification_156());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_153());
		bean.setMd5("12fe095bb76e50bb1119872e5b428240");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_153() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cc5f4978-269d-41cd-a00d-2d3e536fd884");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/73e87f4f-460d-4d55-8322-667b92f61abf");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_156() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e04609ec-63b8-4c58-9535-f076b540b0d4");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_142() {
		Resource bean = session().createRaw(Resource.T, "ea23ab6e-768f-401d-95de-3153941c63be");
		bean.setFileSize(550l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 32, 52, 925));
		bean.setName("Excel_24x24.png");
		bean.setSpecification(rasterImageSpecification_157());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_154());
		bean.setMd5("efc9b09a71d245360634a5378aba0a92");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_154() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c99b663d-f8e4-4a1f-bdb7-b6f316e9f1e1");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/32/e6e83253-53f5-48c8-b379-90a9ebab416a");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_157() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8c996fba-a9a9-4c56-ac56-ea59423701b5");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	@Managed
	private Folder folder_79() {
		Folder bean = session().createRaw(Folder.T, "3b75ef76-5187-4366-8a2a-e94899712894");
		bean.setSubFolders(Lists.list(folder_80(), folder_81(), folder_78(), folder_82()));
		bean.setDisplayName(localizedString_181());
		bean.setName("ActionRequests");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_181() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bc3f5278-f262-4f63-b1e2-fc6753e34e16");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actions")));
		return bean;
	}

	@Managed
	private Folder folder_80() {
		Folder bean = session().createRaw(Folder.T, "6f64ce8d-10f7-41b2-8da4-f3fac9a3fdf6");
		bean.setParent(folder_79());
		bean.setDisplayName(localizedString_183());
		bean.setIcon(adaptiveIcon_38());
		bean.setName("ModelImports");
		bean.setContent(simpleQueryAction_8());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_8() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "0ab8ffb4-0642-4e09-a938-4bcf7c26bbb7");
		bean.setTypeSignature("com.braintribe.model.cortex.action.ImportTypeFromDbSchema");
		bean.setDisplayName(localizedString_182());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_182() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e7513c49-8ce3-4742-843a-db9b481bf5bc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Imports Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_183() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d501f419-4716-4e4f-8432-b548238b7319");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Imports")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_38() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "9984789e-cb97-4614-bc02-23d8c8929e69");
		bean.setRepresentations(Sets.set(resource_145(), resource_144(), resource_146(), resource_143()));
		return bean;
	}

	// Managed
	private Resource resource_143() {
		Resource bean = session().createRaw(Resource.T, "18d407c8-dea8-452a-a09e-1cf00e7982b8");
		bean.setFileSize(20071l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 23, 10, 196));
		bean.setName("tf controlcenter 16x16_06.png");
		bean.setSpecification(rasterImageSpecification_158());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_155());
		bean.setMd5("410623a01dd5fa48e86efd06bb03d029");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_155() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d54324c0-579e-48ba-b3db-bbb118b434b3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/23/ed1a9c28-4b2f-4256-8e67-59e7ccb32211");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_158() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e0fa926f-20ba-43fd-abcc-255e603ff966");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_144() {
		Resource bean = session().createRaw(Resource.T, "bb4ff254-8240-4080-912f-a416b8c8d896");
		bean.setFileSize(20211l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 18, 56, 809));
		bean.setName("tf controlcenter 24x24_06.png");
		bean.setSpecification(rasterImageSpecification_159());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_156());
		bean.setMd5("0de756c7778c4a6f6474bea653854489");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_156() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "08258db6-901b-462c-9353-120b41580fe8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/18/f2b1662e-9ac8-4646-91de-e6ef7b631494");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_159() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "17a52f35-b1ee-426d-ac61-6212a72c0697");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_145() {
		Resource bean = session().createRaw(Resource.T, "898add1d-8265-4c13-83fa-cca41639e29e");
		bean.setFileSize(20189l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 47, 2, 646));
		bean.setName("tf controlcenter 32x32_06.png");
		bean.setSpecification(rasterImageSpecification_160());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_157());
		bean.setMd5("1dd0f3ab0788fa5a9d1d0c9f37947878");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_157() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c8f45136-c6f0-4790-ae55-ad0e166fd48f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/47/eaa91040-d53b-4841-b25a-166c106b2325");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_160() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1dcb2f2d-32c3-45a7-935d-5bd35427f779");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_146() {
		Resource bean = session().createRaw(Resource.T, "955b3a4f-9738-471b-a899-8aa4e7a5bb26");
		bean.setFileSize(21048l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 46, 56, 950));
		bean.setName("tf controlcenter 64x64_06.png");
		bean.setSpecification(rasterImageSpecification_161());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_158());
		bean.setMd5("30eb0f432e816e417f720fd151773b21");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_158() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7d967d3e-e590-42ed-ac87-f61c37786868");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/46/dc1d2ec0-6218-4368-8f59-9beded680c18");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_161() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c52f0f53-7091-4360-9334-8984b7b6ff36");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_81() {
		Folder bean = session().createRaw(Folder.T, "3b12de88-7531-4e27-bc91-2529b522e66b");
		bean.setParent(folder_79());
		bean.setDisplayName(localizedString_185());
		bean.setIcon(adaptiveIcon_39());
		bean.setName("ConnectionTests");
		bean.setContent(simpleQueryAction_9());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_9() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "7c7042e8-a359-40ba-b347-95f857b8383b");
		bean.setTypeSignature("com.braintribe.model.cortex.action.TestJdbcConnector");
		bean.setDisplayName(localizedString_184());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_184() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2ca61dc4-f55e-40f9-97db-bd46d85c5913");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connection Tests Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_185() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2e760386-2efd-49ed-9289-3cd296f599ce");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connection Tests")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_39() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "2c579870-e2ca-48a7-aa25-6f7c288ea96f");
		bean.setRepresentations(Sets.set(resource_148(), resource_149(), resource_150(), resource_147()));
		return bean;
	}

	// Managed
	private Resource resource_147() {
		Resource bean = session().createRaw(Resource.T, "23b8de6c-5dec-4f40-bec9-4bbe28b1b585");
		bean.setFileSize(20215l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 47, 2, 650));
		bean.setName("tf controlcenter 32x32_05.png");
		bean.setSpecification(rasterImageSpecification_162());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_159());
		bean.setMd5("a0a23197a0e31f044b8af256834da89a");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_159() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d8c364f5-6ed5-496e-b3df-9b16086ce93d");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/47/b6142051-2835-4e17-aa3a-c1b7237bbb21");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_162() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c37925c6-7a0f-40f9-ad16-a46243c5eea2");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_148() {
		Resource bean = session().createRaw(Resource.T, "46c5632a-b36d-4ad5-a2c2-ce868cd10488");
		bean.setFileSize(20017l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 23, 10, 192));
		bean.setName("tf controlcenter 16x16_05.png");
		bean.setSpecification(rasterImageSpecification_163());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_160());
		bean.setMd5("243e8a1dfc48df4c8831cc169743decf");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_160() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "82a8ffd7-7fe3-44cb-a2f2-255cfca0c8d8");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/23/3681f17d-04c4-47bc-9a39-46b8d349b74f");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_163() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f39ad255-d6df-42d8-afb6-4b9f9f185cd4");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_149() {
		Resource bean = session().createRaw(Resource.T, "6db8c308-5020-4112-b156-8e54fc26ebdd");
		bean.setFileSize(20813l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 46, 56, 956));
		bean.setName("tf controlcenter 64x64_05.png");
		bean.setSpecification(rasterImageSpecification_164());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_161());
		bean.setMd5("4f063ed4c9d3e327516d62b1153cc1b2");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_161() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "dd0012f0-cef2-4580-aba8-dad3ab583170");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/46/78340e1a-3636-48b4-b3e4-b276184dd4e3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_164() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "125793fb-e998-41fd-8511-7c7f8e0252eb");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_150() {
		Resource bean = session().createRaw(Resource.T, "341e9661-260e-4500-afeb-433abe941d22");
		bean.setFileSize(20102l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 18, 56, 804));
		bean.setName("tf controlcenter 24x24_05.png");
		bean.setSpecification(rasterImageSpecification_165());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_162());
		bean.setMd5("54170a45eefbbeca4f2b6289aba14e30");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_162() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "63f94e83-57ca-4041-adf2-c13a72743fe3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/18/cb5099fd-91be-4903-8bd5-f915458fdc19");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_165() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "93319f93-6475-4b28-ac58-3ad6e90cdf36");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Folder folder_82() {
		Folder bean = session().createRaw(Folder.T, "3e40df0c-c9d2-4e07-9717-a2dbfc4c1604");
		bean.setParent(folder_79());
		bean.setDisplayName(localizedString_187());
		bean.setIcon(adaptiveIcon_40());
		bean.setName("Deployments");
		bean.setContent(simpleQueryAction_10());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_10() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "95469471-5819-4526-a541-0ba56a503f69");
		bean.setTypeSignature("com.braintribe.model.cortex.action.Deploy");
		bean.setDisplayName(localizedString_186());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_186() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d1f121f9-5d47-4419-a6d7-dcd56bbd4142");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deployments Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_187() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8de495bc-8a3c-4b25-9a0d-9037f97dffa0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deployments")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_40() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "5c8d0352-00ef-492f-a026-1c828f0ecad0");
		bean.setRepresentations(Sets.set(resource_153(), resource_151(), resource_152(), resource_154()));
		return bean;
	}

	// Managed
	private Resource resource_151() {
		Resource bean = session().createRaw(Resource.T, "c6101860-3e9d-436f-ac44-9454e9d32d75");
		bean.setFileSize(19823l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 18, 56, 815));
		bean.setName("tf controlcenter 24x24_07.png");
		bean.setSpecification(rasterImageSpecification_166());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_163());
		bean.setMd5("54e0f7995b5e50055cf2dc1b2f2d9ecf");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_163() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e20a7b6b-4805-4251-ae45-450eea9c917a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/18/69db12af-ab47-4f38-86f3-29e49a8417b9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_166() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c00fcb3d-a6fd-4e8a-8890-5ba974359785");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_152() {
		Resource bean = session().createRaw(Resource.T, "68ad795f-c633-4047-8f4a-cf57007f8f0e");
		bean.setFileSize(19873l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 47, 2, 642));
		bean.setName("tf controlcenter 32x32_07.png");
		bean.setSpecification(rasterImageSpecification_167());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_164());
		bean.setMd5("d5ed27001db048eb331b4e3274f8c5a4");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_164() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "500576f8-dd3c-4d68-a23c-05570db5064c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/47/5c4d5ee6-18ff-4b02-aab8-f423f8cd3abf");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_167() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "61e89369-bc90-4460-b860-2066895c2af6");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_153() {
		Resource bean = session().createRaw(Resource.T, "e1047f33-98d8-4382-a9c5-7ae1d0826040");
		bean.setFileSize(19864l);
		bean.setCreated(newGmtDate(2014, 6, 22, 15, 46, 56, 942));
		bean.setName("tf controlcenter 64x64_07.png");
		bean.setSpecification(rasterImageSpecification_168());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_165());
		bean.setMd5("1ae8bcb00691cd938034a28c76858f81");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_165() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b8f74707-44c6-41f7-8890-31d5f0db7a72");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2217/46/cd55fcd5-70a5-407d-b15b-01d88cded447");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_168() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "eabfa022-ecd7-4f77-bbd5-7a936dc08ced");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_154() {
		Resource bean = session().createRaw(Resource.T, "0cd43866-8cf8-4929-9eda-ae96a7938952");
		bean.setFileSize(19719l);
		bean.setCreated(newGmtDate(2014, 6, 22, 13, 23, 10, 200));
		bean.setName("tf controlcenter 16x16_07.png");
		bean.setSpecification(rasterImageSpecification_169());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_166());
		bean.setMd5("93a6fc326c66a82151715e4c74ac2e45");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_166() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "772dedee-0b4e-417e-94f5-c280aecbe103");
		bean.setModuleName(currentModuleName());
		bean.setPath("1407/2215/23/3c82b71b-22bf-49f0-aaac-08b751f83c73");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_169() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b8e42e66-aae4-4dff-ba17-41f80963f970");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Variable variable_77() {
		Variable bean = session().createRaw(Variable.T, "f184da07-1550-4251-b644-e2208f31ef23");
		bean.setTypeSignature("com.braintribe.model.deploymentapi.request.DeploymentMode");
		bean.setDefaultValue(DeploymentMode.bootstrappingOnly);
		bean.setName("Mode");
		bean.setDescription(localizedString_188());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_188() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e899f5ce-a1f5-45e4-97e3-3eb2a613a3c9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "v_mode_0")));
		return bean;
	}

	// Managed
	private Folder folder_83() {
		Folder bean = session().createRaw(Folder.T, "0ec2aabd-c81d-46bf-a9a1-4f3defab1df1");
		bean.setParent(folder_17());
		bean.setDisplayName(localizedString_191());
		bean.setIcon(adaptiveIcon_41());
		bean.setName("SystemAccesses");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_191() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c1e4ce70-be74-4fc6-9f88-bcdd9989dab4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System Accesses")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_41() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "5fbb49e8-e6b3-4d00-817e-ba366ca7cd27");
		return bean;
	}

	// Managed
	private Folder folder_84() {
		Folder bean = session().createRaw(Folder.T, "8664a017-cb61-4a8c-a3f5-49f4b04dde69");
		bean.setParent(folder_14());
		bean.setDisplayName(localizedString_193());
		bean.setName("adfs");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_193() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "79bf17ab-06f8-4c4d-8d8f-9fbe8647b1b0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "adfs")));
		return bean;
	}

	// Managed
	private Folder folder_85() {
		Folder bean = session().createRaw(Folder.T, "41126a88-268d-456b-9a9f-ff33f7dc954a");
		bean.setDisplayName(localizedString_194());
		bean.setName("DeployviaService");
		bean.setContent(templateServiceRequestAction_24());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_24() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "b628808f-3ada-4596-bdf1-4ed98ec32a90");
		bean.setTemplate(template_43());
		bean.setDisplayName(localizedString_63());
		return bean;
	}

	// Managed
	private Template template_43() {
		Template bean = session().createRaw(Template.T, "cf88750f-a52f-4e55-9fda-c76e81b624ba");
		bean.setPrototype(deploy_1());
		bean.setScript(changeValueManipulation_86());
		return bean;
	}

	@Managed
	private Deploy deploy_1() {
		Deploy bean = session().createRaw(Deploy.T, "443638cd-0080-4a79-a873-42d99c78aa6b");
		bean.setMode(DeploymentMode.bootstrappingOnly);
		bean.setExternalIds(Sets.set("cortex"));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_86() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "129b1c85-916e-460b-85f3-33f5304e9c36");
		bean.setOwner(localEntityProperty_77());
		bean.setNewValue(variable_77());
		return bean;
	}

	@Managed
	private LocalEntityProperty localEntityProperty_77() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "1f980108-0a31-4e28-b36a-c08c04102a4e");
		bean.setPropertyName("mode");
		bean.setEntity(deploy_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_194() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "651fe5ba-57a7-4d6b-8f59-5f21e0a145e1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy via Service")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_171() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cbc3360d-9fef-4713-914f-a869816f6fe3");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/21/37e14012-f20c-4d9e-87cf-8beb20a1a902");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_97() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "9ee933e5-1772-44b2-b3a0-c84017475595");
		bean.setOwner(localEntityProperty_82());
		bean.setNewValue(variable_87());
		return bean;
	}

	// Managed
	private Variable variable_87() {
		Variable bean = session().createRaw(Variable.T, "83733192-0e0a-4e6c-beea-caf690bc83a1");
		bean.setTypeSignature("boolean");
		bean.setName("addToActionBar");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_82() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "88ef9545-d88a-41ce-9f2a-f55f12288217");
		bean.setPropertyName("addToActionBar");
		bean.setEntity(createServiceRequestTemplate_4());
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_4() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "ed3fafe4-3c6c-4128-8bc8-9f2a061e801a");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_84() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "60344262-1492-4d17-93cb-d19973baa2da");
		bean.setPropertyName("variableProperties");
		bean.setEntity(createServiceRequestTemplate_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_203() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "01f757a8-0ddf-41eb-9e6f-627ab25e937a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template")));
		return bean;
	}

	// Managed
	private Variable variable_92() {
		Variable bean = session().createRaw(Variable.T, "92741986-72f8-4413-b3ae-b239691cd028");
		bean.setTypeSignature("boolean");
		bean.setName("addToActionBar");
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "6c87b267-e7ff-4d0d-8c27-b40aa2622a5d");
		bean.setFolders(Lists.list(folder_13(), folder_14(), folder_15(), folder_25(), folder_26(), folder_27(), folder_28(), folder_29(),
				folder_32(), folder_167()));
		bean.setDisplayName(localizedString_205());
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_205() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ed50ddec-0247-42c1-a702-eb96f0ccb8b6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Folders")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_96() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "663f3723-2c7f-4b15-b8bb-0befc69ef69b");
		bean.setPropertyName("resetExisting");
		return bean;
	}

	// Managed
	private Template template_51() {
		Template bean = session().createRaw(Template.T, "6d04d97d-ab52-4293-ae8c-4ea608964206");
		bean.setName(localizedString_222());
		bean.setPrototype(createServiceRequestTemplate_5());
		bean.setScript(compoundManipulation_31());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_222() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c1454264-90ee-4307-ba59-0ef390008d47");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template-Template")));
		return bean;
	}

	// Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_5() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "d8a0d435-8e7d-4035-a9b2-74221797a2ee");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_31() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "c6da7592-c6f0-4f18-96b4-649435a087a9");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_14(), changeValueManipulation_118(), changeValueManipulation_119(),
				changeValueManipulation_120(), changeValueManipulation_121()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_118() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "fd1731fc-7ad7-4aea-a53d-70f11e2615eb");
		bean.setOwner(localEntityProperty_102());
		bean.setNewValue(variable_92());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_102() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b58721f8-e0c0-4b1f-bc71-9fb44c01cf4c");
		bean.setPropertyName("addToActionBar");
		bean.setEntity(createServiceRequestTemplate_2());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_119() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4fb7b74a-7c8d-430b-96fd-5666e0e3546e");
		bean.setOwner(localEntityProperty_103());
		bean.setNewValue(variable_103());
		return bean;
	}

	// Managed
	private Variable variable_103() {
		Variable bean = session().createRaw(Variable.T, "cad406a8-630f-4224-8ffe-494dda1772a8");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("criterionType");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_103() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6e67f1dc-14b6-4ba0-b43a-349435ad985d");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_2());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_120() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a1dc04e2-23ac-4d09-b903-53b60cb3e608");
		bean.setOwner(localEntityProperty_104());
		bean.setNewValue(variable_104());
		return bean;
	}

	// Managed
	private Variable variable_104() {
		Variable bean = session().createRaw(Variable.T, "df4573b5-88db-49b4-9f89-ccd53325b509");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("templateRequest");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_104() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a6b8fa21-f641-4629-8f83-46e86026a2cb");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_2());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_121() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "e13dfd20-43ee-40c6-acd2-44e86aa97677");
		bean.setOwner(localEntityProperty_84());
		bean.setNewValue(variable_105());
		return bean;
	}

	// Managed
	private Variable variable_105() {
		Variable bean = session().createRaw(Variable.T, "3d9d0aa1-1d6a-41a5-924a-dbd8a8ed3465");
		bean.setTypeSignature("set<string>");
		bean.setName("variableProperties");
		return bean;
	}

	// Managed
	private Folder folder_87() {
		Folder bean = session().createRaw(Folder.T, "eff89914-638d-4640-b509-8303560d337b");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_224());
		bean.setName("$recordTemplateScript");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_224() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "469bd63f-a2f3-45bb-81e2-42d879cdd877");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Record")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_181() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0acd30ce-46c7-4283-925f-ffae2665f0d5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/1210/07/4fd124fb-4593-411f-954a-6b7e6b6aae04");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_48() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "802bced2-a6a9-4c0e-bb00-858438c59214");
		bean.setPropertyName("cartridge.externalId");
		return bean;
	}

	// Managed
	private Template template_55() {
		Template bean = session().createRaw(Template.T, "4b1bd01e-0f48-46cb-bacd-c1cc86615268");
		bean.setName(localizedString_239());
		bean.setScript(compoundManipulation_37());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_239() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7a1809f5-ce64-4e69-9809-dd48fbaa0454");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ensure Workbench-Template")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_37() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "a1d7e23c-5cc5-4985-813a-7113c22f60a1");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_151(), changeValueManipulation_152(), changeValueManipulation_153(),
				changeValueManipulation_154(), changeValueManipulation_155()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_151() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "2948f181-1acf-471b-80ad-e2c5a1f03638");
		bean.setOwner(localEntityProperty_121());
		bean.setNewValue(variable_122());
		return bean;
	}

	// Managed
	private Variable variable_122() {
		Variable bean = session().createRaw(Variable.T, "7aeaf0e1-240e-41c4-88fc-39bfdd5098f8");
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setName("access");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_121() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "8208f26b-f637-4132-b3fc-7bb131714598");
		bean.setPropertyName("access");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_152() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c789cfb2-4289-4c91-a356-f15249205b1a");
		bean.setOwner(localEntityProperty_122());
		bean.setNewValue(variable_123());
		return bean;
	}

	// Managed
	private Variable variable_123() {
		Variable bean = session().createRaw(Variable.T, "86412c7c-f97a-48e8-974e-295013ad6fb7");
		bean.setTypeSignature("string");
		bean.setName("domainId");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_122() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9062badf-9d2e-41a6-85f2-dc6f087b9a90");
		bean.setPropertyName("domainId");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_153() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "8806b5e9-3853-4467-bef3-3aa6a7187413");
		bean.setOwner(localEntityProperty_123());
		bean.setNewValue(variable_124());
		return bean;
	}

	// Managed
	private Variable variable_124() {
		Variable bean = session().createRaw(Variable.T, "531b7ba7-578d-4fb9-9dae-e8fe9cc0aad0");
		bean.setTypeSignature("boolean");
		bean.setName("redeploy");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_123() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a96879e5-3a94-4543-b05f-f8a5ca300884");
		bean.setPropertyName("redeploy");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_154() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "7305db8a-cb12-4e2c-8070-29fa7ceaee51");
		bean.setOwner(localEntityProperty_96());
		bean.setNewValue(variable_125());
		return bean;
	}

	// Managed
	private Variable variable_125() {
		Variable bean = session().createRaw(Variable.T, "0b02df44-dd25-40bd-a004-cd4ff01aa3a5");
		bean.setTypeSignature("boolean");
		bean.setName("resetExisting");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_155() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "ccc6c054-8819-4a9f-af3d-ba111fa34aa7");
		bean.setOwner(localEntityProperty_124());
		bean.setNewValue(variable_126());
		return bean;
	}

	// Managed
	private Variable variable_126() {
		Variable bean = session().createRaw(Variable.T, "d5e71ea0-2ede-4fbf-a454-fbd8516cb554");
		bean.setTypeSignature("string");
		bean.setName("sessionId");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_124() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "ed441950-7c8a-4718-95f7-23eb129389da");
		bean.setPropertyName("sessionId");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_185() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "27b87210-a315-4734-9746-9edea44c5d43");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/57/75fd6a82-eb8f-43bb-921f-3afe5f7f5d10");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_246() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4913da85-76ab-4573-a98f-b1de0fe9c8d0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template New")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_26() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "d581d74f-4f2f-4dc2-888f-a4b8265aee84");
		bean.setTemplate(template_60());
		bean.setDisplayName(localizedString_247());
		bean.setInplaceContextCriterion(entityCriterion_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_247() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0c2414c7-a133-4e23-aece-6a593791b3d4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template Newest")));
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_14() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "4603c05d-7197-4783-ae12-2d028f4114e6");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private Template template_60() {
		Template bean = session().createRaw(Template.T, "163db555-e3c9-40ca-b54b-d71224998fe9");
		bean.setName(localizedString_248());
		bean.setPrototype(createServiceRequestTemplate_1());
		bean.setScript(compoundManipulation_41());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_248() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "94613588-e70b-4807-879a-8c3fc20abc8b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template Newest-Template")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_41() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "0a6c8670-6a30-4d69-9809-2c1f9c5f9d6c");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_178(), changeValueManipulation_179(), changeValueManipulation_180(),
				changeValueManipulation_181(), changeValueManipulation_182()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_178() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "14d1a709-6073-4b60-bd99-1adb4c071dff");
		bean.setOwner(localEntityProperty_133());
		bean.setNewValue(variable_147());
		return bean;
	}

	// Managed
	private Variable variable_147() {
		Variable bean = session().createRaw(Variable.T, "b4716f7c-424d-4c95-95ab-49de778ba950");
		bean.setTypeSignature("string");
		bean.setName("actionName");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_133() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "fdd869c0-db2e-474d-8854-30a0a47b8d0f");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_8());
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_8() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "29dcd159-393d-472b-8295-cde897e8c6d6");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_179() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3a175edf-3d03-4c08-bd84-418a4e88d9c4");
		bean.setOwner(localEntityProperty_134());
		bean.setNewValue(variable_148());
		return bean;
	}

	// Managed
	private Variable variable_148() {
		Variable bean = session().createRaw(Variable.T, "5249253c-e460-4fa5-a197-2dd8e4cdc1c4");
		bean.setTypeSignature("boolean");
		bean.setName("addToActionBar");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_134() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a339531d-87e6-4558-bc0c-111b07f5c77b");
		bean.setPropertyName("addToActionBar");
		bean.setEntity(createServiceRequestTemplate_8());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_180() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c447acae-823c-42c2-a94f-968fd55e9a35");
		bean.setOwner(localEntityProperty_135());
		bean.setNewValue(variable_149());
		return bean;
	}

	// Managed
	private Variable variable_149() {
		Variable bean = session().createRaw(Variable.T, "407d0507-8ce4-4d00-8c05-ecf51b2a06e7");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("criterionType");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_135() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "82257036-8fb8-428f-9756-0a2dd94498da");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_8());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_181() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "6860d4d4-2cb8-4d60-8d0c-1568f121ce9a");
		bean.setOwner(localEntityProperty_136());
		bean.setNewValue(variable_150());
		return bean;
	}

	// Managed
	private Variable variable_150() {
		Variable bean = session().createRaw(Variable.T, "3e3fb878-da0e-4eee-a167-12b53b10f33e");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("templateRequest");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_136() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "dd2574d6-2b2e-4f8d-86f3-2aea8559ded3");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_8());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_182() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "94459781-54f5-4049-be52-8a6e4466a00c");
		bean.setOwner(localEntityProperty_137());
		bean.setNewValue(variable_151());
		return bean;
	}

	// Managed
	private Variable variable_151() {
		Variable bean = session().createRaw(Variable.T, "70fdd8c4-fd80-4982-9bb8-223ada25330e");
		bean.setTypeSignature("set<string>");
		bean.setName("variableProperties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_137() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d18522b9-4d77-4341-b359-f0592fe8c75e");
		bean.setPropertyName("variableProperties");
		bean.setEntity(createServiceRequestTemplate_8());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_183() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a65f1856-34cd-4bfc-a4b5-7549efc68ec8");
		bean.setOwner(localEntityProperty_138());
		bean.setNewValue(variable_152());
		return bean;
	}

	// Managed
	private Variable variable_152() {
		Variable bean = session().createRaw(Variable.T, "191031c5-a30a-4c9e-b82e-8fb0c69ee73f");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("criterionType");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_138() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "cafeb783-0719-4ce1-ac1a-bc75b6015b5d");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_9());
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_9() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "45f51e85-0eef-49d4-8b7e-71872a50cfa5");
		return bean;
	}

	// Managed
	private Template template_61() {
		Template bean = session().createRaw(Template.T, "40a7147e-e3ea-4c52-a1f4-b64d94dfdc06");
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_20());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_20() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "d85ee1e1-f1a3-49ad-9972-fff2b4942346");
		bean.setRestriction(restriction_18());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.Worker");
		return bean;
	}

	// Managed
	private Restriction restriction_18() {
		Restriction bean = session().createRaw(Restriction.T, "85446e61-edb6-4ccb-9221-917e477b7ea3");
		bean.setCondition(valueComparison_29());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_29() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "90cf88e2-4f63-4d1f-8b0e-b8eb7a4adf12");
		bean.setLeftOperand(propertyOperand_48());
		bean.setRightOperand(propertyOperand_49());
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_49() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "c7cdac70-226f-42db-83ee-9be05eef6125");
		bean.setPropertyName("bla");
		return bean;
	}



	// Managed
	private Folder folder_90() {
		Folder bean = session().createRaw(Folder.T, "0b171da1-f9f8-4e48-8758-283b56bd7a26");
		bean.setParent(folder_14());
		bean.setDisplayName(localizedString_252());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_252() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "638568d9-d8c0-469e-a001-6ee42b110408");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Query Template")));
		return bean;
	}

	// Managed
	private Folder folder_91() {
		Folder bean = session().createRaw(Folder.T, "ba5d5f45-f40a-40b4-92d8-27e854b6db4e");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_255());
		bean.setIcon(adaptiveIcon_36());
		bean.setName("Test Connection");
		bean.setContent(templateServiceRequestAction_27());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_27() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "bfca6470-2d24-4498-8703-628cffbcbbeb");
		bean.setTemplate(template_62());
		bean.setDisplayName(localizedString_253());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_253() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8bbf451b-2a20-47d5-88e0-669313d2eaea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection")));
		return bean;
	}

	// Managed
	private Template template_62() {
		Template bean = session().createRaw(Template.T, "05ddabea-9edc-4c3e-b1f4-bba6b859ade7");
		bean.setName(localizedString_254());
		bean.setPrototype(testDatabaseConnection_1());
		bean.setTechnicalName("TestDatabaseConnection");
		bean.setScript(changeValueManipulation_187());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_254() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1ca57f95-f969-4a96-a847-fbdfce785965");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection Template")));
		return bean;
	}

	@Managed
	private TestDatabaseConnection testDatabaseConnection_1() {
		TestDatabaseConnection bean = session().createRaw(TestDatabaseConnection.T, "fc5eca3d-f3b1-4173-8755-2de450613379");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_187() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "60f9d576-5b3c-45cd-9502-699fb3591031");
		bean.setOwner(localEntityProperty_141());
		bean.setNewValue(variable_156());
		return bean;
	}

	// Managed
	private Variable variable_156() {
		Variable bean = session().createRaw(Variable.T, "e3f5f6c6-ab1d-40ae-b7d1-295947f87e82");
		bean.setTypeSignature("com.braintribe.model.deployment.database.pool.DatabaseConnectionPool");
		bean.setName("Connection Pool");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_141() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "348023ec-8ab1-4e4d-b605-527272a83080");
		bean.setPropertyName("connectionPool");
		bean.setEntity(testDatabaseConnection_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_255() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "17c89d43-8137-4dd1-92f6-01e5f0e5f979");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Test Connection")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_256() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8b3cd386-ded2-46cf-84b2-9419613b2349");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template")));
		return bean;
	}

	@Managed
	private Resource resource_164() {
		Resource bean = session().createRaw(Resource.T, "24be5065-a495-4648-9a19-521250972f75");
		bean.setFileSize(917l);
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 21, 58, 812));
		bean.setName("export_32x32.png");
		bean.setSpecification(rasterImageSpecification_196());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_171());
		bean.setMd5("feba84f79a5fa83b89403d961cbce566");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_196() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d46593e6-54b6-42ce-bb7c-cdf95cd27b38");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_200() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c94d1724-198f-4223-9b04-8621b82c6719");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/52/6e76b021-0004-48a3-8cfd-952199ecfda9");
		return bean;
	}

	@Managed
	private Folder folder_96() {
		Folder bean = session().createRaw(Folder.T, "ada520b0-f9cd-48b9-8c4b-e222830a7a4b");
		bean.setParent(folder_97());
		bean.setDisplayName(localizedString_273());
		bean.setIcon(adaptiveIcon_44());
		bean.setName("$condenseEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_273() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "70fc98f6-be73-4737-8936-92239dde9903");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_44() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "37e78b57-6ed7-462f-a4ed-a9895927a06f");
		bean.setName("Condensation Icon");
		bean.setRepresentations(Sets.set(resource_173(), resource_5(), resource_174(), resource_172()));
		return bean;
	}

	// Managed
	private Resource resource_172() {
		Resource bean = session().createRaw(Resource.T, "64915057-6e6b-47ac-9f99-9416da2627b2");
		bean.setFileSize(677l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 52, 25, 353));
		bean.setName("Condensation_24x24.png");
		bean.setSpecification(rasterImageSpecification_204());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_200());
		bean.setMd5("cecbf3f3e30ac5da952053cf06aed0cb");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_204() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d51a4cb5-fc27-45a8-b645-aa8de55d38c1");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_173() {
		Resource bean = session().createRaw(Resource.T, "f8700726-61f0-440c-9c15-f672e1c15973");
		bean.setFileSize(488l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 52, 25, 349));
		bean.setName("Condensation_16x16.png");
		bean.setSpecification(rasterImageSpecification_205());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_206());
		bean.setMd5("81c39f77d3e143fc9544fede3d7054c2");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_206() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5c2dc3db-98c7-4d7a-abe7-51229fc3539e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/52/158f7421-bfc8-43db-a841-3f626d482006");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_205() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e9880d08-6dae-404b-8df1-929086f56c98");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_174() {
		Resource bean = session().createRaw(Resource.T, "263a15f8-9e68-4cac-b1a1-d4535af78790");
		bean.setFileSize(873l);
		bean.setCreated(newGmtDate(2015, 4, 30, 15, 52, 25, 356));
		bean.setName("Condensation_32x32.png");
		bean.setSpecification(rasterImageSpecification_206());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_207());
		bean.setMd5("9de9121f0ca76e873e77fe03928facfe");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_207() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7a057e28-0761-4ae0-9e8a-bec7b62f1347");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3017/52/1bfdf299-34ea-412f-9f3c-e3b24c8dea77");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_206() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ca25610d-9d27-425f-8cc7-3c8e92741201");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	@Managed
	private Folder folder_97() {
		Folder bean = session().createRaw(Folder.T, "dbdb37ba-80e5-4d32-add0-9e12f6c17d21");
		bean.setParent(folder_35());
		bean.setSubFolders(Lists.list(folder_98(), folder_96(), folder_99()));
		bean.setDisplayName(localizedString_274());
		bean.setIcon(adaptiveIcon_45());
		bean.setName("View");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_274() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7d5f06c9-7848-4961-8a6d-def5a71b4199");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_45() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "c81f2e9b-4049-46d5-99a4-4f4f424c6fd5");
		bean.setRepresentations(Sets.set(resource_176(), resource_175()));
		return bean;
	}

	// Managed
	private Resource resource_175() {
		Resource bean = session().createRaw(Resource.T, "0e74700d-51d6-415b-bbae-c5d7d0e13150");
		bean.setFileSize(477l);
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 22, 43, 906));
		bean.setName("view_16x16.png");
		bean.setSpecification(rasterImageSpecification_207());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_208());
		bean.setMd5("068351a0336c6cfae580e1a64828cd8e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_208() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "062959d3-69b3-4c9c-a7dd-0f01666c5dfc");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/22/b1f8eb08-948c-4606-baab-33e96f48c850");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_207() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0e75c346-71d8-42d7-9354-5c945d46b18b");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_176() {
		Resource bean = session().createRaw(Resource.T, "93ef2548-fb22-43da-9235-6cb9b24e4b90");
		bean.setFileSize(1210l);
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 22, 51, 401));
		bean.setName("view_32x32.png");
		bean.setSpecification(rasterImageSpecification_208());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_209());
		bean.setMd5("b599c004747fbdd06bc59da58b058edb");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_209() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f04b4b17-5671-4c27-ad84-27fec8e45146");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/22/4e7e8243-7584-4a0e-bd90-aa6b909a0c32");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_208() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1b2a26d5-6f61-4f75-af4d-f9d521ae5b0a");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_98() {
		Folder bean = session().createRaw(Folder.T, "dbd390ea-a312-4e4a-8087-96206be7ccb0");
		bean.setParent(folder_97());
		bean.setDisplayName(localizedString_275());
		bean.setName("$displayMode");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_275() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d483c4aa-061a-4457-b8f5-46524ba47541");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "List View")));
		return bean;
	}

	// Managed
	private Folder folder_99() {
		Folder bean = session().createRaw(Folder.T, "8a08822d-22aa-4b0c-9a17-e6d45d13ef36");
		bean.setParent(folder_97());
		bean.setDisplayName(localizedString_276());
		bean.setName("$uncondenseLocal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_276() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "696e535c-31af-4d61-acdc-7e8ed100ccc6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Hide Details Here")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_279() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1a0042a4-9e33-492a-a43b-36370cb33759");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Applications")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_281() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b1a34367-7d8e-4532-8fc6-9d7f15e0db08");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_284() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "adad8813-0a6c-4aa7-b3d0-4a360e719aea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template New")));
		return bean;
	}

	// Managed
	private Folder folder_100() {
		Folder bean = session().createRaw(Folder.T, "60637ca2-834c-4d51-a1d1-faba8a651e49");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_285());
		bean.setName("Create Template Newest");
		bean.setContent(templateServiceRequestAction_26());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_285() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "76e88a59-8532-4ae7-a41d-bb148bfda94a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template Newest")));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_213() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "396f4ddf-77d8-42a8-b150-31b365c08995");
		bean.setOwner(localEntityProperty_152());
		bean.setNewValue(variable_169());
		return bean;
	}

	// Managed
	private Variable variable_169() {
		Variable bean = session().createRaw(Variable.T, "f1a053ad-d0ff-49c8-849c-0f179bed27c0");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("templateRequest");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_152() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d3473444-a2fc-47f9-86e7-c77e28c3293e");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_9());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_153() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a8308906-7bca-4f2b-b08c-307a00c3f15b");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_9());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_289() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4372f34b-aa51-4ab0-9a6b-5ccc459bfeb1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ensure Workbench")));
		return bean;
	}

	@Managed
	private Resource resource_181() {
		Resource bean = session().createRaw(Resource.T, "e7166e22-6b8f-4c00-b71b-470e0f85f3f2");
		bean.setFileSize(1398l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 57, 42, 298));
		bean.setName("ImportPackage_32x32.png");
		bean.setSpecification(rasterImageSpecification_216());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_185());
		bean.setMd5("3575c44a4087d48a8892b0cc477fe131");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_216() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "209b308e-619c-408a-830a-48c9f177f1a8");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Folder folder_102() {
		Folder bean = session().createRaw(Folder.T, "769542d2-ee95-4168-a3db-2520a7b30a5f");
		bean.setParent(folder_2());
		bean.setDisplayName(localizedString_279());
		bean.setName("Applications");
		return bean;
	}

	// Managed
	private Variable variable_170() {
		Variable bean = session().createRaw(Variable.T, "150e10a9-dced-4a7c-b376-401d510d51bf");
		bean.setTypeSignature("string");
		bean.setName("actionName");
		return bean;
	}

	// Managed
	private Folder folder_105() {
		Folder bean = session().createRaw(Folder.T, "9bfc05fc-acb7-4426-b051-690085841d94");
		bean.setDisplayName(localizedString_295());
		bean.setName("dsa");
		bean.setContent(templateQueryAction_19());
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_19() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "1ddd9a4c-16cb-4c0b-92da-5f95a03416a0");
		bean.setTemplate(template_61());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_295() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1684f5c0-a2bb-4f52-a26d-5a4c5896df8c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "dsa")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_230() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "00bbb24e-67ae-4430-a222-27056a15821a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/3016/57/1469660b-7fc5-4f91-abce-58be7a3f11cf");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_28() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "8f7bde3f-57df-462a-ad16-f34fe2bfc2b5");
		bean.setTemplate(template_71());
		bean.setDisplayName(localizedString_284());
		bean.setInplaceContextCriterion(entityCriterion_18());
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_18() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "108385bb-172c-46dd-8e8d-80c2813eaecb");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private Template template_71() {
		Template bean = session().createRaw(Template.T, "7bbbbe72-8473-4e92-bc8a-d32485569629");
		bean.setName(localizedString_296());
		bean.setPrototype(createServiceRequestTemplate_10());
		bean.setScript(compoundManipulation_52());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_296() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "eb9a4afa-9015-4870-905a-2db074e5a783");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template New-Template")));
		return bean;
	}

	// Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_10() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "708fbb2c-5f66-4bd1-85a2-71118ab31e5e");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_52() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "18c19824-3ef8-4cb5-9135-4d8bb46f1d97");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_219(), changeValueManipulation_97(), changeValueManipulation_220(),
				changeValueManipulation_221(), changeValueManipulation_222()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_219() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a86b4050-852f-499c-b44a-a629cd417ed2");
		bean.setOwner(localEntityProperty_156());
		bean.setNewValue(variable_172());
		return bean;
	}

	// Managed
	private Variable variable_172() {
		Variable bean = session().createRaw(Variable.T, "df10826c-aa7b-4370-81ee-02705d0f6d0d");
		bean.setTypeSignature("string");
		bean.setName("actionName");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_156() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "22d9e152-fbea-434f-b0dd-d2c37da7163b");
		bean.setPropertyName("actionName");
		bean.setEntity(createServiceRequestTemplate_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_220() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4ff153a3-918b-4196-9931-db20ff2f4e55");
		bean.setOwner(localEntityProperty_157());
		bean.setNewValue(variable_173());
		return bean;
	}

	// Managed
	private Variable variable_173() {
		Variable bean = session().createRaw(Variable.T, "c23f412f-a45f-4f8d-ad9e-f17fde8a8800");
		bean.setTypeSignature("com.braintribe.model.meta.GmEntityType");
		bean.setName("criterionType");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_157() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "85396b65-edaa-4a9a-962a-331ba0509453");
		bean.setPropertyName("criterionType");
		bean.setEntity(createServiceRequestTemplate_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_221() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "237c7759-0a1a-4ec4-97e3-e0ef2bd42d26");
		bean.setOwner(localEntityProperty_158());
		bean.setNewValue(variable_174());
		return bean;
	}

	// Managed
	private Variable variable_174() {
		Variable bean = session().createRaw(Variable.T, "71a1fbc1-dc47-4931-981b-3bfe4e9107f6");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setName("templateRequest");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_158() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d2d73328-351e-4bb6-a46f-b949395bfc53");
		bean.setPropertyName("templateRequest");
		bean.setEntity(createServiceRequestTemplate_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_222() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "9f1d3b21-0128-4c19-b04b-94fd4a0f3b5e");
		bean.setOwner(localEntityProperty_159());
		bean.setNewValue(variable_175());
		return bean;
	}

	// Managed
	private Variable variable_175() {
		Variable bean = session().createRaw(Variable.T, "54d3ae66-3ba0-42d6-af72-052beed8af2f");
		bean.setTypeSignature("set<string>");
		bean.setName("variableProperties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_159() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a5cbf11c-e927-42ed-906a-9e3bc2f172dd");
		bean.setPropertyName("variableProperties");
		bean.setEntity(createServiceRequestTemplate_4());
		return bean;
	}

	// Managed
	private Folder folder_106() {
		Folder bean = session().createRaw(Folder.T, "12a4337c-4689-47ae-b98f-121aed6158c5");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_246());
		bean.setName("Create Template New");
		bean.setContent(templateServiceRequestAction_28());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_30() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "d1f2d68b-7504-4c69-ad2b-3244a163bdc2");
		bean.setTemplate(template_51());
		bean.setDisplayName(localizedString_203());
		bean.setInplaceContextCriterion(entityCriterion_19());
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_19() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "ce5744a9-2b18-43f3-bf58-9b87f76ad1c7");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_228() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "5c64fd7d-3e85-4722-9cf8-6e7c560b7fe3");
		bean.setOwner(localEntityProperty_162());
		bean.setNewValue(variable_180());
		return bean;
	}

	// Managed
	private Variable variable_180() {
		Variable bean = session().createRaw(Variable.T, "8e644852-74af-40b6-9e79-8f0a58c766ed");
		bean.setTypeSignature("boolean");
		bean.setName("addToActionBar");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_162() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "3546204a-e234-4d3b-98a1-a1d22663814a");
		bean.setPropertyName("addToActionBar");
		bean.setEntity(createServiceRequestTemplate_9());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_31() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "8f2e50eb-5b5e-4cfc-8e92-7e1fd76291d9");
		bean.setTemplate(template_77());
		bean.setDisplayName(localizedString_315());
		bean.setInplaceContextCriterion(entityCriterion_22());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_315() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "132077b7-ed56-43ee-b7be-6f449ed3b4c9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Workbench Action")));
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_22() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "f867362d-9732-4ee8-ac97-edfb14b86292");
		bean.setTypeSignature("com.braintribe.model.service.api.ServiceRequest");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private Template template_77() {
		Template bean = session().createRaw(Template.T, "fc7d4f8c-3820-480c-bbeb-bc683c502480");
		bean.setName(localizedString_316());
		bean.setPrototype(createServiceRequestTemplate_11());
		bean.setScript(compoundManipulation_56());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_316() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a33bcd42-7ac5-4dc4-b85b-903870c5b720");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Workbench Action-Template")));
		return bean;
	}

	// Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_11() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "2e8e24b4-cdcb-4d26-b5fb-93bebf085636");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_56() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "ade52734-e908-443b-a4b1-39440e29445d");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_233(), changeValueManipulation_228(), changeValueManipulation_183(),
				changeValueManipulation_213(), changeValueManipulation_234()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_233() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3b1259b6-2280-41d0-9505-109c5a9ac712");
		bean.setOwner(localEntityProperty_153());
		bean.setNewValue(variable_170());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_234() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "520b8d33-570c-41f2-a736-e09776e0399c");
		bean.setOwner(localEntityProperty_165());
		bean.setNewValue(variable_182());
		return bean;
	}

	// Managed
	private Variable variable_182() {
		Variable bean = session().createRaw(Variable.T, "801a58b6-3073-41d5-9941-b5e7f990ec81");
		bean.setTypeSignature("set<string>");
		bean.setName("variableProperties");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_165() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "8370b665-0e9b-4085-ac68-9fa331e8ce8b");
		bean.setPropertyName("variableProperties");
		bean.setEntity(createServiceRequestTemplate_9());
		return bean;
	}

	@Managed
	private Resource resource_191() {
		Resource bean = session().createRaw(Resource.T, "e94bb541-3dcb-49ee-bd71-7544c0b72d2b");
		bean.setFileSize(623l);
		bean.setCreated(newGmtDate(2015, 4, 30, 14, 57, 42, 291));
		bean.setName("ImportPackage_16x16.png");
		bean.setSpecification(rasterImageSpecification_240());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_230());
		bean.setMd5("9986e22a71995c9cdec6c2002c3f0e31");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_240() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "acffeb53-ea4b-4133-96b4-6372bb711d92");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_331() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3b11fb7b-2c70-4e80-a7f7-62a7a9349ac2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "test")));
		return bean;
	}

	// Managed
	private Folder folder_116() {
		Folder bean = session().createRaw(Folder.T, "60f0de50-dc18-4430-8f4f-82db8eaac511");
		bean.setParent(folder_8());
		bean.setDisplayName(localizedString_338());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("Images");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_338() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "90e8a6b9-a494-4b2c-8031-8e29f48ede81");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images")));
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_27() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "5b2f86c6-2cc7-4efe-831b-04146741c479");
		bean.setTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		bean.setStrategy(EntityTypeStrategy.assignable);
		return bean;
	}

	// Managed
	private Folder folder_118() {
		Folder bean = session().createRaw(Folder.T, "89419ba2-41ad-4e43-b133-05cd2be9539c");
		bean.setParent(folder_14());
		bean.setDisplayName(localizedString_345());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_345() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1fcd647c-38c4-4c9e-80db-de6d4e5a082f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Document Model Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_349() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8ed4756c-9765-4a54-92ee-f6000edc734e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "my folder")));
		return bean;
	}

	// Managed
	private Folder folder_119() {
		Folder bean = session().createRaw(Folder.T, "a1e25c22-8929-468e-acdd-732732bfb4dc");
		bean.setDisplayName(localizedString_166());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("Cartridges");
		bean.setContent(simpleQueryAction_1());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private Folder folder_120() {
		Folder bean = session().createRaw(Folder.T, "8512b108-d9c7-403f-83dd-8e2698ef5cb9");
		bean.setParent(folder_80());
		bean.setDisplayName(localizedString_349());
		return bean;
	}

	// Managed
	private Resource resource_211() {
		Resource bean = session().createRaw(Resource.T, "95470b8a-8fa7-4c03-aba8-5318200c69f9");
		bean.setFileSize(489l);
		bean.setCreated(newGmtDate(2015, 4, 21, 14, 21, 8, 136));
		bean.setName("export_16x16.png");
		bean.setSpecification(rasterImageSpecification_277());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_276());
		bean.setMd5("6b791ac55d821e5fbbda891b962f2326");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_276() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8ce33ba9-5a1a-4ca0-b20b-c695bec22ce7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2116/21/ce121d50-9d62-4542-b0b2-c9e77dc0ccad");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_277() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f96623e6-5226-452d-9f5b-85d4caac1e6d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_352() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1baa15a5-35f8-413e-85d9-1b06248a47dd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Template")));
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_281() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3e077de8-b5e4-4a6a-9717-c271b4dc25db");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_355() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6c8d65f6-7961-453f-9243-04632a1fd7ff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New ")));
		return bean;
	}

	// Managed
	private Folder folder_121() {
		Folder bean = session().createRaw(Folder.T, "c97d57dd-e2da-4fc7-ab36-6e0fd1028608");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_358());
		bean.setName("More Actions 2");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_358() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a7ed5c49-9e90-40a1-98ff-1264f53782c4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "More Actions 2")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_361() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "79724c62-db72-4f99-a840-a9c7f52915e0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Workbench Action")));
		return bean;
	}

	// Managed
	private Resource resource_215() {
		Resource bean = session().createRaw(Resource.T, "0c2a4fa6-c482-463f-9edc-e29e1bdcf640");
		bean.setFileSize(15684l);
		bean.setCreated(newGmtDate(2015, 4, 12, 8, 7, 54, 146));
		bean.setName("Import_32x32.png");
		bean.setSpecification(rasterImageSpecification_281());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_181());
		bean.setMd5("8da8aacd14cdbf3dcf2273b65a99b0c3");
		return bean;
	}

	// Managed
	private Folder folder_124() {
		Folder bean = session().createRaw(Folder.T, "61d8ab5b-e73a-4727-8120-f766767e95b0");
		bean.setDisplayName(localizedString_256());
		bean.setName("Create Template");
		bean.setContent(templateServiceRequestAction_30());
		return bean;
	}

	// Managed
	private Resource resource_217() {
		Resource bean = session().createRaw(Resource.T, "110fb21c-3013-4a4b-9fd1-6b1f27a73f1b");
		bean.setFileSize(14973l);
		bean.setCreated(newGmtDate(2015, 4, 12, 8, 7, 54, 142));
		bean.setName("Import_16x16.png");
		bean.setSpecification(rasterImageSpecification_294());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_103());
		bean.setMd5("eb9140d29861e6080e82d490110a8ea1");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_294() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a0e4f9a5-ac00-4ef6-93f4-b6c7947d8f53");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_125() {
		Folder bean = session().createRaw(Folder.T, "aabf43da-67bd-4bc3-96f9-421659a8633e");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_366());
		bean.setName("$editTemplateScript");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_366() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "260c3f15-8bc8-440b-9b7c-1a01140b86b7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit Template")));
		return bean;
	}

	// Managed
	private Folder folder_126() {
		Folder bean = session().createRaw(Folder.T, "45ac0d5f-fcbb-40dd-abed-0c70ad5184c0");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_361());
		bean.setName("Create Workbench Action");
		bean.setContent(templateServiceRequestAction_31());
		return bean;
	}

	// Managed
	private Folder folder_128() {
		Folder bean = session().createRaw(Folder.T, "3b9656df-e390-4481-9a6a-3566cdc2997c");
		bean.setParent(folder_14());
		bean.setDisplayName(localizedString_370());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_370() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e09c4601-2626-49df-bdd3-6cd6ee88c087");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "My Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_371() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6b20a86a-b2b4-49b1-ad8d-d266d921882f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "CortexAccess")));
		return bean;
	}

	// Managed
	private Folder folder_129() {
		Folder bean = session().createRaw(Folder.T, "07628790-ef24-4b96-834e-56f35167ab3f");
		bean.setDisplayName(localizedString_355());
		bean.setName("$instantiateEntity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_373() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8d1b49a3-7670-4680-8882-3e041c9d4c4f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private Folder folder_132() {
		Folder bean = session().createRaw(Folder.T, "2b69ec71-8037-4ef3-9137-b6c24fd3d796");
		bean.setParent(folder_4());
		bean.setDisplayName(localizedString_371());
		bean.setName("CortexAccess");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_36() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "0293dc5a-714b-4af1-96c8-e47426da8412");
		bean.setTemplate(template_55());
		bean.setDisplayName(localizedString_289());
		bean.setInplaceContextCriterion(entityCriterion_27());
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "6503a025-a0f0-42b5-ad1b-5ee0ca767ec9");
		bean.setFolders(Lists.list(folder_35()));
		bean.setDisplayName(localizedString_281());
		bean.setName("actionbar");
		return bean;
	}

	// Managed
	private Folder folder_133() {
		Folder bean = session().createRaw(Folder.T, "f6515267-b3ca-4eff-8a73-f336a2a3adc1");
		bean.setParent(folder_79());
		bean.setDisplayName(localizedString_381());
		bean.setIcon(adaptiveIcon_53());
		bean.setName("UpdateDbSchema");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_381() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8b1d19dc-b81f-4029-834f-fa18a9378e42");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Schema Updates")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_53() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "ce8dd73c-b508-44f9-b4f2-b2600d66aa0b");
		return bean;
	}

	// Managed
	private Folder folder_134() {
		Folder bean = session().createRaw(Folder.T, "6b77fe24-04cd-459c-b4f6-5bedff17194b");
		bean.setDisplayName(localizedString_382());
		bean.setName("$openModel");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_382() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "decd23b3-5ef3-4019-939d-3ac417ad16de");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open Modeller")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_385() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bf8920de-e3dd-4e1c-a354-dbbf0c922c19");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Root")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_386() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "23fcec52-41bc-474c-bfa3-3637636658cd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Accesses")));
		return bean;
	}

	// Managed
	private Folder folder_140() {
		Folder bean = session().createRaw(Folder.T, "ef6c2e7c-8057-4239-9072-4f3a9ad254d7");
		bean.setParent(folder_33());
		bean.setDisplayName(localizedString_386());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("Accesses");
		return bean;
	}

	// Managed
	private Folder folder_142() {
		Folder bean = session().createRaw(Folder.T, "fa0599bc-9bc3-44cc-89f8-025500acc153");
		bean.setParent(folder_3());
		bean.setDisplayName(localizedString_331());
		return bean;
	}

	// Managed
	private Folder folder_143() {
		Folder bean = session().createRaw(Folder.T, "8af91094-3872-454b-b4aa-d45e19994796");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_352());
		bean.setName("CreateTemplate");
		bean.setContent(templateServiceRequestAction_22());
		return bean;
	}

	// Managed
	private Folder folder_144() {
		Folder bean = session().createRaw(Folder.T, "3b2f6f10-ee2a-4dc5-9699-1b7e785faa8e");
		bean.setDisplayName(localizedString_373());
		bean.setName("$detailsPanelVisibility");
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_3() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "dbb2d0e8-c421-4460-97b2-64c551c380a6");
		bean.setFolders(Lists.list(folder_3()));
		bean.setDisplayName(localizedString_385());
		bean.setName("root");
		return bean;
	}

	// Managed
	private Folder folder_149() {
		Folder bean = session().createRaw(Folder.T, "90aae135-b5eb-4ea6-87c7-07d08453f5cd");
		bean.setParent(folder_35());
		bean.setDisplayName(localizedString_65());
		bean.setName("Ensure Workbench");
		bean.setContent(templateServiceRequestAction_36());
		return bean;
	}

	@Managed
	private CreateServiceRequestTemplate createServiceRequestTemplate_14() {
		CreateServiceRequestTemplate bean = session().createRaw(CreateServiceRequestTemplate.T, "9279417e-e077-4ea1-b29b-6e72377ee995");
		bean.setFolderPath("actionbar/more");
		bean.setIgnoreStandardProperties(Boolean.TRUE);
		bean.setInstantiationAction(Boolean.TRUE);
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_281() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "de05358f-26db-43d2-815d-3f6ffd454aec");
		bean.setOwner(localEntityProperty_189());
		bean.setNewValue(variable_210());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_189() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "28f924aa-e62d-43ef-84f6-3fda28c34b13");
		bean.setPropertyName("instantiationAction");
		bean.setEntity(createServiceRequestTemplate_14());
		return bean;
	}

	// Managed
	private Variable variable_210() {
		Variable bean = session().createRaw(Variable.T, "e83b5c9a-de36-442c-8c1c-06af5190726f");
		bean.setLocalizedName(localizedString_399());
		bean.setTypeSignature("boolean");
		bean.setName("instantiationAction");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_399() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ce5b5770-16e4-4562-bacb-739dc0c1759f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Is Instantiation Action")));
		return bean;
	}

	@Managed
	private CreateModel createModel_4() {
		CreateModel bean = session().createRaw(CreateModel.T, "f58f2f7e-10c6-49d7-93c3-a5cd84fbcd10");
		bean.setGroupId("custom.model");
		bean.setVersion("1.0");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_69() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "27bdb64d-4d1a-41df-9a5a-cbf196f3d815");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_286(), changeValueManipulation_287(), changeValueManipulation_288(),
				changeValueManipulation_289()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_286() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c4d3b98a-511b-4343-93b5-5c72114fe345");
		bean.setOwner(localEntityProperty_191());
		bean.setNewValue(variable_211());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_191() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0cdbc655-46be-4281-ac64-52e9fd941454");
		bean.setPropertyName("dependencies");
		bean.setEntity(createModel_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_287() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "8060ca16-1013-49df-8398-7f502e03b0d4");
		bean.setOwner(localEntityProperty_192());
		bean.setNewValue(variable_212());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_192() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "981e8374-e9af-4646-98e8-b38dd769a9a4");
		bean.setPropertyName("groupId");
		bean.setEntity(createModel_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_288() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "d6d504f1-376a-4666-af21-0d7507e211dc");
		bean.setOwner(localEntityProperty_193());
		bean.setNewValue(variable_213());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_193() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "956a08e5-6ece-4c59-84d9-6068f57954aa");
		bean.setPropertyName("name");
		bean.setEntity(createModel_4());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_289() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3bf2c634-83f3-4b54-8da7-699d69c424f5");
		bean.setOwner(localEntityProperty_194());
		bean.setNewValue(variable_214());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_194() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b48240aa-e382-45b8-bca0-fc08d1f39e6c");
		bean.setPropertyName("version");
		bean.setEntity(createModel_4());
		return bean;
	}

	// Managed
	private Variable variable_211() {
		Variable bean = session().createRaw(Variable.T, "f74cc092-e6bc-4f3a-aa1c-6029520c2e6c");
		bean.setLocalizedName(localizedString_404());
		bean.setTypeSignature("list<com.braintribe.model.meta.GmMetaModel>");
		bean.setName("dependencies");
		return bean;
	}

	// Managed
	private Variable variable_212() {
		Variable bean = session().createRaw(Variable.T, "c218218e-7d23-47c6-8ad6-ce1f1ae39769");
		bean.setLocalizedName(localizedString_405());
		bean.setTypeSignature("string");
		bean.setDefaultValue("custom.model");
		bean.setName("groupId");
		return bean;
	}

	@Managed
	private Variable variable_213() {
		Variable bean = session().createRaw(Variable.T, "3867ec46-bb8a-4cec-a172-1c83720c5df7");
		bean.setLocalizedName(localizedString_406());
		bean.setTypeSignature("string");
		bean.setName("name");
		return bean;
	}

	// Managed
	private Variable variable_214() {
		Variable bean = session().createRaw(Variable.T, "d9e956d9-0259-4fba-95d6-e27181f2e486");
		bean.setLocalizedName(localizedString_407());
		bean.setTypeSignature("string");
		bean.setDefaultValue("1.0");
		bean.setName("version");
		return bean;
	}

	@Managed
	private Folder folder_150() {
		Folder bean = session().createRaw(Folder.T, "3dc2cd18-2927-4e25-8d19-597938c68572");
		bean.setSubFolders(Lists.list(folder_151()));
		bean.setDisplayName(localizedString_400());
		bean.setName("globalactionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_400() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4a92cb0f-e095-409d-bf9a-ce6f18b53207");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "globalactionbar")));
		return bean;
	}

	// Managed
	private Folder folder_151() {
		Folder bean = session().createRaw(Folder.T, "3d5842a6-43af-4f15-8f87-36904993bc37");
		bean.setParent(folder_150());
		bean.setDisplayName(localizedString_401());
		bean.setName("Create Model (global)");
		bean.setContent(templateInstantiationServiceRequestAction_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_401() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "12f379d7-4c28-414c-b4bf-9384ebe198ed");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Model (global)")));
		return bean;
	}

	// Managed
	private TemplateInstantiationServiceRequestAction templateInstantiationServiceRequestAction_1() {
		TemplateInstantiationServiceRequestAction bean = session().createRaw(TemplateInstantiationServiceRequestAction.T,
				"b43d485d-31ce-4200-ac30-f28030d2afc7");
		bean.setTemplate(template_91());
		bean.setDisplayName(localizedString_402());
		bean.setIcon(adaptiveIcon_34());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_26());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_402() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a5c0c423-1037-47b5-8623-a51fd632c986");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New Model")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_26() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "f4c36715-0974-4d1a-82be-3816671abc4b");
		bean.setTypeCondition(isAssignableTo_22());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_22() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "f3aa7e49-8ebf-4701-b847-d06b9856a357");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_91() {
		Template bean = session().createRaw(Template.T, "c80d9d38-e690-4340-a5b0-66535e9b6557");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_28()));
		bean.setName(localizedString_403());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.model.CreateModel");
		bean.setTechnicalName("CreateModelTemplate");
		bean.setPrototype(createModel_4());
		bean.setScript(compoundManipulation_69());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_403() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d32e753f-fd89-4378-bff6-e614ab522cc5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create New Model")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_404() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f2684a39-2ff0-4cb3-963d-af54a0dd9a06");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Dependencies")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_405() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "55155b18-129d-4197-b3fc-67a365c8918e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Group Id")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_406() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9dd00474-4e5c-427d-aa92-507b81189157");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Name")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_407() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f2ab56ba-5c73-48ba-bec8-13b6fc028286");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Version")));
		return bean;
	}

	@Managed
	private Export export_1() {
		Export bean = session().createRaw(Export.T, "e3298c20-dadf-47ed-885e-c05d8bf7540f");
		bean.setDescriptor(exportDescriptor_2());
		return bean;
	}

	@Managed
	private StaticSupplier staticSupplier_1() {
		StaticSupplier bean = session().createRaw(StaticSupplier.T, "78d9f658-0753-4794-98e5-ecd0f725eb8b");
		return bean;
	}

	// Managed
	private DefaultPredicate defaultPredicate_1() {
		DefaultPredicate bean = session().createRaw(DefaultPredicate.T, "e05a24a6-ae20-473e-abb1-153fe16f06ca");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_71() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "086a1115-fe0d-4699-a60a-41da4e2c7cf8");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_291(), changeValueManipulation_292(), changeValueManipulation_293()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_291() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "4b774bad-155c-49ea-9bcb-855673901993");
		bean.setOwner(localEntityProperty_196());
		bean.setNewValue(variable_216());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_196() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "4b283038-af98-44aa-a465-f31a8afd16aa");
		bean.setPropertyName("description");
		bean.setEntity(export_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_292() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "15eda4cd-d890-4a67-94cb-b886f3fdd3d7");
		bean.setOwner(localEntityProperty_197());
		bean.setNewValue(variable_217());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_197() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "c02fd468-fa32-4339-acf2-84c8f3548859");
		bean.setPropertyName("entities");
		bean.setEntity(staticSupplier_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_293() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "60f0eeae-b191-4a00-a9b8-5606b8de5c5a");
		bean.setOwner(localEntityProperty_198());
		bean.setNewValue(variable_218());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_198() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9636a202-604b-429c-a9d3-72f1c8eeab13");
		bean.setPropertyName("name");
		bean.setEntity(export_1());
		return bean;
	}

	// Managed
	private Variable variable_216() {
		Variable bean = session().createRaw(Variable.T, "84cc5a2e-d82a-484f-8891-ba6b004752b5");
		bean.setLocalizedName(localizedString_416());
		bean.setTypeSignature("string");
		bean.setName("description");
		return bean;
	}

	@Managed
	private Variable variable_217() {
		Variable bean = session().createRaw(Variable.T, "e67617a4-365f-4b73-af89-b0c73f1364f8");
		bean.setLocalizedName(localizedString_417());
		bean.setTypeSignature("list<com.braintribe.model.generic.GenericEntity>");
		bean.setName("entities");
		return bean;
	}

	@Managed
	private Variable variable_218() {
		Variable bean = session().createRaw(Variable.T, "14378ee7-312b-4559-b791-0455b6063cd6");
		bean.setLocalizedName(localizedString_418());
		bean.setTypeSignature("string");
		bean.setName("name");
		return bean;
	}

	@Managed
	private Folder folder_153() {
		Folder bean = session().createRaw(Folder.T, "5ed081ae-2459-4abd-a236-824d2cc33def");
		bean.setParent(folder_35());
		bean.setSubFolders(Lists.list(folder_154(), folder_155(), folder_163(), folder_158()));
		bean.setDisplayName(localizedString_412());
		bean.setIcon(adaptiveIcon_68());
		bean.setName("exchangePackage");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_412() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "45792ab8-b94c-46f7-83b1-58a88702c53b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange Package")));
		return bean;
	}

	// Managed
	private Folder folder_154() {
		Folder bean = session().createRaw(Folder.T, "b9b6252b-319d-41cf-8901-3f9b0a88944d");
		bean.setParent(folder_153());
		bean.setDisplayName(localizedString_413());
		bean.setName("Export");
		bean.setIcon(adaptiveIcon_1());
		bean.setContent(templateServiceRequestAction_38());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_413() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2dbb1dd5-b0d9-4263-9353-b4fc305c7fab");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_38() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "523b1ac9-df4a-4d7d-8f98-ac52a3df13f5");
		bean.setTemplate(template_93());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_414());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_16());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_414() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3c26946b-8e47-4dbf-84ea-99e9178effa4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_28() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "1fc732ad-907e-40b9-81a1-73d9b35f2f7e");
		bean.setTypeCondition(typeConditionConjunction_1());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_24() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "ed11d9dd-6f76-4607-9b9d-167459f62e6d");
		bean.setTypeSignature("com.braintribe.model.generic.GenericEntity");
		return bean;
	}

	// Managed
	private Template template_93() {
		Template bean = session().createRaw(Template.T, "1301c3b1-9e0b-4a8d-9f20-51d128447e85");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_15(), dynamicPropertyMetaDataAssignment_24()));
		bean.setName(localizedString_415());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.Export");
		bean.setTechnicalName("ExportTemplate");
		bean.setPrototype(export_1());
		bean.setScript(compoundManipulation_71());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_415() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "39237be3-acb9-414b-b91f-49a868c3a535");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export Package")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_416() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c91a49e3-77c2-40cb-abac-6b11e0d46aeb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Description")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_417() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1e4efe27-e1c0-49fc-96d0-785f976fcb0d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entities")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_418() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "76439ba8-fca6-4dd5-967e-9e0d8a0001e3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Name")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_56() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "5995bf29-67fd-489c-972b-da41bc4ce259");
		bean.setName("Export Icon");
		bean.setRepresentations(Sets.set(resource_211(), resource_164()));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_15() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "8cab09a7-7a3e-4fb7-8472-41065768b038");
		bean.setMetaData(Sets.set(priority_7()));
		bean.setVariable(variable_218());
		return bean;
	}

	// Managed
	private Priority priority_7() {
		Priority bean = session().createRaw(Priority.T, "299a6175-7999-406f-a3c3-f372f4c0a192");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_81() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "c9bf7172-3a7f-4953-97f6-343aa15ce4e2");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_303(), changeValueManipulation_304(), changeValueManipulation_305(),
				changeValueManipulation_306(), changeValueManipulation_307(), changeValueManipulation_308(), changeValueManipulation_333()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_303() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "bd7ab1b8-d7cb-46be-856f-f3092ab4d7ae");
		bean.setOwner(localEntityProperty_208());
		bean.setNewValue(variable_228());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_208() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "1d506e08-353a-460f-9975-7936be8242a0");
		bean.setPropertyName("description");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_304() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1d819fa8-715e-4788-920e-1637d672bbe3");
		bean.setOwner(localEntityProperty_209());
		bean.setNewValue(variable_229());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_209() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "723460c3-da98-4f1f-9b47-4fd1f3ac797d");
		bean.setPropertyName("entities");
		bean.setEntity(staticSupplier_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_305() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "2001a167-3a99-4b53-8107-8da981e38cc0");
		bean.setOwner(localEntityProperty_210());
		bean.setNewValue(variable_230());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_210() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d3bef65c-e2c9-408e-98c7-9db65dbda5e0");
		bean.setPropertyName("name");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_306() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a71f3898-8c55-47ad-9e9d-01d4721be037");
		bean.setOwner(localEntityProperty_211());
		bean.setNewValue(variable_231());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_211() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9e1668d9-1908-4268-a933-cac3a7a6cd0c");
		bean.setPropertyName("prettyOutput");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_307() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1ec96572-59d9-4d89-8aae-8eb5f7f7e8c9");
		bean.setOwner(localEntityProperty_212());
		bean.setNewValue(variable_232());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_212() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "45874c60-5fa0-492f-9c91-3cf91330e9eb");
		bean.setPropertyName("stabilizeOrder");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_308() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "7aa6adcc-bbe8-43fb-9534-357900375dd5");
		bean.setOwner(localEntityProperty_213());
		bean.setNewValue(variable_233());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_213() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6393d2e4-51d7-4d44-b026-a1a01e059998");
		bean.setPropertyName("writeEmptyProperties");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	@Managed
	private Variable variable_228() {
		Variable bean = session().createRaw(Variable.T, "d53c491c-1c3b-45c8-9403-026acfc3a04a");
		bean.setLocalizedName(localizedString_440());
		bean.setTypeSignature("string");
		bean.setName("description");
		return bean;
	}

	@Managed
	private Variable variable_229() {
		Variable bean = session().createRaw(Variable.T, "ae7218a5-abfb-4e51-ae1d-de4d57c5f924");
		bean.setLocalizedName(localizedString_441());
		bean.setTypeSignature("list<com.braintribe.model.generic.GenericEntity>");
		bean.setName("entities");
		return bean;
	}

	@Managed
	private Variable variable_230() {
		Variable bean = session().createRaw(Variable.T, "7f9561c8-6a0b-4fb4-8dad-e743415384e3");
		bean.setLocalizedName(localizedString_442());
		bean.setTypeSignature("string");
		bean.setName("name");
		return bean;
	}

	// Managed
	private Variable variable_231() {
		Variable bean = session().createRaw(Variable.T, "d195e482-e881-4215-89d9-d9715543c450");
		bean.setLocalizedName(localizedString_443());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("prettyOutput");
		return bean;
	}

	// Managed
	private Variable variable_232() {
		Variable bean = session().createRaw(Variable.T, "e5c8b457-f499-4d1c-98c5-fe1cd17d2b21");
		bean.setLocalizedName(localizedString_444());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("stabilizeOrder");
		return bean;
	}

	// Managed
	private Variable variable_233() {
		Variable bean = session().createRaw(Variable.T, "be825b76-93fb-47de-92eb-0d57aadcd976");
		bean.setLocalizedName(localizedString_445());
		bean.setTypeSignature("boolean");
		bean.setName("writeEmptyProperties");
		return bean;
	}

	// Managed
	private Folder folder_155() {
		Folder bean = session().createRaw(Folder.T, "90ab453c-93fa-4c1e-9777-d37ad2b752e3");
		bean.setParent(folder_153());
		bean.setDisplayName(localizedString_437());
		bean.setName("Export as Resource");
		bean.setIcon(adaptiveIcon_1());
		bean.setContent(templateServiceRequestAction_39());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_437() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1eb9820d-1323-4864-bbab-067b92872c2d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export & Write")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_39() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "344c5fa1-01ee-4bd2-ba6a-e75e7c788a67");
		bean.setTemplate(template_94());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_438());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_16());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_438() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "73e9bbd6-ead5-4a34-82e2-cb3b05707d4c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export as Resource")));
		return bean;
	}

	// Managed
	private Template template_94() {
		Template bean = session().createRaw(Template.T, "dede22f2-2752-4eff-be84-73903f8c1e63");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_26(), dynamicPropertyMetaDataAssignment_27(),
				dynamicPropertyMetaDataAssignment_16(), dynamicPropertyMetaDataAssignment_23()));
		bean.setName(localizedString_439());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.ExportAndWriteToResource");
		bean.setTechnicalName("ExportAndWriteToResourceTemplate");
		bean.setPrototype(exportAndWriteToResource_1());
		bean.setScript(compoundManipulation_81());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_439() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "27540519-54e9-4da9-9db7-4814e060a808");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Export & Write to Resource")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_440() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "898e1b2e-2438-426a-8e00-69384d0a24b2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Description")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_441() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9c5aa799-6962-48b4-8bc6-2236910acd15");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entities")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_442() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9192e69a-1ab5-4ef5-adde-4d1df4e6040b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Name")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_443() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a5dfd59e-b16a-4268-9649-296f396ca041");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Pretty Output")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_444() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4e24d649-88bf-4562-b6e0-0ea936d53d84");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Stabilize Order")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_445() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b7594385-0a93-4a3b-b7ee-120b1bd38027");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Write Empty Properties")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_16() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "1f8025ab-99f0-4f0b-b969-f628d1ccf629");
		bean.setMetaData(Sets.set(priority_8()));
		bean.setVariable(variable_230());
		return bean;
	}

	// Managed
	private Priority priority_8() {
		Priority bean = session().createRaw(Priority.T, "8302d38b-b078-47cf-b5cd-4d41b598bffc");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	@Managed
	private ReadFromResource readFromResource_1() {
		ReadFromResource bean = session().createRaw(ReadFromResource.T, "29c50cb7-87c0-4769-816e-a53fb0aecf10");
		bean.setLenient(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_82() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "a42cdf59-a737-4d26-97e5-074d38ba15a6");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_310(), changeValueManipulation_311()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_310() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "663cc5a3-e14e-42b6-bc5d-4a68e2d26881");
		bean.setOwner(localEntityProperty_215());
		bean.setNewValue(variable_235());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_215() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bb3e8955-9d71-43d9-a7c6-c9eab9143719");
		bean.setPropertyName("lenient");
		bean.setEntity(readFromResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_311() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "04f1a229-2d79-434c-9642-da8ecb9d359c");
		bean.setOwner(localEntityProperty_216());
		bean.setNewValue(variable_236());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_216() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9765c065-c9e1-461f-a1a0-b356710999ae");
		bean.setPropertyName("resource");
		bean.setEntity(readFromResource_1());
		return bean;
	}

	// Managed
	private Variable variable_235() {
		Variable bean = session().createRaw(Variable.T, "e856761a-8eb2-415d-830c-9dd07a7395f7");
		bean.setLocalizedName(localizedString_451());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("lenient");
		return bean;
	}

	@Managed
	private Variable variable_236() {
		Variable bean = session().createRaw(Variable.T, "4a61caf8-e4ef-4686-a327-e5e35605cb4a");
		bean.setLocalizedName(localizedString_452());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		bean.setName("resource");
		return bean;
	}

	@Managed
	private Folder folder_156() {
		Folder bean = session().createRaw(Folder.T, "b562e0a8-2886-4a13-9ead-bfbd2f8a574b");
		bean.setParent(folder_35());
		bean.setSubFolders(Lists.list(folder_158(), folder_157(), folder_160()));
		bean.setDisplayName(localizedString_446());
		bean.setIcon(adaptiveIcon_65());
		bean.setName("import");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_446() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "77dec2e2-38e3-486d-a79e-2a46055aff10");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Import")));
		return bean;
	}

	@Managed
	private Folder folder_157() {
		Folder bean = session().createRaw(Folder.T, "3d9df447-d3ac-43c6-82e4-b6cbf4c8b51c");
		bean.setParent(folder_156());
		bean.setDisplayName(localizedString_447());
		bean.setName("Read");
		bean.setContent(templateServiceRequestAction_40());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_447() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a9ecdf04-1122-4fec-90bd-b10e2e6f3447");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read from Resource")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_40() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "5cc082e2-4066-4ffb-bcb6-623b7ca33869");
		bean.setTemplate(template_95());
		bean.setDisplayName(localizedString_448());
		bean.setIcon(adaptiveIcon_65());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_13());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_448() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "16f0b2ca-9013-402f-8306-47726ef66e25");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_39() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "70a63d1a-3fcc-499a-92ab-8222d6434b76");
		bean.setTypeCondition(isAssignableTo_35());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_35() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "c4270343-7f1d-41ca-a48d-1d5b98131735");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private Template template_95() {
		Template bean = session().createRaw(Template.T, "19ffd4cf-af42-4f94-9bac-e00542519b42");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_17()));
		bean.setName(localizedString_449());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.ReadFromResource");
		bean.setTechnicalName("ReadFromResourceTemplate");
		bean.setPrototype(readFromResource_1());
		bean.setScript(compoundManipulation_82());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_449() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "85a5c301-5409-49f4-9342-3bcc1695d995");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "ReadFromResource Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_451() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8b15a1ef-c1c3-4134-9778-a63376ef01db");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Lenient")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_452() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2ea25b99-3708-4f25-8418-17d4ee75a0d0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_65() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e1f6665a-de78-4a8e-adf0-46bb5254eeab");
		bean.setName("Import Icon");
		bean.setRepresentations(Sets.set(resource_217(), resource_215()));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_13() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "1bc8c9e5-172f-4890-9332-8fde0da8fea3");
		bean.setCriteria(Lists.list(typeConditionCriterion_39(), valueConditionCriterion_15()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_15() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "2677678b-0bdd-423d-a9c4-c32eb4e07ba8");
		bean.setPropertyPath("name");
		bean.setOperator(ComparisonOperator.matchesIgnoreCase);
		bean.setOperand(".*(\\.xml|\\.json|\\.bin)");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_17() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "4400c726-5bd7-4791-89ca-e056c84eae61");
		bean.setMetaData(Sets.set(hidden_2()));
		bean.setVariable(variable_236());
		return bean;
	}

	// Managed
	private Hidden hidden_2() {
		Hidden bean = session().createRaw(Hidden.T, "a0917868-85ce-424e-8f9b-e5f51daeb281");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_2());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_2() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "95c25f78-c67c-487c-b1f6-dd3e74c66ba5");
		bean.setPropertyPath("resource");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	@Managed
	private ReadFromResourceAndImport readFromResourceAndImport_1() {
		ReadFromResourceAndImport bean = session().createRaw(ReadFromResourceAndImport.T, "b96803ea-d841-4d55-a5b8-9a4eea15c219");
		bean.setRequiresGlobalId(Boolean.TRUE);
		bean.setLenient(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_83() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "efd32113-6c19-434a-bf09-ddb8a133b919");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_312(), changeValueManipulation_313(), changeValueManipulation_314(),
				changeValueManipulation_315(), changeValueManipulation_316(), changeValueManipulation_317()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_312() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "08e716d8-4e16-4160-8ada-8f8c356bbd6e");
		bean.setOwner(localEntityProperty_217());
		bean.setNewValue(variable_237());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_217() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "af56954d-8ee4-49d6-91b6-e98097a5e7df");
		bean.setPropertyName("createShallowInstanceForMissingReferences");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_313() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "cda143f3-d311-4021-b154-e3126e340fab");
		bean.setOwner(localEntityProperty_218());
		bean.setNewValue(variable_238());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_218() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "e108e599-26d0-4a93-979d-f0bda54c9492");
		bean.setPropertyName("includeEnvelope");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_314() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "7d7a6a44-c4b2-4360-a3da-aaf5e2e4736d");
		bean.setOwner(localEntityProperty_219());
		bean.setNewValue(variable_239());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_219() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "5614cea6-88d6-4545-8dbf-fae5cda61304");
		bean.setPropertyName("lenient");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_315() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "8d2081a3-a7df-4dcf-a445-dc144e805817");
		bean.setOwner(localEntityProperty_220());
		bean.setNewValue(variable_240());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_220() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "2817fcad-bd7e-4867-a468-fc7958242069");
		bean.setPropertyName("requiresGlobalId");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_316() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "99d64331-c803-4d0f-8cad-c6f3637ac605");
		bean.setOwner(localEntityProperty_221());
		bean.setNewValue(variable_241());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_221() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b33ddfce-71e5-48d8-9e94-d687c9c48eef");
		bean.setPropertyName("resource");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_317() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "83575e40-e5a3-4099-8d27-677b9bb403ef");
		bean.setOwner(localEntityProperty_222());
		bean.setNewValue(variable_242());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_222() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "f2dfefd8-c718-4100-a546-96abb887cc68");
		bean.setPropertyName("useSystemSession");
		bean.setEntity(readFromResourceAndImport_1());
		return bean;
	}

	// Managed
	private Variable variable_237() {
		Variable bean = session().createRaw(Variable.T, "54d27425-145d-4e52-91a4-9f7fb0b5969c");
		bean.setLocalizedName(localizedString_456());
		bean.setTypeSignature("boolean");
		bean.setName("createShallowInstanceForMissingReferences");
		return bean;
	}

	// Managed
	private Variable variable_238() {
		Variable bean = session().createRaw(Variable.T, "da27d0e6-5e05-4ad6-9e07-339a2d21faf8");
		bean.setLocalizedName(localizedString_457());
		bean.setTypeSignature("boolean");
		bean.setName("includeEnvelope");
		return bean;
	}

	@Managed
	private Variable variable_239() {
		Variable bean = session().createRaw(Variable.T, "db71a3d4-b71a-4d93-a685-dd95a46ca989");
		bean.setLocalizedName(localizedString_458());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("lenient");
		return bean;
	}

	// Managed
	private Variable variable_240() {
		Variable bean = session().createRaw(Variable.T, "d9c0480a-62ec-4164-8774-bcd494b8b213");
		bean.setLocalizedName(localizedString_459());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("requiresGlobalId");
		return bean;
	}

	@Managed
	private Variable variable_241() {
		Variable bean = session().createRaw(Variable.T, "a9467570-d99b-480d-95ed-3f88bc7907ca");
		bean.setLocalizedName(localizedString_460());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		bean.setName("resource");
		return bean;
	}

	// Managed
	private Variable variable_242() {
		Variable bean = session().createRaw(Variable.T, "d1a51dea-0ebb-4cb5-8a4d-21385f3ccff2");
		bean.setLocalizedName(localizedString_461());
		bean.setTypeSignature("boolean");
		bean.setName("useSystemSession");
		return bean;
	}

	@Managed
	private Folder folder_158() {
		Folder bean = session().createRaw(Folder.T, "c36b05e6-1f40-46e3-bbfd-92ddde48d94a");
		bean.setParent(folder_156());
		bean.setDisplayName(localizedString_453());
		bean.setName("Read and Import");
		bean.setIcon(adaptiveIcon_66());
		bean.setContent(templateServiceRequestAction_41());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_453() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bc9628cd-5a5f-47cb-b87b-897839d407b4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read & Import")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_41() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "c84269a9-c1ee-4eb4-a43e-38410bbc0b30");
		bean.setTemplate(template_96());
		bean.setDisplayName(localizedString_454());
		bean.setInplaceContextCriterion(conjunctionCriterion_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_454() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2b72b345-f703-4d9d-83aa-b5a3bfbc92af");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read and Import")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_40() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "141192e0-6a88-4bc0-ad96-29773d98948d");
		bean.setTypeCondition(isAssignableTo_36());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_36() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "dfe8baa7-8f6e-46bd-bb8a-508baff94c4e");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private Template template_96() {
		Template bean = session().createRaw(Template.T, "cec475a0-c626-405a-973a-21c08150ab95");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_19(), dynamicPropertyMetaDataAssignment_18()));
		bean.setName(localizedString_455());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.ReadFromResourceAndImport");
		bean.setTechnicalName("ReadFromResourceAndImportTemplate");
		bean.setPrototype(readFromResourceAndImport_1());
		bean.setScript(compoundManipulation_83());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_455() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "32982155-6153-4ee6-8995-a129bec1bef1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read & Import")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_456() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "69a4eee0-cf4e-47f4-aba3-e575e7053bdd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Shallow Instance For Missing References")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_457() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3b03fd0e-b77c-4a73-9560-c7c02decae13");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Include Envelope")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_458() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "583596d0-7efd-480b-be65-3abf9da477cc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Lenient")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_459() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "69bd41c4-a2bf-4c23-a90c-1ee99214fe2a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Requires Global Id")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_460() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6f625cf6-74a5-41d8-8e85-91a42a6536e7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_461() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "78b38a58-99fc-444f-aef4-a1cb615148c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Use System Session")));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_66() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7a0741a6-8485-4395-82bd-00d7a7285b94");
		bean.setName("Import ExchangePackage Icon");
		bean.setRepresentations(Sets.set(resource_181(), resource_191()));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_14() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "8a00632e-1445-44f0-82ff-e5080d313e1e");
		bean.setCriteria(Lists.list(typeConditionCriterion_40(), valueConditionCriterion_16()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_16() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "bee46d9d-6de4-4724-a9b8-1dfc58e235b5");
		bean.setPropertyPath("name");
		bean.setOperator(ComparisonOperator.matches);
		bean.setOperand(".*\\.tfx\\.zip");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_18() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "b858ac57-00e1-4588-9d98-e21f16fd9ee4");
		bean.setMetaData(Sets.set(hidden_3()));
		bean.setVariable(variable_241());
		return bean;
	}

	// Managed
	private Hidden hidden_3() {
		Hidden bean = session().createRaw(Hidden.T, "0532b4fe-77a7-4fd9-b2f6-1f7bc65288ce");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_3());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_3() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "aac6a158-d7c8-4d77-88f8-544f2e3c8322");
		bean.setPropertyPath("resource");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_19() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "9dbdf5dd-832d-4361-9611-016befb2dad1");
		bean.setMetaData(Sets.set(priority_9()));
		bean.setVariable(variable_239());
		return bean;
	}

	// Managed
	private Priority priority_9() {
		Priority bean = session().createRaw(Priority.T, "df1c4694-cda7-445e-9447-adae6d3c89e5");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_37() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "122e8bee-c5d3-4d21-9f4e-15d47f66a136");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_17() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "69190e54-2506-4c6e-90e9-4c8eed9bae99");
		bean.setPropertyPath("id");
		bean.setOperator(ComparisonOperator.notEqual);
		return bean;
	}

	@Managed
	private ConjunctionCriterion conjunctionCriterion_16() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "c6571af4-eda5-416b-928c-9e34652e8989");
		bean.setCriteria(Lists.list(typeConditionCriterion_28(), valueConditionCriterion_17()));
		return bean;
	}

	// Managed
	private TypeConditionConjunction typeConditionConjunction_1() {
		TypeConditionConjunction bean = session().createRaw(TypeConditionConjunction.T, "03a542ee-4362-4f57-942d-cb89b3b33e01");
		bean.setOperands(Lists.list(isAssignableTo_24(), typeConditionNegation_1()));
		return bean;
	}

	// Managed
	private TypeConditionNegation typeConditionNegation_1() {
		TypeConditionNegation bean = session().createRaw(TypeConditionNegation.T, "fcde21f7-7fc7-4609-9a1e-1e2a9271f023");
		bean.setOperand(isAssignableTo_37());
		return bean;
	}

	@Managed
	private WriteToResource writeToResource_1() {
		WriteToResource bean = session().createRaw(WriteToResource.T, "70c313ca-580b-499a-b56b-54319a70aed7");
		bean.setEncodingType(EncodingType.xml);
		bean.setStabilizeOrder(Boolean.TRUE);
		bean.setPrettyOutput(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_84() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "e8e894d8-19d5-46b4-934b-213cd7291aec");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_318(), changeValueManipulation_319(), changeValueManipulation_320(),
				changeValueManipulation_321(), changeValueManipulation_322(), changeValueManipulation_323()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_318() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "0d9eea1f-979c-4d85-9a65-451402efb3d1");
		bean.setOwner(localEntityProperty_223());
		bean.setNewValue(variable_243());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_223() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0a53db11-08a3-4623-bdaf-7fa3f8bd731c");
		bean.setPropertyName("assembly");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_319() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "d531e72d-79b5-4f1e-8b96-0975d8f6102a");
		bean.setOwner(localEntityProperty_224());
		bean.setNewValue(variable_244());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_224() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "8a0ec4b8-d8ed-4fc0-a63b-659b6c86578e");
		bean.setPropertyName("encodingType");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_320() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "fc7584d2-8caa-4282-a823-b3cece4d3e1c");
		bean.setOwner(localEntityProperty_225());
		bean.setNewValue(variable_245());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_225() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b6f1501a-993b-4d9d-92d5-d525e285e94f");
		bean.setPropertyName("prettyOutput");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_321() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "8b0b68d1-d0e9-4bbd-89f4-ef53c0e71453");
		bean.setOwner(localEntityProperty_226());
		bean.setNewValue(variable_246());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_226() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "987904d2-06a9-4d68-a320-8b6a49855ede");
		bean.setPropertyName("resourceBaseName");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_322() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a5480a7b-280e-4020-b62d-572c69fc4474");
		bean.setOwner(localEntityProperty_227());
		bean.setNewValue(variable_247());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_227() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9be7c4dc-912f-43d9-85d5-86aa45d282e2");
		bean.setPropertyName("stabilizeOrder");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_323() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "30945fc3-57ea-4038-88ff-585bcedb8824");
		bean.setOwner(localEntityProperty_228());
		bean.setNewValue(variable_248());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_228() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bc28d267-1b11-44e4-b15c-9772f81d72b9");
		bean.setPropertyName("writeEmptyProperties");
		bean.setEntity(writeToResource_1());
		return bean;
	}

	@Managed
	private Variable variable_243() {
		Variable bean = session().createRaw(Variable.T, "1fa0a640-cc67-425f-8c38-6ca5a5f274d9");
		bean.setLocalizedName(localizedString_465());
		bean.setTypeSignature("list<com.braintribe.model.generic.GenericEntity>");
		bean.setName("assembly");
		return bean;
	}

	// Managed
	private Variable variable_244() {
		Variable bean = session().createRaw(Variable.T, "77afa2e0-9c78-4ecc-97bc-ef02089039e8");
		bean.setLocalizedName(localizedString_466());
		bean.setTypeSignature("com.braintribe.model.exchangeapi.EncodingType");
		bean.setDefaultValue(EncodingType.xml);
		bean.setName("encodingType");
		return bean;
	}

	// Managed
	private Variable variable_245() {
		Variable bean = session().createRaw(Variable.T, "3297333a-122c-41b7-a3ec-f340823e8658");
		bean.setLocalizedName(localizedString_467());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("prettyOutput");
		return bean;
	}

	@Managed
	private Variable variable_246() {
		Variable bean = session().createRaw(Variable.T, "6ec27833-7897-4029-be91-86dc94e428c5");
		bean.setLocalizedName(localizedString_468());
		bean.setTypeSignature("string");
		bean.setName("resourceBaseName");
		return bean;
	}

	// Managed
	private Variable variable_247() {
		Variable bean = session().createRaw(Variable.T, "b2ae9216-4a29-4bf3-a711-2d1f7c9ef638");
		bean.setLocalizedName(localizedString_469());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("stabilizeOrder");
		return bean;
	}

	// Managed
	private Variable variable_248() {
		Variable bean = session().createRaw(Variable.T, "4fc37dd7-7187-4e3d-b0ac-b147b50b6f32");
		bean.setLocalizedName(localizedString_470());
		bean.setTypeSignature("boolean");
		bean.setName("writeEmptyProperties");
		return bean;
	}

	// Managed
	private Folder folder_159() {
		Folder bean = session().createRaw(Folder.T, "2ddf5681-d497-42a6-96c8-a4fadd744a57");
		bean.setParent(folder_153());
		bean.setDisplayName(localizedString_462());
		bean.setName("Write to Resource");
		bean.setIcon(adaptiveIcon_56());
		bean.setContent(templateServiceRequestAction_42());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_462() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f020737f-3b58-4415-a640-b4eafd3e2b67");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Write to Resource")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_42() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "b5852d70-e50c-4220-89c9-0d267816d4ef");
		bean.setTemplate(template_97());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_463());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_42());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_463() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e21d100c-f627-4ea7-b905-078a980e89b9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Write to Resource")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_42() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "8886fd0e-a99e-4eca-8866-771b8beaadbe");
		bean.setTypeCondition(typeConditionConjunction_2());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_38() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "d42e401a-1fb7-43bf-8eb2-f26721e6ae10");
		bean.setTypeSignature("com.braintribe.model.generic.GenericEntity");
		return bean;
	}

	// Managed
	private Template template_97() {
		Template bean = session().createRaw(Template.T, "5004c2be-0ca0-4e3f-9c90-50bfcc5d98ab");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_20(), dynamicPropertyMetaDataAssignment_21()));
		bean.setName(localizedString_464());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.WriteToResource");
		bean.setTechnicalName("WriteToResourceTemplate");
		bean.setPrototype(writeToResource_1());
		bean.setScript(compoundManipulation_84());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_464() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6a915223-84ca-4b3a-9e42-d6893e34d56a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Write to Resource")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_465() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bd572bae-b0ba-4137-a467-ca3b51feaf50");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assembly")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_466() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ceb9cca3-3f50-4b5b-a904-6bce81a28f92");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Encoding Type")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_467() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c79333e7-c2e5-41ca-a02e-635c28e53539");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Pretty Output")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_468() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "35c666a3-46a9-4906-88d5-1f0d021ac23a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource Base Name")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_469() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "422dd346-806d-4dc7-9d88-ddb2a1dffb7e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Stabilize Order")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_470() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0289c0d3-ad4c-47a0-ba33-63214d0141c4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Write Empty Properties")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_20() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "0770dd2b-15be-4989-a48b-4a817c07aaf0");
		bean.setMetaData(Sets.set(priority_10()));
		bean.setVariable(variable_246());
		return bean;
	}

	// Managed
	private Priority priority_10() {
		Priority bean = session().createRaw(Priority.T, "feb273db-e38f-4b2e-8ee1-2ebcb24cf70d");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_21() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "aeb70bda-d13c-434c-aaea-65ab735b79a7");
		bean.setMetaData(Sets.set(hidden_4()));
		bean.setVariable(variable_243());
		return bean;
	}

	// Managed
	private Hidden hidden_4() {
		Hidden bean = session().createRaw(Hidden.T, "ce357f56-08ce-42e0-aa7d-21a90f09a7d7");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_4());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_4() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "dcf9fde6-4a45-4c04-a9b2-0c27adba217e");
		bean.setPropertyPath("assembly");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	// Managed
	private TypeConditionConjunction typeConditionConjunction_2() {
		TypeConditionConjunction bean = session().createRaw(TypeConditionConjunction.T, "ff84d32f-ce36-4fb5-84be-5fb5f82200a7");
		bean.setOperands(Lists.list(isAssignableTo_38(), typeConditionNegation_2()));
		return bean;
	}

	// Managed
	private TypeConditionNegation typeConditionNegation_2() {
		TypeConditionNegation bean = session().createRaw(TypeConditionNegation.T, "c8a1ce38-4b1e-44a6-80f4-d85fc5f1c434");
		bean.setOperand(isAssignableTo_39());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_39() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "23cc0905-80bd-4ee9-807a-3fa2b3fd7896");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	@Managed
	private com.braintribe.model.exchangeapi.Import import_1() {
		com.braintribe.model.exchangeapi.Import bean = session().createRaw(com.braintribe.model.exchangeapi.Import.T,
				"faa9b916-3e3f-403e-a1dd-bf9d69ec9f6b");
		bean.setRequiresGlobalId(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_85() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "f9664620-83b4-4835-adb3-d94521f8f255");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_324(), changeValueManipulation_325(), changeValueManipulation_326(),
				changeValueManipulation_327(), changeValueManipulation_328()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_324() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "2a363288-3ec5-41c1-8d98-9c79779d47fc");
		bean.setOwner(localEntityProperty_229());
		bean.setNewValue(variable_249());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_229() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0eb3a5b6-9859-451a-bf85-8681cc42f4d1");
		bean.setPropertyName("exchangePackage");
		bean.setEntity(import_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_325() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "06e5f643-469a-467e-8776-46a79e2164f1");
		bean.setOwner(localEntityProperty_230());
		bean.setNewValue(variable_250());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_230() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a088e7a0-b9aa-413a-a2d5-e957a161e794");
		bean.setPropertyName("createShallowInstanceForMissingReferences");
		bean.setEntity(import_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_326() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "86ca0837-b630-4c5b-90ff-8fc3262f1f3a");
		bean.setOwner(localEntityProperty_231());
		bean.setNewValue(variable_251());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_231() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "5ebe53fb-f6c3-4c6d-8180-dfca999256be");
		bean.setPropertyName("includeEnvelope");
		bean.setEntity(import_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_327() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "99cd8fdd-00ed-43a9-b373-1bb03fd8b483");
		bean.setOwner(localEntityProperty_232());
		bean.setNewValue(variable_252());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_232() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b16ffc7a-98d0-45af-8bf7-00599c2fe55a");
		bean.setPropertyName("requiresGlobalId");
		bean.setEntity(import_1());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_328() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "483f14af-27ed-4d1e-b809-ca630e229737");
		bean.setOwner(localEntityProperty_233());
		bean.setNewValue(variable_253());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_233() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6e080e61-b43e-43c0-bf4b-ff7d2ade6d8e");
		bean.setPropertyName("useSystemSession");
		bean.setEntity(import_1());
		return bean;
	}

	@Managed
	private Variable variable_249() {
		Variable bean = session().createRaw(Variable.T, "483e0733-2129-4480-9b06-71957bedb048");
		bean.setLocalizedName(localizedString_474());
		bean.setTypeSignature("com.braintribe.model.exchange.ExchangePackage");
		bean.setName("exchangePackage");
		return bean;
	}

	// Managed
	private Variable variable_250() {
		Variable bean = session().createRaw(Variable.T, "ddfaf7c4-bf2f-4b5f-92ee-e97ec7043ecc");
		bean.setLocalizedName(localizedString_475());
		bean.setTypeSignature("boolean");
		bean.setName("createShallowInstanceForMissingReferences");
		return bean;
	}

	// Managed
	private Variable variable_251() {
		Variable bean = session().createRaw(Variable.T, "a013b3df-fb42-45c1-aa28-46ab520be932");
		bean.setLocalizedName(localizedString_476());
		bean.setTypeSignature("boolean");
		bean.setName("includeEnvelope");
		return bean;
	}

	// Managed
	private Variable variable_252() {
		Variable bean = session().createRaw(Variable.T, "7f3a2794-9e2e-458a-8b1f-76b8afa6f956");
		bean.setLocalizedName(localizedString_477());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("requiresGlobalId");
		return bean;
	}

	// Managed
	private Variable variable_253() {
		Variable bean = session().createRaw(Variable.T, "929ba043-cb79-43ae-b22e-2c29786143b0");
		bean.setLocalizedName(localizedString_478());
		bean.setTypeSignature("boolean");
		bean.setName("useSystemSession");
		return bean;
	}

	@Managed
	private Folder folder_160() {
		Folder bean = session().createRaw(Folder.T, "8ef362d5-ee77-4f1d-a3b0-c35667649a23");
		bean.setParent(folder_156());
		bean.setDisplayName(localizedString_471());
		bean.setName("Import Package");
		bean.setIcon(adaptiveIcon_66());
		bean.setContent(templateServiceRequestAction_43());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_471() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f151ab7f-eff5-418e-ae3d-b00e86c1f77b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Import")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_43() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "a30feaa0-9650-4c21-abef-a1a74dada04e");
		bean.setTemplate(template_98());
		bean.setDisplayName(localizedString_472());
		bean.setInplaceContextCriterion(typeConditionCriterion_43());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_472() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "427051a7-2df1-41ae-965d-7ed30b3d3b8b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Import Package")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_43() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "a3df1ec5-e995-4551-815e-4afcf16261e9");
		bean.setTypeCondition(isAssignableTo_40());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_40() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "bfb4753e-fbcf-4656-ba87-df72a238e950");
		bean.setTypeSignature("com.braintribe.model.exchange.ExchangePackage");
		return bean;
	}

	// Managed
	private Template template_98() {
		Template bean = session().createRaw(Template.T, "06b3a4f5-c82c-4777-8d6b-50c0523672c5");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_22()));
		bean.setName(localizedString_473());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.Import");
		bean.setTechnicalName("ImportTemplate");
		bean.setPrototype(import_1());
		bean.setScript(compoundManipulation_85());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_473() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5ca81df9-7de4-45d7-b419-e4a5ea53a6c6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Import")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_474() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ea79ce5d-5dcf-4163-9356-cdb4629c5402");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange Package")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_475() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "de2d37cf-b1d1-4108-8edc-ab6261c75aa4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Create Shallow Instance For Missing References")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_476() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "56af0b08-2467-472f-859e-8f260fde8b98");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Include Envelope")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_477() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4a5cbff0-131c-45a5-968a-c7178ebf39c0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Requires Global Id")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_478() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "91102a6a-ed5e-4a9d-8b49-7c314dd9bd3b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Use System Session")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_22() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "f14fc35c-8f25-4f83-9b37-cdf5d29f2f66");
		bean.setMetaData(Sets.set(hidden_5()));
		bean.setVariable(variable_249());
		return bean;
	}

	// Managed
	private Hidden hidden_5() {
		Hidden bean = session().createRaw(Hidden.T, "472c0128-0bd3-4c32-8082-2457f6ade3fe");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_5());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_5() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "07c15951-99b8-4b97-ae81-64c1847a0dcd");
		bean.setPropertyPath("exchangePackage");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_23() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "adba66d5-2a8f-4987-a0c5-c9b1ad659372");
		bean.setMetaData(Sets.set(hidden_6()));
		bean.setVariable(variable_229());
		return bean;
	}

	// Managed
	private Hidden hidden_6() {
		Hidden bean = session().createRaw(Hidden.T, "307db69d-7357-4044-95c5-be8ebfcd15a6");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_6());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_6() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "e5b0cf54-a8d2-4642-9b1b-f1679c9765a1");
		bean.setPropertyPath("entities");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_24() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "0db644dd-dbf6-4885-a8c3-14b22b1add4d");
		bean.setMetaData(Sets.set(hidden_7()));
		bean.setVariable(variable_217());
		return bean;
	}

	// Managed
	private Hidden hidden_7() {
		Hidden bean = session().createRaw(Hidden.T, "4393b001-52c1-469a-9633-bf970d0adc5e");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_7());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_7() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "6b0acd54-847d-406d-9d8a-0eeabe43e765");
		bean.setPropertyPath("entities");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_479() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "431a918b-8323-4337-b92c-4e289e0df018");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange Packages")));
		return bean;
	}

	// Managed
	private Folder folder_161() {
		Folder bean = session().createRaw(Folder.T, "6a243c35-a72b-40f5-b373-be8062ad95a9");
		bean.setParent(folder_8());
		bean.setDisplayName(localizedString_479());
		bean.setName("Exchange Packages");
		bean.setIcon(adaptiveIcon_68());
		bean.setContent(templateQueryAction_21());
		return bean;
	}

	// Managed
	private Template template_99() {
		Template bean = session().createRaw(Template.T, "98a5ef0a-1880-48d9-82bc-2edd1ae82b5a");
		bean.setMetaData(Sets.set(queryString_14(), defaultView_10()));
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		bean.setPrototype(entityQuery_34());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_34() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "349ffa42-007e-4415-a71f-7416017a2b5c");
		bean.setOrdering(simpleOrdering_24());
		bean.setRestriction(restriction_27());
		bean.setEntityTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_24() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "5fd23f84-eb1d-4b3f-9a03-3758ca4f20d9");
		bean.setOrderBy(propertyOperand_72());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_72() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "b211ee41-09eb-426b-8a58-a252331dbc0b");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_27() {
		Restriction bean = session().createRaw(Restriction.T, "5da74e2a-8222-4b4b-a349-40a730e38d9f");
		bean.setCondition(valueComparison_50());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_50() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "8d10814e-8497-4a36-bd4d-404e536d326f");
		bean.setLeftOperand(propertyOperand_73());
		bean.setRightOperand("*.tfx.zip");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_73() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9bdb5348-040c-46e5-bc7f-cc3098cb5918");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private DefaultView defaultView_10() {
		DefaultView bean = session().createRaw(DefaultView.T, "d85e02ba-8619-4052-8151-6541604db8e1");
		bean.setViewIdentification("List");
		return bean;
	}

	// Managed
	private QueryString queryString_14() {
		QueryString bean = session().createRaw(QueryString.T, "d6401a5f-1a99-47a5-b8c6-727d00dfc175");
		bean.setValue("from Resource r where r.name like '*.tfx.zip' order by r.name");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_21() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "038372a3-5c0e-4c81-aae3-90eb2d0a299e");
		bean.setTemplate(template_99());
		return bean;
	}

	// Managed
	private Resource resource_267() {
		Resource bean = session().createRaw(Resource.T, "d8c48657-e700-4dae-8f75-efc92443875b");
		bean.setCreator("cortex");
		bean.setFileSize(1063l);
		bean.setCreated(newGmtDate(2017, 8, 14, 13, 33, 51, 174));
		bean.setName("TFX-Resource_16x16.png");
		bean.setSpecification(rasterImageSpecification_383());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_23f());
		bean.setMd5("bc2068719b5e683519d0311f95cce315");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_23f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "364ec802-42b6-4ec3-81af-c49f6570710f");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/1415/3351/2245d90f-67d1-4503-b26f-543042a2c1ef");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_383() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "38774c28-592c-4c5a-87a5-34ffefd3b045");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_384() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "0196a20d-3e97-4c84-bcd7-b3a74e0ff39a");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_268() {
		Resource bean = session().createRaw(Resource.T, "aa21ec80-5cad-48d7-9480-0b39c9320eb2");
		bean.setCreator("cortex");
		bean.setFileSize(1932l);
		bean.setCreated(newGmtDate(2017, 8, 14, 13, 33, 51, 433));
		bean.setName("TFX-Resource_32x32.png");
		bean.setSpecification(rasterImageSpecification_384());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_24f());
		bean.setMd5("c7e7c26f9490dcf87691749b6aa4a355");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_24f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "3be28a7d-cd88-4327-9498-557ce348b53e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/1415/3351/6e74e1a2-ab31-41c0-a417-b89852359f8b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_385() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "445f377f-eb20-46a6-a136-21b5a3432f31");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_269() {
		Resource bean = session().createRaw(Resource.T, "366c056b-af50-4f2e-b826-29b0d251d02a");
		bean.setCreator("cortex");
		bean.setFileSize(2094l);
		bean.setCreated(newGmtDate(2017, 8, 14, 13, 33, 51, 449));
		bean.setName("TFX-Resource_64x64.png");
		bean.setSpecification(rasterImageSpecification_385());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_25f());
		bean.setMd5("01d6b36d46abf4c2f296b7c8c6434463");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_25f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "64b67ab9-7c50-446e-9cc0-5b1b1b9c5f9e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/1415/3351/0b8ed416-fc81-4076-8008-17497300da7a");
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_68() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "333b2901-4195-4144-bdd7-918451afc3b6");
		bean.setName("ExchangePackage Icon");
		bean.setRepresentations(Sets.set(resource_268(), resource_267(), resource_269()));
		return bean;
	}

	@Managed
	private ReadFromResource readFromResource_2() {
		ReadFromResource bean = session().createRaw(ReadFromResource.T, "4f590495-650b-4a1b-8f85-621c2f96af2c");
		bean.setEncodingType(EncodingType.xml);
		bean.setLenient(Boolean.TRUE);
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_86() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "80baacc7-5ef8-4837-acfb-6ebd91c7e13b");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_330(), changeValueManipulation_331()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_330() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "38540201-2412-4c2b-9cf2-299df230df07");
		bean.setOwner(localEntityProperty_235());
		bean.setNewValue(variable_255());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_235() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "e1d3259e-d63c-462f-88d3-379e723ad085");
		bean.setPropertyName("lenient");
		bean.setEntity(readFromResource_2());
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_331() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "bfbb2cd9-b25d-4569-a9dc-7796315d40d7");
		bean.setOwner(localEntityProperty_236());
		bean.setNewValue(variable_256());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_236() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "7546659b-84b3-4332-89a8-5bc0b10653bb");
		bean.setPropertyName("resource");
		bean.setEntity(readFromResource_2());
		return bean;
	}

	// Managed
	private Variable variable_255() {
		Variable bean = session().createRaw(Variable.T, "ee6f0f0b-5a78-4251-b615-6bb07d9ed358");
		bean.setLocalizedName(localizedString_485());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("lenient");
		return bean;
	}

	@Managed
	private Variable variable_256() {
		Variable bean = session().createRaw(Variable.T, "089b2ebc-fb2a-4c9a-a1a5-106b9bb3da8f");
		bean.setLocalizedName(localizedString_486());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		bean.setName("resource");
		return bean;
	}

	// Managed
	private Folder folder_162() {
		Folder bean = session().createRaw(Folder.T, "67cc7dc5-42b3-4677-b438-44049fe49373");
		bean.setParent(folder_35());
		bean.setSubFolders(Lists.list(folder_163(), folder_158()));
		bean.setDisplayName(localizedString_480());
		bean.setName("import");
		bean.setIcon(adaptiveIcon_68());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_480() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "39e955a5-8f55-465f-93c7-b1c81e1efd0f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Import")));
		return bean;
	}

	@Managed
	private Folder folder_163() {
		Folder bean = session().createRaw(Folder.T, "27205021-4234-4633-8000-c7fb5435f0ea");
		bean.setParent(folder_162());
		bean.setDisplayName(localizedString_481());
		bean.setName("Read");
		bean.setIcon(adaptiveIcon_66());
		bean.setContent(templateServiceRequestAction_44());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_481() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b60536da-1161-4f42-b412-5dab56f3cdd3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_44() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "977b2e99-eeea-4ad4-b95b-a181883d5b7d");
		bean.setTemplate(template_100());
		bean.setDisplayName(localizedString_482());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_17());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_482() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "eb340674-bb0b-4ffc-a61d-ffd168984632");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_44() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "00dd5f58-a603-4f64-b7f3-5832c5edbe4a");
		bean.setTypeCondition(isAssignableTo_41());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_41() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "4eadfbdd-c22c-4324-8a2a-e32698dc5136");
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private Template template_100() {
		Template bean = session().createRaw(Template.T, "ae3b4b53-f6d7-4f5f-b1ae-cac933535613");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_25()));
		bean.setName(localizedString_483());
		bean.setPrototypeTypeSignature("com.braintribe.model.exchangeapi.ReadFromResource");
		bean.setTechnicalName("ReadFromResourceTemplate");
		bean.setPrototype(readFromResource_2());
		bean.setScript(compoundManipulation_86());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_483() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2a407542-d802-41ac-9693-6e25ebae8127");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Read")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_485() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "40b32f2f-1681-4a70-a7ce-b9b200ffe511");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Lenient")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_486() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9bbf64ee-414e-4b4d-a678-c44bd280383a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resource")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_17() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "7ad3bf6e-8b27-4ce5-83e6-23093f70f33e");
		bean.setCriteria(Lists.list(typeConditionCriterion_44(), valueConditionCriterion_18()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_18() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "ca675739-0e2e-439d-bff3-2319959013c7");
		bean.setPropertyPath("name");
		bean.setOperator(ComparisonOperator.matches);
		bean.setOperand(".*\\.tfx\\.zip");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_25() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "974f6d28-8f1d-40fd-ba9a-bfd000bbed0e");
		bean.setMetaData(Sets.set(hidden_8()));
		bean.setVariable(variable_256());
		return bean;
	}

	// Managed
	private Hidden hidden_8() {
		Hidden bean = session().createRaw(Hidden.T, "fe9d9e75-ca44-4bdc-b700-0e887a29c467");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_8());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_8() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "549c7232-464d-4b81-93be-8e6f65cd0e8f");
		bean.setPropertyPath("resource");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		return bean;
	}

	@Managed
	private GetDeploymentStatusNotification getDeploymentStatusNotification_1() {
		GetDeploymentStatusNotification bean = session().createRaw(GetDeploymentStatusNotification.T, "ffefb201-c8a3-44c8-b2fb-bf5879faccae");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_332() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "f913329a-2d97-4ac5-9c8e-5352e71533e3");
		bean.setOwner(localEntityProperty_237());
		bean.setNewValue(variable_257());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_237() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "27693973-8ab8-4625-8c02-c8c08e8c87a2");
		bean.setPropertyName("deployables");
		bean.setEntity(getDeploymentStatusNotification_1());
		return bean;
	}

	// Managed
	private Variable variable_257() {
		Variable bean = session().createRaw(Variable.T, "6f9cf33e-2a78-4754-a9c9-a3b570fd1624");
		bean.setLocalizedName(localizedString_490());
		bean.setTypeSignature("list<com.braintribe.model.deployment.Deployable>");
		bean.setName("deployables");
		return bean;
	}

	// Managed
	private Folder folder_164() {
		Folder bean = session().createRaw(Folder.T, "2491ebdf-fec8-44f2-b34a-daf05daa98df");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_487());
		bean.setName("Deployment Status");
		bean.setIcon(adaptiveIcon_69());
		bean.setContent(templateServiceRequestAction_45());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_487() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3fc130cf-fdcd-4338-b1c0-e4114de1492e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deployment Status")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_45() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "0f8c49b3-0ccb-447a-a777-83d2321697cb");
		bean.setTemplate(template_101());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setDisplayName(localizedString_488());
		bean.setInplaceContextCriterion(typeConditionCriterion_45());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_488() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "714472cf-6513-4c74-b2fc-359fdf7b5084");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deployment Status")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_45() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "b1828078-1ed5-4e12-b381-a1ad61fd24b9");
		bean.setTypeCondition(isAssignableTo_42());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_42() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "dc6cc996-6c77-47d6-814a-0f3e2a9caf00");
		bean.setTypeSignature("com.braintribe.model.deployment.Deployable");
		return bean;
	}

	// Managed
	private Template template_101() {
		Template bean = session().createRaw(Template.T, "03dc3a57-74f9-4f26-aff6-4d1fbe605f8d");
		bean.setName(localizedString_489());
		bean.setPrototypeTypeSignature("com.braintribe.model.deploymentreflection.request.GetDeploymentStatusNotification");
		bean.setTechnicalName("GetDeploymentStatusNotificationTemplate");
		bean.setPrototype(getDeploymentStatusNotification_1());
		bean.setScript(changeValueManipulation_332());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_489() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "34c02ded-bdf1-41f1-9f73-06305068f2a3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "GetDeploymentStatusNotification Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_490() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8b4cf4fa-8999-49c9-9177-4f631edbedfc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deployables")));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_28f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e53fdcbc-5300-4159-a2fd-51755ff29cbb");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/2511/2923/1e2e2e51-cda3-4131-aad9-8e9a3307ccff");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_388() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b6c1ec55-4a00-4c52-ab72-5cecd6bd4b4d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_272() {
		Resource bean = session().createRaw(Resource.T, "6b1b4866-95ea-4ecd-9782-15e35d983bc0");
		bean.setCreator("cortex");
		bean.setFileSize(382l);
		bean.setCreated(newGmtDate(2017, 8, 25, 9, 29, 23, 428));
		bean.setName("infoIcon_16x16.png");
		bean.setSpecification(rasterImageSpecification_388());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_28f());
		bean.setMd5("1332900924f97c4fc9e740c2fad29ddc");
		return bean;
	}

	// Managed
	private Resource resource_273() {
		Resource bean = session().createRaw(Resource.T, "4d5ecffa-b1f6-489b-98f7-1de0e7b5a551");
		bean.setCreator("cortex");
		bean.setFileSize(770l);
		bean.setCreated(newGmtDate(2017, 8, 25, 9, 29, 59, 677));
		bean.setName("infoIcon_32x32.png");
		bean.setSpecification(rasterImageSpecification_389());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_29f());
		bean.setMd5("d2e777c5bf604f1dd1f48c7fa4fd7515");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_29f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "91ff04d5-6eab-4ce6-b20d-7607e9c7ef48");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/2511/2959/fe3b0ba3-6d1e-41da-b7bd-d9f96193ae33");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_389() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b4813b87-bba9-4a22-bf05-0630fe70a948");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_390() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a91f88a4-a05a-4600-92cc-d8a90fbd2cc0");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_30f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "020844fb-318f-4f86-97bd-2f50641d51af");
		bean.setModuleName(currentModuleName());
		bean.setPath("1709/2511/3008/fa99e4c3-0a3d-4775-95b5-84044fc0fa4d");
		return bean;
	}

	// Managed
	private Resource resource_274() {
		Resource bean = session().createRaw(Resource.T, "92c69cb2-441c-4b7e-812c-516ee0a3aaed");
		bean.setCreator("cortex");
		bean.setFileSize(1606l);
		bean.setCreated(newGmtDate(2017, 8, 25, 9, 30, 8, 240));
		bean.setName("infoIcon_64x64.png");
		bean.setSpecification(rasterImageSpecification_390());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_30f());
		bean.setMd5("848e9eff3960fa31269eebe16ec3292e");
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_69() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "e0c4545e-f61b-4c2e-b7f8-302bc5f98f61");
		bean.setName("GetDeploymentStatusIcon");
		bean.setRepresentations(Sets.set(resource_274(), resource_273(), resource_272()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_491() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6635bafd-826b-411a-847e-0fff136a99bb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Engines Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_492() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "02942343-50ee-4566-931b-ce6df8c4a3a6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Definitions Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_493() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "68b8588d-c044-4a90-9d74-63ba47fd7652");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transition Processors Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_494() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3f78a8b7-7e11-4f64-b744-0f04e2f17a90");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Conditions Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_495() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "46a2b629-8ef2-490c-8e95-fafa682cea32");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Workers Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_496() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "57ab5e38-2b68-4bc8-8e99-84640ae1a241");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Apps Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_497() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a42784e3-0859-4775-9044-f2db447a6949");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Service Processors Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_498() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "39bf02de-0eb2-45e8-a66b-00ed364ec88c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connections Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_499() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fde55009-bbb0-4392-a4c8-5d322e31bd99");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System Deployables Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_500() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "220fdc41-e964-454b-9ce9-f9e77dc2c226");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Deployables Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_501() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "48d11b03-b3c8-4710-9360-d9bf15b6aadd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Enum Types Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_502() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fd072072-eeda-4c8c-9710-ca7a4ba43c3b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entity Types Query")));
		return bean;
	}

	@Managed
	private ExportDescriptor exportDescriptor_2() {
		ExportDescriptor bean = session().createRaw(ExportDescriptor.T, "7a7fe447-4838-474b-a94d-c66c9975bfc3");
		bean.setEntitySupplier(staticSupplier_1());
		bean.setShallowifyingPredicate(defaultPredicate_1());
		bean.setSkipId(Boolean.TRUE);
		bean.setSkipPartition(Boolean.TRUE);
		return bean;
	}

	@Managed
	private ExportAndWriteToResource exportAndWriteToResource_1() {
		ExportAndWriteToResource bean = session().createRaw(ExportAndWriteToResource.T, "1517feaf-d160-4afc-94a5-584c0ff7d86e");
		bean.setName("export");
		bean.setStabilizeOrder(Boolean.TRUE);
		bean.setDescriptor(exportDescriptor_2());
		bean.setPrettyOutput(Boolean.TRUE);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_503() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2136fd16-7c10-433d-9d9a-04cc50b08802");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add Resource Binaries")));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_333() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "a3694804-52b8-4efc-94e1-a8e916989a73");
		bean.setOwner(localEntityProperty_238());
		bean.setNewValue(variable_258());
		return bean;
	}

	@Managed
	private Variable variable_258() {
		Variable bean = session().createRaw(Variable.T, "b77f61f7-ec6c-4a9e-bb76-4a1c8c82e81b");
		bean.setLocalizedName(localizedString_503());
		bean.setTypeSignature("boolean");
		bean.setDefaultValue(Boolean.TRUE);
		bean.setName("addResourceBinaries");
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_238() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "87ad2df4-20f9-45e9-b589-b318d2a9ebda");
		bean.setPropertyName("addResourceBinaries");
		bean.setEntity(exportAndWriteToResource_1());
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_26() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "032603ab-0a06-4809-9112-e928b5bf9011");
		bean.setMetaData(Sets.set(priority_11()));
		bean.setVariable(variable_258());
		return bean;
	}

	// Managed
	private Priority priority_11() {
		Priority bean = session().createRaw(Priority.T, "6954f691-fb12-40a9-862a-97a239535d6c");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(-1.0d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_27() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "241be826-5486-4cf9-9a0e-041a8ef9a436");
		bean.setMetaData(Sets.set(priority_12()));
		bean.setVariable(variable_228());
		return bean;
	}

	// Managed
	private Priority priority_12() {
		Priority bean = session().createRaw(Priority.T, "99af38ce-adcc-4547-ba6e-df71ffa68816");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.9d);
		return bean;
	}

	@Managed
	private AddToCortexModel addToCortexModel_1() {
		AddToCortexModel bean = session().createRaw(AddToCortexModel.T, "550868ce-d766-4da7-b40a-4e836504a73b");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_335() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c9185518-734a-483c-9074-60ac4cac2916");
		bean.setOwner(localEntityProperty_239());
		bean.setNewValue(variable_259());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_239() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "7edcba92-1536-4eea-9914-3fa8489a29b8");
		bean.setPropertyName("models");
		bean.setEntity(addToCortexModel_1());
		return bean;
	}

	// Managed
	private Variable variable_259() {
		Variable bean = session().createRaw(Variable.T, "0bf5833e-dc1c-4999-ab49-f4995ef0b3e6");
		bean.setLocalizedName(localizedString_508());
		bean.setTypeSignature("list<com.braintribe.model.meta.GmMetaModel>");
		bean.setName("models");
		return bean;
	}

	// Managed
	private Folder folder_165() {
		Folder bean = session().createRaw(Folder.T, "74ab98b1-2520-4c4b-9ed0-16bca38dd183");
		bean.setParent(folder_56());
		bean.setDisplayName(localizedString_505());
		bean.setName("Add to CortexModel");
		bean.setIcon(adaptiveIcon_65());
		bean.setContent(templateServiceRequestAction_46());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_505() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "88a74885-ec3e-434d-a26c-913a57cd8c6b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add to CortexModel")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_46() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "76f0558d-aff9-4f71-8dc3-ce82d64e4196");
		bean.setTemplate(template_102());
		bean.setDisplayName(localizedString_506());
		bean.setInplaceContextCriterion(typeConditionCriterion_46());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_506() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b48be488-e2e9-4206-af01-48868a95a81e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add to CortexModel")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_46() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "5ce74d30-cf2b-4685-b3b9-906a49e3249a");
		bean.setTypeCondition(isAssignableTo_43());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_43() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "db62c36a-1b5e-4b09-8b2a-e5f34bf23c4a");
		bean.setTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private Template template_102() {
		Template bean = session().createRaw(Template.T, "b990caa9-bc50-41ca-8337-49362986383a");
		bean.setName(localizedString_507());
		bean.setPrototypeTypeSignature("com.braintribe.model.cortexapi.model.AddToCortexModel");
		bean.setTechnicalName("AddToCortexModelTemplate");
		bean.setPrototype(addToCortexModel_1());
		bean.setScript(changeValueManipulation_335());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_507() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "246246c9-0441-4f45-a13e-0c45ed70bc44");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "AddToCortexModel Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_508() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c2f6d68d-e66d-45d4-bfd4-4f7f70c784f9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Models")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_4() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "91ec6d43-5a9c-403e-998a-ed0462d67829");
		bean.setFolders(Lists.list(folder_150()));
		bean.setDisplayName(localizedString_509());
		bean.setName("globalActionBar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_509() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "139bcc48-fe1e-4a57-ae6c-484ac605ecc3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Action Bar")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_28() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "ea5cbdc1-5e05-4376-b81f-f9ac9122d698");
		bean.setMetaData(Sets.set(mandatory_1(), priority_5()));
		bean.setVariable(variable_213());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_510() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8cdf889e-168d-4637-a435-5cc06d31b23a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Name")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_35() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "12fe030e-36df-409e-b068-6e8bb5c1bf91");
		bean.setOrdering(simpleOrdering_25());
		bean.setRestriction(restriction_28());
		bean.setEntityTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_25() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "00399170-1daa-4e0a-9159-5f1e4212afb4");
		bean.setOrderBy(propertyOperand_74());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_74() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "19b418b5-f130-4bd7-b329-81e5100895e8");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_28() {
		Restriction bean = session().createRaw(Restriction.T, "7a82f42e-6702-4f2c-81d5-c8820e69b1e3");
		bean.setCondition(disjunction_15());
		return bean;
	}

	// Managed
	private Disjunction disjunction_15() {
		Disjunction bean = session().createRaw(Disjunction.T, "d5ade5a9-17bb-4048-94cd-317e587b6dab");
		bean.setOperands(Lists.list(valueComparison_51(), valueComparison_52()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_51() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "2fbfd14a-5ae1-439f-9d1c-d022dd163dcf");
		bean.setLeftOperand(propertyOperand_75());
		bean.setRightOperand("tribefire.cortex*");
		bean.setOperator(Operator.ilike);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_75() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "7d849c79-80f8-4a76-b146-a461ac038f35");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_52() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "7b53f3b8-053f-453b-bc5d-94932bc82392");
		bean.setLeftOperand(propertyOperand_76());
		bean.setRightOperand("com.braintribe*");
		bean.setOperator(Operator.ilike);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_76() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "fa402ae7-690a-4716-8f4c-1991a19a65c5");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_36() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "e0518faa-a0d0-43c8-a941-5708265410dd");
		bean.setOrdering(simpleOrdering_26());
		bean.setRestriction(restriction_29());
		bean.setEntityTypeSignature("com.braintribe.model.meta.GmMetaModel");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_26() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "1329a8a8-e8e0-4381-9783-a5a035dc4718");
		bean.setOrderBy(propertyOperand_77());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_77() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "f05bef4f-8da8-4970-bbe7-8dd7d4d4a981");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_29() {
		Restriction bean = session().createRaw(Restriction.T, "85c5353c-c42e-48f9-9f4c-a1b9b1725267");
		bean.setCondition(negation_13());
		return bean;
	}

	// Managed
	private Negation negation_13() {
		Negation bean = session().createRaw(Negation.T, "3f45a41e-3ae1-4c8a-9d97-6f2c7501f46a");
		bean.setOperand(disjunction_16());
		return bean;
	}

	// Managed
	private Disjunction disjunction_16() {
		Disjunction bean = session().createRaw(Disjunction.T, "8e3e9aee-1b84-4438-baad-e1763ccebba7");
		bean.setOperands(Lists.list(valueComparison_53(), valueComparison_54()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_53() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "1fd0e188-b37c-4482-ac2e-f45cad57e635");
		bean.setLeftOperand(propertyOperand_78());
		bean.setRightOperand("tribefire.cortex*");
		bean.setOperator(Operator.ilike);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_78() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e6b4ccba-65cd-4d30-b26d-6c4fe1775c9b");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_54() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "263faa31-b80e-4c27-bd73-5d8959489cc6");
		bean.setLeftOperand(propertyOperand_79());
		bean.setRightOperand("com.braintribe*");
		bean.setOperator(Operator.ilike);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_79() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "f58d91f9-a614-45f5-97b0-05de432cf434");
		bean.setPropertyName("name");
		return bean;
	}

	@Managed
	private Folder folder_166() {
		Folder bean = session().createRaw(Folder.T, "26f5abf0-7972-4bbf-b8a5-7410d348ebb1");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_167()));
		bean.setDisplayName(localizedString_511());
		bean.setName("General");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_511() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a703c78b-f45d-4e48-834c-d5ddf499ce6d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "General")));
		return bean;
	}

	@Managed
	private Folder folder_167() {
		Folder bean = session().createRaw(Folder.T, "08d632ab-9da4-4994-8d19-26ac646ff3d9");
		bean.setParent(folder_166());
		bean.setDisplayName(localizedString_512());
		bean.setName("Cortex Configuration");
		bean.setIcon(adaptiveIcon_32());
		bean.setContent(simpleQueryAction_15());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_512() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5eff262b-5a2d-48a9-a34c-f9ee820bfa6a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Cortex Configuration")));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_15() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "f14600cd-233b-4991-95d5-104374e970b2");
		bean.setTypeSignature("com.braintribe.model.cortex.deployment.CortexConfiguration");
		bean.setDisplayName(localizedString_513());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_513() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c6f9335c-1d0b-4bf0-86ed-b2cbce60b38e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Cortex Configuration")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_29() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "e08c44d9-1754-43f0-9c9d-e77cb9748ebc");
		bean.setMetaData(Sets.set(priority_13()));
		bean.setVariable(variable_23());
		return bean;
	}

	// Managed
	private Priority priority_13() {
		Priority bean = session().createRaw(Priority.T, "2f075115-4c8c-4c77-b7c1-d00cae9f9e9c");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	@Managed
	private Folder folder_168() {
		Folder bean = session().createRaw(Folder.T, "ea85bcdf-9a38-4b96-b5e3-1d04e0b402cb");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_169(), folder_170(), folder_171(), folder_172()));
		bean.setDisplayName(localizedString_514());
		bean.setName("Checks");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_514() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "52471b14-d96e-4d01-96de-00f2542e5ce7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Checks")));
		return bean;
	}

	// Managed
	private Folder folder_169() {
		Folder bean = session().createRaw(Folder.T, "f2a7dcbe-99f1-441d-b761-ea4e3f5633a4");
		bean.setParent(folder_168());
		bean.setDisplayName(localizedString_515());
		bean.setName("All Checks");
		bean.setIcon(adaptiveIcon_71());
		bean.setContent(simpleQueryAction_16());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_515() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "326237aa-0f14-407e-844c-0e04121e30f3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Checks")));
		return bean;
	}

	// Managed
	private Folder folder_170() {
		Folder bean = session().createRaw(Folder.T, "0de00c8e-bc34-4613-a6c1-5bd5fd38913d");
		bean.setParent(folder_168());
		bean.setDisplayName(localizedString_516());
		bean.setName("Vitality Checks");
		bean.setIcon(adaptiveIcon_70());
		bean.setContent(templateQueryAction_22());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_516() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "52327812-5553-4f0e-b83c-89486b33791b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Vitality Checks")));
		return bean;
	}

	// Managed
	private Folder folder_171() {
		Folder bean = session().createRaw(Folder.T, "1fb6a718-1782-41b1-8506-42847d1eaeab");
		bean.setParent(folder_168());
		bean.setDisplayName(localizedString_517());
		bean.setName("Connectivity Checks");
		bean.setIcon(adaptiveIcon_19());
		bean.setContent(templateQueryAction_23());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_517() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2eebe225-448e-498c-8384-cf4ca164168e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connectivity Checks")));
		return bean;
	}

	// Managed
	private Folder folder_172() {
		Folder bean = session().createRaw(Folder.T, "65c9284d-5fb1-4dc8-81c2-bbc648404475");
		bean.setParent(folder_168());
		bean.setDisplayName(localizedString_518());
		bean.setName("Functional Checks");
		bean.setIcon(adaptiveIcon_14());
		bean.setContent(templateQueryAction_24());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_518() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a3cb2304-5ce6-497a-bf6d-958c03f2709f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Functional Checks")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_70() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "dacba099-edb4-4442-84c9-acc9d22da4ce");
		bean.setName("Vitality Check Icon");
		bean.setRepresentations(Sets.set(resource_275(), resource_277(), resource_276()));
		return bean;
	}

	// Managed
	private Resource resource_275() {
		Resource bean = session().createRaw(Resource.T, "ed304c2c-1c3f-4e9f-a575-86e5bcc46b5f");
		bean.setCreator("cortex");
		bean.setFileSize(614l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 15, 12, 148));
		bean.setName("health-16.png");
		bean.setSpecification(rasterImageSpecification_392());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_31f());
		bean.setMd5("d257e01528ac00f14d0698bd7d34e461");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_31f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a9d6bc87-b486-4556-8299-52f9d3b6a859");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1512/33a5a5e9-dfe4-4a95-89b7-220a0020c00d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_392() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "fa5c48b7-0762-499e-865e-92eee4293b25");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_276() {
		Resource bean = session().createRaw(Resource.T, "914b3417-b25d-405d-9dd1-2f32a9b968f1");
		bean.setCreator("cortex");
		bean.setFileSize(951l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 15, 56, 454));
		bean.setName("health-32.png");
		bean.setSpecification(rasterImageSpecification_393());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_32f());
		bean.setMd5("b22d961026c67ac286d0fbe4ee62f52c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_393() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4cb02770-6855-4239-b51d-b9c3a65c2057");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_32f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1d4e83be-5ced-4e22-9aee-8ada383d1be1");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1556/b9b77a04-143c-426d-94c9-206c1f46d20e");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_396() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d5a6e88d-7a7a-4321-8fd3-8e71665ed626");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_33f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "606347ae-227b-47ba-9faf-c882ee88103e");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1606/ad153114-d400-403b-a61e-1c69848d530c");
		return bean;
	}

	// Managed
	private Resource resource_277() {
		Resource bean = session().createRaw(Resource.T, "ebaf3802-f962-4449-8971-eddaf13b1eaa");
		bean.setCreator("cortex");
		bean.setFileSize(1735l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 16, 6, 698));
		bean.setName("health-64.png");
		bean.setSpecification(rasterImageSpecification_396());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_33f());
		bean.setMd5("efa844f8df2937e34d7e67762afcedd8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_398() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e2718e5a-f7d3-4be3-8fbe-7d7673f8cdb8");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_278() {
		Resource bean = session().createRaw(Resource.T, "139d82ae-f951-48e9-aa03-c0edafa21c01");
		bean.setCreator("cortex");
		bean.setFileSize(491l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 17, 41, 777));
		bean.setName("check-16.png");
		bean.setSpecification(rasterImageSpecification_398());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_34f());
		bean.setMd5("64b98db3dc41cd70c5f0fa4567ea3628");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_34f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "294fd8d3-d0ca-4b7f-a43e-d0c2388fcd16");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1741/6b3244cd-f536-4587-99fc-545f29c506ca");
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_71() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "7e30007a-a74e-48b5-b322-ef642c179919");
		bean.setName("All Checks Icon");
		bean.setRepresentations(Sets.set(resource_279(), resource_280(), resource_278()));
		return bean;
	}

	// Managed
	private Resource resource_279() {
		Resource bean = session().createRaw(Resource.T, "29acf85a-c911-4099-b128-bdf290103a01");
		bean.setCreator("cortex");
		bean.setFileSize(457l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 18, 3, 974));
		bean.setName("check-32.png");
		bean.setSpecification(rasterImageSpecification_399());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_35f());
		bean.setMd5("482b53149dde3c8a941303c1fb6aa34b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_399() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5717c96a-3a99-475b-957d-5e0cc17e8230");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_35f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "9222a10b-6bab-407a-bb38-848b936ddcef");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1803/a779045f-ec40-4102-9a1f-4c3ce5b9294b");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_36f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "00f3fa6f-945c-4d31-92fd-49220e78fd22");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1316/1809/41fb961d-5a5b-4212-8c1b-19cf68bacbcb");
		return bean;
	}

	// Managed
	private Resource resource_280() {
		Resource bean = session().createRaw(Resource.T, "57d7329e-ae46-4365-8286-a41267f97f66");
		bean.setCreator("cortex");
		bean.setFileSize(568l);
		bean.setCreated(newGmtDate(2020, 1, 13, 15, 18, 9, 940));
		bean.setName("check-64.png");
		bean.setSpecification(rasterImageSpecification_402());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_36f());
		bean.setMd5("8e1dcb2b67db675d09db1052467aae2d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_402() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b9c5b4e5-cece-429b-bc9f-9aaa521e62b8");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_16() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "635b8d7f-02c3-4688-970a-1a8833dbc070");
		bean.setTypeSignature("com.braintribe.model.extensiondeployment.check.CheckBundle");
		bean.setDisplayName(localizedString_519());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_519() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "221dc6a4-eee1-4d48-97d0-72a79f8776ab");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "CheckBundles Query")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_22() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "55e5d3de-7e19-4e7a-8e75-7c3d8b07f296");
		bean.setTemplate(template_103());
		bean.setDisplayName(localizedString_520());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_520() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8a90bdef-f5a3-4e0a-bcb7-8e14b3ac1a89");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Vitality Checks")));
		return bean;
	}

	// Managed
	private Template template_103() {
		Template bean = session().createRaw(Template.T, "8949c226-bd82-40b5-81d6-1483a7527668");
		bean.setPrototype(entityQuery_37());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_37() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "c1dab73a-13cc-4808-b566-6d6f01a56c73");
		bean.setRestriction(restriction_30());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.check.CheckBundle");
		return bean;
	}

	// Managed
	private Restriction restriction_30() {
		Restriction bean = session().createRaw(Restriction.T, "35a3248c-fe7f-4daa-a93f-0d9747f49fb7");
		bean.setCondition(valueComparison_55());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_55() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "1ec9daec-4d9e-426b-b630-a1a7cdea4f13");
		bean.setLeftOperand(propertyOperand_80());
		bean.setRightOperand(CheckCoverage.vitality);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_80() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "0ce985f9-242c-435c-ac5a-ed44e1473afa");
		bean.setPropertyName("coverage");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_23() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "12ebb209-8aa2-4497-a62d-91f54108e7f1");
		bean.setTemplate(template_104());
		bean.setDisplayName(localizedString_521());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_521() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2954d680-e24a-40d7-8523-94e45f144af9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Connectivity Checks")));
		return bean;
	}

	// Managed
	private Template template_104() {
		Template bean = session().createRaw(Template.T, "ae2365e0-1d07-4b18-bf3f-0965f8808c9b");
		bean.setPrototype(entityQuery_38());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_38() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "f1a00b10-109a-4013-b047-46a9bc17d0f9");
		bean.setRestriction(restriction_31());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.check.CheckBundle");
		return bean;
	}

	// Managed
	private Restriction restriction_31() {
		Restriction bean = session().createRaw(Restriction.T, "ec554f94-fcec-4c92-aa5e-8e623dac6943");
		bean.setCondition(valueComparison_56());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_56() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "9f4153ef-ee39-446b-94c9-cf5e7d74ce6f");
		bean.setLeftOperand(propertyOperand_81());
		bean.setRightOperand(CheckCoverage.connectivity);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_81() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "4090b931-3882-4fcc-8e6e-d5b60fb12bb4");
		bean.setPropertyName("coverage");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_24() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "6c95b00b-dd63-4caa-8a0a-32b63c06fca4");
		bean.setTemplate(template_105());
		return bean;
	}

	// Managed
	private Template template_105() {
		Template bean = session().createRaw(Template.T, "e06d1104-76b1-47bc-81e2-13325f98fc5c");
		bean.setDescription(localizedString_522());
		bean.setPrototype(entityQuery_39());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_522() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ee4ba994-a259-495e-93bb-84c45805e670");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Functional Checks")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_39() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "ea3c6f71-2f12-44be-b8e9-c277d57cefff");
		bean.setRestriction(restriction_32());
		bean.setEntityTypeSignature("com.braintribe.model.extensiondeployment.check.CheckBundle");
		return bean;
	}

	// Managed
	private Restriction restriction_32() {
		Restriction bean = session().createRaw(Restriction.T, "77f56e4e-b09b-4b73-bb93-9dfb116a4694");
		bean.setCondition(valueComparison_57());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_57() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "10e4d94e-fa4b-40bf-a20b-2d53504f23d9");
		bean.setLeftOperand(propertyOperand_82());
		bean.setRightOperand(CheckCoverage.functional);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_82() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "a103598f-cf38-44de-8b69-66ca8401b75c");
		bean.setPropertyName("coverage");
		return bean;
	}

	@Managed
	private Folder folder_173() {
		Folder bean = session().createRaw(Folder.T, "2010d61a-b71f-4f28-94f5-9499ea483c48");
		bean.setSubFolders(Lists.list(folder_174(), folder_176(), folder_175()));
		bean.setDisplayName(localizedString_523());
		bean.setName("Modules");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_523() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "950bca22-8985-4079-bea3-8804ef58be41");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modules")));
		return bean;
	}

	// Managed
	private Folder folder_174() {
		Folder bean = session().createRaw(Folder.T, "f7251598-be48-4789-836b-b1cd8c78c170");
		bean.setParent(folder_173());
		bean.setDisplayName(localizedString_524());
		bean.setName("All Modules");
		bean.setIcon(adaptiveIcon_72());
		bean.setContent(simpleQueryAction_17());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_524() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d540f68c-c868-469f-a299-411fe86dd5ad");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Modules")));
		return bean;
	}

	// Managed
	private Folder folder_175() {
		Folder bean = session().createRaw(Folder.T, "f8a613a3-df4b-457f-ac40-eed8918a7473");
		bean.setParent(folder_173());
		bean.setDisplayName(localizedString_525());
		bean.setName("Functional Modules");
		bean.setIcon(adaptiveIcon_14());
		bean.setContent(templateQueryAction_26());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_525() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9deae39b-9d98-4710-a980-12c426a000a2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Functional Modules")));
		return bean;
	}

	// Managed
	private Folder folder_176() {
		Folder bean = session().createRaw(Folder.T, "3bc42baf-d267-4b9c-bdae-512ff01815da");
		bean.setParent(folder_173());
		bean.setDisplayName(localizedString_526());
		bean.setName("Configurational Modules");
		bean.setIcon(adaptiveIcon_32());
		bean.setContent(templateQueryAction_25());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_526() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2665802e-5c77-446c-8ec8-25b6a6bba391");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configurational Modules")));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_17() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "43fc58ed-778b-4f11-9dfe-7ff147a4d487");
		bean.setTypeSignature("com.braintribe.model.deployment.Module");
		bean.setDisplayName(localizedString_527());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_527() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d2e48fd2-e1d8-44b6-9578-a10e52976e03");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Modules")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_25() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "6202018a-e967-48dd-a443-71842ccee493");
		bean.setTemplate(template_106());
		bean.setDisplayName(localizedString_528());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_528() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "388b9fe6-b2fd-445a-943f-ca1710cee93a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Configurational Modules")));
		return bean;
	}

	// Managed
	private Template template_106() {
		Template bean = session().createRaw(Template.T, "834eb7fe-fbf5-4a4b-8380-04934ce4618a");
		bean.setPrototype(entityQuery_40());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_40() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "7965e6c1-9831-4947-86bd-e117fcef5fdc");
		bean.setRestriction(restriction_33());
		bean.setEntityTypeSignature("com.braintribe.model.deployment.Module");
		return bean;
	}

	// Managed
	private Restriction restriction_33() {
		Restriction bean = session().createRaw(Restriction.T, "ef8cb34b-5aee-4085-8b47-233ab775eaba");
		bean.setCondition(valueComparison_58());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_58() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "3731a1ea-0289-42aa-84a3-1423367f8608");
		bean.setLeftOperand(propertyOperand_83());
		bean.setRightOperand(Boolean.TRUE);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_83() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e300e1b1-5a57-4e6b-8687-a67f00df2f48");
		bean.setPropertyName("bindsInitializers");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_26() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "aa09b204-3d1a-4625-9d4e-2862c3b72c79");
		bean.setTemplate(template_107());
		bean.setDisplayName(localizedString_529());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_529() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e7ba2ea6-d630-46f9-9e73-1ed711ed4d85");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Functional Modules")));
		return bean;
	}

	// Managed
	private Template template_107() {
		Template bean = session().createRaw(Template.T, "470d0892-ce2f-490a-b807-b8aad51a8c22");
		bean.setPrototype(entityQuery_41());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_41() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "f20a934d-1ae8-4333-9091-97027a685df2");
		bean.setRestriction(restriction_35());
		bean.setEntityTypeSignature("com.braintribe.model.deployment.Module");
		return bean;
	}

	// Managed
	private Restriction restriction_35() {
		Restriction bean = session().createRaw(Restriction.T, "ea5d879a-d4a6-4a72-b326-56c104d68977");
		bean.setCondition(disjunction_17());
		return bean;
	}

	// Managed
	private Disjunction disjunction_17() {
		Disjunction bean = session().createRaw(Disjunction.T, "eef60081-5040-4d42-9021-dc220aa18850");
		bean.setOperands(Lists.list(valueComparison_59(), valueComparison_60()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_59() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "95b47ce3-3ed6-44b9-a04c-8d106cbbb6e7");
		bean.setLeftOperand(propertyOperand_84());
		bean.setRightOperand(Boolean.TRUE);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_84() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "8a869ece-f1e4-4fd6-839c-9d4924c38361");
		bean.setPropertyName("bindsHardwired");
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_60() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "439d57f5-d05b-4cc3-9b9d-0a830618b623");
		bean.setLeftOperand(propertyOperand_85());
		bean.setRightOperand(Boolean.TRUE);
		bean.setOperator(Operator.equal);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_85() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "a7494615-4cac-4928-9beb-bdca667c1775");
		bean.setPropertyName("bindsDeployables");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_403() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "33827c63-f849-41f0-8088-03f0fcb3893a");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_281() {
		Resource bean = session().createRaw(Resource.T, "170e0d5c-5e69-4811-baee-480f5d028cc6");
		bean.setCreator("cortex");
		bean.setFileSize(644l);
		bean.setCreated(newGmtDate(2020, 1, 14, 7, 57, 28, 916));
		bean.setName("module_16.png");
		bean.setSpecification(rasterImageSpecification_403());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_37f());
		bean.setMd5("b342c5ed2282721dc3d76725769f292e");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_37f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "52d6e5ac-9c57-43a7-a304-f787bdf151f7");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1408/5728/1cd74721-d1c3-4b51-8b32-e6cf2fefcad4");
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_72() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "6cfd5519-219a-45a7-a56e-3126b098203f");
		bean.setName("Modules Icon");
		bean.setRepresentations(Sets.set(resource_281(), resource_282(), resource_283()));
		return bean;
	}

	// Managed
	private Resource resource_282() {
		Resource bean = session().createRaw(Resource.T, "942ad7fa-85e6-4e94-afd4-09f1fa09b6fb");
		bean.setCreator("cortex");
		bean.setFileSize(972l);
		bean.setCreated(newGmtDate(2020, 1, 14, 7, 57, 54, 763));
		bean.setName("module_32.png");
		bean.setSpecification(rasterImageSpecification_406());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_38f());
		bean.setMd5("175567e128d859c437ce0fc0eb9dcf93");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_38f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c31d8bec-8b53-43fa-8ad0-1dd8daabe974");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1408/5754/c35613a5-bedb-4dfd-8d23-cf8654b2e6a6");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_406() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1c4522f1-abbf-4176-9e36-14368f6e6ee5");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_39f() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "a49399f4-ea0c-4ec7-98c2-bd8b162a6ce2");
		bean.setModuleName(currentModuleName());
		bean.setPath("2002/1408/5800/7f944e14-2a80-4e8f-a468-9e6d9fdb5815");
		return bean;
	}

	// Managed
	private Resource resource_283() {
		Resource bean = session().createRaw(Resource.T, "1ae0f04a-d2eb-4bfb-9d5a-a3ae548099cb");
		bean.setCreator("cortex");
		bean.setFileSize(1684l);
		bean.setCreated(newGmtDate(2020, 1, 14, 7, 58, 0, 453));
		bean.setName("module_64.png");
		bean.setSpecification(rasterImageSpecification_408());
		bean.setMimeType("image/png");
		bean.setResourceSource(moduleSource_39f());
		bean.setMd5("9c44742ba301d2bb4a03822e9138a1b8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_408() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8a297034-546f-45d2-9670-1517cec537aa");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_42() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "833a58d7-a79e-4b12-aac5-f3c23512a3c4");
		bean.setOrdering(simpleOrdering_27());
		bean.setRestriction(restriction_36());
		bean.setEntityTypeSignature("com.braintribe.model.accessdeployment.IncrementalAccess");
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_27() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "0703cd6f-50a9-4eea-b300-e4e73375b33e");
		bean.setOrderBy(propertyOperand_86());
		bean.setDirection(OrderingDirection.ascending);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_86() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "b2f18d0a-f49d-4ca7-b9a1-7ca3ce9db775");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private Restriction restriction_36() {
		Restriction bean = session().createRaw(Restriction.T, "4dc03bd7-b738-4b7d-b0ee-a3c80ecb2b0d");
		bean.setCondition(negation_14());
		return bean;
	}

	// Managed
	private Negation negation_14() {
		Negation bean = session().createRaw(Negation.T, "12853e90-36d3-4441-8148-25bb0767dc1b");
		bean.setOperand(valueComparison_61());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_61() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "c31de3a4-a6e7-4032-a9ac-f4ed7c7e757a");
		bean.setLeftOperand(entitySignature_3());
		bean.setRightOperand("com.braintribe.model.accessdeployment.Hardwired*");
		bean.setOperator(Operator.like);
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_3() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "7f0d659e-fa7d-4c76-8103-bd79391b3e18");
		bean.setOperand(propertyOperand_87());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_87() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "a5750430-1ae4-4fa4-8f5b-a3d362349766");
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