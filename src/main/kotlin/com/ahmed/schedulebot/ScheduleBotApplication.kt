package com.ahmed.schedulebot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class ScheduleBotApplication

fun main(args: Array<String>) {
    runApplication<ScheduleBotApplication>(*args)
}
