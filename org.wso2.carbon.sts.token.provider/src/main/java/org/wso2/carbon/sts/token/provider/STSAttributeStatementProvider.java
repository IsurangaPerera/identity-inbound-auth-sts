package org.wso2.carbon.sts.token.provider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.apache.cxf.sts.token.provider.AttributeStatementProvider;
import org.apache.cxf.sts.token.provider.TokenProviderParameters;
import org.apache.wss4j.common.saml.bean.AttributeBean;
import org.apache.wss4j.common.saml.bean.AttributeStatementBean;
import org.apache.wss4j.dom.WSConstants;

public class STSAttributeStatementProvider implements
		AttributeStatementProvider {

	@Override
	public AttributeStatementBean getStatement(
			TokenProviderParameters providerParameters) {

		ClaimsManager claimsManager = providerParameters.getClaimsManager();
		ProcessedClaimCollection retrievedClaims = new ProcessedClaimCollection();
		if (claimsManager != null) {
			ClaimsParameters params = new ClaimsParameters();

			params.setAdditionalProperties(providerParameters
					.getAdditionalProperties());
			
			params.setAppliesToAddress(providerParameters.getAppliesToAddress());

			retrievedClaims = claimsManager.retrieveClaimValues(
					providerParameters.getRequestedPrimaryClaims(), params);
		}
		
		if (retrievedClaims == null) {
            return null;
        }
		
		Iterator<ProcessedClaim> claimIterator = retrievedClaims.iterator();
        if (!claimIterator.hasNext()) {
            return null;
        }
		
        List<AttributeBean> attributeList = new ArrayList<AttributeBean>();
        String tokenType = providerParameters.getTokenRequirements().getTokenType();

        AttributeStatementBean attrBean = new AttributeStatementBean(); 

        while (claimIterator.hasNext()) {
            ProcessedClaim claim = claimIterator.next();
            AttributeBean attributeBean = new AttributeBean(); 

            URI name = claim.getClaimType();
            if (WSConstants.WSS_SAML2_TOKEN_TYPE.equals(tokenType)
                    || WSConstants.SAML2_NS.equals(tokenType)) {
                attributeBean.setQualifiedName(name.toString());
                //attributeBean.setNameFormat(claim.getNameFormat());
            } else {
                attributeBean.setSimpleName(name.toString());
                //attributeBean.setQualifiedName(claim.getName());
            }
            attributeBean.setAttributeValues(claim.getValues()); 

            attributeList.add(attributeBean);
        }
        attrBean.setSamlAttributes(attributeList);

        return attrBean;

	}

}
