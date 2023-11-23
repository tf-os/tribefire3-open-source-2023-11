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
package tribefire.cortex.assets.cortex.initializer.wire.space;

import com.braintribe.model.crypto.configuration.encryption.AsymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.encryption.SymmetricEncryptionConfiguration;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingFormat;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.cortex.initializer.wire.contract.CortexInitializerContract;
import tribefire.cortex.assets.cortex.initializer.wire.contract.CortexLookupContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;

@Managed
public class CortexInitializerSpace extends AbstractInitializerSpace implements CortexInitializerContract {

	@Import
	private CortexLookupContract cortexLookup;

	@Override
	public void initialize() {
		hashingConfiguration();
		symmetricEncryptionConfiguration();
		asymmetricEncryptionConfiguration();

		cortexLookup.cortexConfiguration().setAsymmetricEncryptionConfiguration(asymmetricEncryptionConfiguration());
		cortexLookup.cortexConfiguration().setHashingConfiguration(hashingConfiguration());
		cortexLookup.cortexConfiguration().setSymmetricEncryptionConfiguration(symmetricEncryptionConfiguration());
	}

	@Managed
	private HashingConfiguration hashingConfiguration() {
		HashingConfiguration bean = session().createRaw(HashingConfiguration.T, "e6cae2c8-2b5c-43dd-93c5-4b6791724fe8");
		bean.setAlgorithm("SHA-256");
		bean.setEnableRandomSalt(Boolean.TRUE);
		bean.setRandomSaltSize(16);
		return bean;
	}

	@Managed
	private SymmetricEncryptionConfiguration symmetricEncryptionConfiguration() {
		SymmetricEncryptionConfiguration bean = session().createRaw(SymmetricEncryptionConfiguration.T, "c12dd4fe-b9db-4558-84ba-c382fca612a6");
		bean.setAlgorithm("AES");
		bean.setSymmetricEncryptionToken(encodedSecretKey());
		return bean;
	}

	// Managed
	private EncodedSecretKey encodedSecretKey() {
		EncodedSecretKey bean = session().createRaw(EncodedSecretKey.T, "71979a91-fa06-4d7e-b3c2-cb1708f2c7e5");
		bean.setEncodedKey("M6HO/sLbBk3fV+xhYrMlZA==");
		bean.setEncodingFormat(KeyEncodingFormat.raw);
		bean.setEncodingStringFormat(KeyEncodingStringFormat.base64);
		bean.setKeyAlgorithm("AES");
		bean.setKeySize(128);
		return bean;
	}

	@Managed
	private AsymmetricEncryptionConfiguration asymmetricEncryptionConfiguration() {
		AsymmetricEncryptionConfiguration bean = session().createRaw(AsymmetricEncryptionConfiguration.T, "7bb511fe-bb72-4103-927e-d9b1788eec8d");
		bean.setAlgorithm("RSA");
		bean.setAsymmetricEncryptionToken(encodedKeyPair());
		return bean;
	}

	// Managed
	private EncodedKeyPair encodedKeyPair() {
		EncodedKeyPair bean = session().createRaw(EncodedKeyPair.T, "4383cbdf-fa70-4497-bf7b-c12020e4877a");
		bean.setKeyAlgorithm("RSA");
		bean.setKeySize(2048);
		bean.setPrivateKey(encodedPrivateKey());
		bean.setPublicKey(encodedPublicKey());
		return bean;
	}

	// Managed
	private EncodedPrivateKey encodedPrivateKey() {
		EncodedPrivateKey bean = session().createRaw(EncodedPrivateKey.T, "e89b4cb6-304e-4156-9d8d-6707a773e9c2");
		bean.setEncodedKey(
				"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCEYTu/QVaC0ijFgVTZMBmSsfVtik4JMbmj2kq6JKodj8lnqIM9mKiTXvg/QtioIbyeyktw1Jkaw9IWCU9YVu12P81qRbO7SVl70p4X6+3JiNvLvhm88+2vaAfndjmdNJ5wuKsxOI4LlNxbwInlYJxMMGg7rraBntEQ/E63qQsAkUEqYNzciqBd/fs9Hjx/PTsipAFuP22itjsBLNLBZ8Y3xt7hJdGOjXZbFXVvIVv5UcTQRSq1kj3hQA1+0z06VdS4c2rVcqWNod2brAYs/hA+elJ+/XDH8KEqVttDQswpfkPwBptqufM5+1SU2OhqOiaUqg1mg8J21Ml99DLhYR2jAgMBAAECggEAZIBZVtMo9brN31xX6We+EoPuu1ID+rD4qjABCZ7qU/UkyADEWpA6c1/ngkeiqsqqI7ebXLba4doG9lntFjkwoNTBg8wVLrv6Jqp5AdpJNfckP2M8sRpTuhZ14cD87p/TJerddUnldV93iGT9i9oz7xfVGnKC9pd3S7jxnjfFNZDyxfKdSqlupxI1sY0weSvHALzDEbF7TSujOKwxSzcBa3xA0gPjiby1YZ2UFHlCKqoOu7JNhoz9+10RCmkO1sjz2jXqB2VE+ak/+yMzgrP86rvaDQO/orzt85pfLopefVeHLdok4U8m6/UfDVkVwGB/rN9OHXdbe5+fPLw5Tdp9gQKBgQDmt8Vakom6AgPzYvfrhyNwM4/dasL7LnWQ5LOYjCEPFkwiWEqT+kEytyteYzLlLTe0YhprofoWoClQy7hQ6sL7mi07uB7W8wEtnHN16lX+O/Ors3R7QpPjfehByGZheg7zKPjUwoah9HmMq9ojG/7nZ/9aJj81mAcEGXWSjKmcQQKBgQCS4tPiQEE38G1SFVXsv7U72mv/BGd+7Yp9uh7l8WDkE6qN8s7frly+ZKDmpTSi5GLUs2qLvlc2f2hJYKcTzmjIYUqvU5UD+xV0ayavizORvGx1WFmlP3zFfdXYLg+MuOwpUnYvG45R054TCcEJuBZ1T3wLIedIhXzOhgIXZVeQ4wKBgQCChB8+t3r2IMG/Y5NAR/iCNokCBq06jvOu/dGjUFI7SK/VFgFKaN2NJRWBlbhq8QN8JaswjDb/qab1r+kazVN0JanFCMlZa6qU9NZUNDZlVeDoluIkAGvM26MR454XModWgy0QnuKup7BDJcyG5AV1Pt2zT8OeO9xsM0LRnoUsgQKBgCPC/ftT0Y0hCDy33RerBSDxaOHJ3LjWdKVvrx4kyoY9E4VD3IIxHXttXI9LIamXGUjX9dTYHruyAV38HjGAS5qdtWVCYEF73BlgDScoKQIOcgmP3SOSdXpPzMS5UifczgKxhPyrJNdfQlk4QrtvCmi9VWIQexL5DQ3rKo8vqUYpAoGBAKFFqISgBALRQnu9fI9pXEIgvzPDBkC8GVsKio7I3SP24aweKHRAPmxGanZ1l00KzmRwmCk6LruIAz+mj6g1Bv8LTu3LZEIvgIGkWJlVigbYfCIWLY1DnMDQEc/2Lzdwe8+nVWfpIG2fUwDBh3Vy6qy2N+CoQuHkd8sr/wuNvoN3");
		bean.setEncodingFormat(KeyEncodingFormat.pkcs8);
		bean.setEncodingStringFormat(KeyEncodingStringFormat.base64);
		bean.setKeyAlgorithm("RSA");
		return bean;
	}

	// Managed
	private EncodedPublicKey encodedPublicKey() {
		EncodedPublicKey bean = session().createRaw(EncodedPublicKey.T, "e0c5ec52-6163-4196-b00e-33f268adadde");
		bean.setEncodedKey(
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhGE7v0FWgtIoxYFU2TAZkrH1bYpOCTG5o9pKuiSqHY/JZ6iDPZiok174P0LYqCG8nspLcNSZGsPSFglPWFbtdj/NakWzu0lZe9KeF+vtyYjby74ZvPPtr2gH53Y5nTSecLirMTiOC5TcW8CJ5WCcTDBoO662gZ7REPxOt6kLAJFBKmDc3IqgXf37PR48fz07IqQBbj9torY7ASzSwWfGN8be4SXRjo12WxV1byFb+VHE0EUqtZI94UANftM9OlXUuHNq1XKljaHdm6wGLP4QPnpSfv1wx/ChKlbbQ0LMKX5D8AabarnzOftUlNjoajomlKoNZoPCdtTJffQy4WEdowIDAQAB");
		bean.setEncodingFormat(KeyEncodingFormat.x509);
		bean.setEncodingStringFormat(KeyEncodingStringFormat.base64);
		bean.setKeyAlgorithm("RSA");
		return bean;
	}

}