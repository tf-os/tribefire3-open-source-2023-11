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
package com.braintribe.model.processing.license.glf;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import com.auxilii.glf.client.exception.LicenseException;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.license.License;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.utils.DateTools;

public class LicenseTools {

	private static Logger logger = Logger.getLogger(LicenseTools.class);

	private static final DateTimeFormatter expiryDateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd").withLocale(Locale.US);

	public static void logLicense(LicenseManager licenseManager) {
		boolean info = logger.isInfoEnabled();
		boolean debug = logger.isDebugEnabled();

		if (info || debug) {
			String licenseInformation = null;
			try {
				License license = licenseManager.getLicense();
				licenseInformation = stringifyLicense(license);
			} catch (Exception e) {
				logger.error("Error while trying to get the current license.", e);
				return;
			}

			if (info)
				logger.info(licenseInformation);
			else
				logger.debug(licenseInformation);
		}
	}

	public static String stringifyLicense(License license) {

		StringBuilder sb = new StringBuilder("License issued to: ");
		if (license == null) {
			sb.append("No valid license found.");
		} else {
			String licensee = license.getLicensee();
			if (licensee != null) {
				sb.append(licensee);
			} else {
				sb.append("unknown");
			}

			String licenseeAccount = license.getLicenseeAccount();
			if (licenseeAccount != null) {
				if (!licenseeAccount.equalsIgnoreCase("default")) {
					sb.append(" (");
					sb.append(licenseeAccount);
					sb.append(")");
				}
			}

			Date expiry = license.getExpiryDate();
			if (expiry != null) {
				sb.append(", Expiry: ");
				sb.append(DateTools.encode(expiry, expiryDateFormat));
			}
		}
		return sb.toString();
	}

	public static Pair<com.auxilii.glf.client.License, String> validateLicense(InputStreamProvider licenseInputStreamProvider, Object license) {
		try {
			LicenseLoader licenseLoader = new LicenseLoader(licenseInputStreamProvider);
			com.auxilii.glf.client.License glfLicense = com.auxilii.glf.client.License.loadLicensePreliminary(GlfLicenseManager.LICENSE_KEY,
					licenseLoader);

			Date expiryDate = glfLicense.getExpiryDate();
			if (expiryDate != null && expiryDate.before(new Date()))
				throw new IllegalArgumentException("License has expired on " + expiryDate);

			if (!glfLicense.isValid()) {
				throw new IllegalStateException("License " + license + " is not valid.");
			}

			return Pair.of(glfLicense, licenseLoader.getMd5());

		} catch (LicenseException e) {
			throw new IllegalStateException("License " + license + " is not valid.", e);
		}

	}

}
