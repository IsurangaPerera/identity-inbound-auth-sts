package org.wso2.carbon.sts.provider;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.claims.ClaimsHandler;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.sts.provider.internal.DataHolder;

@Component(
		name = "org.wso2.carbon.cxf.sts.provider",
		service = Provider.class,
		immediate = true
)

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
        
		abstractOperation.setStsProperties(DataHolder.getInstance()
				.getStaticPropertyBean());
		abstractOperation.setEncryptIssuedToken(encryptIssuedToken);
		abstractOperation.setServices(services);
		abstractOperation.setReturnReferences(returnReferences);
		abstractOperation.setTokenStore(tokenStore);
		
		claimsManager.setClaimHandlers(DataHolder.getInstance().getCalimsHandler());
		abstractOperation.setClaimsManager(claimsManager);
		
		abstractOperation.setEventListener(eventListener);
    }
    
    @Reference(
            name = "StaticProperty",
            service = STSPropertiesMBean.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.STATIC
    )
    public void addStaticPropertyBean(STSPropertiesMBean staticProperty) {
    	DataHolder.getInstance().setStaticPropertyBean(staticProperty);
    }	
    
    @Reference(
            name = "ClaimsHandler",
            service = ClaimsHandler.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.STATIC
    )
    public void addClaimsHandler(ClaimsHandler claimsHandler) {
    	DataHolder.getInstance().addClaimsHandler(claimsHandler);
    }	
}