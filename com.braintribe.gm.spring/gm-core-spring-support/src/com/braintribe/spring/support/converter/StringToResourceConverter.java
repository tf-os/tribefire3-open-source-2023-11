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
package com.braintribe.spring.support.converter;

import java.io.File;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.convert.converter.Converter;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;

/**
 * A converter that converts a local File into a {@link Resource} with a 
 * LocalFileSource containing the path of the local file.
 *
 */
public class StringToResourceConverter implements Converter<String, Resource>{

	private Converter<String, File> stringToFileConverter;
	
	@Required
	public void setStringToFileConverter(Converter<String, File> stringToFileConverter) {
		this.stringToFileConverter = stringToFileConverter;
	}
	

	@Override
	public Resource convert(String localFilePath) {
		File localFile = stringToFileConverter.convert(localFilePath);
		return createResource(localFile);
	}
	
	public static Resource createResource(File localFile) {
		Resource resource = Resource.T.create();
		FileUploadSource source = FileUploadSource.T.create();
		source.setLocalFilePath(localFile.getAbsolutePath());
		resource.setResourceSource(source);
		return resource;
	}
	
	
	
}
