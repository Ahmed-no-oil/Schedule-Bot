package com.floxd.schedulebot.services.discord

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class EventsListener : EventListener {
    private val ELINA_SERVER_ID = "399628142886780949"
    private val TESTING_SERVER_ID = "1094555160732241961"

    override fun onEvent(event: GenericEvent) {
        if (event is GuildReadyEvent) onGuildReady(event)
        if (event is SlashCommandInteractionEvent) onSlashCommandInteraction(event)
    }

    private fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.fullCommandName) {
            "schedule set" -> {
                val classloader = Thread.currentThread().contextClassLoader
                val inputStream = classloader.getResourceAsStream("image.templates/schedule1stmay-Recovered.png")
                    ?: throw Exception("couldn't find file")
                event.replyFiles(FileUpload.fromData(inputStream, "schedule" + LocalDate.now().toString() + ".png"))
                    .queue()
            }
        }
    }

    private fun onGuildReady(event: GuildReadyEvent) {
        if (event.guild.id == TESTING_SERVER_ID || event.guild.id == ELINA_SERVER_ID) {
            event.guild.updateCommands().addCommands(
                Commands.slash("schedule", "Stream schedule").addSubcommands(
                    SubcommandData("set", "Set the schedule for the week.")
                )
            ).queue()
        }
    }
}