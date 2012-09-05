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
package org.apache.ws.security.policy.builders;

import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.security.policy.SP11Constants;
import org.apache.ws.security.policy.SP13Constants;
import org.apache.ws.security.policy.SPConstants;
import org.apache.ws.security.policy.SPUtils;
import org.apache.ws.security.policy.model.InitiatorToken;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class InitiatorTokenBuilder implements AssertionBuilder<Element> {

    public Assertion build(Element element, AssertionBuilderFactory factory) throws IllegalArgumentException {

        final SPConstants.SPVersion spVersion = SPConstants.SPVersion.getSPVersion(element.getNamespaceURI());
        final Element nestedPolicyElement = SPUtils.getFirstPolicyChildElement(element);
        final Policy nestedPolicy = nestedPolicyElement != null ? factory.getPolicyEngine().getPolicy(nestedPolicyElement) : new Policy();
        InitiatorToken initiatorToken = new InitiatorToken(
                spVersion,
                nestedPolicy
        );
        initiatorToken.setOptional(SPUtils.isOptional(element));
        initiatorToken.setIgnorable(SPUtils.isIgnorable(element));
        return initiatorToken;
    }

    public QName[] getKnownElements() {
        return new QName[]{SP13Constants.INITIATOR_TOKEN, SP11Constants.INITIATOR_TOKEN};
    }
}