# 1. 概述

有4种常用的管理Gradle依赖方法：

-   手动管理：在每个module中定义插件依赖库，每次升级依赖库是都需要手动更改（不建议使用）
-   使用ext的方式管理依赖库：这是Google推荐管理依赖的方法[Android官方文档](https://developer.android.google.cn/studio/build/gradle-tips?hl=zh-cn#configure-project-wide-properties)
-   kotlin+buildSrc：自动补全和单击跳转，依赖更新时将重新构建整个项目
-   Composing builds：自动补全和单击跳转，依赖更新时不会重新构建整个项目

# 2. 手动管理

手动管理就是，在每个module中定义插件依赖库。常用的依赖库依赖方式如下：

## 2.1. jar依赖

```
//依赖引入libs下所有的jar
implementation fileTree(dir:'libs',include:['*.jar'])

//指定依赖某一个或几个jar
implementation files('libs/XXX.jar','libs/XXX.jar')
```

## 2.2. aar依赖

aar依赖需要额外增加 `flatDir {dirs "libs"}`语句，如下所示：

```
android {
    ...
    repositories { 
        flatDir {
            dirs "libs"
        }
    }
}    
dependencies {
    implementation fileTree(dir:'libs',include:['*.aar'])
    implementation(name:'XXX',ext:'aar')
}
```
## 2.3. module依赖

当项目中有多个Module时，我们需要在settings.gradle中引入，如下所示：

```
include ':app'
include ':library1', ':library2'
```

接着在模块build.gradle引入：

```
implementation project(':library1')
implementation project(':library2')
```

## 2.4. Gradle的远程库依赖

当在Android Studio中新建一个项目时，会在项目build.gradle有如下代码：

```
buildscript {
    repositories {
        google()
        jcenter()
        
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.4.0'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        
    }
}
```

这些代码都是默认的，在`buildscript`和`allprojects块`中，通过repositories来引入谷歌的Maven库和JCenter库。首先会从谷歌的Maven库去寻找相应的库，如果找不到会从JCenter库中去寻找。

然后在模块**`build.gradle`加入如下的代码，就可以引入远程库。

```
implementation group:'com.android.support',name:'appcompat-v7',version:'28.0.0'
//简写
implementation 'com.android.support:appcompat-v7:28.0.0'
```

# 3. 使用ext的方式管理依赖库

ext全局配置使用方式如下：

-   在根目录下创建config.gradle文件，定义依赖库版本和依赖配置
-   在项目的build.gradle文件中使用config.gradle配置
-   在模块的build.gradel文件中使用config.gradle配置

## 3.1. 项目的build.gradle配置

首先在根目录下创建config.gradle文件来进行配置，如下所示：

```
/**
 * Gradle依赖全局管理-ext方式
 * */
ext.deps = [:]

/**
 * 全局定义Gradle依赖库版本
 * */
def versions = [:]
versions.android_gradle_plugin = '4.0.1'
versions.kotlin = "1.6.10"

ext.versions = versions

/**
 * 全局定义Gradle依赖
 * */
def deps = [:]
deps.android_gradle_plugin = "com.android.tools.build:gradle:$versions.android_gradle_plugin"

def kotlin = [:]
kotlin.plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$versions.kotlin"
deps.kotlin = kotlin

ext.deps = deps
```

在config.gradle文件中定义好配置好，需要在项目的build.gradle中使用配置，如下所示：

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    //所有的子项目或者所有的modules都可以从这个配置文件中读取内容
    apply from: 'config.gradle'

    repositories {
        google()
        jcenter()
    }
    dependencies {
        //使用config中的配置
        classpath deps.android_gradle_plugin
        //使用config中的配置
        classpath deps.kotlin.plugin
    }
}

allprojects {
    repositories {
        google()
        jcenter()
//      maven { url 'https://jitpack.io' }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

## 3.2. 模块的build.gradle配置


### 3.2.1. 配置buildVersion版本

继续在config.build文件中定义统一的buildVersion，如下所示：

```
/**
 * 定义全局的build_versions
 */
def build_versions = [:]
build_versions.min_sdk = 21
build_versions.compile_sdk = 31
build_versions.target_sdk = 31
build_versions.build_tools = "31.0.3"
ext.build_versions = build_versions
```

在每个模块的build.gradle中使用配置，如下所示：

```
...
android {
    compileSdkVersion build_versions.compile_sdk
    buildToolsVersion build_versions.build_tools

    defaultConfig {
        applicationId "com.example.xxxx"
        minSdkVersion build_versions.min_sdk
        targetSdkVersion build_versions.target_sdk
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    
    ...
}

dependencies {
    ...
}
```

这么做的好处就是，更好的保证了多个模块buildVersion的统一。

### 3.2.2. 配置dependencies

继续在config.gradle中定义依赖，如下所示：

```
/**
 * 定义全局的Gradle依赖库版本
 * */
def versions = [:]
versions.android_gradle_plugin = "4.0.1"
versions.core_ktx = "1.1.0"
versions.kotlin = "1.6.10"
versions.appcompat = "1.2.0-alpha02"
versions.material = "1.0.0"
versions.constraint_layout = "2.0.0-alpha2"
versions.junit = "4.12"
versions.ext_junit = "1.1.2"
versions.espresso_core = "3.2.0"
versions.fragment = "1.2.0"

ext.versions = versions

/**
 * 定义全局的Gradle依赖
 * */
def deps = [:]
deps.android_gradle_plugin = "com.android.tools.build:gradle:$versions.android_gradle_plugin"

def kotlin = [:]
kotlin.plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$versions.kotlin"
deps.kotlin = kotlin

deps.core_ktx = "androidx.core:core-ktx:$versions.core_ktx"
deps.app_compat = "androidx.appcompat:appcompat:$versions.appcompat"
deps.material = "com.google.android.material:material:$versions.material"
deps.constraint_layout = "androidx.constraintlayout:constraintlayout:$versions.constraint_layout"
deps.junit = "junit:junit:$versions.junit"
deps.ext_junit = "androidx.test.ext:junit:$versions.ext_junit"
deps.espresso_core = "androidx.test.espresso:espresso-core:$versions.espresso_core"

def fragment = [:]
fragment.runtime = "androidx.fragment:fragment:${versions.fragment}"
fragment.runtime_ktx = "androidx.fragment:fragment-ktx:${versions.fragment}"
fragment.testing = "androidx.fragment:fragment-testing:${versions.fragment}"
deps.fragment = fragment

ext.deps = deps
```

在module的build.grdle中使用配置，如下所示：

```
...
android{
    ...
}

dependencies {
    implementation deps.core_ktx
    implementation deps.app_compat 
    implementation deps.material 
    implementation deps.constraint_layout 
    testImplementation deps.junit 
    androidTestImplementation deps.ext_junit 
    androidTestImplementation deps.espresso_core 

    implementation deps.fragment.runtime
    implementation deps.fragment.runtime_ktx
    implementation deps.fragment.testing
}
```

# 4. kotlin+buildSrc

共享 buildSrc 库工件的引用，全局只有一个地方可以修改它，支持自动补全（这个很爽），支持跳转。 但是他也有一个缺点，依赖更新将重新构建整个项目，这个不是很好。使用方式如下：

-   在项目根目录下新建一个名为 buildSrc，定义依赖库配置
-   在模块的build.gradle中使用配置

## 4.1. 定义配置

   1.  在项目根目录下新建一个名为 buildSrc 的文件夹( 名字必须是 buildSrc，因为运行 Gradle 时会检查项目中是否存在一个名为 buildSrc 的目录 )
    1.  在 `buildSrc/src/main/java/包名/` 目录下新建 Deps.kt 文件，添加以下内容：

 ```
package com.build.src.example

object Versions {
    val core_ktx = "1.1.0"
    val kotlin = "1.6.10"
    val appcompat = "1.2.0-alpha02"
    val material = "1.0.0"
    val constraint_layout = "2.0.0-alpha2"
    val junit = "4.12"
    val ext_junit = "1.1.2"
    val espresso_core = "3.2.0"
    val fragment = "1.2.0"
}

object Deps {
    val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    val app_compat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val material = "com.google.android.material:material:${Versions.material}"
    val constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.constraint_layout}"
    val junit = "junit:junit:${Versions.junit}"
    val ext_junit = "androidx.test.ext:junit:${Versions.ext_junit}"
    val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"

    val fragment_runtime = "androidx.fragment:fragment:${Versions.fragment}"
    val fragment_runtime_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    val fragment_testing = "androidx.fragment:fragment-testing:${Versions.fragment}"

}
```   


## 4.2. 模块的build.gradle配置

在每个module的build.gradle中使用配置，如下：

```
...
android{
    ...
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation Deps.core_ktx
    implementation Deps.app_compat
    implementation Deps.material
    implementation Deps.constraint_layout

    testImplementation Deps.junit
    androidTestImplementation Deps.ext_junit
    androidTestImplementation Deps.espresso_core

    implementation Deps.fragment_runtime
    implementation Deps.fragment_runtime_ktx
    implementation Deps.fragment_testing

}
```

# 5. Composing builds

这种方式拥有buildSrc的优点，同时依赖更新不用重新构建整个项目。使用方式如下：

## 5.1. 新建java-gradle-plugin类型module


### 5.1.1. 新建VersionPlugin目录

新建VersionPlugin文件夹，VersionPlugin需要放到和Project同级目录，或者Project以外的其他地方。**需要注意 VersionPlugin 不能放在 Project 目录下**，否则会抛出以下异常：

```
Project directory '/Users/username/Downloads/ComposingBuilds-vs-buildSrc/Project-ComposingBuild/versionPlugin' is not part of the build defined by settings file '/Users/username/Downloads/ComposingBuilds-vs-buildSrc/Project-ComposingBuild/settings.gradle'. If this is an unrelated build, it must have its own settings file.
```

### 5.1.2. 配置VersionPlugin的build.gradle

-   在 VersionPlugin 目录下新建 build.gradle 文件，添加以下内容

```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        // 因为使用的 Kotlin 需要需要添加 Kotlin 插件
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10"
    }
}

apply plugin: 'kotlin'
apply plugin: 'java-gradle-plugin'///既不是lib也不是application,指定类型为gradle插件

repositories {
    // 需要添加 jcenter 否则会提示找不到 gradlePlugin
    jcenter()
}

dependencies {
    implementation gradleApi()
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10"

}

compileKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
compileTestKotlin {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

gradlePlugin {
    plugins {
        version {
            // 在 app 模块需要通过 id 引用这个插件
            id = 'com.example.version.plugin'
            // 实现这个插件的类的路径
            implementationClass = 'com.example.version.plugin.VersionPlugin'
        }
    }
}
```

-   在 `VersionPlugin/src/main/java/包名/` 目录下新建 VersionPlugin.kt 文件，添加以下内容

```
package com.example.version.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class VersionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
    }

    companion object {
    }
}
```

### 5.1.3。 在Project中依赖VersionPlugin

-   首先在项目的settings.gradle中，添加以下配置：

```
rootProject.name = "CompsoingBuild"
includeBuild("../VersionPlugin")
include ':app'
```

-   接着在项目的build.gradle中，添加以下内容

```
plugins {
    // 这个 id 就是在 versionPlugin 文件夹下 build.gradle 文件内定义的id
    id "com.example.version.plugin" apply false
}
```

完成上述步骤，就可以开始定义依赖库配置了。

## 5.2. 配置BuildVersion版本

-   在 `VersionPlugin/src/main/java/包名/` 目录下新建 BuildConfig.kt 文件，添加以下内容

```
package com.example.version.plugin

/**
 * 配置和 Build 相关的
 */
object BuildConfig {
    val compileSdkVersion = 29
    val buildToolsVersion = "29.0.3"
    val minSdkVersion = 26
    val targetSdkVersion = 29
    val versionCode = 10000
    val versionName = "1.0.0"
}
```

-   在模块的build.gradle中，使用版本配置

```
import com.example.version.plugin.*

plugins {
    ...
}

android{
    compileSdkVersion BuildConfig.compileSdkVersion

    defaultConfig {
        applicationId "com.example.compsoingbuild"
        minSdkVersion BuildConfig.minSdkVersion
        targetSdkVersion BuildConfig.targetSdkVersion
        versionCode BuildConfig.versionCode
        versionName BuildConfig.versionName

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
    ...
}
```

## 5.3. 定义依赖库配置

-   在 `VersionPlugin/src/main/java/包名/` 目录下新建 Deps.kt 文件，添加以下内容：

```
package com.example.version.plugin

object Versions {
    val core_ktx = "1.1.0"
    val kotlin = "1.6.10"
    val appcompat = "1.2.0-alpha02"
    val material = "1.0.0"
    val constraint_layout = "2.0.0-alpha2"
    val junit = "4.12"
    val ext_junit = "1.1.2"
    val espresso_core = "3.2.0"
    val fragment = "1.2.0"
}

object Deps {
    val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    val app_compat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val material = "com.google.android.material:material:${Versions.material}"
    val constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.constraint_layout}"
    val junit = "junit:junit:${Versions.junit}"
    val ext_junit = "androidx.test.ext:junit:${Versions.ext_junit}"
    val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"

    val fragment_runtime = "androidx.fragment:fragment:${Versions.fragment}"
    val fragment_runtime_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    val fragment_testing = "androidx.fragment:fragment-testing:${Versions.fragment}"

}
```

-   在模块的build.gradle中，使用配置：

```
...

dependencies {

    implementation Deps.espresso_core
    implementation Deps.app_compat
    implementation Deps.material
    implementation Deps.constraint_layout

    testImplementation Deps.junit
    androidTestImplementation Deps.ext_junit
    androidTestImplementation Deps.espresso_core

    implementation Deps.fragment_runtime
    implementation Deps.fragment_runtime_ktx
    implementation Deps.fragment_testing
}
```
  


  
