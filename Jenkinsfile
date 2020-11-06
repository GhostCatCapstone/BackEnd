pipeline {
    agent any
    
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
