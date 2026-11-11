pipeline {
    agent any

    tools {
        jdk 'jdk-25'
    }

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
    }

    environment {
        GRADLE = 'gradlew.bat --console=plain --stacktrace'
    }

    stages {
//        stage('Hello') {
//            steps {
//                echo "Building ${env.JOB_NAME} #${env.BUILD_NUMBER} on ${env.NODE_NAME}"
//                bat 'java -version'
//                bat 'git log -1 --oneline'
//            }
//        }
        stage('Check tools') {
            steps {
            bat 'java -version'
            bat 'echo ANDROID_HOME=%ANDROID_HOME%'
            script {
                if (!env.ANDROID_HOME) {
                    error 'ANDROID_HOME is not set.'
                }
            }
            bat "${GRADLE} --version"
            }
        }
        stage('Android Lint') {
            steps {
                bat "${GRADLE} lintDebug"
            }
            post {
                always {
                    recordIssues enabledForFailure: true,
                        tools: [androidLintParser(pattern: 'app/build/reports/lint-results-debug.xml')]
                }
            }
        }

		stage('Static analysis') {
			steps {
				bat "${GRADLE} detekt ktlintCheck"
			}
			post {
				always {
					recordIssues enabledForFailure: true,
					tool: checkStyle(id: 'detekt',
					name: 'detekt',
					pattern: 'app/build/reports/detekt/detekt.xml'
					),
					qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]
					recordIssues enabledForFailure: true,
					tool: checkStyle(id: 'ktlint', name: 'ktlint', pattern: 'app/build/reports/ktlint/**/*.xml'),
					qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]
				}
			}
		}

		stage('Coverage') {
			steps {
				bat "${GRADLE} createDebugUnitTestCoverageReport"
				bat 'powershell -NoProfile -ExecutionPolicy Bypass -File ci\\coverage-gate.ps1 -Report app/build/reports/coverage/test/debug/report.xml -Min 10'
			}
			post {
				always {
					recordCoverage(
						tools: [[parser: 'JACOCO', pattern: 'app/build/reports/coverage/test/debug/report.xml']],
					sourceCodeRetention: 'NEVER'
					)
				}
			}
		}

        stage('Unit tests') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    bat "${GRADLE} testDebugUnitTest"
                }
            }
            post {
                always {
                    junit testResults: 'app/build/test-results/testDebugUnitTest/*.xml', allowEmptyResults: false
                }
            }
        }
        stage('Build debug APK') {
            steps {
                bat "${GRADLE} assembleDebug"
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
                }
            }
        }
    }

    post {
        always {
            echo "Finished with result: ${currentBuild.currentResult}"
        }
        failure {
            echo 'Build failed: open the first red stage and read the log from the bottom.'
        }
    }

}