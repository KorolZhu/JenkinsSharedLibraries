#!/usr/bin/env groovy

def call() {

    pipelineIosAppDevelopBranch()
    
    /*
    if (env.BRANCH_NAME.startsWith('feature/')) {
        pipelineIosAppFeatureBranch()
    } else if (env.BRANCH_NAME == 'develop') {
        pipelineIosAppDevelopBranch()
    } else if (env.BRANCH_NAME.startsWith('test/')) {
        pipelineIosAppTestBranch()
    } else if (env.BRANCH_NAME == 'release') {
        pipelineIosAppReleaseBranch()
    } else if (env.BRANCH_NAME == 'master') {
        pipelineIosAppMasterBranch()
    } else {
        pipelineIosAppTagBranch()
    }
    */
}
