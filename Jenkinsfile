pipeline {
    agent any
    
    stages {
        stage ('Package') {
            steps {
                sh 'mvn package'
                archiveArtifacts artifacts: 'src/**/*.java'
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }

        
        stage('test') {
            steps {
                sh 'echo Test steps here'
            }
        }
        
        stage('deploy') {
            steps {
                sh 'aws lambda update-function-code --function-name ExampleFunction --zip-file fileb://target/example-java-1.0-SNAPSHOT.jar'
            }
        }
    }
}
