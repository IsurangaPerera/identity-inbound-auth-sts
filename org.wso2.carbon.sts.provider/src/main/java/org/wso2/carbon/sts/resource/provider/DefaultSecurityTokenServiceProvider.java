package org.wso2.carbon.sts.resource.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.event.STSEventListener;
import org.apache.cxf.sts.operation.AbstractOperation;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.operation.TokenRenewOperation;
import org.apache.cxf.sts.operation.TokenValidateOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.apache.cxf.sts.token.renewer.SAMLTokenRenewer;
import org.apache.cxf.sts.token.renewer.TokenRenewer;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.sts.token.validator.TokenValidator;
import org.apache.cxf.sts.token.validator.UsernameTokenValidator;
import org.apache.cxf.sts.token.validator.X509TokenValidator;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
/**
 * A "default" SecurityTokenServiceProvider implementation that defines the Issue and Validate
 * Operations of the STS and adds support for issuing and validating SAML Assertions, and
 * validating UsernameTokens and X.509 Tokens. It also defines the Renew Operation for SAML
 * tokens.
 */
public class DefaultSecurityTokenServiceProvider extends CarbonSecurityTokenServiceProvider {

    @SuppressWarnings("unused")
	private STSPropertiesMBean stsProperties;
    private boolean encryptIssuedToken;
    private List<ServiceMBean> services;
    private boolean returnReferences = true;
    private TokenStore tokenStore;
    private ClaimsManager claimsManager = new ClaimsManager();
    private STSEventListener eventListener;

    public DefaultSecurityTokenServiceProvider() throws Exception {
        super();
    }
    
    public void setWebServiceContext(WebServiceContext context) {
    	super.setWebServiceContext(context);
    }

    public void setReturnReferences(boolean returnReferences) {
        this.returnReferences = returnReferences;
    }

    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public void setStsProperties(STSPropertiesMBean stsProperties) {
        this.stsProperties = stsProperties;
    }

    public void setEncryptIssuedToken(boolean encryptIssuedToken) {
        this.encryptIssuedToken = encryptIssuedToken;
    }

    public void setServices(List<ServiceMBean> services) {
        this.services = services;
    }

    public void setClaimsManager(ClaimsManager claimsManager) {
        this.claimsManager = claimsManager;
    }

    public void setEventListener(STSEventListener listener) {
        this.eventListener = listener;
    }

    @Override
    public Source invoke(Source request) {
        if (getIssueOperation() == null) {
            setIssueOperation(createTokenIssueOperation());
        }
        if (getValidateOperation() == null) {
            setValidateOperation(createTokenValidateOperation());
        }
        if (getRenewOperation() == null) {
            setRenewOperation(createTokenRenewOperation());
        }
        return super.invoke(request);
    }

    private TokenIssueOperation createTokenIssueOperation() {
        TokenIssueOperation issueOperation = new TokenIssueOperation();
        populateAbstractOperation(issueOperation);

        return issueOperation;
    }

    private TokenValidateOperation createTokenValidateOperation() {
        TokenValidateOperation validateOperation = new TokenValidateOperation();
        populateAbstractOperation(validateOperation);

        return validateOperation;
    }

    private TokenRenewOperation createTokenRenewOperation() {
        TokenRenewOperation renewOperation = new TokenRenewOperation();
        populateAbstractOperation(renewOperation);

        List<TokenRenewer> tokenRenewers = new ArrayList<>();
        tokenRenewers.add(new SAMLTokenRenewer());
        renewOperation.setTokenRenewers(tokenRenewers);

        return renewOperation;
    }

    private void populateAbstractOperation(AbstractOperation abstractOperation) {
        List<TokenProvider> tokenProviders = new ArrayList<>();
        tokenProviders.add(new SAMLTokenProvider());

        List<TokenValidator> tokenValidators = new ArrayList<>();
        tokenValidators.add(new SAMLTokenValidator());
        tokenValidators.add(new UsernameTokenValidator());
        tokenValidators.add(new X509TokenValidator());

        abstractOperation.setTokenProviders(tokenProviders);
        abstractOperation.setTokenValidators(tokenValidators);
        
        // STSProperties mockup
        STSPropertiesMBean stsProperties = new StaticSTSProperties();
        Crypto crypto = null;
		try {
			crypto = CryptoFactory.getInstance(getEncryptionProperties());
		} catch (WSSecurityException e) {
			e.printStackTrace();
		}
        stsProperties.setEncryptionCrypto(crypto);
        stsProperties.setSignatureCrypto(crypto);
        stsProperties.setEncryptionUsername("myservicekey");
        stsProperties.setSignatureUsername("mystskey");
        stsProperties.setCallbackHandler(new PasswordCallbackHandler());
        stsProperties.setIssuer("STS");
        
        abstractOperation.setStsProperties(stsProperties);
        abstractOperation.setEncryptIssuedToken(encryptIssuedToken);
        abstractOperation.setServices(services);
        abstractOperation.setReturnReferences(returnReferences);
        abstractOperation.setTokenStore(tokenStore);
        abstractOperation.setClaimsManager(claimsManager);
        abstractOperation.setEventListener(eventListener);
    }
    
    private Properties getEncryptionProperties() {
        Properties properties = new Properties();
        properties.put(
            "org.apache.wss4j.crypto.provider", "org.apache.wss4j.common.crypto.Merlin"
        );
        properties.put("org.apache.wss4j.crypto.merlin.keystore.password", "stsspass");
        properties.put("org.apache.wss4j.crypto.merlin.keystore.file", "stsstore.jks");

        return properties;
    }
}