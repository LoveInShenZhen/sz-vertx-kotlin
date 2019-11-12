package sz.objectPool

//
// Created by kk on 2019/10/23.
//
class PoolConfig(
    // 数量控制参数
    // 对象池中最大对象数,默认为8
    val maxTotal: Int = 8,
    // 对象池中最大空闲的对象数,默认也为8
    val maxIdle: Int = 8,
    // 对象池中最少空闲的对象数,默认为 -1
    val minIdle: Int = -1,
    // 驱逐检测的间隔时间, 默认10分钟
    val timeBetweenEvictionRunsSeconds: Int = 600,
    // 从对象池里借对象时的超时时间, 拍脑袋决定默认值 5000 ms
    // 设置为 0 或者负数的时候, 表示不进行超时控制
    val borrowTimeoutMs: Long = 5000
)