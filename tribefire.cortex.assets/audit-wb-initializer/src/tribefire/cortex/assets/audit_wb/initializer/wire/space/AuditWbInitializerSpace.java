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
package tribefire.cortex.assets.audit_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.audit_wb.initializer.wire.contract.AuditWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class AuditWbInitializerSpace extends AbstractInitializerSpace implements AuditWbInitializerContract {

	@Override
	public void initialize() {
		workbenchPerspective_1();
		folder_4();
		workbenchPerspective_2();
		folder_28();
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "de99583f-0186-470c-a43d-753d609c6ddb");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	@Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "01365514-75c5-431d-b205-84761bbe8510");
		bean.setContent(simpleQueryAction_1());
		bean.setDisplayName(localizedString_1());
		bean.setIcon(adaptiveIcon_1());
		bean.setName("ManipulationRecord");
		bean.setParent(folder_2());
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_1() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "a1e36049-4c3f-47df-a78c-56d671748ccf");
		bean.setTypeSignature("com.braintribe.model.audit.ManipulationRecord");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4a5fbd97-0fb0-4e07-bda8-fedb3a4e616f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Records")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "ad2189bf-f804-4cab-9389-7c140e15324c");
		bean.setName("Auditing Icon");
		bean.setRepresentations(Sets.set(resource_3(), resource_1(), resource_4(), resource_2()));
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "bea68457-b57d-40e3-a778-87766e05b72c");
		bean.setCreated(newGmtDate(2015, 11, 3, 14, 12, 35, 452));
		bean.setFileSize(20062l);
		bean.setMd5("2424260bd600ed489ccf7e08b6248889");
		bean.setMimeType("image/png");
		bean.setName("audit_24x24_02.png");
		bean.setResourceSource(moduleSource_1());
		bean.setSpecification(rasterImageSpecification_2());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "90cb7b16-e67e-49a6-8ec1-cdda5d7b1cef");
		bean.setPath("1512/0315/12/14e4eb18-785f-411c-a699-70ead0c48a42");
		bean.setModuleName(currentModuleName());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "5532d8c3-c62c-40ec-b24d-d688ded82650");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "8958be1e-4b98-43e1-9282-802dd73545e9");
		bean.setCreated(newGmtDate(2015, 11, 3, 14, 12, 35, 364));
		bean.setFileSize(20256l);
		bean.setMd5("e40850a8096b18cec500cc17657ad505");
		bean.setMimeType("image/png");
		bean.setName("audit_32x32_02.png");
		bean.setResourceSource(moduleSource_2());
		bean.setSpecification(rasterImageSpecification_3());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1f822d6c-5d81-4016-89d7-b074d7a7f92e");
		bean.setPath("1512/0315/12/67098f18-9966-466d-a9f1-b0f7bcb1dd4c");
		bean.setModuleName(currentModuleName());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_3() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8d5385fa-edea-4f84-8d2c-26493d86f5c3");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_3() {
		Resource bean = session().createRaw(Resource.T, "55f6aa70-a3eb-45be-a49f-b9a8d39b85d9");
		bean.setCreated(newGmtDate(2015, 11, 3, 14, 12, 35, 444));
		bean.setFileSize(19803l);
		bean.setMd5("1d2477e8eda3dcc37d0c64ebf039bd23");
		bean.setMimeType("image/png");
		bean.setName("audit_16x16_02.png");
		bean.setResourceSource(moduleSource_3());
		bean.setSpecification(rasterImageSpecification_4());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_3() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "26e3e4f7-23f4-4432-88a9-4a415eb410c8");
		bean.setPath("1512/0315/12/35404ed2-c3fd-424d-a86a-e8adb3a932c4");
		bean.setModuleName(currentModuleName());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_4() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "4cd9cf0b-cbab-4116-bdd5-6b9471805d8d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_4() {
		Resource bean = session().createRaw(Resource.T, "3ea7e979-0bab-4749-92d4-0aab6166acc5");
		bean.setCreated(newGmtDate(2015, 11, 3, 14, 12, 35, 397));
		bean.setFileSize(20987l);
		bean.setMd5("985181507c9b139ecc4a3dc6e7d51c70");
		bean.setMimeType("image/png");
		bean.setName("audit_64x64_02.png");
		bean.setResourceSource(moduleSource_4());
		bean.setSpecification(rasterImageSpecification_1());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_4() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "42ff5002-b486-4ebd-ae24-252972d9d2fa");
		bean.setPath("1512/0315/12/9c2b77cd-7f07-48bb-a2ae-29b20f8c702e");
		bean.setModuleName(currentModuleName());
		return bean;
	}

	@Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "28dc644e-0edc-4421-a33d-21910f268d9b");
		bean.setDisplayName(localizedString_2());
		bean.setName("Auditing");
		bean.setParent(folder_3());
		bean.setSubFolders(Lists.list(folder_1()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "016814ce-a42f-4954-b05b-ac73f0ccd81e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Auditing")));
		return bean;
	}

	@Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "6edef43e-fb2a-4075-9fa7-1c3811936e43");
		bean.setDisplayName(localizedString_3());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_2()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "45ecb181-b4c8-4247-9f66-e677b122d4f5");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "71ba9d3f-7bd0-4861-a9aa-e7984a4128c1");
		bean.setDisplayName(localizedString_4());
		bean.setFolders(Lists.list(folder_3()));
		bean.setName("root");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e4f191a1-7c96-4e9b-a295-aaf1cf086e59");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c96ea9f4-2015-4794-8cec-44ab1a0b7d43");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add To Clipboard")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "75fe93c7-dfe8-4b82-88b5-6d389d7bf1b6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	@Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "8e979ea6-f8f6-4faa-938b-1b605ae2bef0");
		bean.setDisplayName(localizedString_7());
		bean.setName("$detailsPanelVisibility");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_7() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "329699c9-5d62-40a5-b3f1-661057ea6d33");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	@Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "d707d0d3-1720-4253-8fd3-3e77818c4870");
		bean.setDisplayName(localizedString_8());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_6(), folder_7(), folder_8(), folder_9(), folder_10(), folder_11(), folder_12(), folder_13(), folder_14(),
				folder_15(), folder_16(), folder_17(), folder_18(), folder_19(), folder_20(), folder_21(), folder_4(), folder_22(), folder_23(),
				folder_24(), folder_25(), folder_26(), folder_27()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_8() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9fcf3ee7-a065-4df4-b751-610b5ace7d42");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Actionbar")));
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "af21e338-941c-482f-a5c9-a63584b0a23f");
		bean.setDisplayName(localizedString_9());
		bean.setName("$exchangeContentView");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_9() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "55f2f032-c28b-486a-866c-e5cd973425bc");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange View")));
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "9a230a7e-cb62-48ba-a47d-2ced2a0a05ca");
		bean.setDisplayName(localizedString_10());
		bean.setName("$workWithEntity");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_10() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "677ae442-e7d0-4eb7-b016-8b9b4410c669");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "670d1d5c-97c7-4aca-b36c-4b1e9d1d2982");
		bean.setDisplayName(localizedString_11());
		bean.setName("$gimaOpener");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_11() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d4a67956-2f26-4839-8b8b-0342162cdff1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "53adabb5-355b-4ae5-b47a-f77440ce8394");
		bean.setDisplayName(localizedString_12());
		bean.setName("$instantiateEntity");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_12() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4e62c762-dac2-44c5-afba-6818a5645978");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New")));
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "319104db-92f2-4b11-ad70-fde55bede131");
		bean.setDisplayName(localizedString_13());
		bean.setName("$deleteEntity");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_13() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9cb99483-b87b-412e-a62f-05ed84525b26");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete ")));
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "4330fa73-1d40-4991-b762-7df581503060");
		bean.setDisplayName(localizedString_14());
		bean.setName("$changeInstance");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_14() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4dde0b89-9c2f-4ca8-9b49-b841f96befb2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "c3f003fb-270d-42a2-8e2a-99543651ed0d");
		bean.setDisplayName(localizedString_15());
		bean.setName("$clearEntityToNull");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_15() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c8650c2b-afd4-46ce-96e7-bb4f01178746");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "ff89e26b-79b4-44be-b6bc-d1fa7958ca59");
		bean.setDisplayName(localizedString_16());
		bean.setName("$addToCollection");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_16() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "514e89b3-e4b5-45a9-901e-67b7b7ead9f0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "ff61ded8-cd31-4658-bdaf-f504d9520c32");
		bean.setDisplayName(localizedString_17());
		bean.setName("$insertBeforeToList");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_17() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7011e590-ba8c-491e-b0fd-d17c3c92ac70");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "a14f05eb-8511-4ef9-9cdc-854623b0fadd");
		bean.setDisplayName(localizedString_18());
		bean.setName("$removeFromCollection");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_18() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5be24fd7-7728-4fb4-adee-72843bd7b778");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "54868cf1-da92-46fd-952f-31d6cadbc64f");
		bean.setDisplayName(localizedString_19());
		bean.setName("$clearCollection");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_19() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0b3836f9-6fc7-4713-9aa7-005a36cc1a85");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "ea5b15cf-39c7-47ec-aa6f-6bce86e6986f");
		bean.setDisplayName(localizedString_20());
		bean.setName("$condenseEntity");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_20() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "47b95fb1-5f3f-475f-a0b4-6bd4ac973b16");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Condensation")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "223e326b-a14e-4780-ae61-bded5d886738");
		bean.setDisplayName(localizedString_21());
		bean.setName("$refreshEntities");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_21() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4ddafd24-5172-4d64-9273-84a8b8efe603");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "1c72fe11-b1f8-41b3-af81-e8104464ac71");
		bean.setDisplayName(localizedString_5());
		bean.setName("$addToClipboard");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "84ac1b03-e1ec-4380-868a-e5c28771e81c");
		bean.setDisplayName(localizedString_22());
		bean.setName("$displayMode");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_22() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "83661fbd-5470-4c6d-a48a-6089a1e70d1c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "List View")));
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "ba804cd3-11e4-4b3a-9c89-6b0a2f953217");
		bean.setDisplayName(localizedString_6());
		bean.setName("$openGmeForAccessInNewTab");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "7c0206e4-7f66-4c5b-b7fc-20f8fe62899e");
		bean.setDisplayName(localizedString_23());
		bean.setName("$ResourceDownload");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5ad6959a-dbba-4030-a696-54e0887fc076");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "678f86b8-3349-47ef-80c5-d6ef8a6d71b4");
		bean.setDisplayName(localizedString_24());
		bean.setName("$recordTemplateScript");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c3e7a12b-7d58-4b1f-8393-4492d936e601");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Record")));
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "c7046ceb-02c4-41b1-9c7c-fc7aa99ae5b1");
		bean.setDisplayName(localizedString_25());
		bean.setName("$editTemplateScript");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4c98cf2f-980a-4d0d-8c80-9c9b543239ef");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit Template")));
		return bean;
	}

	// Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "e1d889b5-aab9-4efb-ab8f-73d613dd0fab");
		bean.setDisplayName(localizedString_26());
		bean.setName("$addMetaDataEditorAction");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "38813641-7ba4-481b-8868-4ac9449ff3b9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add MetaData")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "a73a4313-3a99-40c2-9ab6-d6f6f5fa861d");
		bean.setDisplayName(localizedString_27());
		bean.setName("$openGmeForAccessWebTerminalInNewTab");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "aa88eb5e-b061-4e13-adb2-c0c7b39ea170");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "3706ca2c-df52-4566-a261-7040c3746225");
		bean.setDisplayName(localizedString_28());
		bean.setName("$copyTextToClipboard");
		bean.setParent(folder_5());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3c28c1a9-c158-4a94-8466-ae593a6e3243");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Copy Text")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "b7c27413-e4f7-4887-ad1b-fb9ce48d43cb");
		bean.setDisplayName(localizedString_29());
		bean.setFolders(Lists.list(folder_1()));
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "10e60c5d-95d0-46b7-a3a9-c0f3d2c22710");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Folders")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1b15781a-663a-45b8-8f14-0efc2752a9c4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Audit")));
		return bean;
	}

	// Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "b9336c55-8e7c-4419-8b01-36f3041a5c15");
		bean.setDisplayName(localizedString_30());
		bean.setName("Audit");
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