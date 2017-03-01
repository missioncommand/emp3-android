package mil.emp3.gradle;

import org.gradle.api.*;

class EmpCommonPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.configure(project) {
            apply from: EmpCommonPlugin.class.classLoader.getResource("common.gradle")
        }
        
        project.task('EchoTaskCommon', description: "Displays project.ext.properties and project.dependencyManagement.dependencies.") << {
            println 'I am EmpCommonPlugin plugin!'

            println 'project.ext.properties:'
            project.ext.properties.each { k, v -> println "\t${k} = ${v}" }

            println 'project.dependencyManagement.dependencies:'
            project.dependencyManagement.dependencyManagementContainer.globalDependencyManagement.versions.each { k, v -> println "\t${k} = ${v}" }
        }
    }
}
