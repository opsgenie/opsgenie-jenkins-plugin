package com.opsgenie.integration.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 */
public class OpsGenieNotificationRequest {
    private String apiKey;
    private NotificationProperties notificationProperties;
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

    public NotificationProperties getNotificationProperties() {
        return notificationProperties;
    }

    public OpsGenieNotificationRequest setNotificationProperties(NotificationProperties notificationProperties) {
        this.notificationProperties = notificationProperties;
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
