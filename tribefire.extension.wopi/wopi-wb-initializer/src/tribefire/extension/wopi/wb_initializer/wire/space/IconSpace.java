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
package tribefire.extension.wopi.wb_initializer.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Icon;
import com.braintribe.model.resource.SimpleIcon;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.wopi.wb_initializer.wire.contract.IconContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.ResourcesContract;

/**
 * Space for defining ICONs
 * 
 *
 */
@Managed
public class IconSpace extends AbstractInitializerSpace implements IconContract {

	@Import
	ResourcesContract resources;

	@Managed
	@Override
	public SimpleIcon logoIcon() {
		SimpleIcon bean = create(SimpleIcon.T);

		bean.setName("Logo Icon");
		bean.setImage(resources.logoPng());

		return bean;
	}

	@Managed
	@Override
	public Icon magnifierIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		bean.setName("Magnifier Icon");
		bean.setRepresentations(set(resources.magnifier16Png(), resources.magnifier32Png(), resources.magnifier64Png()));
		return bean;
	}

	@Managed
	@Override
	public Icon tribefireIcon() {
		SimpleIcon bean = create(SimpleIcon.T);
		bean.setName("tribefire Icon");
		bean.setImage(resources.tribefire16Png());
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon officeIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		bean.setName("office-icon");
		bean.setRepresentations(set(resources.office16Png(), resources.office32Png(), resources.office64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon adxIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		bean.setName("adx-icon");
		bean.setRepresentations(set(resources.adx16Png(), resources.adx32Png(), resources.adx64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon sessionExpiredIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("session-expired-icon");
		bean.setRepresentations(set(resources.sessionExpired16Png(), resources.sessionExpired32Png(), resources.sessionExpired64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon addDemoAndTestingDocsIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("add-demo-docs-icon");
		bean.setRepresentations(set(resources.addDemoDocs16Png(), resources.addDemoDocs32Png(), resources.addDemoDocs64Png()));
		return bean;
	}

	@Managed
	@Override
	public Icon wordIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("word-icon");
		// @formatter:off
		bean.setRepresentations(set(
				resources.word16Png()));
		// @formatter:on

		return bean;
	}

	@Managed
	@Override
	public Icon excelIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("excel-icon");
		// @formatter:off
		bean.setRepresentations(set(
				resources.excel16Png()));
		// @formatter:on

		return bean;
	}

	@Managed
	@Override
	public Icon powerpointIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("powerpoint-icon");
		// @formatter:off
		bean.setRepresentations(set(
				resources.powerpoint16Png()));
		// @formatter:on

		return bean;
	}

	// -----------------------------------------------------------------------
	// STANDARD ACTIONS
	// -----------------------------------------------------------------------

	@Managed
	@Override
	public AdaptiveIcon viewIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("view-icon");
		bean.setRepresentations(set(resources.view16Png(), resources.view32Png(), resources.view64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon openIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("open-icon");
		bean.setRepresentations(set(resources.open16Png(), resources.open32Png(), resources.open64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon editIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("edit-icon");
		bean.setRepresentations(set(resources.edit16Png(), resources.edit32Png(), resources.edit64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon deleteIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("delete-icon");
		bean.setRepresentations(set(resources.delete16Png(), resources.delete32Png(), resources.delete64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon assignIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("assign-icon");
		bean.setRepresentations(set(resources.assign16Png(), resources.assign32Png(), resources.assign64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon addIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("add-icon");
		bean.setRepresentations(set(resources.add16Png(), resources.add32Png(), resources.add64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon removeIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("remove-icon");
		bean.setRepresentations(set(resources.remove16Png(), resources.remove32Png(), resources.remove64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon refreshIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("refresh-icon");
		bean.setRepresentations(set(resources.refresh16Png(), resources.refresh32Png(), resources.refresh64Png()));
		return bean;
	}
	@Managed
	@Override
	public AdaptiveIcon downloadIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("download-icon");
		bean.setRepresentations(set(resources.download16Png(), resources.download32Png(), resources.download64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon runIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("run-icon");
		bean.setRepresentations(set(resources.run16Png(), resources.run32Png(), resources.run64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon homeIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("home-icon");
		bean.setRepresentations(set(resources.home16Png(), resources.home32Png(), resources.home64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon mailIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("mail-icon");
		bean.setRepresentations(set(resources.mail16Png(), resources.mail32Png(), resources.mail64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon copyIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("copy-icon");
		bean.setRepresentations(set(resources.copy16Png(), resources.copy32Png(), resources.copy64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon changesIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("changes-icon");
		bean.setRepresentations(set(resources.changes16Png(), resources.changes32Png(), resources.changes64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon uploadIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("upload-icon");
		bean.setRepresentations(set(resources.upload16Png(), resources.upload32Png(), resources.upload64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon backIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("back-icon");
		bean.setRepresentations(set(resources.back16Png(), resources.back32Png(), resources.back64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon nextIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("next-icon");
		bean.setRepresentations(set(resources.next16Png(), resources.next32Png(), resources.next64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon commitIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("commit-icon");
		bean.setRepresentations(set(resources.commit16Png(), resources.commit32Png(), resources.commit64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon expandIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("expand-icon");
		bean.setRepresentations(set(resources.expand16Png(), resources.expand32Png(), resources.expand64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon fileIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("file-icon");
		bean.setRepresentations(set(resources.file16Png(), resources.file32Png(), resources.file64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon eventIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("event-icon");
		bean.setRepresentations(set(resources.event16Png(), resources.event32Png(), resources.event64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon settingsIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("settings-icon");
		bean.setRepresentations(set(resources.settings16Png(), resources.settings32Png(), resources.settings64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon infoIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("info-icon");
		bean.setRepresentations(set(resources.info16Png(), resources.info32Png(), resources.info64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon pictureIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("picture-icon");
		bean.setRepresentations(set(resources.picture16Png(), resources.picture32Png(), resources.picture64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon codeIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("code-icon");
		bean.setRepresentations(set(resources.code16Png(), resources.code32Png(), resources.code64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon healthIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("health-icon");
		bean.setRepresentations(set(resources.health16Png(), resources.health32Png(), resources.health64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon activeIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("active-icon");
		bean.setRepresentations(set(resources.active16Png(), resources.active32Png(), resources.active64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon inactiveIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("inactive-icon");
		bean.setRepresentations(set(resources.inactive16Png(), resources.inactive32Png(), resources.inactive64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon statisticsIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("statistics-icon");
		bean.setRepresentations(set(resources.statistics16Png(), resources.statistics32Png(), resources.statistics64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon sessionOpenIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("session-open-icon");
		bean.setRepresentations(set(resources.sessionOpen16Png(), resources.sessionOpen32Png(), resources.sessionOpen64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon sessionClosedIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("session-closed-icon");
		bean.setRepresentations(set(resources.sessionClosed16Png(), resources.sessionClosed32Png(), resources.sessionClosed64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon repositoryTemplateIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("repository-template-icon");
		bean.setRepresentations(set(resources.repositoryTemplate16Png(), resources.repositoryTemplate32Png(), resources.repositoryTemplate64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon documentationIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("documentation-icon");
		bean.setRepresentations(set(resources.documentation16Png(), resources.documentation32Png(), resources.documentation64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon academyIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("academy-icon");
		bean.setRepresentations(set(resources.academy16Png(), resources.academy32Png(), resources.academy64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon publicKeyIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("public-key-icon");
		bean.setRepresentations(set(resources.publicKey16Png(), resources.publicKey32Png(), resources.publicKey64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon migrationToolIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);

		bean.setName("migration-tool-icon");
		bean.setRepresentations(set(resources.migrationTool16Png(), resources.migrationTool32Png(), resources.migrationTool64Png()));
		return bean;
	}

	@Managed
	@Override
	public AdaptiveIcon quickAccessIcon() {
		AdaptiveIcon bean = create(AdaptiveIcon.T);
		bean.setName("quickAccess-icon");
		bean.setRepresentations(set(resources.quickAccess16Png()));
		return bean;
	}

}
