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
package tribefire.cortex.assets.auth.initializer.wire.space;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.specification.RasterImageSpecification;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Maps;
import com.braintribe.wire.api.util.Sets;

import tribefire.cortex.assets.auth.initializer.wire.contract.AuthInitializerContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.module.model.resource.ModuleSource;

@Managed
public class AuthInitializerSpace extends AbstractInitializerSpace implements AuthInitializerContract {

	@Override
	public void initialize() {
		role_2();
		role_3();
		user_1();
		adaptiveIcon_1();
	}

	// Managed
	private Role role_1() {
		Role bean = session().createRaw(Role.T, "266d6239-82dc-47b6-929c-c001070e976c");
		bean.setDescription(localizedString_1());
		bean.setId("d1f84ead-9cf1-4572-97df-f00da5acd5c1");
		bean.setLocalizedName(localizedString_2());
		bean.setName("tf-locksmith");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_1() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "9fc4d0cd-e56f-475c-a93e-ace5eee518ef");
		bean.setLocalizedValues(
				Maps.map(Maps.entry("default", "role having various security settings disabled in order to repair broken configuration.")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_2() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "e32a4b1b-9aae-424a-ab7b-4a3bdda37f56");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tribefire Locksmith Role")));
		return bean;
	}

	// Managed
	private Role role_2() {
		Role bean = session().createRaw(Role.T, "6b520ae4-e09c-419a-ac9b-55add98f80ab");
		bean.setDescription(localizedString_3());
		bean.setId("4d7cbe9b-aab7-480c-9135-78779122d69f");
		bean.setLocalizedName(localizedString_4());
		bean.setName("tf-internal");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_3() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "0d61f436-9e82-4836-a3a3-36185ac84e65");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "internal role that bypasses the SecurityAspect completely.")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_4() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "904d96e3-fb01-46d7-9498-1f9ad8ca4dff");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tribefire Internal Role")));
		return bean;
	}

	// Managed
	private Role role_3() {
		Role bean = session().createRaw(Role.T, "4bb84832-4a2f-4f02-a7a2-811818223cc5");
		bean.setDescription(localizedString_5());
		bean.setId("97d6c636-8dd0-41fe-994c-1aa5d5bbba2e");
		bean.setLocalizedName(localizedString_6());
		bean.setName("tf-admin");
		return bean;
	}

	// Managed
	private LocalizedString localizedString_5() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "3a39c0da-78ad-46a4-93e4-5bd35b9cfaed");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "admin role that can be used to configure the system.")));
		return bean;
	}

	// Managed
	private LocalizedString localizedString_6() {
		LocalizedString bean = session().createRaw(LocalizedString.T, "07722c7e-6ff6-49ef-8aa8-b67227c3965f");
		bean.setLocalizedValues(Maps.map(Maps.entry("default", "tribefire Admin Role")));
		return bean;
	}

	// Managed
	private User user_1() {
		User bean = session().createRaw(User.T, "c136a100-22a9-4bfe-8cf4-d8fbd48c918b");
		bean.setFirstName("");
		bean.setId("d968cb04-3495-466a-96de-b9aa5e9bf3b6");
		bean.setLastName("Locksmith");
		bean.setName("locksmith");
		bean.setRoles(Sets.set(role_1()));
		return bean;
	}

	// Managed
	private AdaptiveIcon adaptiveIcon_1() {
		AdaptiveIcon bean = session().createRaw(AdaptiveIcon.T, "bcad7a09-21dc-42cb-b92b-f3d2e027804d");
		bean.setName("cortex-icon");
		bean.setRepresentations(Sets.set(resource_1(), resource_2(), resource_3(), resource_4()));
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_1() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e0ba4728-13b9-4db5-89fb-03ad93535e98");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/2717/0541/d579b1cf-2e59-4a32-ac83-cdd326f5078e");
		return bean;
	}

	// Managed
	private Resource resource_1() {
		Resource bean = session().createRaw(Resource.T, "a12e85e1-512d-4e49-9fd9-0b5ce4fea4ad");
		bean.setCreated(newGmtDate(2018, 8, 27, 15, 5, 41, 691));
		bean.setCreator("cortex");
		bean.setFileSize(324l);
		bean.setMd5("775c3cd4c49fda466104082b364a6c07");
		bean.setMimeType("image/png");
		bean.setName("cx.16.png");
		bean.setResourceSource(moduleSource_1());
		bean.setSpecification(rasterImageSpecification_1());
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_1() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "933a7bac-507c-4ebf-a3a0-1cdd7c7d087c");
		bean.setPageCount(1);
		bean.setPixelHeight(16);
		bean.setPixelWidth(16);
		return bean;
	}

	// Managed
	private Resource resource_2() {
		Resource bean = session().createRaw(Resource.T, "30fa178e-df0d-4332-95d7-ab01d7c5a94e");
		bean.setCreated(newGmtDate(2018, 8, 27, 15, 5, 42, 777));
		bean.setCreator("cortex");
		bean.setFileSize(705l);
		bean.setMd5("c64326e992080aaca38481f046f1984a");
		bean.setMimeType("image/png");
		bean.setName("cx.32.png");
		bean.setResourceSource(moduleSource_2());
		bean.setSpecification(rasterImageSpecification_2());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_2() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "8d1cdecb-4172-42f8-89d4-62179e6cc860");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/2717/0542/cada23f0-3569-4fc9-be4d-cd8f1fce6d2f");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_2() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "706d12ad-08b4-4c21-b577-9d91e21a064f");
		bean.setPageCount(1);
		bean.setPixelHeight(32);
		bean.setPixelWidth(32);
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_3() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "586eabc7-8cc4-41b2-91e6-a0dc6a653b30");
		bean.setPageCount(1);
		bean.setPixelHeight(64);
		bean.setPixelWidth(64);
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_3() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "c9612ba7-1da5-4903-9f79-bdd2b6fa09de");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/2717/0542/1d037d7c-2f55-4908-a92a-a91314416c35");
		return bean;
	}

	// Managed
	private Resource resource_3() {
		Resource bean = session().createRaw(Resource.T, "2a4c3dd0-8ebd-4ca8-a7b6-c87ecdcb1ee6");
		bean.setCreated(newGmtDate(2018, 8, 27, 15, 5, 42, 798));
		bean.setCreator("cortex");
		bean.setFileSize(1507l);
		bean.setMd5("50b8f1306210adb6f392dbdc169612c6");
		bean.setMimeType("image/png");
		bean.setName("cx.64.png");
		bean.setResourceSource(moduleSource_3());
		bean.setSpecification(rasterImageSpecification_3());
		return bean;
	}

	// Managed
	private Resource resource_4() {
		Resource bean = session().createRaw(Resource.T, "26b1bb6c-8681-4ee2-b2b6-e9807afd1259");
		bean.setCreated(newGmtDate(2018, 8, 27, 15, 5, 42, 810));
		bean.setCreator("cortex");
		bean.setFileSize(3377l);
		bean.setMd5("d0dfa4f1b743311f9c851a9fb22ce38a");
		bean.setMimeType("image/png");
		bean.setName("cx.128.png");
		bean.setResourceSource(moduleSource_4());
		bean.setSpecification(rasterImageSpecification_4());
		return bean;
	}

	// Managed
	private ModuleSource moduleSource_4() {
		ModuleSource bean = session().createRaw(ModuleSource.T, "e014b508-bc10-40c2-85ca-a54881841d21");
		bean.setModuleName(currentModuleName());
		bean.setPath("1809/2717/0542/e0f1b130-61b8-40e7-978a-1d056764bc0d");
		return bean;
	}

	// Managed
	private RasterImageSpecification rasterImageSpecification_4() {
		RasterImageSpecification bean = session().createRaw(RasterImageSpecification.T, "aafd25ad-b7c0-4589-8442-7a29ba7fa128");
		bean.setPageCount(1);
		bean.setPixelHeight(128);
		bean.setPixelWidth(128);
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