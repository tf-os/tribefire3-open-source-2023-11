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
package com.braintribe.gwt.async.client;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.braintribe.gwt.async.testing.Futures;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author peter.gazdik
 */
public class FutureTest {

	private static final String VALUE = "VALUE";

	private Future<String> f;
	private Throwable error;

	private final AtomicInteger atomicCounter = new AtomicInteger(0);

	// #######################################################
	// ## . . . . . . . . Sync Future tests . . . . . . . . ##
	// #######################################################

	@Test
	public void fromSupplier() throws Exception {
		syncFuture();
		assertIsValue(f.getResult());
	}

	@Test
	public void fromSupplier_Error() throws Exception {
		f = Future.<String> fromSupplier(() -> throwNpe()) //
				.onError(this::collectError);

		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void fromSupplierAsyncGet() throws Exception {
		syncFuture();

		f.get(Future.asyncGwt(e -> { /*NOOP*/ }, this::assertIsValue));
	}

	// #######################################################
	// ## . . . . . . . . Async Future tests . . . . . . . .##
	// #######################################################

	@Test
	public void fromCallbackConsumerAsyncGet() throws Exception {
		asyncFuture();
		f.get(Future.asyncGwt(e -> { /*NOOP*/ }, this::assertIsValue));

		waitFor(f);
	}

	@Test
	public void fromCallbackConsumer_Error() throws Exception {
		f = Future.<String> fromAsyncCallbackConsumer(gwtCallback -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThen() throws Exception {
		asyncFuture();
		f = f.andThen(value -> atomicCounter.incrementAndGet());

		waitFor(f);
		assertThat(atomicCounter.get()).isEqualTo(1);
	}

	@Test
	public void andThen_Error() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.andThen(value -> {
					// We do not get here, becaue the NPE is propagated all the way to onError
					throw new IllegalStateException("THIS IS NOT REACHED");
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenNotify() throws Exception {
		asyncFuture();
		Future<String> lowF = f.andThenNotify((value, callback) -> callback.onSuccess(value.toLowerCase()));
		Future<Integer> lenF = f.andThenNotify((value, callback) -> callback.onSuccess(value.length()));

		assertEventualValue(lowF, VALUE.toLowerCase());
		assertEventualValue(lenF, VALUE.length());
	}

	@Test
	public void andThenNotify_Error() throws Exception {
		asyncFuture();
		f = f.<String> andThenNotify((value, callback) -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenMap() throws Exception {
		asyncFuture();
		Future<Integer> lenF = f.andThenMap(String::length);

		assertEventualValue(lenF, VALUE.length());
	}

	@Test
	public void andThenMap_Error() throws Exception {
		asyncFuture();
		f = f.<String> andThenMap(value -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenMapAsync() throws Exception {
		asyncFuture();
		Future<Integer> lenF = f.andThenMapAsync(value -> asyncFuture(value.length()));

		assertEventualValue(lenF, VALUE.length());
	}

	@Test
	public void andThenMapAsync_Error() throws Exception {
		asyncFuture();
		f = f.<String> andThenMapAsync(value -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenOrOnError_WithValue() throws Exception {
		asyncFuture();
		f = f.andThenOrOnError((value, error) -> {
			assertThat(value).isEqualTo(VALUE);
			assertThat(error).isNull();
			atomicCounter.incrementAndGet();
		});

		waitFor(f);
		assertThat(atomicCounter.get()).isEqualTo(1);
	}

	@Test
	public void andThenOrOnError_WithValue_Error() throws Exception {
		asyncFuture();
		f = f.andThenOrOnError((value, error) -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenOrOnError_WithError() throws Exception {
		asyncFuture();

		f = f.andThen(value -> throwNpe()) //
				.andThenOrOnError((value, error) -> {
					this.error = error;
				});

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andThenOrOnError_WithError_Error() throws Exception {
		asyncFuture();

		f = f.andThen(value -> throwNpe()) //
				.andThenOrOnError((value, error) -> {
					throw new IllegalStateException();
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
		assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void andFinally_WithValue() throws Exception {
		asyncFuture();
		f = f.andFinally(() -> atomicCounter.incrementAndGet());

		waitFor(f);
		assertThat(atomicCounter.get()).isEqualTo(1);
	}

	@Test
	public void andFinally_WithValue_Error() throws Exception {
		asyncFuture();
		f = f.andFinally(() -> throwNpe()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void andFinally_WithError() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.andFinally(() -> atomicCounter.incrementAndGet());

		waitFor(f);
		assertThat(atomicCounter.get()).isEqualTo(1);
	}

	@Test
	public void andFinally_WithError_Error() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.andFinally(() -> {
					throw new IllegalStateException();
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
		assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void onError_Error() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.onError(e -> {
					throw new IllegalStateException();
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
		assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void contextualizeError() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.contextualizeError(e -> new IllegalStateException()) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void contextualizeError_Error() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.contextualizeError(e -> {
					throw new IllegalStateException();
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(NullPointerException.class);
		assertThat(error.getSuppressed()[0]).isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void catchError() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.catchError(e -> VALUE.toLowerCase());

		waitFor(f);
		assertEventualValue(f, VALUE.toLowerCase());
	}

	@Test
	public void catchError_Error() throws Exception {
		asyncFuture();
		f = f.andThen(value -> throwNpe()) //
				.catchError(e -> {
					throw new IllegalStateException();
				}) //
				.onError(this::collectError);

		waitFor(f);
		assertThat(error).isInstanceOf(IllegalStateException.class);
	}

	// #######################################################
	// ## . . . . . . . . . . . Helpers . . . . . . . . . . ##
	// #######################################################

	private void syncFuture() {
		f = Future.fromSupplier(() -> VALUE);
	}

	private void asyncFuture() {
		f = Future.fromAsyncCallbackConsumer(gwtCallback -> provideValueAsync(VALUE, gwtCallback));
	}

	private <T> Future<T> asyncFuture(T value) {
		return Future.fromAsyncCallbackConsumer(gwtCallback -> provideValueAsync(value, gwtCallback));
	}

	private <T> void provideValueAsync(T value, AsyncCallback<T> gwtCallback) {
		doAsync(() -> {
			try {
				Thread.sleep(10);
				gwtCallback.onSuccess(value);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void doAsync(Runnable r) {
		new Thread(r).start();
	}

	private <T> void assertEventualValue(Future<T> f, T expected) {
		assertThat(waitForValue(f)).isEqualTo(expected);
	}

	private <T> T waitForValue(Future<T> future) {
		return Futures.waitForValue(future);
	}

	private void waitFor(Future<?> f) {
		Futures.waitFor(f);
	}

	private void assertIsValue(String result) {
		Assertions.assertThat(result).isEqualTo(VALUE);
	}

	private <T> T throwNpe() {
		throw new NullPointerException();
	}

	private void collectError(Throwable e) {
		error = e;
	}

}
