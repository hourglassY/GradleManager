package com.example.version.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * desc  : 如果出现红色警告可以忽略，不会影响项目的编译和运行
 */
class VersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
    }

    companion object {
    }
}
