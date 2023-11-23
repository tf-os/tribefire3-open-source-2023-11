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
package com.braintribe.doc.pages;

import java.util.List;
import java.util.Map;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class MapWithNonStringKeys implements TemplateMethodModelEx {

	private final Map<?, ?> map;

	public MapWithNonStringKeys(Map<?, ?> map) {
		this.map = map;
	}

	@Override
	public TemplateModel exec(List args) throws TemplateModelException {
		if (args.size() != 1) {
			throw new TemplateModelException("Wrong arguments");
		}

		BeanModel argument = (BeanModel) args.get(0);
		Object originalObject = argument.getWrappedObject();

		DefaultObjectWrapperBuilder builder = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28);
		builder.setExposeFields(true);
		builder.setExposureLevel(BeansWrapper.EXPOSE_ALL);

		Object valueObject = map.get(originalObject);
		BeanModel beanModel = new BeanModel(valueObject, builder.build());
		return beanModel;
	}
}
