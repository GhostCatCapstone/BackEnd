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
                sh 'echo Deploy steps here'
            }
        }
    }
}
