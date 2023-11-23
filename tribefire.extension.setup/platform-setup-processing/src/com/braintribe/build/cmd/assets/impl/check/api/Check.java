package com.braintribe.build.cmd.assets.impl.check.api;

import com.braintribe.build.cmd.assets.impl.check.process.CheckResult;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;

/**
 * An interface to be implemented by concrete check types (see {@link ArtifactCheck} and {@link GroupCheck}).
 */
public interface Check<C extends GroupCheckContext> {

	/**
	 * The check name. By default, it's just the simple class name.
	 */
	default String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Value that is passed to the corresponding {@link CheckResult#getTitle() check result title}. Cannot be <tt>null</tt>.
	 */
	String getTitle();

	/**
	 * Executes a check on a artifact group. A check can be either a type of {@code ArtifactCheck} or {@code GroupCheck}.
	 *
	 * @param context
	 *            the respective context which contains information about the artifact group on which the check is applied
	 *            (i.e {@link GroupCheckContext}) if the check is of type {@link GroupCheck} or information about the
	 *            artifact on which the check is applied (i.e. {@link ArtifactCheckContext}) if the check is of type
	 *            {@link ArtifactCheck}.
	 * @return the check result status
	 */
	ResultStatus execute(C context);

	/**
	 * Indicates whether this check implementation may also try to fix the problem it detects in
	 * {@link #execute(GroupCheckContext)}.
	 */
	boolean fixable();

	/**
	 * Attempts to fix the error detected in {@link #execute(GroupCheckContext)}
	 *
	 * @param context
	 *            same context as passed to {@link #execute(GroupCheckContext)}.
	 * @return the check (or in this fix) result status.
	 */
	ResultStatus fixError(C context);
}
