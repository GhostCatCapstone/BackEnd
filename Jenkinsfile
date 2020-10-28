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
                sh 'echo "The deploy stage will occur in the DeployLambdaJob"'
            }
        }
    }
}
