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
package com.braintribe.model.processing.service.impl;

import static com.braintribe.model.processing.service.common.FailureCodec.INSTANCE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;

/**
 * {@link FailureCodec} tests.
 */
public class FailureCodecTest {

	@Test
	public void testExceptionChain() throws Exception {
		testFailureCodec(getExceptionChain());
	}

	@Test
	public void testExceptionChainWithSuppressed() throws Exception {
		testFailureCodec(getExceptionChainWithSuppressed());
	}

	@Test
	public void testExceptionChainWithDuplicatedCauseReferences() throws Exception {
		testFailureCodec(getExceptionChainWithDuplicatedCauseReferences());
	}

	@Test
	public void testExceptionChainWithDuplicatedSuppressedReferences() throws Exception {
		testFailureCodec(getExceptionChainWithDuplicatedSuppressedReferences());
	}

	@Test
	public void testExceptionChainWithSuppressedAndCircularReferences() throws Exception {
		testFailureCodec(getExceptionChainWithSuppressedAndCircularReferences());
	}

	@Test
	public void testNotificationException() throws Exception {
		testFailureCodec(getNotificationException());
	}

	@Test
	public void testExceptionWithSuppressedNotifications() throws Exception {
		testFailureCodec(getExceptionWithSuppressedNotifications());
	}

	@Test
	public void testNotificationExceptionWithSuppressedNotification() throws Exception {
		testFailureCodec(getNotificationExceptionWithSuppressedNotification());
	}

	@Test
	public void testNotificationExceptionWithSuppressedNotifications() throws Exception {
		testFailureCodec(getNotificationExceptionWithSuppressedNotifications());
	}

	protected void testFailureCodec(Throwable original) throws Exception {

		System.out.println("Original stack trace: ");
		original.printStackTrace(System.out);

		Failure failure = INSTANCE.encode(original);
		
		assertSerialization(original, failure);

		Throwable reconstructed = INSTANCE.decode(failure);

		assertEquals(original, reconstructed);

		System.out.println("Reconstructed stack trace: ");
		reconstructed.printStackTrace(System.out);

	}

	private static Exception getExceptionChain() throws Exception {
		Exception result = null;
		try {
			try {
				try {
					try {
						try {
							Integer.parseInt("A");
						} catch (Exception e) {
							try {
								throw new IOException("io", e);
							} catch (Exception ioc) {
								throw new Exception("java.lang's", ioc);
							}
						}
					} catch (Exception e) {
						throw new Exception("java.lang's", e);
					}
				} catch (Exception e) {
					throw new IllegalStateException("illegal state excep.", e);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("illegal argum. excep.", e);
			}
		} catch (Exception e) {
			result = e;
		}
		return result;
	}

	private static Exception getExceptionChainWithDuplicatedCauseReferences() throws Exception {
		Exception result = null;
		try {
			try {
				try {
					try {
						try {
							Integer.parseInt("A");
						} catch (Exception e1) {
							try {
								throw new IOException("io", e1);
							} catch (Exception ioc) {
								throw new Exception("java.lang's", e1);
							}
						}
					} catch (Exception e2) {
						throw new Exception("java.lang's", e2);
					}
				} catch (Exception e3) {
					throw new IllegalStateException("illegal state excep.", e3);
				}
			} catch (Exception e4) {
				throw new IllegalArgumentException("illegal argum. excep.", e4);
			}
		} catch (Exception e5) {
			result = e5;
		}
		return result;
	}

	private static Exception getExceptionChainWithSuppressed() throws Exception {
		Exception result = null;
		try {
			try {
				try {
					try {
						try {
							Integer.parseInt("A");
						} catch (Exception e) {
							try {
								throw new IOException("io", e);
							} catch (Exception ioc) {
								Throwable suppressed0 = new UnsupportedClassVersionError("unsupported class version excep.");
								Throwable suppressed1 = new UnsupportedOperationException("unsupported operation excep.", e);
								Throwable suppressed2 = new UnsupportedEncodingException("unsupported encoding");
								Exception exception = new Exception("java.lang's", ioc);
								exception.addSuppressed(suppressed0);
								exception.addSuppressed(suppressed1);
								exception.addSuppressed(suppressed2);
								throw exception;
							}
						}
					} catch (Exception e) {
						throw new Exception("java.lang's", e);
					}
				} catch (Exception e) {
					throw new IllegalStateException("illegal state excep.", e);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("illegal argum. excep.", e);
			}
		} catch (Exception e) {
			result = e;
		}
		return result;
	}

	private static Exception getExceptionChainWithDuplicatedSuppressedReferences() {
		Exception result = null;
		Throwable suppressed = new UnsupportedClassVersionError("unsupported class version excep.");
		try {
			try {
				try {
					try {
						try {
							Integer.parseInt("A");
						} catch (Exception e) {
							try {
								throw new IOException("io", e);
							} catch (Exception ioc) {
								Throwable suppressed0 = new InvalidParameterException("invalid param excep.");
								Throwable suppressed1 = new UnsupportedOperationException("unsupported operation excep.", e);
								Throwable suppressed2 = new UnsupportedEncodingException("unsupported encoding");
								Throwable suppressed3 = new UnsupportedOperationException("unsupported operation excep.", suppressed);
								Exception exception = new Exception("java.lang's", ioc);
								exception.addSuppressed(suppressed0);
								exception.addSuppressed(suppressed1);
								exception.addSuppressed(suppressed2);
								exception.addSuppressed(suppressed3);
								throw exception;
							}
						}
					} catch (Exception e) {

						Throwable suppressed0 = new InvalidParameterException("invalid param excep.");
						Throwable suppressed1 = new UnsupportedOperationException("unsupported operation excep.", e);
						Throwable suppressed2 = new UnsupportedEncodingException("unsupported encoding");
						Throwable suppressed3 = new UnsupportedOperationException("unsupported operation excep.", suppressed);

						Exception exception = new Exception("java.lang's", e);
						exception.addSuppressed(suppressed0);
						exception.addSuppressed(suppressed1);
						exception.addSuppressed(suppressed2);
						exception.addSuppressed(suppressed3);
						throw exception;

					}
				} catch (Exception e) {
					throw new IllegalStateException("illegal state excep.", e);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("illegal argum. excep.", e);
			}
		} catch (Exception e) {
			result = e;
		}
		return result;
	}

	private static Exception getExceptionChainWithSuppressedAndCircularReferences() {

		Exception e3 = new InvalidParameterException("Invalid Parameter Exception 3");

		Exception e2 = new IllegalStateException("Illegal State Exception 2", e3);
		e2.addSuppressed(e3);

		Exception e1 = new IllegalArgumentException("Illegal Argument Exception 1.", e2);
		e1.addSuppressed(e2);
		e1.addSuppressed(e3);

		Exception e0 = new IOException("IO Exception 0", e1);
		e0.addSuppressed(e1);
		e0.addSuppressed(e2);
		e0.addSuppressed(e3);

		Exception root = new UnsupportedOperationException("Unsupported Operation Exception", e0);

		root.addSuppressed(e0);
		root.addSuppressed(e1);
		root.addSuppressed(e2);
		root.addSuppressed(e3);

		e3.addSuppressed(root);

		return root;

	}

	private static Exception getNotificationException() throws Exception {
		Exception result = new ServiceProcessorNotificationException(FooNotification.T.create());
		return result;
	}

	private static Exception getExceptionWithSuppressedNotifications() throws Exception {
		Exception result = null;
		try {
			try {
				try {
					try {
						try {
							Integer.parseInt("A");
						} catch (Exception e) {
							try {
								throw new IOException("io", e);
							} catch (Exception ioc) {
								Throwable suppressed0 = new ServiceProcessorNotificationException(FooNotification.T.create());
								Throwable suppressed1 = new UnsupportedClassVersionError("unsupported class version excep.");
								Throwable suppressed2 = new UnsupportedOperationException("unsupported operation excep.", e);
								Exception exception = new Exception("java.lang's", ioc);
								exception.addSuppressed(suppressed0);
								exception.addSuppressed(suppressed1);
								exception.addSuppressed(suppressed2);
								throw exception;
							}
						}
					} catch (Exception e) {
						throw new Exception("java.lang's", e);
					}
				} catch (Exception e) {
					IllegalStateException exception = new IllegalStateException("illegal state excep.", e);
					Throwable suppressed0 = new UnsupportedEncodingException("unsupported class version excep.");
					Throwable suppressed1 = new ServiceProcessorNotificationException(BarNotification.T.create());
					exception.addSuppressed(suppressed0);
					exception.addSuppressed(suppressed1);
					throw exception;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("illegal argum. excep.", e);
			}
		} catch (Exception e) {
			result = e;
		}
		return result;
	}

	private static Exception getNotificationExceptionWithSuppressedNotification() throws Exception {
		Exception result = new ServiceProcessorNotificationException(FooNotification.T.create());
		Throwable suppressed = new ServiceProcessorNotificationException(BarNotification.T.create());
		result.addSuppressed(suppressed);
		return result;
	}

	private static Exception getNotificationExceptionWithSuppressedNotifications() throws Exception {
		Exception result = new ServiceProcessorNotificationException(FooNotification.T.create());
		Throwable suppressed0 = new ServiceProcessorNotificationException(BarNotification.T.create());
		Throwable suppressed1 = new ServiceProcessorNotificationException(FooNotification.T.create());
		Throwable suppressed2 = new ServiceProcessorNotificationException(BarNotification.T.create());
		result.addSuppressed(suppressed0);
		result.addSuppressed(suppressed1);
		result.addSuppressed(suppressed2);
		return result;
	}

	private void assertEquals(Throwable original, Throwable reconstructed) {
		assertEquals(original, reconstructed, new HashSet<Throwable>());
	}

	private void assertEquals(Throwable original, Throwable reconstructed, Set<Throwable> compared) {

		Assert.assertNotNull(original);
		Assert.assertNotNull(reconstructed);

		if (!compared.add(original)) {
			return;
		}

		Assert.assertEquals(original.getClass(), reconstructed.getClass());
		Assert.assertEquals(original.getMessage(), reconstructed.getMessage());

		if (original.getCause() != null || reconstructed.getCause() != null) {
			assertEquals(original.getCause(), reconstructed.getCause(), compared);
		}

		Throwable[] originalSuppressed = original.getSuppressed();
		Throwable[] reconstructedSuppressed = reconstructed.getSuppressed();

		Assert.assertEquals(originalSuppressed.length, reconstructedSuppressed.length);

		for (int i = 0; i < originalSuppressed.length; i++) {
			assertEquals(originalSuppressed[i], reconstructedSuppressed[i], compared);
		}

	}

	private void assertSerialization(Throwable original, Failure failure) {
		assertSerialization(original, failure, new HashSet<Throwable>());
	}

	private void assertSerialization(Throwable original, Failure failure, Set<Throwable> compared) {

		Assert.assertNotNull(original);
		Assert.assertNotNull(failure);

		if (!compared.add(original)) {
			return;
		}

		if (original.getCause() != null ) {
			assertSerialization(original.getCause(), failure.getCause(), compared);
		} else {
			Assert.assertNull(failure.getCause());
		}
		
		if (original instanceof ServiceProcessorNotificationException) {
			ServiceRequest notification = ((ServiceProcessorNotificationException) original).getNotification();
			if (notification != null) {
				Assert.assertNotNull(failure.getNotification());
				Assert.assertTrue(failure.getNotification().getClass() == notification.getClass());
			} else {
				Assert.assertNull(failure.getNotification());
			}
		}

		Throwable[] suppressed = original.getSuppressed();

		Assert.assertEquals(suppressed.length, failure.getSuppressed().size());

		for (int i = 0; i < suppressed.length; i++) {
			assertSerialization(suppressed[i], failure.getSuppressed().get(i), compared);
		}

	}

	public static interface FooNotification extends ServiceRequest {
		EntityType<FooNotification> T = EntityTypes.T(FooNotification.class);
	}

	public static interface BarNotification extends ServiceRequest {
		EntityType<BarNotification> T = EntityTypes.T(BarNotification.class);
	}

}
