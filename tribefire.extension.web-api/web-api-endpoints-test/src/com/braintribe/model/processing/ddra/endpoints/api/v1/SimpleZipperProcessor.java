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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequestSimple;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipTask;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipe;
import com.braintribe.utils.stream.api.StreamPipes;

public class SimpleZipperProcessor implements ServiceProcessor<ZipRequestSimple, Resource> {

	@Override
	public Resource process(ServiceRequestContext requestContext, ZipRequestSimple request) {
		try {

			Resource zipOutResource = null;

			StreamPipe pipe = StreamPipes.simpleFactory().newPipe("demo-zip-" + request.getName());

			zipOutResource = Resource.createTransient(pipe::openInputStream);
			zipOutResource.setName(request.getName() + ".zip");
			zipOutResource.setMimeType("application/zip");

			MessageDigest md5Instance = MessageDigest.getInstance("MD5");
			try (OutputStream out = pipe.openOutputStream();
					InputStream inputResourceStream = request.getResource().openStream();
					ZipOutputStream zipOutputStream = new ZipOutputStream(new DigestOutputStream(out, md5Instance))) {

				zipOutputStream.putNextEntry(new ZipEntry(request.getName()));
				IOTools.transferBytes(inputResourceStream, zipOutputStream);
			}
			return zipOutResource;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "I don't care what went wrong");
		}

	}

	private List<InputStream> getInput(ZipTask zipTask) {
		if (zipTask.getInputResources() != null)
			return zipTask.getInputResources() //
					.stream() //
					.map(res -> res.openStream()) //
					.collect(Collectors.toList());

		URL resource = getClass().getResource("zip-default-resource.txt.vm");

		try {
			return CollectionTools.getList(resource.openStream());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
