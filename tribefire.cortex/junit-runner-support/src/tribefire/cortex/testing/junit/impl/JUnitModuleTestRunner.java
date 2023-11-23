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
package tribefire.cortex.testing.junit.impl;

import static com.braintribe.utils.SysPrint.spOut;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.junit.experimental.categories.Categories.CategoryFilter;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.braintribe.cfg.Configurable;
import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.testing.category.KnownIssue;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.thread.api.EmptyThreadContextScoping;
import com.braintribe.thread.api.ThreadContextScoping;
import com.braintribe.utils.FileTools;

import tribefire.cortex.testing.junit.classpathfinder.ClasspathClassesFinder;
import tribefire.cortex.testing.junit.formatter.JUnitResultFormatterAsRunListener;
import tribefire.cortex.testing.junit.formatter.ThreadLocalRunListener;
import tribefire.cortex.testing.junit.runner.AuthorizingSuite;
import tribefire.cortex.testrunner.api.ModuleTestRunner;
import tribefire.cortex.testrunner.api.RunTests;

/**
 * A {@link ModuleTestRunner} which uses <tt>JUnit</tt> to run the tests and <tt>ant</tt> to create the test result <tt>.xml</tt>s
 *
 * @author Neidhart.Orlich
 */
public class JUnitModuleTestRunner implements ModuleTestRunner {

	private static Logger log = Logger.getLogger(JUnitModuleTestRunner.class);

	private static String[] acceptedClassNames = { "**Test", "**Tests" };

	private Supplier<Class<?>[]> testClassesSupplier = JUnitModuleTestRunner::findTestClasses;

	private long timeoutInMs = 0;
	private Class<?>[] excludedCategories = new Class<?>[] { KnownIssue.class, SpecialEnvironment.class, VerySlow.class };
	private ThreadContextScoping threadContextScoping = EmptyThreadContextScoping.INSTANCE;

	private String moduleDescriptor = "<unknown module>";

	private static Class<?>[] findTestClasses() {
		ClasspathClassesFinder finder = new ClasspathClassesFinder(acceptedClassNames);

		logAndSpOut("Searching for classes in: " + finder.getClasspathRoots());

		return finder.find().stream().toArray(Class<?>[]::new);
	}

	public void setModuleDescriptor(String moduleDescriptor) {
		this.moduleDescriptor = moduleDescriptor;
	}

	@Configurable
	public void setTestClassesSupplier(Supplier<Class<?>[]> testClassesSupplier) {
		this.testClassesSupplier = testClassesSupplier;
	}

	/**
	 * @param timeoutInMs
	 *            maximum time in milliseconds all tests combined may take. If they take longer the test run will be aborted. A value <= 0 disables
	 *            the timeout meaning tests may take indefinitely. Disabled per default.
	 */
	@Configurable
	public void setTimeoutInMs(long timeoutInMs) {
		this.timeoutInMs = timeoutInMs;
	}

	@Configurable
	public void setExcludedCategories(Class<?>[] excludedCategories) {
		this.excludedCategories = excludedCategories;
	}

	@Configurable
	public void setThreadContextScoping(ThreadContextScoping threadContextScoping) {
		this.threadContextScoping = threadContextScoping;
	}

	@Override
	public void runTests(RunTests request, File reportRootDir) {
		new JUnitGump(reportRootDir, request).runForrestRun();
	}

	private class JUnitGump {

		private final File reportRootDir;
		private final RunTests request;
		private final Suite suite;

		public JUnitGump(File reportRootDir, RunTests request) {
			this.reportRootDir = reportRootDir;
			this.request = request;
			this.suite = createClasspathSuite();
		}

		public void runForrestRun() {
			if (suite == null)
				return; // No tests found

			if (timeoutInMs <= 0)
				runSuite();
			else
				runWithTimeout();
		}

		private void runWithTimeout() {
			ExecutorService executor = Executors.newSingleThreadExecutor();

			Runnable runTestSuite = threadContextScoping.bindContext(this::runSuite);
			final Future<?> future = executor.submit(runTestSuite);

			RuntimeException exception = null;

			try {
				future.get(timeoutInMs, TimeUnit.MILLISECONDS);

			} catch (TimeoutException e) {
				future.cancel(true);
				throw exception = Exceptions.unchecked(e, exceptionMsg("finish test exectution due to a timeout after " + timeoutInMs + "ms"));

			} catch (ExecutionException | InterruptedException e) {
				future.cancel(true);
				throw exception = Exceptions.unchecked(e, exceptionMsg("finish test excetion"));

			} finally {
				shutDown(executor, exception);
			}
		}

		private void shutDown(ExecutorService executor, RuntimeException exception) {
			try {
				executor.shutdownNow();
				executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

			} catch (Exception e) {
				RuntimeException shutDownEnchecked = Exceptions.unchecked(e, exceptionMsg("shutdown executor after tests finished."));

				if (exception != null)
					exception.addSuppressed(shutDownEnchecked);
				else
					exception = shutDownEnchecked;

				throw exception;
			}
		}

		private String exceptionMsg(String what) {
			return "Could not " + what + ". Module: " + moduleDescriptor;
		}

		private Suite createClasspathSuite() {
			try {
				return tryCreateClasspathSuite();

			} catch (InitializationError e) {
				throw Exceptions.unchecked(e, "Could not create JUnit test suite.");

			} catch (NoTestsRemainException e) {
				logAndSpOut("No tests passed the configured filters.");
				return null;
			}
		}

		private Suite tryCreateClasspathSuite() throws InitializationError, NoTestsRemainException {
			RunnerBuilder runnerBuilder = new AllDefaultPossibilitiesBuilder(true);
			Class<?>[] classes = testClassesSupplier.get();

			if (classes.length == 0) {
				logAndSpOut("No tests found.");
				return null;
			}

			Suite suite = new AuthorizingSuite(runnerBuilder, classes, threadContextScoping);
			suite.filter(CategoryFilter.exclude(excludedCategories));
			applyRequestFiltersIfRelevant(suite);

			logAndSpOut("Suite has " + suite.testCount() + " tests.");

			return suite;
		}

		private void applyRequestFiltersIfRelevant(Suite suite) throws NoTestsRemainException {
			Set<String> classNames = request.getClassNames();
			if (!classNames.isEmpty())
				suite.filter(new TestFilter.ClassNameFilter(classNames));
		}

		private void runSuite() {
			JUnitCore juCore = new JUnitCore();
			juCore.addListener(new ThreadLocalRunListener(this::createResultFormatter));
			juCore.addListener(new LoggingRunListener());

			Result run = juCore.run(suite);
			int runCount = run.getRunCount();
			int testCount = suite.testCount();
			if (runCount != testCount) {
				// If a test method could not be run at all it does not show up in the result xmls - not even as a
				// failure at all.
				// As a temporary way to not completely lose this information a separate file is created to be able to
				// check for eventual lost runs.
				StringBuilder sb = new StringBuilder("" + //
						"Test suite for [" + moduleDescriptor + "] has" + testCount + " tests but only ran " + runCount + //
						". Probably something went wrong when initializing a test class. " + //
						"The following " + run.getFailures().size() + " failures were recorded when running the tests:\n" //
				);

				for (Failure failure : run.getFailures()) {
					sb.append(failure.getDescription());
					sb.append(":\n\t");
					sb.append(failure.getMessage());
					sb.append("\n");
				}

				File errorsFile = new File(reportRootDir, "COLLECTED_ERRORS.txt");
				try (FileWriter writer = new FileWriter(errorsFile)) {
					writer.append(sb);
				} catch (IOException e) {
					log.error("Could not write to error file after finishing test runs. Module: " + moduleDescriptor, e);
				}
			}
		}

		private JUnitResultFormatterAsRunListener createResultFormatter() {
			FileTools.ensureFolderExists(reportRootDir);

			return new JUnitResultFormatterAsRunListener(new XMLJUnitResultFormatter(), reportRootDir, request.getKeepOriginalSysouts(), suite);
		}

	}

	/* package */ static void logAndSpOut(String msg) {
		log.info(msg);
		spOut(1, msg);
	}

	@Override
	public String testRunnerSelector() {
		return "com/braintribe/junit4";
	}

}
