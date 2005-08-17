/*
* Copyright  2003-2004 The Apache Software Foundation.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/
package org.apache.ws.sandbox.security.trust.message.token;

import javax.xml.namespace.QName;

import org.apache.ws.sandbox.security.trust.TrustConstants;
import org.apache.ws.sandbox.security.trust.WSTrustException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Ruchith Fernando
 */
public class KeySize extends ValueElement {

    
    public static final QName TOKEN = new QName(TrustConstants.WST_NS,TrustConstants.KEY_SIZE_LN,TrustConstants.WST_PREFIX);

    public KeySize(Element elem) throws WSTrustException {
    	super(elem);
    }

    public KeySize(Document doc) {
        super(doc);
    }

    /**
     * Sets the key size value of the <code>wst:KeySize</code> element
     * @param keySize
     */
    public void setKeySize(int keySize) {
    	
    	this.setValue(Integer.toString(keySize));
    }

    /**
     * Returns the key size if set otherwise returns -1
     * @return Returns the key size if set otherwise returns -1
     * @throws WSTrustException
     */
    public int getKeySize() {
        if(this.getValue() != null)
        	return Integer.parseInt(this.getValue());
        else
        	return -1;
    }

	/**
	 * Returns the QName of this type
	 * @see org.apache.ws.sandbox.security.trust.message.token.AbstractToken#getToken()
	 */
	protected QName getToken() {
		return TOKEN;
	}
}
