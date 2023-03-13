package sz.cli.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

//
// Created by kk on 2021/10/4.
//

data class ProtoDir(
    var src_path: String,
    var dest_path: String,
    // 当 proto 文件中, import 的文件名有冲突时, 改成 import 此映射中的文件名
    val file_mapping: MutableMap<String, String> = mutableMapOf(),
    // 当 源 proto 文件, 与其他目录里有同名文件时, 改成指定的新文件名
    val rename_to: MutableMap<String, String> = mutableMapOf()
) {
    fun ToUnixPath(): ProtoDir {
        this.src_path = this.src_path.replace("\\", "/")
        this.dest_path = this.dest_path.replace("\\", "/")

        return this
    }
}

data class ThirdProtoDir(
    var src_path: String,
    var dest_path: String,
) {
    fun ToUnixPath(): ThirdProtoDir {
        this.src_path = this.src_path.replace("\\", "/")
        this.dest_path = this.dest_path.replace("\\", "/")

        return this
    }
}

data class ProtoSource(
    var src_base_dir: String,
    var dest_base_dir: String,
    val path_prefix_replace: Map<String, String>,
    val file_mapping: MutableMap<String, String>,
    val proto_dirs: List<ProtoDir>,
    val third_proto_dirs: List<ThirdProtoDir>
) {
    fun ToUnixPath(): ProtoSource {
        this.src_base_dir = this.src_base_dir.replace("\\", "/")
        this.dest_base_dir = this.dest_base_dir.replace("\\", "/")

        this.proto_dirs.forEach {
            it.ToUnixPath()
        }

        this.third_proto_dirs.forEach {
            it.ToUnixPath()
        }

        return this
    }
}

val config: Config = ConfigFactory.defaultApplication()
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

val protos: ProtoSource = config.extract<ProtoSource>("protos").ToUnixPath()

