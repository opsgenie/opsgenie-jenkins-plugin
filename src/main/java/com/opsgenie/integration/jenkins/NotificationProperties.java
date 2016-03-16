package com.opsgenie.integration.jenkins;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 */
public class NotificationProperties {
    private boolean notifyAborted;
    private boolean notifyFailure;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifySuccess;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean isNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public NotificationProperties setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
        return this;
    }

    public NotificationProperties setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
        return this;
    }

    public NotificationProperties setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
        return this;
    }

    public NotificationProperties setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
        return this;
    }

    public NotificationProperties setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
        return this;
    }

    public NotificationProperties setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
        return this;
    }

    public NotificationProperties setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        return this;
    }

}
