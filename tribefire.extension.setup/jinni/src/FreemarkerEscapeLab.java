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
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

public class FreemarkerEscapeLab {
	
	private static final Version FREEMARKER_VERSION = Configuration.VERSION_2_3_28;
	
	public static void main(String args[]) {
		try {
			Map<String, Object> dataModel = asMap();

			Configuration freeMarkerConfig = new Configuration(FREEMARKER_VERSION);

			String literals[] = {
					"$\\{foobar}",
					"\\\"",
					"<#assign a=\"\\{\">${a}",
			};
			
			for (String s: literals) {
			
				System.out.print(s + " -> ");
				
				String result = null;
				try (StringReader in = new StringReader(s); Writer out = new StringWriter()) {
					Template template = new Template("", in, freeMarkerConfig);
					template.process(dataModel, out);
					result = out.toString();
				}
			
				System.out.println(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	private String processStringWithFreeMarker(Configuration freeMarkerConfig, String templatedString,
			Map<String, Object> dataModel) throws Exception {
		String result = null;
		try (StringReader in = new StringReader(templatedString); Writer out = new StringWriter()) {
			Template template = new Template("", in, freeMarkerConfig);
			template.process(dataModel, out);
			result = out.toString();
		}
		return result;
	}
}
