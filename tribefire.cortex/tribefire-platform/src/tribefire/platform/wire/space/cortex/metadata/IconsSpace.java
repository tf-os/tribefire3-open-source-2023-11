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
package tribefire.platform.wire.space.cortex.metadata;

import static com.braintribe.wire.api.util.Sets.set;

import java.nio.file.Path;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.space.MasterResourcesSpace;

@Managed
public class IconsSpace implements WireSpace {

	@Import
	private MasterResourcesSpace resources;

	@Managed
	public AdaptiveIcon access() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Access Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Access_16x16.png"),
				icon("Access_24x24.png"),
				icon("Access_32x32.png"),
				icon("Access_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon collaborativeSmoodAccess() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("CollaborativeSmoodAccess Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("SmoodAccess_16x16.png"),
				icon("SmoodAccess_24x24.png"),
				icon("SmoodAccess_32x32.png"),
				icon("SmoodAccess_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon connection() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Connection Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Connection_16x16.png"),
				icon("Connection_24x24.png"),
				icon("Connection_32x32.png"),
				icon("Connection_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon file() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("File Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("File_16x16.png"),
				icon("File_24x24.png"),
				icon("File_32x32.png"),
				icon("File_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon image() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Image Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Image_16x16.png"),
				icon("Image_24x24.png"),
				icon("Image_32x32.png"),
				icon("Image_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon cartridge() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Cartridge Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Cartridge_16x16.png"),
				icon("Cartridge_24x24.png"),
				icon("Cartridge_32x32.png"),
				icon("Cartridge_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon stateChangeProcessor() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("StateChangeProcessor Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("StateChangeProcessor_16x16.png"),
				icon("StateChangeProcessor_24x24.png"),
				icon("StateChangeProcessor_32x32.png"),
				icon("StateChangeProcessor_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon actionProcessor() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("ActionProcessor Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("ActionProcessor_16x16.png"),
				icon("ActionProcessor_24x24.png"),
				icon("ActionProcessor_32x32.png"),
				icon("ActionProcessor_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon entityType() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("EntityType Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("EntityType_16x16.png"),
				icon("EntityType_24x24.png"),
				icon("EntityType_32x32.png"),
				icon("EntityType_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon enumType() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("EnumType Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("EnumType_16x16.png"),
				icon("EnumType_24x24.png"),
				icon("EnumType_32x32.png"),
				icon("EnumType_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon baseModel() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("BaseModel Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("BaseModel_16x16.png"),
				icon("BaseModel_24x24.png"),
				icon("BaseModel_32x32.png"),
				icon("BaseModel_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon customModel() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("CustomModel Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("CustomModel_16x16.png"),
				icon("CustomModel_24x24.png"),
				icon("CustomModel_32x32.png"),
				icon("CustomModel_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon group() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Group Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Group_16x16.png"),
				icon("Group_24x24.png"),
				icon("Group_32x32.png"),
				icon("Group_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon app() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("App Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("App_16x16.png"),
				icon("App_24x24.png"),
				icon("App_32x32.png"),
				icon("App_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon streamer() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Streamer Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Streamer_16x16.png"),
				icon("Streamer_24x24.png"),
				icon("Streamer_32x32.png"),
				icon("Streamer_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon stateEngine() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("StateEngine Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("StateEngine_16x16.png"),
				icon("StateEngine_24x24.png"),
				icon("StateEngine_32x32.png"),
				icon("StateEngine_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon condition() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Condition Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Condition_16x16.png"),
				icon("Condition_24x24.png"),
				icon("Condition_32x32.png"),
				icon("Condition_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon transitionProcessor() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("TransitionProcessor Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("TransitionProcessor_16x16.png"),
				icon("TransitionProcessor_24x24.png"),
				icon("TransitionProcessor_32x32.png"),
				icon("TransitionProcessor_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon processDefinition() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("ProcessDefinition Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("ProcessDefinition_16x16.png"),
				icon("ProcessDefinition_24x24.png"),
				icon("ProcessDefinition_32x32.png"),
				icon("ProcessDefinition_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	@Managed
	public AdaptiveIcon worker() {
		AdaptiveIcon bean = AdaptiveIcon.T.create();
		bean.setName("Worker Icon");
		// @formatter:off
		bean.setRepresentations(
			set(
				icon("Worker_16x16.png"),
				icon("Worker_24x24.png"),
				icon("Worker_32x32.png"),
				icon("Worker_64x64.png")
			)
		);
		// @formatter:on
		return bean;
	}

	protected Resource icon(String fileName) {
		return resource("Resources/Icons/" + fileName);
	}

	protected Resource resource(String filePath) {

		Path path = null;
		try {
			path = resources.webInf(filePath).asPath();
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve " + filePath);
		}

		FileUploadSource source = FileUploadSource.T.create();
		source.setLocalFilePath(path.toString());

		Resource resource = Resource.T.create();
		resource.setResourceSource(source);
		return resource;

	}

}
