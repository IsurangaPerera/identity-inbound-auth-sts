package org.wso2.carbon.sts.resource.interceptor;

import java.util.Collection;
import java.util.List;

import javax.wsdl.Definition;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.wsdlto.core.WSDLDefinitionBuilder;
import org.apache.cxf.wsdl11.WSDLServiceBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.sts.resource.internal.DataHolder;

@Component(
        name = "org.wso2.carbon.sts.resource.interceptor.MessageBuilderComponent",
        immediate = true
)
public class MessageBuilderComponent {

	@Activate
	public void start(BundleContext c) {

		try {
			Bus bus = new ExtensionManagerBus();
			((ExtensionManagerBus)bus).initialize();

			WSDLDefinitionBuilder builder = new WSDLDefinitionBuilder(bus);
			Definition definition = builder.build(c.getBundle().getResource("ws-trust-1.4-service.wsdl").toString());
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

		} catch (Exception e) {
			// log here
		}
	}

}
