package com.opsgenie.integration.jenkins;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 */
public class AlertProperties {
    private String tags;
    private String alias;
    private String alertNote;
    private boolean addCommitListToDesc;
    private boolean addFailedTestToDesc;
    private String recipients;
    private String teams;

    public AlertProperties setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public AlertProperties setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public AlertProperties setAlertNote(String alertNote) {
        this.alertNote = alertNote;
        return this;
    }

    public AlertProperties setAddCommitListToDesc(boolean addCommitListToDesc) {
        this.addCommitListToDesc = addCommitListToDesc;
        return this;
    }

    public AlertProperties setAddFailedTestToDesc(boolean addFailedTestToDesc) {
        this.addFailedTestToDesc = addFailedTestToDesc;
        return this;
    }


    public AlertProperties setRecipients(String recipients) {
        this.recipients = recipients;
        return this;
    }

    public AlertProperties setTeams(String teams) {
        this.teams = teams;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public String getAlias() {
        return alias;
    }

    public String getAlertNote() {
        return alertNote;
    }

    public boolean isAddCommitListToDesc() {
        return addCommitListToDesc;
    }

    public boolean isAddFailedTestToDesc() {
        return addFailedTestToDesc;
    }

    public String getRecipients() {
        return recipients;
    }

    public String getTeams() {
        return teams;
    }
}
