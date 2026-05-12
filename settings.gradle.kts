pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // 阿里云镜像：缓解国内访问 repo.maven.apache.org 出现 403 / 超时等问题
        maven {
            name = "AliyunGradlePlugin"
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
        maven {
            name = "AliyunGoogle"
            url = uri("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "AliyunCentral"
            url = uri("https://maven.aliyun.com/repository/central")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            name = "AliyunPublic"
            url = uri("https://maven.aliyun.com/repository/public")
        }
        mavenCentral()
    }
}

rootProject.name = "HamTools"
include(":app")
