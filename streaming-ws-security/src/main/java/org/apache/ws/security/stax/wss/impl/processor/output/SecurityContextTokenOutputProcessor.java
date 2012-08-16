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
package org.apache.ws.security.stax.wss.impl.processor.output;

import org.apache.ws.security.stax.wss.ext.WSSConstants;
import org.apache.ws.security.stax.wss.ext.WSSSecurityProperties;
import org.apache.ws.security.stax.wss.ext.WSSUtils;
import org.apache.ws.security.stax.wss.ext.WSSecurityException;
import org.apache.xml.security.stax.ext.*;
import org.apache.xml.security.stax.ext.stax.XMLSecAttribute;
import org.apache.xml.security.stax.ext.stax.XMLSecEvent;
import org.apache.xml.security.stax.ext.stax.XMLSecStartElement;
import org.apache.xml.security.stax.impl.securityToken.AbstractSecurityToken;
import org.apache.xml.security.stax.impl.util.IDGenerator;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SecurityContextTokenOutputProcessor extends AbstractOutputProcessor {

    public SecurityContextTokenOutputProcessor() throws XMLSecurityException {
        super();
    }

    @Override
    public void processEvent(XMLSecEvent xmlSecEvent, OutputProcessorChain outputProcessorChain) throws XMLStreamException, XMLSecurityException {
        try {
            String tokenId = outputProcessorChain.getSecurityContext().get(WSSConstants.PROP_USE_THIS_TOKEN_ID_FOR_SECURITYCONTEXTTOKEN);
            if (tokenId == null) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_ENCRYPTION);
            }
            SecurityTokenProvider wrappingSecurityTokenProvider = outputProcessorChain.getSecurityContext().getSecurityTokenProvider(tokenId);
            if (wrappingSecurityTokenProvider == null) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_ENCRYPTION);
            }
            final SecurityToken wrappingSecurityToken = wrappingSecurityTokenProvider.getSecurityToken();
            if (wrappingSecurityToken == null) {
                throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_ENCRYPTION);
            }

            final String wsuId = IDGenerator.generateID(null);
            final String identifier = IDGenerator.generateID(null);

            final AbstractSecurityToken securityContextSecurityToken = new AbstractSecurityToken(wsuId) {

                @Override
                public boolean isAsymmetric() {
                    return wrappingSecurityToken.isAsymmetric();
                }

                @Override
                public Key getKey(String algorithmURI, XMLSecurityConstants.KeyUsage keyUsage) throws XMLSecurityException {
                    return wrappingSecurityToken.getSecretKey(algorithmURI, null);
                }

                @Override
                public PublicKey getPubKey(String algorithmURI, XMLSecurityConstants.KeyUsage keyUsage) throws XMLSecurityException {
                    return wrappingSecurityToken.getPublicKey(null, null);
                }

                @Override
                public X509Certificate[] getX509Certificates() throws XMLSecurityException {
                    return wrappingSecurityToken.getX509Certificates();
                }

                @Override
                public void verify() throws XMLSecurityException {
                    wrappingSecurityToken.verify();
                }

                @Override
                public SecurityToken getKeyWrappingToken() {
                    return wrappingSecurityToken;
                }

                @Override
                public WSSConstants.TokenType getTokenType() {
                    return WSSConstants.SecurityContextToken;
                }
            };
            wrappingSecurityToken.addWrappedToken(securityContextSecurityToken);

            SecurityTokenProvider securityContextSecurityTokenProvider = new SecurityTokenProvider() {

                @Override
                public SecurityToken getSecurityToken() throws WSSecurityException {
                    return securityContextSecurityToken;
                }

                @Override
                public String getId() {
                    return wsuId;
                }
            };

            FinalSecurityContextTokenOutputProcessor finalSecurityContextTokenOutputProcessor = new FinalSecurityContextTokenOutputProcessor(securityContextSecurityToken, identifier);
            finalSecurityContextTokenOutputProcessor.setXMLSecurityProperties(getSecurityProperties());
            finalSecurityContextTokenOutputProcessor.setAction(getAction());
            XMLSecurityConstants.Action action = getAction();
            if (action.equals(WSSConstants.SIGNATURE_WITH_DERIVED_KEY)) {
                outputProcessorChain.getSecurityContext().put(WSSConstants.PROP_USE_THIS_TOKEN_ID_FOR_DERIVED_KEY, wsuId);
                if (wrappingSecurityToken.getProcessor() != null) {
                    finalSecurityContextTokenOutputProcessor.addBeforeProcessor(wrappingSecurityToken.getProcessor());
                } else {
                    finalSecurityContextTokenOutputProcessor.addBeforeProcessor(WSSSignatureOutputProcessor.class.getName());
                }
            } else if (action.equals(WSSConstants.ENCRYPT_WITH_DERIVED_KEY)) {
                outputProcessorChain.getSecurityContext().put(WSSConstants.PROP_USE_THIS_TOKEN_ID_FOR_DERIVED_KEY, wsuId);
                if (wrappingSecurityToken.getProcessor() != null) {
                    finalSecurityContextTokenOutputProcessor.addBeforeProcessor(wrappingSecurityToken.getProcessor());
                } else {
                    finalSecurityContextTokenOutputProcessor.addAfterProcessor(EncryptEndingOutputProcessor.class.getName());
                }
            }

            finalSecurityContextTokenOutputProcessor.init(outputProcessorChain);
            outputProcessorChain.getSecurityContext().registerSecurityTokenProvider(wsuId, securityContextSecurityTokenProvider);
            securityContextSecurityToken.setProcessor(finalSecurityContextTokenOutputProcessor);

        } finally {
            outputProcessorChain.removeProcessor(this);
        }
        outputProcessorChain.processEvent(xmlSecEvent);
    }

    class FinalSecurityContextTokenOutputProcessor extends AbstractOutputProcessor {

        private final SecurityToken securityToken;
        private final String identifier;

        FinalSecurityContextTokenOutputProcessor(SecurityToken securityToken, String identifier) throws XMLSecurityException {
            super();
            this.securityToken = securityToken;
            this.identifier = identifier;
        }

        @Override
        public void processEvent(XMLSecEvent xmlSecEvent, OutputProcessorChain outputProcessorChain) throws XMLStreamException, XMLSecurityException {
            outputProcessorChain.processEvent(xmlSecEvent);
            if (xmlSecEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                XMLSecStartElement xmlSecStartElement = xmlSecEvent.asStartElement();
                if (xmlSecStartElement.getName().equals(WSSConstants.TAG_wsse_Security)
                        && WSSUtils.isInSecurityHeader(xmlSecStartElement, ((WSSSecurityProperties) getSecurityProperties()).getActor())) {
                    OutputProcessorChain subOutputProcessorChain = outputProcessorChain.createSubChain(this);

                    List<XMLSecAttribute> attributes = new ArrayList<XMLSecAttribute>(1);
                    attributes.add(createAttribute(WSSConstants.ATT_wsu_Id, securityToken.getId()));
                    createStartElementAndOutputAsEvent(subOutputProcessorChain, WSSConstants.TAG_wsc0502_SecurityContextToken, true, attributes);
                    createStartElementAndOutputAsEvent(subOutputProcessorChain, WSSConstants.TAG_wsc0502_Identifier, false, null);
                    createCharactersAndOutputAsEvent(subOutputProcessorChain, identifier);
                    createEndElementAndOutputAsEvent(subOutputProcessorChain, WSSConstants.TAG_wsc0502_Identifier);
                    createEndElementAndOutputAsEvent(subOutputProcessorChain, WSSConstants.TAG_wsc0502_SecurityContextToken);

                    outputProcessorChain.removeProcessor(this);
                }
            }
        }
    }
}