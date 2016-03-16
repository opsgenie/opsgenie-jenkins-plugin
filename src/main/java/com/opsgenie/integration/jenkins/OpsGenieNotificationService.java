package com.opsgenie.integration.jenkins;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.model.alert.CreateAlertRequest;
import com.ifountain.opsgenie.client.model.alert.CreateAlertResponse;
import hudson.Util;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.model.JenkinsLocationConfiguration;

import java.io.PrintStream;
import java.util.*;

/**
 * @author Omer Ozkan
 * @version 16/03/16
 */
public class OpsGenieNotificationService {

    private AbstractBuild build;
    private AbstractProject project;
    private JobResult jobResult;
    private AlertProperties alertProperties;
    private NotificationProperties notificationProperties;
    private PrintStream logger;
    private OpsGenieClient opsGenieClient;
    private CreateAlertRequest createAlertRequest;

    public OpsGenieNotificationService(OpsGenieNotificationRequest request) {
        build = request.getBuild();
        project = build.getProject();

        alertProperties = request.getAlertProperties();
        notificationProperties = request.getNotificationProperties();
        logger = request.getListener().getLogger();
        jobResult = JobResult.fromBuild(build);

        opsGenieClient = new OpsGenieClient();
        createAlertRequest = new CreateAlertRequest();
        createAlertRequest.setApiKey(request.getApiKey());
    }

    public boolean sendPostBuildAlert() {
        populateCreateAlertRequestWithMandatoryFields();

        HashMap<String, String> details = new HashMap<>();
        details.put("Params", formatBuildVariables());
        details.put("Duration", build.getDurationString());
        details.put("Status", build.getResult().toString());
        details.put("Url", new JenkinsLocationConfiguration().getUrl() + build.getUrl());


        StringBuilder descriptionBuilder = new StringBuilder();

        if (alertProperties.isAddCommitListToDesc()) {
            descriptionBuilder.append(formatCommitList((build.getChangeSet())));
        }

        if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
            Set<User> culprits = build.getCulprits();
            if (!culprits.isEmpty()) {
                details.put("Culprits", formatCulprits(culprits));
            }
        }

        AbstractTestResultAction testResult = build.getAction(AbstractTestResultAction.class);
        if (testResult != null) {
            String testSummary = String.format("Passed -> %s\nFailed -> %s\nSkipped -> %s",
                    testResult.getTotalCount() - testResult.getFailCount() - testResult.getSkipCount(),
                    testResult.getFailCount(),
                    testResult.getSkipCount());
            details.put("Test Summary", testSummary);

            if (build.getResult() == Result.UNSTABLE && alertProperties.isAddFailedTestToDesc()) {
                descriptionBuilder.append(formatFailedTests(testResult.getFailedTests()));
            }
        }

        createAlertRequest.setDetails(details);
        createAlertRequest.setDescription(descriptionBuilder.toString());
        return sendAlertToOpsGenie();
    }

    private void populateCreateAlertRequestWithMandatoryFields() {

        createAlertRequest.setMessage(String.format("%s (%s) [%s]", project.getName(), build.getDisplayName(),
                jobResult.getName()));
        createAlertRequest.setTags(splitStringWithComma(alertProperties.getTags()));
        createAlertRequest.setRecipients(splitStringWithComma(alertProperties.getRecipients()));
        createAlertRequest.setTeams(splitStringWithComma(alertProperties.getTeams()));


        if (!Util.fixNull(alertProperties.getAlias()).isEmpty()) {
            createAlertRequest.setAlias(alertProperties.getAlias());
        }

        if (!Util.fixNull(alertProperties.getAlertNote()).isEmpty()) {
            createAlertRequest.setNote(alertProperties.getAlertNote());
        }
    }

    private boolean sendAlertToOpsGenie() {
        if (jobResult.shouldSendAlert(notificationProperties)) {
            try {
                CreateAlertResponse createAlertResponse = opsGenieClient.alert().createAlert(createAlertRequest);
                logger.println("OpsGenie Alert created id: " + createAlertResponse.getAlertId());
                return true;
            } catch (Exception e) {
                logger.println("Could not create alert. Reason: " + e.getMessage());
                e.printStackTrace(logger);
                return false;
            }
        } else {
            logger.println("Skipping OpsGenie Notification");
            return true;
        }
    }

    private String formatBuildVariables() {
        StringBuilder buildVariablesBuilder = new StringBuilder();
        Map<String, String> buildVariables = build.getBuildVariables();
        for (Map.Entry<String, String> entry : buildVariables.entrySet()) {
            buildVariablesBuilder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return buildVariablesBuilder.toString();
    }

    private String formatFailedTests(List<? extends TestResult> failedTests) {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("<h3>Failed Tests</h3>");
        for (TestResult failedTest : failedTests) {
            descriptionBuilder.append(String.format("<strong>%s</strong>\n", failedTest.getFullName()))
                    .append(failedTest.getErrorDetails()).append("\n\n");
        }
        return descriptionBuilder.toString();
    }

    private String formatCulprits(Set<User> culprits) {
        StringBuilder culpritsBuilder = new StringBuilder();
        for (User culprit : culprits) {
            culpritsBuilder.append(culprit.getFullName()).append(",");
        }
        return culpritsBuilder.toString();
    }

    private String formatCommitList(ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet) {
        StringBuilder commitListBuildler = new StringBuilder();
        commitListBuildler.append("<h3>Last Commiters</h3>");
        if (changeLogSet.isEmptySet()) {
            commitListBuildler.append("No changes.\n\n");
        }

        for (ChangeLogSet.Entry entry : changeLogSet) {
            commitListBuildler
                    .append(entry.getMsg())
                    .append(" - <strong>")
                    .append(entry.getAuthor().getDisplayName())
                    .append("</strong>\n");
        }
        return commitListBuildler.toString();
    }

    private List<String> splitStringWithComma(String unparsed) {
        if (unparsed == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(unparsed.split(","));
    }

    public boolean sendPreBuildAlert() {
        CauseAction causeAction = build.getAction(CauseAction.class);

        StringBuilder descriptionBuilder = new StringBuilder();

        for (Cause cause : causeAction.getCauses()) {
            descriptionBuilder.append(cause.getShortDescription()).append("\n");
        }

        createAlertRequest.setMessage(String.format("%s (%s) [started]", project.getName(), build.getDisplayName()));
        createAlertRequest.setDescription(descriptionBuilder.toString());
        return sendAlertToOpsGenie();
    }

}
