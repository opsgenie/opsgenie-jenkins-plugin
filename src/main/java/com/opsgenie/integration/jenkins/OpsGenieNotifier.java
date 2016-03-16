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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Omer Ozkan
 * @version 14/03/16
 */
public class OpsGenieNotifier extends Notifier {
    private boolean enable = true;
    private boolean notifyBuildStart;
    private boolean notifyAborted;
    private boolean notifyFailure = true;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable = true;
    private boolean notifySuccess;
    private boolean notifyBackToNormal = true;
    private boolean notifyRepeatedFailure = true;

    private String tags;
    private String alias;
    private String alertNote;

    private boolean addCommitListToDescription = true;
    private boolean addFailedTestToDescription = true;

    private String apiKey;
    private String recipients;
    private String teams;

    @DataBoundConstructor
    public OpsGenieNotifier(boolean enable,
                            boolean notifyBuildStart,
                            boolean notifyAborted,
                            boolean notifyFailure,
                            boolean notifyNotBuilt,
                            boolean notifyUnstable,
                            boolean notifySuccess,
                            boolean notifyBackToNormal,
                            boolean notifyRepeatedFailure,
                            String tags,
                            String alias,
                            String customAlertMessage,
                            String alertNote,
                            boolean addCommitListToDescription,
                            boolean addFailedTestToDescription,
                            String apiKey,
                            String recipients,
                            String teams) {
        this.enable = enable;
        this.notifyBuildStart = notifyBuildStart;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifyUnstable = notifyUnstable;
        this.notifySuccess = notifySuccess;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.tags = tags;
        this.alias = alias;
        this.alertNote = alertNote;
        this.addCommitListToDescription = addCommitListToDescription;
        this.addFailedTestToDescription = addFailedTestToDescription;
        this.apiKey = apiKey;
        this.recipients = recipients;
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
        PrintStream logger = listener.getLogger();
        if (!enable) {
            logger.println("Skipping sending alert");
            return true;
        }

        OpsGenieNotificationService service = createOpsGenieNotificationService(build, listener);
        return service.sendPostBuildAlert();
    }

    private OpsGenieNotificationService createOpsGenieNotificationService(AbstractBuild<?, ?> build, BuildListener listener) {
        NotificationProperties notificationProperties =
            new NotificationProperties()
                .setNotifyAborted(notifyAborted)
                .setNotifyBackToNormal(notifyBackToNormal)
                .setNotifyFailure(notifyFailure)
                .setNotifyNotBuilt(notifyNotBuilt)
                .setNotifyRepeatedFailure(notifyRepeatedFailure)
                .setNotifySuccess(notifySuccess)
                .setNotifyUnstable(notifyUnstable);

        AlertProperties alertProperties =
                new AlertProperties()
                    .setAlertNote(alertNote)
                    .setAddFailedTestToDesc(addFailedTestToDescription)
                    .setAddCommitListToDesc(addCommitListToDescription)
                    .setAlias(alias)
                    .setTags(Util.fixNull(tags).isEmpty() ? getDescriptor().getTags() : tags)
                    .setRecipients(Util.fixNull(recipients).isEmpty() ? getDescriptor().getRecipients() : recipients)
                    .setTeams(Util.fixNull(teams).isEmpty() ? getDescriptor().getTeams() : teams);

        String apiKeyGiven = Util.fixNull(apiKey).isEmpty() ? getDescriptor().getApiKey() : apiKey;

        OpsGenieNotificationRequest request =
                new OpsGenieNotificationRequest()
                    .setAlertProperties(alertProperties)
                    .setNotificationProperties(notificationProperties)
                    .setBuild(build)
                    .setListener(listener)
                    .setApiKey(apiKeyGiven);

        return new OpsGenieNotificationService(request);
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        OpsGenieNotificationService opsGenieNotificationService = createOpsGenieNotificationService(build, listener);
        return opsGenieNotificationService.sendPreBuildAlert();
    }

    @Override
    public String toString() {
        return "OpsGenieNotifier{" +
                "disable=" + enable +
                ", notifyBuildStart=" + notifyBuildStart +
                ", notifyAborted=" + notifyAborted +
                ", notifyFailure=" + notifyFailure +
                ", notifyNotBuilt=" + notifyNotBuilt +
                ", notifySuccess=" + notifySuccess +
                ", notifyBackToNormal=" + notifyBackToNormal +
                ", notifyRepeatedFailure=" + notifyRepeatedFailure +
                ", tags='" + tags + '\'' +
                ", alias='" + alias + '\'' +
                ", alertNote='" + alertNote + '\'' +
                ", addCommitListToDesc=" + addCommitListToDescription +
                ", addTestSummaryToDesc=" + addFailedTestToDescription +
                ", apiKey='" + apiKey + '\'' +
                ", recipients='" + recipients + '\'' +
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
    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    @Exported
    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    @Exported
    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    @Exported
    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    @Exported
    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    @Exported
    public boolean isNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    @Exported
    public String getTags() {
        return tags;
    }

    @Exported
    public String getAlias() {
        return alias;
    }

    @Exported
    public String getAlertNote() {
        return alertNote;
    }

    @Exported
    public boolean isAddCommitListToDescription() {
        return addCommitListToDescription;
    }

    @Exported
    public boolean isAddFailedTestToDescription() {
        return addFailedTestToDescription;
    }

    @Exported
    public String getApiKey() {
        return apiKey;
    }

    @Exported
    public String getRecipients() {
        return recipients;
    }

    @Exported
    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

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
        private String recipients;
        private String tags;

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
            teams = formData.getString("teams");
            recipients = formData.getString("recipients");
            tags = formData.getString("tags");
            save();
            return super.configure(req, formData);
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getTeams() {
            return teams;
        }

        public String getRecipients() {
            return recipients;
        }

        public String getTags() {
            return tags;
        }

        @Override
        public String toString() {
            return "DescriptorImpl{" +
                    "apiKey='" + apiKey + '\'' +
                    ", teams='" + teams + '\'' +
                    ", recipients='" + recipients + '\'' +
                    ", tags='" + tags + '\'' +
                    '}';
        }
    }
}
