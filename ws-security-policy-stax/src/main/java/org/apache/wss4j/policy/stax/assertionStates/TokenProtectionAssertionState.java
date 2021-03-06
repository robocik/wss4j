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

import org.apache.neethi.Assertion;
import org.apache.wss4j.policy.AssertionState;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.common.WSSPolicyException;
import org.apache.wss4j.policy.model.AbstractSymmetricAsymmetricBinding;
import org.apache.wss4j.policy.stax.Assertable;
import org.apache.wss4j.policy.stax.DummyPolicyAsserter;
import org.apache.wss4j.policy.stax.PolicyAsserter;
import org.apache.wss4j.stax.ext.WSSConstants;
import org.apache.wss4j.stax.ext.WSSUtils;
import org.apache.wss4j.stax.securityToken.WSSecurityTokenConstants;
import org.apache.wss4j.stax.securityEvent.WSSecurityEventConstants;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.stax.securityEvent.SecurityEvent;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.xml.security.stax.securityEvent.SignedElementSecurityEvent;
import org.apache.xml.security.stax.securityEvent.TokenSecurityEvent;
import org.apache.xml.security.stax.securityToken.InboundSecurityToken;
import org.apache.xml.security.stax.securityToken.SecurityToken;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * WSP1.3, 6.5 Token Protection Property
 */
public class TokenProtectionAssertionState extends AssertionState implements Assertable {

    private final ArrayList<SignedElementSecurityEvent> signedElementEvents = new ArrayList<SignedElementSecurityEvent>();
    private final ArrayList<TokenSecurityEvent<? extends SecurityToken>> tokenSecurityEvents =
            new ArrayList<TokenSecurityEvent<? extends SecurityToken>>();
    private PolicyAsserter policyAsserter;

    public TokenProtectionAssertionState(Assertion assertion, 
                                         PolicyAsserter policyAsserter,
                                         boolean initialAssertionState) {
        super(assertion, initialAssertionState);
        
        this.policyAsserter = policyAsserter;
        if (this.policyAsserter == null) {
            this.policyAsserter = new DummyPolicyAsserter();
        }
        
        if (initialAssertionState) {
            String namespace = getAssertion().getName().getNamespaceURI();
            policyAsserter.assertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS));
        }
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                SecurityEventConstants.SignedElement,
                WSSecurityEventConstants.EncryptedKeyToken,
                WSSecurityEventConstants.IssuedToken,
                WSSecurityEventConstants.KerberosToken,
                SecurityEventConstants.KeyValueToken,
                WSSecurityEventConstants.RelToken,
                WSSecurityEventConstants.SamlToken,
                WSSecurityEventConstants.SecurityContextToken,
                WSSecurityEventConstants.UsernameToken,
                SecurityEventConstants.X509Token,
                WSSecurityEventConstants.Operation,
        };
    }

    @Override
    public boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException, XMLSecurityException {

        AbstractSymmetricAsymmetricBinding abstractSymmetricAsymmetricBinding = (AbstractSymmetricAsymmetricBinding) getAssertion();
        boolean protectTokens = abstractSymmetricAsymmetricBinding.isProtectTokens();
        String namespace = getAssertion().getName().getNamespaceURI();

        if (securityEvent instanceof SignedElementSecurityEvent) {
            SignedElementSecurityEvent signedElementSecurityEvent = (SignedElementSecurityEvent) securityEvent;
            if (signedElementSecurityEvent.isSigned()) {
                signedElementEvents.add(signedElementSecurityEvent);
            }
        } else if (securityEvent instanceof TokenSecurityEvent) {
            @SuppressWarnings("unchecked")
            TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent 
                = (TokenSecurityEvent<? extends SecurityToken>) securityEvent;
            tokenSecurityEvents.add(tokenSecurityEvent);
        } else { //Operation
            for (int i = 0; i < tokenSecurityEvents.size(); i++) {
                TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent = tokenSecurityEvents.get(i);

                SecurityToken securityToken = getEffectiveSignatureToken(tokenSecurityEvent.getSecurityToken());

                //a token can only be signed if it is included in the message:
                if (((InboundSecurityToken)securityToken).isIncludedInMessage() && isSignatureToken(securityToken)) {
                    //[WSP1.3_8.9]
                    boolean signsItsSignatureToken = signsItsSignatureToken(securityToken);
                    if (protectTokens && !signsItsSignatureToken) {
                        setAsserted(false);
                        setErrorMessage("Token " + WSSUtils.pathAsString(((InboundSecurityToken)securityToken).getElementPath()) + " must be signed by its signature.");
                        policyAsserter.unassertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS),
                                                      getErrorMessage());
                        return false;
                    } else if (!protectTokens && signsItsSignatureToken) {
                        setAsserted(false);
                        setErrorMessage("Token " + WSSUtils.pathAsString(((InboundSecurityToken)securityToken).getElementPath()) + " must not be signed by its signature.");
                        policyAsserter.unassertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS),
                                                      getErrorMessage());
                        return false;
                    }
                }

                if (isEndorsingToken(securityToken) && !signsMainSignature(securityToken)) {
                    //[WSP1.3_8.9b]
                    setAsserted(false);
                    setErrorMessage("Token " + WSSUtils.pathAsString(((InboundSecurityToken)securityToken).getElementPath()) + " must sign the main signature.");
                    policyAsserter.unassertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS),
                                                  getErrorMessage());
                    return false;
                }

                if (isMainSignatureToken(securityToken)
                        && !signsSignedSupportingTokens(securityToken)) {
                    setAsserted(false);
                    setErrorMessage("Main signature must sign the Signed*Supporting-Tokens.");
                    policyAsserter.unassertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS),
                                                  getErrorMessage());
                    return false;
                }
            }
        }
        
        policyAsserter.assertPolicy(new QName(namespace, SPConstants.PROTECT_TOKENS));
        return true;
    }

    private boolean isSignatureToken(SecurityToken securityToken) {
        List<WSSecurityTokenConstants.TokenUsage> tokenUsages = securityToken.getTokenUsages();
        for (int i = 0; i < tokenUsages.size(); i++) {
            WSSecurityTokenConstants.TokenUsage tokenUsage = tokenUsages.get(i);
            if (WSSecurityTokenConstants.TokenUsage_Signature.equals(tokenUsage)
                    || WSSecurityTokenConstants.TokenUsage_MainSignature.equals(tokenUsage)
                    || tokenUsage.getName().contains("Endorsing")) {
                return true;
            }
        }
        return false;
    }

    private boolean isEndorsingToken(SecurityToken securityToken) throws XMLSecurityException {
        SecurityToken rootToken = WSSUtils.getRootToken(securityToken);
        List<WSSecurityTokenConstants.TokenUsage> tokenUsages = rootToken.getTokenUsages();
        for (int i = 0; i < tokenUsages.size(); i++) {
            WSSecurityTokenConstants.TokenUsage tokenUsage = tokenUsages.get(i);
            if (tokenUsage.getName().contains("Endorsing")) {
                return true;
            }
        }
        return false;
    }

    private boolean isSignedSupportingToken(SecurityToken securityToken) throws XMLSecurityException {
        SecurityToken rootToken = WSSUtils.getRootToken(securityToken);
        List<WSSecurityTokenConstants.TokenUsage> tokenUsages = rootToken.getTokenUsages();
        for (int i = 0; i < tokenUsages.size(); i++) {
            WSSecurityTokenConstants.TokenUsage tokenUsage = tokenUsages.get(i);
            if (tokenUsage.getName().contains("Signed")) {
                return true;
            }
        }
        return false;
    }

    private boolean isMainSignatureToken(SecurityToken securityToken) throws XMLSecurityException {
        SecurityToken rootToken = WSSUtils.getRootToken(securityToken);
        List<WSSecurityTokenConstants.TokenUsage> tokenUsages = rootToken.getTokenUsages();
        return tokenUsages.contains(WSSecurityTokenConstants.TokenUsage_MainSignature);
    }

    private boolean signsMainSignature(SecurityToken securityToken) throws XMLSecurityException {

        List<QName> signaturePath = new LinkedList<QName>();
        signaturePath.addAll(WSSConstants.WSSE_SECURITY_HEADER_PATH);
        signaturePath.add(WSSConstants.TAG_dsig_Signature);

        for (int i = 0; i < signedElementEvents.size(); i++) {
            SignedElementSecurityEvent signedElementSecurityEvent = signedElementEvents.get(i);
            if (WSSUtils.pathMatches(signedElementSecurityEvent.getElementPath(), signaturePath, true, false)) {
                SecurityToken signingSecurityToken = getEffectiveSignatureToken(signedElementSecurityEvent.getSecurityToken());
                //todo ATM me just check if the token signs a signature but we don't know if it's the main signature
                if (signingSecurityToken != null && signingSecurityToken.getId().equals(securityToken.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean signsItsSignatureToken(SecurityToken securityToken) throws XMLSecurityException {
        for (int i = 0; i < signedElementEvents.size(); i++) {
            SignedElementSecurityEvent signedElementSecurityEvent = signedElementEvents.get(i);
            if (WSSUtils.pathMatches(signedElementSecurityEvent.getElementPath(), ((InboundSecurityToken)securityToken).getElementPath(), false, false)) {

                SecurityToken signingSecurityToken = signedElementSecurityEvent.getSecurityToken();
                signingSecurityToken = getEffectiveSignatureToken(signingSecurityToken);

                if (signingSecurityToken.getId().equals(securityToken.getId())) {
                    //ok we've found the correlating signedElementSecurityEvent. Now we have to find the Token that
                    //is covered by this signedElementSecurityEvent:
                    for (int j = 0; j < tokenSecurityEvents.size(); j++) {
                        TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent = tokenSecurityEvents.get(j);
                        SecurityToken st = getEffectiveSignatureToken(tokenSecurityEvent.getSecurityToken());

                        if (signedElementSecurityEvent.getXmlSecEvent() == ((InboundSecurityToken)st).getXMLSecEvent()) {
                            //...and we got the covered token
                            //next we have to see if the token is the same:
                            if (st.getId().equals(securityToken.getId())) { //NOPMD
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean signsSignedSupportingTokens(SecurityToken securityToken) throws XMLSecurityException {

        List<SecurityToken> signedSupportingTokens = new LinkedList<SecurityToken>();
        List<SignedElementSecurityEvent> signedElements = new LinkedList<SignedElementSecurityEvent>();

        for (int i = 0; i < tokenSecurityEvents.size(); i++) {
            TokenSecurityEvent<? extends SecurityToken> tokenSecurityEvent = tokenSecurityEvents.get(i);
            SecurityToken supportingToken = tokenSecurityEvent.getSecurityToken();
            if (isSignedSupportingToken(supportingToken)) {
                if (signedSupportingTokens.contains(supportingToken)) {
                    continue;
                }
                signedSupportingTokens.add(supportingToken);
                List<QName> elementPath = ((InboundSecurityToken)supportingToken).getElementPath();

                boolean found = false;
                for (int j = 0; j < signedElementEvents.size(); j++) {
                    SignedElementSecurityEvent signedElementSecurityEvent = signedElementEvents.get(j);
                    if (WSSUtils.pathMatches(signedElementSecurityEvent.getElementPath(), elementPath, false, false)) {
                        SecurityToken elementSignatureToken = getEffectiveSignatureToken(signedElementSecurityEvent.getSecurityToken());

                        if (elementSignatureToken != null && elementSignatureToken.getId().equals(securityToken.getId())) {
                            if (!signedElements.contains(signedElementSecurityEvent)) {
                                signedElements.add(signedElementSecurityEvent);
                            }
                            found = true;
                        }
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        if (signedSupportingTokens.size() > signedElements.size()) {
            return false;
        }

        return true;
    }

    private SecurityToken getEffectiveSignatureToken(SecurityToken securityToken) throws XMLSecurityException {
        SecurityToken tmp = WSSUtils.getRootToken(securityToken);
        List<? extends SecurityToken> wrappedTokens = tmp.getWrappedTokens();
        for (int i = 0; i < wrappedTokens.size(); i++) {
            SecurityToken token = wrappedTokens.get(i);
            if (isSignatureToken(token)) {
                //WSP 1.3, 6.5 [Token Protection] Property: Note that in cases where derived keys are used
                //the 'main' token, and NOT the derived key token, is covered by the signature.
                if (WSSecurityTokenConstants.DerivedKeyToken.equals(token.getTokenType())) {
                    return tmp;
                }
                tmp = token;
            }
        }
        return tmp;
    }
}
