        // You will need Docker Pipeline plugin in Jenkins for this Jenkins file script to work
pipeline {
    environment {
        registry = "myjddocker/nodejs-erp"
        registryCredential = 'docker_id'
        dockerImage = ''
    }
    agent any
    stages {
        stage('Cloning Git Repo.') {
        steps {
        git branch: 'master', url: 'https://github.com/jdarwin/nodejs-deploy.git'
        }
    }
    stage('Building our image') {
        steps {
            script {
            dockerImage = docker.build registry + ":$BUILD_NUMBER"
            }
        }
    }
    stage('Push NodeJS docker image') {
        steps {
        script {
                docker.withRegistry( '', registryCredential ) {
                    dockerImage.push()
                }
            }
        }
    }
    // cleaning stage works well, but it immediately deletes the image, not very useful for now.
    //stage('Cleaning up') {
    //    steps {
    //            sh "docker rmi $registry:$BUILD_NUMBER"
    //        }
    //    }
    stage('NodeJS Deploy to K8s')
    {
        steps{
            sshagent(['k8s-jenkins'])
            {
                script{
                    try{

                        sh 'kubectl apply -f nodejs-deploy.yaml --kubeconfig=/home/student/.kube/config'
                    }catch(error)
                        {
                        }
                }
            }
        }
    }
}
}}
    }
}
}
