package org.wso2.carbon.sts.security.mgt.internal;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.neethi.PolicyBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.sts.security.mgt.SecurityPolicyManager;
import org.wso2.carbon.sts.security.mgt.SecurityPolicyProvider;
import org.wso2.carbon.sts.security.util.SecurityScenario;

@Component(name = "org.wso2.carbon.sts.security.mgt.internal.ServiceComponent", immediate = true)
public class ServiceComponent {

	private ServiceRegistration<?> serviceRegistration;
	XMLInputFactory factory = XMLInputFactory.newInstance();

	@Activate
	protected void start(BundleContext bundleContext) throws Exception {

		PolicyBuilder builder = new PolicyBuilder();
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

			}
		}

		serviceRegistration = bundleContext.registerService(
				SecurityPolicyProvider.class.getName(),
				SecurityPolicyManager.getInstance(), null);
	}

	@Deactivate
	protected void stop() throws Exception {

		serviceRegistration.unregister();
	}
}
