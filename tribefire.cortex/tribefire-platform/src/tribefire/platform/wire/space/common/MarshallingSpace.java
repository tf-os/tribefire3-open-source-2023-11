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
package tribefire.platform.wire.space.common;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.linkedMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import com.braintribe.cartridge.common.processing.RequiredTypeEnsurer;
import com.braintribe.codec.marshaller.api.ConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.jse.JseMarshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.url.UrlEncodingMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.marshallerdeployment.HardwiredMarshaller;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.api.util.Maps;

import tribefire.cortex.check.processing.CheckBundlesResponseHtmlMarshaller;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
@Managed
public class MarshallingSpace implements WireSpace {

	@Import
	private AccessServiceSpace accessService;

	@Import
	private CartridgeInformationSpace cartridgeInfo;

	@Managed
	public ConfigurableMarshallerRegistry registry() {
		BasicConfigurableMarshallerRegistry bean = new BasicConfigurableMarshallerRegistry();

		for (Entry<HardwiredMarshaller, Marshaller> entry : hardwiredMarshallers().entrySet()) {
			HardwiredMarshaller deployable = entry.getKey();
			Marshaller marshaller = entry.getValue();

			if (shouldRegisterMarshallers())
				for (String mimeType : deployable.getMimeTypes())
					bean.registerMarshaller(mimeType, marshaller);
		}

		return bean;
	}

	// This is meant to be overridden in cartridge-base
	protected boolean shouldRegisterMarshallers() {
		return true;
	}

	@Managed
	public Map<HardwiredMarshaller, Marshaller> hardwiredMarshallers() {
		return linkedMap( //
				marsh(xmlMarshaller(), "xml", "XML", "application/xml", "text/xml", "gm/xml"), //
				marsh(binMarshaller(), "bin", "Bin", "application/gm", "gm/bin"), //
				marsh(jsonMarshaller(), "json", "JSON", "application/json", "text/x-json", "gm/json"), //
				marsh(yamlMarshaller(), "yaml", "YAML", "text/yaml", "text/x-yaml", "application/x-yaml"), //
				marsh(jseMarshaller(), "jseh", "JSEH", "gm/jseh"), //
				marsh(jseScriptMarshaller(), "jse", "JSE", "gm/jse"), //
				marsh(urlEncodeingMarshaller(), "url", "URL", "application/x-www-form-urlencoded"), //
				marsh(checkBundlesResponseHtmlMarshaller(), "check-bundles-response", "Check Bundles Response",
						"text/html;spec=check-bundles-response") //
		);
	}

	private static Maps.Entry<HardwiredMarshaller, Marshaller> marsh(Marshaller marshaller, String type, String typeForName, String... mimeTypes) {
		return entry(hardwiredMarshaller(type, typeForName, mimeTypes), marshaller);
	}

	private static HardwiredMarshaller hardwiredMarshaller(String type, String typeForName, String... mimeTypes) {
		HardwiredMarshaller bean = HardwiredMarshaller.T.create();
		bean.setExternalId("marshaller." + type);
		bean.setGlobalId("hardwired:marshaller/" + bean.getExternalId());
		bean.setName(typeForName + " Marshaller");
		bean.setMimeTypes(asList(mimeTypes));

		return bean;
	}

	@Managed

	public StaxMarshaller xmlMarshaller() {
		StaxMarshaller bean = new StaxMarshaller();
		bean.setRequiredTypesReceiver(requiredTypeEnsurer());
		return bean;
	}

	@Managed

	public JsonStreamMarshaller jsonMarshaller() {
		JsonStreamMarshaller bean = new JsonStreamMarshaller();
		// TODO: Must have the required type receiver
		return bean;
	}

	@Managed

	public YamlMarshaller yamlMarshaller() {
		YamlMarshaller bean = new YamlMarshaller();
		return bean;
	}

	@Managed

	public Marshaller binMarshaller() {
		Bin2Marshaller bean = new Bin2Marshaller();
		bean.setRequiredTypesReceiver(requiredTypeEnsurer());
		return bean;
	}

	@Managed

	public ManMarshaller manMarshaller() {
		ManMarshaller bean = new ManMarshaller();
		return bean;
	}

	@Managed
	public Marshaller jseMarshaller() {
		JseMarshaller bean = new JseMarshaller();
		bean.setHostedMode(true);
		return bean;
	}

	@Managed
	public Marshaller jseScriptMarshaller() {
		JseMarshaller bean = new JseMarshaller();
		bean.setHostedMode(false);
		return bean;
	}

	@Managed
	public Marshaller urlEncodeingMarshaller() {
		UrlEncodingMarshaller bean = new UrlEncodingMarshaller();
		return bean;
	}

	@Managed
	public Marshaller checkBundlesResponseHtmlMarshaller() {
		return new CheckBundlesResponseHtmlMarshaller();
	}

	@Managed
	public Consumer<Set<String>> requiredTypeEnsurer() {
		return new Consumer<Set<String>>() {

			Consumer<Set<String>> delegate;

			@Override
			public void accept(Set<String> object) throws RuntimeException {
				if (delegate == null) {
					delegate = rawRequiredTypeEnsurer();
				}
				delegate.accept(object);
			}

		};
	}

	@Managed
	public RequiredTypeEnsurer rawRequiredTypeEnsurer() {
		RequiredTypeEnsurer bean = new RequiredTypeEnsurer();
		bean.setAccessService(accessService.service());
		bean.setErrorIsFatal(false);
		return bean;
	}

}
