package mil.emp3.gradle;

import org.gradle.api.*;

class EmpAndroidAppPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.configure(project) {
            apply from: EmpAndroidAppPlugin.class.classLoader.getResource("android-app.gradle")
        }

        project.task('EchoTaskApp', description: "Displays android-related configuration properties.") << {
            println 'I am EmpAndroidAppPlugin plugin!'

            println '[ android extension properties ]'
            println "\tcompileSdkVersion: $project.android.compileSdkVersion"
            println "\tbuildToolsVersion: $project.android.buildToolsVersion"

            println '\t[ defaultConfig ]'
            println "\t\tminSdkVersion: $project.android.defaultConfig.minSdkVersion"
            println "\t\ttargetSdkVersion: $project.android.defaultConfig.targetSdkVersion"
            println "\t\tversionCode: $project.android.defaultConfig.versionCode"
            println "\t\tversionName: $project.android.defaultConfig.versionName"
            
            println '\t[ buildTypes ]'
            project.android.buildTypes.each { type ->
                println "\t\t[ ${type.name} ]"
                println "\t\t\tdebuggable: $type.debuggable"
                println "\t\t\tminifyEnabled: $type.minifyEnabled"
            }
        }
    }
}
