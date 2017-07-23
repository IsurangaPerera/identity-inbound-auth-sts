package org.wso2.carbon.sts.resource.interceptor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.w3c.dom.Document;
import org.wso2.carbon.sts.resource.provider.DefaultSecurityTokenServiceProvider;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SoapMessageInterceptor {

	SAAJInInterceptor saajIn = new SAAJInInterceptor();
	WebServiceContext context;

	public void setWebServiceContext(WebServiceContext context) {
		this.context = context;
	}

	public SOAPMessage handleMessage(SoapMessage message) throws SOAPException {

		SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
		if (soapMessage == null) {
			saajIn.handleMessage(message);
			soapMessage = message.getContent(SOAPMessage.class);
		}

		DOMSource tokenRequest = null;
		tokenRequest = new DOMSource(soapMessage.getSOAPBody()
				.extractContentAsDocument());

		Source tokenResponse = null;
		try {
			DefaultSecurityTokenServiceProvider provider = new DefaultSecurityTokenServiceProvider();
			provider.setWebServiceContext(context);
			tokenResponse = provider.invoke(tokenRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buildSoapResponse(tokenResponse);
	}

	public SOAPMessage buildSoapResponse(Source response) throws SOAPException {

		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		SOAPPart part = soapMsg.getSOAPPart();

		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();

		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
			transformer.transform(response, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		DocumentBuilder db = null;
		DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
		dFact.setNamespaceAware(true);
		try {
			db = dFact.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(writer.toString()));
		Document doc = null;
		try {
			doc = db.parse(is);
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		body.addDocument(doc);

		return soapMsg;
	}
}
