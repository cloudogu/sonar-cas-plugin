#!groovy
@Library(['github.com/cloudogu/ces-build-lib@1.44.3'])
import com.cloudogu.ces.cesbuildlib.*

node {
    timestamps {
        repositoryOwner = 'cloudogu'
        projectName = 'sonar-cas-plugin'
        projectPath = "/go/src/github.com/${repositoryOwner}/${projectName}/"
        githubCredentialsId = 'sonarqube-gh'

        def javaHome = tool 'JDK8'
        Maven mvn = new MavenWrapper(this, javaHome)
        // Sonar stopped support for JRE8 for its client, so for now we run the analysis in a separate container.
        // Once the lib is upgraded to JDK11 this can be removed
        String SonarJreImage = 'adoptopenjdk/openjdk11:jre-11.0.11_9-alpine'
        Git git = new Git(this)

        stage('Checkout') {
            checkout scm
            git.clean('')
        }

        stage('Build') {
            mvn 'clean install -DskipTests'
            archive '**/target/*.jar'
        }

        stage('Unit Test') {
            mvn 'test'
        }

        stage('SonarQube') {
            def branch = "${env.BRANCH_NAME}"

            def scannerHome = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
            withSonarQubeEnv {
                git = new Git(this, "cesmarvin")

                sh "git config 'remote.origin.fetch' '+refs/heads/*:refs/remotes/origin/*'"
                gitWithCredentials("fetch --all")

                if (branch == "master") {
                    echo "This branch has been detected as the main branch."
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName} -Dsonar.projectName=${projectName}"
                } else if (branch == "develop") {
                    echo "This branch has been detected as the develop branch."
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName} -Dsonar.projectName=${projectName} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=master  "
                } else if (env.CHANGE_TARGET) {
                    echo "This branch has been detected as a pull request."
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName} -Dsonar.projectName=${projectName} -Dsonar.branch.name=${env.CHANGE_BRANCH}-PR${env.CHANGE_ID} -Dsonar.branch.target=${env.CHANGE_TARGET} "
                } else if (branch.startsWith("feature/") || branch.startsWith("bugfix/")) {
                    echo "This branch has been detected as a feature branch."
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName} -Dsonar.projectName=${projectName} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop"
                } else {
                    echo "WARNING: This branch has not been detected. Assuming a feature branch."
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=${projectName} -Dsonar.projectName=${projectName} -Dsonar.branch.name=${env.BRANCH_NAME} -Dsonar.branch.target=develop"
                }
            }
            timeout(time: 2, unit: 'MINUTES') { // Needed when there is no webhook for example
                def qGate = waitForQualityGate()
                if (qGate.status != 'OK') {
                    unstable("Pipeline unstable due to SonarQube quality gate failure")
                }
            }
        }
    }
}

String repositoryOwner
String projectName
String projectPath
String githubCredentialsId

void gitWithCredentials(String command) {
    withCredentials([usernamePassword(credentialsId: 'cesmarvin', usernameVariable: 'GIT_AUTH_USR', passwordVariable: 'GIT_AUTH_PSW')]) {
        sh(
                script: "git -c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" " + command,
                returnStdout: true
        )
    }
}
