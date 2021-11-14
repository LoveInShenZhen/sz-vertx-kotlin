package sz.cli.config

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

//
// Created by kk on 2021/10/4.
//

data class ProtoDir(
    val src_path: String,
    val dest_path: String,
    // 当 proto 文件中, import 的文件名有冲突时, 改成 import 此映射中的文件名
    val file_mapping: MutableMap<String, String> = mutableMapOf(),
    // 当 源 proto 文件, 与其他目录里有同名文件时, 改成指定的新文件名
    val rename_to: MutableMap<String, String> = mutableMapOf()
)

data class ProtoSource(
    val src_base_dir: String,
    val dest_base_dir: String,
    val path_prefix_replace: Map<String, String>,
    val file_mapping: MutableMap<String, String>,
    val proto_dirs: List<ProtoDir>
)

val config = ConfigFactory.defaultApplication()
    .withFallback(ConfigFactory.systemProperties())
    .resolve()

val protos: ProtoSource = config.extract("protos")

