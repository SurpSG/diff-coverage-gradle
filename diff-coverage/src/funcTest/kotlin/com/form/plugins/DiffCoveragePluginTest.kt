package com.form.plugins

import com.form.coverage.tasks.git.JgitDiff
import org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.bsh.commands.dir
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class DiffCoveragePluginTest {

    @get:Rule
    var testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File
//    private lateinit var diffFilePath: String
//    private lateinit var gradleRunner: GradleRunner

    @Before
    fun setup() {
        buildFile = testProjectDir.newFile("build.gradle")

        buildFile.appendText("""
            plugins {
                id 'com.form.diff-coverage'
                id 'java'
                id 'jacoco'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                testImplementation 'junit:junit:4.12'
            }
        """.trimIndent())

//        diffFilePath = getResourceFile<DiffCoveragePluginTest>("test.diff.file")
//                .copyTo(testProjectDir.newFile("1.diff"), true)
//                .absolutePath.replace("\\", "/")

        getResourceFile<DiffCoveragePluginTest>("src")
                .copyRecursively(
                        testProjectDir.newFolder("src"),
                        true
                )

//        gradleRunner = GradleRunner.create()
//                .withPluginClasspath()
//                .withProjectDir(testProjectDir.root)
//                .withTestKitDir(testProjectDir.newFolder())
//                .apply {
//                    // gradle testkit jacoco support
//                    File("./build/testkit/testkit-gradle.properties")
//                            .copyTo(File(projectDir, "gradle.properties"))
//                }
//                .apply {
//                    withArguments("test").build()
//                }
    }

//    @Test
//    fun `diff-coverage should use git to generate diff`() {
        // setup
//        File("../.gitignore").copyTo(testProjectDir.root.resolve(".gitignore")).apply {
//            appendText("caches/")
//        }
//        val git = NativeGit(testProjectDir.root)
//        git.apply {
//            exec("init")
//            exec("add", ".gitignore")
//            exec("commit", "-m", "\"initial commit\"")
//        }
//
//        val oldVersionFile = "src/main/java/com/java/test/Class1.java"
//        testProjectDir.root.toPath().resolve(oldVersionFile).let {
//            getResourceFile<DiffCoveragePluginTest>("Class1GitTest.java")
//                    .copyTo(it.toFile(), true)
//        }
//        git.apply {
//            exec("add", oldVersionFile)
//            exec("commit", "-m", "\"add all\"")
//        }
//
//        getResourceFile<DiffCoveragePluginTest>("src").copyRecursively(
//                testProjectDir.root.resolve(File("src")),
//                true
//        )
//
//        buildFile.appendText("""
//
//            diffCoverageReport {
//                diffSource {
//                    git.compareWith 'HEAD'
//                }
//                violationRules {
//                    minLines = 0.7
//                    failOnViolation = true
//                }
//            }
//        """.trimIndent())
//
//        // run
//        val result = gradleRunner
//                .withArguments("diffCoverage", "-d")
//                .buildAndFail()
//
//        // assert
//        println(result.output)
//        assertTrue(result.output.contains("lines covered ratio is 0.6, but expected minimum is 0.7"))
//        assertEquals(FAILED, result.task(":diffCoverage")!!.outcome)
//    }

    @Test
    fun `jgitTest`() {
        // setup
        val gitignore = testProjectDir.root.resolve(".gitignore").apply {
            appendText("\n*")
            appendText("\n!*.java")
            appendText("\n!gitignore")
            appendText("\n!*/")
        }
        println("gitIgnore:\n${gitignore.readText()}")
        val git: Git = Git.init()
                .setDirectory(testProjectDir.root)
                .call()
        println("Created a new repository at " + git.repository.directory)



//        val git = NativeGit(testProjectDir.root)
//        git.apply {
////            exec("init")
//            exec("add", ".")
//            exec("commit", "--allow-empty", " -n", "-m", "\"initial commit\"")
//        }
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Added all").call();

        val oldVersionFile = "src/main/java/com/java/test/Class1.java"
        testProjectDir.root.toPath().resolve(oldVersionFile).let {
            getResourceFile<DiffCoveragePluginTest>("Class1GitTest.java")
                    .copyTo(it.toFile(), true)
        }
        git.add().addFilepattern(oldVersionFile).call();
        git.commit().setMessage("Added old file version").call();
//        git.apply {
//            exec("add", oldVersionFile)
//            exec("commit", "-m", "\"add all\"")
//        }

        getResourceFile<DiffCoveragePluginTest>("src").copyRecursively(
                testProjectDir.root.resolve(File("src")),
                true
        )

        NativeGit(testProjectDir.root).apply {
            println(exec("diff"))
            println("----")
            println(exec("diff", "HEAD"))
        }

        println("0------------------------------")
        println(JgitDiff(testProjectDir.root).obtain("refs/heads/master"))
        println("00---------------")
        println(JgitDiff(testProjectDir.root).obtain("HEAD"))
        println("1------------------------------")
    }
}
