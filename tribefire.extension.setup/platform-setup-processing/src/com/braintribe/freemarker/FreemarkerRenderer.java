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
package com.braintribe.freemarker;

import java.io.IOException;
import java.io.Writer;

import com.braintribe.exception.Exceptions;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreemarkerRenderer {

	private final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_28);

	public FreemarkerRenderer(TemplateLoader templateLoader) {
		// Got these settings from the tutorial:
		// https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html
		freemarkerConfig.setTemplateLoader(templateLoader);
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfig.setLogTemplateExceptions(false);
		freemarkerConfig.setWrapUncheckedExceptions(true);
	}

	public static FreemarkerRenderer loadingViaClassLoader(Class<?> clazz) {
		return loadingViaClassLoader(clazz, "");
	}

	public static FreemarkerRenderer loadingViaClassLoader(Class<?> clazz, String basePackagePath) {
		return new FreemarkerRenderer(new ClassTemplateLoader(clazz, basePackagePath));
	}

	public void renderTemplate(String templateName, Object dataModel, Writer writer) {
		try{
			Template temp = freemarkerConfig.getTemplate(templateName);

			temp.process(dataModel, writer);

		} catch (TemplateException | IOException e) {
			throw Exceptions.unchecked(e, "Could not write file using freemarker template");
		}
	}

}
