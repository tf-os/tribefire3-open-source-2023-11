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
package com.braintribe.web.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.web.multipart.api.PartReader;
import com.braintribe.web.multipart.api.SequentialFormDataReader;
import com.braintribe.web.multipart.impl.ServletMultiparts;

public class MultipartServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 7830858376903147100L;

	public MultipartServlet() {
		// Auto-generated constructor stub
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		SequentialFormDataReader multipartFormDataStreaming = ServletMultiparts.formDataReader(req).sequential();

		PartReader partStreaming = null;

		resp.setContentType("text/plain; charset=UTF-8");
		PrintWriter writer = resp.getWriter();

		while ((partStreaming = multipartFormDataStreaming.next()) != null) {
			writer.println("-----------------------------------");
			for (String headerName : partStreaming.getHeaderNames()) {
				String value = partStreaming.getHeader(headerName);
				writer.println(headerName + ": " + value);
			}

			writer.println();

			if (partStreaming.isFile()) {
				InputStream in = partStreaming.openStream();

				int res;
				while ((res = in.read()) != -1) {
					byte b = (byte) res;
					writer.write((char) b);
				}

				in.close();
			} else {
				writer.print(partStreaming.getContentAsString());
			}
			writer.println();
		}
	}

}
