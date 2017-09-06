package org.wso2.carbon.sts.security.provider.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.neethi.Policy;
import org.w3c.dom.Document;

public class SecurityScenario {

    /**
     * Modules that need to be engaged for this security scenario
     */
    private List<String> modules = new ArrayList<>();
    private List<String> services = new ArrayList<>();

    private String scenarioId;
    private String summary;
    private String category;
    private String description;
    private boolean isCurrentScenario;
    private String wsuId;
    private String type;
    private boolean isGeneralPolicy;
    private Policy policy;
	private Document policyDocument;

    /**
     * Constructs a new SecurityScenarioDO.
     */
    public SecurityScenario() {
        isGeneralPolicy = true;
    }

    /**
     * Getter for property 'summary'.
     *
     * @return Value for property 'summary'.
     * @see #setSummary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Setter for property 'summary'.
     *
     * @param summary Value to set for property 'summary'.
     * @see #getSummary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Getter for property 'description'.
     *
     * @return Value for property 'description'.
     * @see #setDescription
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for property 'description'.
     *
     * @param description Value to set for property 'description'.
     * @see #getDescription
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property 'scenarioId'.
     *
     * @return Value for property 'scenarioId'.
     * @see #setScenarioId
     */
    public String getScenarioId() {
        return scenarioId;
    }

    /**
     * Setter for property 'scenarioId'.
     *
     * @param scenarioId Value to set for property 'scenarioId'.
     * @see #getScenarioId
     */
    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }


    public void addModule(String module) {
        modules.add(module);
    }

    public void addService(String service) {
        services.add(service);
    }

    /**
     * Getter for property 'currentScenario'.
     *
     * @return Value for property 'currentScenario'.
     * @see #setIsCurrentScenario
     */
    public boolean getIsCurrentScenario() {
        return isCurrentScenario;
    }

    /**
     * Setter for property 'currentScenario'.
     *
     * @param currentScenario Value to set for property 'currentScenario'.
     * @see #isCurrentScenario
     */
    public void setIsCurrentScenario(boolean currentScenario) {
        isCurrentScenario = currentScenario;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getModules() {
        return modules;
    }

    public List<String> getServices() {
        return services;
    }

    public String getWsuId() {
        return wsuId;
    }

    public void setWsuId(String wsuId) {
        this.wsuId = wsuId;
    }

    public boolean isCurrentScenario() {
        return isCurrentScenario;
    }

    public void setCurrentScenario(boolean currentScenario) {
        isCurrentScenario = currentScenario;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getGeneralPolicy() {
        return isGeneralPolicy;
    }

    public void setGeneralPolicy(boolean isGeneralPolicy) {
        this.isGeneralPolicy = isGeneralPolicy;
    }

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}
	
	public Policy getPolicy() {
		return this.policy;
	}
	
	public void addPolicyDocument(Document docPolicy) {
		this.policyDocument = docPolicy;
	}
	
	public Document getPolicyDocument(){
		return this.policyDocument;
	}
}