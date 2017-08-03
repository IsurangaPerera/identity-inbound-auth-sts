package org.wso2.carbon.sts.resource.interceptor;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.neethi.Policy;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.sts.resource.WSContext;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.carbon.sts.resource.provider.PasswordCallbackHandler;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.util.BufferUtil;

@Component(
        name = "org.wso2.carbon.sts.resource.interceptor.MSF4JMessageInInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class MSF4JMessageInInterceptor implements Interceptor {
	
	public static final String METHOD = "org.wso2.carbon.sts.resource.STSResource";
	
	SAAJInInterceptor saajIn = new SAAJInInterceptor();

	@Override
	public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo)
			throws Exception {
		
	}

	@Override
	public boolean preCall(Request request, Response response, ServiceMethodInfo smi)
			throws Exception {
		if(METHOD.equals(smi.getMethodName())) {
			Policy policy = DataHolder.getInstance().getPolicy();
			SoapMessage message = processMessage(request);
			WSContext.getInstance().buildWebServiceContext(request, message);
			
			message.put(Message.REQUESTOR_ROLE, Boolean.FALSE);
			message.put(Message.INBOUND_MESSAGE, new Boolean(true));
			message.put(SoapMessage.HTTP_REQUEST_METHOD, request.getHttpMethod());
			
			request.setProperty(Source.class.getName(), getSource(message));
			
			if (!policy.isEmpty()) {
				PolicyInInterceptor pi = new PolicyInInterceptor();
				InterceptorChain chain = message.getInterceptorChain();
				chain.add(pi);
				chain.doIntercept(message);
				System.out.println(message.getContent(Exception.class).getMessage());

				/*chain.add(new PolicyBasedWSS4JStaxInInterceptor());
				boolean state = chain.doIntercept(message);
				System.out.println(state);*/		
			} 
		}
		return true;
	}
	
	private SoapMessage processMessage(Request request) throws EndpointException {
		List<ByteBuffer> fullMessageBody = request.getFullMessageBody();
		ByteBuffer buffer = BufferUtil.merge(fullMessageBody);

		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes, 0, bytes.length);
		buffer.clear();
		bytes = new byte[buffer.capacity()];
		buffer.get(bytes, 0, bytes.length);

		Message m = new MessageImpl();
		m.setContent(XMLStreamReader.class, StaxUtils
				.createXMLStreamReader(new ByteArrayInputStream(bytes)));
		setExtensions(m);

		return new SoapMessage(m);
	}

	private void setExtensions(Message m) throws EndpointException {

		Exchange ex = DataHolder.getInstance().getExchange();
		ex.getEndpoint()
				.getEndpointInfo()
				.setProperty("security.callback-handler",
						new PasswordCallbackHandler());
		ex.getEndpoint()
				.getEndpointInfo()
				.setProperty(PolicyConstants.POLICY_OVERRIDE,
						DataHolder.getInstance().getPolicy());

		PhaseInterceptorChain chain = new PhaseInterceptorChain(
				new PhaseManagerImpl().getInPhases());

		m.setInterceptorChain(chain);
		ex.setInMessage(m);
		m.setExchange(ex);
	}

	private Source getSource(SoapMessage message) throws SOAPException {
		SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
		if (soapMessage == null) {
			saajIn.handleMessage(message);
			soapMessage = message.getContent(SOAPMessage.class);
		}

		NodeList it = soapMessage.getSOAPHeader().getChildNodes();

		for (int i = 0; i < it.getLength(); i++) {
			if (it.item(i).getLocalName() != null) {
				Node n = it.item(i);
				QName q = new QName(n.getNamespaceURI(), n.getLocalName());
				Header header = new Header(q, n);
				message.getHeaders().add(header);
			}

		}

		DOMSource source = new DOMSource(soapMessage.getSOAPBody()
				.extractContentAsDocument());

		return source;
	}
}
