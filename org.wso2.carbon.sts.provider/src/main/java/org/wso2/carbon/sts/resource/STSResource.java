package org.wso2.carbon.sts.resource;

import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.soap.SOAPException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.sts.resource.interceptor.PrimaryMessageBus;
import org.wso2.carbon.sts.resource.internal.DataHolder;
import org.wso2.carbon.sts.security.provider.SecurityPolicyService;
import org.wso2.carbon.sts.security.provider.SecurityPolicyServiceImpl;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;


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
	
	@POST
	@Path("/wso2carbon-sts")
	@Consumes(MediaType.TEXT_XML)
	public String processRequest(@Context Request request) {
		PrimaryMessageBus bus = new PrimaryMessageBus();
		String response = null;
		try {
			response = bus.handleMessage(request);

			return response;
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Reference(
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
	}
}
