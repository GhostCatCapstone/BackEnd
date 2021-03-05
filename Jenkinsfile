@Library('github.com/releaseworks/jenkinslib') _

pipeline {
    agent any
    
    tools { 
        maven 'apache maven 3.6.3' 
        jdk 'JDK 8' 
    }
    
    stages {
        stage ('build') {
            steps {
                sh 'mvn clean compile package'
            }
        }

        
        stage('test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('deploy') {
            steps {
                sh 'echo "The deploy stage will occur in the DeployLambdaJob"'
            }
        }
    }
}
