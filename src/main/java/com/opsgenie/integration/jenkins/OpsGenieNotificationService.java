package com.opsgenie.integration.jenkins;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import hudson.ProxyConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

/**
 * @author Omer Ozkan
 * @author kaganyildiz
 * @version 09/07/17
 */

public class OpsGenieNotificationService {
    private final static String INTEGRATION_PATH = "/v1/json/jenkins";

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(OpsGenieNotificationService.class);

    private AbstractBuild build;
    private AbstractProject project;
    private AlertProperties alertProperties;
    private PrintStream consoleOutputLogger;
    private Map<String, Object> requestPayload;
    private ObjectMapper mapper;
    private OpsGenieNotificationRequest request;

    public OpsGenieNotificationService(OpsGenieNotificationRequest request) {
        build = request.getBuild();
        project = build.getProject();

        this.request = request;
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        requestPayload = new HashMap<>();

        alertProperties = request.getAlertProperties();
        consoleOutputLogger = request.getListener().getLogger();
    }

    private boolean checkResponse(String res) {
        try {
            ResponseFromOpsGenie response = mapper.readValue(res, ResponseFromOpsGenie.class);
            if (response.status.equals("successful")) {
                consoleOutputLogger.println("Sending job data to OpsGenie is done");
                return true;
            } else {
                consoleOutputLogger.println(String.format("Response status is : %s , failed", response.status));
                logger.error(String.format("Response status is : %s , failed", response.status));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace(consoleOutputLogger);
            logger.error("Exception while checking response" + e.getMessage());
        }
        return !res.isEmpty();
    }

    private String sendWebhookToOpsGenie(String data) {
        try {
            String apiUrl = this.request.getApiUrl();
            String apiKey = this.request.getApiKey();


            URI inputURI = new URI(apiUrl);
            String scheme = "https";
            String host = apiUrl;
            if (inputURI.isAbsolute()) {
                scheme = inputURI.getScheme();
                host = inputURI.getHost();
            }

            URI uri = new URIBuilder()
                    .setScheme(scheme)
                    .setHost(host)
                    .setPath(INTEGRATION_PATH)
                    .addParameter("apiKey", apiKey)
                    .build();

            HttpClient client;

            HttpPost post = new HttpPost(uri);
            StringEntity params = new StringEntity(data);
            post.addHeader("content-type", "application/x-www-form-urlencoded");
            post.setEntity(params);

            if (Jenkins.getInstance() != null && Jenkins.getInstance().proxy != null) {
                // A proxy is configured, so we will use it for this request as well.
                ProxyConfiguration proxy = Jenkins.getInstance().proxy;

                // Check if the host of opsgenie is excluded from the proxy.
                Boolean isHostExcludedFromProxy = false;
                for (Pattern pattern: proxy.getNoProxyHostPatterns()) {
                    if (pattern.matcher(host).matches()) {
                        isHostExcludedFromProxy = true;
                    }
                }

                if (!isHostExcludedFromProxy) {
                    // Host is not excluded from proxy.
                    if (proxy.getUserName() != null && proxy.getPassword() != null) {
                        // Authentication for proxy is configured.
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(new AuthScope(proxy.name, proxy.port), new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword()));
                        client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
                    } else {
                        client = HttpClientBuilder.create().build();
                    }

                    HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                    RequestConfig config = RequestConfig.custom()
                            .setProxy(proxyHost)
                            .build();

                    post.setConfig(config);
                } else {
                    // Proxy is configured but opsgenie should not use the proxy.
                    client = HttpClientBuilder.create().build();
                }
            } else {
                // No proxy is configured.
                client = HttpClientBuilder.create().build();
            }

            consoleOutputLogger.println("Sending job data to OpsGenie...");
            HttpResponse response = client.execute(post);

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace(consoleOutputLogger);
            logger.error("Exception while sending webhook: " + e.getMessage());
        }
        return "";
    }

    protected boolean sendPreBuildPayload() {

        populateRequestPayloadWithMandatoryFields();

        requestPayload.put("isPreBuild", "true");

        if(alertProperties.getBuildStartPriority() != null) {
            requestPayload.put("priority", alertProperties.getBuildStartPriority().getValue());
        }

        String payload = "";
        try {
            payload = this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestPayload);
        } catch (Exception e) {
            e.printStackTrace(consoleOutputLogger);
            logger.error("Exception while serializing pre request:" + e.getMessage());
        }
        String response = sendWebhookToOpsGenie(payload);

        return checkResponse(response);
    }

    private String formatCommitList(ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet) {
        StringBuilder commitListBuilder = new StringBuilder();
        if (changeLogSet.isEmptySet()) {
            commitListBuilder.append("No changes.\n\n");
        }

        for (ChangeLogSet.Entry entry : changeLogSet) {
            commitListBuilder
                    .append(entry.getMsg())
                    .append(" - <strong>")
                    .append(entry.getAuthor().getDisplayName())
                    .append("</strong><br>\n");
        }
        return commitListBuilder.toString();
    }

    private String formatFailedTests(List<? extends TestResult> failedTests) {
        StringBuilder testResultBuilder = new StringBuilder();
        for (TestResult failedTest : failedTests) {
            testResultBuilder
                    .append(String.format("<strong>%s</strong>%n", failedTest.getFullName()));

            if (StringUtils.isNotBlank(failedTest.getErrorDetails())) {
                testResultBuilder.append(failedTest.getErrorDetails());
            }

            testResultBuilder.append("\n\n");
        }
        return testResultBuilder.toString();
    }

    private String formatBuildVariables() {
        StringBuilder buildVariablesBuilder = new StringBuilder();
        Map<String, String> buildVariables = build.getBuildVariables();
        for (Map.Entry<String, String> entry : buildVariables.entrySet()) {
            buildVariablesBuilder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return buildVariablesBuilder.toString();
    }

    protected boolean sendAfterBuildData() {
        populateRequestPayloadWithMandatoryFields();

        if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
            Set<User> culprits = build.getCulprits();
            if (!culprits.isEmpty()) {
                requestPayload.put("culprits", formatCulprits(culprits));
            }
        }

        StringBuilder descriptionBuilder = new StringBuilder();
        AbstractTestResultAction testResult = build.getAction(AbstractTestResultAction.class);
        if (testResult != null) {
            String passedTestCount = Integer.toString(testResult.getTotalCount() - testResult.getFailCount() - testResult.getSkipCount());
            requestPayload.put("passedTestCount", passedTestCount);
            String failedTestCount = Integer.toString(testResult.getFailCount());
            requestPayload.put("failedTestCount", failedTestCount);
            String skippedTestCount = Integer.toString(testResult.getSkipCount());
            requestPayload.put("skippedTestCount", skippedTestCount);

            if (build.getResult() == Result.UNSTABLE || build.getResult() == Result.FAILURE) {
                descriptionBuilder.append(formatFailedTests(testResult.getFailedTests()));
                requestPayload.put("failedTests", descriptionBuilder);
            }
        }

        requestPayload.put("commitList", formatCommitList(build.getChangeSet()));
        AbstractBuild previousBuild = build.getPreviousBuild();
        if (previousBuild != null) {
            String previousDisplayName = previousBuild.getDisplayName();
            requestPayload.put("previousDisplayName", previousDisplayName);
            String previousTime = previousBuild.getTimestamp().getTime().toString();
            requestPayload.put("previousTime", previousTime);
            Result previousResult = previousBuild.getResult();
            if(previousResult != null){
                requestPayload.put("previousStatus", previousResult.toString());
            }
            AbstractProject previousProject = previousBuild.getProject();
            if(previousProject != null){
                String previousProjectName = previousProject.getName();
                requestPayload.put("previousProjectName", previousProjectName);
            }
        }

        requestPayload.put("isPreBuild", "false");
        requestPayload.put("duration", build.getDurationString());
        requestPayload.put("params", formatBuildVariables());

        if(alertProperties.getPriority() != null) {
            requestPayload.put("priority", alertProperties.getPriority().getValue());
        }

        String payload = "";
        try {
            payload = this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestPayload);
        } catch (Exception e) {
            e.printStackTrace(consoleOutputLogger);
            logger.error("Exception while serializing post request :" + e.getMessage());
        }

        String response = sendWebhookToOpsGenie(payload);
        return checkResponse(response);
    }


    private String formatCulprits(Set<User> culprits) {
        StringBuilder culpritsBuilder = new StringBuilder();
        for (User culprit : culprits) {
            culpritsBuilder.append(culprit.getFullName()).append(",");
        }
        return culpritsBuilder.toString();
    }

    private void populateRequestPayloadWithMandatoryFields() {
        String time = Objects.toString(build.getTimestamp().getTime());
        requestPayload.put("time", time);

        String projectName = project.getName();
        requestPayload.put("projectName", projectName);

        String displayName = build.getDisplayName();
        requestPayload.put("displayName", displayName);

        String status = Objects.toString(build.getResult());
        requestPayload.put("status", status);

        String url = build.getUrl();
        requestPayload.put("url", new JenkinsLocationConfiguration().getUrl() + url);

        List<String> tags = splitStringWithComma(alertProperties.getTags());
        requestPayload.put("tags", tags);

        List<String> teams = splitStringWithComma(alertProperties.getTeams());
        requestPayload.put("teams", teams);

        String startTime = Objects.toString(build.getStartTimeInMillis());
        requestPayload.put("startTimeInMillis", startTime);
    }

    private List<String> splitStringWithComma(String unparsed) {
        if (unparsed == null) {
            return Collections.emptyList();
        }

        ArrayList<String> tokens = new ArrayList<>();

        for (String token : unparsed.trim().split(",")) {
            tokens.add(token.trim());
        }

        return tokens;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseFromOpsGenie {

        @JsonProperty("status")
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
