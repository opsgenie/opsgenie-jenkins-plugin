OpsGenie plugin for Jenkins
------------------------

Provides Jenkins notification integration with OpsGenie .

# Install Instructions for OpsGenie

1. Get an OpsGenie account: https://app.opsgenie.com/customer/register
2. Configure the Jenkins integration:
   https://app.opsgenie.com/integration#/add/Jenkins
3. Install this plugin on your Jenkins server.
4. Configure it in your Jenkins job (and optionally as global configuration) and
   **add it as a Post-build action**.

# Developer instructions

[Install Maven](https://github.com/jenkinsci/workflow-plugin) and JDK.
```
$ mvn -version | grep -v home
Apache Maven 3.5.0 (; 2017-04-03T22:39:06+03:00)
Java version: 1.8.0_131, vendor: Oracle Corporation
Default locale: en_US, platform encoding: UTF-8
```
Create an HPI file to install in Jenkins (HPI file will be in
`target/opsgenie-notification.hpi`).

    mvn hpi:hpi
