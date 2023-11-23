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
package com.braintribe.utils.system.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Base64;

import com.braintribe.logging.Logger;

public class SystemInformationLogWriter extends AbstractSystemInformationLog {

	private static Logger logger = Logger.getLogger(SystemInformationLogWriter.class);

	@Override
	protected void logInformation(String info) {
		try {
			byte[] infoBytes = info.getBytes("UTF-8");
			for (int i=0; i<infoBytes.length; ++i) {
				infoBytes[i] = infoBytes[i] ^= 0x42; 
			}
			String output = Base64.getEncoder().encodeToString(infoBytes);
			if (output != null) {
				logger.log(this.logLevel, "=======START");
				while (output.length() > 80) {
					logger.log(this.logLevel, output.substring(0, 80));
					output = output.substring(80);
				}
				if (output.length() > 0) {
					logger.log(this.logLevel, output);
				}
				logger.log(this.logLevel, "=======END");
			}
		} catch(Exception e) {
			logger.info("Could not compile system information", e);
		}
	}

	public static void main(String[] args) throws Exception {
		if ((args == null) || (args.length == 0)) {
			System.out.println("Please provide the file path.");
			System.exit(1);
		}
		File inputFile = new File(args[0]);
		System.out.println("Reading from file "+inputFile.getAbsolutePath());
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));

			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					sb.append(line);
				}
			}
			String input = sb.toString();
			byte[] infoBytes = Base64.getDecoder().decode(input);
			for (int i=0; i<infoBytes.length; ++i) {
				infoBytes[i] = infoBytes[i] ^= 0x42; 
			}
			String info = new String(infoBytes, "UTF-8");
			System.out.println(info);

		} finally {
			if (br != null) {
				try {
					br.close();
				} catch(Exception e) {
					//ignore
				}
			}
		}
	}

}
