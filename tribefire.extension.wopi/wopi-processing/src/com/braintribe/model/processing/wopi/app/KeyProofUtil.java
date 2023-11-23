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
package com.braintribe.model.processing.wopi.app;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.xml.bind.DatatypeConverter;

public class KeyProofUtil {

	/**
	 * @param strWopiProofKey
	 *            - Proof key from REST header
	 * @param expectedProofArray
	 *            - Byte Array from Specfication -- Contains querystring, time and accesskey combined by defined
	 *            algorithm in spec 4 bytes that represent the length, in bytes, of the access_token on the request. The
	 *            access_token. 4 bytes that represent the length, in bytes, of the full URL of the WOPI request,
	 *            including any query string parameters. The WOPI request URL in all uppercase. All query string
	 *            parameters on the request URL should be included. 4 bytes that represent the length, in bytes, of the
	 *            X-WOPI-TimeStamp value. The X-WOPI-TimeStamp value.
	 * @return
	 * @throws Exception
	 */
	public static boolean verifyProofKey(String strModulus, String strExponent, String strWopiProofKey, byte[] expectedProofArray) {
		try {
			PublicKey publicKey = getPublicKey(strModulus, strExponent);

			Signature verifier = Signature.getInstance("SHA256withRSA");
			verifier.initVerify(publicKey);
			verifier.update(expectedProofArray); // Or whatever interface specifies.

			final byte[] signedProof = DatatypeConverter.parseBase64Binary(strWopiProofKey);

			boolean verify = verifier.verify(signedProof);
			return verify;
		} catch (Exception e) {
			throw new IllegalStateException("Could not verify proof key", e);
		}
	}

	/**
	 * Gets a public RSA Key using WOPI Discovery Modulus and Exponent for PKI Signed Validation
	 *
	 * @param modulus
	 * @param exponent
	 * @return
	 * @throws Exception
	 */
	private static RSAPublicKey getPublicKey(String modulus, String exponent) throws Exception {
		BigInteger mod = new BigInteger(1, DatatypeConverter.parseBase64Binary(modulus));
		BigInteger exp = new BigInteger(1, DatatypeConverter.parseBase64Binary(exponent));
		KeyFactory factory = KeyFactory.getInstance("RSA");
		KeySpec ks = new RSAPublicKeySpec(mod, exp);

		return (RSAPublicKey) factory.generatePublic(ks);
	}

	/**
	 * Generates expected proof
	 *
	 * @param url
	 * @param accessToken
	 * @param timestampStr
	 * @return
	 */
	public static byte[] getExpectedProofBytes(String url, final String accessToken, final String timestampStr) {
		final byte[] accessTokenBytes = accessToken.getBytes(StandardCharsets.UTF_8);

		final byte[] hostUrlBytes = url.toUpperCase().getBytes(StandardCharsets.UTF_8);

		final Long timestamp = Long.valueOf(timestampStr);

		final ByteBuffer byteBuffer = ByteBuffer.allocate(4 + accessTokenBytes.length + 4 + hostUrlBytes.length + 4 + 8);
		byteBuffer.putInt(accessTokenBytes.length);
		byteBuffer.put(accessTokenBytes);
		byteBuffer.putInt(hostUrlBytes.length);
		byteBuffer.put(hostUrlBytes);
		byteBuffer.putInt(8);
		byteBuffer.putLong(timestamp);

		return byteBuffer.array();
	}

	//@formatter:off
	//TODO: starting point for the implementation
	// -----------------------------------------
//	// ------------------
//	// TODO:cleanup
//	WopiWacClient wopiWacClient = wopiWacConnector.wopiWacClient();
//	ProofKey proofKey = wopiWacClient.proofKey();
//	String exponent = proofKey.getExponent();
//	String modulus = proofKey.getModulus();
//	String value = proofKey.getValue();
//
//	String oldexponent = proofKey.getOldexponent();
//	String oldmodulus = proofKey.getOldmodulus();
//	String oldvalue = proofKey.getOldvalue();
//
//	// Enumeration<String> headerNames = request.getHeaderNames();
//	// while (headerNames.hasMoreElements()) {
//	// System.out.println(request.getHeader(headerNames.nextElement()));
//	// }
//	//
//	// System.out.println(request.getHeader(WopiHeader.Proof.key()));
//
//	String strWopiHeaderProofKey = "IflL8OWCOCmws5qnDD5kYMraMGI3o+T+hojoDREbjZSkxbbx7XIS1Av85lohPKjyksocpeVwqEYm9nVWfnq05uhDNGp2MsNyhPO9unZ6w25Rjs1hDFM0dmvYx8wlQBNZ/CFPaz3inCMaaP4PtU85YepaDccAjNc1gikdy3kSMeG1XZuaDixHvMKzF/60DMfLMBIu5xP4Nt8i8Gi2oZs4REuxi6yxOv2vQJQ5+8Wu2Olm8qZvT4FEIQT9oZAXebn/CxyvyQv+RVpoU2gb4BreXAdfKthWF67GpJyhr+ibEVDoIIolUvviycyEtjsaEBpOf6Ne/OLRNu98un7WNDzMTQ==";
//
//	String wopiProof = request.getWopiProof();
//	String strWopiRequest = request.getRequestURL().toString();
//	strWopiRequest = strWopiRequest + "?access_token=" + accessToken;
//	// String strWopiHeaderTimeStamp = "635655897610773532";
//	String strWopiHeaderTimeStamp = "0";
//
//	// https://contoso.com/wopi/files/RVQ29k8tf3h8cJ/Endy+aAMPy0iGhLatGNrhvKofPY9p2w
//
//	// byte[] expectedProofArray = getExpectedProofBytes( strWopiRequest, strAccessToken, strWopiHeaderTimeStamp
//	// );
//	// KeyProofUtil.verifyProofKey(strModulus, strExponent, strWopiProofKey, expectedProofArray);
//	byte[] expectedProofArray = KeyProofUtil.getExpectedProofBytes(strWopiRequest, accessToken, strWopiHeaderTimeStamp);
//	boolean verifyProofKey = KeyProofUtil.verifyProofKey(modulus, exponent, strWopiHeaderProofKey, expectedProofArray);
//	// if (!verifyProofKey) {
//	// // return WopiHttpStatusMessage.returnServerError(null);
//	// throw new IllegalStateException("Could not verify validity of Office Online request");
//	// }
//
//	System.out.println();
//
//	// ------------------
//	// -----------------------------------------
	//@formatter:on

}
