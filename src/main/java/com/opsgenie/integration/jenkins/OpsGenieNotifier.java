package com.opsgenie.integration.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;

/**
 * @author Omer Ozkan
 * @author kaganyildiz
 * @version 09/07/17
 */
public class OpsGenieNotifier extends Notifier {
    private static final String DEFAULT_API_URL = "https://api.opsgenie.com/";

    private boolean enable = true;
    private String tags;
    private boolean notifyBuildStart;

    private String apiKey;
    private String apiUrl;
    private String teams;

    @DataBoundConstructor
    public OpsGenieNotifier(boolean enable,
                            boolean notifyBuildStart,
                            String tags,
                            String apiKey,
                            String apiUrl,
                            String teams) {
        this.enable = enable;
        this.notifyBuildStart = notifyBuildStart;
        this.tags = tags;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.teams = teams;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (!isEnable()) {
            return true;
        }
        OpsGenieNotificationService service = createOpsGenieNotificationService(build, listener);

        return service.sendAfterBuildData();
    }

    private OpsGenieNotificationService createOpsGenieNotificationService(AbstractBuild<?, ?> build, BuildListener listener) {

        // This variables for override the fields if they are not empty
        String tagsGiven = Util.fixNull(tags).isEmpty() ? getDescriptor().getTags() : tags;
        String teamsGiven = Util.fixNull(teams).isEmpty() ? getDescriptor().getTeams() : teams;

        AlertProperties alertProperties =
                new AlertProperties()
                        .setTags(tagsGiven)
                        .setTeams(teamsGiven);

        String apiKeyGiven = Util.fixNull(apiKey).isEmpty() ? getDescriptor().getApiKey() : apiKey;
        String apiUrlGiven = Util.fixNull(apiUrl).isEmpty() ? getDescriptor().getApiUrl() : apiUrl;

        OpsGenieNotificationRequest request =
                new OpsGenieNotificationRequest()
                        .setAlertProperties(alertProperties)
                        .setBuild(build)
                        .setListener(listener)
                        .setApiKey(apiKeyGiven)
                        .setApiUrl(apiUrlGiven);

        return new OpsGenieNotificationService(request);
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (!isEnable() || !isNotifyBuildStart()) {
            return true;
        }

        OpsGenieNotificationService opsGenieNotificationService = createOpsGenieNotificationService(build, listener);
        return opsGenieNotificationService.sendPreBuildPayload();
    }

    @Override
    public String toString() {
        return "OpsGenieNotifier{" +
                "disable=" + enable +
                ", notifyBuildStart=" + notifyBuildStart +
                ", tags='" + tags + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", teams='" + teams + '\'' +
                '}';
    }

    @Exported
    public boolean isEnable() {
        return enable;
    }

    @Exported
    public boolean isNotifyBuildStart() {
        return notifyBuildStart;
    }

    @Exported
    public String getApiKey() {
        return apiKey;
    }

    @Exported
    public String getApiUrl() {
        return apiUrl;
    }

    @Exported
    public String getTags() {
        return tags;
    }

    @Exported
    public String getTeams() {
        return teams;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         * <p/>
         * <p/>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String apiKey;
        private String teams;
        private String tags;
        private String apiUrl;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Send Alert to OpsGenie";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            apiKey = formData.getString("apiKey");
            apiUrl = formData.getString("apiUrl");
            tags = formData.getString("tags");
            teams = formData.getString("teams");
            save();
            return super.configure(req, formData);
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getApiUrl() {
            if (StringUtils.isBlank(apiUrl)) {
                apiUrl = OpsGenieNotifier.DEFAULT_API_URL;
            }
            return apiUrl;
        }

        public String getTeams() {
            return teams;
        }


        public String getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return "DescriptorImpl{" +
                    "apiKey='" + apiKey + '\'' +
                    ", teams='" + teams + '\'' +
                    ", tags='" + tags + '\'' +
                    '}';
        }
    }
}
