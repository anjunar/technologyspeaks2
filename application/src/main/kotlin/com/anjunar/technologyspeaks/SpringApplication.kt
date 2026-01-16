package com.anjunar.technologyspeaks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringApplication {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            SpringContext.context = runApplication<SpringApplication>(*args)

            SpringContext.context.getBean(StartUpRunner::class.java).run()
        }

    }

}
