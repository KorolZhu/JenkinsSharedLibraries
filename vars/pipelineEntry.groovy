#!/usr/bin/env groovy

import java.text.SimpleDateFormat;
import io.issenn.devops.jenkins.pipeline.environment.EnvironmentConstants

def call() {
    def dateFormat
    def date
    def formattedDate

    dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    date = new Date()
    formattedDate = dateFormat.format(date)

    echo "${formattedDate}"

    if (env.CICD_TYPE == 'AndroidApp') {
        log.info("entry pipelineAndroidApp")
        pipelineAndroidApp()
    } else if (env.CICD_TYPE == 'IosApp') {
        log.info("entry pipelineIosApp")
        pipelineIosApp()
    } else {
        error "Don't know what to do with this CICD_TYPE: ${env.CICD_TYPE}"
    }
}