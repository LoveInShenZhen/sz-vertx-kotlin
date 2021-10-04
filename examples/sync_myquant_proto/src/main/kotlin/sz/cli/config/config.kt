package sz.cli.config

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

//
// Created by kk on 2021/10/4.
//

data class ProtoDir(
    val src_path: String,
    val dest_path: String,
    val java_package: String,
    val file_mapping: Map<String, String>
)

data class ProtoSource(
    val src_base_dir: String,
    val dest_base_dir: String,
    val path_prefix_replace: Map<String, String>,
    val file_mapping: Map<String, String>,
    val proto_dirs: List<ProtoDir>
)

val config = ConfigFactory.defaultApplication()

val protos: ProtoSource = config.extract("protos")

