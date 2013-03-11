/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wss4j.policy.stax.assertionStates;

import org.apache.wss4j.policy.WSSPolicyException;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.AbstractToken;
import org.apache.wss4j.policy.model.X509Token;
import org.apache.wss4j.stax.ext.WSSConstants;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.stax.ext.XMLSecurityConstants;
import org.apache.xml.security.stax.impl.securityToken.AbstractInboundSecurityToken;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.TokenSecurityEvent;
import org.apache.xml.security.stax.securityEvent.X509TokenSecurityEvent;

import java.security.cert.X509Certificate;

/**
 * WSP1.3, 5.4.3 X509Token Assertion
 */

public class X509TokenAssertionState extends TokenAssertionState {

    public X509TokenAssertionState(AbstractSecurityAssertion assertion, boolean asserted) {
        super(assertion, asserted);
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                SecurityEventConstants.X509Token
        };
    }

    @Override
    public boolean assertToken(TokenSecurityEvent tokenSecurityEvent, AbstractToken abstractToken) throws WSSPolicyException, XMLSecurityException {
        if (!(tokenSecurityEvent instanceof X509TokenSecurityEvent)) {
            throw new WSSPolicyException("Expected a X509TokenSecurityEvent but got " + tokenSecurityEvent.getClass().getName());
        }

        X509Token x509Token = (X509Token) abstractToken;

        AbstractInboundSecurityToken securityToken = (AbstractInboundSecurityToken) tokenSecurityEvent.getSecurityToken();
        XMLSecurityConstants.TokenType tokenType = securityToken.getTokenType();
        if (!(WSSConstants.X509V3Token.equals(tokenType)
                || WSSConstants.X509V1Token.equals(tokenType)
                || WSSConstants.X509Pkcs7Token.equals(tokenType)
                || WSSConstants.X509PkiPathV1Token.equals(tokenType))) {
            throw new WSSPolicyException("Invalid Token for this assertion");
        }

        try {
            X509Certificate x509Certificate = securityToken.getX509Certificates()[0];
            if (x509Token.getIssuerName() != null) {
                final String certificateIssuerName = x509Certificate.getIssuerX500Principal().getName();
                if (!x509Token.getIssuerName().equals(certificateIssuerName)) {
                    setErrorMessage("IssuerName in Policy (" + x509Token.getIssuerName() + ") didn't match with the one in the certificate (" + certificateIssuerName + ")");
                    return false;
                }
            }
            if (x509Token.isRequireKeyIdentifierReference() && securityToken.getKeyIdentifierType() != WSSConstants.WSSKeyIdentifierType.X509_KEY_IDENTIFIER) {
                setErrorMessage("Policy enforces KeyIdentifierReference but we got " + securityToken.getKeyIdentifierType());
                return false;
            } else if (x509Token.isRequireIssuerSerialReference() && securityToken.getKeyIdentifierType() != WSSConstants.WSSKeyIdentifierType.ISSUER_SERIAL) {
                setErrorMessage("Policy enforces IssuerSerialReference but we got " + securityToken.getKeyIdentifierType());
                return false;
            } else if (x509Token.isRequireEmbeddedTokenReference() && securityToken.getKeyIdentifierType() != WSSConstants.WSSKeyIdentifierType.SECURITY_TOKEN_DIRECT_REFERENCE) {
                setErrorMessage("Policy enforces EmbeddedTokenReference but we got " + securityToken.getKeyIdentifierType());
                return false;
            } else if (x509Token.isRequireThumbprintReference() && securityToken.getKeyIdentifierType() != WSSConstants.WSSKeyIdentifierType.THUMBPRINT_IDENTIFIER) {
                setErrorMessage("Policy enforces ThumbprintReference but we got " + securityToken.getKeyIdentifierType());
                return false;
            }
            if (x509Certificate.getVersion() == 2) {
                setErrorMessage("X509Certificate Version " + x509Certificate.getVersion() + " not supported");
                return false;
            }
            if (x509Token.getTokenType() != null) {
                switch (x509Token.getTokenType()) {
                    case WssX509V3Token10:
                    case WssX509V3Token11:
                        if (WSSConstants.X509V3Token != securityToken.getTokenType() || x509Certificate.getVersion() != 3) {
                            setErrorMessage("X509Certificate Version " + x509Certificate.getVersion() + " mismatch; Policy enforces " + x509Token.getTokenType());
                            return false;
                        }
                        break;
                    case WssX509V1Token11:
                        if (WSSConstants.X509V1Token != securityToken.getTokenType() || x509Certificate.getVersion() != 1) {
                            setErrorMessage("X509Certificate Version " + x509Certificate.getVersion() + " mismatch; Policy enforces " + x509Token.getTokenType());
                            return false;
                        }
                        break;
                    case WssX509PkiPathV1Token10:
                    case WssX509PkiPathV1Token11:
                        if (securityToken.getTokenType() != WSSConstants.X509PkiPathV1Token) {
                            setErrorMessage("Policy enforces " + x509Token.getTokenType() + " but we got " + securityToken.getTokenType());
                            return false;
                        }
                        break;
                    case WssX509Pkcs7Token10:
                    case WssX509Pkcs7Token11:
                        setErrorMessage("Unsupported token type: " + securityToken.getTokenType());
                        return false;
                }
            }
        } catch (XMLSecurityException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        //always return true to prevent false alarm in case additional tokens with the same usage
        //appears in the message but do not fulfill the policy and are also not needed to fulfil the policy.
        return true;
    }
}
