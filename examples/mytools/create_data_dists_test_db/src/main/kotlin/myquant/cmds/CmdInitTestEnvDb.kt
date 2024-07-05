package myquant.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.ebean.DB
import io.ebean.Database
import models.*
import models.query.*
import myquant.common.oss
import myquant.tools.toJsonPretty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//
// Created by drago on 2024/5/27 周一.
//
@Suppress("DuplicatedCode")
class CmdInitTestEnvDb : CliktCommand(help = "初始化测试环境数据库", name = "init_test_env_db") {

    private val log: Logger = LoggerFactory.getLogger("app")


    private lateinit var config: Config

    private lateinit var test_env_db: Database
    private lateinit var prod_env_db: Database
    private lateinit var exchanges: List<String>
    private lateinit var prod_env_oss: oss
    private lateinit var test_env_oss: oss

    override fun run() {
        initConfig()
        initDB()

        exchanges = config.getStringList("app.enabled_exchanges")

        prod_env_oss = oss(
            accessKeyId = config.getString("app.oss.prod_env.ACCESS_KEY_ID"),
            accessKeySecret = config.getString("app.oss.prod_env.ACCESS_KEY_SECRET"),
            bucketName = config.getString("app.oss.prod_env.BUCKET_NAME"),
            endpoint = config.getString("app.oss.prod_env.ENDPOINT")
        )

        test_env_oss = oss(
            accessKeyId = config.getString("app.oss.test_env.ACCESS_KEY_ID"),
            accessKeySecret = config.getString("app.oss.test_env.ACCESS_KEY_SECRET"),
            bucketName = config.getString("app.oss.test_env.BUCKET_NAME"),
            endpoint = config.getString("app.oss.test_env.ENDPOINT")
        )

        init_daybar_by_year_meta()
        init_daybar_by_day_meta()
        init_dividend_snapshot_meta()
        init_exchange_info()
        init_instrument_by_year_meta()
        init_instrument_by_day_meta()
        init_instrument_last_snapshot_meta()
        init_trade_calendar_meta()
        init_data_revision_by_day()
    }

    private fun initConfig() {
        config = ConfigFactory.load()
    }

    private fun initDB() {
        test_env_db = DB.byName("test_env_db")
        prod_env_db = DB.byName("prod_env_db")
    }

    private fun init_daybar_by_year_meta() {
        log.info("初始化测试环境 按年度分发的历史日线数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.daybar_by_year_meta.clean")) {
                val sql = "delete from daybar_by_year_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 daybar_by_year_meta")
            }

            val from_year = this.config.getInt("app.tables.daybar_by_year_meta.from_year")

            // 从生产库里查询记录
            val records = QDaybarByYearMeta(prod_env_db)
                .where().exchange.`in`(exchanges)
                .year.ge(from_year)
                .findList()

            records.forEach {
                // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
                // 从测试环境, 查询记录
                val exists_record = QDaybarByYearMeta(test_env_db).where()
                    .exchange.eq(it.exchange)
                    .year.eq(it.year)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("daybar_by_year_meta 记录已存在 ${it.exchange} ${it.year}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                // 在测试数据库里保存分发记录
                val rec = DaybarByYearMeta()
                rec.exchange = it.exchange
                rec.year = it.year
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.created_at = it.created_at
                rec.fixed_at = it.fixed_at
                rec.data_last_mtime = it.data_last_mtime

                test_env_db.save(rec)

                log.info("已初始化记录 daybar_by_year_meta: ${rec.exchange} ${rec.year}")
            }

            tx.commit()
        }
    }

    private fun init_daybar_by_day_meta() {
        log.info("初始化测试环境 按日分发的历史日线数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.daybar_by_day_meta.clean")) {
                val sql = "delete from daybar_by_day_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 daybar_by_day_meta")
            }

            // 从生产库里查询记录
            val from_date = LocalDate.parse(
                config.getString("app.tables.daybar_by_day_meta.from_date"),
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            val records = QDaybarByDayMeta(prod_env_db)
                .where().exchange.`in`(exchanges)
                .trade_date.ge(from_date)
                .findList()

            records.forEach {
                // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
                // 从测试环境, 查询记录
                val exists_record = QDaybarByDayMeta(test_env_db).where()
                    .exchange.eq(it.exchange)
                    .trade_date.eq(it.trade_date)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("记录已存在, 跳过 daybar_by_day_meta: ${it.exchange} ${it.trade_date}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                // 在测试数据库里保存分发记录
                val rec = DaybarByDayMeta()
                rec.exchange = it.exchange
                rec.trade_date = it.trade_date
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.created_at = it.created_at
                rec.fixed_at = it.fixed_at
                rec.data_last_mtime = it.data_last_mtime

                test_env_db.save(rec)
                log.info("已初始化记录 daybar_by_day_meta: ${rec.exchange} ${rec.trade_date}")
            }

            tx.commit()
        }
    }

    private fun init_dividend_snapshot_meta() {
        log.info("初始化测试环境分红配送数据")
        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.dividend_snapshot_meta.clean")) {
                val sql = "delete from dividend_snapshot_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 dividend_snapshot_meta")
            }

            // 从生产库里查询记录
            val records = QDividendSnapshotMeta(prod_env_db).findList()

            records.forEach {
                val exists_record = QDividendSnapshotMeta(test_env_db).where()
                    .type.eq(it.type)
                    .setMaxRows(1)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("记录已存在, 跳过 dividend_snapshot_meta: ${it.type}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                // 在测试数据库里保存分发记录
                val rec = DividendSnapshotMeta()
                rec.type = it.type
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.updated_at = it.updated_at
                rec.snapshot_last_mtime = it.snapshot_last_mtime
                rec.snapshot_id = it.snapshot_id

                test_env_db.save(rec)
                log.info("已初始化记录 dividend_snapshot_meta: ${rec.type} ${rec.snapshot_last_mtime}")
            }

            tx.commit()
        }
    }

    private fun init_exchange_info() {
        log.info("初始化交易所数据范围信息")
        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.exchange_info.clean")) {
                val sql = "delete from exchange_info"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 exchange_info")
            }

            // 从生产库里查询记录
            val records = QExchangeInfo(prod_env_db).findList()

            records.forEach {
                val rec = ExchangeInfo()
                rec.exchange = it.exchange
                rec.first_date = it.first_date

                test_env_db.save(rec)
            }

            tx.commit()
            log.info("已初始化 exchange_info 表")
        }
    }

    private fun init_instrument_by_year_meta() {
        log.info("初始化按年分发的历史码表数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.instrument_by_year_meta.clean")) {
                val sql = "delete from instrument_by_year_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 instrument_by_year_meta")
            }

            val from_year = this.config.getInt("app.tables.instrument_by_year_meta.from_year")

            val records = QInstrumentByYearMeta(prod_env_db)
                .where()
                .exchange.`in`(exchanges)
                .year.ge(from_year)
                .findList()

            records.forEach {
                // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
                // 从测试环境, 查询记录
                val exists_record = QInstrumentByYearMeta(test_env_db).where()
                    .exchange.eq(it.exchange)
                    .year.eq(it.year)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("记录已存在, 跳过 instrument_by_year_meta: ${it.exchange} ${it.year}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                val rec = InstrumentByYearMeta()
                rec.exchange = it.exchange
                rec.year = it.year
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.created_at = it.created_at
                rec.fixed_at = it.fixed_at
                rec.data_last_mtime = it.data_last_mtime

                test_env_db.save(rec)
                log.info("已初始化记录 instrument_by_year_meta: ${rec.exchange} ${rec.year}")
            }

            tx.commit()
        }
    }

    private fun init_instrument_by_day_meta() {
        log.info("初始化按交易日分发的历史码表")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.instrument_by_day_meta.clean")) {
                val sql = "delete from instrument_by_day_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 instrument_by_day_meta")
            }

            // 从生产库里查询记录
            val from_date = LocalDate.parse(
                config.getString("app.tables.instrument_by_day_meta.from_date"),
                DateTimeFormatter.ISO_LOCAL_DATE
            )

            val records = QInstrumentByDayMeta(prod_env_db)
                .where().exchange.`in`(exchanges)
                .trade_date.ge(from_date)
                .findList()

            records.forEach {
                // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
                // 从测试环境, 查询记录
                val exists_record = QInstrumentByDayMeta(test_env_db).where()
                    .exchange.eq(it.exchange)
                    .trade_date.eq(it.trade_date)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("记录已存在, 跳过 instrument_by_day_meta: ${it.exchange} ${it.trade_date}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                val rec = InstrumentByDayMeta()
                rec.exchange = it.exchange
                rec.trade_date = it.trade_date
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.created_at = it.created_at
                rec.fixed_at = it.fixed_at
                rec.data_last_mtime = it.data_last_mtime

                test_env_db.save(rec)
            }

            tx.commit()
        }
    }

    private fun init_instrument_last_snapshot_meta() {
        log.info("初始化码表快照数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.instrument_last_snapshot_meta.clean")) {
                val sql = "delete from instrument_last_snapshot_meta"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 instrument_last_snapshot_meta")
            }

            exchanges.forEach { exchange ->
                val record = QInstrumentLastSnapshotMeta(prod_env_db).where()
                    .exchange.eq(exchange)
                    .orderBy().created_at.desc()
                    .setMaxRows(1)
                    .findOne()!!

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(record.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(record.oss_object, data)

                val rec = InstrumentLastSnapshotMeta()
                rec.exchange = record.exchange
                rec.snapshot_id = record.snapshot_id
                rec.oss_bucket = record.oss_bucket
                rec.oss_object = record.oss_object
                rec.size = record.size
                rec.sha1 = record.sha1
                rec.dists_type = record.dists_type
                rec.other_dists_info = record.other_dists_info
                rec.created_at = record.created_at

                test_env_db.save(rec)

                log.info("码表快照数据初始化: ${rec.exchange}, ${rec.oss_object}")
            }

            tx.commit()
        }

        log.info("码表快照数据初始化完成")
    }

    private fun init_trade_calendar_meta() {
        log.info("初始化交易日历数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.trade_calendar_meta.clean")) {
                val sql = "delete from trade_calendar_meta"
                test_env_db.sqlUpdate(sql).execute()
            }

            // 从生产环境查询
            val record = QTradeCalendarMeta(prod_env_db).findOne()!!

            // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
            // 从测试环境, 查询记录
            val exists_record = QTradeCalendarMeta(test_env_db).findOne()

            if (exists_record != null) {
                if (exists_record.sha1 == record.sha1) {
                    // 记录已存在, 跳过
                    log.info("日历没有发生更新, 跳过")
                    return
                } else {
                    // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                    test_env_db.delete(exists_record)
                }
            }

            // 从 prod_env_oss 下载
            val data = prod_env_oss.getObject(record.oss_object)

            // 上传到 test_env_oss
            test_env_oss.putObject(record.oss_object, data)

            val rec = TradeCalendarMeta()
            rec.oss_bucket = record.oss_bucket
            rec.oss_object = record.oss_object
            rec.size = record.size
            rec.sha1 = record.sha1
            rec.dists_type = record.dists_type
            rec.other_dists_info = record.other_dists_info
            rec.updated_at = record.updated_at

            test_env_db.save(rec)

            tx.commit()
        }

        log.info("交易日历数据初始化完成")
    }

    private fun init_data_revision_by_day() {
        log.info("初始化测试环境 data_revision_by_day 补丁记录数据")

        test_env_db.beginTransaction().use { tx ->
            if (this.config.getBoolean("app.tables.data_revision_by_day.clean")) {
                val sql = "delete from data_revision_by_day"
                test_env_db.sqlUpdate(sql).execute()
                log.info("已清空表 data_revision_by_day")
            }

            // 从生产库里查询记录
            val from_date = LocalDate.parse(
                config.getString("app.tables.data_revision_by_day.from_date"),
                DateTimeFormatter.ISO_LOCAL_DATE
            )
            val records = QDataRevisionByDay(prod_env_db)
                .where().exchange.`in`(exchanges)
                .trade_date.ge(from_date)
                .findList()

            records.forEach {
                // 根据 sha1 记录, 判断是否已经存在已有记录, 如果存在就跳过
                // 从测试环境, 查询记录
                val exists_record = QDataRevisionByDay(test_env_db).where()
                    .exchange.eq(it.exchange)
                    .symbol.eq(it.symbol)
                    .trade_date.eq(it.trade_date)
                    .data_type.eq(it.data_type)
                    .findOne()

                if (exists_record != null) {
                    if (exists_record.sha1 == it.sha1) {
                        // 记录已存在, 跳过
                        log.info("补丁记录已存在, 跳过 ${it.exchange} ${it.symbol} ${it.trade_date} ${it.data_type}")
                        return@forEach
                    } else {
                        // sha1 不一致, 该记录的数据需要被删除, 重新上传数据并生成该条记录
                        test_env_db.delete(exists_record)
                    }
                }

                // 从 prod_env_oss 下载
                val data = prod_env_oss.getObject(it.oss_object)

                // 上传到 test_env_oss
                test_env_oss.putObject(it.oss_object, data)

                // 在测试数据库里保存补丁记录
                val rec = DataRevisionByDay()
                rec.exchange = it.exchange
                rec.symbol = it.symbol
                rec.trade_date = it.trade_date
                rec.data_type = it.data_type
                rec.oss_bucket = it.oss_bucket
                rec.oss_object = it.oss_object
                rec.size = it.size
                rec.sha1 = it.sha1
                rec.dists_type = it.dists_type
                rec.other_dists_info = it.other_dists_info
                rec.last_cutime = it.last_cutime
                rec.created_at = it.created_at
                rec.records_count = it.records_count
                rec.last_mtime = it.last_mtime

                test_env_db.save(rec)
                log.info("已初始化记录 data_revision_by_day: ${rec.exchange} ${rec.trade_date} ${rec.sha1}")
            }

            tx.commit()
        }

        log.info("测试环境 data_revision_by_day 补丁记录数据初始化完成")
    }
}