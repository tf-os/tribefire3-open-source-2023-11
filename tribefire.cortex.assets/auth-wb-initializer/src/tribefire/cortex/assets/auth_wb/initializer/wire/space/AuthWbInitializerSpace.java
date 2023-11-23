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
package tribefire.cortex.assets.auth_wb.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.template.Template;
import com.braintribe.model.workbench.SimpleQueryAction;
import com.braintribe.model.workbench.TemplateQueryAction;
import com.braintribe.model.workbench.WorkbenchPerspective;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.auth_wb.initializer.wire.contract.AuthWbInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class AuthWbInitializerSpace extends AbstractInitializerSpace implements AuthWbInitializerContract {

	@Override
	public void initialize() {
		folder_13();
		folder_27();
		folder_28();
		folder_29();
		folder_30();
		folder_31();
		folder_32();
		folder_33();
		folder_34();
		folder_35();

		workbenchPerspective_1();
		workbenchPerspective_2();
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f5799cec-654d-4f99-8468-d73d5afe0c43");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images Query")));
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "6cedf4b5-1504-413a-8d0d-da16b7cfd41e");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c59d7ab0-80ca-4653-85da-862fcc6a49e7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "All Identities")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f61fccf0-5de9-435d-ad3a-83b466afade6");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "View")));
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "ef2c29cd-2d7e-47a2-a305-61cb11401a44");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 5, 916));
		bean.setFileSize(884l);
		bean.setMd5("777db1f4e13861506d172bfe0841ffbc");
		bean.setMimeType("image/png");
		bean.setName("us_group_24x24.png");
		bean.setResourceSource(moduleSource_1());
		bean.setSpecification(rasterImageSpecification_2());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c53c8fb4-74e0-4824-af88-691484912197");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/0906eade-08a3-42ea-8ae0-2badcb44e121");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "3254f54a-150c-4fb7-ba61-fb087d19cd8e");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ccc194cb-45cf-4fab-bde5-6410b769e64b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9c732edf-9c02-4177-a0d7-e75950cee6fd");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "05eab8c2-9686-4e72-9b0f-71ab626e8072");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 5, 922));
		bean.setFileSize(3129l);
		bean.setMd5("80b46b5bcaa96c85431fdb52f31c628c");
		bean.setMimeType("image/png");
		bean.setName("us_group_64x64.png");
		bean.setResourceSource(moduleSource_2());
		bean.setSpecification(rasterImageSpecification_4());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "bdbffe6f-2bbc-4c2c-a5ed-935fa8660b1c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/561ad846-1692-4828-b078-2c9a0e7fdc8b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_4() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b680606e-7014-4017-a162-df972b7ff778");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "d7c7eae3-36f0-424f-998b-2b28fbe060f4");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "New")));
		return bean;
	}

	@Managed
	private Folder folder_1() {
		Folder bean = session().createRaw(Folder.T, "bbd68b52-2aea-4850-98c2-68c6e63b719d");
		bean.setContent(simpleQueryAction_1());
		bean.setDisplayName(localizedString_8());
		bean.setIcon(adaptiveIcon_1());
		bean.setName("  Users");
		bean.setParent(folder_2());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_1() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "a3276e4a-014c-451f-8a38-38f33e7a8321");
		bean.setDisplayName(localizedString_7());
		bean.setTypeSignature("com.braintribe.model.user.User");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_7() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bd853543-01c8-4c09-b2bf-2f250b4e1989");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Users Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_8() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "195d1d39-3d0b-4666-a6ce-00f73cc4486e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "  Users")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "b0d98272-0518-42dd-9e1c-97f149de3c07");
		bean.setName("Users Icon");
		bean.setRepresentations(Sets.set(resource_4(), resource_7(), resource_5(), resource_6()));
		return bean;
	}

	// Managed
	private Resource resource_4() {
		Resource bean = session().createRaw(Resource.T, "863aaa08-658a-4c7e-b2e7-0a80d9d681d4");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 31, 701));
		bean.setFileSize(936l);
		bean.setMd5("e2f7aee8dff74fa7a341582f1b91661b");
		bean.setMimeType("image/png");
		bean.setName("us_user_32x32.png");
		bean.setResourceSource(moduleSource_5());
		bean.setSpecification(rasterImageSpecification_6());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_5() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c9017ca1-8950-4962-b779-cb1023ba1d68");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/8344093f-7a2e-4ecb-93a8-7e43cebeb006");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_6() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "46e84781-24fe-48a0-8564-71f60b3577f5");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_5() {
		Resource bean = session().createRaw(Resource.T, "e5a0880a-3b23-481c-885b-c8f70c2c6c3f");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 31, 696));
		bean.setFileSize(680l);
		bean.setMd5("0e921338f875ee61c77080bfbad2a83b");
		bean.setMimeType("image/png");
		bean.setName("us_user_24x24.png");
		bean.setResourceSource(moduleSource_6());
		bean.setSpecification(rasterImageSpecification_7());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_6() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "136c9c13-a06d-42b2-b324-48ae8ac81f1b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/de742f98-567b-4391-bc38-43f628e7c4e4");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_7() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "399deb59-86f4-4f1b-99cd-af2644d42d28");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_6() {
		Resource bean = session().createRaw(Resource.T, "9cc90317-33e5-4285-9e1a-2914d3e57303");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 31, 704));
		bean.setFileSize(2359l);
		bean.setMd5("ec5735fbb63cca0829f1c3484f8c9106");
		bean.setMimeType("image/png");
		bean.setName("us_user_64x64.png");
		bean.setResourceSource(moduleSource_7());
		bean.setSpecification(rasterImageSpecification_8());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_7() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "6ecb43a3-b9bf-40ba-8752-6c2047548e45");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/89cc88f4-1c7a-47a0-89a9-8ee86b1d331d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_8() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "e3c689f5-6822-46be-b8cf-25872f7693bf");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_7() {
		Resource bean = session().createRaw(Resource.T, "d2063214-1f0a-45eb-a3b6-5784040d8233");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 31, 692));
		bean.setFileSize(479l);
		bean.setMd5("f05e365231389e618e9c37088e10f8f8");
		bean.setMimeType("image/png");
		bean.setName("us_user_16x16.png");
		bean.setResourceSource(moduleSource_8());
		bean.setSpecification(rasterImageSpecification_9());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_8() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ae91833c-f314-4c50-996e-651242e8f0cf");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/e2a5d13d-0412-43f2-b027-f6476b9a1181");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_9() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d77c526c-561c-41f5-bdc2-a5aca0d56d19");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_2() {
		Folder bean = session().createRaw(Folder.T, "75c89008-b407-4847-b686-92b7cb866f91");
		bean.setDisplayName(localizedString_9());
		bean.setName("IdentityAdministration");
		bean.setParent(folder_3());
		bean.setSubFolders(Lists.list(folder_11(), folder_1(), folder_12()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_9() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "4766ea42-fb2e-4a0e-9ee5-89682f94b39e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Users & Groups")));
		return bean;
	}

	@Managed
	private Folder folder_3() {
		Folder bean = session().createRaw(Folder.T, "44fcfc27-e4c0-400d-981b-27755077735b");
		bean.setDisplayName(localizedString_10());
		bean.setName("Configuration");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_2(), folder_9()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_10() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6b136d02-0a0a-445f-bf38-ffc4966eb4d0");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "User Administration")));
		return bean;
	}

	@Managed
	private Folder folder_4() {
		Folder bean = session().createRaw(Folder.T, "0dd94d64-34f3-4cf8-ba5a-ebbaf3f4414e");
		bean.setDisplayName(localizedString_4());
		bean.setName("root");
		bean.setSubFolders(Lists.list(folder_3(), folder_5()));
		return bean;
	}

	@Managed
	private Folder folder_5() {
		Folder bean = session().createRaw(Folder.T, "d66fc92a-78fa-446f-8b62-5c4b7e20cf4a");
		bean.setDisplayName(localizedString_11());
		bean.setName("System");
		bean.setParent(folder_4());
		bean.setSubFolders(Lists.list(folder_6()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_11() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "fd063d93-f757-49a4-8c18-8b8e998cebb3");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "System")));
		return bean;
	}

	@Managed
	private Folder folder_6() {
		Folder bean = session().createRaw(Folder.T, "7cd1d9d3-6b6e-4310-b7fd-2800d400d175");
		bean.setDisplayName(localizedString_12());
		bean.setName("Resources");
		bean.setParent(folder_5());
		bean.setSubFolders(Lists.list(folder_7(), folder_8()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_12() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c811506a-fe46-4366-b3b5-b7bc02002985");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Resources")));
		return bean;
	}

	// Managed
	private Folder folder_7() {
		Folder bean = session().createRaw(Folder.T, "fb9f32ed-e5bd-40db-ad2f-d4e338094987");
		bean.setContent(templateQueryAction_1());
		bean.setDisplayName(localizedString_14());
		bean.setIcon(adaptiveIcon_2());
		bean.setName("Images");
		bean.setParent(folder_6());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private TemplateQueryAction templateQueryAction_1() {
		TemplateQueryAction bean = session().createRaw(TemplateQueryAction.T, "ee7fabd4-453b-4ae4-9816-8fb9490b0362");
		bean.setDisplayName(localizedString_1());
		bean.setTemplate(template_1());
		return bean;
	}

	// Managed
	private Template template_1() {
		Template bean = session().createRaw(Template.T, "0b7df3a5-e158-4202-9771-e30422e9a332");
		bean.setName(localizedString_13());
		bean.setPrototype(entityQuery_1());
		bean.setTechnicalName("ImagesQueryTemplate");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_13() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2608d4d6-1738-4f8e-b80d-21f5efcf13ca");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images Query Template")));
		return bean;
	}

	// Managed
	private EntityQuery entityQuery_1() {
		EntityQuery bean = session().createRaw(EntityQuery.T, "ce2479dc-544b-448f-9a49-76ed02d6e390");
		bean.setEntityTypeSignature("com.braintribe.model.resource.Resource");
		bean.setRestriction(restriction_1());
		return bean;
	}

	// Managed
	private Restriction restriction_1() {
		Restriction bean = session().createRaw(Restriction.T, "f9eaa599-335d-422e-88f3-6499210c1e1f");
		bean.setCondition(valueComparison_1());
		return bean;
	}

	// Managed
	private ValueComparison valueComparison_1() {
		ValueComparison bean = session().createRaw(ValueComparison.T, "b3218cdc-7436-41ab-a6dc-c431d1f5795e");
		bean.setLeftOperand(propertyOperand_1());
		bean.setOperator(Operator.like);
		bean.setRightOperand("image*");
		return bean;
	}

	// Managed
	private PropertyOperand propertyOperand_1() {
		PropertyOperand bean = session().createRaw(PropertyOperand.T, "ee462fc6-0d02-439a-86bb-a4d1dfc84647");
		bean.setPropertyName("mimeType");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_14() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "ca9803c5-d2ba-4d9a-a523-32a011c3b25b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Images")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_2() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "d15dc41d-93c5-423a-a67c-b864a090a796");
		bean.setName("Images Icon");
		bean.setRepresentations(Sets.set(resource_9(), resource_8()));
		return bean;
	}

	// Managed
	private Resource resource_8() {
		Resource bean = session().createRaw(Resource.T, "81038c39-6e0c-4bbc-b7f9-1468b80206ce");
		bean.setCreated(newGmtDate(2014, 10, 21, 19, 3, 10, 431));
		bean.setFileSize(19748l);
		bean.setMd5("7c374e1d9c4be34abe8c570aaa6dd1d5");
		bean.setMimeType("image/png");
		bean.setName("tribefire_Icons_01_16x16_im.png");
		bean.setResourceSource(moduleSource_9());
		bean.setSpecification(rasterImageSpecification_10());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_9() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "0231942a-d8f1-4443-ae86-2bb7f9fdc2bd");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2120/03/f3a8aa25-be20-4e3a-ad06-ec3fa61706e8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_10() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a3e93b8f-7025-4b79-82bd-965802c32d7f");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_9() {
		Resource bean = session().createRaw(Resource.T, "a473e915-093a-421e-b099-e83df095fa32");
		bean.setCreated(newGmtDate(2014, 10, 21, 19, 3, 10, 490));
		bean.setFileSize(20309l);
		bean.setMd5("32a5620c7e8a63aa1e39491e294cce08");
		bean.setMimeType("image/png");
		bean.setName("tribefire_Icons_01_64x64_im.png");
		bean.setResourceSource(moduleSource_10());
		bean.setSpecification(rasterImageSpecification_11());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_10() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5f46d4fa-8d7a-4c9c-aee3-ffbfc5f5cd53");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2120/03/d7b268e0-5eef-45fa-9d58-c322602aaf39");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_11() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "c5d3a178-f9b0-480d-8f1a-3bcc1920a21e");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Folder folder_8() {
		Folder bean = session().createRaw(Folder.T, "a8661e72-2832-431c-aedc-feb92180a404");
		bean.setContent(simpleQueryAction_2());
		bean.setDisplayName(localizedString_16());
		bean.setIcon(adaptiveIcon_3());
		bean.setName("Files");
		bean.setParent(folder_6());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_2() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "ca3b7c58-2a7e-4799-8ce1-e625ec7fa63e");
		bean.setDisplayName(localizedString_15());
		bean.setTypeSignature("com.braintribe.model.resource.Resource");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_15() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "b22ebf13-6358-47fa-9bea-27b754a25878");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_16() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3656a33f-0b3b-43b9-b52b-1f2cbfec361a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Files")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_3() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "b8d48b90-3eb9-471f-aa8c-49c6b58ce73f");
		bean.setName("Files Icon");
		bean.setRepresentations(Sets.set(resource_11(), resource_10()));
		return bean;
	}

	// Managed
	private Resource resource_10() {
		Resource bean = session().createRaw(Resource.T, "417b9cbb-abc4-4ae0-8d00-0c04cfbf9ecb");
		bean.setCreated(newGmtDate(2014, 10, 21, 19, 3, 10, 480));
		bean.setFileSize(20079l);
		bean.setMd5("6da736391282db589f9420551cb34cc5");
		bean.setMimeType("image/png");
		bean.setName("tribefire_Icons_01_64x64_fi.png");
		bean.setResourceSource(moduleSource_11());
		bean.setSpecification(rasterImageSpecification_12());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_11() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "9278a227-b23b-4996-a555-d42214d0c0f5");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2120/03/4efdd8aa-e3df-4ae8-86e6-4f3b49d5b1ab");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_12() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "dfb18b5b-f503-48d9-820c-8f83e756d32e");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_11() {
		Resource bean = session().createRaw(Resource.T, "83068744-060c-4874-8e45-0db5762895ec");
		bean.setCreated(newGmtDate(2014, 10, 21, 19, 3, 10, 416));
		bean.setFileSize(19781l);
		bean.setMd5("047840a3e9eebb505ffb24021396da12");
		bean.setMimeType("image/png");
		bean.setName("tribefire_Icons_01_16x16_fi.png");
		bean.setResourceSource(moduleSource_12());
		bean.setSpecification(rasterImageSpecification_13());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_12() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "7057f5be-f2d9-440c-ab12-2949caf5e31c");
		bean.setModuleName(currentModuleName());
		bean.setPath("1411/2120/03/0304edd3-6957-45b3-9948-9889bc85f1f7");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_13() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f7e58a04-e174-4e84-b94b-7382985b6f6d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	@Managed
	private Folder folder_9() {
		Folder bean = session().createRaw(Folder.T, "4c37abf7-279b-4e88-8f44-290cfa618259");
		bean.setDisplayName(localizedString_17());
		bean.setName("Authorization");
		bean.setParent(folder_3());
		bean.setSubFolders(Lists.list(folder_10()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_17() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "603c97aa-e0b2-4a81-9652-7bdc4723101e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Authorization")));
		return bean;
	}

	@Managed
	private Folder folder_10() {
		Folder bean = session().createRaw(Folder.T, "19abf796-4dfb-4898-a53a-4d39d216b32f");
		bean.setContent(simpleQueryAction_3());
		bean.setDisplayName(localizedString_19());
		bean.setIcon(adaptiveIcon_4());
		bean.setName("  Roles");
		bean.setParent(folder_9());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_3() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "5f141206-ba94-4956-945e-188dcc5b1edc");
		bean.setDisplayName(localizedString_18());
		bean.setTypeSignature("com.braintribe.model.user.Role");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_18() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8fbe1165-81c6-4d3a-b1c2-a3f042d93f54");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Roles Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_19() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6a9ec36e-a8d0-418d-9483-7f77178ce148");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "  Roles")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_4() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "b92bd9f3-c906-4dde-bca4-9c5d7bb4f5a4");
		bean.setName("Roles Icon");
		bean.setRepresentations(Sets.set(resource_12(), resource_15(), resource_13(), resource_14()));
		return bean;
	}

	// Managed
	private Resource resource_12() {
		Resource bean = session().createRaw(Resource.T, "c6b2a24d-dc50-461a-be97-22b4259710b4");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 34, 680));
		bean.setFileSize(2279l);
		bean.setMd5("ed2131e0dd5032a96a7ef4f9088ba1c3");
		bean.setMimeType("image/png");
		bean.setName("us_role_64x64.png");
		bean.setResourceSource(moduleSource_13());
		bean.setSpecification(rasterImageSpecification_14());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_13() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "05428822-1129-408a-b235-5e0ca95fb9fc");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/2c1fbba3-bc1e-49e9-bfaa-c45ba5299864");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_14() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "1332bdc5-50e5-40ed-9c9e-bc6b2fdb13a1");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_13() {
		Resource bean = session().createRaw(Resource.T, "f1e9a290-d149-4b81-9e1c-7a380897253c");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 34, 677));
		bean.setFileSize(891l);
		bean.setMd5("4d84922f6491127f7a1984ee030bf625");
		bean.setMimeType("image/png");
		bean.setName("us_role_32x32.png");
		bean.setResourceSource(moduleSource_14());
		bean.setSpecification(rasterImageSpecification_15());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_14() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "ffbce79f-8911-492c-a459-761768ec1d90");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/5ba35934-2449-4378-b386-edf20df15d5b");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_15() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "2fc9e113-aff0-4343-b293-097493ff8ae0");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private Resource resource_14() {
		Resource bean = session().createRaw(Resource.T, "96cf4a39-2ea6-4cb5-9787-420b5a17026b");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 34, 673));
		bean.setFileSize(662l);
		bean.setMd5("acda20690ef3eb1bef0b58cd3829cf08");
		bean.setMimeType("image/png");
		bean.setName("us_role_24x24.png");
		bean.setResourceSource(moduleSource_15());
		bean.setSpecification(rasterImageSpecification_16());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_15() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "335d4e99-92ff-4b5f-ab02-b6902b945d64");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/2f10ff54-c33b-4932-a6d3-bab754d76d00");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_16() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "21eee817-5859-48e6-8961-0771ac97c899");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_15() {
		Resource bean = session().createRaw(Resource.T, "a7a03f3d-649b-4497-974a-730e52ee0d35");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 34, 670));
		bean.setFileSize(414l);
		bean.setMd5("72e913d7adabb7325fdc187948e3ead3");
		bean.setMimeType("image/png");
		bean.setName("us_role_16x16.png");
		bean.setResourceSource(moduleSource_16());
		bean.setSpecification(rasterImageSpecification_17());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_16() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "82e49840-bb44-40bb-ac9a-01e61a185991");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/d5c039ce-27f2-4cd0-9a2f-30f9815072ee");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_17() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "d2986b45-c54e-4f9d-8cbc-90f1e52082b6");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Folder folder_11() {
		Folder bean = session().createRaw(Folder.T, "08c51757-a535-4eee-aa5f-0a00db2541ec");
		bean.setContent(simpleQueryAction_4());
		bean.setDisplayName(localizedString_2());
		bean.setIcon(adaptiveIcon_5());
		bean.setName("AllIdentities");
		bean.setParent(folder_2());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_4() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "02297c26-67b7-46ee-8ac9-14cdbaaeb7f0");
		bean.setDisplayName(localizedString_20());
		bean.setTypeSignature("com.braintribe.model.user.Identity");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_20() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "8cf27a2e-5215-45c6-9c62-d32c21976bbb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Identities Query")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_5() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "b2efb433-40b0-4169-9d7e-81ac07a5b36d");
		bean.setName("Identities Icon");
		bean.setRepresentations(Sets.set(resource_16(), resource_19(), resource_18(), resource_17()));
		return bean;
	}

	// Managed
	private Resource resource_16() {
		Resource bean = session().createRaw(Resource.T, "ca58ca0b-cd99-4c44-8920-27da47472f8d");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 4, 426));
		bean.setFileSize(1021l);
		bean.setMd5("97e6f429a6f2b19573f630d1fa4fd92f");
		bean.setMimeType("image/png");
		bean.setName("us_identities_24x24.png");
		bean.setResourceSource(moduleSource_17());
		bean.setSpecification(rasterImageSpecification_18());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_17() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "f92cb903-d543-4088-ba33-c79482b39201");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/438f1709-3c54-4c25-95c2-31579b52671d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_18() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "17dadca3-a4b0-4b9b-8fa1-467b376e8b75");
		bean.setPageCount(1);
		bean.setPixelHeight(24);
		bean.setPixelWidth(24);
		return bean;
	}

	// Managed
	private Resource resource_17() {
		Resource bean = session().createRaw(Resource.T, "de3c4881-6504-4e09-a0c1-e911357ac1c6");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 4, 435));
		bean.setFileSize(3408l);
		bean.setMd5("0e6f87b9782dddb5396f31f55141ad69");
		bean.setMimeType("image/png");
		bean.setName("us_identities_64x64.png");
		bean.setResourceSource(moduleSource_18());
		bean.setSpecification(rasterImageSpecification_19());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_18() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5c10fb58-f332-441f-815c-fef37aaa9f41");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/207b4a07-abda-449e-a73c-5b075cdc3f5c");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_19() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "b4dab4e9-35b7-4af9-98c2-498e40eefc0c");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private Resource resource_18() {
		Resource bean = session().createRaw(Resource.T, "1d4ffcc4-0e2e-4fad-8dc8-9d25f0cd7d5b");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 4, 422));
		bean.setFileSize(627l);
		bean.setMd5("8de575038f8d8f94991c6a1b8ab2e862");
		bean.setMimeType("image/png");
		bean.setName("us_identities_16x16.png");
		bean.setResourceSource(moduleSource_19());
		bean.setSpecification(rasterImageSpecification_1());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_19() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "d95dd65e-f15e-4b11-a1a1-96c24f602a26");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/4e4dc8c8-e258-4bf7-b3d5-7fe0e6be7afc");
		return bean;
	}

	// Managed
	private Resource resource_19() {
		Resource bean = session().createRaw(Resource.T, "3a2c88f4-c4fa-4a03-8e95-011f17986afd");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 6, 4, 430));
		bean.setFileSize(1355l);
		bean.setMd5("69c74f1b248ce758d59babab65674cb2");
		bean.setMimeType("image/png");
		bean.setName("us_identities_32x32.png");
		bean.setResourceSource(moduleSource_20());
		bean.setSpecification(rasterImageSpecification_20());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_20() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5c580f34-dc6f-4ab9-a5a8-d66d0ff9aa46");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/06/44a67b62-b3e4-4d3e-8925-dd73d688fd99");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_20() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "72541ed7-6e59-4279-9590-78b3fc2f46a9");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	@Managed
	private Folder folder_12() {
		Folder bean = session().createRaw(Folder.T, "bd4d48e9-807c-4af1-8a6f-dac1a9396ff4");
		bean.setContent(simpleQueryAction_5());
		bean.setDisplayName(localizedString_22());
		bean.setIcon(adaptiveIcon_6());
		bean.setName("  Groups");
		bean.setParent(folder_2());
		bean.setTags(Sets.set("homeFolder"));
		return bean;
	}

	// Managed
	private SimpleQueryAction simpleQueryAction_5() {
		SimpleQueryAction bean = session().createRaw(SimpleQueryAction.T, "495af5da-3380-4019-b067-e32158ad36c8");
		bean.setDisplayName(localizedString_21());
		bean.setTypeSignature("com.braintribe.model.user.Group");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_21() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a2079229-3905-42e3-84cf-26fddc9169b7");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Groups Query")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_22() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "43c32eb8-8854-4d7e-b0f1-e9ee1291f32f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "  Groups")));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_6() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "bc7a1484-78e7-4f13-8bfe-c64c70848872");
		bean.setName("Groups Icon");
		bean.setRepresentations(Sets.set(resource_2(), resource_20(), resource_21(), resource_1()));
		return bean;
	}

	// Managed
	private Resource resource_20() {
		Resource bean = session().createRaw(Resource.T, "c54fd94c-e52f-44ca-9f98-f0f568ec4e36");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 5, 912));
		bean.setFileSize(566l);
		bean.setMd5("a991c0d1cba91ce249b548f24b30ba7e");
		bean.setMimeType("image/png");
		bean.setName("us_group_16x16.png");
		bean.setResourceSource(moduleSource_21());
		bean.setSpecification(rasterImageSpecification_21());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_21() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "5263a455-e4c0-4b58-83a2-ced733afccd1");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/519919cf-06f9-4a5b-9615-d58461057bc6");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_21() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "93aacfdd-d44c-4a67-8439-33e4db121c4d");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_21() {
		Resource bean = session().createRaw(Resource.T, "b2d2aedd-94a9-4a6b-83e6-45fe6116a36d");
		bean.setCreated(newGmtDate(2015, 4, 26, 16, 7, 5, 918));
		bean.setFileSize(1203l);
		bean.setMd5("2d3853cc68b09746aea5eb5ad4172d43");
		bean.setMimeType("image/png");
		bean.setName("us_group_32x32.png");
		bean.setResourceSource(moduleSource_22());
		bean.setSpecification(rasterImageSpecification_22());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_22() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "61caf2fc-317c-4a5a-9e47-ee5a45e1ec7b");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2618/07/32640264-a129-41ef-8b4e-5eaf9c41b527");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_22() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "76d8287f-e818-4e69-96a6-13051a46bbad");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_7() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "da842888-87b7-430f-8b3d-a47bf83cf598");
		bean.setName("View Icon");
		bean.setRepresentations(Sets.set(resource_22(), resource_23()));
		return bean;
	}

	// Managed
	private Resource resource_22() {
		Resource bean = session().createRaw(Resource.T, "1357c717-15ad-4545-9c50-e8b19e56696c");
		bean.setCreated(newGmtDate(2015, 4, 25, 14, 17, 43, 852));
		bean.setFileSize(477l);
		bean.setMd5("068351a0336c6cfae580e1a64828cd8e");
		bean.setMimeType("image/png");
		bean.setName("view_16x16.png");
		bean.setResourceSource(moduleSource_23());
		bean.setSpecification(rasterImageSpecification_24());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_23() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "cae1d3de-44d7-4cec-84d6-d280fdee93b7");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2516/17/5cb9055c-9528-442f-b95b-8d0353e30675");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_24() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "a6a2deda-e5a4-4b11-bee7-19f9b5aa3cb0");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_23() {
		Resource bean = session().createRaw(Resource.T, "df5e0ec2-a7b2-4fe8-863e-64708a41872f");
		bean.setCreated(newGmtDate(2015, 4, 25, 14, 17, 43, 831));
		bean.setFileSize(1210l);
		bean.setMd5("b599c004747fbdd06bc59da58b058edb");
		bean.setMimeType("image/png");
		bean.setName("view_32x32.png");
		bean.setResourceSource(moduleSource_24());
		bean.setSpecification(rasterImageSpecification_25());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_24() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "4222a006-5eb1-4fc4-b4a3-9f18c4be33ed");
		bean.setModuleName(currentModuleName());
		bean.setPath("1505/2516/17/4a9c64d5-758d-4876-9b24-0088e7ea8ca8");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_25() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "f04f921b-4f3f-438c-a2a3-5189b991e99b");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private LocalizedString localizedString_23() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "bbdc36e4-059f-4d3b-8db9-885fe08a0baf");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Exchange View")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_24() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "576696da-be67-477f-b2c9-74a196444c7a");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit Template")));
		return bean;
	}

	@Managed
	private Folder folder_13() {
		Folder bean = session().createRaw(Folder.T, "dba38435-150a-4f5e-b25a-470ff748b574");
		bean.setDisplayName(localizedString_25());
		bean.setName("$addToClipboard");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_25() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "189d9e3d-b65c-4b8b-8697-7d74ff704175");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add To Clipboard")));
		return bean;
	}

	@Managed
	private Folder folder_14() {
		Folder bean = session().createRaw(Folder.T, "c1be3f37-8bae-4e8f-8144-4a60eb956918");
		bean.setDisplayName(localizedString_26());
		bean.setName("actionbar");
		bean.setSubFolders(Lists.list(folder_15(), folder_16(), folder_17(), folder_18(), folder_19(), folder_20(), folder_21(), folder_22(),
				folder_23(), folder_24(), folder_25(), folder_13(), folder_26()));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_26() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "cab296db-2771-4651-aaef-7ab62327d2fe");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Action bar")));
		return bean;
	}

	// Managed
	private Folder folder_15() {
		Folder bean = session().createRaw(Folder.T, "253f0566-95c2-40da-af63-689f08935d84");
		bean.setDisplayName(localizedString_23());
		bean.setName("$exchangeContentView");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private Folder folder_16() {
		Folder bean = session().createRaw(Folder.T, "8b977be7-3465-4311-a16e-5d26ed4ebd38");
		bean.setDisplayName(localizedString_27());
		bean.setName("$workWithEntity");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_27() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "366aa279-9d02-4778-810c-5585f1777c9c");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Open")));
		return bean;
	}

	// Managed
	private Folder folder_17() {
		Folder bean = session().createRaw(Folder.T, "ef4a01d5-5e5a-40a8-b533-a7f1a1acca40");
		bean.setDisplayName(localizedString_28());
		bean.setName("$deleteEntity");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_28() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9ddfacb9-51ca-48d5-b05f-16e97508a782");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Delete ")));
		return bean;
	}

	// Managed
	private Folder folder_18() {
		Folder bean = session().createRaw(Folder.T, "665f2130-d98c-4884-9e6c-2c1df46340c4");
		bean.setDisplayName(localizedString_29());
		bean.setName("$changeInstance");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_29() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "f5a1ff71-a4d7-4ffb-afff-04d6f8d35605");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Assign")));
		return bean;
	}

	// Managed
	private Folder folder_19() {
		Folder bean = session().createRaw(Folder.T, "0b5088e7-890d-44e2-a32b-5555acdb1891");
		bean.setDisplayName(localizedString_5());
		bean.setName("$clearEntityToNull");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private Folder folder_20() {
		Folder bean = session().createRaw(Folder.T, "7b1a511c-d0a8-4872-92ae-d02893c5f956");
		bean.setDisplayName(localizedString_30());
		bean.setName("$addToCollection");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_30() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "88cefd27-1be0-4942-8921-da11b5288f5e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Add")));
		return bean;
	}

	// Managed
	private Folder folder_21() {
		Folder bean = session().createRaw(Folder.T, "34cf31f4-ec00-4bfc-bfed-cd49f3499e43");
		bean.setDisplayName(localizedString_31());
		bean.setName("$insertBeforeToList");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_31() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "aec5a8e7-49f4-4f93-a802-1b2f861293e8");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Insert Before")));
		return bean;
	}

	// Managed
	private Folder folder_22() {
		Folder bean = session().createRaw(Folder.T, "f4cdbb0e-f25f-4eca-8648-67c8e4bf964c");
		bean.setDisplayName(localizedString_32());
		bean.setName("$removeFromCollection");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_32() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "2317bae4-0260-4827-b8ad-d721a5ee6396");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Remove")));
		return bean;
	}

	// Managed
	private Folder folder_23() {
		Folder bean = session().createRaw(Folder.T, "fcb3e303-b5f9-40b1-89c8-56634d2b60a0");
		bean.setDisplayName(localizedString_33());
		bean.setName("$clearCollection");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_33() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "5cecea93-aa52-456f-92ce-4ab170264f57");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Clear")));
		return bean;
	}

	// Managed
	private Folder folder_24() {
		Folder bean = session().createRaw(Folder.T, "089ea0dc-2c55-496c-9978-139cc4c69da1");
		bean.setDisplayName(localizedString_34());
		bean.setName("$refreshEntities");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_34() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "a605df01-9626-450f-b7e8-85c5515627de");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Refresh")));
		return bean;
	}

	// Managed
	private Folder folder_25() {
		Folder bean = session().createRaw(Folder.T, "1a73db85-1e36-48ec-8364-81920d1ac960");
		bean.setDisplayName(localizedString_35());
		bean.setName("$ResourceDownload");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_35() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e019e1a4-f722-4cc0-9cd7-483000deca1d");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Download")));
		return bean;
	}

	// Managed
	private Folder folder_26() {
		Folder bean = session().createRaw(Folder.T, "46e8a6a3-e2b2-4827-8f55-b7b4be6533cb");
		bean.setDisplayName(localizedString_36());
		bean.setName("$gimaOpener");
		bean.setParent(folder_14());
		return bean;
	}

	// Managed
	private LocalizedString localizedString_36() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "38dae9b5-3024-4cdb-b437-7f7392fe519e");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Edit")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_37() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "23cc648f-1be6-40bb-a846-8ea5cc2df162");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Switch To")));
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_1() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "a7072d23-dc75-4f56-9116-1aad57dfb04e");
		bean.setDisplayName(localizedString_38());
		bean.setFolders(Lists.list(folder_12(), folder_10(), folder_1()));
		bean.setName("homeFolder");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_38() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "6340fea1-f58a-4039-8f49-116b8bbb27bb");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Home Folders")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_39() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0203efea-2f91-4969-8f46-0515d074e88b");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private Folder folder_27() {
		Folder bean = session().createRaw(Folder.T, "06180840-aef6-45f1-9b86-22716e02132c");
		bean.setDisplayName(localizedString_40());
		bean.setName("$recordTemplateScript");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_40() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9b1697f8-1cd6-40dc-9a24-cdbd14aa5073");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Record")));
		return bean;
	}

	// Managed
	private Folder folder_28() {
		Folder bean = session().createRaw(Folder.T, "55a36826-bfb5-46ce-b172-84970e1814a8");
		bean.setDisplayName(localizedString_6());
		bean.setName("$instantiateEntity");
		return bean;
	}

	// Managed
	private Folder folder_29() {
		Folder bean = session().createRaw(Folder.T, "f7baa322-8e5a-4181-8cdc-620e246fecac");
		bean.setDisplayName(localizedString_41());
		bean.setName("$uncondenseLocal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_41() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "254f0f31-943d-42bc-9ff4-0a8724d676de");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Hide Details Here")));
		return bean;
	}

	// Managed
	private Folder folder_30() {
		Folder bean = session().createRaw(Folder.T, "35929f9f-2897-4e35-bf1e-ab3325410ddd");
		bean.setDisplayName(localizedString_24());
		bean.setName("$editTemplateScript");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_42() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "249323ee-579a-4e63-8e1e-fb994ec98508");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Entry Points")));
		return bean;
	}

	// Managed
	private Folder folder_31() {
		Folder bean = session().createRaw(Folder.T, "05e9ccfe-02c4-46f8-b106-689a09c0064c");
		bean.setDisplayName(localizedString_43());
		bean.setName("$displayMode");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_43() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3aae18d5-f598-4f10-9ef8-4ad43c0ddf81");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "List View")));
		return bean;
	}

	// Managed
	private Folder folder_32() {
		Folder bean = session().createRaw(Folder.T, "f00d41e8-3054-4605-ae3f-730c7b92b448");
		bean.setDisplayName(localizedString_3());
		bean.setIcon(adaptiveIcon_7());
		bean.setName("View");
		return bean;
	}

	// Managed
	private Folder folder_33() {
		Folder bean = session().createRaw(Folder.T, "04fc8cb5-732a-4f53-8909-f8c099200c6d");
		bean.setDisplayName(localizedString_39());
		bean.setName("$detailsPanelVisibility");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_44() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "c12f803e-f779-4ca1-b377-24664fd42974");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "Show/Hide Details")));
		return bean;
	}

	// Managed
	private Folder folder_34() {
		Folder bean = session().createRaw(Folder.T, "abec1e42-5f2a-4211-9030-6bdeb7fb9647");
		bean.setDisplayName(localizedString_37());
		bean.setName("$openGmeForAccessInNewTab");
		return bean;
	}

	// Managed
	private WorkbenchPerspective workbenchPerspective_2() {
		WorkbenchPerspective bean = session().createRaw(WorkbenchPerspective.T, "fc447269-8691-4e56-8808-db8639be59eb");
		bean.setDisplayName(localizedString_42());
		bean.setFolders(Lists.list(folder_4()));
		bean.setName("root");
		return bean;
	}

	// Managed
	private Folder folder_35() {
		Folder bean = session().createRaw(Folder.T, "ee959eab-0c96-47b6-9ed4-51b096af9dd1");
		bean.setDisplayName(localizedString_44());
		bean.setName("$condenseEntity");
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