#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.48.0'])
import com.cloudogu.ces.cesbuildlib.*

node() {
    Git git = new Git(this, "cesmarvin")
    git.committerName = 'cesmarvin'
    git.committerEmail = 'cesmarvin@cloudogu.com'

    timestamps {
        properties([
                // Keep only the last 10 build to preserve space
                buildDiscarder(logRotator(numToKeepStr: '10')),
                // Don't run concurrent builds for a branch, because they use the same workspace directory
                disableConcurrentBuilds()
        ])

        catchError {

            stage('Checkout') {
                checkout scm
                git.clean("")
            }

            Maven mvn = new MavenInDocker(this, "3.5.0-jdk-8")

            stage('Build') {
                setupMaven(mvn)
                mvn 'clean compile -DskipTests'
            }

            stage('Unit Test') {
                mvn 'test'
            }
        }

        stage('Statical Code Analysis') {
            def sonarQube = new SonarQube(this, [sonarQubeEnv: 'ces-sonar'])

            sonarQube.analyzeWith(new MavenInDocker(this, "3.5.0-jdk-8"))
            sonarQube.timeoutInMinutes = 4

            if (!sonarQube.waitForQualityGateWebhookToBeCalled()) {
                unstable("Pipeline unstable due to SonarQube quality gate failure")
            }
        }
    }

    // Archive Unit and integration test results, if any
    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml,**/target/surefire-reports/TEST-*.xml'
}

def setupMaven(mvn) {
    if ("master".equals(env.BRANCH_NAME)) {
        mvn.additionalArgs = "-DperformRelease"
        currentBuild.description = mvn.getVersion()
    }
}
