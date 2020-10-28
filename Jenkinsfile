@Library('github.com/releaseworks/jenkinslib') _

pipeline {
    agent any
    
    stages {
        stage ('build') {
            steps {
                sh 'mvn clean compile package'
                archiveArtifacts artifacts: 'src/**/*.java'
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }

        
        stage('test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('deploy') {
            steps {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'aws-key', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    AWS("--region=us-east-1 aws lambda update-function-code --function-name ExampleFunction --zip-file fileb://target/example-java-1.0-SNAPSHOT.jar")
                }
            }
        }
    }
}
