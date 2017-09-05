package org.wso2.carbon.sts.resource.interceptor;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.CheckFaultInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapActionInInterceptor;
import org.apache.cxf.binding.soap.interceptor.StartBodyInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.carbon.sts.resource.utils.SOAPUtils;
import org.wso2.carbon.sts.resource.utils.WSContext;
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

	public static final String WST_NS_05_12 = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
	public static final QName FAILED_AUTH = new QName(WST_NS_05_12,
			"FailedAuthentication");
	public static final QName INVALID_REQUEST = new QName(WST_NS_05_12,
			"InvalidRequest");

	@Override
	public void postCall(Request request, int status,
			ServiceMethodInfo serviceMethodInfo) throws Exception {

		/*
		 * SoapHeaderOutFilterInterceptor(); SoapPreProtocolOutInterceptor();
		 * SoapOutInterceptor();
		 */

	}

	@Override
	public boolean preCall(Request request, Response response,
			ServiceMethodInfo smi) throws SoapFault {

		if (METHOD.equals(smi.getMethodName()) && request.getHttpMethod().equalsIgnoreCase("POST")) {

			SAAJInInterceptor saajIn = new SAAJInInterceptor();
			boolean faultExist = false;
			DOMSource source = null;

			SoapMessage message = processMessage(request);
			WSContext.getInstance().buildWebServiceContext(request, message);

			message.put(Message.REQUESTOR_ROLE, Boolean.FALSE);
			message.put(Message.INBOUND_MESSAGE, Boolean.TRUE);
			message.put(SoapMessage.HTTP_REQUEST_METHOD,
					request.getHttpMethod());
			message.put(Message.CONTENT_TYPE, request.getContentType());
			message.put("endpoint-processes-headers",
					"{http://cxf.apache.org/outofband/Header}outofbandHeader");

			InterceptorChain chain = message.getInterceptorChain();

			chain.add((org.apache.cxf.interceptor.Interceptor<? extends Message>) saajIn
					.getAdditionalInterceptors().iterator().next());
			chain.add(new ReadHeadersInterceptor(message.getExchange().getBus()));
			chain.add(new MustUnderstandInterceptor());
			chain.add(new SoapActionInInterceptor());
			chain.add(new CheckFaultInterceptor());
			chain.add(new StartBodyInterceptor());
			chain.add(new PolicyInInterceptor());
			chain.add(saajIn);

			chain.doIntercept(message);

			if (message.getContent(Exception.class) != null) {
				SoapFault fault = new SoapFault(message.getContent(
						Exception.class).getMessage(), FAILED_AUTH);

				response.setEntity(SOAPUtils.getInstance().soapToString(
						SOAPUtils.getInstance().createSoapFault(fault)));

				faultExist = true;
			}

			if (!faultExist) {

				try {
					source = new DOMSource(message
							.getContent(SOAPMessage.class).getSOAPBody()
							.extractContentAsDocument());
					request.setProperty(Source.class.getName(), source);
				} catch (SOAPException e) {
					faultExist = true;
					// log here
				}
			}

			if (faultExist) {
				response.send();
				return false;
			}
		}
		return true;
	}

	private SoapMessage processMessage(Request request) {
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

	private void setExtensions(Message m) {

		Exchange ex = DataHolder.getInstance().getExchange();
		CallbackHandler cb = DataHolder.getInstance().getPasswordCallbackHandler();
		ex.getEndpoint()
				.getEndpointInfo()
				.setProperty("security.callback-handler", cb);
		ex.getEndpoint()
				.getEndpointInfo()
				.setProperty(PolicyConstants.POLICY_OVERRIDE,
						DataHolder.getInstance().getPolicy());
		/*
		 * ex.getEndpoint() .getEndpointInfo()
		 * .setProperty(SecurityConstants.ENABLE_STREAMING_SECURITY,
		 * Boolean.TRUE);
		 */

		PhaseInterceptorChain chain = new PhaseInterceptorChain(
				new PhaseManagerImpl().getInPhases());

		m.setInterceptorChain(chain);
		ex.setInMessage(m);
		m.setExchange(ex);
	}

}
