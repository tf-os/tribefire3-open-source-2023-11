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
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

public class SwaggerLab {
	public static void main(String[] args) {
		SwaggerParser parser = new SwaggerParser();
		Swagger swagger = parser.read("https://raw.githubusercontent.com/MicrosoftDocs/vsts-rest-api-specs/master/specification/serviceEndpoint/azure-devops-server-6.0/serviceEndpoint-onprem.json");
		System.out.println(swagger);
	}
}
