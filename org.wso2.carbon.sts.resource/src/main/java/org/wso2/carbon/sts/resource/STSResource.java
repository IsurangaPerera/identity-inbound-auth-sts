package org.wso2.carbon.sts.resource;

import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import javax.security.auth.callback.CallbackHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Provider;

import org.apache.cxf.binding.soap.SoapFault;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.wso2.carbon.sts.provider.DefaultSecurityTokenServiceProvider;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.carbon.sts.resource.utils.SOAPUtils;
import org.wso2.carbon.sts.resource.utils.WSContext;
import org.wso2.carbon.sts.security.provider.SecurityPolicyService;
import org.wso2.carbon.sts.security.provider.SecurityPolicyServiceImpl;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
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
	
	private SOAPUtils instance = SOAPUtils.getInstance();
	
	@POST
	@Path("/wso2carbon-sts")
	@Consumes(MediaType.TEXT_XML)
	public void processRequest(@Context Request request, @Context Response response) {

		try {
			DefaultSecurityTokenServiceProvider provider = DataHolder.getInstance().getServiceProvider();
			provider.setWebServiceContext(WSContext.getInstance()
					.getWSContext());
			Source resp = provider.invoke((Source) request
					.getProperty(Source.class.getName()));
			
			String m = instance.soapToString(instance.buildSoapResponse(resp));
			
			response.setEntity(m);
		
		} catch (SoapFault fault) {
			response.setEntity(instance.soapToString(instance
					.createSoapFault(fault)));
		} catch (Exception e) {
			//log here
		}
		
		response.send();
	}
	
	@GET
	@Path("/wso2carbon-sts")
	public String getWSDL(@Context Request request) {
		
		return DataHolder.getInstance().getWSDL();
	}
	
	@Activate
	public void start(BundleContext bundleContext) {
		
		URL resource = bundleContext.getBundle().getResource(
				"ws-trust-1.4-service.wsdl");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;
		try {
			dbFactory.setIgnoringComments(true);
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(resource.openStream());
			Document policyDoc = DataHolder.getInstance().getPolicyDocument();
			
			Node policyElement = policyDoc.getFirstChild();
			Node wsdl = doc.getDocumentElement();
			Node firstDocImportedNode = doc.adoptNode(policyElement);
			
			wsdl.appendChild(firstDocImportedNode);
			
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        
	        DataHolder.getInstance().addWSDL(sw.toString());
	    
		} catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
		
	}
	
	@Reference(
	    name = "provider",
	    service = Provider.class,
	    cardinality = ReferenceCardinality.MANDATORY,
	    policy = ReferencePolicy.STATIC
	)
	public void addServiceProvider(@SuppressWarnings("rawtypes") Provider provider) {
		
		DataHolder.getInstance().setServiceProvider(((DefaultSecurityTokenServiceProvider)provider));
	}
	
	@Reference(
        name = "policy",
        service = SecurityPolicyService.class,
        cardinality = ReferenceCardinality.MANDATORY,
        policy = ReferencePolicy.STATIC
	)
	public void addPolicy(SecurityPolicyService provider) {
		
		DataHolder.getInstance().setPolicy(((SecurityPolicyServiceImpl)provider).getEffectivePolicy());
		DataHolder.getInstance().setPolicyDocument(((SecurityPolicyServiceImpl)provider).gePolicyDocument());
	}
	
	@Reference(
	    name = "callbackHandler",
	    service = CallbackHandler.class,
	    cardinality = ReferenceCardinality.MANDATORY,
	    policy = ReferencePolicy.STATIC
	)
	public void addPasswordCallbackHandler(CallbackHandler callbackHandler) {
			
		DataHolder.getInstance().setPasswordCallbackHandler(callbackHandler);
	}
}
