package org.wso2.carbon.sts.security.internal;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.AssertionBuilderRegistryImpl;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.PolicyEngineImpl;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistryImpl;
import org.apache.cxf.ws.security.policy.WSSecurityPolicyLoader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.sts.security.provider.SecurityPolicyService;
import org.wso2.carbon.sts.security.provider.SecurityPolicyServiceImpl;
import org.wso2.carbon.sts.security.provider.util.SecurityScenario;
import org.wso2.carbon.sts.security.provider.util.SecurityScenarioDatabase;

@Component(name = "org.wso2.carbon.sts.security.provider", immediate = true)
public class SecurityComponent {

	private ServiceRegistration<?> serviceRegistration;
	XMLInputFactory factory = XMLInputFactory.newInstance();

	@Activate
	protected void start(BundleContext bundleContext) throws Exception {
		
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

		PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
		XMLStreamReader streamReader = null;

		URL resource = bundleContext.getBundle().getResource(
				"/scenarios/scenario-config.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(resource.openStream());
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("Scenario");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;
				String id = eElement.getAttribute("id");

				// Need to clarify what to do with this case
				if (id.equalsIgnoreCase("policyFromRegistry"))
					continue;

				SecurityScenario scenario = new SecurityScenario();

				URL policyPath = bundleContext.getBundle().getResource(
						"/scenarios/" + id + "-policy.xml");

				try {
					streamReader = factory.createXMLStreamReader(policyPath
							.openStream());
				} catch (FileNotFoundException | XMLStreamException e) {

				}
				
				scenario.setPolicy(builder.getPolicy(streamReader));
				scenario.setCategory(eElement.getElementsByTagName("Category")
						.item(0).getTextContent());
				scenario.setScenarioId(id);
				scenario.setDescription(eElement
						.getElementsByTagName("Description").item(0)
						.getTextContent());
				scenario.setSummary(eElement.getElementsByTagName("Summary")
						.item(0).getTextContent());
				scenario.setType(eElement.getElementsByTagName("Type").item(0)
						.getTextContent());
				scenario.setWsuId(eElement.getElementsByTagName("WsuId")
						.item(0).getTextContent());
				
				SecurityScenarioDatabase.put(id, scenario);

			}
		}

		serviceRegistration = bundleContext.registerService(
				SecurityPolicyService.class.getName(),
				new SecurityPolicyServiceImpl(), null);

	}

	@Deactivate
	protected void stop() throws Exception {

		serviceRegistration.unregister();
	}
}