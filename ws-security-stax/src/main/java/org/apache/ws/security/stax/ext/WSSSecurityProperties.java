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
package org.apache.ws.security.stax.ext;

import java.net.URL;
import java.security.KeyStore;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.ws.security.common.bsp.BSPRule;
import org.apache.ws.security.common.crypto.Crypto;
import org.apache.ws.security.common.crypto.Merlin;
import org.apache.xml.security.stax.config.ConfigurationProperties;
import org.apache.xml.security.stax.ext.XMLSecurityProperties;

import javax.security.auth.callback.CallbackHandler;

/**
 * Main configuration class to supply keys etc.
 * This class is subject to change in the future.
 * Probably we will allow to configure the framework per WSDL
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class WSSSecurityProperties extends XMLSecurityProperties {

    private String actor;
    private CallbackHandler callbackHandler;
    private final List<BSPRule> ignoredBSPRules = new LinkedList<BSPRule>();

    private Integer timestampTTL = 300;
    private boolean strictTimestampCheck = true;

    /**
     * This variable controls whether types other than PasswordDigest or PasswordText
     * are allowed when processing UsernameTokens.
     *
     * By default this is set to false so that the user doesn't have to explicitly
     * reject custom token types in the callback handler.
     */
    private boolean handleCustomPasswordTypes = false;
    private WSSConstants.UsernameTokenPasswordType usernameTokenPasswordType;
    private String tokenUser;

    private WSSConstants.KeyIdentifierType derivedKeyKeyIdentifierType;
    private WSSConstants.DerivedKeyTokenReference derivedKeyTokenReference;

    private Class<? extends Merlin> signatureCryptoClass;
    private Crypto cachedSignatureCrypto;
    private KeyStore cachedSignatureKeyStore;
    private KeyStore signatureKeyStore;
    private String signatureUser;
    private boolean enableSignatureConfirmationVerification = false;

    private Class<? extends Merlin> signatureVerificationCryptoClass;
    private KeyStore signatureVerificationKeyStore;
    private Crypto cachedSignatureVerificationCrypto;
    private KeyStore cachedSignatureVerificationKeyStore;
    private Class<? extends Merlin> decryptionCryptoClass;

    private KeyStore decryptionKeyStore;
    private Crypto cachedDecryptionCrypto;
    private KeyStore cachedDecryptionKeyStore;
    private Class<? extends Merlin> encryptionCryptoClass;

    private KeyStore encryptionKeyStore;
    private Crypto cachedEncryptionCrypto;
    private KeyStore cachedEncryptionKeyStore;
    private String encryptionUser;
    private WSSConstants.KeyIdentifierType encryptionKeyIdentifierType;
    private boolean useReqSigCertForEncryption = false;
    private String encryptionCompressionAlgorithm;

    public WSSSecurityProperties() {
        super();
        setAddExcC14NInclusivePrefixes(true);
    }

    public WSSSecurityProperties(WSSSecurityProperties wssSecurityProperties) {
        super(wssSecurityProperties);

        this.actor = wssSecurityProperties.actor;
        this.callbackHandler = wssSecurityProperties.callbackHandler;
        this.ignoredBSPRules.addAll(wssSecurityProperties.ignoredBSPRules);
        this.timestampTTL = wssSecurityProperties.timestampTTL;
        this.strictTimestampCheck = wssSecurityProperties.strictTimestampCheck;
        this.handleCustomPasswordTypes = wssSecurityProperties.handleCustomPasswordTypes;
        this.usernameTokenPasswordType = wssSecurityProperties.usernameTokenPasswordType;
        this.tokenUser = wssSecurityProperties.tokenUser;
        this.derivedKeyKeyIdentifierType = wssSecurityProperties.derivedKeyKeyIdentifierType;
        this.derivedKeyTokenReference = wssSecurityProperties.derivedKeyTokenReference;
        this.signatureCryptoClass = wssSecurityProperties.signatureCryptoClass;
        this.cachedSignatureCrypto = wssSecurityProperties.cachedSignatureCrypto;
        this.cachedSignatureKeyStore = wssSecurityProperties.cachedSignatureKeyStore;
        this.signatureKeyStore = wssSecurityProperties.signatureKeyStore;
        this.signatureUser = wssSecurityProperties.signatureUser;
        this.enableSignatureConfirmationVerification = wssSecurityProperties.enableSignatureConfirmationVerification;
        this.signatureVerificationCryptoClass = wssSecurityProperties.signatureVerificationCryptoClass;
        this.signatureVerificationKeyStore = wssSecurityProperties.signatureVerificationKeyStore;
        this.cachedSignatureVerificationCrypto = wssSecurityProperties.cachedSignatureVerificationCrypto;
        this.cachedSignatureVerificationKeyStore = wssSecurityProperties.cachedSignatureVerificationKeyStore;
        this.decryptionCryptoClass = wssSecurityProperties.decryptionCryptoClass;
        this.decryptionKeyStore = wssSecurityProperties.decryptionKeyStore;
        this.cachedDecryptionCrypto = wssSecurityProperties.cachedDecryptionCrypto;
        this.cachedDecryptionKeyStore = wssSecurityProperties.cachedDecryptionKeyStore;
        this.encryptionCryptoClass = wssSecurityProperties.encryptionCryptoClass;
        this.encryptionKeyStore = wssSecurityProperties.encryptionKeyStore;
        this.cachedEncryptionCrypto = wssSecurityProperties.cachedEncryptionCrypto;
        this.cachedEncryptionKeyStore = wssSecurityProperties.cachedEncryptionKeyStore;
        this.encryptionUser = wssSecurityProperties.encryptionUser;
        this.encryptionKeyIdentifierType = wssSecurityProperties.encryptionKeyIdentifierType;
        this.useReqSigCertForEncryption = wssSecurityProperties.useReqSigCertForEncryption;
        this.encryptionCompressionAlgorithm = wssSecurityProperties.encryptionCompressionAlgorithm;
    }

    /**
     * returns the password callback handler
     *
     * @return
     */
    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }


    /**
     * sets the password callback handler
     *
     * @param callbackHandler
     */
    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    /**
     * returns the KeyIdentifierType which will be used in the secured document
     *
     * @return The KeyIdentifierType
     */
    public WSSConstants.KeyIdentifierType getEncryptionKeyIdentifierType() {
        return encryptionKeyIdentifierType;
    }

    /**
     * Specifies the KeyIdentifierType to use in the secured document
     *
     * @param encryptionKeyIdentifierType
     */
    public void setEncryptionKeyIdentifierType(WSSConstants.KeyIdentifierType encryptionKeyIdentifierType) {
        this.encryptionKeyIdentifierType = encryptionKeyIdentifierType;
    }

    public Integer getTimestampTTL() {
        return timestampTTL;
    }

    public void setTimestampTTL(Integer timestampTTL) {
        this.timestampTTL = timestampTTL;
    }

    public boolean isStrictTimestampCheck() {
        return strictTimestampCheck;
    }


    public void setStrictTimestampCheck(boolean strictTimestampCheck) {
        this.strictTimestampCheck = strictTimestampCheck;
    }

    /**
     * @param handleCustomTypes
     * whether to handle custom UsernameToken password types or not
     */
    public void setHandleCustomPasswordTypes(boolean handleCustomTypes) {
        this.handleCustomPasswordTypes = handleCustomTypes;
    }

    /**
     * @return whether custom UsernameToken password types are allowed or not
     */
    public boolean getHandleCustomPasswordTypes() {
        return handleCustomPasswordTypes;
    }

    public String getTokenUser() {
        return tokenUser;
    }

    public void setTokenUser(String tokenUser) {
        this.tokenUser = tokenUser;
    }

    public WSSConstants.UsernameTokenPasswordType getUsernameTokenPasswordType() {
        return usernameTokenPasswordType;
    }

    public void setUsernameTokenPasswordType(WSSConstants.UsernameTokenPasswordType usernameTokenPasswordType) {
        this.usernameTokenPasswordType = usernameTokenPasswordType;
    }

    public boolean isEnableSignatureConfirmationVerification() {
        return enableSignatureConfirmationVerification;
    }

    public void setEnableSignatureConfirmationVerification(boolean enableSignatureConfirmationVerification) {
        this.enableSignatureConfirmationVerification = enableSignatureConfirmationVerification;
    }

    public boolean isUseReqSigCertForEncryption() {
        return useReqSigCertForEncryption;
    }

    public void setUseReqSigCertForEncryption(boolean useReqSigCertForEncryption) {
        this.useReqSigCertForEncryption = useReqSigCertForEncryption;
    }

    public String getActor() {
        return actor;
    }


    public void setActor(String actor) {
        this.actor = actor;
    }

    public WSSConstants.KeyIdentifierType getDerivedKeyKeyIdentifierType() {
        return derivedKeyKeyIdentifierType;
    }

    public void setDerivedKeyKeyIdentifierType(WSSConstants.KeyIdentifierType derivedKeyKeyIdentifierType) {
        this.derivedKeyKeyIdentifierType = derivedKeyKeyIdentifierType;
    }

    public WSSConstants.DerivedKeyTokenReference getDerivedKeyTokenReference() {
        return derivedKeyTokenReference;
    }

    public void setDerivedKeyTokenReference(WSSConstants.DerivedKeyTokenReference derivedKeyTokenReference) {
        this.derivedKeyTokenReference = derivedKeyTokenReference;
    }

    public void addIgnoreBSPRule(BSPRule bspRule) {
        ignoredBSPRules.add(bspRule);
    }

    public List<BSPRule> getIgnoredBSPRules() {
        return Collections.unmodifiableList(ignoredBSPRules);
    }

    public void setSignatureUser(String signatureUser) {
        this.signatureUser = signatureUser;
    }

    public String getSignatureUser() {
        return signatureUser;
    }

    public KeyStore getSignatureKeyStore() {
        return signatureKeyStore;
    }

    public void loadSignatureKeyStore(URL url, char[] keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(url.openStream(), keyStorePassword);
        this.signatureKeyStore = keyStore;
    }

    public Class<? extends Merlin> getSignatureCryptoClass() {
        if (signatureCryptoClass != null) {
            return signatureCryptoClass;
        }
        signatureCryptoClass = org.apache.ws.security.common.crypto.Merlin.class;
        return signatureCryptoClass;
    }

    public void setSignatureCryptoClass(Class<? extends Merlin> signatureCryptoClass) {
        this.signatureCryptoClass = signatureCryptoClass;
    }

    public Crypto getSignatureCrypto() throws WSSConfigurationException {

        if (this.getSignatureKeyStore() == null) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "signatureKeyStoreNotSet");
        }

        if (this.getSignatureKeyStore() == cachedSignatureKeyStore) {
            return cachedSignatureCrypto;
        }

        Class<? extends Merlin> signatureCryptoClass = this.getSignatureCryptoClass();

        try {
            Merlin signatureCrypto = signatureCryptoClass.newInstance();
            signatureCrypto.setDefaultX509Identifier(ConfigurationProperties.getProperty("DefaultX509Alias"));
            signatureCrypto.setCryptoProvider(ConfigurationProperties.getProperty("CertProvider"));
            signatureCrypto.setKeyStore(this.getSignatureKeyStore());
            cachedSignatureCrypto = signatureCrypto;
            cachedSignatureKeyStore = this.getSignatureKeyStore();
            return signatureCrypto;
        } catch (Exception e) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "signatureCryptoFailure", e);
        }
    }

    public KeyStore getSignatureVerificationKeyStore() {
        return signatureVerificationKeyStore;
    }

    public void loadSignatureVerificationKeystore(URL url, char[] keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(url.openStream(), keyStorePassword);
        this.signatureVerificationKeyStore = keyStore;
    }

    public Class<? extends Merlin> getSignatureVerificationCryptoClass() {
        if (signatureVerificationCryptoClass != null) {
            return signatureVerificationCryptoClass;
        }
        signatureVerificationCryptoClass = Merlin.class;
        return signatureVerificationCryptoClass;
    }

    public void setSignatureVerificationCryptoClass(Class<? extends Merlin> signatureVerificationCryptoClass) {
        this.signatureVerificationCryptoClass = signatureVerificationCryptoClass;
    }

    public Crypto getSignatureVerificationCrypto() throws WSSConfigurationException {

        if (this.getSignatureVerificationKeyStore() == null) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "signatureVerificationKeyStoreNotSet");
        }

        if (this.getSignatureVerificationKeyStore() == cachedSignatureVerificationKeyStore) {
            return cachedSignatureVerificationCrypto;
        }

        Class<? extends Merlin> signatureVerificationCryptoClass = this.getSignatureVerificationCryptoClass();

        try {
            Merlin signatureVerificationCrypto = signatureVerificationCryptoClass.newInstance();
            signatureVerificationCrypto.setKeyStore(this.getSignatureVerificationKeyStore());
            signatureVerificationCrypto.setDefaultX509Identifier(ConfigurationProperties.getProperty("DefaultX509Alias"));
            signatureVerificationCrypto.setCryptoProvider(ConfigurationProperties.getProperty("CertProvider"));
            cachedSignatureVerificationCrypto = signatureVerificationCrypto;
            cachedSignatureVerificationKeyStore = this.getSignatureVerificationKeyStore();
            return signatureVerificationCrypto;
        } catch (Exception e) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "signatureVerificationCryptoFailure", e);
        }
    }

    /**
     * Returns the decryption keystore
     *
     * @return A keystore for decryption operation
     */
    public KeyStore getDecryptionKeyStore() {
        return decryptionKeyStore;
    }

    /**
     * loads a java keystore from the given url for decrypt operations
     *
     * @param url              The URL to the keystore
     * @param keyStorePassword The keyStorePassword
     * @throws Exception thrown if something goes wrong while loading the keystore
     */
    public void loadDecryptionKeystore(URL url, char[] keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(url.openStream(), keyStorePassword);
        this.decryptionKeyStore = keyStore;
    }

    /**
     * Returns the decryption crypto class
     *
     * @return
     */
    public Class<? extends Merlin> getDecryptionCryptoClass() {
        if (decryptionCryptoClass != null) {
            return decryptionCryptoClass;
        }
        decryptionCryptoClass = Merlin.class;
        return decryptionCryptoClass;
    }

    /**
     * Sets a custom decryption class
     *
     * @param decryptionCryptoClass
     */
    public void setDecryptionCryptoClass(Class<? extends Merlin> decryptionCryptoClass) {
        this.decryptionCryptoClass = decryptionCryptoClass;
    }

    /**
     * returns the decryptionCrypto for the key-management
     *
     * @return A Crypto instance
     * @throws WSSConfigurationException thrown if something goes wrong
     */
    public Crypto getDecryptionCrypto() throws WSSConfigurationException {

        if (this.getDecryptionKeyStore() == null) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "decryptionKeyStoreNotSet");
        }

        if (this.getDecryptionKeyStore() == cachedDecryptionKeyStore) {
            return cachedDecryptionCrypto;
        }

        Class<? extends Merlin> decryptionCryptoClass = this.getDecryptionCryptoClass();

        try {
            Merlin decryptionCrypto = decryptionCryptoClass.newInstance();
            decryptionCrypto.setKeyStore(this.getDecryptionKeyStore());
            decryptionCrypto.setDefaultX509Identifier(ConfigurationProperties.getProperty("DefaultX509Alias"));
            decryptionCrypto.setCryptoProvider(ConfigurationProperties.getProperty("CertProvider"));
            cachedDecryptionCrypto = decryptionCrypto;
            cachedDecryptionKeyStore = this.getDecryptionKeyStore();
            return decryptionCrypto;
        } catch (Exception e) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "decryptionCryptoFailure", e);
        }
    }

    /**
     * Returns the encryption keystore
     *
     * @return A keystore for encryption operation
     */
    public KeyStore getEncryptionKeyStore() {
        return encryptionKeyStore;
    }

    /**
     * loads a java keystore from the given url for encrypt operations
     *
     * @param url              The URL to the keystore
     * @param keyStorePassword The keyStorePassword
     * @throws Exception thrown if something goes wrong while loading the keystore
     */
    public void loadEncryptionKeystore(URL url, char[] keyStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("jks");
        keyStore.load(url.openStream(), keyStorePassword);
        this.encryptionKeyStore = keyStore;
    }

    /**
     * Returns the encryption crypto class
     *
     * @return
     */
    public Class<? extends Merlin> getEncryptionCryptoClass() {
        if (encryptionCryptoClass != null) {
            return encryptionCryptoClass;
        }
        encryptionCryptoClass = Merlin.class;
        return encryptionCryptoClass;
    }

    /**
     * Sets a custom encryption class
     *
     * @param encryptionCryptoClass
     */
    public void setEncryptionCryptoClass(Class<? extends Merlin> encryptionCryptoClass) {
        this.encryptionCryptoClass = encryptionCryptoClass;
    }

    /**
     * returns the encryptionCrypto for the key-management
     *
     * @return A Crypto instance
     * @throws WSSConfigurationException thrown if something goes wrong
     */
    public Crypto getEncryptionCrypto() throws WSSConfigurationException {

        if (this.getEncryptionKeyStore() == null) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "encryptionKeyStoreNotSet");
        }

        if (this.getEncryptionKeyStore() == cachedEncryptionKeyStore) {
            return cachedEncryptionCrypto;
        }

        Class<? extends Merlin> encryptionCryptoClass = this.getEncryptionCryptoClass();

        try {
            Merlin encryptionCrypto = encryptionCryptoClass.newInstance();
            encryptionCrypto.setKeyStore(this.getEncryptionKeyStore());
            encryptionCrypto.setDefaultX509Identifier(ConfigurationProperties.getProperty("DefaultX509Alias"));
            encryptionCrypto.setCryptoProvider(ConfigurationProperties.getProperty("CertProvider"));
            cachedEncryptionCrypto = encryptionCrypto;
            cachedEncryptionKeyStore = this.getEncryptionKeyStore();
            return encryptionCrypto;
        } catch (Exception e) {
            throw new WSSConfigurationException(WSSConfigurationException.ErrorCode.FAILURE, "encryptionCryptoFailure", e);
        }
    }
    
    /**
     * Returns the alias for the encryption key in the keystore
     *
     * @return the alias for the encryption key in the keystore as string
     */
    public String getEncryptionUser() {
        return encryptionUser;
    }

    /**
     * Specifies the the alias for the encryption key in the keystore
     *
     * @param encryptionUser the the alias for the encryption key in the keystore as string
     */
    public void setEncryptionUser(String encryptionUser) {
        this.encryptionUser = encryptionUser;
    }

    public String getEncryptionCompressionAlgorithm() {
        return encryptionCompressionAlgorithm;
    }

    public void setEncryptionCompressionAlgorithm(String encryptionCompressionAlgorithm) {
        this.encryptionCompressionAlgorithm = encryptionCompressionAlgorithm;
    }
}