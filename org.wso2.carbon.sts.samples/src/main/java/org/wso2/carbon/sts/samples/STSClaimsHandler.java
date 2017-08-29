package org.wso2.carbon.sts.samples;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.rt.security.claims.Claim;
import org.apache.cxf.rt.security.claims.ClaimCollection;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsParameters;
import org.apache.cxf.sts.claims.ProcessedClaim;
import org.apache.cxf.sts.claims.ProcessedClaimCollection;
import org.osgi.service.component.annotations.Component;

@Component(
		name = "org.wso2.carbon.sts.ClaimsHandler",
		service = ClaimsHandler.class,
		immediate = true
)
public class STSClaimsHandler implements ClaimsHandler {

	public static final URI ROLE = URI
			.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/role");
	public static final URI GIVEN_NAME = URI
			.create("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname");
	public static final URI LANGUAGE = URI
			.create("http://schemas.mycompany.com/claims/language");

	public ProcessedClaimCollection retrieveClaimValues(ClaimCollection claims,
			ClaimsParameters parameters) {

		if (claims != null && !claims.isEmpty()) {
			ProcessedClaimCollection claimCollection = new ProcessedClaimCollection();
			for (Claim requestClaim : claims) {
				ProcessedClaim claim = new ProcessedClaim();
				claim.setClaimType(requestClaim.getClaimType());
				claim.setIssuer("Test Issuer");
				claim.setOriginalIssuer("Original Issuer");
				if (ROLE.equals(requestClaim.getClaimType())) {
					if ("alice".equals(parameters.getPrincipal().getName())) {
						claim.addValue("admin-user");
					} else {
						claim.addValue("ordinary-user");
					}
				} else if (GIVEN_NAME.equals(requestClaim.getClaimType())) {
					claim.addValue(parameters.getPrincipal().getName());
				} else if (LANGUAGE.equals(requestClaim.getClaimType())) {
					claim.addValue(parameters.getPrincipal().getName());
				}
				claimCollection.add(claim);
			}
			return claimCollection;
		}
		return null;
	}

	public List<URI> getSupportedClaimTypes() {
		List<URI> list = new ArrayList<>();
		list.add(ROLE);
		list.add(GIVEN_NAME);
		list.add(LANGUAGE);
		return list;
	}
}