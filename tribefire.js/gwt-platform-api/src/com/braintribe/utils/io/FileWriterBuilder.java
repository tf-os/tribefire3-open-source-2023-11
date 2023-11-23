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
package com.braintribe.utils.io;

import java.io.File;

/**
 * {@link WriterBuilder} extension that writes data to a {@link File}.
 * 
 * @author peter.gazdik
 */
public interface FileWriterBuilder extends WriterBuilder<File> {

	/** If set to {@code true}, the writer will append the data to the end of the file if the file already exists. */
	FileWriterBuilder append(boolean append);

}
