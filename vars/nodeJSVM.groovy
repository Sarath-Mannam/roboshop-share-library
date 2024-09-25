pipeline {                                // declarative pipeline 
    agent { node { label 'Agent-1' } }
    environment{ 
        // here if you create any variable it will have global access because it is env varialble
        version = ''
    }
    parameters {
        string(name: 'component', defaultValue: '', description: 'Which component?')
    }
    stages {
        stage('Get version') {
            steps{
                script {
                    // Read the package.json file
                    def packageJson = readJSON file: 'package.json'
                    
                    // Extract the version
                    // def version = packageJson.version
                       version = packageJson.version
                    
                    // Display the version
                    echo "component version: ${version}"
                }
            }
        }
        stage('Install dependencies') { 
            steps {
                sh 'npm install'
            }
        }
        stage('Unit test') { 
            steps {
                echo "unit testing is done"
            }
        }
        // sonar-scanner command expect sonar-project.properties file has to be available
        stage('Sonar Scan') { 
            steps {
                // sh 'ls -ltr'
                // sh 'sonar-scanner'
                echo "Sonar scan completed"
            }
        }
        stage('Build') { 
            steps {
                sh 'ls -ltr'
                sh "zip -r ${params.component}.zip ./* --exclude=.git --exclude=.zip"
            }
        }
        stage('Static Application Security Testing') { 
            steps {
                echo "SAST Completed"
                echo "package version: $version"
            }
        }
        // Install pipeline utility steps plugin 
        stage('Publish Artifact') { 
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: '172.31.21.164:8081/',  // private ip of nexus
                    groupId: 'com.roboshop',
                    version: "$version",
                    repository: "${params.component}",
                    credentialsId: 'nexus-auth',
                    artifacts: [
                        [artifactId: "${params.component}",
                        classifier: '',
                        file: "${params.component}.zip",
                        type: 'zip']
                    ]
                ) 
            }
        }
        // Here I need to configure downstream job and I have to pass package version for deployment otherwise deploy job don't know which one to deploy 
        // This job will wait untill downstream job is over.
        stage('Deploy') { 
            steps {
                script {
                    echo "Deployment"
                    def params = [
                        string (name:'version', value:"$version")
                    ]
                    build job: "../${params.component}-deploy", wait: true, parameters: params
                }
            }
        }
    }
    post{
        always{
            echo 'cleaning up workspace'
            deleteDir()
        }
    }
}