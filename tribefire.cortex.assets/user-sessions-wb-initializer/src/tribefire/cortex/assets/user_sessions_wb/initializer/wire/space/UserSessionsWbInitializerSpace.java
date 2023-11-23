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
package tribefire.cortex.assets.user_sessions_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.gm.model.user_session_service.MapToUserSession;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeStrategy;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.meta.data.prompt.VirtualEnum;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.template.meta.DynamicPropertyMetaDataAssignment;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.model.workbench.meta.QueryString;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.user_sessions_wb.initializer.wire.contract.UserSessionsWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class UserSessionsWbInitializerSpace extends AbstractInitializerSpace implements UserSessionsWbInitializerContract {

	@Override
	public void initialize() {
		folder_54();
		workbenchPerspective_1();
		workbenchPerspective_2();
		workbenchPerspective_3();
		workbenchPerspective_4();
		workbenchPerspective_5();
		workbenchPerspective_6();
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "33cccc63-66fe-49d8-b2d9-723e72c67424");
		bean.setDisplayName(localizedString_1());
		bean.setFolders(Lists.list(folder_49()));
		bean.setName("root");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ddc73177-62cf-48d8-871e-01b1fcf1bcfa");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "root")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "b5c40ec2-3641-40d0-9a4b-18f82b7a5606");
		bean.setDisplayName(localizedString_2());
		bean.setFolders(Lists.list(folder_53(), folder_52(), folder_55(), folder_51()));
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "04efaca5-7819-4e02-9c18-2a69532c1197");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "homeFolder")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_3() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "8a671fa7-0423-404a-af47-32a8812c5944");
		bean.setDisplayName(localizedString_3());
		bean.setFolders(Lists.list(folder_1()));
		bean.setName("actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ca82584c-6c8c-4b0b-b11c-d74c8f3886ff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_4() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "298fc6b5-3192-49a9-8158-9660dac2e746");
		bean.setDisplayName(localizedString_4());
		bean.setFolders(Lists.list(folder_16()));
		bean.setName("headerbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ce4b2a42-5193-449f-9b37-8dc57bff850f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "headerbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_5() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "5f39f815-111d-4f67-bb1f-e1b4b8f09619");
		bean.setDisplayName(localizedString_5());
		bean.setFolders(Lists.list(folder_42()));
		bean.setName("global-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0de31247-07e5-446a-bf6a-a5b8f96d91c8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "global-actionbar")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_6() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "c4d21cfd-f8b3-41a1-bd3f-7bd14ec632eb");
		bean.setDisplayName(localizedString_6());
		bean.setFolders(Lists.list(folder_28()));
		bean.setName("tab-actionbar");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4ac52105-b5a7-4974-918e-509dd072fcb3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tab-actionbar")));
		return bean;
	}

	@Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "5ae021a8-ad79-40b5-814c-a281b3267d88");
		bean.setDisplayName(localizedString_55());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_2(), folder_3(), folder_4(), folder_5(), folder_6(), folder_7(), folder_8(), folder_9(), folder_10(),
				folder_11(), folder_12(), folder_13(), folder_14(), folder_15(), folder_56()));
		return bean;
	}

	// Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "fe8e7831-b37a-4e0f-94f4-48516b01b957");
		bean.setDisplayName(localizedString_68());
		bean.setName("$exchangeContentView");
		return bean;
	}

	// Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "c1c80cee-4d57-4de5-9820-772205cf7b7f");
		bean.setDisplayName(localizedString_59());
		bean.setName("$workWithEntity");
		return bean;
	}

	// Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "dcaf0132-f261-456e-818f-48b54aa6657e");
		bean.setDisplayName(localizedString_60());
		bean.setName("$gimaOpener");
		return bean;
	}

	// Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "8b19937b-4476-473f-bb62-2a7dc11d56ae");
		bean.setDisplayName(localizedString_61());
		bean.setName("$deleteEntity");
		return bean;
	}

	// Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "53926b5b-c1d7-4ce9-9ffc-b9cef7c5b378");
		bean.setDisplayName(localizedString_62());
		bean.setName("$changeInstance");
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "b60e4c22-1154-42c3-9e93-56c1657c4387");
		bean.setDisplayName(localizedString_63());
		bean.setName("$clearEntityToNull");
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "8a457746-87c1-4a55-b6ef-2bf42895b3b0");
		bean.setDisplayName(localizedString_64());
		bean.setName("$addToCollection");
		return bean;
	}

	// Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "96241e76-97c3-4b4c-9fa8-3e6ca3bdf16b");
		bean.setDisplayName(localizedString_65());
		bean.setName("$insertBeforeToList");
		return bean;
	}

	// Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "377632e8-3e23-4b80-80bb-f8eb2366ab83");
		bean.setDisplayName(localizedString_66());
		bean.setName("$removeFromCollection");
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "46b105d5-a6b4-4279-95c2-151c49f97ac8");
		bean.setDisplayName(localizedString_67());
		bean.setName("$clearCollection");
		return bean;
	}

	// Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "7d180249-02eb-4946-9a84-4bfbc489c6ee");
		bean.setDisplayName(localizedString_70());
		bean.setName("$refreshEntities");
		return bean;
	}

	// Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "24374e94-c5ac-49ec-90b6-c44f78badcd9");
		bean.setDisplayName(localizedString_71());
		bean.setName("$ResourceDownload");
		return bean;
	}

	// Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "872b49c2-a8f6-4233-8075-ec4e7b385a92");
		bean.setDisplayName(localizedString_72());
		bean.setName("$executeServiceRequest");
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "b7ec3f94-f44e-40c9-b378-09b8283e0dc0");
		bean.setDisplayName(localizedString_69());
		bean.setName("$addToClipboard");
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "45d7b2ef-4fae-4dee-be28-fb1241f3dd93");
		bean.setDisplayName(localizedString_56());
		bean.setName("headerbar");
		bean.setSubFolders(Lists.list(folder_17(), folder_18(), folder_19(), folder_20(), folder_25()));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "369c1970-1f69-4d95-b0c3-80f25ae8f191");
		bean.setDisplayName(localizedString_23());
		bean.setName("tb_Logo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2f1f79ca-5def-4b28-b303-1f0b331617a4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tb_ Logo")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "34ef5f0b-4359-4e44-a436-4380f290c6ba");
		bean.setDisplayName(localizedString_24());
		bean.setName("$quickAccess-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "467456b5-d5c8-44e8-99b3-e9768b2eb237");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access-slot")));
		return bean;
	}

	// Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "6c517256-5e5d-449b-84c5-962048fd3a65");
		bean.setDisplayName(localizedString_25());
		bean.setName("$globalState-slot");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bd702e7b-dd7c-4869-8e3a-be57766703df");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global State-slot")));
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "e7a2eb1a-eae3-4430-adc8-dfa20bbd3273");
		bean.setDisplayName(localizedString_26());
		bean.setName("$settingsMenu");
		bean.setSubFolders(Lists.list(folder_21(), folder_22(), folder_23(), folder_24()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ae3b7ae5-a2f4-4b00-bbba-b7ba0d1a474c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Settings Menu")));
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "086a8102-8fb5-4689-8db3-2d287a42f8d6");
		bean.setDisplayName(localizedString_27());
		bean.setName("$reloadSession");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d8874d6a-8346-4c7a-a63a-a5a5772bf905");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Reload Session")));
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "aa9cdccb-55ec-4f0b-8ce2-ea2655a4802f");
		bean.setDisplayName(localizedString_28());
		bean.setName("$showSettings");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8f8fd7b7-b73d-48de-a69f-a539e37ff887");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Settings")));
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "945b179a-7a17-4e51-8a6e-3ec483b4d030");
		bean.setDisplayName(localizedString_29());
		bean.setName("$uiTheme");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c45e9dbc-8a51-4d18-8e2a-b7cd4ffcd361");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Ui Theme")));
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "1731283c-df69-4fe6-85a3-47424d495261");
		bean.setDisplayName(localizedString_30());
		bean.setName("$showAbout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "43c7fb18-33ff-4c1d-8868-714cd808d027");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show About")));
		return bean;
	}

	// Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "735ec9f5-3da3-44a3-96b4-8873d397adce");
		bean.setDisplayName(localizedString_31());
		bean.setName("$userMenu");
		bean.setSubFolders(Lists.list(folder_26(), folder_27()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "16258d53-057c-4483-abd3-3620506313aa");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Menu")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "7b8ffc46-0974-49bd-be87-e969fd46f6d3");
		bean.setDisplayName(localizedString_32());
		bean.setName("$showUserProfile");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e65f3538-963d-4326-960d-70df96cbe65e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show User Profile")));
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "58773d0a-f6f8-4313-8a33-cf6c473e896a");
		bean.setDisplayName(localizedString_33());
		bean.setName("$showLogout");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "84c08a4e-61da-4b57-b543-386e69fcb927");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show Logout")));
		return bean;
	}

	// Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "af27041e-01eb-4e80-9cef-80c215ab2b4c");
		bean.setDisplayName(localizedString_57());
		bean.setName("tab-actionbar");
		bean.setSubFolders(Lists.list(folder_29(), folder_35()));
		return bean;
	}

	// Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "12315bd4-bf26-48f4-8301-03855ad165bc");
		bean.setDisplayName(localizedString_35());
		bean.setName("$explorer");
		bean.setSubFolders(Lists.list(folder_30(), folder_31(), folder_32(), folder_33(), folder_34()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_35() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9cb025d2-a0f0-4ece-a28e-1072321cd477");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Explorer")));
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "b4ad06c0-f96a-41c7-b3f6-c3bb6003bb6d");
		bean.setDisplayName(localizedString_36());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fadadb67-8ca5-4f80-8aa6-14be11eba17d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "8a613cc9-04af-476f-a34d-34610b7c6d20");
		bean.setDisplayName(localizedString_37());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2bb8057d-b9c2-40b7-b67f-ea8f672b718d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "91daae7b-9d7f-4dda-bbce-6b55ea55986a");
		bean.setDisplayName(localizedString_38());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_38() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "88910f06-0650-4884-a04a-e9cf19cf9ac1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "a7b950dc-c29a-4e48-a2e8-9d8facb82fa4");
		bean.setDisplayName(localizedString_39());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_39() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "46c599d4-a02c-43a4-8679-68631555d701");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "1474e4ee-61f2-4f7f-a615-4bb02fee57c5");
		bean.setDisplayName(localizedString_40());
		bean.setName("$notificationsConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "28ed9bf8-4339-4b71-a6b3-6d4fea4d72b2");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Notifications Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "4923de08-2cad-4e85-b594-1d5f813e060a");
		bean.setDisplayName(localizedString_41());
		bean.setName("$selection");
		bean.setSubFolders(Lists.list(folder_36(), folder_37(), folder_38(), folder_39(), folder_40(), folder_41()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "08d044e3-c33c-4be1-aa6a-dfa59e58d3b3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Selection")));
		return bean;
	}

	// Managed
	private Folder folder_36() {
		Folder bean = session().createRaw(Folder.T, "73a41d41-3fac-4f28-9a56-cdd53c96f047");
		bean.setDisplayName(localizedString_42());
		bean.setName("$homeConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cd401da7-5a12-497e-a01d-5330ac9bcaea");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_37() {
		Folder bean = session().createRaw(Folder.T, "ef970000-6f45-4f4a-a010-3509b0cd7359");
		bean.setDisplayName(localizedString_43());
		bean.setName("$changesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cb1284b9-6905-4bd9-af0d-0af60b8967a8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_38() {
		Folder bean = session().createRaw(Folder.T, "37d63137-798d-442c-bc3d-63732bc2d861");
		bean.setDisplayName(localizedString_44());
		bean.setName("$transientChangesConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2d748e5d-988c-4695-9a23-7f732beed5d8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Transient Changes Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_39() {
		Folder bean = session().createRaw(Folder.T, "ad1db51e-dcf4-4255-a0e3-72dc48ca374e");
		bean.setDisplayName(localizedString_45());
		bean.setName("$clipboardConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_45() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "aff0a8f9-8183-4b6d-af08-c09d70fdf435");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clipboard Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_40() {
		Folder bean = session().createRaw(Folder.T, "b2297b20-8bd6-49a1-a1e0-608e1ca2ae2c");
		bean.setDisplayName(localizedString_46());
		bean.setName("$quickAccessConstellation");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_46() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d49f6760-3cf9-4ddf-8ce1-05030174781b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Quick Access Constellation")));
		return bean;
	}

	// Managed
	private Folder folder_41() {
		Folder bean = session().createRaw(Folder.T, "8da74b51-ec98-4784-805c-170595ca3ac9");
		bean.setDisplayName(localizedString_47());
		bean.setName("$expertUI");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_47() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "529f21f6-075d-406e-8df7-8b6d30a5000e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Expert U I")));
		return bean;
	}

	// Managed
	private Folder folder_42() {
		Folder bean = session().createRaw(Folder.T, "fec5feb5-cbfc-4e7c-bf55-5cc7dda57d08");
		bean.setDisplayName(localizedString_58());
		bean.setName("global-actionbar");
		bean.setSubFolders(Lists.list(folder_43(), folder_44(), folder_45(), folder_46(), folder_47(), folder_48()));
		return bean;
	}

	// Managed
	private Folder folder_43() {
		Folder bean = session().createRaw(Folder.T, "17fec39c-3bd3-4f9b-8161-8635b5bb8d4f");
		bean.setDisplayName(localizedString_49());
		bean.setName("$new");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_49() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ca5304f6-236e-4f62-8170-6624614b841b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New")));
		return bean;
	}

	// Managed
	private Folder folder_44() {
		Folder bean = session().createRaw(Folder.T, "a72dbeec-2f12-49a8-a17a-df0e2bc91235");
		bean.setDisplayName(localizedString_50());
		bean.setName("$dualSectionButtons");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_50() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "03562a60-bbf4-476f-8d19-60713a4f5bf9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Dual Section Buttons")));
		return bean;
	}

	// Managed
	private Folder folder_45() {
		Folder bean = session().createRaw(Folder.T, "10dda594-37f8-40d1-a15d-a333e2e895ab");
		bean.setDisplayName(localizedString_51());
		bean.setName("$upload");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_51() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4d42ef6f-9872-40ee-a491-fb4954af84e7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Upload")));
		return bean;
	}

	// Managed
	private Folder folder_46() {
		Folder bean = session().createRaw(Folder.T, "60ee3470-b164-457d-b416-88ccb990e1b9");
		bean.setDisplayName(localizedString_52());
		bean.setName("$undo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_52() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2654a416-a803-4d56-be84-497b79e715c7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Undo")));
		return bean;
	}

	// Managed
	private Folder folder_47() {
		Folder bean = session().createRaw(Folder.T, "fac56f64-89a8-41da-85ea-360fa4944b48");
		bean.setDisplayName(localizedString_53());
		bean.setName("$redo");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_53() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3d299f65-0fb0-417c-b454-bec847efd39b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Redo")));
		return bean;
	}

	// Managed
	private Folder folder_48() {
		Folder bean = session().createRaw(Folder.T, "0b904f05-0e8f-4d51-b852-9d44c9dc4142");
		bean.setDisplayName(localizedString_54());
		bean.setName("$commit");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_54() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "60df47b5-3d8e-43b9-b5f9-0423f06e22bb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Commit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_55() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e9b0fa21-75ef-41d3-aa2c-04670b7fe7e4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_56() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7c69ff21-7eae-4c04-8f5b-05e2340b42b1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Header Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_57() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f1b347c7-a6e2-4fc9-b826-cf69ae421d9c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Tab Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_58() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cf757643-aef5-4e76-a2c8-9c320671da48");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Global Action Bar")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_59() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c74149c7-c2df-47a4-b88f-6aa4f8ceb9fa");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_60() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "79fbf3ab-aed5-48f9-bd39-6aaa3575b50e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_61() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "be9cce3c-b56b-4c6f-a59c-4031efa02604");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Close")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_62() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a9443911-100d-4450-9b39-21b4a0d94348");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_63() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bc459ca3-ce85-4fc9-991f-c2ca52e1102c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_64() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "64bc74c7-db3f-4e08-b68e-94d8cffbd821");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_65() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d2d2f260-b499-416c-bf2e-6991226308c1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_66() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "85e7477f-c7f4-4edb-a7b6-a2882f0bf2c6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_67() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "41aefbad-49be-4fb1-ae18-8e7992dc55ff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_68() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "1ebdbe4c-51fe-4ce7-b84a-deeb23535953");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_69() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "47dc80e4-36c2-4ad0-aa6d-4df80bfd5328");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add to Clipboard")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_70() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0e89a019-7925-45f6-b487-bc993f56bc30");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_71() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7e8c2519-73d7-496d-9a77-e7d17d9d6bab");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_72() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9a80c8e4-7db6-4d89-8258-276dfa929a1c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Execute")));
		return bean;
	}

	// Managed
	private Folder folder_49() {
		Folder bean = session().createRaw(Folder.T, "bff120b8-115b-4b08-9917-858d48a1d78a");
		bean.setDisplayName(localizedString_73());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_50()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_73() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e959bb7d-d4cd-4dba-9131-1eae70efcbcb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	@Managed
	private Folder folder_50() {
		Folder bean = session().createRaw(Folder.T, "04d66c99-3f5b-4740-8e69-01d12422a45b");
		bean.setDisplayName(localizedString_74());
		bean.setName("user-sessions");
		bean.setSubFolders(Lists.list(folder_53()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_74() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "da29f391-874e-4e04-a47b-d1eef6f2c164");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Session Administration")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_76() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9dc2bb16-870b-4092-8e37-3d29001808ef");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Internal Sessions")));
		return bean;
	}

	@Managed
	private Folder folder_51() {
		Folder bean = session().createRaw(Folder.T, "00316d73-a7c5-4378-80c7-4ca815f03a4c");
		bean.setContent(templateQueryAction_2());
		bean.setDisplayName(localizedString_76());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("Internal Sessions");
		return bean;
	}

	// Managed
	private Template template_2() {
		Template bean = session().createRaw(Template.T, "a67db12b-2925-442b-80b0-2e7eaa19d44c");
		bean.setMetaData(Sets.set(queryString_2()));
		bean.setPrototype(entityQuery_7());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_2() {
		QueryString bean = session().createRaw(QueryString.T, "dda3e486-75af-4350-9119-b37f4726bd44");
		bean.setValue("from PersistenceUserSession s where s.sessionType = 'internal' order by creationDate desc");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_2() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "f0f47540-4a34-480c-9fca-9ed64f0c3671");
		bean.setTemplate(template_2());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_77() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b40dc24b-d403-4e02-89a5-6452cda9176e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Sessions")));
		return bean;
	}

	@Managed
	private Folder folder_52() {
		Folder bean = session().createRaw(Folder.T, "6a0ba70c-8a2e-4e89-84c1-876838589253");
		bean.setContent(templateQueryAction_3());
		bean.setDisplayName(localizedString_77());
		bean.setIcon(adaptiveIcon_1());
		bean.setName("User Sessions");
		return bean;
	}

	// Managed
	private Template template_3() {
		Template bean = session().createRaw(Template.T, "01ad79a7-57fe-4172-bb93-00d2efba91f6");
		bean.setMetaData(Sets.set(queryString_3()));
		bean.setPrototype(entityQuery_9());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private QueryString queryString_3() {
		QueryString bean = session().createRaw(QueryString.T, "d9b8d938-3b95-4358-80da-afcf6a752d46");
		bean.setValue("from PersistenceUserSession s where s.sessionType = 'normal' order by creationDate desc");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_3() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "394cb438-61ee-4c8d-ac2c-ed31a438cf47");
		bean.setTemplate(template_3());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_78() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "09f3bf9a-61c0-4725-866d-0be22a8aab23");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Sessions")));
		return bean;
	}

	@Managed
	private Folder folder_53() {
		Folder bean = session().createRaw(Folder.T, "059895e4-50e7-4e9d-b68a-80adc62d60fe");
		bean.setContent(templateQueryAction_4());
		bean.setDisplayName(localizedString_78());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("Sessions");
		bean.setParent(folder_50());
		bean.setSubFolders(Lists.list(folder_52(), folder_55(), folder_51()));
		return bean;
	}

	// Managed
	private Template template_4() {
		Template bean = session().createRaw(Template.T, "593612e3-7e96-497c-a329-db4ee0c74bcc");
		bean.setMetaData(Sets.set(dynamicPropertyMetaDataAssignment_1()));
		bean.setPrototype(entityQuery_5());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_4() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "0f538616-bb5c-45bc-8be4-670a9e8a0557");
		bean.setForceFormular(Boolean.TRUE);
		bean.setTemplate(template_4());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_5() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "358fc9d4-6e58-42a0-8cf4-b4bec8139032");
		bean.setEntityTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		bean.setOrdering(simpleOrdering_5());
		bean.setRestriction(restriction_4());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_5() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "39d2809a-87d3-47cc-a57a-132112f5535f");
		bean.setDirection(OrderingDirection.descending);
		bean.setOrderBy(propertyOperand_11());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_11() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "bbcb44be-7e73-4c79-bd19-1a91a63c2d69");
		bean.setPropertyName("creationDate");
		return bean;
	}

	// Managed
	private Restriction restriction_4() {
		Restriction bean = session().createRaw(Restriction.T, "01eafe72-e492-4581-9ddb-b0ff7d67c8fc");
		bean.setCondition(conjunction_1());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_6() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "5c13988c-b00b-4cc6-9964-d8d419c8eae8");
		bean.setLeftOperand(propertyOperand_12());
		bean.setOperator(Operator.ilike);
		bean.setRightOperand(variable_2());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_12() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "791d42a4-a72b-48fc-a991-bbadb531c81f");
		bean.setPropertyName("id");
		return bean;
	}

	// Managed
	private Variable variable_2() {
		Variable bean = session().createRaw(Variable.T, "4fbc5fbc-1549-4a92-94db-300d23ec7d01");
		bean.setDefaultValue("*");
		bean.setLocalizedName(localizedString_97());
		bean.setName("Session Id");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_80() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b79367da-9bf2-4541-a27a-82af3bdd2ebe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Sessions per Role")));
		return bean;
	}

	// Managed
	private Folder folder_54() {
		Folder bean = session().createRaw(Folder.T, "cb2ffdc8-3ee3-4434-b349-fc7425e054f8");
		bean.setContent(templateQueryAction_5());
		bean.setDisplayName(localizedString_80());
		bean.setName("Sessions per Role");
		bean.setParent(folder_50());
		return bean;
	}

	// Managed
	private Template template_5() {
		Template bean = session().createRaw(Template.T, "33cf7840-91db-4650-abc6-211f4486de17");
		bean.setMetaData(Sets.set(queryString_5()));
		bean.setPrototype(entityQuery_6());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_6() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "f319fcb4-743e-4167-ad7f-6545d0d594de");
		bean.setEntityTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		bean.setOrdering(simpleOrdering_6());
		bean.setRestriction(restriction_5());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_6() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "0a6af709-8d7f-4754-89f6-382ea291253e");
		bean.setDirection(OrderingDirection.descending);
		bean.setOrderBy(propertyOperand_13());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_13() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "c0ae520a-fe5b-4ebf-b7e3-cbd4e944e205");
		bean.setPropertyName("creationDate");
		return bean;
	}

	// Managed
	private Restriction restriction_5() {
		Restriction bean = session().createRaw(Restriction.T, "69a55c3c-22db-43e1-a7ca-4ae264b87881");
		bean.setCondition(valueComparison_7());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_7() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "7c8cccb8-9bd9-498e-8ea0-d257c2b632db");
		bean.setLeftOperand(variable_3());
		bean.setOperator(Operator.in);
		bean.setRightOperand(propertyOperand_14());
		return bean;
	}

	// Managed
	private Variable variable_3() {
		Variable bean = session().createRaw(Variable.T, "4e8f572a-ba61-41e7-ae1b-a0697e7b455e");
		bean.setLocalizedName(localizedString_98());
		bean.setName("Role");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_14() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "e7138489-76b8-4c85-b2a0-02af78204b0d");
		bean.setPropertyName("effectiveRoles");
		return bean;
	}

	// Managed
	private QueryString queryString_5() {
		QueryString bean = session().createRaw(QueryString.T, "1b790e70-266f-4864-96b4-12d824103fa8");
		bean.setValue("from PersistenceUserSession s where :role in s.effectiveRoles order by creationDate desc");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_5() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "26bc68ed-e4d1-4073-912d-76d654fae4fd");
		bean.setTemplate(template_5());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_7() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "cc9ae05e-7d90-4082-8e71-2fae5306f777");
		bean.setEntityTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		bean.setOrdering(simpleOrdering_7());
		bean.setRestriction(restriction_6());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_7() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "b8c3712d-3d88-496c-9f31-959b13502618");
		bean.setDirection(OrderingDirection.descending);
		bean.setOrderBy(propertyOperand_15());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_15() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "d4139ee7-0369-4fd4-9dff-e14c1d3f5fff");
		bean.setPropertyName("creationDate");
		return bean;
	}

	// Managed
	private Restriction restriction_6() {
		Restriction bean = session().createRaw(Restriction.T, "c2d8011d-2e6e-48ab-9701-ff57447a193a");
		bean.setCondition(valueComparison_8());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_8() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "058b0676-c8ec-4364-ab63-b6d8fd0706ae");
		bean.setLeftOperand(propertyOperand_16());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("internal");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_16() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "4c07ed21-e171-49ed-951f-9d63340677fd");
		bean.setPropertyName("sessionType");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_81() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "53153f11-29aa-4436-9630-4b82d3c552f3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trusted Sessions")));
		return bean;
	}

	@Managed
	private Folder folder_55() {
		Folder bean = session().createRaw(Folder.T, "eb51a076-95e9-48e6-9697-12dbc6bfa05f");
		bean.setContent(templateQueryAction_6());
		bean.setDisplayName(localizedString_81());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Trusted Sessions");
		return bean;
	}

	// Managed
	private Template template_6() {
		Template bean = session().createRaw(Template.T, "6c4807c7-00af-4e31-b143-5ff21e4ba9b8");
		bean.setMetaData(Sets.set(queryString_6()));
		bean.setPrototype(entityQuery_8());
		bean.setPrototypeTypeSignature("com.braintribe.model.query.EntityQuery");
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_8() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "c4f282b0-f223-47cd-8ed7-0d529d68094c");
		bean.setEntityTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		bean.setOrdering(simpleOrdering_8());
		bean.setRestriction(restriction_7());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_8() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "39265bf5-6965-4fc8-a9dc-5550268d913c");
		bean.setDirection(OrderingDirection.descending);
		bean.setOrderBy(propertyOperand_17());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_17() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "77770f5f-2792-453c-bfc0-35b559c5a5e6");
		bean.setPropertyName("creationDate");
		return bean;
	}

	// Managed
	private Restriction restriction_7() {
		Restriction bean = session().createRaw(Restriction.T, "e56121dc-19de-4349-8c48-8c0f54b48e64");
		bean.setCondition(valueComparison_9());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_9() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "603bb76c-116b-4e5b-a816-13336766016c");
		bean.setLeftOperand(propertyOperand_18());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("trusted");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_18() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "b909ae3d-0497-4be3-a4d4-8212f637f216");
		bean.setPropertyName("sessionType");
		return bean;
	}

	// Managed
	private QueryString queryString_6() {
		QueryString bean = session().createRaw(QueryString.T, "e1327e3b-ce69-4c97-872e-a086231aa855");
		bean.setValue("from PersistenceUserSession s where s.sessionType = 'trusted' order by creationDate desc");
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_6() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "40536062-73ee-4aab-9b38-56515e9fe472");
		bean.setTemplate(template_6());
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_9() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "50596e42-7366-43fc-98d5-bb2dbeeb2b7e");
		bean.setEntityTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		bean.setOrdering(simpleOrdering_9());
		bean.setRestriction(restriction_8());
		return bean;
	}

	// Managed
	private SimpleOrdering simpleOrdering_9() {
		SimpleOrdering bean = session().createRaw(SimpleOrdering.T, "cf6e29f1-8eb3-4cb8-9041-3e3fd99d30b4");
		bean.setDirection(OrderingDirection.descending);
		bean.setOrderBy(propertyOperand_19());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_19() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9bb96a3b-f87a-4494-bc30-ecf0a2b7600d");
		bean.setPropertyName("creationDate");
		return bean;
	}

	// Managed
	private Restriction restriction_8() {
		Restriction bean = session().createRaw(Restriction.T, "fdd255ff-d377-4dff-bfb5-9fa9dc0aec0a");
		bean.setCondition(valueComparison_10());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_10() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "e0073d10-1a29-4cdc-a8f5-6580f25e31d1");
		bean.setLeftOperand(propertyOperand_20());
		bean.setOperator(Operator.equal);
		bean.setRightOperand("normal");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_20() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "0ea1a6e7-a721-46bf-b9dd-9c2104562e7d");
		bean.setPropertyName("sessionType");
		return bean;
	}

	// Managed
	private Folder folder_56() {
		Folder bean = session().createRaw(Folder.T, "6d775327-78f5-47bd-b7f2-50e7ec56e7c7");
		bean.setContent(templateServiceRequestAction_1());
		bean.setDisplayName(localizedString_82());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("Show As UserSession");
		bean.setParent(folder_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_82() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3606fefe-40ca-4d6d-b6f9-a51be0951e21");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show As UserSession")));
		return bean;
	}

	// Managed
	private TemplateServiceRequestAction templateServiceRequestAction_1() {
		TemplateServiceRequestAction bean = session().createRaw(TemplateServiceRequestAction.T, "0e876b81-d5a3-4d70-a2af-fd683745f5de");
		bean.setDisplayName(localizedString_83());
		bean.setInplaceContextCriterion(entityCriterion_1());
		bean.setMultiSelectionSupport(Boolean.TRUE);
		bean.setTemplate(template_7());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_83() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6c281b08-f84f-4588-a4bc-3546f4039537");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show as UserSession")));
		return bean;
	}

	// Managed
	private EntityCriterion entityCriterion_1() {
		EntityCriterion bean = session().createRaw(EntityCriterion.T, "1f504301-641f-4ee8-9d78-6ad93ca5711c");
		bean.setStrategy(EntityTypeStrategy.assignable);
		bean.setTypeSignature("com.braintribe.gm.model.usersession.PersistenceUserSession");
		return bean;
	}

	// Managed
	private Template template_7() {
		Template bean = session().createRaw(Template.T, "545ae02f-1e92-4421-a255-d99044d3930d");
		bean.setDescription(localizedString_84());
		bean.setName(localizedString_85());
		bean.setPrototype(mapToUserSession_1());
		bean.setPrototypeTypeSignature("com.braintribe.gm.model.user_session_service.MapToUserSession");
		bean.setScript(compoundManipulation_1());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_84() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d0386464-6db8-4abc-95f3-d5549d2c4414");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Shows a PersistenceUserSession as a logical UserSession")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_85() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "53bb1387-9200-4ada-a9f6-727686e6adc9");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show as UserSession Template")));
		return bean;
	}

	// Managed
	private CompoundManipulation compoundManipulation_1() {
		CompoundManipulation bean = session().createRaw(CompoundManipulation.T, "b25989dc-4a0d-43d6-a073-9208b5834e2c");
		bean.setCompoundManipulationList(Lists.list(changeValueManipulation_1()));
		return bean;
	}

	// Managed
	private ChangeValueManipulation changeValueManipulation_1() {
		ChangeValueManipulation bean = session().createRaw(ChangeValueManipulation.T, "5fea1fe2-f13e-40b0-bdda-3933e95ca9db");
		bean.setNewValue(variable_4());
		bean.setOwner(localEntityProperty_1());
		return bean;
	}

	// Managed
	private LocalEntityProperty localEntityProperty_1() {
		LocalEntityProperty bean = session().createRaw(LocalEntityProperty.T, "d0471a41-0fc5-4e9c-8f8c-e036a6c760d9");
		bean.setEntity(mapToUserSession_1());
		bean.setPropertyName("persistenceUserSessions");
		return bean;
	}

	// Managed
	private Variable variable_4() {
		Variable bean = session().createRaw(Variable.T, "b0b4c4cd-f92b-43ee-8360-43e648e9dbcd");
		bean.setName("PersistenceUserSession");
		bean.setTypeSignature("list<com.braintribe.gm.model.usersession.PersistenceUserSession>");
		return bean;
	}

	@Managed
	private MapToUserSession mapToUserSession_1() {
		MapToUserSession bean = session().createRaw(MapToUserSession.T, "ba4bcc37-ac94-4843-92b9-53771f9682c2");
		return bean;
	}

	// Managed
	private Resource resource_5() {
		Resource bean = session().createRaw(Resource.T, "944cf275-d68f-4435-acd3-c69a00488a90");
		bean.setCreated(newGmtDate(2020, 4, 12, 12, 30, 10, 966));
		bean.setCreator("cortex");
		bean.setFileSize(479l);
		bean.setMd5("f05e365231389e618e9c37088e10f8f8");
		bean.setMimeType("image/png");
		bean.setName("user-16.png");
		bean.setResourceSource(moduleSource_11());
		bean.setSpecification(rasterImageSpecification_15());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_11() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "2274aa76-7dde-40df-a744-482c9c7d3cf9");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1214/3010/647b68ef-1431-4683-9ba4-2a86baf1a232");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_15() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3432a03e-b616-450a-a593-769319e219d1");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_7() {
		Resource bean = session().createRaw(Resource.T, "ca38dbb7-904d-49d7-b7a3-88097b2cabb2");
		bean.setCreated(newGmtDate(2020, 4, 12, 12, 32, 58, 961));
		bean.setCreator("cortex");
		bean.setFileSize(529l);
		bean.setMd5("496566b4e1355de8ab73eaf2ade21715");
		bean.setMimeType("image/png");
		bean.setName("info-16.png");
		bean.setResourceSource(moduleSource_13());
		bean.setSpecification(rasterImageSpecification_19());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_13() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c45b818e-eb51-41a2-b9a0-cb526ff58041");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1214/3258/c6f10fb0-36ca-4437-a920-c1c223f85a27");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_19() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "20f37fd7-7138-4cc3-8fe6-a50d6a07b059");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_8() {
		Resource bean = session().createRaw(Resource.T, "64dac573-72f8-4690-9a41-f8532c31ec40");
		bean.setCreated(newGmtDate(2020, 4, 12, 12, 33, 49, 32));
		bean.setCreator("cortex");
		bean.setFileSize(924l);
		bean.setMd5("22d5f56b06c49ce8c1c6ddb381f37e22");
		bean.setMimeType("image/png");
		bean.setName("friends-16.png");
		bean.setResourceSource(moduleSource_14());
		bean.setSpecification(rasterImageSpecification_21());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_14() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7262dc26-da66-478a-b24c-c126644fa757");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1214/3349/4d65daea-f224-400b-8f68-a6beed6f2b7d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_21() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1177f5a7-0154-4ce2-b0b5-38850d270745");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "440c935d-a81f-4cdb-89d8-f6fe483d4ee9");
		bean.setName("user-icon");
		bean.setRepresentations(Sets.set(resource_5(), resource_15(), resource_16()));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_2() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "f94a9277-c227-4842-8a12-85de293f1e51");
		bean.setName("info-icon");
		bean.setRepresentations(Sets.set(resource_7(), resource_17(), resource_18()));
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_3() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "25bccfaf-6a54-4b81-b61a-02661868d89d");
		bean.setName("friends-icon");
		bean.setRepresentations(Sets.set(resource_8(), resource_13(), resource_14()));
		return bean;
	}

	// Managed
	private Resource resource_9() {
		Resource bean = session().createRaw(Resource.T, "ac305256-6c49-4ed5-920d-a22b209d1b33");
		bean.setCreated(newGmtDate(2020, 4, 12, 12, 56, 44, 917));
		bean.setCreator("cortex");
		bean.setFileSize(851l);
		bean.setMd5("06ccc2f7add735f749f740d3895bb75c");
		bean.setMimeType("image/png");
		bean.setName("time-16.png");
		bean.setResourceSource(moduleSource_15());
		bean.setSpecification(rasterImageSpecification_23());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_15() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "b22566ee-aec3-4c52-bf31-e4d466820244");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1214/5644/a57fcf67-7ca1-43be-b669-57f00d6fbdc7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_23() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "09394db8-f864-40cd-ad18-e486a7dd6e65");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private AdaptiveIcon adaptiveIcon_4() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "fa4476f1-1568-47d3-b3c3-20f3998c5b48");
		bean.setName("time-icon");
		bean.setRepresentations(Sets.set(resource_9(), resource_12(), resource_19()));
		return bean;
	}

	// Managed
	private Resource resource_12() {
		Resource bean = session().createRaw(Resource.T, "92e62879-9439-47fa-83db-171a4a8d032f");
		bean.setCreated(newGmtDate(2020, 4, 12, 14, 54, 41, 233));
		bean.setCreator("cortex");
		bean.setFileSize(1414l);
		bean.setMd5("2f07ea01eaba95ece9e93b559cabb4c3");
		bean.setMimeType("image/png");
		bean.setName("time-32.png");
		bean.setResourceSource(moduleSource_18());
		bean.setSpecification(rasterImageSpecification_29());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_18() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "57a5617e-46a6-43ca-a375-3804c51e63fc");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1216/5441/b73067db-55e7-4c9d-8bb8-d41db46ec979");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_29() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "48bd97d9-4e6c-481e-97fc-41fe444458ac");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_13() {
		Resource bean = session().createRaw(Resource.T, "dd645d33-601e-46fa-b774-4f5bc9c8ed98");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 9, 327));
		bean.setCreator("cortex");
		bean.setFileSize(1606l);
		bean.setMd5("71f0d726346957244f863642d647b581");
		bean.setMimeType("image/png");
		bean.setName("friends-32.png");
		bean.setResourceSource(moduleSource_19());
		bean.setSpecification(rasterImageSpecification_31());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_19() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "01ba79c7-371e-4c3d-b564-c84ed5698056");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1909/f958ed5e-5613-4146-9847-a865dcfc4470");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_31() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d725355c-f776-4460-bdd1-aecba07ccc94");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_14() {
		Resource bean = session().createRaw(Resource.T, "e15e417b-21be-4efb-84e6-dbcce680b4e4");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 9, 339));
		bean.setCreator("cortex");
		bean.setFileSize(3147l);
		bean.setMd5("587ec5864438a1cff63e5adbe9a093ae");
		bean.setMimeType("image/png");
		bean.setName("friends-64.png");
		bean.setResourceSource(moduleSource_20());
		bean.setSpecification(rasterImageSpecification_33());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_20() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c8666557-53b5-48dd-8518-eb176402dee1");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1909/5972fda5-916d-4b2b-9ed1-317f9dcac0da");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_33() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ca938381-ce80-4fe3-9e4d-30a97eaee2c9");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_15() {
		Resource bean = session().createRaw(Resource.T, "051f8c86-18d4-47f2-b903-9f37a06052e5");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 20, 753));
		bean.setCreator("cortex");
		bean.setFileSize(936l);
		bean.setMd5("e2f7aee8dff74fa7a341582f1b91661b");
		bean.setMimeType("image/png");
		bean.setName("user-32.png");
		bean.setResourceSource(moduleSource_21());
		bean.setSpecification(rasterImageSpecification_35());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_21() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "80b3e396-c2f6-497e-9866-f255396623fb");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1920/c601e268-713f-44d3-a774-32595d9d3ae7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_35() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f72528e3-7e08-4530-89f3-51f69a74234e");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_16() {
		Resource bean = session().createRaw(Resource.T, "bc885f14-eda9-42b7-8cde-f2e00d9cdb38");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 20, 763));
		bean.setCreator("cortex");
		bean.setFileSize(2359l);
		bean.setMd5("ec5735fbb63cca0829f1c3484f8c9106");
		bean.setMimeType("image/png");
		bean.setName("user-64.png");
		bean.setResourceSource(moduleSource_22());
		bean.setSpecification(rasterImageSpecification_37());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_22() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "592ffe5a-5b9e-4298-8aa3-9ddfa6ea6793");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1920/eb1d2116-f561-491e-b9be-06831b629d95");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_37() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "07210587-842d-4e6a-831f-85ddb1072344");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_17() {
		Resource bean = session().createRaw(Resource.T, "25182926-7610-4302-93eb-52b84477e7fb");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 31, 165));
		bean.setCreator("cortex");
		bean.setFileSize(1033l);
		bean.setMd5("f75a27ea77456ac2e844d820d937d171");
		bean.setMimeType("image/png");
		bean.setName("info-32.png");
		bean.setResourceSource(moduleSource_23());
		bean.setSpecification(rasterImageSpecification_39());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_23() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "1fb5c1b7-f379-4d7a-904f-387a0443cce4");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1931/93875b21-40a5-493e-901b-8c8b18c4d9cc");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_39() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3cf748f3-d188-4262-923a-932025be765c");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_18() {
		Resource bean = session().createRaw(Resource.T, "9eb3abe0-18f5-4ae2-8096-da544ac1e402");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 31, 177));
		bean.setCreator("cortex");
		bean.setFileSize(2054l);
		bean.setMd5("9c1926459a61b4982452c694ed4d1436");
		bean.setMimeType("image/png");
		bean.setName("info-64.png");
		bean.setResourceSource(moduleSource_24());
		bean.setSpecification(rasterImageSpecification_41());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_24() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "18203370-a9a4-4f5b-8fad-6ba20e643c09");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1931/cbce0fd3-09ed-45b2-8e69-9bea7a27e6d1");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_41() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "ad30c3a3-8aa5-4516-9545-4a10f50a18d0");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_19() {
		Resource bean = session().createRaw(Resource.T, "0e87a26e-af92-4149-8cf9-1cf93ea544f8");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 19, 37, 748));
		bean.setCreator("cortex");
		bean.setFileSize(2607l);
		bean.setMd5("5562fde2cf615ebd485613a3d2dce8bb");
		bean.setMimeType("image/png");
		bean.setName("time-64.png");
		bean.setResourceSource(moduleSource_25());
		bean.setSpecification(rasterImageSpecification_43());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_25() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "fd88429e-fa9d-4a8e-9f96-8bf516004d95");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/1937/8605fb3a-ea3d-4738-abda-e60bccadcd1b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_43() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e8eb706a-eff5-48fd-805c-eb153854cdac");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_20() {
		Resource bean = session().createRaw(Resource.T, "26cce26a-47ea-4299-b39c-fa6610fcffa7");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 35, 28, 412));
		bean.setCreator("cortex");
		bean.setFileSize(896l);
		bean.setMd5("9c85fd839871e3d91e7e4efd3d63433e");
		bean.setMimeType("image/png");
		bean.setName("user-circle-16.png");
		bean.setResourceSource(moduleSource_26());
		bean.setSpecification(rasterImageSpecification_45());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_26() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c9fe484e-ceee-417e-81b1-2ceca72f398b");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/3528/889a5f09-eea2-4255-9702-927c3d0b261d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_45() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "79e28f74-80bc-4aed-9a6d-6e9c2cf0c949");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_21() {
		Resource bean = session().createRaw(Resource.T, "36836a7b-77a9-477f-94d5-c3a998b8d4a4");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 35, 28, 425));
		bean.setCreator("cortex");
		bean.setFileSize(1654l);
		bean.setMd5("e238e2d514ee1420d64d86d85f28a988");
		bean.setMimeType("image/png");
		bean.setName("user-circle-32.png");
		bean.setResourceSource(moduleSource_27());
		bean.setSpecification(rasterImageSpecification_47());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_27() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4289e621-d932-407a-86ba-ab8fe190eb40");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/3528/8a481985-049f-495d-8b74-7df5fa037afa");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_47() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "8cfa2117-4721-477d-aead-b0e6894d7570");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_22() {
		Resource bean = session().createRaw(Resource.T, "1de8cff2-4596-494a-82f6-3f6fbbd33aa7");
		bean.setCreated(newGmtDate(2020, 4, 12, 15, 35, 28, 433));
		bean.setCreator("cortex");
		bean.setFileSize(3425l);
		bean.setMd5("c7ae37552a6e1daf2090c766a2401281");
		bean.setMimeType("image/png");
		bean.setName("user-circle-64.png");
		bean.setResourceSource(moduleSource_28());
		bean.setSpecification(rasterImageSpecification_49());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_28() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "2c8b7ac0-baf5-488a-81f2-6601bb855269");
		bean.setModuleName(currentModuleName());
		bean.setPath("2005/1217/3528/16489de8-93e8-49a3-a234-a869f36420d3");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_49() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "84b6ffcc-89df-461a-9d49-6ca56f04aa2a");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_5() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "914276a3-7dba-408e-aec5-a964ed22f107");
		bean.setName("user-circle-icon");
		bean.setRepresentations(Sets.set(resource_21(), resource_22(), resource_20()));
		return bean;
	}

	// Managed
	private Conjunction conjunction_1() {
		Conjunction bean = session().createRaw(Conjunction.T, "4d4f8837-e8af-463b-aad2-5859bbfe62f5");
		bean.setOperands(Lists.list(valueComparison_6(), valueComparison_12(), valueComparison_11()));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_11() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "b14a980e-f261-49bb-ba99-2d855556b736");
		bean.setLeftOperand(propertyOperand_21());
		bean.setOperator(Operator.ilike);
		bean.setRightOperand(variable_5());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_21() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "9ac8eeed-b3dc-45ce-8cb6-c80c3c5d26f3");
		bean.setPropertyName("userName");
		return bean;
	}

	// Managed
	private Variable variable_5() {
		Variable bean = session().createRaw(Variable.T, "6a66ed8a-7ff6-431e-9295-d0c6cea14360");
		bean.setDefaultValue("*");
		bean.setLocalizedName(localizedString_86());
		bean.setName("Username");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_86() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a9f8e6df-0dc0-4575-ae8e-b751eac88c36");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User")));
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_12() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "4cacd689-43c7-419e-be92-eaa1eef5397d");
		bean.setLeftOperand(propertyOperand_22());
		bean.setOperator(Operator.ilike);
		bean.setRightOperand(variable_6());
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_22() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "77346bae-69ed-4a46-8a5d-1c730a647499");
		bean.setPropertyName("sessionType");
		return bean;
	}

	@Managed
	private Variable variable_6() {
		Variable bean = session().createRaw(Variable.T, "f6455e91-993a-48b8-8764-5778d2520e61");
		bean.setDefaultValue("*");
		bean.setLocalizedName(localizedString_88());
		bean.setName("Session Type");
		bean.setTypeSignature("string");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_88() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "7b093a31-7288-4069-8bce-d4fa8655d598");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Type")));
		return bean;
	}

	// Managed
	private VirtualEnumConstant virtualEnumConstant_1() {
		VirtualEnumConstant bean = session().createRaw(VirtualEnumConstant.T, "a96ab8d5-b7ba-4c67-ba74-1195c1b23e5a");
		bean.setDisplayValue(localizedString_89());
		bean.setIcon(adaptiveIcon_4());
		bean.setValue("*");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_89() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "744a8da2-6ae7-43f4-a5be-90f7605ea8bd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show All")));
		return bean;
	}

	// Managed
	private VirtualEnumConstant virtualEnumConstant_2() {
		VirtualEnumConstant bean = session().createRaw(VirtualEnumConstant.T, "a87b003b-941d-4190-bf35-582646a3cc52");
		bean.setDisplayValue(localizedString_90());
		bean.setIcon(adaptiveIcon_1());
		bean.setValue("normal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_90() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9746adc9-dbf1-468e-9355-ad947fc4fb52");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Normal")));
		return bean;
	}

	// Managed
	private VirtualEnumConstant virtualEnumConstant_3() {
		VirtualEnumConstant bean = session().createRaw(VirtualEnumConstant.T, "bb1331fd-f2df-49e0-966c-9fac7002c9bd");
		bean.setDisplayValue(localizedString_91());
		bean.setIcon(adaptiveIcon_3());
		bean.setValue("trusted");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_91() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "964f5f68-b03f-4331-8e49-a5b379e84f07");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Trusted")));
		return bean;
	}

	// Managed
	private VirtualEnumConstant virtualEnumConstant_4() {
		VirtualEnumConstant bean = session().createRaw(VirtualEnumConstant.T, "fb0a1abd-30db-4d39-8e06-dd93becd07a6");
		bean.setDisplayValue(localizedString_92());
		bean.setIcon(adaptiveIcon_2());
		bean.setValue("internal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_92() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ee911fc9-cab3-4185-b1fd-933fb2cc4116");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Internal")));
		return bean;
	}

	// Managed
	private VirtualEnum virtualEnum_1() {
		VirtualEnum bean = session().createRaw(VirtualEnum.T, "8dae73d5-08a0-463f-aa3d-e975c6c7800d");
		bean.setConstants(Lists.list(virtualEnumConstant_1(), virtualEnumConstant_2(), virtualEnumConstant_3(), virtualEnumConstant_4()));
		bean.setForceSelection(Boolean.TRUE);
		bean.setInherited(Boolean.TRUE);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_97() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f9587f2b-549b-4dc0-bafb-b488cabc2c01");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Session Id")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_98() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2bd64c86-120e-4f21-8a38-a8ca44b825d1");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Role")));
		return bean;
	}

	// Managed
	private DynamicPropertyMetaDataAssignment dynamicPropertyMetaDataAssignment_1() {
		DynamicPropertyMetaDataAssignment bean = session().createRaw(DynamicPropertyMetaDataAssignment.T, "24fef958-43e2-491b-9ba6-5d6a0116caa8");
		bean.setMetaData(Sets.set(virtualEnum_1()));
		bean.setVariable(variable_6());
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