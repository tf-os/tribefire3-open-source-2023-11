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
package com.braintribe.template.processing.projection;

import static com.braintribe.template.processing.helper.FileHelper.collectRelativePaths;
import static com.braintribe.template.processing.helper.FileHelper.copyFile;
import static com.braintribe.template.processing.helper.FileHelper.deleteFile;
import static com.braintribe.template.processing.helper.FileHelper.ensureDirExists;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.config.api.ModeledConfiguration;
import com.braintribe.template.processing.ArtifactTemplateConsts;
import com.braintribe.template.processing.api.ArtifactTemplateProjector;
import com.braintribe.template.processing.helper.FileHelper;
import com.braintribe.template.processing.projection.support.StaticHandler;
import com.braintribe.template.processing.projection.support.TemplateHandler;
import com.braintribe.template.processing.projection.support.TemplateSupport;
import com.braintribe.utils.FileTools;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

/**
 * 
 * Using FreeMarker, projects an artifact template to the installation directory. <br>
 * <br>
 * 
 * It implements different functionalities of the artifact templates, like projected file relocation, ignoring template, creating directory etc. <br>
 *
 */
public class ArtifactTemplateFreeMarkerProjector implements ArtifactTemplateProjector, ArtifactTemplateConsts {

	private final Version freeMarkerVersion;
	private ModeledConfiguration modelConfiguration;

	public ArtifactTemplateFreeMarkerProjector(Version freeMarkerVersion,  ModeledConfiguration modelConfiguration) {
		this.freeMarkerVersion = freeMarkerVersion;
		this.modelConfiguration = modelConfiguration;
	}

	@Override
	public void project(ArtifactTemplateRequest request, Path templateDir, Path installationDir) {
		try {
			projectStaticDir(templateDir, installationDir);
			projectDynamicDir(request, templateDir, installationDir);
		} catch (Exception e) {
			throw Exceptions.unchecked(e, e.getMessage());
		}
	}

	private void projectStaticDir(Path templateDir, Path installationDir) throws Exception {
		Path staticDir = templateDir.resolve(STATIC_DIR_FULL);
		if (!Files.exists(staticDir)) {
			return;
		}

		List<Path> staticFiles = collectRelativePaths(staticDir);
		for (Path staticFile : staticFiles) {
			Path source = staticDir.resolve(staticFile);
			Path target = installationDir.resolve(staticFile);
			copyFile(source, target);
		}
	}

	private void projectDynamicDir(ArtifactTemplateRequest request, Path templateDir, Path installationDir) throws Exception {
		Path dynamicDir = templateDir.resolve(DYNAMIC_DIR_FULL);
		if (!Files.exists(dynamicDir)) {
			return;
		}

		Map<String, Object> dataModel = asMap("request", request, "support", new TemplateSupport(modelConfiguration));

		Configuration freeMarkerConfig = new Configuration(freeMarkerVersion);
		freeMarkerConfig.setDirectoryForTemplateLoading(dynamicDir.toFile());

		processStaticTemplate(templateDir, installationDir, dataModel, freeMarkerConfig);
		processProjectedTemplates(templateDir, installationDir, dataModel, freeMarkerConfig);
	}

	private void processStaticTemplate(Path templateDir, Path installationDir, Map<String, Object> dataModel, Configuration freeMarkerConfig)
			throws Exception {
		Path staticHandlerFile = templateDir.resolve(STATIC_TEMPLATE_FULL);
		if (!Files.exists(staticHandlerFile)) {
			return;
		}

		StaticHandler staticHandler = new StaticHandler();
		dataModel.put("static", staticHandler);
		projectFreeMarkerTemplate(staticHandlerFile.getFileName().toString(), dataModel, new NullOutputStream(), freeMarkerConfig);
		dataModel.remove("static");

		for (String dirToCreate : staticHandler.getDirsToCreate()) {
			ensureDirExists(installationDir.resolve(dirToCreate));
		}
		for (String fileToIgnore : staticHandler.getIgnoredFiles()) {
			deleteFile(installationDir.resolve(fileToIgnore));
		}
		for (Entry<String, String> fileToRelocate : staticHandler.getFileRelocations().entrySet()) {
			Path source = installationDir.resolve(fileToRelocate.getKey());
			Path target = installationDir.resolve(fileToRelocate.getValue());
			copyFile(source, target);
			if (!Files.isSameFile(source, target)) {
				deleteFile(source);
			}
		}
	}

	private void processProjectedTemplates(Path templateDir, Path installationDir, Map<String, Object> dataModel, Configuration freeMarkerConfig)
			throws Exception, FileNotFoundException, IOException {
		Path projectedDir = templateDir.resolve(PROJECTED_DIR_FULL);
		if (!Files.exists(projectedDir))
			return;

		List<Path> projectedTemplates = collectRelativePaths(projectedDir);
		for (Path projectedTemplate : projectedTemplates) {
			Path tempProjection = FileHelper.createTempFile("projection", "tmp").toPath();
			Path projectedTemplateWithoutExt = removeFtlExtension(projectedTemplate);

			TemplateHandler templateHandler = new TemplateHandler();
			dataModel.put("template", templateHandler);
			try {
				projectFreeMarkerTemplate(projectedDir.getFileName().toString() + "/" + projectedTemplate, dataModel,
						new FileOutputStream(tempProjection.toFile()), freeMarkerConfig);

			} catch (TemplateModelException e) {
				if (e.getCause() instanceof StopTemplateProjectionException)
					continue; // this means skipping the subsequent copying
				else
					throw e;

			} finally {
				dataModel.remove("template");
			}

			Path projection = resolveProjection(installationDir, projectedTemplateWithoutExt, templateHandler);
			copyFile(tempProjection, projection);
			deleteFile(tempProjection);
		}
	}

	private Path removeFtlExtension(Path projectedTemplate) {
		if (projectedTemplate.toString().endsWith(DOT_FTL))
			return Paths.get(FileTools.getNameWithoutExtension(projectedTemplate.toString()));
		else
			return Paths.get(projectedTemplate.toString());
	}

	private Path resolveProjection(Path installationDir, Path projectedTemplateWithoutExt, TemplateHandler templateHandler) {
		if (templateHandler.getRelocationTarget() != null)
			return installationDir.resolve(templateHandler.getRelocationTarget());
		else
			return installationDir.resolve(projectedTemplateWithoutExt);
	}

	private void projectFreeMarkerTemplate(String templateName, Map<String, Object> dataModel, OutputStream output, Configuration freeMarkerConfig)
			throws Exception {

		Template fileTemplate = freeMarkerConfig.getTemplate(templateName);
		try (Writer outputWriter = new OutputStreamWriter(output)) {
			fileTemplate.process(dataModel, outputWriter);
		}
	}

	private class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			// write nowhere
		}
	}

}
