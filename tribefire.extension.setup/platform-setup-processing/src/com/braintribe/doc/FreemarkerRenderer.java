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
package com.braintribe.doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.braintribe.exception.Exceptions;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerRenderer {
	private final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_28);

	public FreemarkerRenderer(File templateFolder) {
		ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(DocUtils.class, "");

		if (templateFolder.isDirectory()) {
			FileTemplateLoader fileTemplateLoader;

			try {
				fileTemplateLoader = new FileTemplateLoader(templateFolder);
			} catch (IOException e) {
				throw Exceptions.unchecked(e, "Could not set Freemarker template folder");
			}

			MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(new TemplateLoader[] { fileTemplateLoader, classTemplateLoader });
			freemarkerConfig.setTemplateLoader(multiTemplateLoader);
		} else {
			freemarkerConfig.setTemplateLoader(classTemplateLoader);
		}
		// Got these settings from the tutorial:
		// https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfig.setLogTemplateExceptions(false);
		freemarkerConfig.setWrapUncheckedExceptions(true);
	}

	public void writeFileFromTemplate(String templateName, Object dataModel, File target) {
		try (FileOutputStream fos = new FileOutputStream(target); Writer writer = new OutputStreamWriter(fos, "UTF-8")) {
			Template temp = freemarkerConfig.getTemplate(templateName);

			temp.process(dataModel, writer);
		} catch (TemplateException | IOException e) {
			throw Exceptions.unchecked(e, "Could not write file using freemarker template");
		}
	}
	
}
