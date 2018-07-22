#!/usr/bin/env groovy

def call(Closure body={}) {

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent {
            node {
                label 'mac-mini'
                customWorkspace "workspace/${JOB_NAME}"
            }
        }

        environment {
            LANG = "C.UTF-8"
            LC_ALL = "en_US.UTF-8"
            LANGUAGE = "en_US.UTF-8"
            ANDROID_SDK_ROOT = "${HOME}/Library/Android/sdk"
            ANDROID_HOME = "${ANDROID_SDK_ROOT}"
            UNITTESTING_STATE = 'false'
            App = "HelloTalk"
            DebugBuildTypes = "Debug"
            ReleaseBuildTypes = "Release"
            CHINAPRODUCTFLAVORS_STATE = 'true'
            ChinaProductFlavors = "China"
            GOOGLEPRODUCTFLAVORS_STATE = 'true'
            GoogleProductFlavors = "Google"
            HTPRIVATEPRODUCTFLAVORS_STATE = 'true'
            HTPrivateProductFlavors = "HTPrivate"
            //-PBUILD_NUMBER=${env.BUILD_NUMBER}
        }

        stages {
            stage('Branch and Tag - error') {
                when {
                    beforeAgent true
                    not {
                        anyOf {
                            branch "feature/*"
                            branch "develop"
                            //branch "test"
                            branch "release/*"
                            branch "master"
                            branch "hotfix/*"
                            buildingTag()
                        }
                    }
                }
                steps {
                    error "Don't know what to do with this branch or tag: ${env.BRANCH_NAME}"
                }
            }

            stage('Checkout SCM') {
                steps {
                    script {
                        def environment = new io.issenn.devops.jenkins.pipeline.environment.EnvironmentConstants(this)
                        //println(environment.repoName(this))
                        println(environment.BRANCH_NAME)
                        println(environment.JOB_NAME)
                    }
                    checkoutGitlab()
                }
            }

            stage('Prepare') {
                steps {
                    script {
                        //gradle '-v'
                        gradle.version()
                    }
                }
            }

            stage('Build entry - feature/*') {
                when {
                    beforeAgent true
                    branch "feature/*"
                }
                steps {
                    buildFeatureBranch()
                }
            }

            stage('Build entry - develop') {
                when {
                    beforeAgent true
                    branch "develop"
                }
                failFast false
                parallel {
                    stage('china flavor - develop') {
                        when {
                            beforeAgent true
                            environment name: 'CHINAPRODUCTFLAVORS_STATE', value: 'true'
                        }
                        stages {
                            stage('Unit Testing - china flavor - develop') {
                                when {
                                    beforeAgent true
                                    environment name: 'UNITTESTING_STATE', value: 'true'
                                }
                                steps {
                                    unittestDevelopBranch(ReleaseBuildTypes, ChinaProductFlavors)
                                }
                            }
                            stage('Build - china flavor - develop') {
                                steps {
                                    buildDevelopBranch(ReleaseBuildTypes, ChinaProductFlavors)
                                }
                            }
                            stage('Artifacts - china flavor - develop') {
                                steps {
                                    artifactsDevelopBranch(ReleaseBuildTypes, ChinaProductFlavors)
                                }
                            }
                            stage('Deploy - china flavor - develop') {
                                agent {
                                    node {
                                        label 'master'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }
                                steps {
                                    deployDevelopBranch(ReleaseBuildTypes, ChinaProductFlavors)
                                }
                            }
                            stage('Testing - china flavor - develop') {
                                steps {
                                    echo "Test"
                                }
                            }
                        }
                    }
                    stage('google flavor - develop') {
                        when {
                            beforeAgent true
                            environment name: 'GOOGLEPRODUCTFLAVORS_STATE', value: 'true'
                        }
                        stages {
                            stage('Unit Testing - google flavor - develop') {
                                when {
                                    beforeAgent true
                                    environment name: 'UNITTESTING_STATE', value: 'true'
                                }
                                steps {
                                    unittestDevelopBranch(ReleaseBuildTypes, GoogleProductFlavors)
                                }
                            }
                            stage('Build - google flavor - develop') {
                                steps {
                                    buildDevelopBranch(ReleaseBuildTypes, GoogleProductFlavors)
                                }
                            }
                            stage('Artifacts - google flavor - develop') {
                                steps {
                                    artifactsDevelopBranch(ReleaseBuildTypes, GoogleProductFlavors)
                                }
                            }
                            stage('Deploy - google flavor - develop') {
                                agent {
                                    node {
                                        label 'master'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }
                                steps {
                                    deployDevelopBranch(ReleaseBuildTypes, GoogleProductFlavors)
                                }
                            }
                            stage('Testing - google flavor - develop') {
                                steps {
                                    echo "Test"
                                }
                            }
                        }
                    }
                    stage('HTPrivate flavor - develop') {
                        when {
                            beforeAgent true
                            environment name: 'HTPRIVATEPRODUCTFLAVORS_STATE', value: 'true'
                        }
                        stages {
                            stage('Unit Testing - HTPrivate flavor - develop') {
                                when {
                                    beforeAgent true
                                    environment name: 'UNITTESTING_STATE', value: 'true'
                                }
                                steps {
                                    unittestDevelopBranch(ReleaseBuildTypes, HTPrivateProductFlavors)
                                }
                            }
                            stage('Build - HTPrivate flavor - develop') {
                                steps {
                                    buildDevelopBranch(ReleaseBuildTypes, HTPrivateProductFlavors)
                                }
                            }
                            stage('Artifacts - HTPrivate flavor - develop') {
                                steps {
                                    artifactsDevelopBranch(ReleaseBuildTypes, HTPrivateProductFlavors)
                                }
                            }
                            stage('Deploy - HTPrivate flavor - develop') {
                                agent {
                                    node {
                                        label 'master'
                                        customWorkspace "workspace/${JOB_NAME}"
                                    }
                                }
                                steps {
                                    deployDevelopBranch(ReleaseBuildTypes, HTPrivateProductFlavors)
                                }
                            }
                            stage('Testing - HTPrivate flavor - develop') {
                                steps {
                                    echo "Test"
                                }
                            }
                        }
                    }
                }
            }

/*
            stage('Build snapshot - release/*') {
                when {
                    branch "release/*"
                }
                steps {
                    buildReleaseBranch()
                }
            }

            stage('Build @ Prod - master') {
                when {
                    branch "master"
                }
                steps {
                    buildMasterBranch()
                }
            }

            stage('Build snapshot - hotfix/*') {
                when {
                    branch "hotfix/*"
                }
                steps {
                    buildHotfixBranch()
                }
            }
            // artifacts
            stage('artifacts') {
                steps {
                    echo "artifacts"
                }
            }
            // Deploy
            stage('Deploy snapshot - feature/*') {
                when {
                    branch "feature/*"
                }
                steps {
                    deployFeatureBranch()
                }
            }

            stage('Deploy snapshot - develop') {
                when {
                    branch "develop"
                }
                steps {
                    deployDevelopBranch()
                }
            }

            stage('Deploy snapshot - release/*') {
                when {
                    branch "release/*"
                }
                steps {
                    deployReleaseBranch()
                }
            }

            stage('Deploy @ Prod - master') {
                when {
                    branch "master"
                }
                steps {
                    deployMasterBranch()
                }
            }

            stage('Deploy snapshot - hotfix/*') {
                when {
                    branch "hotfix/*"
                }
                steps {
                    deployHotfixBranch()
                }
            }

            stage('Test') {
                steps {
                    echo "Test"
                }
            }
*/

        }
    }
}

/**
 * feature/* for feature branches; merge back into develop
 * develop for ongoing development work
 * test/*
 * release/* to prepare production releases; merge back into develop and tag master
 * master for production-ready releases
 * hotfix/* to patch master quickly; merge back into develop and tag master
 */

def unittestFeatureBranch(String buildTypes='', String productFlavors='') {
    echo "Feature branch - Unit Testing"
    def args = ((productFlavors ?: '') + (buildTypes ?: '')) ? (((productFlavors ?: '') + (buildTypes ?: '')) + 'UnitTest' ) : ''
    unittest(args)
}

def unittestDevelopBranch(String buildTypes='', String productFlavors='') {
    echo "Develop branch - Unit Testing"
    def args = ((productFlavors ?: '') + (buildTypes ?: '')) ? (((productFlavors ?: '') + (buildTypes ?: '')) + 'UnitTest' ) : ''
    unittest(args)
}

def buildFeatureBranch() {
    echo "Feature branch"
}

def buildDevelopBranch(String buildTypes='', String productFlavors='') {
    echo "Develop branch - Build"
    def args = (productFlavors ?: '') + (buildTypes ?: '')
    build(args)
    // sonar()
    // javadoc()
    // deploy(env.JBOSS_TST)
}

def buildReleaseBranch() {
    echo "Release branch"
}

def buildMasterBranch() {
    echo "Master branch"
}

def buildHotfixBranch() {
    echo "Hotfix branch"
}

def artifactsDevelopBranch(String buildTypes = '', String productFlavors = '') {
    echo "Develop branch - Artifacts"
    productFlavors = productFlavors.toLowerCase()
    buildTypes = buildTypes.toLowerCase()
    def name = "${App}" + (((productFlavors ? ('-' + productFlavors) : '') + (buildTypes ? ('-'+ buildTypes) : '')) ?: '')
    def path = "${App}/build/outputs/apk/" + (productFlavors ?: '*') + '/' + (buildTypes ?: '*') + "/${App}-" + (productFlavors ?: '*') + '-' + (buildTypes ?: '*') + '.apk'
    println(path)
    artifacts(name, path)
}


def deployFeatureBranch() {
    echo "Feature branch"
}

def deployDevelopBranch(String buildTypes = '', String productFlavors = '') {
    echo "Develop branch - Deploy"
    productFlavors = productFlavors.toLowerCase()
    buildTypes = buildTypes.toLowerCase()
    def name = "${App}" + (((productFlavors ? ('-' + productFlavors) : '') + (buildTypes ? ('-'+ buildTypes) : '')) ?: '')
    def path = "${App}/build/outputs/apk/" + (productFlavors ?: '*') + '/' + (buildTypes ?: '*') + "/${App}-" + (productFlavors ?: '*') + '-' + (buildTypes ?: '*') + '.apk'
    println(path)
    deploy(name, path)
}

def deployReleaseBranch() {
    echo "Feature branch"
}

def deployMasterBranch() {
    echo "Feature branch"
}

def deployHotfixBranch() {
    echo "Feature branch"
}

def unittest(String args='') {
    echo "Unit Testing"
    gradle "clean test${args}"
}

def build(String args='') {
    echo "Build"
    gradle "clean assemble${args}"
}

def artifacts(String name, String path) {
    echo "stash '${name}' '${path}'"
    stash name: "${name}", includes: "${path}"
}

def deploy(String name, String path) {
    echo "deploy '${name}' '${path}'"
    unstash "${name}"
    //sh "mv ${WORKSPACE}/build/IPA/${XCODE_CONFIGURATION}-${XCODE_SDK}/*.ipa /var/www/nginx/html/testing.hellotalk.com/test-1.0-1.ipa"
}