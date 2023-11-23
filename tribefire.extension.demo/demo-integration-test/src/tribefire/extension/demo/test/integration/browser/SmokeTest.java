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
package tribefire.extension.demo.test.integration.browser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.model.processing.webrpc.client.ErroneusMultipartDataCapture;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.playwright.PlaywrightTest;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.WaitForNavigationOptions;
import com.microsoft.playwright.Playwright;

import tribefire.cortex.test.TribefireTest;

/**
 * A simple, browser-based test for UI components. The test uses <i>Playwright</i> to automate the browser and it
 * (smoke-)tests the services login, landing page and explorer (with cortex and demo access).
 *
 * @author michael.lafite
 */
public class SmokeTest extends AbstractTest implements PlaywrightTest, TribefireTest {

	private final String servicesUrl = tribefireServicesURL();
	private final String user = tribefireDefaultUser();
	private final String password = tribefireDefaultPassword();

	private final int defaultNavigationTimeout = 10000;

	private Playwright playwright;
	private Browser browser;
	private BrowserContext browserContext;

	@Before
	public void init() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless()).setSlowMo(300));
		browserContext = browser.newContext(
				new NewContextOptions().setViewportSize(1920, 1080).setRecordVideoSize(1920, 1080).setRecordVideoDir(Paths.get(testReportDir())));
	}

	@After
	public void close() {
		browserContext.close();
		browser.close();
		playwright.close();
	}

	@Test
	public void testLogin() {
		logger.info("Starting login test ...");
		login(true); // true: with false login attempt
		logger.info("Finished test.");
	}

	private Page login() {
		return login(false); // without false login attempt
	}

	private Page login(boolean testWrongPassword) {
		Page page = browserContext.newPage();

		// navigate to services page and wait for redirection to services/home
		page.waitForNavigation(new WaitForNavigationOptions().setUrl("**/home").setTimeout(defaultNavigationTimeout), () -> {
			page.navigate(servicesUrl);
		});

		assertThat(page.title()).isEqualTo("tribefire Services");

		assertThat(page.innerText("div[id=tf_caption]")).contains("Welcome to Tribefire.", "Smart Data Platform");
		assertThat(page.innerText("a[href=login]")).isEqualTo("Sign in");

		// click on 'Sign in' and wait for navigation to login page
		page.waitForNavigation(new WaitForNavigationOptions().setUrl("**/login").setTimeout(defaultNavigationTimeout), () -> {
			page.click("a[href=login]");
		});

		assertThat(page.innerText("body")).contains("Sign in to tribefire");

		if (testWrongPassword) {
			logger.info("Intentionally testing with wrong credentials ...");
			page.fill("input[id=input_user]", "WRONG USER");
			page.fill("input[id=input_password]", "WRONG PASSWORD");

			page.click("button[type=submit]");

			assertThat(page.innerText("body").contains("Invalid Credentials"));

			logger.info("Login failed - as expected.");

			screenshot(page, "login-invalid-credentials");
		}

		page.fill("input[id=input_user]", user);
		page.fill("input[id=input_password]", password);

		// navigate to services page and wait for redirection to services/home
		page.waitForNavigation(new WaitForNavigationOptions().setUrl("**/home").setTimeout(defaultNavigationTimeout), () -> {
			page.click("button[type=submit]");
		});

		return page;
	}

	@Test
	public void testLandingPage() {
		logger.info("Starting landing page test ...");

		Page page = login();

		assertThat(page.innerText("body")).contains("OVERVIEW", "Administration", "User Sessions");

		screenshot(page, "landing-page");

		page.click("//a[text() = 'About']");
		page.waitForLoadState();
		assertThat(page).isNotNull();

		logger.info("landing page about url: " + page.url());
		assertThat(page.locator("div.tf_tabmenuelement-selected").first().innerText()).isEqualTo("ABOUT");

		FrameLocator theframe = page.frameLocator("#tf_content");
		assertThat(theframe).isNotNull();

		Locator frame = theframe.locator("table:has(div.aboutSectionWrapper)");
		assertThat(frame).isNotNull();

		Locator envInfo = frame.locator("div.aboutSectionTitle").first();
		assertThat(envInfo.innerText()).isEqualTo("ENVIRONMENT INFO");

		String allInnerTexts = frame.locator("td[id=main]").allInnerTexts().toString();
		assertThat(allInnerTexts).contains("Maximum Frequency", "CPU Load", "Java Home");

		logger.info("Finished test.");
	}

	@Test
	public void testCortexAccess() {
		logger.info("Starting 'cortex' access test ...");

		AttributeContext context = AttributeContexts.peek().derive().set(ErroneusMultipartDataCapture.class, ip -> {
			String filename = "capture/mp-capture-testCortexAccess-" + UUID.randomUUID().toString() + ".txt";
			System.out.println("ErroneusMultipartDataCapture to: " + filename);
			logger.error("ErroneusMultipartDataCapture to: " + filename);
			File file = new File(filename);
			file.getParentFile().mkdirs();
			try (OutputStream out = new FileOutputStream(file); InputStream in = ip.openInputStream()) {
				IOTools.transferBytes(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).build();

		AttributeContexts.push(context);

		try {

			Page servicePage = login();
			Locator adminLink = servicePage.locator("a[target='tfControlCenter-cortex']");
			Assertions.assertThat(adminLink.innerText()).isEqualTo("Administration");
			adminLink.click();

			// new Tab...
			Page pageExplorer = browserContext.pages().get(1);
			pageExplorer.waitForLoadState();
			assertThat(pageExplorer.title()).isEqualTo("tribefire Explorer");

			pageExplorer.waitForFunction("document.querySelector('body').innerText.includes('Entity Types')");
			pageExplorer.waitForLoadState();
			assertThat(pageExplorer.innerText("body")).contains("Base Models", "Workers", "Cortex Configuration");

			pageExplorer.click("//span[text() = 'System Accesses']");
			pageExplorer.waitForLoadState();
			pageExplorer.waitForFunction("document.querySelector('div[id=gmAssemblyPanel0]').innerText.includes('Cortex')");

			screenshot(pageExplorer, "cortex-system-accesses");

			assertThat(pageExplorer.innerText("div[id=gmAssemblyPanel0]")).contains("Authentication and Authorization", "Cortex Workbench",
					"Platform Setup (setup)");

		} finally {
			AttributeContexts.pop();
		}
		logger.info("Finished test.");
	}

	@Test
	public void testDemoAccess() {
		logger.info("Starting 'demo' access test ...");

		Page servicePage = login();
		Locator adminLink = servicePage.locator("a[target='tfExplorer-access.demo']");
		Assertions.assertThat(adminLink.innerText()).isEqualTo("Explore");
		adminLink.click();

		// new Tab...
		Page pageExplorer = browserContext.pages().get(1);
		pageExplorer.waitForLoadState();
		assertThat(pageExplorer.title()).isEqualTo("tribefire Explorer");

		pageExplorer.waitForFunction("document.querySelector('body').innerText.includes('Persons')");
		pageExplorer.waitForLoadState();
		assertThat(pageExplorer.innerText("body")).contains("Companies");

		pageExplorer.click("//span[text() = 'Persons']");
		pageExplorer.waitForLoadState();
		pageExplorer.waitForFunction("document.querySelector('div[id=gmAssemblyPanel0]').innerText.includes('John Doe')");

		screenshot(pageExplorer, "demo-persons");
		pageExplorer.waitForLoadState();
		assertThat(pageExplorer.innerText("div[id=gmAssemblyPanel0]")).contains("Jane Doe", "James Doeman");

		logger.info("Finished test.");
	}
}
