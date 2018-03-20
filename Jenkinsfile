@Library('SonarSource@1.6') _

pipeline {
  agent {
    label 'linux'
  }
  stages {
    stage('QA') {
      steps {
        burgrNotifyQaStarted()
      }
      post {
        always {
          burgrNotifyQaResult()
        }
      }
    }
    stage('Promote') {
      steps {
        repoxPromoteBuild()
      }
      post {
        always {
          burgrNotifyPromote()
        }
      }
    }
  }
}
