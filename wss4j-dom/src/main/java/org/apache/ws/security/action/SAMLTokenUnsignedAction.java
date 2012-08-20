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

package org.apache.ws.security.action;

import javax.security.auth.callback.CallbackHandler;

import org.apache.ws.security.common.ext.WSSecurityException;
import org.apache.ws.security.common.saml.AssertionWrapper;
import org.apache.ws.security.common.saml.SAMLCallback;
import org.apache.ws.security.common.saml.SAMLUtil;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecSAMLToken;
import org.w3c.dom.Document;

public class SAMLTokenUnsignedAction extends SAMLTokenSignedAction {

    public void execute(WSHandler handler, int actionToDo, Document doc, RequestData reqData)
            throws WSSecurityException {
        WSSecSAMLToken builder = new WSSecSAMLToken(reqData.getWssConfig());

        CallbackHandler samlCallbackHandler = 
                handler.getCallbackHandler(
                    WSHandlerConstants.SAML_CALLBACK_CLASS,
                    WSHandlerConstants.SAML_CALLBACK_REF, 
                    reqData
                );
        if (samlCallbackHandler == null) {
            throw new WSSecurityException(
                WSSecurityException.ErrorCode.FAILURE, 
                "noSAMLCallbackHandler"
            );
        }
        SAMLCallback samlCallback = new SAMLCallback();
        SAMLUtil.doSAMLCallback(samlCallbackHandler, samlCallback);

        AssertionWrapper assertion = new AssertionWrapper(samlCallback);

        // add the SAMLAssertion Token to the SOAP Envelope
        builder.build(doc, assertion, reqData.getSecHeader());
    }
}
