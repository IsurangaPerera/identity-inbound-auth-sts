package org.wso2.carbon.sts.provider;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.Context;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.staxutils.StaxUtils;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

public abstract class AbstractResource implements Microservice {

	protected SoapMessage processMessage(byte[] msg) throws Exception {
		SoapMessage message = new SoapMessage(new MessageImpl());
		Exchange ex = new ExchangeImpl();
		ex.setInMessage(message);
		message.setContent(XMLStreamReader.class,
				StaxUtils.createXMLStreamReader(new ByteArrayInputStream(msg)));
		return message;
	}

	public abstract void processRequest(@Context Request request)
			throws UnsupportedEncodingException;
	
	//public abstract String oija();

}
