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
package org.apache.wss4j.policy.stax.test;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SAMLCallback;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.common.saml.bean.SubjectBean;
import org.apache.wss4j.common.WSSPolicyException;
import org.apache.wss4j.stax.securityToken.WSSecurityTokenConstants;
import org.apache.wss4j.stax.impl.securityToken.*;
import org.apache.xml.security.stax.impl.util.IDGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.common.SAMLVersion;
import org.apache.wss4j.policy.stax.PolicyEnforcer;
import org.apache.wss4j.stax.ext.WSSConstants;
import org.apache.wss4j.stax.securityEvent.*;

import javax.xml.namespace.QName;

import java.util.Date;

public class SupportingTokensTest extends AbstractPolicyTestBase {

    @Test
    public void testSupportingTokenPolicy() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSupportingTokenPolicyNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);
        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V1Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertEquals(e.getMessage(),
                    "X509Certificate Version 3 mismatch; Policy enforces WssX509V3Token11");
            Assert.assertEquals(e.getFaultCode(), WSSecurityException.INVALID_SECURITY);
        }
    }

    @Test
    public void testX509SupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        x509TokenSecurityEvent = new X509TokenSecurityEvent();
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testX509SupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V1Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        x509TokenSecurityEvent = new X509TokenSecurityEvent();
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V1Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testX509SupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        x509TokenSecurityEvent = new X509TokenSecurityEvent();
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V1Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testX509SupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:X509Token>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RequireThumbprintReference/>\n" +
                        "               <sp:WssX509V3Token11/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:X509Token>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        X509TokenSecurityEvent x509TokenSecurityEvent = new X509TokenSecurityEvent();
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V1Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        x509TokenSecurityEvent = new X509TokenSecurityEvent();
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        x509TokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(x509TokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testUsernameSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:UsernameToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:NoPassword/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:UsernameToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        UsernameTokenSecurityEvent usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        UsernameSecurityTokenImpl usernameSecurityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_NONE,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        usernameSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(usernameSecurityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        usernameSecurityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_NONE,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        usernameSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(usernameSecurityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testUsernameSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:UsernameToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:NoPassword/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:UsernameToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        UsernameTokenSecurityEvent usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        UsernameSecurityTokenImpl securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_DIGEST,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_DIGEST,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testUsernameSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:UsernameToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:NoPassword/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:UsernameToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        UsernameTokenSecurityEvent usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        UsernameSecurityTokenImpl securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_NONE,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_DIGEST,
                "username", "password", new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testUsernameSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:UsernameToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:NoPassword/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:UsernameToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        UsernameTokenSecurityEvent usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        UsernameSecurityTokenImpl securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_DIGEST,
                "username", "password", new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        usernameTokenSecurityEvent = new UsernameTokenSecurityEvent();
        usernameTokenSecurityEvent.setUsernameTokenProfile(WSSConstants.NS_USERNAMETOKEN_PROFILE11);
        securityToken = new UsernameSecurityTokenImpl(
                WSSConstants.UsernameTokenPasswordType.PASSWORD_NONE,
                "username", null, new Date().toString(), null, new byte[10], 10L,
                null, IDGenerator.generateID(null), WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference);
        securityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        usernameTokenSecurityEvent.setSecurityToken(securityToken);
        policyEnforcer.registerSecurityEvent(usernameTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testIssuedSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:IssuedToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <sp:RequestSecurityTokenTemplate/>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:IssuedToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testIssuedSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:IssuedToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <sp:RequestSecurityTokenTemplate/>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:IssuedToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testIssuedSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:IssuedToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <sp:RequestSecurityTokenTemplate/>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:IssuedToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testIssuedSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:IssuedToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <sp:RequestSecurityTokenTemplate/>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:IssuedToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        securityContextTokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        securityContextTokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        securityContextTokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(securityContextTokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKerberosSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KerberosToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:KerberosToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KerberosTokenSecurityEvent tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        KerberosServiceSecurityTokenImpl kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKerberosSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KerberosToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:KerberosToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KerberosTokenSecurityEvent tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        KerberosServiceSecurityTokenImpl kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testKerberosSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KerberosToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:KerberosToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KerberosTokenSecurityEvent tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        KerberosServiceSecurityTokenImpl kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKerberosSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KerberosToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:KerberosToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KerberosTokenSecurityEvent tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        KerberosServiceSecurityTokenImpl kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KerberosTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        kerberosServiceSecurityToken = getKerberosServiceSecurityToken(WSSecurityTokenConstants.KerberosToken);
        kerberosServiceSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(kerberosServiceSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSpnegoSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SpnegoContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SpnegoContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSpnegoSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SpnegoContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SpnegoContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testSpnegoSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SpnegoContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SpnegoContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSpnegoSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SpnegoContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SpnegoContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecurityContextTokenSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecurityContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecurityContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecurityContextTokenSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecurityContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecurityContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testSecurityContextTokenSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecurityContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecurityContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecurityContextTokenSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecurityContextToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecurityContextToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecureConversationTokenSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecureConversationToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecureConversationToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecureConversationTokenSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecureConversationToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecureConversationToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testSecureConversationTokenSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecureConversationToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecureConversationToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSecureConversationTokenSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SecureConversationToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SecureConversationToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SecurityContextTokenSecurityEvent tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SecurityContextTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSamlTokenSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SamlToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SamlToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SAMLCallback samlCallback = new SAMLCallback();
        samlCallback.setSamlVersion(SAMLVersion.VERSION_20);
        samlCallback.setIssuer("CN=transmitter,OU=swssf,C=CH");
        SubjectBean subjectBean = new SubjectBean();
        samlCallback.setSubject(subjectBean);
        SamlAssertionWrapper samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        SamlTokenSecurityEvent tokenSecurityEvent = new SamlTokenSecurityEvent();
        SamlSecurityTokenImpl samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SamlTokenSecurityEvent();
        samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSamlTokenSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SamlToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SamlToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SAMLCallback samlCallback = new SAMLCallback();
        samlCallback.setSamlVersion(SAMLVersion.VERSION_20);
        samlCallback.setIssuer("xs:anyURI");
        SubjectBean subjectBean = new SubjectBean();
        samlCallback.setSubject(subjectBean);
        SamlAssertionWrapper samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        SamlTokenSecurityEvent tokenSecurityEvent = new SamlTokenSecurityEvent();
        SamlSecurityTokenImpl samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new SamlTokenSecurityEvent();
        samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testSamlTokenSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SamlToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SamlToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SAMLCallback samlCallback = new SAMLCallback();
        samlCallback.setSamlVersion(SAMLVersion.VERSION_20);
        samlCallback.setIssuer("CN=transmitter,OU=swssf,C=CH");
        SubjectBean subjectBean = new SubjectBean();
        samlCallback.setSubject(subjectBean);
        SamlAssertionWrapper samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        SamlTokenSecurityEvent tokenSecurityEvent = new SamlTokenSecurityEvent();
        SamlSecurityTokenImpl samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        samlCallback.setIssuer("xs:anyURI");
        samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        tokenSecurityEvent = new SamlTokenSecurityEvent();
        samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testSamlTokenSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:SamlToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "           <wsp:Policy/>\n" +
                        "       </sp:SamlToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        SAMLCallback samlCallback = new SAMLCallback();
        samlCallback.setSamlVersion(SAMLVersion.VERSION_20);
        samlCallback.setIssuer("xs:anyURI");
        SubjectBean subjectBean = new SubjectBean();
        samlCallback.setSubject(subjectBean);
        SamlAssertionWrapper samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        SamlTokenSecurityEvent tokenSecurityEvent = new SamlTokenSecurityEvent();
        SamlSecurityTokenImpl samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        samlCallback.setIssuer("CN=transmitter,OU=swssf,C=CH");
        samlAssertionWrapper = createSamlAssertionWrapper(samlCallback);

        tokenSecurityEvent = new SamlTokenSecurityEvent();
        samlSecurityToken =
            new SamlSecurityTokenImpl(
                    samlAssertionWrapper, getX509Token(WSSecurityTokenConstants.X509V3Token), null, null,
                    WSSecurityTokenConstants.KeyIdentifier_SecurityTokenDirectReference, null);
        samlSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(samlSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testRelTokenSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:RelToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "       </sp:RelToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        RelTokenSecurityEvent tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testRelTokenSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:RelToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "       </sp:RelToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        RelTokenSecurityEvent tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testRelTokenSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:RelToken>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "       </sp:RelToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        RelTokenSecurityEvent tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testRelTokenSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:RelTokenSecurityEvent>\n" +
                        "           <sp:IssuerName>CN=transmitter,OU=swssf,C=CH</sp:IssuerName>\n" +
                        "       </sp:RelTokenSecurityEvent>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        RelTokenSecurityEvent tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("test");
        X509SecurityTokenImpl x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new RelTokenSecurityEvent();
        tokenSecurityEvent.setIssuerName("CN=transmitter,OU=swssf,C=CH");
        x509SecurityToken = getX509Token(WSSecurityTokenConstants.X509V3Token);
        x509SecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(x509SecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKeyValueTokenSupportingTokenPolicyAdditionalToken() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KeyValueToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RsaKeyValue/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:KeyValueToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KeyValueTokenSecurityEvent tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        RsaKeyValueSecurityTokenImpl rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKeyValueTokenSupportingTokenPolicyAdditionalTokenNegative() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KeyValueToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RsaKeyValue/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:KeyValueToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KeyValueTokenSecurityEvent tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        DsaKeyValueSecurityTokenImpl dsaKeyValueSecurityToken = getDsaKeyValueSecurityToken();
        dsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(dsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        dsaKeyValueSecurityToken = getDsaKeyValueSecurityToken();
        dsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(dsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));

        try {
            policyEnforcer.registerSecurityEvent(operationSecurityEvent);
            Assert.fail("Exception expected");
        } catch (WSSecurityException e) {
            Assert.assertTrue(e.getCause() instanceof WSSPolicyException);
        }
    }

    @Test
    public void testKeyValueTokenSupportingTokenPolicyAdditionalTokenLastIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KeyValueToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RsaKeyValue/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:KeyValueToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KeyValueTokenSecurityEvent tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        RsaKeyValueSecurityTokenImpl rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }

    @Test
    public void testKeyValueTokenSupportingTokenPolicyAdditionalTokenFirstIgnore() throws Exception {
        String policyString =
                "<sp:SupportingTokens xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sp3=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200802\">\n" +
                        "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "       <sp:KeyValueToken>\n" +
                        "           <wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">\n" +
                        "               <sp:RsaKeyValue/>\n" +
                        "           </wsp:Policy>\n" +
                        "       </sp:KeyValueToken>\n" +
                        "   </wsp:Policy>\n" +
                        "</sp:SupportingTokens>";

        PolicyEnforcer policyEnforcer = buildAndStartPolicyEngine(policyString);

        KeyValueTokenSecurityEvent tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        RsaKeyValueSecurityTokenImpl rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        tokenSecurityEvent = new KeyValueTokenSecurityEvent();
        rsaKeyValueSecurityToken = getRsaKeyValueSecurityToken();
        rsaKeyValueSecurityToken.addTokenUsage(WSSecurityTokenConstants.TokenUsage_SupportingTokens);
        tokenSecurityEvent.setSecurityToken(rsaKeyValueSecurityToken);
        policyEnforcer.registerSecurityEvent(tokenSecurityEvent);

        OperationSecurityEvent operationSecurityEvent = new OperationSecurityEvent();
        operationSecurityEvent.setOperation(new QName("definitions"));
        policyEnforcer.registerSecurityEvent(operationSecurityEvent);

        policyEnforcer.doFinal();
    }
}
