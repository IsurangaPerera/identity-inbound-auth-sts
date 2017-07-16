package org.wso2.carbon.sts.provider;

import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

import java.nio.ByteBuffer;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.message.MessageImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.BufferUtil;

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
	
	@GET
	@Path("/wso2carbon-sts")
	@Consumes(MediaType.TEXT_XML)
	public void processRequest(@Context Request request)
			{
		List<ByteBuffer> fullMessageBody = request.getFullMessageBody();
		ByteBuffer buffer = BufferUtil.merge(fullMessageBody);

		SoapMessage soap = null;
		
		try {
			soap = processMessage(buffer.array());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
			
	}
	
	@Activate
	public void activate() {
		System.out.println("Bundle Activated$$$$$$$$$$$$$##############@@@@@@@@@@@@@@@@@@@@@@@@@@22222");	
	}
}
