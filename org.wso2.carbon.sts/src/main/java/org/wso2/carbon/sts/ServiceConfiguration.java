package org.wso2.carbon.sts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Endpoint;

import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.base.ServerConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.DefaultAttributeStatementProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.StaticSTSProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {
	
	public static final String HOST_NAME = "HostName";
    public static final String STS_HOST_NAME = "STSHostName";

	private static final Log log = LogFactory
			.getLog(ServiceConfiguration.class);
	private static ServerConfiguration serverConfig = ServerConfiguration
			.getInstance();

	public static final String WSDL_LOCATION = null;

	@Bean(name = Bus.DEFAULT_BUS_ID)
	public SpringBus springBus() {
		return new SpringBus();
	}

	@Bean
	public SAMLTokenProvider transportSamlTokenProvider() {
		SAMLTokenProvider tokenProvider = new SAMLTokenProvider();

		List<AttributeStatementProvider> providerList = new ArrayList<>();
		providerList.add(new DefaultAttributeStatementProvider());
		tokenProvider.setAttributeStatementProviders(providerList);

		return tokenProvider;
	}

	@Bean
	public StaticSTSProperties transportSTSProperties() {
		String callBackHandlerName = null;
		String issuerName = null;
		
		StaticSTSProperties prop = new StaticSTSProperties();

		callBackHandlerName = serverConfig
				.getFirstProperty("Security.STSCallBackHandlerName");

		issuerName = serverConfig.getFirstProperty(STS_HOST_NAME);

		if (StringUtils.isBlank(issuerName)) {
			issuerName = "https://" + serverConfig.getFirstProperty(HOST_NAME);
		}
		
		
		
		prop.setCallbackHandlerClass(callBackHandlerName);
		prop.setIssuer(issuerName);

		return prop;
	}

	@Bean
	public TokenIssueOperation tokenIssueOperation() {
		TokenIssueOperation issueOperation = new TokenIssueOperation();

		List<TokenProvider> tokenProviders = new ArrayList<>();
		tokenProviders.add(transportSamlTokenProvider());
		
		/* carbon.xml default entry has to be changed for C4
		 * org.apache.cxf.sts.cache.DefaultInMemoryTokenStore can be used for CXF
		 */
		//String tokenStore = serverConfig.getFirstProperty("Security.TokenStoreClassName").getClass()
		
		issueOperation.setTokenProviders(tokenProviders);
		issueOperation.setStsProperties(transportSTSProperties());
		//issueOperation.setTokenStore(tokenStore);

		return issueOperation;
	}

	@Bean
	public SecurityTokenServiceProvider transportSTSProvider() {
		SecurityTokenServiceProvider sts = null;
		try {
			sts = new SecurityTokenServiceProvider();
		} catch (Exception e) {
			log.debug("unable to create SecurityTokenServiceProvider");
		}

		sts.setIssueOperation(tokenIssueOperation());
		// sts.setRenewOperation(renewOperation);
		// sts.setValidateOperation(validateOperation);
		// sts.setCancelOperation(cancelOperation);

		return sts;
	}

	@Bean
	public Endpoint endpoint() {
		EndpointImpl endpoint = null;
		try {
			endpoint = new EndpointImpl(springBus(), transportSTSProvider());
		} catch (Exception e) {
			e.printStackTrace();
		}
		endpoint.setWsdlLocation(WSDL_LOCATION);
		endpoint.publish("http://localhost:8081/services/baeldung");
		return endpoint;
	}

}
