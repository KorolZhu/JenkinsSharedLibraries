#!/usr/bin/env groovy

def call() {
    checkout changelog: true, poll: true, scm: [
        $class: 'GitSCM',
        branches: scm.branches,
        browser: [$class: 'GitLab', repoUrl: 'http://gitlab.hellotalk.com', version: '10.7'],
        doGenerateSubmoduleConfigurations: false,
        gitTool: 'git',
        extensions: scm.extensions + [
            [
                $class: 'CloneOption',
                depth: 100,
                honorRefspec: true,
                noTags: false,
                reference: '',
                //shallow: true,
                timeout: 120
            ],
            [
                $class: 'LocalBranch',
                localBranch: '**'
            ],
            [
                $class: 'GitTagMessageExtension'
            ]
        ],
        submoduleCfg: scm.submoduleCfg,
        userRemoteConfigs: scm.userRemoteConfigs
    ]
}
