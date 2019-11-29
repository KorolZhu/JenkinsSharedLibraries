#!/usr/bin/env groovy

def call(Closure body={}) {
    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent none

        options {
            skipDefaultCheckout()
            ansiColor('xterm')
            disableConcurrentBuilds()
        }

        triggers {
            pollSCM('H/2 * * * *')
        }

        environment {
            LANG = "C.UTF-8"
            LC_ALL = "en_US.UTF-8"
            LANGUAGE = "en_US.UTF-8"
            UNITTESTING_STATE = 'false'
            TESTING_STATE = 'false'
        }

        stages {
            stage('Checkout SCM') {
                agent {
                    node {
                        label 'master'
                        customWorkspace "workspace/${JOB_NAME.replace('%2F', '/')}"
                    }
                }
                steps {
                    script {
                        def scmVars = checkoutGitlab()
                        env.GIT_COMMIT = scmVars.GIT_COMMIT
                        env.GIT_PREVIOUS_SUCCESSFUL_COMMIT = scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT
                        env.GIT_BRANCH = scmVars.GIT_BRANCH
                        echo "current SHA: ${scmVars.GIT_COMMIT}"
                        echo "previous SHA: ${scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
                        echo "scmVars: ${scmVars}"
                    }
                }
            }

            stage('Build') {
                agent {
                    node {
                        label 'master'
                        customWorkspace "workspace/${JOB_NAME.replace('%2F', '/')}"
                    }
                }
                environment {
                    PATH = "/Users/mac/.rbenv/shims:/usr/local/bin:${PATH}"
                }
                steps {
                    buildBranch()
                }
                post {
                    success {
                        archiveArtifacts artifacts: 'build/IPA/*.dSYM.zip', fingerprint: true
                        archiveArtifacts artifacts: 'build/IPA/*.ipa', fingerprint: true
                    }
                }
            }
        }
        post {
            success {
                node('master') {
                    echo 'end'
                }
            }
            always {
                node('master') {
                    //step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "zhuzhi@hellotalk.com", sendToIndividuals: true])
                }
            }
        }
    }
}

def buildBranch() {
    echo "${env.GIT_BRANCH} branch - Build"
    sh 'bundle install'
    sh 'bundle exec fastlane ios do_publish_all'
}
