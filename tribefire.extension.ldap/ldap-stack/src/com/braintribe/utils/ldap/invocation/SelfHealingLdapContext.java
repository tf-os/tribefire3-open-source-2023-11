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
package com.braintribe.utils.ldap.invocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.function.Supplier;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.ThrowableTools;

public class SelfHealingLdapContext implements InvocationHandler {

	protected static Logger logger = Logger.getLogger(SelfHealingLdapContext.class);

	protected LdapContext subject;
	protected Supplier<LdapContext> callback;
	protected long invocationTimeout = 30000L;

	protected String suUsername = null;
	protected String suPassword = null;

	protected String currentUsername = null;
	protected String currentPassword = null;

	protected int passwordCharsInLog = 2;

	public SelfHealingLdapContext(LdapContext subject, Supplier<LdapContext> callback) {
		this.subject = subject;
		this.callback = callback;

		Hashtable<? extends Object, ? extends Object> env;
		try {
			env = this.subject.getEnvironment();
		} catch (NamingException e) {
			throw new RuntimeException("Could not access environment of " + subject, e);
		}
		if (env != null) {
			this.suUsername = (String) env.get(Context.SECURITY_PRINCIPAL);
			this.suPassword = (String) env.get(Context.SECURITY_CREDENTIALS);
		}
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result = null;
		boolean invokeWithTimeout = true;

		try {
			// logger.debug("Invoking "+m.getName()+" with args "+StringTools.createStringFromArray(args));

			String methodName = m.getName();
			if ((methodName.equals("addToEnvironment")) && (args != null) && (args.length == 2)) {
				invokeWithTimeout = false;
				String varName = (String) args[0];
				Object varValue = args[1];

				if (varName != null) {
					if (varName.equals(Context.SECURITY_PRINCIPAL)) {
						this.currentUsername = (String) varValue;
						logger.debug(() -> "(addToEnvironment) Storing entry DN " + this.currentUsername);
					} else if (varName.equals(Context.SECURITY_CREDENTIALS)) {
						this.currentPassword = (String) varValue;
						logger.debug(() -> "(addToEnvironment) Storing PW "
								+ StringTools.simpleObfuscatePassword(this.currentPassword, this.passwordCharsInLog));
					}
				}
			} else if ((methodName.equals("removeFromEnvironment")) && (args != null) && (args.length == 1)) {
				invokeWithTimeout = false;
				String varName = (String) args[0];

				if (varName != null) {
					if (varName.equals(Context.SECURITY_PRINCIPAL)) {
						logger.trace(() -> "(removeFromEnvironment) Removing stored entry DN");
						this.subject.addToEnvironment(Context.SECURITY_PRINCIPAL, this.suUsername);
						this.currentUsername = null;
						return null;
					} else if (varName.equals(Context.SECURITY_CREDENTIALS)) {
						logger.trace(() -> "(removeFromEnvironment) Removing PW "
								+ StringTools.simpleObfuscatePassword(this.currentPassword, this.passwordCharsInLog));
						this.subject.addToEnvironment(Context.SECURITY_CREDENTIALS, this.suPassword);
						this.currentPassword = null;
						return null;
					}
				}
			}

			if (invokeWithTimeout) {
				result = invokeWithTimeout(m, args);
			} else {
				result = m.invoke(this.subject, args);
			}
		} catch (Exception itex) {

			/* Throwable ex=itex.getCause(); if(!(ex instanceof CommunicationException)) { throw ex; } */
			// String secType = (String)subject.getEnvironment().get(Context.SECURITY_AUTHENTICATION);
			try {
				String entrydn = this.currentUsername;
				String password = this.currentPassword;
				logger.debug(() -> "Reusing stored DN/PW " + this.currentUsername + "/"
						+ StringTools.simpleObfuscatePassword(this.currentPassword, this.passwordCharsInLog));

				if (entrydn == null) {
					entrydn = this.suUsername;
					password = this.suPassword;
				}

				String ctxEntrydn = (String) subject.getEnvironment().get(Context.SECURITY_PRINCIPAL);
				String ctxPassword = (String) subject.getEnvironment().get(Context.SECURITY_CREDENTIALS);
				logger.debug(() -> "As a reference, the CTX DN/PW are " + ctxEntrydn + "/"
						+ StringTools.simpleObfuscatePassword(ctxPassword, this.passwordCharsInLog));

				// check if the client supplied invalid credentials; in this case we don't want to repeat
				// the method invocation as this could cause the account to be locked
				if (itex.getCause() != null) {

					boolean skipHealing = false;
					String details = "";

					if (itex.getCause() instanceof javax.naming.NamingException) {
						javax.naming.NamingException ae = (javax.naming.NamingException) itex.getCause();

						details = String.format("explanation='%s', remainingname='%s', resolvedName='%s', resolvedObj=%s", ae.getExplanation(),
								ae.getRemainingName(), ae.getResolvedName(), ae.getResolvedObj());
					}

					if (itex.getCause() instanceof javax.naming.AuthenticationException) {
						javax.naming.AuthenticationException ae = (javax.naming.AuthenticationException) itex.getCause();

						String msg = (ae.getMessage() != null ? ae.getMessage().toLowerCase() : "");
						if (msg.contains("invalid credentials") || msg.contains("account is locked")) {
							skipHealing = true;
						}
					}

					if (itex.getCause() instanceof javax.naming.OperationNotSupportedException) {
						javax.naming.OperationNotSupportedException ae = (javax.naming.OperationNotSupportedException) itex.getCause();

						String msg = (ae.getMessage() != null ? ae.getMessage().toLowerCase() : "");
						if (msg.contains("invalid credentials") || msg.contains("account is locked")) {
							skipHealing = true;
						}
					}

					if (skipHealing) {
						logger.info(String.format(
								"invalid credentials ('%s') passed in or account is locked for user with dn = '%s' (details are %s); skipping re-healing as this would not work anyway",
								StringTools.simpleObfuscatePassword(password, this.passwordCharsInLog), entrydn, details));
						throw itex;
					}
				}

				logger.debug(() -> String.format("e-x-c-e-p-t-i-o-n while invoking method %s(%s): %s", m.getName(), java.util.Arrays.toString(args),
						itex.getMessage()));

				// use a temporary subject so that we can be sure we have a new one before
				// closing the existing one
				LdapContext tmpSubject = callback.get();
				logger.debug("successfully created temporary LDAP context");

				tmpSubject.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
				if (entrydn == null) {
					logger.warn("no entrydn found in context (entrydn == null)");
				} else {
					tmpSubject.addToEnvironment(Context.SECURITY_PRINCIPAL, entrydn);
				}

				if (password == null) {
					logger.warn("could not get the security credentials from the LDAP context (password == null).");
				} else {
					tmpSubject.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
				}

				logger.debug(String.format("SelfHealingLdapContext: renewed stale ldap connection for context: entrydn = %s, password = %s", entrydn,
						StringTools.simpleObfuscatePassword(password, this.passwordCharsInLog)));

				if ((entrydn != null) && (password != null)) {

					logger.debug("successfully created new LDAP context, closing old one");

					try {
						if (this.subject != null) {
							this.subject.close();
							this.subject = null;
						}
					} catch (Exception e) {
						logger.info(String.format("could not close stale/faulty LDAP context: %s", e.getMessage()), e);
					} finally {
						logger.debug("replacing existing subject with newly created one");
						this.subject = tmpSubject;
					}
				} else {
					logger.warn("unable to replace existing LDAP context with new one - missing arguments prevent this");
				}

			} catch (Exception e) {
				logger.error("Error while trying to prepare rehealing.", e);
				StringBuffer stackTrace = new StringBuffer();
				stackTrace.append("Itex: " + ThrowableTools.getStackTraceString(itex) + "\n");
				stackTrace.append("Itex-cause: " + ThrowableTools.getStackTraceString(itex.getCause()) + "\n");
				stackTrace.append("Retry: " + ThrowableTools.getStackTraceString(e) + "\n");
				throw new RuntimeException("Problem with LDAP - we tried to prepare a reconnect and try again (but this did not work either).\n"
						+ stackTrace.toString());
			}
			try {
				if (invokeWithTimeout) {
					result = invokeWithTimeout(m, args);
				} else {
					result = m.invoke(this.subject, args);
				}
			} catch (InvocationTargetException itex1) {
				Throwable ex1 = itex1.getCause();
				throw ex1;
			}
		}
		return result;
	}

	/**
	 * Performs the requested method call by creating a separate thread where the method will be started. This allows us to
	 * limit the maximum execution time of the method call.
	 * 
	 * @param m
	 *            The method that should be executed.
	 * @param args
	 *            The arguments for the method call.
	 * @return The result of the method call.
	 * @throws Exception
	 *             Thrown when a timeout is reached or an other error occured
	 */
	protected Object invokeWithTimeout(Method m, Object[] args) throws Exception {

		// create the new thread
		InvocationThread it = new InvocationThread(this.subject, m, args);
		try {
			it.setDaemon(true);
			it.setName("LDAPInvocationThread");
			it.start();
			// wait for the end of the method call (with a maximum time limit)
			it.join(this.getInvocationTimeout());
		} catch (Exception e) {
			logger.error("Could not start/join invocation handler thread.", e);
		}
		// was there an exception? if so, re-throw it
		if (it.isExceptionThrown()) {
			throw it.getException();
		}

		// is the result available? if not, we have reached the timeout
		if (it.isResultReady()) {
			return it.getResult();
		}

		throw new Exception("Timeout while waiting for a response from the LDAP/AD server.");
	}

	public int getPasswordCharsInLog() {
		return passwordCharsInLog;
	}
	public void setPasswordCharsInLog(int passwordCharsInLog) {
		this.passwordCharsInLog = passwordCharsInLog;
	}

	public long getInvocationTimeout() {
		return invocationTimeout;
	}
	public void setInvocationTimeout(long invocationTimeout) {
		this.invocationTimeout = invocationTimeout;
	}
}
