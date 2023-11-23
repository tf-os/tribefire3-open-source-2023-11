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
package tribefire.cortex.testing.junit.formatter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitResultFormatter;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.Suite;

import com.braintribe.utils.FileTools;

import junit.framework.JUnit4TestAdapterCache;
import junit.framework.Test;

/**
 * Adopts {@link JUnitResultFormatter} into {@link RunListener}, and also captures stdout/stderr by intercepting the likes of {@link System#out}.
 *
 * Because Ant JUnit formatter uses one stderr/stdout per one test suite, we capture each test case into a separate report file.
 */
public class JUnitResultFormatterAsRunListener extends RunListener {
	protected final JUnitResultFormatter formatter;
	private ByteArrayOutputStream stdout, stderr;
	private PrintStream oldStdout, oldStderr;
	private int problem;
	private long startTime;
	private final File reportDir;
	private final boolean keepOriginalSysOuts;
	private final Suite suite;

	public JUnitResultFormatterAsRunListener(JUnitResultFormatter formatter, File reportDir, boolean keepOriginalSysOuts, Suite suite) {
		this.formatter = formatter;
		this.reportDir = reportDir;
		this.keepOriginalSysOuts = keepOriginalSysOuts;
		this.suite = suite;
	}

	@Override
	public void testRunStarted(Description description) throws Exception {
		// NO OP
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		int runCount = result.getRunCount();
		int testCount = suite.testCount();
		if (runCount != testCount) {
			// If a test method could not be run at all, in some cases it does not show up in the result xmls - not even
			// as a failure at all.
			// This is to create test reports so that they show up as failed tests.

			for (Failure failure : result.getFailures()) {
				Description description = failure.getDescription();
				Test cachedTest = JUnit4TestAdapterCache.getDefault().get(description);
				// If the test had been cached before there is already a report xml and we don't need to create another
				// one
				if (cachedTest == null) {
					testStarted(description);
					testFailure(failure);
					testFinished(description);
				}
			}
		}
	}

	@Override
	public void testStarted(Description description) throws Exception {
		String outFileName = FileTools.normalizeFilename("TEST-" + description.getDisplayName() + ".xml", '_');
		File outFile = new File(reportDir, outFileName);
		formatter.setOutput(new FileOutputStream(outFile));
		formatter.startTestSuite(new JUnitTest(description.getDisplayName()));
		Test test = JUnit4TestAdapterCache.getDefault().asTest(description);
		formatter.startTest(test);
		problem = 0;
		startTime = System.currentTimeMillis();

		if (!keepOriginalSysOuts) {
			this.oldStdout = System.out;
			this.oldStderr = System.err;
			System.setOut(new PrintStream(stdout = new ByteArrayOutputStream()));
			System.setErr(new PrintStream(stderr = new ByteArrayOutputStream()));
		}
	}

	@Override
	public void testFinished(Description description) throws Exception {
		if (!keepOriginalSysOuts) {
			System.out.flush();
			System.err.flush();
			System.setOut(oldStdout);
			System.setErr(oldStderr);
			formatter.setSystemOutput(stdout.toString());
			formatter.setSystemError(stderr.toString());
		}

		Test test = JUnit4TestAdapterCache.getDefault().asTest(description);
		formatter.endTest(test);

		JUnitTest suite = new JUnitTest(description.getDisplayName());
		suite.setCounts(1, problem, 0);
		suite.setRunTime(System.currentTimeMillis() - startTime);
		formatter.endTestSuite(suite);
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		testAssumptionFailure(failure);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		problem++;
		Test test = JUnit4TestAdapterCache.getDefault().asTest(failure.getDescription());
		formatter.addError(test, failure.getException());
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		super.testIgnored(description);
	}
}