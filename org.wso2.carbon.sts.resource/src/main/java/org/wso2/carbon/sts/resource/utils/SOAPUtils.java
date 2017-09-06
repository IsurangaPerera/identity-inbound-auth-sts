package org.wso2.carbon.sts.resource.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.cxf.binding.soap.SoapFault;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SOAPUtils {
	
	private SOAPUtils() {}
	
	private static SOAPUtils instance = new SOAPUtils();
	
	public static SOAPUtils getInstance() {
		return instance;
	}
	
	public synchronized SOAPMessage createSoapFault(SoapFault fault) {
		
		SOAPMessage soapMsg = null;
		
		try {
		MessageFactory factory = MessageFactory.newInstance();
		soapMsg = factory.createMessage();
		SOAPPart part = soapMsg.getSOAPPart();

		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();
		
		SOAPFault soapFault = body.addFault();
		soapFault.setFaultString(fault.getMessage());
		soapFault.setFaultCode(fault.getCode());
		
		}catch(SOAPException e) {
			//log here
		}
		
		return soapMsg;
	}
	
	public synchronized String soapToString(SOAPMessage msg)  {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		String soap = null;
		try {
			msg.writeTo(stream);
			soap = new String(stream.toByteArray(), "utf-8");
		} catch (IOException | SOAPException e) {
			//log here
		} 
		return soap;
	}
	
	public SOAPMessage buildSoapResponse(Source response) {

		SOAPMessage soapMsg = null;

		try {
			MessageFactory factory = MessageFactory.newInstance();
			soapMsg = factory.createMessage();
			SOAPPart part = soapMsg.getSOAPPart();

			SOAPEnvelope envelope = part.getEnvelope();
			SOAPBody body = envelope.getBody();

			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(response, result);
			

			DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
			dFact.setNamespaceAware(true);
			dFact.setValidating(false);
			dFact.setIgnoringComments(false);
			dFact.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = dFact.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(writer.toString()));
			Document doc = db.parse(is);

			body.addDocument(doc);
		} catch (Exception e) {
			// log here
		}

		return soapMsg;
	}

}
