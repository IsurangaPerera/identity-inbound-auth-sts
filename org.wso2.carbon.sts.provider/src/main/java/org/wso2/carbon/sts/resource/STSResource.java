package org.wso2.carbon.sts.resource;

import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.jaxws.JaxwsServiceBuilder;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.sts.resource.provider.DefaultSecurityTokenServiceProvider;
import org.wso2.carbon.sts.resource.utils.SOAPUtils;
import org.wso2.carbon.sts.resource.utils.WSContext;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;


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
	
	SOAPUtils instance = SOAPUtils.getInstance();
	
	@POST
	@Path("/wso2carbon-sts")
	@Consumes(MediaType.TEXT_XML)
	
	public void processRequest(@Context Request request, @Context Response response) {

		try {
			DefaultSecurityTokenServiceProvider provider = new DefaultSecurityTokenServiceProvider();
			provider.setWebServiceContext(WSContext.getInstance()
					.getWSContext());
			Source resp = provider.invoke((Source) request
					.getProperty(Source.class.getName()));
			
			String m = instance.soapToString(instance.buildSoapResponse(resp));
			
			response.setEntity(m);
		
		} catch (SoapFault fault) {
			response.setEntity(instance.soapToString(instance.createSoapFault(fault)));
		} catch (Exception e) {
			//log here
		}
		
		response.send();
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
