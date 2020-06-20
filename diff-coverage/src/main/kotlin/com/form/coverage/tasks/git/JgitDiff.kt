package com.form.coverage.tasks.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.CoreConfig
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.treewalk.filter.TreeFilter
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.IllegalArgumentException
import java.util.logging.Logger


class JgitDiff(workingDir: File) {

    private val repository: Repository = initRepository(workingDir)

    private fun initRepository(workingDir: File): Repository = try {
        FileRepositoryBuilder().apply {
            findGitDir(workingDir)
            readEnvironment()
//            findGitDir(workingDir.resolve(".git"))
//            findGitDir()
            isMustExist = true
        }.build()
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
                "Git directory not found in the project root ${workingDir.absolutePath}",
                e
        )
    }

    fun obtain(commit: String): String {
        val diffContent = ByteArrayOutputStream()
        Git(repository).use {
            DiffFormatter(diffContent).apply {
                initialize()

                scan(
                        getTreeIterator(repository, commit),
                        FileTreeIterator(repository)
                ).forEach {
                    format(it)
                }

                close()
            }
        }

        return String(diffContent.toByteArray()).apply {
            println(this)
        }
    }

    private fun DiffFormatter.initialize() {
        setRepository(repository)

        val autoCRLF: CoreConfig.AutoCRLF = CoreConfig.AutoCRLF.FALSE
        val enum = repository.config.getEnum(
                ConfigConstants.CONFIG_CORE_SECTION,
                null,
                ConfigConstants.CONFIG_KEY_AUTOCRLF,
                autoCRLF
        )
        println("autoCRLF: $enum")
        repository.config.setEnum(
                ConfigConstants.CONFIG_CORE_SECTION,
                null,
                ConfigConstants.CONFIG_KEY_AUTOCRLF,
                if ("\r\n" == System.lineSeparator()) {
                    CoreConfig.AutoCRLF.TRUE
                } else {
                    CoreConfig.AutoCRLF.INPUT
                }
        )
        pathFilter = TreeFilter.ALL
    }

    private fun getTreeIterator(repo: Repository, name: String): AbstractTreeIterator {
        val id: ObjectId = repo.resolve(name)
        val parser = CanonicalTreeParser()
        repo.newObjectReader().use { objectReader ->
            RevWalk(repo).use { revWalk ->
                parser.reset(objectReader, revWalk.parseTree(id))
                return parser
            }
        }
    }
}

fun main() {
    val file = File("C:\\Users\\Crulio\\tmp\\test")
    file.deleteRecursively()
    file.mkdir()
    file.resolve(".gitignore").apply {
        createNewFile()
        writeText("""
        *
        !*.java
        !.gitignore
        !*/
    """.trimIndent())

    }
    val copyRecursively = File("C:\\Users\\Crulio\\IdeaProjects\\diff-coverage-gradle-fork\\diff-coverage\\src\\funcTest\\resources\\src")
            .copyRecursively(file.resolve("src"), true)
    val repository: Repository = FileRepositoryBuilder.create(File(file, ".git")).apply {
        config.setEnum(
                ConfigConstants.CONFIG_CORE_SECTION,
                null,
                ConfigConstants.CONFIG_KEY_AUTOCRLF,
                if ("\r\n" == System.lineSeparator()) {
                    CoreConfig.AutoCRLF.TRUE
                } else {
                    CoreConfig.AutoCRLF.INPUT
                }
        )
//        config.save()
        create()
    }
    Git(repository).use { git ->

        val oldVersionFile = "src\\main\\java\\com\\java\\test\\Class1.java"
        git.add().addFilepattern(".gitignore").call();
        git.add().addFilepattern(oldVersionFile).call();
        git.commit().setMessage("Added old file").call();
        git.add().addFilepattern(".").call();
        git.commit().setMessage("Added all").call();

        File("C:\\Users\\Crulio\\IdeaProjects\\diff-coverage-gradle-fork\\diff-coverage\\src\\funcTest\\resources\\Class1GitTest.java")
                .copyTo(file.resolve(oldVersionFile), true)
        println(JgitDiff(file).obtain("HEAD"))
//        git.add().addFilepattern(oldVersionFile).call();
//        git.commit().setMessage("Added old file version").call();
//
//        File("C:\\Users\\Crulio\\IdeaProjects\\diff-coverage-gradle-fork\\diff-coverage\\src\\funcTest\\resources\\src")
//                .copyRecursively(file.resolve("src"), true)
//
//        println("=================")
//        println(JgitDiff(file).obtain("HEAD"))
    }
}

inline fun <reified T> getResourceFile(filePath: String): File {
    return T::class.java.classLoader
            .getResource(filePath)!!.file
            .let(::File)
}
