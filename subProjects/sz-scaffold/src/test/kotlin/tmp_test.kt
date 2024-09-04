import jodd.io.FileNameUtil
import org.junit.Test
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class TempTest {

    @Test
    fun fileNameUtilTest() {
        val config_file = """C:\Users\drago\work\github\sz-vertx-kotlin\examples\archetype\sz-web-starter\conf\application.conf"""

        val conf_dir = FileNameUtil.getFullPath(config_file)

        val app_home = FileNameUtil.concat(conf_dir, "..")

        log.info(app_home)
    }

    @Test
    fun pathUtilTest() {
        val workingDir = Path.of(".")
        log.info(workingDir.absolutePathString())
        val configPath = Path.of("""archetype\sz-web-starter\conf\application.conf""")
        log.info(configPath.absolutePathString())
        val conf_dir = configPath.parent.absolutePathString()
        log.info(conf_dir)
        val app_home = configPath.parent.parent.absolutePathString()
        log.info(app_home)
    }

    companion object {
        val log = LoggerFactory.getLogger(TempTest::class.java)!!
    }
}