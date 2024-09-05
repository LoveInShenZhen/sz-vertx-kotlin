package com.myquant.controllers.sample


import com.myquant.MainApp.Companion.log
import com.myquant.controllers.sample.reply.HelloReply
import kotlinx.coroutines.future.await
import sz.scaffold.annotations.Comment
import sz.scaffold.controller.ApiController
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool


@Suppress("DuplicatedCode")
@Comment("测试样例代码")
class SampleController : ApiController() {

  @Comment("测试接口")
  suspend fun hello(@Comment("访问者名称") name: String): HelloReply {
    // 在 ForkJoinPool.commonPool 里执行协程
    // 因为 ebean 不支持协程, 所以可以采取如下的方式, 在 ForkJoinPool.commonPool 的线程上执行, 但是又不会阻塞 vertx 的协程
    log.debug("看看我在哪个线程上")
    return CompletableFuture.supplyAsync {
      val reply = HelloReply()

      reply.msg = "Hello $name, 准备就绪, 请开始你的表演!"

      log.debug(reply.msg)

      log.debug(ForkJoinPool.getCommonPoolParallelism().toString())

      return@supplyAsync reply
    }.await()
  }

}
