package myquant.common

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.common.auth.CredentialsProvider
import com.aliyun.oss.common.auth.DefaultCredentialProvider
import java.io.ByteArrayInputStream

//
// Created by drago on 2024/5/27 周一.
//
class oss(accessKeyId: String, accessKeySecret: String, val bucketName: String, private val endpoint: String) {

    private val credentialsProvider: CredentialsProvider = DefaultCredentialProvider(accessKeyId, accessKeySecret)

    private val ossClient = OSSClientBuilder().build(endpoint, credentialsProvider)

    fun putObject(objectKey: String, body: ByteArray) {
        if (this.bucketName == "data-dists") {
            throw Exception("不允许向生产环境的 OSS 上传")
        }
        ossClient.putObject(this.bucketName, objectKey, ByteArrayInputStream(body) )
    }

    fun getObject(objectKey: String): ByteArray {
        val ossObject = this.ossClient.getObject(this.bucketName, objectKey)
        return ossObject.objectContent.buffered().readAllBytes()
    }
}