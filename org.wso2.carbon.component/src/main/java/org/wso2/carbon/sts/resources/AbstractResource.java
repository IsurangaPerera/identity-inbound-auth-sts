package org.wso2.carbon.sts.resources;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.staxutils.StaxUtils;

public abstract class AbstractResource {
	
	private SoapMessage setUpMessage(byte[] msg) throws Exception {
        SoapMessage message = new SoapMessage(new MessageImpl());
        Exchange ex = new ExchangeImpl();
        ex.setInMessage(message);
        message.setContent(XMLStreamReader.class, StaxUtils.createXMLStreamReader(new ByteArrayInputStream(msg)));
        return message;
    }

}
