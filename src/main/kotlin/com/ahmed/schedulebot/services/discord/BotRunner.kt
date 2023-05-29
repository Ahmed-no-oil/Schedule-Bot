package com.ahmed.schedulebot.services.discord

import net.dv8tion.jda.api.JDABuilder
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(val eventsListener: EventsListener) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val token = System.getenv("DISCORD_TOKEN")
        assert(token != null, { "env var DISCORD_TOKEN must be set" })
        val builder: JDABuilder = JDABuilder.createDefault(token)
        builder.addEventListeners(eventsListener)
        builder.build()
    }
}
