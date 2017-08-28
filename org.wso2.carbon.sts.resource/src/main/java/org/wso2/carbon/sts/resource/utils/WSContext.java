package org.wso2.carbon.sts.resource.utils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.security.SecurityContext;
import org.apache.wss4j.common.principal.CustomTokenPrincipal;
import org.wso2.carbon.messaging.Header;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Request;

public class WSContext {
	
	private static WSContext context = new WSContext();
	private WebServiceContext wsContext;
	
	private WSContext() {}
	
	public static WSContext getInstance() {
		return context;
	}
	
	public WebServiceContext getWSContext() {
		return wsContext;
	}
	
	public void buildWebServiceContext(Request request,
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
		msgCtx.put(SoapVersion.class.getName(), Soap12.getInstance());
		msgCtx.setScope(MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);

		WebServiceContext context = new WebServiceContextImpl(msgCtx);

		wsContext = context;
	}
	
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

}
