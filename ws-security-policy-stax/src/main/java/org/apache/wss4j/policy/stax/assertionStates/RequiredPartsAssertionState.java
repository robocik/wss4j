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

import org.apache.wss4j.policy.AssertionState;
import org.apache.wss4j.common.WSSPolicyException;
import org.apache.wss4j.policy.model.AbstractSecurityAssertion;
import org.apache.wss4j.policy.model.Header;
import org.apache.wss4j.policy.model.RequiredParts;
import org.apache.xml.security.stax.securityEvent.SecurityEvent;
import org.apache.xml.security.stax.securityEvent.SecurityEventConstants;
import org.apache.wss4j.policy.stax.Assertable;
import org.apache.wss4j.policy.stax.DummyPolicyAsserter;
import org.apache.wss4j.policy.stax.PolicyAsserter;
import org.apache.wss4j.stax.ext.WSSConstants;
import org.apache.wss4j.stax.ext.WSSUtils;
import org.apache.wss4j.stax.securityEvent.RequiredPartSecurityEvent;
import org.apache.wss4j.stax.securityEvent.WSSecurityEventConstants;

import javax.xml.namespace.QName;

import java.util.*;

/**
 * WSP1.3, 4.3.2 RequiredParts Assertion
 */
public class RequiredPartsAssertionState extends AssertionState implements Assertable {

    private final Map<Header, Boolean> headers = new HashMap<Header, Boolean>();
    private PolicyAsserter policyAsserter;

    public RequiredPartsAssertionState(AbstractSecurityAssertion assertion,
                                       PolicyAsserter policyAsserter,
                                       boolean asserted) {
        super(assertion, asserted);

        RequiredParts requiredParts = (RequiredParts) assertion;
        for (int i = 0; i < requiredParts.getHeaders().size(); i++) {
            Header header = requiredParts.getHeaders().get(i);
            headers.put(header, Boolean.FALSE);
        }
        
        this.policyAsserter = policyAsserter;
        if (this.policyAsserter == null) {
            this.policyAsserter = new DummyPolicyAsserter();
        }
        
        if (asserted) {
            policyAsserter.assertPolicy(getAssertion());
        }
    }

    @Override
    public SecurityEventConstants.Event[] getSecurityEventType() {
        return new SecurityEventConstants.Event[]{
                WSSecurityEventConstants.RequiredPart
        };
    }

    @Override
    public boolean assertEvent(SecurityEvent securityEvent) throws WSSPolicyException {
        RequiredPartSecurityEvent requiredPartSecurityEvent = (RequiredPartSecurityEvent) securityEvent;

        Iterator<Map.Entry<Header, Boolean>> elementMapIterator = headers.entrySet().iterator();
        while (elementMapIterator.hasNext()) {
            Map.Entry<Header, Boolean> next = elementMapIterator.next();
            Header header = next.getKey();
            QName headerQName = new QName(header.getNamespace(), header.getName() == null ? "" : header.getName());

            List<QName> header11Path = new LinkedList<QName>();
            header11Path.addAll(WSSConstants.SOAP_11_HEADER_PATH);
            header11Path.add(headerQName);

            if (WSSUtils.pathMatches(header11Path, requiredPartSecurityEvent.getElementPath(), true, header.getName() == null)) {
                next.setValue(Boolean.TRUE);
                break;
            }
        }
        //if we return false here other required elements will trigger a PolicyViolationException
        policyAsserter.assertPolicy(getAssertion());
        return true;
    }

    @Override
    public boolean isAsserted() {
        clearErrorMessage();
        Iterator<Map.Entry<Header, Boolean>> elementMapIterator = headers.entrySet().iterator();
        while (elementMapIterator.hasNext()) {
            Map.Entry<Header, Boolean> next = elementMapIterator.next();
            if (Boolean.FALSE.equals(next.getValue())) {
                setErrorMessage("Element " + next.getKey().toString() + " must be present");
                policyAsserter.unassertPolicy(getAssertion(), getErrorMessage());
                return false;
            }
        }
        policyAsserter.assertPolicy(getAssertion());
        return true;
    }
}
