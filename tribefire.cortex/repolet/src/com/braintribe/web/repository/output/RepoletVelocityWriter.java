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
package com.braintribe.web.repository.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.braintribe.logging.Logger;

/**
 * Velocity based implementation of {@link RepoletWriter}
 * 
 */
public class RepoletVelocityWriter implements RepoletWriter<Writer> {

	private Template listTemplate;
	private Template notFoundTemplate;

	private static final String listTemplateLocation = "com/braintribe/web/repository/output/templates/list.html.vm";
	private static final String notFoundTemplateLocation = "com/braintribe/web/repository/output/templates/404.html.vm";

	private static final Logger log = Logger.getLogger(RepoletVelocityWriter.class);

	public RepoletVelocityWriter() {
		try {
			loadTemplates();
		} catch (Exception e) {
			log.error("failed to create " + this.getClass() + ": " + e.getMessage(), e);
		}
	}

	public void writeList(String path, Collection<BreadCrumb> breadCrumbs, Collection<String> entries, Writer writer, Map<String, Object> attributes)
			throws IOException {
		loadTemplates();
		VelocityContext context = new VelocityContext();
		context.put("path", path);
		context.put("breadCrumbs", breadCrumbs);
		context.put("entries", entries);
		addAttributesToContext(attributes, context);
		writeList(context, writer);
	}

	public void writeNotFound(String path, boolean printInspectedPaths, Collection<String> inspectedPaths, Writer writer,
			Map<String, Object> attributes) throws IOException {
		VelocityContext context = new VelocityContext();
		context.put("path", path);
		context.put("printInspectedPaths", printInspectedPaths);
		context.put("inspectedPaths", inspectedPaths);
		addAttributesToContext(attributes, context);
		writeNotFound(context, writer);
	}

	private void addAttributesToContext(Map<String, Object> attributes, VelocityContext context) {
		for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
			context.put(attribute.getKey(), attribute.getValue());
		}
	}

	private void writeList(Context context, Writer writer) throws IOException {
		templateMerge(listTemplate, context, writer);
	}

	private void writeNotFound(Context context, Writer writer) throws IOException {
		templateMerge(notFoundTemplate, context, writer);
	}

	private void templateMerge(Template template, Context context, Writer writer) throws IOException {
		try {
			template.merge(context, writer);
		} catch (Exception e) {
			throw new IOException("failed to write to template: " + e.getMessage(), e);
		}
	}

	private VelocityEngine createVelocityEngine() {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
		ve.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		return ve;
	}

	private void loadTemplates() throws IOException {
		VelocityEngine ve = createVelocityEngine();
		try {
			ve.init();
		} catch (Exception e) {
			throw new IOException("failed to initialize velocity engine: " + e.getMessage(), e);
		}
		listTemplate = loadTemplate(ve, listTemplateLocation);
		notFoundTemplate = loadTemplate(ve, notFoundTemplateLocation);
	}

	private Template loadTemplate(VelocityEngine ve, String templateLocation) throws IOException {

		Template template = null;
		try {
			template = ve.getTemplate(templateLocation);
		} catch (Exception e) {
			throw new IOException("failed to load template from [ " + templateLocation + " ]: " + e.getMessage(), e);
		}

		if (template == null)
			throw new IOException("null template loaded from [ " + templateLocation + " ] ");

		return template;
	}

}
