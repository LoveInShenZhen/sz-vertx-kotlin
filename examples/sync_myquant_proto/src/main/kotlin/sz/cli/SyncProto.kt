package sz.cli

import jodd.io.FileNameUtil
import jodd.io.FileUtil
import jodd.io.findfile.FindFile
import jodd.util.StringUtil
import sz.cli.config.ProtoDir
import sz.cli.config.ProtoSource
import java.io.File

//
// Created by kk on 2021/10/4.
//
class SyncProto(val protoSource: ProtoSource) {

    fun Sync() {
        this.scan()
        this.protoSource.proto_dirs.forEach {
            this.Sync(it)
        }
    }

    fun scan() {
        this.protoSource.proto_dirs.forEach { proto_dir ->
            val files = destProtoFiles(proto_dir)
            files.forEach { protoFile ->
                if (this.protoSource.file_mapping.contains(protoFile.name)) {
                    // 发现有同名的 proto 文件, 准备 rename
                    if (proto_dir.rename_to.contains(protoFile.name)) {
                        val new_name = proto_dir.rename_to[protoFile.name]!!

                        this.protoSource.file_mapping[new_name] =
                            protoFile.absolutePath.removePrefix(this.protoSource.dest_base_dir)
                                .removePrefix("/")
                                .replace("/api/", "/")
                        logger.warn("proto 文件: ${protoFile.absolutePath} 被重命名为 ${new_name}")
                    } else {
                        logger.error("重复的 proto 文件名, 需要在配置文件里单独进行映射单独进行映射. src_dir: ${proto_dir.src_path} file: ${protoFile.name}")
                    }
                } else {
                    this.protoSource.file_mapping[protoFile.name] =
                        protoFile.absolutePath.removePrefix(this.protoSource.dest_base_dir)
                            .removePrefix("/")
                            .replace("/api/", "/")
                }
            }
        }
    }

    fun Sync(protoDir: ProtoDir) {
        val dest_dir = FileNameUtil.concat(this.protoSource.dest_base_dir, protoDir.dest_path)
        FileUtil.mkdirs(dest_dir)

        // clean old proto files
        FileUtil.cleanDir(dest_dir)

        logger.debug("sync for : ${protoDir}")
        srcProtoFiles(protoDir).forEach { srcProtoFile ->
            logger.info("sync file: ${srcProtoFile.absolutePath}")
            val lines = mutableListOf<String>()


            val destFileName = protoDir.rename_to.getOrDefault(srcProtoFile.name, srcProtoFile.name)
            val destProtoFile = File(FileNameUtil.concat(dest_dir, destFileName))

            srcProtoFile.forEachLine { line ->
                if (isSyntaxLine(line)) {
                    lines.add(line)
                    lines.add("")
                    lines.add("option java_multiple_files = false;")
                    lines.add(
                        """option java_outer_classname = "${
                            this.javaOuterClassName(
                                protoDir,
                                destProtoFile
                            )
                        }";"""
                    )
                    lines.add("""option java_package = "${this.javaPackage(protoDir)}";""")

                    return@forEachLine
                }

                if (isImportLine(line)) {
                    val importPath = this.importOf(line)

                    if (protoDir.file_mapping.contains(importPath)) {
                        // 以 file_mapping 的优先
                        lines.add("""import "${protoDir.file_mapping[importPath]}";""")
                    } else if (this.protoSource.file_mapping.contains(importPath)) {
                        lines.add("""import "${this.protoSource.file_mapping[importPath]}";""")
                    } else if (this.importPathNeedReplacPrefix(importPath)) {
                        lines.add("""import "${this.importPathReplacPrefix(importPath)}";""")
                    } else {
                        lines.add(line)
                    }

                    return@forEachLine
                }

                lines.add(line)
            }

            destProtoFile.writeText(lines.joinToString("\n"))
        }
    }

    fun srcProtoFiles(protoDir: ProtoDir): List<File> {
        val path = FileNameUtil.concat(protoSource.src_base_dir, protoDir.src_path)
        return FindFile.createWildcardFF()
            .matchOnlyFileName()
            .include("*.proto")
            .searchPath(File(path))
            .findAll()
    }

    fun destProtoFiles(protoDir: ProtoDir): List<File> {
        val destDirPath = FileNameUtil.concat(protoSource.dest_base_dir, protoDir.dest_path)
        return srcProtoFiles(protoDir).map { srcProtoFile ->
            val filePath = FileNameUtil.concat(destDirPath, srcProtoFile.name)
            File(filePath)
        }
    }

    fun process(protoFile: File, protoDir: ProtoDir) {
        val lines = mutableListOf<String>()

    }

    // match line: syntax = "proto3";
    val regexSyntax = Regex("""\s*syntax\s*=\s*"(\S+)"\s*;\s*""")
    fun isSyntaxLine(line: String): Boolean {
        return regexSyntax.matches(line)
    }

    // match line: package tradeaccount.api;
    val regexPackage = Regex("""\s*package\s+(?<packagename>\S+)\s*;\s*""")

    fun isPackageLine(line: String): Boolean {
        return regexPackage.matches(line)
    }

    fun packageOf(line: String): String {
        return regexPackage.matchEntire(line)!!.groups["packagename"]!!.value
    }

    // match line: import "core/account.proto";
    val regexImport = Regex("""\s*import\s+"(?<importpath>\S+)"\s*;\s*""")

    fun isImportLine(line: String): Boolean {
        return regexImport.matches(line)
    }

    fun importOf(line: String): String {
        return regexImport.matchEntire(line)!!.groups["importpath"]!!.value
    }

    fun importPathNeedReplacPrefix(importPath: String): Boolean {
        var needReplace = false
        this.protoSource.path_prefix_replace.forEach {
            if (importPath.startsWith(it.key)) {
                needReplace = true
            }
        }

        return needReplace
    }

    fun importPathReplacPrefix(importPath: String): String {
        var newPath = importPath
        this.protoSource.path_prefix_replace.forEach {
            if (importPath.startsWith(it.key)) {
                newPath = importPath.replace(it.key, it.value)
            }
        }

        return newPath
    }

    fun javaOuterClassName(protoDir: ProtoDir, protoFile: File): String {
        val fileName = protoFile.nameWithoutExtension
        val parts = fileName.split(".", "-").toMutableList()
        parts.add("proto")
        return parts.map {
            StringUtil.capitalize(it.lowercase())
        }.joinToString("")
    }

    fun javaPackage(protoDir: ProtoDir): String {
        val parts = protoDir.dest_path.split("/")
        return "myquant.proto.${parts.joinToString(".")}"
    }

}