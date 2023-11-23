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
package tribefire.cortex.assets.setup_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.pr.criteria.ComparisonOperator;
import com.braintribe.model.generic.pr.criteria.ConjunctionCriterion;
import com.braintribe.model.generic.pr.criteria.TypeConditionCriterion;
import com.braintribe.model.generic.pr.criteria.ValueConditionCriterion;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.selector.PropertyValueComparator;
import com.braintribe.model.platformsetup.api.request.AddAssetDependencies;
import com.braintribe.model.platformsetup.api.request.CloseTrunkAsset;
import com.braintribe.model.platformsetup.api.request.MergeTrunkAsset;
import com.braintribe.model.platformsetup.api.request.RenameAsset;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.workbench.PrototypeQueryAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.setup_wb.initializer.wire.contract.SetupWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class SetupWbInitializerSpace extends AbstractInitializerSpace implements SetupWbInitializerContract {

	@Override
	public void initialize() {
		workbenchPerspective_1();
		workbenchPerspective_2();
		workbenchPerspective_3();
		workbenchPerspective_4();
		workbenchPerspective_5();
		workbenchPerspective_6();
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a9fbaf7c-c21a-4fa1-b733-1dc70166e94d");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "b2b70027-1276-4ae4-9d11-e7440ae172eb");
		bean.setCreated(newGmtDate(2018, 1, 22, 19, 8, 11, 590));
		bean.setCreator("cortex");
		bean.setFileSize(551l);
		bean.setMd5("5478336eb1ce80dbe3fcce9688d18738");
		bean.setMimeType("image/png");
		bean.setName("run-32.png");
		bean.setResourceSource(moduleSource_1());
		bean.setSpecification(rasterImageSpecification_1());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "33ff7d39-64c8-4ea6-85d9-81cba64499e4");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0710/1120/9ed0353c-0a0a-4485-aaf2-71babc5f5700");
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "8cb78156-89ce-4329-b6cd-fa8f23735d0b");
		bean.setCreated(newGmtDate(2018, 1, 22, 19, 8, 11, 615));
		bean.setCreator("cortex");
		bean.setFileSize(346l);
		bean.setMd5("5194afa6471a42e57f5beb12242c6768");
		bean.setMimeType("image/png");
		bean.setName("run-16.png");
		bean.setResourceSource(moduleSource_2());
		bean.setSpecification(rasterImageSpecification_2());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "770f74b6-3eb4-432b-af04-bf004c04e813");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0710/1119/06ad2eca-40ec-4ef5-a77e-fd1f079de0a7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e31b6f87-453a-4327-8b39-2f453958668d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "c9bdc05a-559b-463d-9703-5171f45c29d1");
		bean.setDisplayName(localizedString_2());
		bean.setFolders(Lists.list(folder_49()));
		bean.setName("root");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ed1d6c8f-092f-400f-9052-093ab6f31693");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "root")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "547c8c02-8a59-44a7-a3ea-4a898ec20121");
		bean.setDisplayName(localizedString_3());
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d5bcbaf4-0430-4904-b429-914c55274f5c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "homeFolder")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_3() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "f7704cb0-2fd6-4b19-8386-b7247b07d00f");
		bean.setDisplayName(localizedString_4());
		bean.setFolders(Lists.list(folder_1()));
		bean.setName("actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3994e41f-2c1b-4751-ae26-4bf3b16a04eb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_4() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "9c717292-65b9-421a-93c1-4edf5f4dd974");
		bean.setDisplayName(localizedString_5());
		bean.setFolders(Lists.list(folder_16()));
		bean.setName("headerbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c2b10f83-b82e-406d-9039-371abe5798a5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "headerbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_5() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "9e8d5d88-628c-4122-b4d2-6b9d07cc772a");
		bean.setDisplayName(localizedString_6());
		bean.setFolders(Lists.list(folder_42()));
		bean.setName("global-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "82a01475-bdca-46d4-b173-ce44ac175aa3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "global-actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_6() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "ba4927b6-953a-4f3c-a89b-4e08b6c7caa3");
		bean.setDisplayName(localizedString_7());
		bean.setFolders(Lists.list(folder_28()));
		bean.setName("tab-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_7() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "65403078-a48e-4a25-b9d0-b00f4e33e9ee");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tab-actionbar")));
		return bean;
	}

	@Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "fc0c9253-a76a-41e1-a3f6-27d59b135384");
		bean.setDisplayName(localizedString_56());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_2(), folder_3(), folder_4(), folder_5(), folder_6(), folder_7(), folder_8(), folder_9(), folder_10(), folder_11(), folder_12(), folder_13(), folder_14(), folder_15(), folder_62()));
		return bean;
	}

	// Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "f6430fd9-b7b0-4974-bf71-370562d860fc");
		bean.setDisplayName(localizedString_69());
		bean.setName("$exchangeContentView");
		return bean;
	}

	// Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "e5169c61-bd32-4c74-90ce-726fbb67c793");
		bean.setDisplayName(localizedString_60());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "dc19697a-36d3-4b63-a480-d067ebead918");
		bean.setDisplayName(localizedString_61());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "7f57e1e8-73cb-4bab-9b9a-70fccf5a59f4");
		bean.setDisplayName(localizedString_62());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "06b41dc3-bc38-4fbd-95d9-e61c2ba62aad");
		bean.setDisplayName(localizedString_63());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "d7a7b7cb-9496-4698-ac07-6e19540a1b83");
		bean.setDisplayName(localizedString_64());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "7dd4af84-04fe-4fec-98f6-143da056bb78");
		bean.setDisplayName(localizedString_65());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "e64f53ae-9d96-43c6-b311-4ecc586eb713");
		bean.setDisplayName(localizedString_66());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "ae0f5e3c-ba7a-46cb-9c0e-2b1ac12fb7be");
		bean.setDisplayName(localizedString_67());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "248fb834-25aa-4aaf-9834-77cf273dbd4b");
		bean.setDisplayName(localizedString_68());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "6a7ced1f-9c4a-42d2-8c52-1af8f86f0220");
		bean.setDisplayName(localizedString_71());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "eb954335-e17e-4c9c-8340-0154f6981d95");
		bean.setDisplayName(localizedString_72());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "2a629ffb-d0e8-4776-a4f7-348a68bb0245");
		bean.setDisplayName(localizedString_73());
		bean.setIcon(adaptiveIcon_1());
		bean.setName("$executeServiceRequest");
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "95929427-c63b-4a4e-864a-6bf4da498b7a");
		bean.setDisplayName(localizedString_70());
		bean.setName("$addToClipboard");
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "4ce0ccc6-e321-44ee-86da-ba95a9225828");
		bean.setDisplayName(localizedString_57());
		bean.setName("headerbar");
		bean.setSubFolders(Lists.list(folder_17(), folder_18(), folder_19(), folder_20(), folder_25()));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "282bdf62-4b7a-4f4a-8055-c7d16612d676");
		bean.setDisplayName(localizedString_24());
		bean.setName("tb_Logo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f755d0b8-e1a3-47e2-bb7c-3cdc02cb5e6a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tb_ Logo")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "95d1f657-694f-4f40-8dbc-4450a47bcbcc");
		bean.setDisplayName(localizedString_25());
		bean.setName("$quickAccess-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4d14985a-e178-4a66-999c-7bca698d28b9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access-slot")));
		return bean;
	}

	// Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "f8959a57-9786-423e-8c03-0814d3213138");
		bean.setDisplayName(localizedString_26());
		bean.setName("$globalState-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e7bf3e2b-d8a0-46dd-bf0d-5839abe5d50b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global State-slot")));
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "841a2198-2b81-4dc2-bcaa-51a493a59dfb");
		bean.setDisplayName(localizedString_27());
		bean.setName("$settingsMenu");
		bean.setSubFolders(Lists.list(folder_21(), folder_22(), folder_23(), folder_74(), folder_24()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4a782271-b755-4a45-899d-f3c5396a741e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Settings Menu")));
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "b5e077ab-9878-401a-bfea-140525f9dd3a");
		bean.setDisplayName(localizedString_28());
		bean.setName("$reloadSession");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bb25d894-fa60-4bcb-860e-a4e9c1d36db8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Reload Session")));
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "bdad7a79-aada-4d4d-8c91-aa962e7fff5d");
		bean.setDisplayName(localizedString_29());
		bean.setName("$showSettings");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6f95dee6-065f-4cbe-890c-28a5fe1b7ae6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Settings")));
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "310f7aa1-bef2-4847-9b85-4662f7402276");
		bean.setDisplayName(localizedString_30());
		bean.setName("$uiTheme");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "22733556-b05f-4965-b4ac-4c9f023df84f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ui Theme")));
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "c37a1c67-8470-4df4-9e6a-64dd7f39eea7");
		bean.setDisplayName(localizedString_31());
		bean.setName("$showAbout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9101e121-841d-4f33-a391-3ac37602b4af");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show About")));
		return bean;
	}

	// Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "b1e17a7a-30c7-4c65-b72e-9a0e13b0e318");
		bean.setDisplayName(localizedString_32());
		bean.setName("$userMenu");
		bean.setSubFolders(Lists.list(folder_26(), folder_27()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "447e4261-4d45-4257-85fa-23be15e79b74");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Menu")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "a4cefb45-68b4-41a8-9140-db9827d65404");
		bean.setDisplayName(localizedString_33());
		bean.setName("$showUserProfile");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "28a7a57c-1ed0-4416-822e-af03429fc210");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show User Profile")));
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "663c2ff7-7069-494a-81b5-59842e478582");
		bean.setDisplayName(localizedString_34());
		bean.setName("$showLogout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_34() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4d449ba6-3eed-407a-a383-8ee33fbaefe3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Logout")));
		return bean;
	}

	// Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "65b14277-8979-4270-b0b3-ec3566721d8f");
		bean.setDisplayName(localizedString_58());
		bean.setName("tab-actionbar");
		bean.setSubFolders(Lists.list(folder_29(), folder_35()));
		return bean;
	}

	// Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "40c63c60-cbf3-4e2c-b1ee-6b8c02599b2d");
		bean.setDisplayName(localizedString_36());
		bean.setName("$explorer");
		bean.setSubFolders(Lists.list(folder_30(), folder_31(), folder_32(), folder_33(), folder_34()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d558c359-73b4-4176-8d84-158dcbd2fc26");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Explorer")));
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "70a8fe5f-f8f6-4fb4-9cd0-6947478da354");
		bean.setDisplayName(localizedString_37());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b7e0028a-99dd-4686-8a48-7eebbaee6ee7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "771bb4b9-201a-4e99-ab0b-1e4afb211335");
		bean.setDisplayName(localizedString_38());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_38() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "28363784-c091-49c3-8298-c6391acd77da");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "b2b09680-bcac-4d8f-90e1-8a8f3e268ea4");
		bean.setDisplayName(localizedString_39());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_39() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2891184b-e356-4c21-9cf7-f21e30128ba1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "a209c8d6-84d5-492a-a5fb-6443e058cbd9");
		bean.setDisplayName(localizedString_40());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "efca5f0c-c6bd-475f-91ef-ae61d53a638b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "f01330d1-0c05-402f-bc13-1324ced752b5");
		bean.setDisplayName(localizedString_41());
		bean.setName("$notificationsConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "415acc37-07c9-4750-ab85-d5b323cf2a54");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notifications Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "cf9af421-11c6-4b3d-8539-6a4761aad0f7");
		bean.setDisplayName(localizedString_42());
		bean.setName("$selection");
		bean.setSubFolders(Lists.list(folder_36(), folder_37(), folder_38(), folder_39(), folder_40(), folder_41()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a8d0c9d1-4e29-4d9e-b6c7-cc0d5754b30c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Selection")));
		return bean;
	}

	// Managed
	private Folder folder_36() {
		Folder bean = session().createRaw(Folder.T, "9a740ad5-2f67-479c-909d-52341d84138d");
		bean.setDisplayName(localizedString_43());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "85dd6cf8-0e87-4e57-bf7a-939b3ad324ce");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_37() {
		Folder bean = session().createRaw(Folder.T, "6530b17b-92a6-4014-b2ee-eab0b4a40bed");
		bean.setDisplayName(localizedString_44());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "629b747d-afdb-46ee-b421-f554ccd5e8c2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_38() {
		Folder bean = session().createRaw(Folder.T, "c4a5f22a-0ecf-4b5e-8344-0214fc1ce451");
		bean.setDisplayName(localizedString_45());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_45() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5ff45fde-ce35-4d59-9a6a-e8f8e873d377");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_39() {
		Folder bean = session().createRaw(Folder.T, "6e3f9cc6-f040-405e-97d4-4a80661a3387");
		bean.setDisplayName(localizedString_46());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_46() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8482c1ac-d1af-460d-897c-b1ad0dbc0a04");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_40() {
		Folder bean = session().createRaw(Folder.T, "5433b6fd-acb7-4d36-8011-495642309258");
		bean.setDisplayName(localizedString_47());
		bean.setName("$quickAccessConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_47() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "220895b2-eb97-447c-8212-f79bb3e79fd9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_41() {
		Folder bean = session().createRaw(Folder.T, "d0303afc-75fc-4369-9050-39de06c82b52");
		bean.setDisplayName(localizedString_48());
		bean.setName("$expertUI");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_48() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f32ee744-d66e-4e33-a2b3-ad8537eee241");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Expert U I")));
		return bean;
	}

	// Managed
	private Folder folder_42() {
		Folder bean = session().createRaw(Folder.T, "0b19b1db-f95b-4b82-bf36-1987125f0922");
		bean.setDisplayName(localizedString_59());
		bean.setName("global-actionbar");
		bean.setSubFolders(Lists.list(folder_43(), folder_44(), folder_45(), folder_46(), folder_47(), folder_48()));
		return bean;
	}

	// Managed
	private Folder folder_43() {
		Folder bean = session().createRaw(Folder.T, "a427ce00-9cc3-4b37-b0c5-cc4de8ff9a60");
		bean.setDisplayName(localizedString_50());
		bean.setName("$new");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_50() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5b82bc4d-77c2-425d-b494-3ffa11a0457f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New")));
		return bean;
	}

	// Managed
	private Folder folder_44() {
		Folder bean = session().createRaw(Folder.T, "260961bf-9e36-489b-a25f-dd0aa4a8ff56");
		bean.setDisplayName(localizedString_51());
		bean.setName("$dualSectionButtons");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_51() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "766e01f8-0b23-4b57-9f1e-7c8bbd3c7cdd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Dual Section Buttons")));
		return bean;
	}

	// Managed
	private Folder folder_45() {
		Folder bean = session().createRaw(Folder.T, "5a44626d-e618-49d8-b25c-a753e5e1670f");
		bean.setDisplayName(localizedString_52());
		bean.setName("$upload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_52() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "950b735e-4f16-4a2e-b526-16b142d77fac");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Upload")));
		return bean;
	}

	// Managed
	private Folder folder_46() {
		Folder bean = session().createRaw(Folder.T, "563a06ff-1b74-4edf-a53c-149e9d9bb016");
		bean.setDisplayName(localizedString_53());
		bean.setName("$undo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_53() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b9023e9d-8e04-4ddc-a903-8709283bf429");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Undo")));
		return bean;
	}

	// Managed
	private Folder folder_47() {
		Folder bean = session().createRaw(Folder.T, "c464343f-5dd2-40b9-9cf5-78d810484ec0");
		bean.setDisplayName(localizedString_54());
		bean.setName("$redo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_54() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "66bf2c9e-a481-421b-8f62-bf72190fb6b2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Redo")));
		return bean;
	}

	// Managed
	private Folder folder_48() {
		Folder bean = session().createRaw(Folder.T, "9a228b62-2f9b-4307-9c9b-bf9ad72e54b1");
		bean.setDisplayName(localizedString_55());
		bean.setName("$commit");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_55() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8da32eeb-88e1-46b6-a375-5e59d0baf626");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Commit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_56() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0e1f2040-736d-4fbd-8a4a-94b183625403");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_57() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b89bcd83-f231-4b03-9436-75417e988c32");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Header Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_58() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "248fe353-f7e6-400b-82d7-86cde80a2ff0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tab Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_59() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "170e4890-80a5-495f-85eb-40dac18d7c60");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_60() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2a257967-ac7a-4d2b-9458-2a0b526b96f4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_61() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "73d96757-0b87-4e72-9640-73ce1db8c1cf");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_62() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2e74f480-4d00-4221-a2b4-6ee04e8614b9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_63() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2587a4c1-3f2c-4544-9c51-5d97b9235c6c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_64() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cad22b3f-14d8-488f-8a25-ecdde4f1119b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_65() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d7e2d710-43ed-4b98-a433-3a1a710c8442");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_66() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "26f66a63-ac23-4e77-ae10-c4e446757667");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_67() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bdebbeac-ad3b-471b-bcfd-37106036a186");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_68() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9cce8b49-c8cb-4792-b87f-4cd494dd6539");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_69() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "dd78e181-dc7f-4891-8f7e-7fc10e350881");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_70() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e0b202cd-7652-49b7-83b0-8075c78ac178");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add to Clipboard")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_71() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "043df8ff-0c21-477c-983c-ed98badfc731");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_72() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bf36d74b-1723-4358-9cf2-920f034763d7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_73() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5513a304-bcff-4af1-ac0c-8b2dc85068ea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Execute")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "c128bfba-acc9-4575-97a2-71f20d518994");
		bean.setName("run-16.png Icon");
		bean.setRepresentations(Sets.set(resource_1(), resource_2()));
		return bean;
	}

	// Managed
	private Folder folder_49() {
		Folder bean = session().createRaw(Folder.T, "20f7d143-b41b-48da-9ec1-eaeffd05f3ee");
		bean.setDisplayName(localizedString_74());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_50()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_74() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "60ab8f5b-f229-4e67-9ae6-916431785342");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private Folder folder_50() {
		Folder bean = session().createRaw(Folder.T, "932b7be7-4a7a-46f9-8d18-73bf3220e581");
		bean.setDisplayName(localizedString_75());
		bean.setName("Platform Assets");
		bean.setSubFolders(Lists.list(folder_51(), folder_70(), folder_52(), folder_69(), folder_53()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_75() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d4114d91-5a9e-4650-8338-c017221423f5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Assets")));
		return bean;
	}

	// Managed
	private Folder folder_51() {
		Folder bean = session().createRaw(Folder.T, "90f58ebb-8d60-412a-9f65-a39dc7297c8b");
		bean.setContent(templateQueryAction_1());
		bean.setDisplayName(localizedString_76());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("All Assets");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_76() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1b9449e4-d939-470b-a18e-ac3f32da430a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Assets")));
		return bean;
	}

	// Managed
	private Folder folder_52() {
		Folder bean = session().createRaw(Folder.T, "720480ab-cc57-4713-9563-4970f1ee1070");
		bean.setContent(templateQueryAction_2());
		bean.setDisplayName(localizedString_77());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Trunk Assets");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_77() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "13d3aaa9-b08e-4132-be80-9cb6ccd9277c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trunk Assets")));
		return bean;
	}

	@Managed
	private Folder folder_53() {
		Folder bean = session().createRaw(Folder.T, "b144870f-7716-4693-bb9e-e50e369fc241");
		bean.setDisplayName(localizedString_78());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Assets per Nature");
		bean.setParent(folder_50());
		bean.setSubFolders(Lists.list(folder_54(), folder_55(), folder_56(), folder_57(), folder_58(), folder_59(), folder_75(), folder_60(), folder_61(), folder_76()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_78() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1b567364-6e20-4625-9a64-95275ceb455f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assets per Nature")));
		return bean;
	}

	// Managed
	private Folder folder_54() {
		Folder bean = session().createRaw(Folder.T, "420e22cd-8093-4940-8d3b-8d8bdf1c112e");
		bean.setContent(templateQueryAction_3());
		bean.setDisplayName(localizedString_79());
		bean.setName("Aggregators");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_79() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6d795ea5-8aef-4903-bd85-8d450baab7df");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Aggregators")));
		return bean;
	}

	// Managed
	private Folder folder_55() {
		Folder bean = session().createRaw(Folder.T, "fdff9d35-b98b-4fda-a2ef-7e2f7124873e");
		bean.setContent(templateQueryAction_4());
		bean.setDisplayName(localizedString_81());
		bean.setName("Container Projections");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_81() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c318beb3-24f9-42db-beeb-39fe93b1c630");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Container Projections")));
		return bean;
	}

	// Managed
	private Folder folder_56() {
		Folder bean = session().createRaw(Folder.T, "daaf0982-bf70-422d-b16e-fd2b2b3793ec");
		bean.setContent(templateQueryAction_5());
		bean.setDisplayName(localizedString_82());
		bean.setName("Custom Cartridges");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_82() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c830ae79-9909-4ab7-81bc-004210e7d3ca");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Cartridges")));
		return bean;
	}

	// Managed
	private Folder folder_57() {
		Folder bean = session().createRaw(Folder.T, "70e2e16f-1f5a-4c25-b4f6-26a3c0111a6a");
		bean.setContent(templateQueryAction_6());
		bean.setDisplayName(localizedString_83());
		bean.setName("Manipulation Primings");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_83() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9824c179-9499-4c66-8d13-32940d6e9a0e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Manipulation Primings")));
		return bean;
	}

	// Managed
	private Folder folder_58() {
		Folder bean = session().createRaw(Folder.T, "54edfa86-166b-49c5-9720-c7cc3c9ec707");
		bean.setContent(templateQueryAction_7());
		bean.setDisplayName(localizedString_84());
		bean.setName("Master Cartridges");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_84() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cf4e88cb-4e1c-467b-8daf-779f4e4db97f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Master Cartridges")));
		return bean;
	}

	// Managed
	private Folder folder_59() {
		Folder bean = session().createRaw(Folder.T, "88577159-c835-452c-a695-9b7be6ffce4b");
		bean.setContent(templateQueryAction_8());
		bean.setDisplayName(localizedString_85());
		bean.setName("Model Primings");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_85() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c92aa130-5137-403b-bf8a-e3cac4ed0b31");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Primings")));
		return bean;
	}

	// Managed
	private Folder folder_60() {
		Folder bean = session().createRaw(Folder.T, "2e5e4b78-44c5-4cb1-9dd7-4e1588018e35");
		bean.setContent(templateQueryAction_9());
		bean.setDisplayName(localizedString_86());
		bean.setName("Plugins");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_86() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b1554a4a-8e11-432a-ab5f-1bbbdc1647c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugins")));
		return bean;
	}

	// Managed
	private Folder folder_61() {
		Folder bean = session().createRaw(Folder.T, "03cce7c1-bb3d-4efb-b23f-b4bb2af3699c");
		bean.setContent(templateQueryAction_10());
		bean.setDisplayName(localizedString_87());
		bean.setName("Plugin Primings");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_87() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "86d1c6d5-7057-4ca9-a11b-a02f4df0ffaf");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugin Primings")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_1() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "2ef8a4d7-af65-44f3-aad3-43792db2fef3");
		bean.setDisplayName(localizedString_88());
		bean.setTemplate(template_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_88() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "da49c1be-d6a9-4267-99fa-4826dba842df");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Assets")));
		return bean;
	}

	// Managed
	private Template template_1() {
		Template bean = session().createRaw(Template.T, "dac3388e-19f9-4abf-a2f7-f8bfc7b119d6");
		bean.setDescription(localizedString_89());
		bean.setName(localizedString_90());
		bean.setPrototype(entityQuery_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_89() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e2b01f86-8ad1-413a-a02e-e14281ca0018");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Template Platform Assets All Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_90() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "af7b56b0-941a-4fd2-ac13-f526bd352334");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Template Platform Assets All Query")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_1() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "ad072b27-8f02-4b7d-8ca7-c168d1f342db");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_2() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "a4c03d4b-ee41-45ae-90a6-c97faf12b059");
		bean.setDisplayName(localizedString_91());
		bean.setTemplate(template_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_91() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9d698954-1912-4036-95ba-75fcdb5557f9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trunk Asset")));
		return bean;
	}

	// Managed
	private Template template_2() {
		Template bean = session().createRaw(Template.T, "0b9cadb2-c705-488a-88e8-70e2b6d3ac2e");
		bean.setDescription(localizedString_92());
		bean.setName(localizedString_93());
		bean.setPrototype(entityQuery_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_92() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8240f772-bb2a-4a7b-85ab-7808cbd50eb9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trunk Asset Query Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_93() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b0e09b1f-9c3d-4847-bfcf-ad16e897b211");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trunk Asset Query Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_2() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "41672722-1ac8-4d14-b14d-a81d3f15cfae");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_1());
		return bean;
	}

	// Managed
	private Restriction restriction_1() {
		Restriction bean = session().createRaw(Restriction.T, "ea194070-9707-41bb-bf62-a05ecaaa5a34");
		bean.setCondition(valueComparison_1());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_1() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "abade2aa-744e-4e16-b43c-b8751006998b");
		bean.setLeftOperand(propertyOperand_1());
		bean.setOperator(Operator.ilike);
		bean.setRightOperand("trunk-*");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_1() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "36eba1fc-199e-4045-ae46-51a455ca6e04");
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_3() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "08887e47-9fbd-4850-a1d7-31ae27fdc7cb");
		bean.setDisplayName(localizedString_94());
		bean.setTemplate(template_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_94() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8453c390-6b31-4f0c-beb7-064deac95cf4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Aggregators")));
		return bean;
	}

	// Managed
	private Template template_3() {
		Template bean = session().createRaw(Template.T, "1ec4a025-404a-492c-a1f1-253e9523d12f");
		bean.setDescription(localizedString_95());
		bean.setName(localizedString_96());
		bean.setPrototype(entityQuery_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_95() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a8d7624b-0a9b-4c4d-ad7a-2d08a985e6e6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Aggregators Query Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_96() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3ef49865-7a37-4e2e-8f24-aa6bbdc396ea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Aggregators Query Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_3() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "fe2d3b62-4bc7-492e-974d-0b5a36bd2b16");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_2());
		return bean;
	}

	// Managed
	private Restriction restriction_2() {
		Restriction bean = session().createRaw(Restriction.T, "b44e4bcb-4b3b-48c0-9e1f-94a0f5d22e44");
		bean.setCondition(valueComparison_2());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_2() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "fd9014a0-361d-4b25-afe9-d95069824a64");
		bean.setLeftOperand(entitySignature_1());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.AssetAggregator");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_1() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "0cfdde0e-e50a-4062-994e-790f66e5f762");
		bean.setOperand(propertyOperand_2());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_2() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "c3b6629b-428a-4c61-a4f6-da354601b07d");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_4() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "690766cf-df53-453e-95a4-3d6ee70a1d53");
		bean.setDisplayName(localizedString_100());
		bean.setTemplate(template_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_100() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1b5e26f8-cb57-4516-a2a0-b4f74ae07fde");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Container Projections")));
		return bean;
	}

	// Managed
	private Template template_4() {
		Template bean = session().createRaw(Template.T, "8c0f1ad7-7255-4fe6-b5c3-c1e42551bbcc");
		bean.setDescription(localizedString_101());
		bean.setName(localizedString_102());
		bean.setPrototype(entityQuery_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_101() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c9be3b78-4867-40b9-9f49-e074db751a09");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Container Projections Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_102() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "80157095-a331-4365-ae03-417f713128e4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Container Projections Query")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_4() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "36b88bfe-27b8-4d86-b660-b073c1f683c1");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_3());
		return bean;
	}

	// Managed
	private Restriction restriction_3() {
		Restriction bean = session().createRaw(Restriction.T, "e5358e58-75d0-4581-8240-162ee4c21e35");
		bean.setCondition(valueComparison_3());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_3() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "2a9dc4b9-9357-42e5-a20e-ebf0e874930e");
		bean.setLeftOperand(entitySignature_2());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.ContainerProjection");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_2() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "5e2f61af-9dbe-4cae-9301-0b701670bf81");
		bean.setOperand(propertyOperand_4());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_4() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "a1eb0194-887b-4be0-8f95-6f09117dd695");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_5() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "8774357b-4ea6-4afb-bf4c-49f4b0426e2d");
		bean.setDisplayName(localizedString_103());
		bean.setTemplate(template_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_103() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a4e90f17-527b-4ee0-a8ec-6b5fe8b83efe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Cartridges")));
		return bean;
	}

	// Managed
	private Template template_5() {
		Template bean = session().createRaw(Template.T, "8183e73f-e8ff-42fa-b9b8-234046a71fb6");
		bean.setDescription(localizedString_104());
		bean.setName(localizedString_105());
		bean.setPrototype(entityQuery_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_104() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "35cb66f1-c5a0-432c-b686-3052230aef82");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Cartridges Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_105() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "99a86a91-bb78-4035-860a-896a1cdc1f12");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Custom Cartridges Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_5() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "23ead1f0-0aaa-4569-9e15-a039414e6f52");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_4());
		return bean;
	}

	// Managed
	private Restriction restriction_4() {
		Restriction bean = session().createRaw(Restriction.T, "a813affc-342b-4d31-9856-bad2391cc3b7");
		bean.setCondition(valueComparison_4());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_4() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "447c1409-adc3-45d6-9b1b-65b6448b86d3");
		bean.setLeftOperand(entitySignature_4());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.CustomCartridge");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_6() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "90275e49-9aca-4214-bea1-9e131033a11d");
		bean.setDisplayName(localizedString_106());
		bean.setTemplate(template_6());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_106() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "27ebc567-8ce2-4943-a259-c2f130340c07");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Manipulation Primings")));
		return bean;
	}

	// Managed
	private Template template_6() {
		Template bean = session().createRaw(Template.T, "27e632a9-2d6f-4826-a1ba-e338fd2b7bb2");
		bean.setDescription(localizedString_107());
		bean.setName(localizedString_108());
		bean.setPrototype(entityQuery_6());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_107() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cbf1ade4-1231-4b06-b1c2-26f6d84e4ff0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Manipulation Primings Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_108() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5b5ee460-c6ad-48f5-b470-20e784a192f5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Manipulation Primings Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_6() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "309b3854-a066-4508-b039-de335c55ace4");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_5());
		return bean;
	}

	// Managed
	private Restriction restriction_5() {
		Restriction bean = session().createRaw(Restriction.T, "071235a8-a31a-4529-bbc9-7c92230200ed");
		bean.setCondition(valueComparison_5());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_5() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "920c2f8f-4c22-43df-8131-4fd4600a3147");
		bean.setLeftOperand(entitySignature_6());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.ManipulationPriming");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_7() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "fc1f7fd0-a5b4-4e8f-b18d-18f4c58eae44");
		bean.setDisplayName(localizedString_109());
		bean.setTemplate(template_7());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_109() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1b7b70e6-4815-4168-ac7f-4fb0ca967ce4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Master Cartridges")));
		return bean;
	}

	// Managed
	private Template template_7() {
		Template bean = session().createRaw(Template.T, "2c4bf544-6e7e-4421-a0d2-939b4cb204c5");
		bean.setDescription(localizedString_110());
		bean.setName(localizedString_111());
		bean.setPrototype(entityQuery_7());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_110() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9f8703f5-b223-4eca-af40-36473586b9c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Master Cartridges Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_111() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a2dc5d6d-78e2-4235-9fe7-71ee724f2494");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Master Cartridges Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_7() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "5c3ae48e-7cdd-43c9-a677-98396d73fb81");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_6());
		return bean;
	}

	// Managed
	private Restriction restriction_6() {
		Restriction bean = session().createRaw(Restriction.T, "f86045d1-fc0e-476c-8c20-7698b44b6023");
		bean.setCondition(valueComparison_6());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_6() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "b766744d-e2d7-4a61-82ae-6aeb70f0b0a7");
		bean.setLeftOperand(entitySignature_8());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.MasterCartridge");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_8() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "3926e789-8a7b-4d9c-a935-6ae2405fb834");
		bean.setDisplayName(localizedString_112());
		bean.setTemplate(template_8());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_112() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e57f9f4e-5ef8-49aa-aece-bfa988cfbf4f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Primings")));
		return bean;
	}

	// Managed
	private Template template_8() {
		Template bean = session().createRaw(Template.T, "c96bfe9e-5d4d-450f-8496-a098b493f28c");
		bean.setDescription(localizedString_113());
		bean.setName(localizedString_114());
		bean.setPrototype(entityQuery_8());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_113() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "850691c0-e993-4fbf-9e4e-cb83d0c6a12e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Primings Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_114() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a97f9677-5108-48fd-8129-349244c18c65");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Model Primings Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_8() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "24788cb9-ae14-4b11-a539-6d060110eb97");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_7());
		return bean;
	}

	// Managed
	private Restriction restriction_7() {
		Restriction bean = session().createRaw(Restriction.T, "bf6164fe-72bd-4f0f-8db1-e7843726795c");
		bean.setCondition(valueComparison_7());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_7() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "1c992fbd-9554-42ab-b26c-f9f36a7e8397");
		bean.setLeftOperand(entitySignature_3());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.ModelPriming");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_9() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "9b3c29f8-2eb1-45f5-8966-ee98bdab568f");
		bean.setDisplayName(localizedString_115());
		bean.setTemplate(template_9());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_115() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "216700de-7fef-4ac0-8faf-026a6077c008");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugins")));
		return bean;
	}

	// Managed
	private Template template_9() {
		Template bean = session().createRaw(Template.T, "51b03971-7049-4477-8c88-79521d6e8a40");
		bean.setDescription(localizedString_116());
		bean.setName(localizedString_117());
		bean.setPrototype(entityQuery_9());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_116() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "60eb1be9-155b-48d7-9525-25604733416c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugins Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_117() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a4ef67b3-e2f7-403b-b564-7deed9b447a1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugins Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_9() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "7fe59046-9572-4be1-806d-3752f437f4c6");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_8());
		return bean;
	}

	// Managed
	private Restriction restriction_8() {
		Restriction bean = session().createRaw(Restriction.T, "2fb115d8-c86f-4b67-b0ee-c7a7859685e9");
		bean.setCondition(valueComparison_8());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_8() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "4b6a8745-089d-430a-972c-d14885172356");
		bean.setLeftOperand(entitySignature_7());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.Plugin");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_10() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "7cf15d68-0d01-49d1-9c84-d1fc0a6094ad");
		bean.setDisplayName(localizedString_118());
		bean.setTemplate(template_10());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_118() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ad491153-e1c5-4e83-9e90-be2d7a30fca9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugin Primings")));
		return bean;
	}

	// Managed
	private Template template_10() {
		Template bean = session().createRaw(Template.T, "18fa178d-485f-4402-baba-7789cae7c99e");
		bean.setDescription(localizedString_119());
		bean.setName(localizedString_120());
		bean.setPrototype(entityQuery_10());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_119() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "125b992a-c3a5-4a60-b8f7-ad4b9dd93ca7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugin Primings Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_120() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ad9c0338-66c2-4d19-9a59-7edbfe19bf72");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Plugin Primings Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_10() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "26b7fcd9-68e6-4502-a790-9114d195bf65");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_9());
		return bean;
	}

	// Managed
	private Restriction restriction_9() {
		Restriction bean = session().createRaw(Restriction.T, "a3135bba-edad-4b05-a87b-77631b720854");
		bean.setCondition(valueComparison_9());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_9() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "30d47802-2dd5-445d-960b-2256c7ae5078");
		bean.setLeftOperand(entitySignature_5());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.PluginPriming");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_4() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "6884941e-df01-40b8-9e40-f418b6e11453");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0711/1208/c1c7ac3f-7025-41d0-b293-842a2f928cb9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_3() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c3bd32bd-d798-4c1c-acf7-8dbe90107ffe");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_5() {
		Resource bean = session().createRaw(Resource.T, "6376a397-59ba-4755-862d-b9d303c4fa83");
		bean.setCreated(newGmtDate(2018, 2, 7, 10, 12, 8, 682));
		bean.setCreator("cortex");
		bean.setFileSize(1126l);
		bean.setMd5("1965cdfec2d1c52baf7342f9254530bc");
		bean.setMimeType("image/png");
		bean.setName("tfIcon");
		bean.setResourceSource(moduleSource_4());
		bean.setSpecification(rasterImageSpecification_3());
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_2() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "95bd1431-b1d2-4d38-90be-eeed9ced399f");
		bean.setName("tf-icon");
		bean.setRepresentations(Sets.set(resource_5()));
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_3() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "79f2db6e-5dcd-4f81-9d45-fc1fd9612cc1");
		bean.setOperand(propertyOperand_11());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_11() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "0e522317-c550-4c8d-9afd-833725e0f504");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_4() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "eeddfcf8-3cac-4233-a50a-af518430c051");
		bean.setOperand(propertyOperand_12());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_12() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "303948a5-bf06-4f1f-833e-806b745e1c78");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_5() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "ba80ad78-dec1-4bb4-9462-dced6535438c");
		bean.setOperand(propertyOperand_13());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_13() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "f770121f-4e9c-4ecb-a196-54b0c9b928b8");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_6() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "b3e88201-a439-499b-888f-6a542120e465");
		bean.setOperand(propertyOperand_14());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_14() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "f2f54ebf-d960-4cbd-aaec-500937416e7b");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_7() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "c7f8eb69-f198-4556-b1d5-da035886b295");
		bean.setOperand(propertyOperand_15());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_15() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e1b0c00c-4660-44a3-b82b-057a1f0a2ea9");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_8() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "c1af3373-c802-41b7-bf58-43cd03bdb6fc");
		bean.setOperand(propertyOperand_16());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_16() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "f0c0eab0-cff8-4032-9cd6-480fa32c3c78");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_1() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "2d305e70-7eba-4006-9eae-4f3381704071");
		bean.setDisplayName(localizedString_121());
		bean.setIcon(adaptiveIcon_2());
		bean.setInplaceContextCriterion(conjunctionCriterion_1());
		bean.setTemplate(template_11());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_121() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "368ae35f-8508-4843-9dde-1fea20d67bfb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into predecessor")));
		return bean;
	}

	// Managed
	private Template template_11() {
		Template bean = session().createRaw(Template.T, "a60a5a73-df90-45f6-a645-a32a9c423462");
		bean.setDescription(localizedString_122());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_4()));
		bean.setName(localizedString_123());
		bean.setPrototype(mergeTrunkAsset_1());
		bean.setScript(compoundManipulation_8());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_122() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "dd0475eb-b9a1-41f0-8e19-f48b32114c0f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into predecessor template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_123() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3cd91e9b-1de3-4dfb-998e-7619c81817a4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into predecessor template")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_1() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "18f5c7c1-83aa-4e67-8a90-8781dab792a2");
		bean.setCriteria(Lists.list(typeConditionCriterion_1(), valueConditionCriterion_1()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_1() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "d7a2c4f5-eb34-4f9f-9450-62a026fae5a0");
		bean.setTypeCondition(isAssignableTo_1());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_1() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "1eadd9fb-d2be-4df2-8247-015692a500ed");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_1() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "b70d7e7f-7464-480e-ab1f-1cc1bb73dfd0");
		bean.setOperand("trunk-.*");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	@Managed
	private MergeTrunkAsset mergeTrunkAsset_1() {
		MergeTrunkAsset bean = session().createRaw(MergeTrunkAsset.T, "4fb85a16-09a0-406e-b2ef-f24c9d88b931");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_1() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "54c21ec5-f433-4b68-ab05-f5f77a61ec6d");
		bean.setNewValue(variable_1());
		bean.setOwner(localEntityProperty_1());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_1() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "5619be41-38e6-4d1f-83bf-6985a657b69c");
		bean.setEntity(mergeTrunkAsset_1());
		bean.setPropertyName("asset");
		return bean;
	}

	@Managed
	private Variable variable_1() {
		Variable bean = session().createRaw(Variable.T, "244e0fc7-6325-4fd6-9602-135450452bd4");
		bean.setLocalizedName(localizedString_124());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_124() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e37cb426-328f-4d79-9bee-5f575f1bb19e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	@Managed
	private Folder folder_62() {
		Folder bean = session().createRaw(Folder.T, "856edda8-0c3f-4f07-a7d2-41499cb14ebc");
		bean.setDisplayName(localizedString_125());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Platform Setup");
		bean.setParent(folder_1());
		bean.setSubFolders(Lists.list(folder_63(), folder_73(), folder_65(), folder_64(), folder_66(), folder_67(), folder_68(), folder_71(), folder_72()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_125() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5d421a60-005b-4da6-a7af-6c14fe8cd5fe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Setup")));
		return bean;
	}

	// Managed
	private Folder folder_63() {
		Folder bean = session().createRaw(Folder.T, "c58659c5-0693-4e03-aad7-e4740a77a3eb");
		bean.setContent(templateServiceRequestAction_1());
		bean.setDisplayName(localizedString_126());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("merge");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_126() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "912427f2-c56d-46ed-af1e-a7585c6ac857");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into predecessor")));
		return bean;
	}

	// Managed
	private Folder folder_64() {
		Folder bean = session().createRaw(Folder.T, "b3dfe606-3635-48d4-b5f0-e7093c70627b");
		bean.setContent(templateServiceRequestAction_2());
		bean.setDisplayName(localizedString_127());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Close");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_127() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "424cb441-a270-4921-91a4-84b81e76e38c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_2() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "78665bdc-cedb-4c30-a554-4205641c5a30");
		bean.setDisplayName(localizedString_128());
		bean.setInplaceContextCriterion(conjunctionCriterion_2());
		bean.setTemplate(template_12());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_128() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "938d266b-98f9-4bf1-a51b-3dda8d82c9ce");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close Trunk Asset")));
		return bean;
	}

	// Managed
	private Template template_12() {
		Template bean = session().createRaw(Template.T, "b575188f-647c-4371-825e-97aa081de7b7");
		bean.setDescription(localizedString_129());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_1()));
		bean.setName(localizedString_130());
		bean.setPrototype(closeTrunkAsset_1());
		bean.setScript(compoundManipulation_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_129() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "93828562-b18d-4f2b-9ca5-b8f5579e6afd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close Trunk Asset Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_130() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f4b36b3c-8b47-41b6-93ad-9afe3bfd24e0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close Trunk Asset Template")));
		return bean;
	}

	@Managed
	private CloseTrunkAsset closeTrunkAsset_1() {
		CloseTrunkAsset bean = session().createRaw(CloseTrunkAsset.T, "074f26db-497e-47a6-bd14-97f04694f183");
		bean.setVersion("1.0");
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_2() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "008286bc-ecc0-401b-b77b-79e0dff44add");
		bean.setCriteria(Lists.list(valueConditionCriterion_2(), typeConditionCriterion_2()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_2() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "e71a6174-68ce-4222-8a02-e34372c97464");
		bean.setOperand("trunk-.*");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_2() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "2aaed5dd-d39d-4f6e-85f9-e2672d3fa5a6");
		bean.setTypeCondition(isAssignableTo_2());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_2() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "a5584e54-61c9-4732-83b5-19c54478e5b3");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_1() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "f16f1be9-69b6-4f8f-a17e-ee041e69d76f");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_2(), changeValueManipulation_3(), changeValueManipulation_4(), changeValueManipulation_5()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_2() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "ddaa59c5-14ef-4918-a959-0fda65b88120");
		bean.setNewValue(variable_2());
		bean.setOwner(localEntityProperty_2());
		return bean;
	}

	@Managed
	private Variable variable_2() {
		Variable bean = session().createRaw(Variable.T, "8408ac9b-3e0b-437b-9f05-22710cd5a82a");
		bean.setLocalizedName(localizedString_131());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_131() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5070f848-7af8-4633-891e-e415eadf7cb4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_2() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d028c1be-db4e-4311-b596-3c00aacab804");
		bean.setEntity(closeTrunkAsset_1());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_3() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "739bddbb-6041-4547-9381-7676f609317c");
		bean.setNewValue(variable_3());
		bean.setOwner(localEntityProperty_3());
		return bean;
	}

	// Managed
	private Variable variable_3() {
		Variable bean = session().createRaw(Variable.T, "d4434dcb-6e35-4432-a6e4-fffd1fbee388");
		bean.setDefaultValue("custom");
		bean.setLocalizedName(localizedString_132());
		bean.setName("groupId");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_132() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "82711442-a083-489b-bbfc-c4af7459ed57");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "group id")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_3() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "45316553-7f6e-42a9-b45f-221d7adebd38");
		bean.setEntity(closeTrunkAsset_1());
		bean.setPropertyName("groupId");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_4() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "551d1a71-5a47-4b6a-aeb1-4d3917cff3af");
		bean.setNewValue(variable_4());
		bean.setOwner(localEntityProperty_4());
		return bean;
	}

	// Managed
	private Variable variable_4() {
		Variable bean = session().createRaw(Variable.T, "e4e42d6f-f4c8-4708-8072-7aaf814d3065");
		bean.setLocalizedName(localizedString_133());
		bean.setName("name");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_133() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "743077a2-964e-4be3-8465-161e593c652a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "name")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_4() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "4f39779f-e835-495c-b339-acc7a53a329f");
		bean.setEntity(closeTrunkAsset_1());
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_5() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "6fc86ee7-261f-416a-a15b-e211cc897c58");
		bean.setNewValue(variable_5());
		bean.setOwner(localEntityProperty_5());
		return bean;
	}

	// Managed
	private Variable variable_5() {
		Variable bean = session().createRaw(Variable.T, "dffe240e-21ae-4c56-b1c7-4bfe37a7bb96");
		bean.setDefaultValue("1.0");
		bean.setLocalizedName(localizedString_134());
		bean.setName("version");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_134() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "83b2ca96-1598-4171-9289-849ecbd9d9e2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "version")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_5() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b2150f74-0424-493e-89bc-2c42873773aa");
		bean.setEntity(closeTrunkAsset_1());
		bean.setPropertyName("version");
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_5() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "62bd9e95-ab50-4739-8430-78f371cd7658");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0713/1326/9f1130d0-4d18-4a92-b184-07e3ccfaa0b9");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_4() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e4782b55-5ddd-4b19-bade-7005d9cda229");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_6() {
		Resource bean = session().createRaw(Resource.T, "5da9a5db-4b30-469d-ab47-c0423f2ad11c");
		bean.setCreated(newGmtDate(2018, 2, 7, 12, 13, 26, 215));
		bean.setCreator("cortex");
		bean.setFileSize(505l);
		bean.setMd5("f255f2f29a56af5dfa341e27e64dcbe9");
		bean.setMimeType("image/png");
		bean.setName("settings_16x16.png");
		bean.setResourceSource(moduleSource_5());
		bean.setSpecification(rasterImageSpecification_4());
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_3() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "b5dd5469-a8a8-4ce1-89a6-d69af5e61e55");
		bean.setName("platform-setup-icon");
		bean.setRepresentations(Sets.set(resource_6(), resource_7(), resource_8()));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_6() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4414828e-05ae-4966-a3f6-5421ff1ec3cc");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0713/1412/6a1d352f-aeb0-4fb7-a181-fb18c3273b81");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_5() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b1a3014f-635a-47fc-a8f2-b24b578c4ae4");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_7() {
		Resource bean = session().createRaw(Resource.T, "0067f949-6e8a-4978-9ee6-9e35b8d70945");
		bean.setCreated(newGmtDate(2018, 2, 7, 12, 14, 12, 981));
		bean.setCreator("cortex");
		bean.setFileSize(1363l);
		bean.setMd5("a1ed0cefbdf8b0431beb707f3ea5c44b");
		bean.setMimeType("image/png");
		bean.setName("settings_32x32.png");
		bean.setResourceSource(moduleSource_6());
		bean.setSpecification(rasterImageSpecification_5());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_7() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8fadd920-faf2-4020-a5bb-4515456fd85e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/0713/1422/bd27decb-44db-4667-bc81-11d3a8bbd49c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_6() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "cccd9ada-8ddc-4c89-8906-d1cafd5872ae");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_8() {
		Resource bean = session().createRaw(Resource.T, "6c0bcc96-35b8-4030-aaee-3b9247a867c9");
		bean.setCreated(newGmtDate(2018, 2, 7, 12, 14, 22, 295));
		bean.setCreator("cortex");
		bean.setFileSize(3634l);
		bean.setMd5("1fa3f4ca05759104ff15645f7bdcaa3f");
		bean.setMimeType("image/png");
		bean.setName("settings_64x64.png");
		bean.setResourceSource(moduleSource_7());
		bean.setSpecification(rasterImageSpecification_6());
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_3() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "49f4f8e7-0348-40d0-bcde-742d96d3b28f");
		bean.setDisplayName(localizedString_135());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_3());
		bean.setTemplate(template_13());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_135() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1fda3ae4-b568-469c-8b87-f5f9d5a8c1fb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge and Transfer Asset")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_3() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "fd520054-c44e-4e4a-a962-c6f73a88e150");
		bean.setCriteria(Lists.list(typeConditionCriterion_3(), valueConditionCriterion_3()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_3() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "dbd5f782-88c9-4c22-8820-fd59f3d608bc");
		bean.setTypeCondition(isAssignableTo_3());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_3() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "d2ad7393-eb0d-408e-bcf5-64fd71b28d60");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_3() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "445a1dac-4dc3-4525-a5f2-bf15829774a4");
		bean.setOperand("trunk-.*");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private Template template_13() {
		Template bean = session().createRaw(Template.T, "426d0892-22ba-4ec7-a2b1-95828b8decd9");
		bean.setDescription(localizedString_136());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_3()));
		bean.setName(localizedString_137());
		bean.setPrototype(mergeTrunkAsset_2());
		bean.setScript(compoundManipulation_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_136() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "505baf09-29be-419d-aa3a-0546af4ba86b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge and Transfer Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_137() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "623224a1-80fa-48a7-a210-42ba2b2cf265");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge and Transfer Template")));
		return bean;
	}

	@Managed
	private MergeTrunkAsset mergeTrunkAsset_2() {
		MergeTrunkAsset bean = session().createRaw(MergeTrunkAsset.T, "1d70c812-5417-4691-a7b5-9c29ac04eac9");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_2() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "e78f737b-d2f5-4c6a-bf82-c544e23b13ef");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_6(), changeValueManipulation_7(), changeValueManipulation_31(), changeValueManipulation_32()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_6() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "c741c979-b188-4299-83ce-8a184410b303");
		bean.setNewValue(variable_6());
		bean.setOwner(localEntityProperty_6());
		return bean;
	}

	@Managed
	private Variable variable_6() {
		Variable bean = session().createRaw(Variable.T, "d73a4637-ccb6-41f0-a52a-9c2b19e4203c");
		bean.setLocalizedName(localizedString_138());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_138() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "20e8733d-c31a-43bd-bb30-b4d901d76718");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_6() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "60b949fe-426b-4815-a4f0-33469149aa4e");
		bean.setEntity(mergeTrunkAsset_2());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_7() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "6a276c70-2929-4d33-b5ee-8f0fa94d9173");
		bean.setNewValue(variable_7());
		bean.setOwner(localEntityProperty_7());
		return bean;
	}

	// Managed
	private Variable variable_7() {
		Variable bean = session().createRaw(Variable.T, "cc95e78d-e13d-4963-b223-b7a96b8e2fc9");
		bean.setDefaultValue("deploy");
		bean.setLocalizedName(localizedString_139());
		bean.setName("transferOperation");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_139() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b111cd0c-3f26-4041-959f-e2fcd56b581c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "transfer operation")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_7() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "1e317b41-22b4-4e0d-ace7-2bab5fafecac");
		bean.setEntity(mergeTrunkAsset_2());
		bean.setPropertyName("transferOperation");
		return bean;
	}

	// Managed
	private Folder folder_65() {
		Folder bean = session().createRaw(Folder.T, "1bdce0a2-72e9-4de1-ac26-15bae3c85d97");
		bean.setContent(templateServiceRequestAction_3());
		bean.setDisplayName(localizedString_140());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Merge and transfer");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_140() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7d6662ce-90b4-4530-83f1-fe5f6d62dbc5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge and transfer")));
		return bean;
	}

	// Managed
	private Folder folder_66() {
		Folder bean = session().createRaw(Folder.T, "873fbd20-6cae-4c99-8c90-95bbd1a0ba92");
		bean.setContent(templateServiceRequestAction_4());
		bean.setDisplayName(localizedString_141());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("closeAndTransfer");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_141() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c17c245e-0bb9-49c8-ae5b-e175c57b693a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close and transfer")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_4() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "06aef567-e748-4785-928b-5b99fbff6695");
		bean.setDisplayName(localizedString_142());
		bean.setInplaceContextCriterion(conjunctionCriterion_4());
		bean.setTemplate(template_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_142() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0bdc6dff-64ef-473c-9c21-62ce0ff99ed7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close and Transfer")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_4() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "90968de1-788d-46ba-b217-2a440c29ac1f");
		bean.setCriteria(Lists.list(typeConditionCriterion_4(), valueConditionCriterion_4()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_4() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "372a4d55-6b4d-4fd9-a970-76fb01b19561");
		bean.setTypeCondition(isAssignableTo_4());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_4() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "653b02ed-36d4-432c-9efd-2dfebd205129");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_4() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "142978e4-533f-40d8-b466-475434c8b664");
		bean.setOperand("trunk-.*");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private Template template_14() {
		Template bean = session().createRaw(Template.T, "b8bfd897-8111-41bc-87e0-140110a5f811");
		bean.setDescription(localizedString_143());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_2()));
		bean.setName(localizedString_144());
		bean.setPrototype(closeTrunkAsset_2());
		bean.setScript(compoundManipulation_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_143() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "72e8b618-93ac-4018-8cc4-0c59a9c53d8b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close and Transfer Asset Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_144() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7348df42-27ae-41f9-ab46-3aa20e1fc97c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close and Transfer Asset Template")));
		return bean;
	}

	@Managed
	private CloseTrunkAsset closeTrunkAsset_2() {
		CloseTrunkAsset bean = session().createRaw(CloseTrunkAsset.T, "bd84f73a-514a-4912-bcde-bde09e13ac92");
		bean.setVersion("1.0");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_3() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "eff44be6-0e91-4bc7-bab9-10c4ea901d04");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_8(), changeValueManipulation_9(), changeValueManipulation_10(), changeValueManipulation_11(), changeValueManipulation_12()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_8() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "12d19717-2719-40a3-adb8-9c36d50d0a12");
		bean.setNewValue(variable_8());
		bean.setOwner(localEntityProperty_8());
		return bean;
	}

	@Managed
	private Variable variable_8() {
		Variable bean = session().createRaw(Variable.T, "5e35fe24-3e05-4574-b0d4-72dff3604835");
		bean.setLocalizedName(localizedString_145());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_145() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "070bb62b-b957-4991-a1d3-7fc8c15e22b5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_8() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a64b011e-95e3-4f12-83f3-b4a8f2a1215a");
		bean.setEntity(closeTrunkAsset_2());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_9() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "77687643-3020-490c-a4cf-4db513573de9");
		bean.setNewValue(variable_9());
		bean.setOwner(localEntityProperty_9());
		return bean;
	}

	// Managed
	private Variable variable_9() {
		Variable bean = session().createRaw(Variable.T, "58d96cc3-9cf3-4dd2-a358-8a8ee4c1de10");
		bean.setDefaultValue("custom");
		bean.setLocalizedName(localizedString_146());
		bean.setName("groupId");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_146() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "64982d5c-2a4a-4958-8171-d04fc1cf20a4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "group id")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_9() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "bacd2e07-0ddf-4237-89b8-0b9d5f1e5289");
		bean.setEntity(closeTrunkAsset_2());
		bean.setPropertyName("groupId");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_10() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "30d4b9a3-16e4-45ca-b0d4-2071794b55e1");
		bean.setNewValue(variable_10());
		bean.setOwner(localEntityProperty_10());
		return bean;
	}

	// Managed
	private Variable variable_10() {
		Variable bean = session().createRaw(Variable.T, "eae31fb9-64ac-42da-9ba9-2111cd494d79");
		bean.setLocalizedName(localizedString_147());
		bean.setName("name");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_147() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b2fa4eb8-22c2-4d7e-9ccf-0dd4bbf3a2ab");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "name")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_10() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "fc5210da-fe23-431d-b4de-29aaf2e990f6");
		bean.setEntity(closeTrunkAsset_2());
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_11() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "42a41252-94d1-4290-88aa-3d415c736e2d");
		bean.setNewValue(variable_11());
		bean.setOwner(localEntityProperty_11());
		return bean;
	}

	// Managed
	private Variable variable_11() {
		Variable bean = session().createRaw(Variable.T, "6a54b40f-d0d4-4b12-9261-887a1aa09fc5");
		bean.setDefaultValue("1.0");
		bean.setLocalizedName(localizedString_148());
		bean.setName("version");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_148() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b0f028ed-d764-4728-9b0a-6d0dc976d50a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "version")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_11() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "3f0efae5-e736-4604-9ca7-6726563fd433");
		bean.setEntity(closeTrunkAsset_2());
		bean.setPropertyName("version");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_12() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3f17c24f-d7aa-4668-91ba-a9c44b071218");
		bean.setNewValue(variable_12());
		bean.setOwner(localEntityProperty_12());
		return bean;
	}

	// Managed
	private Variable variable_12() {
		Variable bean = session().createRaw(Variable.T, "c2969901-bbc6-4576-b410-0992432f15c0");
		bean.setDefaultValue("deploy");
		bean.setLocalizedName(localizedString_149());
		bean.setName("transferOperation");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_149() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7b31ba39-cacf-47af-b666-822270ca4ee4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "transfer operation")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_12() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "ea9cf35c-79d8-426f-a1c5-7ff2ce7f9786");
		bean.setEntity(closeTrunkAsset_2());
		bean.setPropertyName("transferOperation");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_5() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "21d52a2e-e4be-4355-b526-08f2806bee20");
		bean.setDisplayName(localizedString_150());
		bean.setInplaceContextCriterion(conjunctionCriterion_5());
		bean.setTemplate(template_15());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_150() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ce663473-ee7c-46fe-a2c4-746ff79b7787");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy Asset")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_5() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "e720d923-9eaa-45e5-a339-dfe523fde4fa");
		bean.setCriteria(Lists.list(typeConditionCriterion_5(), valueConditionCriterion_5()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_5() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "ab927e3a-86c2-4a29-9e22-419702022847");
		bean.setTypeCondition(isAssignableTo_5());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_5() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "ab55a612-fcd6-4343-9cb0-ef842e5f3640");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_5() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "7c1995c1-9f66-4fce-8fa6-f60c6b4c9f41");
		bean.setOperand("^(?!trunk-).*$");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private Template template_15() {
		Template bean = session().createRaw(Template.T, "5caee6ff-5579-4458-97a1-fe5e20c095da");
		bean.setDescription(localizedString_151());
		bean.setName(localizedString_152());
		bean.setPrototype(transferAsset_1());
		bean.setScript(compoundManipulation_4());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_151() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "86d067b8-3343-4d6c-a500-a02264c02e75");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_152() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "74b07b60-c198-47f2-b58b-3eb7a1035e12");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy Template")));
		return bean;
	}

	@Managed
	private TransferAsset transferAsset_1() {
		TransferAsset bean = session().createRaw(TransferAsset.T, "4276f92a-128e-444b-a379-fa9b295672fe");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_4() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "e7a07ec7-16ad-4f0f-90b1-d2e7beb39f92");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_13(), changeValueManipulation_16()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_13() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "e7b13c9f-fb4e-4f8b-88cc-82671e5ff120");
		bean.setNewValue(variable_13());
		bean.setOwner(localEntityProperty_13());
		return bean;
	}

	// Managed
	private Variable variable_13() {
		Variable bean = session().createRaw(Variable.T, "bfce6656-681c-4e1e-bfd1-3dfedcefa24f");
		bean.setLocalizedName(localizedString_153());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_153() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cebc3eef-ba47-4658-9aef-dc40ce4e635e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_13() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "1d9d3ad0-379a-4639-97cd-4db406c184f0");
		bean.setEntity(transferAsset_1());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_6() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "7069d673-358b-46fd-ba20-23157694dc1c");
		bean.setDisplayName(localizedString_154());
		bean.setInplaceContextCriterion(conjunctionCriterion_6());
		bean.setTemplate(template_16());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_154() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c9d3dcd6-17a7-430d-923a-75531aa56979");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Install Asset")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_6() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "73873867-da02-4fed-9914-fdfe9ef71028");
		bean.setCriteria(Lists.list(typeConditionCriterion_6(), valueConditionCriterion_6()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_6() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "1255cdb7-20d4-4364-9ce1-e6ba501a1214");
		bean.setTypeCondition(isAssignableTo_6());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_6() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "cafb67b6-cbeb-41f8-b4e8-6efaff684d0a");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_6() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "489581ce-89d0-47ca-94ce-f47e55d8b5bb");
		bean.setOperand("^(?!trunk-).*$");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private Template template_16() {
		Template bean = session().createRaw(Template.T, "4a5fa9b1-8c1c-4bc8-87cc-31e199f4cf70");
		bean.setDescription(localizedString_155());
		bean.setName(localizedString_156());
		bean.setPrototype(transferAsset_2());
		bean.setScript(compoundManipulation_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_155() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9753bcaa-655f-4348-9855-3361b876c62f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Install Asset Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_156() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9945608d-1348-4565-850a-d16b7569a5d9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Install Asset Template")));
		return bean;
	}

	@Managed
	private TransferAsset transferAsset_2() {
		TransferAsset bean = session().createRaw(TransferAsset.T, "d077a614-ffc4-449e-a25e-9a88eb64d7d0");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_5() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "c010b37e-5175-44b6-a8e0-f01855d3409a");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_14(), changeValueManipulation_15()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_14() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "d00dabf9-d961-43c5-ad16-437007ff5fdc");
		bean.setNewValue(variable_14());
		bean.setOwner(localEntityProperty_14());
		return bean;
	}

	// Managed
	private Variable variable_14() {
		Variable bean = session().createRaw(Variable.T, "d926979f-9302-4482-a60e-627dba84a6b0");
		bean.setLocalizedName(localizedString_157());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_157() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "709c3399-a0ce-484a-b7ab-e50eca508b1a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_14() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "db00ce11-9ef1-4e74-b44c-51767c72ab35");
		bean.setEntity(transferAsset_2());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private Folder folder_67() {
		Folder bean = session().createRaw(Folder.T, "29847ee8-9eb3-430e-bfa2-970d75fbf374");
		bean.setContent(templateServiceRequestAction_5());
		bean.setDisplayName(localizedString_158());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Deploy");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_158() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ef46939d-b27e-41ed-8b6a-4c81032922b2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Deploy")));
		return bean;
	}

	// Managed
	private Folder folder_68() {
		Folder bean = session().createRaw(Folder.T, "43611cf5-7f9a-44a2-9495-9c357f8516a7");
		bean.setContent(templateServiceRequestAction_6());
		bean.setDisplayName(localizedString_159());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Install");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_159() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7df9205b-d78e-4ac9-8544-54e65eae3fd3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Install")));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_15() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1b78227d-8466-48d9-a0bc-be9589efd57c");
		bean.setNewValue("install");
		bean.setOwner(localEntityProperty_15());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_15() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "91359cbb-c006-469f-b007-ad63ee90f95c");
		bean.setEntity(transferAsset_2());
		bean.setPropertyName("transferOperation");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_16() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "31e5a1e1-f4ed-4c1c-bb75-c7b53dd79067");
		bean.setNewValue("deploy");
		bean.setOwner(localEntityProperty_16());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_16() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "0a9ce191-b9fe-4dde-92d6-77d4fbaaf75b");
		bean.setEntity(transferAsset_1());
		bean.setPropertyName("transferOperation");
		return bean;
	}

	// Managed
	private Folder folder_69() {
		Folder bean = session().createRaw(Folder.T, "b508e556-0f9e-4ac3-b01b-0483eb3cf571");
		bean.setContent(templateQueryAction_11());
		bean.setDisplayName(localizedString_160());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Platform Setup");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_160() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f9ad20fb-0fa5-43ca-a392-173c927759f6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Setup")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_11() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "eb71aa46-ff87-48ff-993f-1b64469701d2");
		bean.setDisplayName(localizedString_161());
		bean.setTemplate(template_17());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_161() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "485d2e41-2acb-4d44-bbbe-f1812624de50");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Setup")));
		return bean;
	}

	// Managed
	private Template template_17() {
		Template bean = session().createRaw(Template.T, "14e9e5f9-4b35-4e42-a6b3-f93026a7eb5f");
		bean.setDescription(localizedString_162());
		bean.setName(localizedString_163());
		bean.setPrototype(entityQuery_11());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_162() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "881bd0a9-ba80-49d0-aa36-a0ada015edcc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Setup Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_163() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "26517a7c-6a0c-44f0-a261-609858ec7d14");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Platform Setup Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_11() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "03d153f3-1f66-4e28-bbf3-d9dcf08be0c8");
		bean.setEntityTypeSignature("com.braintribe.model.platformsetup.PlatformSetup");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_1() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "58ff6b9f-f351-4ab1-aecb-a715d6114f04");
		bean.setMetaData(Sets.set(hidden_1()));
		bean.setVariable(variable_2());
		return bean;
	}

	// Managed
	private Hidden hidden_1() {
		Hidden bean = session().createRaw(Hidden.T, "0f13a984-1c8f-4c6b-be73-92f242d69f84");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_1());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_1() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "493270a7-31da-4e8d-b135-da6f975437de");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		bean.setPropertyPath("asset");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_2() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "a31b0204-088c-4132-a134-22daf274a659");
		bean.setMetaData(Sets.set(hidden_2()));
		bean.setVariable(variable_8());
		return bean;
	}

	// Managed
	private Hidden hidden_2() {
		Hidden bean = session().createRaw(Hidden.T, "d423d2ef-e34b-49be-a56e-ed9927ee550d");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_2());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_2() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "f980e4fa-9d7d-40bc-be8f-3342d9115b69");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		bean.setPropertyPath("asset");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_3() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "bc74ff39-163b-4420-ac03-5a1cd698dd17");
		bean.setMetaData(Sets.set(hidden_3()));
		bean.setVariable(variable_6());
		return bean;
	}

	// Managed
	private Hidden hidden_3() {
		Hidden bean = session().createRaw(Hidden.T, "cc81ecdc-670d-4e19-951a-4724caeb5613");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_3());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_3() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "bd43dd73-5bac-40e0-8a88-1ef7b5a2f633");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		bean.setPropertyPath("asset");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_4() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "23eb5ba4-c3c7-4f09-88e8-00fb3679713f");
		bean.setMetaData(Sets.set(hidden_4()));
		bean.setVariable(variable_1());
		return bean;
	}

	// Managed
	private Hidden hidden_4() {
		Hidden bean = session().createRaw(Hidden.T, "0c459a0c-3586-4bb3-b9f3-6fe9fadc47a9");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_4());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_4() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "e9aaccf7-8474-43b7-90a8-628b4992e854");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		bean.setPropertyPath("asset");
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_4() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "50b3c559-718a-4acf-92e6-f19e5efc6b96");
		bean.setName("data-asset-icon");
		bean.setRepresentations(Sets.set(resource_9(), resource_10(), resource_11()));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_8() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d1433f8b-5591-4d70-931e-a5df47fce93e");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/2722/0109/94ae3f5f-478e-477a-943f-9ed8426e6708");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_7() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5c4a2b21-4faf-47c6-a233-e09f14403fcb");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_9() {
		Resource bean = session().createRaw(Resource.T, "889267d3-e25e-48dc-8880-10664d951be9");
		bean.setCreated(newGmtDate(2018, 2, 27, 20, 1, 9, 311));
		bean.setCreator("cortex");
		bean.setFileSize(1115l);
		bean.setMd5("021715cbb823ebb874fdc3dcdb0c4bf4");
		bean.setMimeType("image/png");
		bean.setName("data-asset-16x16.png");
		bean.setResourceSource(moduleSource_8());
		bean.setSpecification(rasterImageSpecification_7());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_9() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e9d40c16-413e-4cbc-b75e-3d6aa993923a");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/2722/0120/0900cae7-d043-4760-8a11-ffe495e1d372");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_8() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8f517b6f-738d-4bba-a9e8-975c7403335e");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_10() {
		Resource bean = session().createRaw(Resource.T, "ab01265b-447b-49f2-8f1b-592a70fadf2a");
		bean.setCreated(newGmtDate(2018, 2, 27, 20, 1, 20, 793));
		bean.setCreator("cortex");
		bean.setFileSize(1510l);
		bean.setMd5("8b2f1e3c6f6999593a722cfac2577b03");
		bean.setMimeType("image/png");
		bean.setName("data-asset-32x32.png");
		bean.setResourceSource(moduleSource_9());
		bean.setSpecification(rasterImageSpecification_8());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_10() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "29369d03-6981-45a0-bf40-d98f071fec2c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1803/2722/0131/e3fc97e4-6b5a-474a-a89e-967155a29256");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_9() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "6a1cde27-ad9e-44d8-8924-cabba8be2e56");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_11() {
		Resource bean = session().createRaw(Resource.T, "4357c8db-e33b-488b-af13-2d84b71af6ee");
		bean.setCreated(newGmtDate(2018, 2, 27, 20, 1, 31, 381));
		bean.setCreator("cortex");
		bean.setFileSize(2220l);
		bean.setMd5("e3412860b7b332f33208ef1bf7b02098");
		bean.setMimeType("image/png");
		bean.setName("data-asset-64x64.png");
		bean.setResourceSource(moduleSource_10());
		bean.setSpecification(rasterImageSpecification_9());
		return bean;
	}

	// Managed
	private Folder folder_70() {
		Folder bean = session().createRaw(Folder.T, "fde20f13-bcd6-4451-b443-c2c9cee19b22");
		bean.setContent(prototypeQueryAction_1());
		bean.setDisplayName(localizedString_164());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Modified Assets");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_164() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "dfb060b5-2b3c-4dfb-934a-4dba043327a8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modified Assets")));
		return bean;
	}

	// Managed
	private PrototypeQueryAction prototypeQueryAction_1() {
		PrototypeQueryAction bean = session().createRaw(PrototypeQueryAction.T, "6298dcf1-7247-47c5-9eb1-6c88cf01c4bf");
		bean.setDisplayName(localizedString_165());
		bean.setQuery(entityQuery_12());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_165() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "56494f78-3e17-4495-bb95-fa30470a49f4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modified Assets")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_12() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "fed44617-9160-40bf-a386-54e8a8ade276");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_10());
		return bean;
	}

	// Managed
	private Restriction restriction_10() {
		Restriction bean = session().createRaw(Restriction.T, "eee12ff6-7379-4e19-a7b2-8ea3e7d4d862");
		bean.setCondition(valueComparison_10());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_10() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "09f9c509-d28c-4763-8fbb-0c8befac7b84");
		bean.setLeftOperand(propertyOperand_17());
		bean.setOperator(Operator.equal);
		bean.setRightOperand(Boolean.TRUE);
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_17() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "da3f5587-8e34-4f8c-bc0c-e9a61565405c");
		bean.setPropertyName("hasUnsavedChanges");
		return bean;
	}

	// Managed
	private Folder folder_71() {
		Folder bean = session().createRaw(Folder.T, "b83072f7-df42-408e-b62a-c8518b50df3b");
		bean.setContent(templateServiceRequestAction_7());
		bean.setDisplayName(localizedString_167());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Add dependencies");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_167() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "98cc0f50-6fd5-448c-87dd-3b4fd558a51b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add dependencies")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_7() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "54076f22-55a2-4835-ac9e-4c531eadc851");
		bean.setDisplayName(localizedString_168());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(typeConditionCriterion_7());
		bean.setTemplate(template_18());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_168() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "50d9903d-5082-4be1-8201-bb317d8e2ded");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add dependency SRA")));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_7() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "4970bba9-b711-405d-9b22-f9dc66295038");
		bean.setTypeCondition(isAssignableTo_7());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_7() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "c950c2b7-d3c5-4de2-87cf-2212c5467e22");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private Template template_18() {
		Template bean = session().createRaw(Template.T, "5be188eb-b804-4872-b123-90c81c48abe7");
		bean.setDescription(localizedString_169());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_6(), dynamicPropertyMetaDataAssignment_7(), dynamicPropertyMetaDataAssignment_5(), dynamicPropertyMetaDataAssignment_9(), dynamicPropertyMetaDataAssignment_10(), dynamicPropertyMetaDataAssignment_11(), dynamicPropertyMetaDataAssignment_12()));
		bean.setName(localizedString_170());
		bean.setPrototype(addAssetDependencies_1());
		bean.setScript(compoundManipulation_6());
		bean.setTechnicalName("AddDependenciesTemplate");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_169() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7ea889ff-0f7e-41a4-9acf-e53e70c82324");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add dependencies")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_170() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a0602011-80f4-467f-9b11-ba631e7c8cf8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add dependencies")));
		return bean;
	}

	@Managed
	private AddAssetDependencies addAssetDependencies_1() {
		AddAssetDependencies bean = session().createRaw(AddAssetDependencies.T, "1db75575-aeb4-4026-8a43-2fcda12503ab");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_6() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "480bc4be-ab2e-4388-91b2-bf745d55d71a");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_18(), changeValueManipulation_17(), changeValueManipulation_19(), changeValueManipulation_20(), changeValueManipulation_21(), changeValueManipulation_22(), changeValueManipulation_23()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_17() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "35abab1d-6a67-4c7b-9690-a9164d9d0d14");
		bean.setNewValue(variable_15());
		bean.setOwner(localEntityProperty_17());
		return bean;
	}

	@Managed
	private Variable variable_15() {
		Variable bean = session().createRaw(Variable.T, "9cd25bee-ff8b-4668-a2bd-dda2d3070546");
		bean.setLocalizedName(localizedString_171());
		bean.setName("dependencies");
		bean.setTypeSignature("list<com.braintribe.model.asset.PlatformAsset>");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_171() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e9e6ce95-4f9d-46b1-9938-4b4f0cbf64de");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "dependencies")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_17() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "b2fca3a5-8a13-4096-b4bb-bb35d3bc09d7");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("dependencies");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_18() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "b571708b-3f24-4ee3-9841-6fa6f2d1e2f0");
		bean.setNewValue(variable_16());
		bean.setOwner(localEntityProperty_18());
		return bean;
	}

	@Managed
	private Variable variable_16() {
		Variable bean = session().createRaw(Variable.T, "9507ecbb-761b-4d89-a381-01b3828e275f");
		bean.setLocalizedName(localizedString_172());
		bean.setName("depender");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_172() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "dee7879e-f665-46b9-b8fe-3c078bdf81a3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "depender")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_18() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "4397ad04-9c17-4939-851b-ce5e86009af2");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("depender");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_19() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "99494aff-5ef7-42d1-b870-4b82f4cd72d7");
		bean.setNewValue(variable_17());
		bean.setOwner(localEntityProperty_19());
		return bean;
	}

	@Managed
	private Variable variable_17() {
		Variable bean = session().createRaw(Variable.T, "69ee4c7a-5250-422f-8bb6-62649d3cf6dd");
		bean.setLocalizedName(localizedString_173());
		bean.setName("asGlobalSetupCandidate");
		bean.setTypeSignature("boolean");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_173() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b3af982b-995b-4a97-863f-80117dc5ccf8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "as global setup candidate")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_19() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "787df2cd-4efb-47e6-b818-f42c80fa0c3a");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("asGlobalSetupCandidate");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_5() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "5aff9123-c562-472a-9876-6bd6fbd56ae4");
		bean.setMetaData(Sets.set(hidden_5(), priority_8()));
		bean.setVariable(variable_16());
		return bean;
	}

	// Managed
	private Hidden hidden_5() {
		Hidden bean = session().createRaw(Hidden.T, "9184a7ca-7272-4ea1-8cc6-28988d610fb6");
		bean.setInherited(Boolean.TRUE);
		bean.setSelector(propertyValueComparator_5());
		return bean;
	}

	// Managed
	private PropertyValueComparator propertyValueComparator_5() {
		PropertyValueComparator bean = session().createRaw(PropertyValueComparator.T, "47c0b0f3-475a-4b41-a724-c2aa295b6409");
		bean.setOperator(com.braintribe.model.meta.selector.Operator.notEqual);
		bean.setPropertyPath("depender");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_20() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "3a0c65a5-d799-45c2-84c0-a6bf8f3ed2b1");
		bean.setNewValue(variable_18());
		bean.setOwner(localEntityProperty_20());
		return bean;
	}

	@Managed
	private Variable variable_18() {
		Variable bean = session().createRaw(Variable.T, "80e6b7a9-d21f-4498-bb3a-1d2c5c9b0440");
		bean.setLocalizedName(localizedString_174());
		bean.setName("asDesigntimeOnly");
		bean.setTypeSignature("boolean");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_174() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6a395122-5c05-453e-8036-ca3d1ef3b678");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "as designtime only")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_20() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "96274954-96c1-4d3d-ba11-e7e3e61df790");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("asDesigntimeOnly");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_21() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1ff58766-1cff-42ba-b609-91c06024c31d");
		bean.setNewValue(variable_19());
		bean.setOwner(localEntityProperty_21());
		return bean;
	}

	@Managed
	private Variable variable_19() {
		Variable bean = session().createRaw(Variable.T, "7efc9f94-5b19-4d68-8ba7-fdd4253d7b86");
		bean.setLocalizedName(localizedString_175());
		bean.setName("asRuntimeOnly");
		bean.setTypeSignature("boolean");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_175() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "89d4ae41-fbb9-4dcc-b5c7-676c92277b8e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "as runtime only")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_21() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "e0c873e2-3adf-4d29-8ca7-c9ee72137bf3");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("asRuntimeOnly");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_6() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "43896e24-4e31-408f-a209-405b60b222d4");
		bean.setMetaData(Sets.set(priority_1()));
		bean.setVariable(variable_15());
		return bean;
	}

	// Managed
	private Priority priority_1() {
		Priority bean = session().createRaw(Priority.T, "0c4aec59-fe2c-48b4-8923-ba0ca69e0749");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.9d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_7() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "646a2561-177c-4176-8d82-69d89912ba19");
		bean.setMetaData(Sets.set(priority_2()));
		bean.setVariable(variable_17());
		return bean;
	}

	// Managed
	private Priority priority_2() {
		Priority bean = session().createRaw(Priority.T, "5eaf6e4b-9177-4602-a18b-8faba86221c2");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.8d);
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_22() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "36fcf96b-5e2b-4712-84ba-967dba13c4a0");
		bean.setNewValue(variable_20());
		bean.setOwner(localEntityProperty_22());
		return bean;
	}

	@Managed
	private Variable variable_20() {
		Variable bean = session().createRaw(Variable.T, "e22cb555-2766-4344-8d24-3b52350608ba");
		bean.setLocalizedName(localizedString_176());
		bean.setName("forStages");
		bean.setTypeSignature("set<string>");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_176() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "225b5958-935e-48db-89f3-a3ee9d85fee8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "for stages")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_22() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "3371a0a2-4c86-402d-8f91-c92c870d2b19");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("forStages");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_23() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "1ccbfb0f-53b6-4b48-ac90-f5ca628ce699");
		bean.setNewValue(variable_21());
		bean.setOwner(localEntityProperty_23());
		return bean;
	}

	@Managed
	private Variable variable_21() {
		Variable bean = session().createRaw(Variable.T, "4e171d0f-ffb7-4487-84c2-c31bedea013f");
		bean.setLocalizedName(localizedString_177());
		bean.setName("customSelector");
		bean.setTypeSignature("com.braintribe.model.asset.selector.DependencySelector");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_177() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f362d77f-8ecb-41e3-83b0-e0099f4ee06b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "custom selector")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_23() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "a0e81461-796e-4aad-8d9b-69787bc76dd2");
		bean.setEntity(addAssetDependencies_1());
		bean.setPropertyName("customSelector");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_9() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "9beb846b-12cd-4a18-a61c-ef58c4066ec5");
		bean.setMetaData(Sets.set(priority_4()));
		bean.setVariable(variable_21());
		return bean;
	}

	// Managed
	private Priority priority_4() {
		Priority bean = session().createRaw(Priority.T, "3b0599cf-833b-428a-a1cc-b8937bc785a4");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.4d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_10() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "ce34e1e3-5e50-489a-b8b6-4e4a57688137");
		bean.setMetaData(Sets.set(priority_5()));
		bean.setVariable(variable_18());
		return bean;
	}

	// Managed
	private Priority priority_5() {
		Priority bean = session().createRaw(Priority.T, "22c3e8c0-1c9e-45c0-a30d-4ff8747c7c7a");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.7d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_11() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "c91d0feb-3982-449f-8a32-77ae98e33985");
		bean.setMetaData(Sets.set(priority_6()));
		bean.setVariable(variable_19());
		return bean;
	}

	// Managed
	private Priority priority_6() {
		Priority bean = session().createRaw(Priority.T, "6e98bc12-b384-4bcf-9a50-53759ee0220a");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.6d);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_12() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "156e6e23-bf9e-49d9-bff5-ea18b4751fcc");
		bean.setMetaData(Sets.set(priority_7()));
		bean.setVariable(variable_20());
		return bean;
	}

	// Managed
	private Priority priority_7() {
		Priority bean = session().createRaw(Priority.T, "b02556fa-1220-4fcc-9da9-683f3a0ca60f");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(0.5d);
		return bean;
	}

	// Managed
	private Priority priority_8() {
		Priority bean = session().createRaw(Priority.T, "3a5d2db6-c94b-4491-9156-4d189f0557dc");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	// Managed
	private Folder folder_72() {
		Folder bean = session().createRaw(Folder.T, "a079b105-1375-4683-9194-5c4a0232b3f5");
		bean.setContent(templateServiceRequestAction_8());
		bean.setDisplayName(localizedString_178());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("renameAsset");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_178() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "79279da0-e1ed-431e-b6a4-2f92c9cb962a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Rename asset")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_8() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "db80e590-64c7-4af5-afbe-1ecebd097921");
		bean.setDisplayName(localizedString_179());
		bean.setInplaceContextCriterion(conjunctionCriterion_7());
		bean.setTemplate(template_19());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_179() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "71c471fa-c60c-43c7-8aa1-b78ca82b94d9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Rename Asset SRA")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_7() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "62fe67fc-97e3-4450-99ec-99d279d49a11");
		bean.setCriteria(Lists.list(valueConditionCriterion_7(), typeConditionCriterion_8()));
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_7() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "0cf1ea42-7d2f-4ad1-93d1-0ecebcf15572");
		bean.setOperand("^(?!trunk-).*$");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_8() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "17fe8c90-25ee-4f3c-aa56-203fd3e086fd");
		bean.setTypeCondition(isAssignableTo_8());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_8() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "fa7a013b-19d1-4026-8f70-0bb0722d4015");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private Template template_19() {
		Template bean = session().createRaw(Template.T, "78c8fca5-5f2e-4797-883a-c38af4feba03");
		bean.setDescription(localizedString_184());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_13()));
		bean.setName(localizedString_185());
		bean.setPrototype(renameAsset_1());
		bean.setScript(compoundManipulation_7());
		return bean;
	}

	@Managed
	private RenameAsset renameAsset_1() {
		RenameAsset bean = session().createRaw(RenameAsset.T, "69fad540-f766-4db8-9487-3d0236217728");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_24() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "411ef813-b4f9-40d7-a416-91f87fd7c35a");
		bean.setNewValue(variable_22());
		bean.setOwner(localEntityProperty_24());
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_7() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "25e35dfc-691b-4b82-ac75-8973743692dc");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_24(), changeValueManipulation_25(), changeValueManipulation_26(), changeValueManipulation_27()));
		return bean;
	}

	@Managed
	private Variable variable_22() {
		Variable bean = session().createRaw(Variable.T, "4d78f8be-ca01-4d46-a3de-e8fbf3b7f090");
		bean.setLocalizedName(localizedString_180());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_180() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d58a5ebc-66d8-4484-be53-4d81e8eefa41");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_24() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "da1dab16-43ae-4f85-a7c0-cfd87841a6ff");
		bean.setEntity(renameAsset_1());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_25() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "66378fb8-e8c0-4690-b2c3-f4ece75ebb9e");
		bean.setNewValue(variable_23());
		bean.setOwner(localEntityProperty_25());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_25() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6e31c803-199a-4eda-b94b-ed442e583926");
		bean.setEntity(renameAsset_1());
		bean.setPropertyName("groupId");
		return bean;
	}

	// Managed
	private Variable variable_23() {
		Variable bean = session().createRaw(Variable.T, "880b5d36-7153-4795-adc8-64067eceff52");
		bean.setLocalizedName(localizedString_181());
		bean.setName("groupId");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_181() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7b3976eb-3efe-462a-b7ba-78726b353cd5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "group id")));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_26() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "eb12723d-9323-4a83-868b-a0dde67f2d68");
		bean.setNewValue(variable_24());
		bean.setOwner(localEntityProperty_26());
		return bean;
	}

	// Managed
	private Variable variable_24() {
		Variable bean = session().createRaw(Variable.T, "713bbb76-9563-4139-af06-82e26175c745");
		bean.setLocalizedName(localizedString_182());
		bean.setName("name");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_182() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "127d97b7-a12d-4d27-a9b3-593c4752be35");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "name")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_26() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "28c80cee-cd40-4d3d-88ef-20e83c59ae96");
		bean.setEntity(renameAsset_1());
		bean.setPropertyName("name");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_27() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "ea802ea3-cb2c-4a4f-8ef5-2b49db2a1ba6");
		bean.setNewValue(variable_25());
		bean.setOwner(localEntityProperty_27());
		return bean;
	}

	// Managed
	private Variable variable_25() {
		Variable bean = session().createRaw(Variable.T, "f2dd197c-5de5-4346-b7b5-1dcaf5e1133d");
		bean.setLocalizedName(localizedString_183());
		bean.setName("version");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_183() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "58b9f034-c576-488c-9b9d-2a23bcb3aab5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "version")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_27() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "8fb2ad56-33e9-405e-a970-414c452d7d96");
		bean.setEntity(renameAsset_1());
		bean.setPropertyName("version");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_13() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "22352974-ea7c-4d59-a6ea-c910fff9681e");
		bean.setMetaData(Sets.set(hidden_6()));
		bean.setVariable(variable_22());
		return bean;
	}

	// Managed
	private Hidden hidden_6() {
		Hidden bean = session().createRaw(Hidden.T, "0959ac8c-6904-4653-a57c-4b8dff541593");
		bean.setInherited(Boolean.TRUE);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_184() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "987cd397-49f4-49e4-ac10-4273e97c0c1a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Rename Asset")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_185() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "80255916-a3e5-4cbb-bac3-407aa3acc4c0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Rename Asset")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_8() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "2aed28db-1968-4ced-a61b-33ce9f435dcd");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_1(), changeValueManipulation_28()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_28() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "66815cf2-9297-4464-bfc6-b818eb38ab19");
		bean.setNewValue(Boolean.TRUE);
		bean.setOwner(localEntityProperty_28());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_28() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "905d6565-5a61-4ef8-91f4-356e136f7b92");
		bean.setEntity(mergeTrunkAsset_1());
		bean.setPropertyName("mergeIntoPredecessor");
		return bean;
	}

	// Managed
	private Folder folder_73() {
		Folder bean = session().createRaw(Folder.T, "e0d84f73-02fd-4e6e-8f58-3881762e9a0f");
		bean.setContent(templateServiceRequestAction_9());
		bean.setDisplayName(localizedString_186());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("mergeIntoAsset");
		bean.setParent(folder_62());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_186() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a4d01a18-b1d7-4c9d-a16e-c7a1dc372352");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into asset")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_9() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "92098d5a-f885-40bb-860e-cba36dd1f089");
		bean.setDisplayName(localizedString_187());
		bean.setForceFormular(Boolean.TRUE);
		bean.setInplaceContextCriterion(conjunctionCriterion_8());
		bean.setTemplate(template_20());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_187() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "655c7be1-53f9-4472-b233-205bad60f98f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into asset")));
		return bean;
	}

	// Managed
	private Template template_20() {
		Template bean = session().createRaw(Template.T, "4ac454bb-324f-443c-b778-697b13920b63");
		bean.setDescription(localizedString_188());
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_14(), dynamicPropertyMetaDataAssignment_15()));
		bean.setName(localizedString_189());
		bean.setPrototype(mergeTrunkAsset_3());
		bean.setScript(compoundManipulation_9());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_188() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0553e3d0-6fd7-4d41-94f4-c2c11c05153e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into asset")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_189() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d15996e5-f0c0-4fbd-9aec-0f475c0fd0c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Merge into asset")));
		return bean;
	}

	// Managed
	private ConjunctionCriterion conjunctionCriterion_8() {
		ConjunctionCriterion bean = session().createRaw(ConjunctionCriterion.T, "4a1d5a38-d3fd-4b4a-a442-3dda06d1b1b5");
		bean.setCriteria(Lists.list(typeConditionCriterion_9(), valueConditionCriterion_8()));
		return bean;
	}

	// Managed
	private TypeConditionCriterion typeConditionCriterion_9() {
		TypeConditionCriterion bean = session().createRaw(TypeConditionCriterion.T, "48e7bd64-2440-424d-9996-c3a922877286");
		bean.setTypeCondition(isAssignableTo_9());
		return bean;
	}

	// Managed
	private IsAssignableTo isAssignableTo_9() {
		IsAssignableTo bean = session().createRaw(IsAssignableTo.T, "9fcb4cb7-41fd-468d-93f6-f85ebc024ad0");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private ValueConditionCriterion valueConditionCriterion_8() {
		ValueConditionCriterion bean = session().createRaw(ValueConditionCriterion.T, "61e9f01b-f8fe-4aff-942b-aa6379483241");
		bean.setOperand("trunk-.*");
		bean.setOperator(ComparisonOperator.matches);
		bean.setPropertyPath("name");
		return bean;
	}

	@Managed
	private MergeTrunkAsset mergeTrunkAsset_3() {
		MergeTrunkAsset bean = session().createRaw(MergeTrunkAsset.T, "67d17cfb-5f85-4b13-b70e-e5dcaf09ec11");
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_9() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "a3fbb193-e673-4ece-977b-5977968e4d89");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_29(), changeValueManipulation_30()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_29() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "62484646-9d78-45dd-b0c7-10be1005ce47");
		bean.setNewValue(variable_26());
		bean.setOwner(localEntityProperty_29());
		return bean;
	}

	@Managed
	private Variable variable_26() {
		Variable bean = session().createRaw(Variable.T, "69149c17-d76f-42a3-a166-848424fe8fb5");
		bean.setLocalizedName(localizedString_190());
		bean.setName("asset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_190() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d340f881-e6e3-4c9d-ae20-3b74363c0ff6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_29() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "859590db-9664-43f1-b242-eb12c1537c38");
		bean.setEntity(mergeTrunkAsset_3());
		bean.setPropertyName("asset");
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_14() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "2543d3b6-d647-43d2-9128-6deaa3bd0569");
		bean.setMetaData(Sets.set(hidden_7()));
		bean.setVariable(variable_26());
		return bean;
	}

	// Managed
	private Hidden hidden_7() {
		Hidden bean = session().createRaw(Hidden.T, "d95b68e4-7a06-4b8e-b59f-282aeca4041d");
		bean.setInherited(Boolean.TRUE);
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_15() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "18ef51b9-fa41-498d-8365-a473aac3afbf");
		bean.setMetaData(Sets.set(priority_9()));
		bean.setVariable(variable_26());
		return bean;
	}

	// Managed
	private Priority priority_9() {
		Priority bean = session().createRaw(Priority.T, "333097ac-ef0a-4247-954d-9a4fe6c9122a");
		bean.setInherited(Boolean.TRUE);
		bean.setPriority(1.0d);
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_30() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "d294d8e4-89fb-4980-98b1-51cf71d15081");
		bean.setNewValue(variable_27());
		bean.setOwner(localEntityProperty_30());
		return bean;
	}

	// Managed
	private Variable variable_27() {
		Variable bean = session().createRaw(Variable.T, "5f9142f0-d8a1-4ccb-b5dc-e7eca8d6eedf");
		bean.setLocalizedName(localizedString_191());
		bean.setName("targetAsset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_191() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "abc8017c-f986-4d1e-94e6-2f89dd33ed6d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "target asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_30() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "766464f9-95ab-47a8-9463-91bfe733a9d3");
		bean.setEntity(mergeTrunkAsset_3());
		bean.setPropertyName("targetAsset");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_31() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "f5cc6d2d-7e3f-4d59-9b57-b6f449ae6834");
		bean.setNewValue(variable_28());
		bean.setOwner(localEntityProperty_31());
		return bean;
	}

	// Managed
	private Variable variable_28() {
		Variable bean = session().createRaw(Variable.T, "24982d30-1925-4649-90a0-81de60a1776a");
		bean.setLocalizedName(localizedString_192());
		bean.setName("mergeIntoPredecessor");
		bean.setTypeSignature("boolean");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_192() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c8449bce-28be-4659-bdb4-e6d35b1f5ac8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "merge into predecessor")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_31() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "6f62796c-c3ad-423b-96f3-36586050ce72");
		bean.setEntity(mergeTrunkAsset_2());
		bean.setPropertyName("mergeIntoPredecessor");
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_32() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "9c755654-a1df-4cd0-bc9d-d7b4ba4e76f5");
		bean.setNewValue(variable_29());
		bean.setOwner(localEntityProperty_32());
		return bean;
	}

	// Managed
	private Variable variable_29() {
		Variable bean = session().createRaw(Variable.T, "c399c307-6f40-4701-b341-2aebc3300743");
		bean.setLocalizedName(localizedString_193());
		bean.setName("targetAsset");
		bean.setTypeSignature("com.braintribe.model.asset.PlatformAsset");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_193() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7bdb015e-dbdb-4fd0-8e87-425d506ae648");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "target asset")));
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_32() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "9325c24b-0c79-4c0b-b5c7-6993c21d2e51");
		bean.setEntity(mergeTrunkAsset_2());
		bean.setPropertyName("targetAsset");
		return bean;
	}

	// Managed
	private Folder folder_74() {
		Folder bean = session().createRaw(Folder.T, "a49d3be4-9977-42a9-8ddb-3135554350ca");
		bean.setDisplayName(localizedString_194());
		bean.setName("$showLog");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_194() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ae19555c-a109-4c8c-894c-028b9517b1c1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Log")));
		return bean;
	}

	// Managed
	private Folder folder_75() {
		Folder bean = session().createRaw(Folder.T, "1ffd8e4d-f1f2-44c4-86e2-1b5217601af4");
		bean.setContent(templateQueryAction_12());
		bean.setDisplayName(localizedString_195());
		bean.setName("Modules");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_195() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "31de5e1f-ecb0-4e82-8593-b400b872cd70");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modules")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_12() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "ab261de0-2f8e-4d2e-a093-e77905f60a81");
		bean.setDisplayName(localizedString_196());
		bean.setTemplate(template_21());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_196() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4568caa8-7336-4f0f-97bb-dd03d83457a6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modules")));
		return bean;
	}

	// Managed
	private Template template_21() {
		Template bean = session().createRaw(Template.T, "4c448c01-3e18-4365-bdd4-d71367c59f03");
		bean.setDescription(localizedString_197());
		bean.setName(localizedString_198());
		bean.setPrototype(entityQuery_13());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_197() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2f7e888f-f255-4338-a7c9-91ef8c099eef");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modules Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_198() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "da8262b3-904e-44e3-b9fc-1fa61e58c95e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Modules Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_13() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "0dac76b9-fb20-4b4e-947f-a54fbedc3a72");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_11());
		return bean;
	}

	// Managed
	private Restriction restriction_11() {
		Restriction bean = session().createRaw(Restriction.T, "2e648816-ff56-4244-8298-6b715c8fe2a7");
		bean.setCondition(valueComparison_11());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_11() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "89c512b8-34ae-4e6b-9fc3-8741ba49a842");
		bean.setLeftOperand(entitySignature_9());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.TribefireModule");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_9() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "9eb3d168-ebff-4e76-92e1-3489934797f7");
		bean.setOperand(propertyOperand_18());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_18() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "433cc75d-eb10-4453-8d89-c06718f4b7d1");
		bean.setPropertyName("nature");
		return bean;
	}

	// Managed
	private Folder folder_76() {
		Folder bean = session().createRaw(Folder.T, "ee7fbcb6-e23b-4fbe-a888-9d3739418a5f");
		bean.setContent(templateQueryAction_13());
		bean.setDisplayName(localizedString_199());
		bean.setName("Priming Modules");
		bean.setParent(folder_53());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_199() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2d44ee43-d966-457c-89b6-184e86825e12");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Priming Modules")));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_13() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "8e62b168-31e7-42d9-8326-b2a20862332b");
		bean.setDisplayName(localizedString_200());
		bean.setTemplate(template_22());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_200() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3977810a-a4d9-4c18-a21f-0c1b6f897937");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Priming Modules")));
		return bean;
	}

	// Managed
	private Template template_22() {
		Template bean = session().createRaw(Template.T, "3dda5d58-7877-4776-a2a8-fc0c2b19033f");
		bean.setDescription(localizedString_201());
		bean.setName(localizedString_202());
		bean.setPrototype(entityQuery_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_201() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "22181f0a-8476-4d19-b4d5-d67343ae6552");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Priming Modules Template")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_202() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cbaeabee-62a3-4689-9109-fe73d7562196");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Priming Modules Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_14() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "e93d465b-ac62-4024-9b26-63ea59e64017");
		bean.setEntityTypeSignature("com.braintribe.model.asset.PlatformAsset");
		bean.setRestriction(restriction_12());
		return bean;
	}

	// Managed
	private Restriction restriction_12() {
		Restriction bean = session().createRaw(Restriction.T, "4c9e5b88-90fd-42b3-909c-6abcaaec2654");
		bean.setCondition(valueComparison_12());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_12() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "9c7825d9-885c-4d00-8413-9b558ce27c02");
		bean.setLeftOperand(entitySignature_10());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("com.braintribe.model.asset.natures.PrimingModule");
		return bean;
	}

	// Managed
	private EntitySignature entitySignature_10() {
		EntitySignature bean = session().createRaw(EntitySignature.T, "fde20eaf-c057-417d-a555-dd1b6e412a25");
		bean.setOperand(propertyOperand_19());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_19() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "1e8f301d-97d8-4645-80f0-a087d8d5a906");
		bean.setPropertyName("nature");
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