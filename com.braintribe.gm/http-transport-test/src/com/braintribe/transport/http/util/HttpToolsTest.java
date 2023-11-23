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
package com.braintribe.transport.http.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Pair;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.http.DefaultHttpClientProvider;

public class HttpToolsTest {

	@Test
	public void testMimeTypes() {
		
		assertThat(HttpTools.getMimeTypeFromContentType("text/html; charset=utf-8", false)).isEqualTo("text/html");
		assertThat(HttpTools.getMimeTypeFromContentType("text/html", false)).isEqualTo("text/html");
		assertThat(HttpTools.getMimeTypeFromContentType("", false)).isNull();
		assertThat(HttpTools.getMimeTypeFromContentType(null, false)).isNull();
		
	}
	
	@Test
	public void testContentDispositionParsing() {
		
		assertThat(HttpTools.getFilenameFromContentDisposition("form-data; name=\"fieldName\"; filename=\"filename.jpg\"")).isEqualTo("filename.jpg");
		assertThat(HttpTools.getFilenameFromContentDisposition("form-data; name=\"fieldName\"; filename*=UTF-8''filename.jpg")).isEqualTo("filename.jpg");
		assertThat(HttpTools.getFilenameFromContentDisposition("form-data; name=\"fieldName\"")).isNull();
		assertThat(HttpTools.getFilenameFromContentDisposition("form-data")).isNull();
		assertThat(HttpTools.getFilenameFromContentDisposition("")).isNull();
		assertThat(HttpTools.getFilenameFromContentDisposition(null)).isNull();
		
	}
	
	@Test
	@Category(SpecialEnvironment.class)
	public void testDownload() throws Exception {
		
		DefaultHttpClientProvider provider = new DefaultHttpClientProvider();
		CloseableHttpClient httpClient = provider.provideHttpClient();
		Pair<File, String> pair = HttpTools.downloadFromUrl("https://12r9gfubiw93zb4r366chx69-wpengine.netdna-ssl.com/wp-content/uploads/2018/02/MainBannerVision2.png", httpClient);
		System.out.println("Downloaded: "+pair.first().getAbsolutePath()+" with name "+pair.second());
		pair.first().delete();
	}
}
