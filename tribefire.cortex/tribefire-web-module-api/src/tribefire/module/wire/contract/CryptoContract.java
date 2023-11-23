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
package tribefire.module.wire.contract;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.braintribe.codec.Codec;
import com.braintribe.model.crypto.key.encoded.EncodedKeyPair;
import com.braintribe.model.crypto.key.encoded.EncodedPrivateKey;
import com.braintribe.model.crypto.key.encoded.EncodedPublicKey;
import com.braintribe.model.crypto.key.encoded.EncodedSecretKey;
import com.braintribe.model.crypto.key.encoded.KeyEncodingStringFormat;
import com.braintribe.model.crypto.key.keystore.KeyStoreCertificate;
import com.braintribe.model.crypto.key.keystore.KeyStoreKeyPair;
import com.braintribe.model.crypto.key.keystore.KeyStoreSecretKey;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.crypto.token.KeyCodecProvider;
import com.braintribe.model.processing.crypto.token.loader.CertificateLoader;
import com.braintribe.model.processing.crypto.token.loader.EncryptionTokenLoader;
import com.braintribe.model.processing.crypto.token.loader.KeyPairLoader;
import com.braintribe.model.processing.crypto.token.loader.PrivateKeyLoader;
import com.braintribe.model.processing.crypto.token.loader.PublicKeyLoader;
import com.braintribe.model.processing.crypto.token.loader.SecretKeyLoader;
import com.braintribe.wire.api.space.WireSpace;

public interface CryptoContract extends WireSpace {

	/* ========== Key Loaders ========== */

	GmExpertRegistry keyLoaderExpertRegistry();

	Map<Class<?>, EncryptionTokenLoader<?, ?>> keyLoaderExpertMap();

	KeyPairLoader<EncodedKeyPair> encodedKeyPairLoader();

	PublicKeyLoader<EncodedPublicKey, PublicKey> encodedPublicKeyLoader();

	PrivateKeyLoader<EncodedPrivateKey, PrivateKey> encodedPrivateKeyLoader();

	SecretKeyLoader<EncodedSecretKey, SecretKey> encodedSecretKeyLoader();

	KeyPairLoader<KeyStoreKeyPair> keyStoreKeyPairLoader();

	CertificateLoader<KeyStoreCertificate, java.security.cert.Certificate> keyStoreCertificateLoader();

	SecretKeyLoader<KeyStoreSecretKey, javax.crypto.SecretKey> keyStoreSecretKeyLoader();

	Map<KeyEncodingStringFormat, Codec<byte[], String>> keyStringCodecs();

	/* ========== Key Codecs ========== */

	KeyCodecProvider<PublicKey> publicKeyCodecProvider();

	KeyCodecProvider<PrivateKey> privateKeyCodecProvider();

	KeyCodecProvider<SecretKey> secretKeyCodecProvider();

	GmExpertRegistry keyCodecProviderRegistry();

	/* ========== Commons ========== */

	Codec<byte[], String> base64Codec();

	Codec<byte[], String> hexCodec();

	Function<byte[], String> fingerprintFunction();

}
