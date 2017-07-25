package org.wso2.carbon.sts.resource.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.security.wss4j.UsernameTokenInterceptor;
import org.apache.neethi.Policy;
import org.apache.wss4j.common.principal.CustomTokenPrincipal;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

public class PrimaryMessageBus {

	SAAJInInterceptor saajIn = new SAAJInInterceptor();
	SoapMessageInterceptor in = new SoapMessageInterceptor();
	
	public String handleMessage(Request request) throws SOAPException {
		
		Policy policy = DataHolder.getInstance().getPolicy();

		List<ByteBuffer> fullMessageBody = request.getFullMessageBody();
		ByteBuffer buffer = BufferUtil.merge(fullMessageBody);

		SoapMessage soap = null;

		try {
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes, 0, bytes.length);
			buffer.clear();
			bytes = new byte[buffer.capacity()];
			buffer.get(bytes, 0, bytes.length);

			soap = processMessage(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}

		in.setWebServiceContext(buildWebServiceContext(request, soap));

		SOAPMessage soapResponse = null;

		if (policy != null) {
			
			/*PolicyEngine.initialiseInterceptors(policy,
					(Message) soap);
			try{
				new UsernameTokenInterceptor().handleMessage(soap);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			soap.setInterceptorChain(null);*/
			soapResponse = in.handleMessage(soap);
		} else {
			soapResponse = in.handleMessage(soap);
		}

		return soapToString(soapResponse);
	}

	private String soapToString(SOAPMessage msg) throws SOAPException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			msg.writeTo(stream);
			return new String(stream.toByteArray(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private SoapMessage processMessage(byte[] msg) {
		
		Bus bus = new ExtensionManagerBus();
        Service service = new ServiceImpl();
        EndpointInfo ei = new EndpointInfo();
        ei.setAddress("http://nowhere.com/bar/foo");
        
        Endpoint endpoint = null;
        try {
			endpoint = new EndpointImpl(bus, service, ei);
		} catch (EndpointException e) {
			e.printStackTrace();
		}
        
		PhaseInterceptorChain chain = new PhaseInterceptorChain(
				new PhaseManagerImpl().getInPhases());
		Message m = new MessageImpl();
		m.setInterceptorChain(chain);
		
		Exchange exchange = new ExchangeImpl();
		exchange.setInMessage(m);
		exchange.put(Bus.class, bus);
		exchange.put(Service.class, service);
		exchange.put(Endpoint.class, endpoint);
		
		m.setExchange(exchange);
		m.setContent(XMLStreamReader.class,
				StaxUtils.createXMLStreamReader(new ByteArrayInputStream(msg)));	
		m.put(Message.REQUESTOR_ROLE, new Boolean(false));
		m.put(Message.INBOUND_MESSAGE, new Boolean(true));
		//m.put(SoapMessage.HTTP_REQUEST_METHOD, request.);
		
		SoapMessage message = new SoapMessage(m);

		return message;
	}

	// security context object
	private SecurityContext createSecurityContext(final Principal p) {
		return new SecurityContext() {
			public Principal getUserPrincipal() {
				return p;
			}

			public boolean isUserInRole(String role) {
				return false;
			}
		};
	}

	private WebServiceContext buildWebServiceContext(Request request,
			Message soap) {
		MessageContext msgCtx = new WrappedMessageContext(soap);
		Principal principal = new CustomTokenPrincipal("alice");
		Map<String, List<String>> headerMap = new HashMap<>();
		Headers headers = request.getHeaders();
		for (Header h : headers.getAll()) {
			List<String> values = new ArrayList<>();
			values.add(h.getValue());
			headerMap.put(h.getName(), values);
		}
		msgCtx.put(SecurityContext.class.getName(),
				createSecurityContext(principal));
		msgCtx.put(MessageContext.HTTP_REQUEST_HEADERS, headerMap);
		msgCtx.put(MessageContext.HTTP_REQUEST_METHOD, request.getHttpMethod());
		msgCtx.setScope(MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);

		WebServiceContext context = new WebServiceContextImpl(msgCtx);

		return context;
	}
}
