package com.opsgenie.integration.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.Result;

/**
 * @author Omer Ozkan
 * @version 17/03/16
 */
enum JobResult {
    SUCCESS("success") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifySuccess();
        }
    },
    FAILURE("failed") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyFailure();
        }
    },
    UNSTABLE("unstable") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyUnstable();
        }
    },
    NOT_BUILT("not built") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyNotBuilt();
        }
    },
    BACK_TO_NORMAL("back to normal") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyBackToNormal();
        }
    },
    REPEATED_FAILURE("failed again") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyRepeatedFailure();
        }
    },
    REPEATED_UNSTABLE("unstable again") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyRepeatedFailure();
        }
    },
    ABORTED("aborted") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return properties.isNotifyAborted();
        }
    },
    UNKNOWN("result is unknown") {
        @Override
        public boolean shouldSendAlert(NotificationProperties properties) {
            return true;
        }
    };

    private String name;

    JobResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean shouldSendAlert(NotificationProperties properties);

    public static JobResult fromBuild(AbstractBuild build) {
        Result result = build.getResult();

        AbstractBuild previousBuild = build.getPreviousBuild();
        Result previousResult = null;
        if (previousBuild != null) {
            previousResult = previousBuild.getResult();
        }


        if (result == Result.SUCCESS) {
            if (previousResult != null && (previousResult == Result.FAILURE) || previousResult == Result.UNSTABLE) {
                return BACK_TO_NORMAL;
            }
            return SUCCESS;
        }

        if (result == Result.FAILURE) {
            if (previousResult != null && previousResult == Result.FAILURE) {
                return REPEATED_FAILURE;
            }
            return FAILURE;
        }

        if (result == Result.UNSTABLE) {
            if (previousResult != null && previousResult == Result.UNSTABLE) {
                return REPEATED_UNSTABLE;
            }
            return UNSTABLE;
        }

        if (result == Result.NOT_BUILT) {
            return NOT_BUILT;
        }

        if (result == Result.ABORTED) {
            return ABORTED;
        }
        return UNKNOWN;
    }
}
