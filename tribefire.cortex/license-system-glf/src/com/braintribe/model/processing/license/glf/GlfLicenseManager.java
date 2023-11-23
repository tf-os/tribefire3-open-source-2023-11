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

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.zip.Adler32;

import com.auxilii.glf.client.License;
import com.auxilii.glf.client.LicenseLoader;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.model.processing.license.LicenseManagerRegistry;
import com.braintribe.model.processing.license.exception.InvalidLicenseManagerConfigurationException;
import com.braintribe.model.processing.license.exception.LicenseViolatedException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.utils.DateTools;

public class GlfLicenseManager implements LicenseManager {

	protected static Logger logger = Logger.getLogger(GlfLicenseManager.class);

	/** the license checksum */
	private final static long LICENSE_CHECKSUM = 1377101523L;

	/** the license key */
	public final static String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq/k9zC0K+/55FfoNgEpTwm0CJghzEfezbSB6trRGEYUObktaK7dBs6eXneEW4j2yrxB7IG7Rq0yoqaqG9kXRXky1ID+Kdq35ULxEKrs5Qa4mdpdJpK8VlXIyOHbNKEV0VysOOsNFoP4hDSrEEQeVty4uOsVAc2XL2qKwTqOr3D1G/x0xILzSGhxXiptHuDQHp6kc5ZjRaTg0Ad9yjHBSpAHefa3nOuiM/y++l9tElzUIU6Lkz/fe5lg9T0YNb4juNyzuZg4T8WtcPoWLrBmctKZrpOGa/krqaYlbvebYY9jJuJaMAvpibVgtb7DHMC4aTVNYPLOS9BJhaw1THATSBQIDAQAB";

	protected LicenseLoader licenseLoader = null;
	protected Supplier<PersistenceGmSession> sessionProvider = null;

	protected volatile long nextLicenseRefresh = -1L;
	protected long refreshEveryMs = Numbers.MILLISECONDS_PER_DAY;
	protected long refreshEveryMsInError = Numbers.MILLISECONDS_PER_SECOND * 10;

	protected volatile boolean licenseIsValid = false;
	protected volatile String licenseViolationReason = null;
	protected volatile Throwable licenseViolationException = null;

	protected ReentrantLock lock = new ReentrantLock();

	@Override
	public void checkLicense() throws LicenseViolatedException {

		this.registerLicenseManager();

		long now = System.currentTimeMillis();

		if (now < this.nextLicenseRefresh) {
			if (this.licenseIsValid)
				return;

			throw new LicenseViolatedException(this.licenseViolationReason, this.licenseViolationException);
		}

		lock.lock();
		try {

			if (now < this.nextLicenseRefresh) {
				if (this.licenseIsValid)
					return;

				throw new LicenseViolatedException(this.licenseViolationReason);
			}

			checkLicenseInternally();
			this.licenseIsValid = true;
			this.licenseViolationReason = "unknown";
			this.licenseViolationException = null;
			this.nextLicenseRefresh = now + this.refreshEveryMs;

		} catch (Exception e) {
			this.licenseIsValid = false;
			this.licenseViolationReason = e.getMessage();
			this.licenseViolationException = e;

			if (logger.isDebugEnabled()) {
				logger.debug("The license could not be validated.", e);
			}

			this.nextLicenseRefresh = now + this.refreshEveryMsInError;

			throw new LicenseViolatedException("The license could not be validated", e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void reloadLicense() {
		this.nextLicenseRefresh = -1;
	}

	protected void registerLicenseManager() throws LicenseViolatedException {
		LicenseManager registeredLicenseManager = LicenseManagerRegistry.getRegisteredLicenseManager();
		if (registeredLicenseManager == null) {
			LicenseManagerRegistry.setRegisteredLicenseManager(this);
		} else {
			if (!this.equals(registeredLicenseManager))
				throw new InvalidLicenseManagerConfigurationException(
						"There is more than one LicenseManager in the system: " + registeredLicenseManager + ". This is not allowed.");
		}
	}

	/**
	 * Checks the server license using {@link com.auxilii.glf.client.License License}. The License.class file is read in
	 * and its checksum is compared with the value in {@link #LICENSE_CHECKSUM}
	 * 
	 * @throws Exception
	 *             If the license is not valid
	 */

	private void checkLicenseInternally() throws Exception {

		License license = License.getInstance(LICENSE_KEY, this.licenseLoader);
		if (!license.isValid())
			throw new Exception(String.format("License '%s' is not valid.", license.getLicensee()));

		PersistenceGmSession session = this.sessionProvider.get();
		com.braintribe.model.license.License modelLicense = LicenseLoaderUtil.getLicenseResource(session);
		this.syncLicenseInformation(license, modelLicense, session);

		long licenseChecksum = getAdler32(License.class);
		if (licenseChecksum != LICENSE_CHECKSUM)
			throw new Exception("The License manager is not the one as expected.");

		if (logger.isTraceEnabled()) {

			String expiry = "it is non-expiring";
			if (license.getExpiryDate() != null) {
				expiry = String.format("it expires on %s", DateTools.encode(license.getExpiryDate(), DateTools.ISO8601_DATE_WITH_MS_FORMAT));
			}

			logger.trace(String.format("This product is licensed to '%s' (%s)", license.getLicensee(), expiry));
		}

	}

	@Override
	public com.braintribe.model.license.License getLicense() throws Exception {
		try {
			PersistenceGmSession session = this.sessionProvider.get();
			com.braintribe.model.license.License modelLicense = LicenseLoaderUtil.getLicenseResource(session);

			return modelLicense;
		} catch (Exception e) {
			throw new Exception("Could not get license from session.", e);
		}
	}

	protected void syncLicenseInformation(License license, com.braintribe.model.license.License modelLicense, PersistenceGmSession session) {

		try {
			String systemLicenseIdLicense = license.getLicenseId();
			String systemLicenseIdModel = modelLicense.getSystemLicenseId();
			if (systemLicenseIdLicense != null) {
				if (systemLicenseIdModel == null || !systemLicenseIdModel.equals(systemLicenseIdLicense)) {
					modelLicense.setSystemLicenseId(systemLicenseIdLicense);
				}
			}

			Date expiryDateLicense = license.getExpiryDate();
			Date expiryDateModel = modelLicense.getExpiryDate();
			if (expiryDateLicense != null) {
				if ((expiryDateModel == null) || (!expiryDateModel.equals(expiryDateLicense))) {
					modelLicense.setExpiryDate(expiryDateLicense);
				}
			} else {
				if (expiryDateModel != null) {
					modelLicense.setExpiryDate(null);
				}
			}

			String licenseeLicense = license.getLicensee();
			String licenseeModel = modelLicense.getLicensee();
			if (licenseeLicense != null) {
				if ((licenseeModel == null) || (!licenseeModel.equals(licenseeLicense))) {
					modelLicense.setLicensee(licenseeLicense);
				}
			}

			String licensorLicense = license.getLicensor();
			String licensorModel = modelLicense.getLicensor();
			if (licensorLicense != null) {
				if ((licensorModel == null) || (!licensorModel.equals(licensorLicense))) {
					modelLicense.setLicensor(licensorLicense);
				}
			}

			String licenseeAccountLicense = license.getLicenseeAccount();
			String licenseeAccountModel = modelLicense.getLicenseeAccount();
			if (licenseeAccountLicense != null) {
				if ((licenseeAccountModel == null) || (!licenseeAccountModel.equals(licenseeAccountLicense))) {
					modelLicense.setLicenseeAccount(licenseeAccountLicense);
				}
			}

			Date issueDateLicense = license.getIssueDate();
			Date issueDateModel = modelLicense.getIssueDate();
			if (issueDateLicense != null) {
				if ((issueDateModel == null) || (!issueDateModel.equals(issueDateLicense))) {
					modelLicense.setIssueDate(issueDateLicense);
				}
			}

			session.commit();
		} catch (Exception e) {
			logger.debug("Could not sync the license.", e);
		}
	}

	/**
	 * Computes the Adler-32 checksum of the class file specified in <code>c</code> by using the
	 * {@link java.util.zip.Adler32 Adler32} class from the SDK.
	 * 
	 * @param c
	 *            The class to generate the checksum for
	 * @return The generated checksum or -1 if the generation is not possible
	 */
	public long getAdler32(Class<?> c) {

		if (c == null)
			return -1;

		try {
			String res = c.getName().replace('.', '/') + ".class";
			InputStream is = ClassLoader.getSystemResourceAsStream(res);
			if (is == null) {
				is = this.getClass().getClassLoader().getResourceAsStream(res);
			}

			Adler32 a = new Adler32();
			int d;
			while ((d = is.read()) != -1)
				a.update(d);

			return a.getValue();

		} catch (Exception e) {
			logger.warn("Error while trying to verify the license checksum", e);
			return -1;
		}

	}

	@Required
	public void setLicenseLoader(LicenseLoader licenseLoader) {
		this.licenseLoader = licenseLoader;
	}
	@Required
	public void setSessionProvider(Supplier<PersistenceGmSession> sessionProvider) {
		this.sessionProvider = sessionProvider;
	}
	@Configurable
	public void setRefreshEveryMs(long refreshEveryMs) {
		this.refreshEveryMs = refreshEveryMs;
	}

}
