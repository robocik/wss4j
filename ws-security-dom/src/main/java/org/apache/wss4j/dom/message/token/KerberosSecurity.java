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

package org.apache.wss4j.dom.message.token;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.wss4j.common.kerberos.KerberosClientAction;
import org.apache.wss4j.common.kerberos.KerberosContextAndServiceNameCallback;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.bsp.BSPEnforcer;
import org.apache.wss4j.common.bsp.BSPRule;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Kerberos Security Token.
 */
public class KerberosSecurity extends BinarySecurity {
    
    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(KerberosSecurity.class);
    private SecretKey secretKey;
    
    /**
     * This constructor creates a new Kerberos token object and initializes
     * it from the data contained in the element.
     *
     * @param elem the element containing the Kerberos token data
     * @param bspEnforcer a BSPEnforcer instance to enforce BSP rules
     * @throws WSSecurityException
     */
    public KerberosSecurity(Element elem, BSPEnforcer bspEnforcer) throws WSSecurityException {
        super(elem, bspEnforcer);
        String valueType = getValueType();
        if (!WSConstants.WSS_GSS_KRB_V5_AP_REQ.equals(valueType)) {
            bspEnforcer.handleBSPRule(BSPRule.R6902);
        }
    }

    /**
     * This constructor creates a new Kerberos element.
     *
     * @param doc
     */
    public KerberosSecurity(Document doc) {
        super(doc);
    }
    
    /**
     * Return true if this token is a Kerberos V5 AP REQ token
     */
    public boolean isV5ApReq() {
        String type = getValueType();
        if (WSConstants.WSS_KRB_V5_AP_REQ.equals(type)
            || WSConstants.WSS_KRB_V5_AP_REQ1510.equals(type)
            || WSConstants.WSS_KRB_V5_AP_REQ4120.equals(type)) {
            return true;
        }
        return false;
    }
    
    /**
     * Return true if this token is a Kerberos GSS V5 AP REQ token
     */
    public boolean isGssV5ApReq() {
        String type = getValueType();
        if (WSConstants.WSS_GSS_KRB_V5_AP_REQ.equals(type)
            || WSConstants.WSS_GSS_KRB_V5_AP_REQ1510.equals(type)
            || WSConstants.WSS_GSS_KRB_V5_AP_REQ4120.equals(type)) {
            return true;
        }
        return false;
    }
    
    /**
     * Retrieve a service ticket from a KDC using the Kerberos JAAS module, and set it in this
     * BinarySecurityToken.
     * @param callbackHandler a CallbackHandler instance to retrieve a password (optional),
     * JAAS Login Module name (required) + service name (required)
     * @throws WSSecurityException
     */
    public void retrieveServiceTicket(
        CallbackHandler callbackHandler
    ) throws WSSecurityException {
        KerberosContextAndServiceNameCallback contextAndServiceNameCallback = new KerberosContextAndServiceNameCallback();
        try {
            callbackHandler.handle(new Callback[]{contextAndServiceNameCallback});
        } catch (IOException e) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, e);
        } catch (UnsupportedCallbackException e) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, e);
        } 

        String jaasLoginModuleName = contextAndServiceNameCallback.getContextName();
        if (jaasLoginModuleName == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "kerberosCallbackContextNameNotSupplied");
        }
        String serviceName = contextAndServiceNameCallback.getServiceName();
        if (serviceName == null) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILURE, "kerberosCallbackServiceNameNotSupplied");
        }
        
        retrieveServiceTicket(jaasLoginModuleName, callbackHandler, serviceName);
    }

    /**
     * Retrieve a service ticket from a KDC using the Kerberos JAAS module, and set it in this
     * BinarySecurityToken.
     * @param jaasLoginModuleName the JAAS Login Module name to use
     * @param callbackHandler a CallbackHandler instance to retrieve a password (optional)
     * @param serviceName the desired Kerberized service
     * @throws WSSecurityException
     */
    public void retrieveServiceTicket(
        String jaasLoginModuleName, 
        CallbackHandler callbackHandler,
        String serviceName
    ) throws WSSecurityException {
        // Get a TGT from the KDC using JAAS
        LoginContext loginContext = null;
        try {
            if (callbackHandler == null) {
                loginContext = new LoginContext(jaasLoginModuleName);
            } else {
                loginContext = new LoginContext(jaasLoginModuleName, callbackHandler);
            }
            loginContext.login();
        } catch (LoginException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(ex.getMessage(), ex);
            }
            throw new WSSecurityException(
                WSSecurityException.ErrorCode.FAILURE,
                "kerberosLoginError", 
                ex, ex.getMessage());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully authenticated to the TGT");
        }
        
        Subject clientSubject = loginContext.getSubject();
        Set<Principal> clientPrincipals = clientSubject.getPrincipals();
        if (clientPrincipals.isEmpty()) {
            throw new WSSecurityException(
                WSSecurityException.ErrorCode.FAILURE, 
                "kerberosLoginError",
                "No Client principals found after login");
        }
        // Store the TGT
        KerberosTicket tgt = getKerberosTicket(clientSubject, null);
        
        // Get the service ticket
        KerberosClientAction action =
            new KerberosClientAction(clientPrincipals.iterator().next(), serviceName);
        byte[] ticket = Subject.doAs(clientSubject, action);
        if (ticket == null) {
            throw new WSSecurityException(
                WSSecurityException.ErrorCode.FAILURE, "kerberosServiceTicketError"
            );
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully retrieved a service ticket");
        }
        
        // Get the Service Ticket (private credential)
        KerberosTicket serviceTicket = getKerberosTicket(clientSubject, tgt);
        if (serviceTicket != null) {
            secretKey = serviceTicket.getSessionKey();
        }
        
        setToken(ticket);
        
        if ("".equals(getValueType())) {
            setValueType(WSConstants.WSS_GSS_KRB_V5_AP_REQ);
        }
    }
    
    /**
     * Get a KerberosTicket from the clientSubject parameter, that is not equal to the supplied KerberosTicket
     * parameter (can be null)
     */
    private KerberosTicket getKerberosTicket(Subject clientSubject, KerberosTicket previousTicket) {
        Set<KerberosTicket> privateCredentials = clientSubject.getPrivateCredentials(KerberosTicket.class);
        if (privateCredentials == null || privateCredentials.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Kerberos client subject private credentials are null");
            }
            return null;
        }
        
        for (KerberosTicket privateCredential : privateCredentials) {
            if (!privateCredential.equals(previousTicket)) {
                return privateCredential;
            }
        }
        return null;
    }
    
    /**
     * Get the SecretKey associated with the service principal
     * @return the SecretKey associated with the service principal
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }
    
    /**
     * Return true if the valueType represents a Kerberos Token
     * @param valueType the valueType of the token
     * @return true if the valueType represents a Kerberos Token
     */
    public static boolean isKerberosToken(String valueType) {
        if (WSConstants.WSS_KRB_V5_AP_REQ.equals(valueType)
            || WSConstants.WSS_GSS_KRB_V5_AP_REQ.equals(valueType)
            || WSConstants.WSS_KRB_V5_AP_REQ1510.equals(valueType)
            || WSConstants.WSS_GSS_KRB_V5_AP_REQ1510.equals(valueType)
            || WSConstants.WSS_KRB_V5_AP_REQ4120.equals(valueType)
            || WSConstants.WSS_GSS_KRB_V5_AP_REQ4120.equals(valueType)) {
            return true;
        }
        return false;
    }
    
}
