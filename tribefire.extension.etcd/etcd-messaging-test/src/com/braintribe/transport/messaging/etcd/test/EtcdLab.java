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
package com.braintribe.transport.messaging.etcd.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.transport.messaging.etcd.EtcdConnection;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.options.WatchOption.Builder;
import io.etcd.jetcd.watch.WatchEvent;

@Category(SpecialEnvironment.class)
public class EtcdLab {

	@Test
	public void testBlockingWatcher() throws Exception {

		List<String> endpointUrls = new ArrayList<>();
		endpointUrls.add("http://127.0.0.1:2379");

		List<URI> endpointUris = endpointUrls.stream().map(u -> {
			try {
				return new URI(u);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList());

		Client client = Client.builder().maxInboundMessageSize((int) Numbers.MEGABYTE * 100).endpoints(endpointUris).build();
		KV kvClient = client.getKVClient();

		ByteSequence bsKey = ByteSequence.from("foo", StandardCharsets.UTF_8);
		ByteSequence bsValue = ByteSequence.from("bar", StandardCharsets.UTF_8);

		Builder watchBuilder = WatchOption.newBuilder().withNoDelete(true);
		watchBuilder.withRange(EtcdConnection.getRangeEnd("foo")).withProgressNotify(false);
		WatchOption option = watchBuilder.build();

		Watch watch = client.getWatchClient();
		watch.watch(bsKey, option, response -> {

			for (WatchEvent event : response.getEvents()) {
				String stringKey = Optional.ofNullable(event.getKeyValue().getKey()).map(k -> k.toString(StandardCharsets.UTF_8)).orElse("");
				System.out.println("Received event for " + stringKey);
			}
		});

		kvClient.put(bsKey, bsValue).get();
		kvClient.get(bsKey).get();

	}

}
