package com.opsgenie.integration.jenkins;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 *
 * @author kaganyildiz
 * @version 09/07/17
 */
public class AlertProperties {
    private String tags;
    private String alias;
    private boolean addCommitListToDesc;
    private boolean addFailedTestToDesc;
    private String teams;

    public AlertProperties setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public AlertProperties setAlias(String alias) {
        this.alias = alias;
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

    public boolean isAddCommitListToDesc() {
        return addCommitListToDesc;
    }

    public boolean isAddFailedTestToDesc() {
        return addFailedTestToDesc;
    }

    public String getTeams() {
        return teams;
    }
}
