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
package com.braintribe.artifact.processing.core.test.writer;

import java.util.List;

import com.braintribe.model.artifact.info.RepositoryOrigin;

public class WriterCommons {

	protected String tab( int index) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < index; i++) {
			buffer.append( '\t');
		}
		return buffer.toString();
	}
	
	protected String dump( List<RepositoryOrigin> origins) {
		StringBuffer buffer = new StringBuffer();
		origins.stream().forEach( o -> {
			if (buffer.length() > 0) {
				buffer.append(",");
			}
			buffer.append( dump( o));
		});
		return "(" + buffer.toString() + ")";
	}
	
	protected String dump( RepositoryOrigin origin) {
		return origin.getName() + "@" + origin.getUrl();
	}
}
