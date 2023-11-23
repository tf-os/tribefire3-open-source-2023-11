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
package com.braintribe.exception;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class ExceptionsTest {

	@Test
	public void testExceptionWrapping() throws Exception {

		RuntimeException re = new RuntimeException("hello");
		RuntimeException actual = Exceptions.unchecked(re, null);

		assertThat(actual).isEqualTo(re);
		assertThat(actual.getMessage()).isEqualTo(re.getMessage());



		Exception checked = new Exception("world");
		actual = Exceptions.unchecked(checked, "hello");

		assertThat(actual).isInstanceOf(RuntimeException.class);
		assertThat(actual.getMessage()).isEqualTo("hello");
	}

	@Test
	public void testStringifier() throws Exception {

		Throwable t = new Throwable("hello");
		Throwable t2 = new Throwable("world", t);

		String actual = Exceptions.stringify(t2);

		assertThat(actual).isNotNull();
		assertThat(actual).contains("hello");
		assertThat(actual).contains("world");
	}

	@Test
	public void suppressionTest() throws Exception {

		try {
			try (InputStream in = new ErrorInputStream("Suppressed exception on wrapper around root cause.")) {
				try (InputStream in3 = new ErrorInputStream("Suppressed exception on root cause.")) {
					Exception cause = new Exception("This is the root cause of everything. The absolute start.");
					cause.addSuppressed(new Exception("This is the first suppressed exception added to the root cause."));
					throw new Exception("First wrapper around root cause (aka root-cause-wrapper)", cause);
				} catch(Exception cause) {
					Exception e = new Exception("This is an explicit exception that wraps around the root-cause-wrapper (it has two suppressed excpeptions).", cause);
					e.addSuppressed(new Exception("This is a suppressed exception to the exception that wraps the the root-cause-wrapper (manually added)"));
					throw e;
				}
			}
		} catch(Throwable t) {
			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		}

	}

	@Test
	public void separateThreadTest() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				Future<?> f = pool.submit(new Runner("This is the root cause of everything. The absolute start."));
				f.get();
			} catch(Throwable t) {
				throw new Exception("Error in separate thread.", t);
			}
		} catch(Throwable t) {
			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}

	}

	private static class Runner implements Callable<Void> {

		public String message;

		public Runner(String message) {
			super();
			this.message = message;
		}

		@Override
		public Void call() throws Exception {
			throw new RuntimeException(message);
		}

	}

	@Test
	public void testSuppressedLoop() throws Exception {

		try {

			Exception e1 = new Exception("Exception 1");
			Exception e2 = new Exception("Exception 2");
			e1.addSuppressed(e2);
			e2.addSuppressed(e1);
			throw e1;

		} catch(Throwable t) {
			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		}

	}

	@Test
	public void testCauseLoop() throws Exception {

		try {

			BogusException e1 = new BogusException("Exception 1");
			BogusException e2 = new BogusException("Exception 2");
			e1.myCause = e2;
			e2.myCause = e1;

			throw e1;

		} catch(Throwable t) {
			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		}

	}

	@Test
	public void testMassiveWrapping() throws Exception {

		try {

			int iterations = 100;
			Exception e = new Exception("root cause");
			for (int i=0; i<iterations; ++i) {
				e = new Exception("Iteration "+i, e);
			}

			throw e;

		} catch(Throwable t) {
			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		}

	}

	static class BogusException extends Exception {

		private static final long serialVersionUID = 1L;
		public Throwable myCause; 

		public BogusException(String string) {
			super(string);
		}

		@Override
		public synchronized Throwable getCause() {
			return myCause;
		}
	}

	static class ErrorInputStream extends InputStream {

		private final String message;

		public ErrorInputStream(String message) {
			this.message = message;
		}
		@Override
		public int read() throws IOException {
			return -1;
		}

		@Override
		public void close() throws IOException {
			throw new RuntimeException(message);
		}
	}

	@Test
	public void comparisonTestComplex() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try (InputStream in = new ErrorInputStream("Suppressed exception on wrapper around root cause.")) {
				try (InputStream in3 = new ErrorInputStream("Suppressed exception on root cause.")) {
					try {
						Future<?> f = pool.submit(new Runner("This is the root cause of everything. The absolute start."));
						f.get();
					} catch(Throwable t) {
						t.addSuppressed(new Exception("This is a manually added suppressed exception to the ExecutionException thrown by the pool.", new Exception("An additional level to the manually added suppressed exception.")));
						throw new Exception("First wrapper around the ExecutionException", t);
					}
				} catch(Exception cause) {
					Exception e = new Exception("This is an explicit exception that wraps around the root-cause-wrapper (it has three suppressed exceptions).", cause);
					e.addSuppressed(new Exception("This is a suppressed exception to the exception that wraps the the root-cause-wrapper (manually added)"));

					try {
						Future<?> f = pool.submit(new Runner("This is a separate thread exception added as a suppressed exception."));
						f.get();
					} catch(Throwable t) {
						e.addSuppressed(Exceptions.contextualize(t, "This is a context on the secondary thread exception. Just to make things a bit more complicated."));
					}


					throw e;
				}
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}

	}

	@Test
	public void testThreeLevels() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				try {
					throw new Exception("First exception");
				} catch(Exception e) {
					throw new Exception("Second exception", e);
				}
			} catch(Exception e2) {
				throw new Exception("Third exception", e2);
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}

	}
	
	@Test
	public void testThreeLevelsAndSuppressed() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				try {
					throw new Exception("First exception");
				} catch(Exception e) {
					Exception suppressed = new Exception("Supressed exception on first exception.", new Exception("Cause for suppressed"));
					e.addSuppressed(suppressed);
					throw new Exception("Second exception", e);
				}
			} catch(Exception e2) {
				throw new Exception("Third exception", e2);
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}

	}
	
	@Test
	public void testThreeLevelsWithContext() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				try {
					throw new Exception("First exception");
				} catch(Exception e) {
					throw Exceptions.contextualize(e, "Context to first exception");
				}
			} catch(Exception e2) {
				throw new Exception("Second exception", e2);
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(Exceptions.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}
	}
	
	@Test
	public void testThreeLevelsWithContextAlternative() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				try {
					try {
						throw new Exception("Root");
					} catch(Exception e) {
						throw new Exception("Exc2", e);
					}
				} catch(Exception e) {
					throw new Exception("Exc1", e);
				}
			} catch(Exception e) {
				throw new Exception("Some exception", e);
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(ExceptionsStringifier.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}
	}
	
	@Test
	public void testThreeLevelsWithContextAlternativeWithContext() throws Exception {

		ExecutorService pool = Executors.newFixedThreadPool(1);
		try {
			try {
				try {
					throw new Exception("First exception");
				} catch(Exception e) {
					throw Exceptions.contextualize(e, "Context to first exception");
				}
			} catch(Exception e2) {
				throw new Exception("Second exception", e2);
			}
		} catch(Throwable t) {

			System.out.println("Exceptions.stringify");
			System.out.println("============================");
			System.out.println(ExceptionsStringifier.stringify(t));
			System.out.println("\n\nprintStackTrace");
			System.out.println("============================");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			System.out.println(sw.toString());
		} finally {
			pool.shutdown();
		}
	}
	
}
