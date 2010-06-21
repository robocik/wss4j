package ch.gigerstyle.xmlsec.processorImpl.input;

import ch.gigerstyle.xmlsec.*;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.BinarySecurityTokenType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * User: giger
 * Date: May 13, 2010
 * Time: 5:55:30 PM
 * Copyright 2010 Marc Giger gigerstyle@gmx.ch
 * <p/>
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
public class BinarySecurityTokenInputProcessor extends AbstractInputProcessor {

    private Map<String, BinarySecurityTokenType> binarySecurityTokens = new HashMap<String, BinarySecurityTokenType>();
    private BinarySecurityTokenType currentBinarySecurityTokenType;
    private boolean isFinishedcurrentBinarySecurityToken = false;

    public BinarySecurityTokenInputProcessor(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    public void processEvent(XMLEvent xmlEvent, InputProcessorChain inputProcessorChain, SecurityContext securityContext) throws XMLStreamException, XMLSecurityException {

        if (currentBinarySecurityTokenType != null) {
            try {
                isFinishedcurrentBinarySecurityToken = currentBinarySecurityTokenType.parseXMLEvent(xmlEvent);
                if (isFinishedcurrentBinarySecurityToken) {
                    currentBinarySecurityTokenType.validate();
                }
            } catch (ParseException e) {
                throw new XMLSecurityException(e);
            }
        }
        else if (xmlEvent.isStartElement()) {
            StartElement startElement = xmlEvent.asStartElement();
            if (startElement.getName().equals(Constants.TAG_wsse_BinarySecurityToken)) {
                currentBinarySecurityTokenType = new BinarySecurityTokenType(startElement);
            }
        }

        if (currentBinarySecurityTokenType != null && isFinishedcurrentBinarySecurityToken) {
            try {
                securityContext.putAsList(BinarySecurityTokenType.class, currentBinarySecurityTokenType);
            } finally {
                //probably we can remove this processor from the chain now?
                currentBinarySecurityTokenType = null;
                isFinishedcurrentBinarySecurityToken = false;
            }
        }

        inputProcessorChain.processEvent(xmlEvent);
    }
}