package com.api.server.controllers.sample.reply

import jodd.datetime.JDateTime
import jodd.datetime.ext.toJDateTime
import models.sample.User
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.reply.ReplyBase
import java.time.Instant
import java.util.*

//
// Created by kk on 2019/11/15.
//
class UserListReply : ReplyBase() {

    @Comment("用户列表")
    var users = listOf<DUser>()

    override fun SampleData() {
        users = listOf(
            DUser(
                user_id = UUID.randomUUID(),
                user_name = "张三",
                remarks = "备注信息:XXXXXXXXXX",
                create_time = JDateTime("2019-02-14 09:35:20", "YYYY-MM-DD hh:mm:ss")
            ),
            DUser(
                user_id = UUID.randomUUID(),
                user_name = "李四",
                remarks = "备注信息:XXXXXXXXXX",
                create_time = JDateTime("2019-02-08 19:15:22", "YYYY-MM-DD hh:mm:ss")
            ),
            DUser(
                user_id = UUID.randomUUID(),
                user_name = "王五",
                remarks = "备注信息:XXXXXXXXXX",
                create_time = JDateTime("2019-03-15 12:05:13", "YYYY-MM-DD hh:mm:ss")
            )
        )
    }
}

data class DUser(val user_id: UUID,
                 val user_name: String,
                 val remarks: String,
                 val create_time: JDateTime) {

    companion object {

        fun buildFrom(user: User): DUser {
            return DUser(
                user_id = user.user_id,
                user_name = user.name,
                remarks = user.remarks ?: "",
                create_time = user.whenCreated!!.toJDateTime()
            )
        }

    }
}