package com.braintribe.build.cmd.assets.impl.check.process;

import static com.braintribe.console.ConsoleOutputs.brightBlue;
import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.brightYellow;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.red;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheck;
import com.braintribe.build.cmd.assets.impl.check.api.ArtifactCheckContext;
import com.braintribe.build.cmd.assets.impl.check.api.Check;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheck;
import com.braintribe.build.cmd.assets.impl.check.api.GroupCheckContext;
import com.braintribe.build.cmd.assets.impl.check.artifact.ArtifactProjectNameCheck;
import com.braintribe.build.cmd.assets.impl.check.artifact.StandardBuildScriptCheck;
import com.braintribe.build.cmd.assets.impl.check.group.GroupBuildScriptExistImportRightScriptCheck;
import com.braintribe.build.cmd.assets.impl.check.group.ParentArtifactBuildScriptCheck;
import com.braintribe.common.lcd.UnreachableCodeException;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConfigurableConsoleOutputContainer;
import com.braintribe.console.output.ConsoleOutput;
import com.braintribe.utils.lcd.StringTools;

public class CheckGroupProcessor {

	private static List<Check<?>> checks = Arrays.asList( //
			// Group checks
			new GroupBuildScriptExistImportRightScriptCheck(), //
			new ParentArtifactBuildScriptCheck(), //

			// Artifact checks
			new ArtifactProjectNameCheck(), //
			new StandardBuildScriptCheck()
	);

	public static CheckReport process(String groupFolder, boolean fixesEnabled) {
		// we are creating a new check execution to make sure the processor itself is thread safe.
		return new CheckExecution(groupFolder, fixesEnabled).execute();
	}

	private static class CheckExecution {

		private boolean fixesApplied;
		private final boolean fixesEnabled;
		private final GroupCheckContextImpl groupCheckContext;
		private final List<ArtifactCheckContextImpl> artifactCheckContexts;

		private final CheckReport report = CheckReport.T.create();

		private final Map<GroupCheck, List<GroupCheckContext>> failedFixableGroupChecksToContexts = newMap();
		private final Map<ArtifactCheck, List<ArtifactCheckContext>> failedFixableArtifactsChecksToContexts = newMap();

		private List<GroupCheck> groupChecks;
		private List<ArtifactCheck> artifactChecks;

		public CheckExecution(String groupFolder, boolean fixesEnabled) {
			this.fixesEnabled = fixesEnabled;
			this.groupCheckContext = GroupCheckContextImpl.create(groupFolder, fixesEnabled);
			this.artifactCheckContexts = ArtifactCheckContextImpl.create(groupCheckContext);
		}

		public CheckReport execute() {
			separateGroupAndArtifactChecks();

			executeChecks("EXECUTE CHECKS");
			ConsoleOutputs.println("\n");

			if (failedFixableArtifactsChecksToContexts.isEmpty() && failedFixableGroupChecksToContexts.isEmpty()) {
				ConsoleOutputs.println(brightYellow("ALL CHECKS HAVE SUCCESSFULLY PASSED!"));
			} else if (fixesEnabled) {
				fixAndReCheck();
			}

			return report;
		}

		private void separateGroupAndArtifactChecks() {
			Map<Boolean, List<Check<?>>> groupByTypeChecks = checks.stream().collect(Collectors.groupingBy(check -> check instanceof GroupCheck));

			this.groupChecks = (List<GroupCheck>) (List<?>) groupByTypeChecks.get(true);
			this.artifactChecks = (List<ArtifactCheck>) (List<?>) groupByTypeChecks.get(false);
		}

		private void fixAndReCheck() {
			ConsoleOutputs.println(brightYellow("=== APPLYING FIXES ==="));
			if (!fixesEnabled) {
				ConsoleOutputs.println(brightYellow("APPLYING FIXES is DISABLED"));
				return;
			}

			ConsoleOutputs.println(brightYellow("APPLYING FIXES is ENABLED"));
			applyFixes();

			ConsoleOutputs.println("\n");

			report.getCheckResults().clear();

			executeChecks("RE-EXECUTE CHECKS");
		}

		private void executeChecks(String title) {
			ConsoleOutputs.println(brightYellow("=== " + title + " ==="));
			if (groupChecks != null) {
				executeGroupChecks();
			}

			if (artifactChecks != null) {
				executeArtifactChecks();
			}
		}

		private void executeGroupChecks() {
			ConsoleOutputs.println(brightYellow("GROUP CHECKS: "));

			for (GroupCheck check : groupChecks) {
				new CheckResultPrinter(check.getTitle()) //
						.addCheckResult(check, executeCheck(check, groupCheckContext, failedFixableGroupChecksToContexts)) //
						.print();
			}
		}

		private void executeArtifactChecks() {
			ConsoleOutputs.println(brightYellow("ARTIFACT CHECKS: "));

			for (ArtifactCheck check : artifactChecks) {
				CheckResultPrinter resultPrinter = new CheckResultPrinter(check.getTitle());

				for (ArtifactCheckContextImpl artifactContext : artifactCheckContexts) {
					resultPrinter.addCheckResult(check, executeCheck(check, artifactContext, failedFixableArtifactsChecksToContexts));
				}

				resultPrinter.print();
			}
		}

		private <CTX extends GroupCheckContext, R extends CheckResult, C extends Check<CTX>> R executeCheck(C check, CTX checkContext,
				Map<C, List<CTX>> failedFixableChecksToContexts) {
			R checkResult = ((GroupCheckContextImpl) checkContext).newCheckResult();
			checkResult.setCheck(check.getName());
			checkResult.setTitle(check.getTitle());

			try {
				ResultStatus status = check.execute(checkContext);
				if (status != null) {
					checkResult.setStatus(status);
				} else {
					checkResult.setStatus(ResultStatus.failure);
					checkResult.setDetailedInfo("The check has not set any status. This is probably a bug in check implementation, see class "
							+ check.getClass().getName() + ".");
				}

			} catch (Exception e) {
				checkResult.setStatus(ResultStatus.failure);
				checkResult.setDetailedInfo("Something went wrong during check execution:\n" + e.getMessage());
			}

			if (checkResult.getStatus() == ResultStatus.error && check.fixable()) {
				acquireList(failedFixableChecksToContexts, check).add(checkContext);

				if (fixesEnabled) {
					checkResult.setIsFixApplied(true);
				}
			}

			report.getCheckResults().add(checkResult);

			return checkResult;
		}

		private void applyFixes() {
			if (!failedFixableGroupChecksToContexts.isEmpty()) {
				applyFixesForGroupChecks();
			}
			if (!failedFixableArtifactsChecksToContexts.isEmpty()) {
				applyFixesForArtifactChecks();
			}
			fixesApplied = true;
		}

		private void applyFixesForGroupChecks() {
			ConsoleOutputs.println(brightYellow("GROUP CHECKS: "));
			for (Entry<GroupCheck, List<GroupCheckContext>> entry : failedFixableGroupChecksToContexts.entrySet()) {
				GroupCheck check = entry.getKey();
				CheckResultPrinter resultPrinter = new CheckResultPrinter(check.getTitle());
				// we know that there is always one groupCheckContext for a GroupCheck
				GroupCheckContext checkContext = first(entry.getValue());
				GroupCheckResult checkResult = ((GroupCheckContextImpl) checkContext).newCheckResult();

				fixError(check, resultPrinter, checkContext, checkResult);
				resultPrinter.print();
			}
		}

		private void applyFixesForArtifactChecks() {
			ConsoleOutputs.println(brightYellow("ARTIFACTS CHECKS: "));
			for (Entry<ArtifactCheck, List<ArtifactCheckContext>> entry : failedFixableArtifactsChecksToContexts.entrySet()) {
				ArtifactCheck check = entry.getKey();
				CheckResultPrinter resultPrinter = new CheckResultPrinter(check.getTitle());
				for (ArtifactCheckContext checkContext : entry.getValue()) {
					ArtifactCheckResult checkResult = ((ArtifactCheckContextImpl) checkContext).newCheckResult();

					fixError(check, resultPrinter, checkContext, checkResult);
				}
				resultPrinter.print();
			}
		}

		private <C extends GroupCheckContext> void fixError(Check<C> check, CheckResultPrinter resultPrinter, C checkContext,
				CheckResult checkResult) {
			try {
				ResultStatus status = check.fixError(checkContext);

				if (status != null) {
					checkResult.setStatus(status);
				} else {
					checkResult.setStatus(ResultStatus.failure);
					checkResult.setDetailedInfo("The check has not set any status. This is probably a bug in check implementation, see class "
							+ check.getClass().getName() + ".");
				}
			} catch (Exception e) {
				checkResult.setStatus(ResultStatus.failure);
				checkResult.setDetailedInfo("Something went wrong during fix execution:\n" + e.getMessage());
			}

			resultPrinter.addFixResult(checkContext, check, checkResult);
		}

		private class CheckResultPrinter {

			ConfigurableConsoleOutputContainer outputs = ConsoleOutputs.configurableSequence();

			public CheckResultPrinter(String title) {
				if (StringTools.isEmpty(title)) {
					outputs.append(yellow(" > TITLE is missing "));
				} else {
					outputs.append(yellow(" > " + title + " "));
				}
			}

			public CheckResultPrinter addCheckResult(Check<?> check, CheckResult result) {
				if (result instanceof ArtifactCheckResult) {
					outputs.append("\n");
					outputs.append(" - " + ((ArtifactCheckResult) result).getArtifactId() + " ");
				}
				outputs.append(printFormated(result.getStatus()));
				if (check.fixable() && result.getStatus().equals(ResultStatus.error) && !CheckExecution.this.fixesApplied) {
					outputs.append(" ");
					outputs.append(brightWhite("[fixable]"));
				}
				if (!StringTools.isEmpty(result.getDetailedInfo())) {
					outputs.append("\n");
					outputs.append("\t" + result.getDetailedInfo());
				}
				return this;
			}

			public CheckResultPrinter addFixResult(GroupCheckContext checkContext, Check<?> check, CheckResult result) {
				if (!check.fixable()) {
					throw new UnreachableCodeException("Illegal attempt to apply fix for a non fixable check!");
				}

				if (checkContext instanceof ArtifactCheckContext) {
					outputs.append("\n");
					outputs.append(" - " + ((ArtifactCheckContext) checkContext).getArtifact().getArtifactId() + " ");
				}

				if (result.getStatus().equals(ResultStatus.success)) {
					// custom string for fixes (instead of success)
					outputs.append(green("[applied fix]"));
				} else {
					outputs.append(printFormated(result.getStatus()));
				}

				if (!StringTools.isEmpty(result.getDetailedInfo())) {
					outputs.append("\n");
					outputs.append("\t" + result.getDetailedInfo());
				}

				return this;
			}

			public void print() {
				ConsoleOutputs.println(outputs);
			}

			private ConsoleOutput printFormated(ResultStatus resultStatus) {
				final String checkResultStatusAsString = "[" + resultStatus.toString() + "]";
				switch (resultStatus) {
					case success:
						return green(checkResultStatusAsString);
					case error:
						return red(checkResultStatusAsString);
					case failure:
						return red(checkResultStatusAsString);
					case skipped:
						return brightBlue(checkResultStatusAsString);
					default:
						return brightWhite(checkResultStatusAsString);
				}
			}
		}
	}
}
