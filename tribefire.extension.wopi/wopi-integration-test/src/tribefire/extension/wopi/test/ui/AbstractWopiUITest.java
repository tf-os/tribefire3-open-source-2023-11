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
package tribefire.extension.wopi.test.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.tfconstants.TribefireComponent;
import com.braintribe.model.processing.tfconstants.TribefireUrlBuilder;
import com.braintribe.model.wopi.DocumentMode;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.service.integration.OpenWopiSessionResult;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.RandomTools;

import tribefire.extension.wopi.test.AbstractWopiTest;

/**
 *
 */
public class AbstractWopiUITest extends AbstractWopiTest {

	protected static final boolean headless = true;

	protected static final File geckoBrowserMac = new File("/Users/ggr/apps/Firefox.app/Contents/MacOS/firefox-bin");
	protected static final File geckoDriverMac = new File("./res/driver/geckodriver_mac");

	protected static final String NOT_NECESSARY = "";
	protected static final String DOC_BUTTON_VIEW = "btnOpenSearchPane-Medium20";// 'find' button
	protected static final String DOCX_BUTTON_VIEW = "btnOpenSearchPane-Medium20"; // 'find' button
	protected static final String DOCX_BUTTON_EDIT = "jewelWordEditor-Default"; // 'file' button
	protected static final String XLS_BUTTON_VIEW = "m_excelWebRenderer_ewaCtl_btnFind-Medium20"; // 'find' button
	protected static final String XLSX_BUTTON_VIEW = "m_excelWebRenderer_ewaCtl_btnFind-Medium20";// 'find' button
	protected static final String XLSX_BUTTON_EDIT = "m_excelWebRenderer_ewaCtl_Jewel-Default";// 'file' button
	protected static final String PPT_BUTTON_VIEW = "PptUpperToolbar.LeftButtonDock.ShowHideComments-Medium20"; // 'comments'
																												// button
	protected static final String PPTX_BUTTON_VIEW = "PptUpperToolbar.LeftButtonDock.ShowHideComments-Medium20"; // 'comments'
																													// button
	protected static final String PPTX_BUTTON_EDIT = "PptJewel-Default"; // 'file' button

	protected String url;

	protected List<WebDriver> drivers;

	protected ExecutorService executorService;

	// -----------------------------------------------------------------------
	// SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	@Override
	public void before() throws Exception {
		super.before();

		drivers = new ArrayList<>();

		url = getURL();

		executorService = new ThreadPoolExecutor(100, 100, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	@After
	@Override
	public void after() throws Exception {
		super.after();
		for (WebDriver driver : drivers) {
			driver.close();
		}
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	private WebDriver getDriver() {
		return getWebDriver(() -> {
			FirefoxOptions options = new FirefoxOptions();
			options.setHeadless(headless);
			WebDriver driver = new FirefoxDriver(options);
			return driver;
		});
	}

	protected WebDriver getDriverAutoClose() {
		WebDriver driver = getDriver();
		drivers.add(driver);
		return driver;
	}

	protected void runParallel(List<Runnable> runnables) {
		runnables.stream().forEach(runnable -> {
			executorService.execute(runnable);
		});

		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw Exceptions.unchecked(e, "Could not wait for termination of parallel execution");
		}
	}

	protected WebDriver getWebDriver(Supplier<WebDriver> webDriverSupplier) {

		System.setProperty("webdriver.firefox.bin", geckoBrowserMac.getAbsolutePath());
		System.setProperty("webdriver.gecko.driver", geckoDriverMac.getAbsolutePath());

		WebDriver driver = webDriverSupplier.get();
		return driver;
	}

	protected void openWopiDocument(File file, DocumentMode documentMode, String button) {
		openWopiDocument(file, documentMode, button, getDriverAutoClose());
	}

	protected void openWopiDocument(File file, DocumentMode documentMode, String button, WebDriver driver) {
		OpenWopiSessionResult wopiSessionResult = openWopiSession(RandomTools.getRandom32CharactersHexString(true), file, documentMode,
				this.ALLOWED_ROLES_ALL, false);

		assertThat(wopiSessionResult.getFailure()).isNull();
		assertThat(wopiSessionResult.getWopiSession()).isNotNull();

		String prepareUrl = prepareUrl(wopiSessionResult.getWopiSession());

		// HTTP GET
		driver.get(prepareUrl);

		int maxWaitInSeconds = 400;
		int waitInterval = 5;

		WebElement fileElement = null;
		boolean found = false;
		while (!(found || maxWaitInSeconds < 0)) {
			try {
				maxWaitInSeconds = maxWaitInSeconds - waitInterval;
				CommonTools.sleep(waitInterval * 1000);

				fileElement = driver.findElement(By.id(button));

				found = true;

			} catch (NoSuchElementException e) {
				// ignore
			}
		}

		assertThat(fileElement).overridingErrorMessage("Could not click on the file button").isNotNull();
		fileElement.click();

		logger.info(() -> "Successfully clicked in Office365");
	}

	protected String prepareUrl(WopiSession wopiSession) {
		String sessionId = session.getSessionAuthorization().getSessionId();

		String wopiUrl = wopiSession.getWopiUrl();
		return url + wopiUrl + "&sessionId=" + sessionId;
	}

	protected String getURL() {
		return getConfigProperty(url, "QA_FORCE_URL", new TribefireUrlBuilder().http().buildFor(TribefireComponent.Services));
	}

	private static String getConfigProperty(String propertyValue, String propertyName, String defaultPropertyName) {
		String systemPropertyName = propertyName.toLowerCase().replaceAll("_", ".");

		if (!CommonTools.isEmpty(propertyValue)) {
			return propertyValue;
		} else if (System.getProperty(systemPropertyName) != null) {
			return System.getProperty(systemPropertyName);
		} else if (System.getenv(propertyName) != null) {
			return System.getenv(propertyName);
		} else {
			return defaultPropertyName;
		}
	}

}
