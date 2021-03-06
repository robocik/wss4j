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

package org.apache.wss4j.dom.saml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.saml.SAMLCallback;
import org.apache.wss4j.common.saml.SAMLUtil;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.apache.wss4j.common.saml.bean.AudienceRestrictionBean;
import org.apache.wss4j.common.saml.bean.ConditionsBean;
import org.apache.wss4j.common.saml.bean.ProxyRestrictionBean;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSSConfig;
import org.apache.wss4j.dom.WSSecurityEngine;
import org.apache.wss4j.dom.WSSecurityEngineResult;
import org.apache.wss4j.dom.common.CustomSamlAssertionValidator;
import org.apache.wss4j.dom.common.SAML1CallbackHandler;
import org.apache.wss4j.dom.common.SAML2CallbackHandler;
import org.apache.wss4j.dom.common.SOAPUtil;
import org.apache.wss4j.dom.common.SecurityTestUtil;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSAMLToken;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.joda.time.DateTime;
import org.w3c.dom.Document;

/**
 * Test-case for sending and processing an a SAML Token with a custom Conditions element.
 */
public class SamlConditionsTest extends org.junit.Assert {
    private static final org.slf4j.Logger LOG = 
        org.slf4j.LoggerFactory.getLogger(SamlConditionsTest.class);
    private WSSecurityEngine secEngine = new WSSecurityEngine();
    
    @org.junit.AfterClass
    public static void cleanup() throws Exception {
        SecurityTestUtil.cleanup();
    }

    public SamlConditionsTest() {
        WSSConfig config = WSSConfig.getNewInstance();
        config.setValidator(WSSecurityEngine.SAML_TOKEN, new CustomSamlAssertionValidator());
        config.setValidator(WSSecurityEngine.SAML2_TOKEN, new CustomSamlAssertionValidator());
        config.setValidateSamlSubjectConfirmation(false);
        secEngine.setWssConfig(config);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 1.1 authentication assertion
     * with a custom Conditions statement.
     */
    @org.junit.Test
    public void testSAML1Conditions() throws Exception {
        SAML1CallbackHandler callbackHandler = new SAML1CallbackHandler();
        callbackHandler.setStatement(SAML1CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        DateTime notBefore = new DateTime();
        conditions.setNotBefore(notBefore);
        conditions.setNotAfter(notBefore.plusMinutes(20));
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 1.1 Authn Assertion (sender vouches):");
            String outputString = 
                XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        List<WSSecurityEngineResult> results = verify(unsignedDoc);
        WSSecurityEngineResult actionResult =
            WSSecurityUtil.fetchActionResult(results, WSConstants.ST_UNSIGNED);
        SamlAssertionWrapper receivedSamlAssertion =
            (SamlAssertionWrapper) actionResult.get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
        assertTrue(receivedSamlAssertion != null);
        assertFalse(receivedSamlAssertion.isSigned());
        assertTrue(receivedSamlAssertion.getSignatureValue() == null);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with an (invalid) custom Conditions statement.
     */
    @org.junit.Test
    public void testSAML2InvalidAfterConditions() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        DateTime notBefore = new DateTime();
        conditions.setNotBefore(notBefore.minusMinutes(5));
        conditions.setNotAfter(notBefore.minusMinutes(3));
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authn Assertion (sender vouches):");
            String outputString = 
                XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        try {
            verify(unsignedDoc);
            fail("Failure expected in processing the SAML Conditions element");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getMessage().contains("SAML token security failure"));
        }
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with an (invalid) custom Conditions statement.
     */
    @org.junit.Test
    public void testSAML2InvalidBeforeConditions() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        DateTime notBefore = new DateTime();
        conditions.setNotBefore(notBefore.plusMinutes(2));
        conditions.setNotAfter(notBefore.plusMinutes(5));
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);
        
        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authn Assertion (sender vouches):");
            String outputString = 
                XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        try {
            verify(unsignedDoc);
            fail("Failure expected in processing the SAML Conditions element");
        } catch (WSSecurityException ex) {
            assertTrue(ex.getMessage().contains("SAML token security failure"));
        }
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with a Conditions statement that has a NotBefore "in the future".
     */
    @org.junit.Test
    public void testSAML2FutureTTLConditions() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        DateTime notBefore = new DateTime();
        conditions.setNotBefore(notBefore.plusSeconds(30));
        conditions.setNotAfter(notBefore.plusMinutes(5));
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML 2 Authn Assertion (sender vouches):");
            String outputString = 
                XMLUtils.PrettyDocumentToString(unsignedDoc);
            LOG.debug(outputString);
        }
        
        verify(unsignedDoc);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with a OneTimeUse Element
     */
    @org.junit.Test
    public void testSAML2OneTimeUse() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        conditions.setTokenPeriodMinutes(5);
        conditions.setOneTimeUse(true);
            
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        String outputString = 
            XMLUtils.PrettyDocumentToString(unsignedDoc);
        assertTrue(outputString.contains("OneTimeUse"));
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(unsignedDoc);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with a ProxyRestriction Element
     */
    @org.junit.Test
    public void testSAML2ProxyRestriction() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        conditions.setTokenPeriodMinutes(5);
        ProxyRestrictionBean proxyRestriction = new ProxyRestrictionBean();
        List<String> audiences = new ArrayList<String>();
        audiences.add("http://apache.org/one");
        audiences.add("http://apache.org/two");
        proxyRestriction.getAudienceURIs().addAll(audiences);
        proxyRestriction.setCount(5);
        conditions.setProxyRestriction(proxyRestriction);
        
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        String outputString = 
            XMLUtils.PrettyDocumentToString(unsignedDoc);
        assertTrue(outputString.contains("ProxyRestriction"));
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(unsignedDoc);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with an AudienceRestriction Element
     */
    @org.junit.Test
    public void testSAML2AudienceRestriction() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        conditions.setTokenPeriodMinutes(5);
        List<String> audiences = new ArrayList<String>();
        audiences.add("http://apache.org/one");
        audiences.add("http://apache.org/two");
        AudienceRestrictionBean audienceRestrictionBean = new AudienceRestrictionBean();
        audienceRestrictionBean.setAudienceURIs(audiences);
        conditions.setAudienceRestrictions(Collections.singletonList(audienceRestrictionBean));
        
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        String outputString = 
            XMLUtils.PrettyDocumentToString(unsignedDoc);
        assertTrue(outputString.contains("AudienceRestriction"));
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(unsignedDoc);
    }
    
    /**
     * Test that creates, sends and processes an unsigned SAML 2 authentication assertion
     * with two AudienceRestriction Elements
     */
    @org.junit.Test
    public void testSAML2AudienceRestrictionSeparateRestrictions() throws Exception {
        SAML2CallbackHandler callbackHandler = new SAML2CallbackHandler();
        callbackHandler.setStatement(SAML2CallbackHandler.Statement.AUTHN);
        callbackHandler.setIssuer("www.example.com");
        
        ConditionsBean conditions = new ConditionsBean();
        conditions.setTokenPeriodMinutes(5);
        
        List<AudienceRestrictionBean> audiencesRestrictions = 
            new ArrayList<AudienceRestrictionBean>();
        AudienceRestrictionBean audienceRestrictionBean = new AudienceRestrictionBean();
        audienceRestrictionBean.setAudienceURIs(Collections.singletonList("http://apache.org/one"));
        audiencesRestrictions.add(audienceRestrictionBean);
        
        audienceRestrictionBean = new AudienceRestrictionBean();
        audienceRestrictionBean.setAudienceURIs(Collections.singletonList("http://apache.org/two"));
        audiencesRestrictions.add(audienceRestrictionBean);
        
        conditions.setAudienceRestrictions(audiencesRestrictions);
        
        callbackHandler.setConditions(conditions);
        
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(callbackHandler, samlCallback);
        SamlAssertionWrapper samlAssertion = new SamlAssertionWrapper(samlCallback);

        WSSecSAMLToken wsSign = new WSSecSAMLToken();

        Document doc = SOAPUtil.toSOAPPart(SOAPUtil.SAMPLE_SOAP_MSG);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document unsignedDoc = wsSign.build(doc, samlAssertion, secHeader);

        String outputString = 
            XMLUtils.PrettyDocumentToString(unsignedDoc);
        assertTrue(outputString.contains("AudienceRestriction"));
        if (LOG.isDebugEnabled()) {
            LOG.debug(outputString);
        }
        
        verify(unsignedDoc);
    }
    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param envelope 
     * @throws Exception Thrown when there is a problem in verification
     */
    private List<WSSecurityEngineResult> verify(Document doc) throws Exception {
        List<WSSecurityEngineResult> results = 
            secEngine.processSecurityHeader(doc, null, null, null);
        String outputString = 
            XMLUtils.PrettyDocumentToString(doc);
        assertTrue(outputString.indexOf("counter_port_type") > 0 ? true : false);
        return results;
    }
    
}
