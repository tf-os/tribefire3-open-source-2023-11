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
package tribefire.cortex.assets.user_statistics_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.meta.QueryString;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.user_statistics_wb.initializer.wire.contract.UserStatisticsWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class UserStatisticsWbInitializerSpace extends AbstractInitializerSpace implements UserStatisticsWbInitializerContract {

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
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8d6eba53-213b-40eb-bbe3-0b87a68b6cb5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/1923/3320/c7466a20-cb9a-4e60-abfa-c10a3f6af889");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ddd0ed25-88fe-42d5-8556-0e7168b55f70");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "90fdf23c-57c1-4bf4-bff5-aad4540e2d62");
		bean.setCreated(newGmtDate(2018, 8, 19, 21, 33, 20, 698));
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
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bea154b7-1c95-4a38-a3ef-c42969a0c95c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/1923/3320/8fe80f8c-916c-41fe-8150-118849732e93");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "9fe54cf3-9a91-4468-9e5d-7d36798ccf1b");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "9244e0bd-068d-4b8b-a0ef-668724b4b5bf");
		bean.setCreated(newGmtDate(2018, 8, 19, 21, 33, 20, 720));
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
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "b6f76482-96aa-4464-a692-db04f3d4c191");
		bean.setDisplayName(localizedString_1());
		bean.setFolders(Lists.list(folder_49()));
		bean.setName("root");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2433fc3e-dca9-451d-93bd-6faa288d36ae");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "root")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "00766a25-caba-47d8-9032-18ec5d124cc8");
		bean.setDisplayName(localizedString_2());
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d068dbd9-6a7e-423e-8a2c-cc0c23d3b5ce");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "homeFolder")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_3() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "c58f08d3-51a7-4017-b5e7-84307036c46c");
		bean.setDisplayName(localizedString_3());
		bean.setFolders(Lists.list(folder_1()));
		bean.setName("actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "822696bc-8952-43f0-8699-1c2529433823");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_4() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "d64ce9b1-0719-4431-9e1a-320a358a91bb");
		bean.setDisplayName(localizedString_4());
		bean.setFolders(Lists.list(folder_16()));
		bean.setName("headerbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c714e917-5c57-4c10-b5b6-98605893ce80");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "headerbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_5() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "6b146d8f-d91c-4f96-85fd-e921eb1a7750");
		bean.setDisplayName(localizedString_5());
		bean.setFolders(Lists.list(folder_42()));
		bean.setName("global-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "615c9d6d-9cee-40d1-a41e-1520759bf787");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "global-actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_6() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "a3604618-0df9-484d-bf8c-d7d7312133c9");
		bean.setDisplayName(localizedString_6());
		bean.setFolders(Lists.list(folder_28()));
		bean.setName("tab-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5ba23614-c0b1-45ce-96a0-2428c0512a9e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tab-actionbar")));
		return bean;
	}

	// Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "99bd05a9-4a64-4be7-991c-803f09717458");
		bean.setDisplayName(localizedString_55());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_2(), folder_3(), folder_4(), folder_5(), folder_6(), folder_7(), folder_8(), folder_9(), folder_10(),
				folder_11(), folder_12(), folder_13(), folder_14(), folder_15()));
		return bean;
	}

	// Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "b9ed6dab-ca63-49d0-9f14-6fe17e4ba0cd");
		bean.setDisplayName(localizedString_68());
		bean.setName("$exchangeContentView");
		return bean;
	}

	// Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "686f7644-2c52-4aae-b69b-d0bf3d8a9d1b");
		bean.setDisplayName(localizedString_59());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "fede1fea-db0b-44f2-9aef-c5a2d5135261");
		bean.setDisplayName(localizedString_60());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "d0ac6f8e-e552-4f2d-ba31-f21693285da2");
		bean.setDisplayName(localizedString_61());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "6950b291-b910-4ab2-88d2-1772d29ea6df");
		bean.setDisplayName(localizedString_62());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "6ebda764-fa32-46c4-8ad0-96bf9e6fb2cd");
		bean.setDisplayName(localizedString_63());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "0145b4b3-1b7b-4525-b23b-9b60b686b84c");
		bean.setDisplayName(localizedString_64());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "981088ac-fb90-47ec-abd7-788832301a6a");
		bean.setDisplayName(localizedString_65());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "540ed122-6895-4073-baca-ac45f547406d");
		bean.setDisplayName(localizedString_66());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "6954118a-0f12-473f-ae6b-a6cc3973bb12");
		bean.setDisplayName(localizedString_67());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "f0c87ccf-04ec-459e-abc9-1e5ad7e7de7b");
		bean.setDisplayName(localizedString_70());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "3f07ca98-73f6-44d9-89ae-b87d356624b7");
		bean.setDisplayName(localizedString_71());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "1f74854a-c7d7-45e5-874b-8855e00d8545");
		bean.setDisplayName(localizedString_72());
		bean.setIcon(adaptiveIcon_1());
		bean.setName("$executeServiceRequest");
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "dba17df2-0536-4872-9a2d-50695c399f21");
		bean.setDisplayName(localizedString_69());
		bean.setName("$addToClipboard");
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "221da335-af79-4565-95ab-9f8d1efeb3cb");
		bean.setDisplayName(localizedString_56());
		bean.setName("headerbar");
		bean.setSubFolders(Lists.list(folder_17(), folder_18(), folder_19(), folder_20(), folder_25()));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "c6a2dba6-a729-4b8a-a561-7b8ce9625d01");
		bean.setDisplayName(localizedString_23());
		bean.setName("tb_Logo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "95a00785-00bf-4c4c-b9ba-0d30ed146b13");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tb_ Logo")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "8a374860-742e-4771-9930-f36e59b1d88f");
		bean.setDisplayName(localizedString_24());
		bean.setName("$quickAccess-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "98960dd9-254f-4343-82df-5ed56b31db9c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access-slot")));
		return bean;
	}

	// Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "02659c9f-698e-4c43-b335-bf47f455521d");
		bean.setDisplayName(localizedString_25());
		bean.setName("$globalState-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "dcef6514-a4e6-4484-b378-407a14b6b8ea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global State-slot")));
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "fc39f3c4-d2e3-4b98-ad4f-a5b7aafe308e");
		bean.setDisplayName(localizedString_26());
		bean.setName("$settingsMenu");
		bean.setSubFolders(Lists.list(folder_21(), folder_22(), folder_23(), folder_24()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7a39b3b1-184e-403a-8201-466f8563b250");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Settings Menu")));
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "8d485026-1ab1-478e-a91d-58648fd385e6");
		bean.setDisplayName(localizedString_27());
		bean.setName("$reloadSession");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f2807c58-98a6-4f67-9e40-8db32aeb56ff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Reload Session")));
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "714ea871-420a-4d77-abb5-c5fdf8e34211");
		bean.setDisplayName(localizedString_28());
		bean.setName("$showSettings");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f7992d6f-31de-490c-a95a-e48868f7a26b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Settings")));
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "c35071ac-e50e-401d-9224-bb2aa19721d0");
		bean.setDisplayName(localizedString_29());
		bean.setName("$uiTheme");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fe99e333-84d3-4e35-8c64-e1ef1cdbb3cb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ui Theme")));
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "c7adc4ba-6f8a-4850-a7a8-bf60aa20545e");
		bean.setDisplayName(localizedString_30());
		bean.setName("$showAbout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "60417a4a-3c0f-40c6-a9e1-b446e0a1d2e1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show About")));
		return bean;
	}

	// Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "bedbfeee-8e77-4a07-8834-dd84c1fdc433");
		bean.setDisplayName(localizedString_31());
		bean.setName("$userMenu");
		bean.setSubFolders(Lists.list(folder_26(), folder_27()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5a17ecc8-2510-4417-a3d9-05d1a459a4eb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Menu")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "a086763e-c1c3-4481-8a06-0114c6773e15");
		bean.setDisplayName(localizedString_32());
		bean.setName("$showUserProfile");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3c77b9de-3123-4b29-9ce0-e76b9c75bcd3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show User Profile")));
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "84a40f79-44b0-4eb7-8442-6b24909b692a");
		bean.setDisplayName(localizedString_33());
		bean.setName("$showLogout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cfbf542c-6f06-41f8-ba84-7c56a49cd1a0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Logout")));
		return bean;
	}

	// Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "5924d27b-3f90-4b16-bcfe-42eac2a8ca2c");
		bean.setDisplayName(localizedString_57());
		bean.setName("tab-actionbar");
		bean.setSubFolders(Lists.list(folder_29(), folder_35()));
		return bean;
	}

	// Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "43f8ae73-3ac8-43e6-ad6e-b334c3f046e1");
		bean.setDisplayName(localizedString_35());
		bean.setName("$explorer");
		bean.setSubFolders(Lists.list(folder_30(), folder_31(), folder_32(), folder_33(), folder_34()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_35() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c1a44240-983d-418b-b1dd-26d5472b6bbb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Explorer")));
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "b00fd2eb-68f8-4583-a38a-f25cd3d92756");
		bean.setDisplayName(localizedString_36());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ea3b6f65-a33d-4267-b634-187038f6fd00");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "33e32b16-8513-4105-9617-419570bbed11");
		bean.setDisplayName(localizedString_37());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e9f0f4f2-1d71-43a4-8447-f844ae79a34d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "8b2838a2-7a85-4963-aabc-061f28ffbfd6");
		bean.setDisplayName(localizedString_38());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_38() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6824aa62-fd34-4b3e-bbc9-317beca9e6af");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "bb7f4ee7-f70a-493f-a7eb-eda76521b8ea");
		bean.setDisplayName(localizedString_39());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_39() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e7cced78-9a50-4fc2-b930-034ccdc463a4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "444d1503-c227-443c-ad28-4297c609acb6");
		bean.setDisplayName(localizedString_40());
		bean.setName("$notificationsConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6648b18e-81ad-49f4-b595-0d8a114fde0d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notifications Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "99311c27-50c1-417d-ae8f-c8b929e1e9d5");
		bean.setDisplayName(localizedString_41());
		bean.setName("$selection");
		bean.setSubFolders(Lists.list(folder_36(), folder_37(), folder_38(), folder_39(), folder_40(), folder_41()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "31a4a1e8-90d2-4251-b257-6ba8c81e0e98");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Selection")));
		return bean;
	}

	// Managed
	private Folder folder_36() {
		Folder bean = session().createRaw(Folder.T, "83e410ff-c64e-48e3-a533-9f36329612e3");
		bean.setDisplayName(localizedString_42());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bd1e1fe9-af74-49f3-b921-4d64c7c65db6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_37() {
		Folder bean = session().createRaw(Folder.T, "ce5d2e63-0881-4941-9da5-f92436b94e99");
		bean.setDisplayName(localizedString_43());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6fa47778-7be3-46b5-a9d8-7504ddc65886");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_38() {
		Folder bean = session().createRaw(Folder.T, "208db51a-85d5-4ad0-9fc6-3c77f95415ae");
		bean.setDisplayName(localizedString_44());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0aaf903a-7df5-4636-b9ad-82ce09969f1c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_39() {
		Folder bean = session().createRaw(Folder.T, "4fdd7347-d6eb-4ef3-a946-2986405f0e89");
		bean.setDisplayName(localizedString_45());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_45() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7d34e54b-7993-4e1e-a0f1-ebb5384518df");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_40() {
		Folder bean = session().createRaw(Folder.T, "ff9a72cf-9f62-4419-b8ec-690b514085f7");
		bean.setDisplayName(localizedString_46());
		bean.setName("$quickAccessConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_46() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "79f052eb-f266-48c2-afa1-593d18a48c97");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_41() {
		Folder bean = session().createRaw(Folder.T, "9177d09f-811f-4067-b4ab-19db355f13ab");
		bean.setDisplayName(localizedString_47());
		bean.setName("$expertUI");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_47() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c452f93b-606d-43d6-9009-c500bcc64002");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Expert U I")));
		return bean;
	}

	// Managed
	private Folder folder_42() {
		Folder bean = session().createRaw(Folder.T, "4fc9080b-b508-4896-a1fa-88d704f8c57f");
		bean.setDisplayName(localizedString_58());
		bean.setName("global-actionbar");
		bean.setSubFolders(Lists.list(folder_43(), folder_44(), folder_45(), folder_46(), folder_47(), folder_48()));
		return bean;
	}

	// Managed
	private Folder folder_43() {
		Folder bean = session().createRaw(Folder.T, "be0b1b8d-8fbf-463d-affd-064a38105d57");
		bean.setDisplayName(localizedString_49());
		bean.setName("$new");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_49() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1121327d-1f28-45aa-8ff2-c9d941b5dee8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New")));
		return bean;
	}

	// Managed
	private Folder folder_44() {
		Folder bean = session().createRaw(Folder.T, "a37a6d76-0545-4777-b5a7-c81ca405d9ff");
		bean.setDisplayName(localizedString_50());
		bean.setName("$dualSectionButtons");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_50() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3c3dcf83-6e4f-4d75-aecb-618c1be24919");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Dual Section Buttons")));
		return bean;
	}

	// Managed
	private Folder folder_45() {
		Folder bean = session().createRaw(Folder.T, "ee976ad9-9f84-4a32-be37-49fce8055752");
		bean.setDisplayName(localizedString_51());
		bean.setName("$upload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_51() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7793a467-fed8-4b67-abc6-fb06c1dfb7ea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Upload")));
		return bean;
	}

	// Managed
	private Folder folder_46() {
		Folder bean = session().createRaw(Folder.T, "4954e499-2c9d-40f6-ae00-3efac2d12372");
		bean.setDisplayName(localizedString_52());
		bean.setName("$undo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_52() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1656047f-4c49-4a8e-a951-b13b181ebd02");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Undo")));
		return bean;
	}

	// Managed
	private Folder folder_47() {
		Folder bean = session().createRaw(Folder.T, "97a2f311-42a0-404f-bd00-4994ab9d39fd");
		bean.setDisplayName(localizedString_53());
		bean.setName("$redo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_53() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8fab2e08-d9fc-4333-9b98-c453675585fc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Redo")));
		return bean;
	}

	// Managed
	private Folder folder_48() {
		Folder bean = session().createRaw(Folder.T, "3dd0d3b6-3800-4458-9a0b-157f956451cf");
		bean.setDisplayName(localizedString_54());
		bean.setName("$commit");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_54() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "de3fe0d5-b5b6-4c2f-90de-247b5d3868b9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Commit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_55() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8a651b8c-6cd2-4fac-8ad0-f5ae88812d32");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_56() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4906eeee-b315-41de-8209-b4318f3e9669");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Header Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_57() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cfa7f91c-d32e-46f4-832d-56c102ccfd80");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tab Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_58() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "62be9477-74fd-456f-85a6-bb84815976c2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_59() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "686738ae-02f1-458b-8620-db7535a534f0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_60() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ce21f992-a3e6-4ed6-b2be-b6b66f74d822");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_61() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "329d965f-18e2-4729-acc2-24da943f3660");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_62() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2ef2bd2f-07b2-4fe7-99d8-aa01e9f81232");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_63() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "293f2fcc-9e8a-47c5-bb45-bd43f554b33c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_64() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a4310557-fb00-49ca-831c-00522a2e12d8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_65() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3e2fc2de-870a-4639-aa24-2b139e5a01a9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_66() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5ae70f36-1bb3-42c3-8c42-05af7f3ffbf0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_67() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "947afaa9-393e-4e00-90d3-642b8447077e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_68() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "25e3d71e-17ce-4417-bcdf-fa0a7f66b70d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_69() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1ec80f83-fe53-44bd-807c-43ec9d2f81bc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add to Clipboard")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_70() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9f26aa8d-caa3-47f8-a1f2-6101f0285d62");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_71() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "45fbf1bb-b23e-4fc8-9596-1f5fa7ee1d57");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_72() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "761fec4e-d6da-4e59-b06c-bdc15dc4cd38");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Execute")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "9136db70-ec8d-4d6d-b0e0-eaae5e0f126f");
		bean.setName("run-16.png Icon");
		bean.setRepresentations(Sets.set(resource_1(), resource_2()));
		return bean;
	}

	// Managed
	private Folder folder_49() {
		Folder bean = session().createRaw(Folder.T, "df585d3f-6fbf-4ef9-a19d-ea16236f7165");
		bean.setDisplayName(localizedString_73());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_50()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_73() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "16ca9662-ad71-4352-98f4-a2d220dc4f65");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private Folder folder_50() {
		Folder bean = session().createRaw(Folder.T, "af7c176c-5cc7-44af-8694-707ad3af2d47");
		bean.setDisplayName(localizedString_74());
		bean.setName("user-statistics");
		bean.setSubFolders(Lists.list(folder_51()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_74() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "677fa4ec-72bd-49b8-ab0a-51d9ebc58bd8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Statistics")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_75() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "30465aff-75f6-47d7-96c3-c77d4c1026e2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Users")));
		return bean;
	}

	// Managed
	private Folder folder_51() {
		Folder bean = session().createRaw(Folder.T, "e082fa5c-7a42-48ed-92d7-4d2767bda288");
		bean.setContent(templateQueryAction_1());
		bean.setDisplayName(localizedString_75());
		bean.setName("All Users");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private Template template_1() {
		Template bean = session().createRaw(Template.T, "ac03a730-6f3a-457c-98f1-59057f3c5ae4");
		bean.setMetaData(Sets.set(queryString_1()));
		bean.setPrototype(entityQuery_1());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_1() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "ee7968ff-01b6-434f-a6a3-62a63e9cf10e");
		bean.setEntityTypeSignature("com.braintribe.model.user.statistics.UserStatistics");
		bean.setOrdering(simpleOrdering_1());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_1() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "48a9439a-75b1-4783-a13e-fedcab078c09");
		bean.setDirection(OrderingDirection.ascending);
		bean.setOrderBy(propertyOperand_1());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_1() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "d587f371-ae6e-48e6-95fc-bfeef859ee62");
		bean.setPropertyName("lastLoginDate");
		return bean;
	}

	// Managed
	private QueryString queryString_1() {
		QueryString bean = session().createRaw(QueryString.T, "ea96553b-bc36-4293-9dee-a575fcc11662");
		bean.setValue("from UserStatistics s order by s.lastLoginDate");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_1() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "f0b4f68a-ef1d-4b19-811b-d0a64440f928");
		bean.setTemplate(template_1());
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