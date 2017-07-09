package com.opsgenie.integration.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 *
 * @author kaganyildiz
 * @version 09/07/17
 */
public class OpsGenieNotificationRequest {
    private String apiKey;
    private String apiUrl;
    private AlertProperties alertProperties;
    private AbstractBuild build;
    private BuildListener listener;

    public String getApiKey() {
        return apiKey;
    }

    public OpsGenieNotificationRequest setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public OpsGenieNotificationRequest setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public AlertProperties getAlertProperties() {
        return alertProperties;
    }

    public OpsGenieNotificationRequest setAlertProperties(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
        return this;
    }

    public AbstractBuild getBuild() {
        return build;
    }

    public OpsGenieNotificationRequest setBuild(AbstractBuild build) {
        this.build = build;
        return this;
    }

    public BuildListener getListener() {
        return listener;
    }

    public OpsGenieNotificationRequest setListener(BuildListener listener) {
        this.listener = listener;
        return this;
    }
}
