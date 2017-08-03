package org.wso2.carbon.sts.resource;

import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.wsdl.Definition;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.jaxws.JaxwsServiceBuilder;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.wsdlto.core.WSDLDefinitionBuilder;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.AssertionBuilderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.PolicyEngineImpl;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistryImpl;
import org.apache.cxf.ws.security.policy.WSSecurityPolicyLoader;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.apache.wss4j.policy.SP12Constants;
import org.apache.wss4j.policy.SPConstants;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.carbon.sts.resource.provider.DefaultSecurityTokenServiceProvider;
import org.wso2.carbon.sts.resource.security.SecurityComponent;
import org.wso2.carbon.sts.resource.security.SecurityPolicyServiceImpl;
import org.wso2.carbon.sts.resource.soap.model.Body;
import org.wso2.carbon.sts.resource.soap.model.Envelope;
import org.wso2.carbon.sts.resource.soap.model.Header;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


@Component(
     name = "org.wso2.carbon.sts.resources.STSResource",
     service = Microservice.class,
     immediate = true
)

@SwaggerDefinition(
    info = @Info(
         title = "/SecurityTokenService Endpoint Swagger Definition", version = "1.0",
         description = "STS /SecurityTokenService endpoint",
         license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"))
)
@Path("/services")
public class STSResource extends AbstractResource {
	
	JaxwsServiceBuilder builder;
	
	@POST
	@Path("/wso2carbon-sts")
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public void processRequest(@Context Request request, @Context Response response) {

		try {
			DefaultSecurityTokenServiceProvider provider = new DefaultSecurityTokenServiceProvider();
			provider.setWebServiceContext(WSContext.getInstance()
					.getWSContext());
			Source resp = provider.invoke((Source) request
					.getProperty(Source.class.getName()));
			
			String m = soapToString(buildSoapResponse(resp));
			
			///////////////////////////////////
			/*JAXBContext jc = JAXBContext.newInstance(Envelope.class);
			StreamSource xml = new StreamSource();
	        Marshaller unmarshaller = jc.createMarshaller();*/
	        Envelope e = new Envelope();
	        e.setBody(new Body());
	        e.setHeader(new Header());
	       // QName qname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "STS");
	        //JAXBElement<Envelope> jb = new JAXBElement(qname, Envelope.class, e);
	        ///////////////////////////////////
			response.setEntity(e);
			response.send();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String soapToString(SOAPMessage msg) throws SOAPException, IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		msg.writeTo(stream);
		return new String(stream.toByteArray(), "utf-8");
	}
	
	public SOAPMessage buildSoapResponse(Source response) throws SOAPException, XMLStreamException, IOException, ParserConfigurationException, SAXException, TransformerException {
		
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
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
		
		
		return soapMsg;
	}
	
	@Activate
	public void start(BundleContext c) throws Exception {

		Bus bus = new ExtensionManagerBus();
		bus.setExtension(new AssertionBuilderRegistryImpl(),
				AssertionBuilderRegistry.class);
		bus.setExtension(new PolicyInterceptorProviderRegistryImpl(),
				PolicyInterceptorProviderRegistry.class);
		
		bus.setExtension(new PolicyEngineImpl(bus), PolicyEngine.class);
		
		@SuppressWarnings("unused")
		PolicyBuilderImpl pb = new PolicyBuilderImpl(bus);
		
		AssertionBuilderRegistryImpl reg = (AssertionBuilderRegistryImpl) bus
				.getExtension(AssertionBuilderRegistry.class);	
		reg.setBus(bus);
		
		pb = new PolicyBuilderImpl(bus);
		
		@SuppressWarnings("unused")
		WSSecurityPolicyLoader loader = new WSSecurityPolicyLoader(bus);
		
		/// ???
		new SecurityComponent().processPolicies(c, bus);
		DataHolder.getInstance().setPolicy(new SecurityPolicyServiceImpl().getEffectivePolicy());

		WSDLDefinitionBuilder builder = new WSDLDefinitionBuilder(bus);
		Definition definition = builder.build("ws-trust-1.4-service.wsdl");
		WSDLServiceBuilder wsb = new WSDLServiceBuilder(bus);
		
		List<ServiceInfo> serviceInfo = wsb.buildServices(definition);
		ServiceInfo si = serviceInfo.get(0);
		Service service = new ServiceImpl(si);

		Collection<EndpointInfo> endpointInfo = si.getEndpoints();
		EndpointInfo ei = endpointInfo.iterator().next();

		Endpoint endpoint = new EndpointImpl(bus, service, ei);

		Exchange exchange = new ExchangeImpl();
		exchange.put(Bus.class, bus);
		exchange.put(Service.class, service);
		exchange.put(Endpoint.class, endpoint);

		DataHolder.getInstance().setExchange(exchange);
	}
	
	/*@Reference(
        name = "policy",
        service = SecurityPolicyService.class,
        cardinality = ReferenceCardinality.MANDATORY,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "removePolicy"
	)
	public void addPolicy(SecurityPolicyService provider) {
		
		DataHolder.getInstance().setPolicy(((SecurityPolicyServiceImpl)provider).getEffectivePolicy());
	}
	
	public void removePolicy(SecurityPolicyService provider) {
		
		DataHolder.getInstance().setPolicy(null);
	}*/
}
