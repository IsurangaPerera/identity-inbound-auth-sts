package org.wso2.carbon.sts.security.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.sts.security.provider.util.SecurityScenario;
import org.wso2.carbon.sts.security.provider.util.SecurityScenarioDatabase;
import org.xml.sax.SAXException;

@Component(
		name = "org.wso2.carbon.sts.security.component",
		immediate = true
)
public class SecurityComponent {

	XMLInputFactory factory = XMLInputFactory.newInstance();
	
	@Activate
	public void start(BundleContext bundleContext) throws Exception {
		
		Bus bus = new ExtensionManagerBus();
		PolicyBuilder builder = bus.getExtension(PolicyBuilder.class);
		
		XMLStreamReader streamReader = null;

		URL resource = bundleContext.getBundle().getResource(
				"/scenarios/scenario-config.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
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
					dBuilder = dbFactory.newDocumentBuilder();
					dbFactory.setIgnoringComments(true);
					Document docPolicy = dBuilder.parse(policyPath.openStream());
					docPolicy.getDocumentElement().normalize();
					scenario.addPolicyDocument(docPolicy);
					
				} catch (ParserConfigurationException | SAXException | IOException e) {
					e.printStackTrace();
				}
				
				try {
					streamReader = factory.createXMLStreamReader(policyPath
							.openStream());
				} catch (FileNotFoundException | XMLStreamException e) {
					e.printStackTrace();
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
				
				SecurityScenarioDatabase.getInstance().put(id, scenario);
			}
		}
	}
	
}