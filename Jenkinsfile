pipeline {
    agent any

    environment {
        JWT_SECRET           = credentials('JWT_SECRET')
        MAIL_PASSWORD        = credentials('MAIL_PASSWORD')
        MAIL_USERNAME        = credentials('MAIL_USERNAME')
        GITHUB_CLIENT_SECRET = credentials('GITHUB_CLIENT_SECRET')
        GITHUB_CLIENT_ID     = credentials('GITHUB_CLIENT_ID')
        GOOGLE_CLIENT_SECRET = credentials('GOOGLE_CLIENT_SECRET')
        GOOGLE_CLIENT_ID     = credentials('GOOGLE_CLIENT_ID')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
            }
        }

        stage('Generate .env') {
            steps {
                echo 'Generating .env file with Jenkins credentials...'
                writeFile file: '.env', text: """
JWT_SECRET=${env.JWT_SECRET}
MAIL_PASSWORD=${env.MAIL_PASSWORD}
MAIL_USERNAME=${env.MAIL_USERNAME}
GITHUB_CLIENT_SECRET=${env.GITHUB_CLIENT_SECRET}
GITHUB_CLIENT_ID=${env.GITHUB_CLIENT_ID}
GOOGLE_CLIENT_SECRET=${env.GOOGLE_CLIENT_SECRET}
GOOGLE_CLIENT_ID=${env.GOOGLE_CLIENT_ID}
                """.stripIndent()
            }
        }

        stage('Build & Deploy') {
            steps {
                echo 'Building and deploying all Docker services...'
                sh 'docker compose down || true'    // Stop previous services (ignore error)
                sh 'docker compose pull'            // Pull latest base images
                sh 'docker compose up -d --build'   // Build & start all services in background
            }
        }

        stage('Post-deploy Check') {
            steps {
                echo 'Checking running containers...'
                sh 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo '🚀 Deploy succeeded!'
        }
        failure {
            echo '❌ Deploy failed! Please check build logs.'
        }
    }
}
