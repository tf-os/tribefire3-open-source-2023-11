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
package com.braintribe.tribefire.cartridge.library.load;

import java.io.File;

public class LicenseResource {
	public File originalFile;
	public File pdfFile;
	public String name;
	public String url;
	public String internalUrl;
	public boolean commercial;
	public String spdxId;
	public boolean isSpdxListed;

	public LicenseResource(File originalFile, File pdfFile, String name, String spdxId, boolean isSpdxListed, String url, String internalUrl,
			boolean commercial) {
		super();
		this.originalFile = originalFile;
		this.pdfFile = pdfFile;
		this.name = name;
		this.spdxId = spdxId;
		this.isSpdxListed = isSpdxListed;
		this.url = url;
		this.internalUrl = internalUrl;
		this.commercial = commercial;

		if (!originalFile.exists()) {
			throw new RuntimeException("Original: " + originalFile.getAbsolutePath() + " does not exist.");
		}
		if (!pdfFile.exists()) {
			throw new RuntimeException("PDF: " + pdfFile.getAbsolutePath() + " does not exist.");
		}
	}

}
