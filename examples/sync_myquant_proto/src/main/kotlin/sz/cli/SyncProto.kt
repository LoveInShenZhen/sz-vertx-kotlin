package sz.cli

import jodd.io.FileNameUtil
import jodd.io.findfile.FindFile
import sz.cli.config.ProtoDir
import sz.cli.config.ProtoSource
import java.io.File

//
// Created by kk on 2021/10/4.
//
class SyncProto(val protoSource: ProtoSource) {

    fun Sync() {
        this.protoSource.proto_dirs.forEach {
            this.Sync(it)
        }
    }

    fun Sync(protoDir: ProtoDir) {
//        FileUtil.mkdirs(protoDir.dest_path)

        logger.debug("sync for : ${protoDir}")
        protoFiles(protoDir).forEach {
            logger.info("file: ${it.absolutePath}")

        }
    }

    fun protoFiles(protoDir: ProtoDir): List<File> {
        val path = FileNameUtil.concat(protoSource.src_base_dir, protoDir.src_path)
        logger.debug("path: $path")
        return FindFile.createWildcardFF()
            .matchOnlyFileName()
            .include("*.proto")
            .searchPath(File(path))
            .findAll()
    }

    fun process(protoFile: File, protoDir: ProtoDir) {
        val lines = mutableListOf<String>()

    }

}