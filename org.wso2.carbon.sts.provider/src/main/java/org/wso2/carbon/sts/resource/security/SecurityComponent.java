package org.wso2.carbon.sts.resource.security;

import java.io.FileNotFoundException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.Bus;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SecurityComponent {

	XMLInputFactory factory = XMLInputFactory.newInstance();

	public void processPolicies(BundleContext bundleContext, Bus bus) throws Exception {

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
				//new TransportBindingBuilder()
				//new AssertionBuilderFactoryImpl()
				//new SupportingTokensBuilder()
				//new AsymmetricBindingBuilder()
				//new PolicyBuilderImpl()
				//new ConfiguredBeanLocator()
				
				try {
					scenario.setPolicy(builder.getPolicy(streamReader));
				} catch(Exception e) {
					e.printStackTrace();
				}
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
	}
}