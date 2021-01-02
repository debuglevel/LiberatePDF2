package rocks.huwi.liberatepdf2.restservice

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import rocks.huwi.liberatepdf2.restservice.storage.StorageProperties
import rocks.huwi.liberatepdf2.restservice.storage.StorageService
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableSwagger2
@EnableAsync
@EnableConfigurationProperties(StorageProperties::class)
open class Application {
    @Bean
    open fun initializeStorageService(storageService: StorageService): CommandLineRunner {
        return CommandLineRunner { args: Array<String?>? -> storageService.initialize() }
    }

    @Bean
    open fun taskExecutor(): TaskExecutor {
        log.debug("Setting up TaskExecutor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.setQueueCapacity(25)
        return executor
    }

    companion object {
        private val log = LoggerFactory.getLogger(Application::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}