package com.floxd.schedulebot.services.discord

import com.floxd.schedulebot.models.DayInSchedule
import com.floxd.schedulebot.services.ScheduleImageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.FileUpload
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@Component
class EventsListener(
    var imageBuilder: ScheduleImageBuilder,
    @Value("\${my-guild.id}") val myGuildId: String,
    @Value("\${my-guild.schedule-channel-name}") val scheduleChannelName: String,
    @Value("\${my-guild.notify-role-id}") val notifyRoleId: String
) : EventListener {

    companion object {
        var selectedDay = DayOfWeek.MONDAY
        var isSettingThisWeek = false
        val weekData = mutableListOf<DayInSchedule>()
        var shouldNotify = true
        var postMessage = "Schedule for the week"
    }

    override fun onEvent(event: GenericEvent) {
        //todo add logging
        if (event is GuildReadyEvent) onGuildReady(event)
        if (event is SlashCommandInteractionEvent) onSlashCommandInteraction(event)
        if (event is StringSelectInteractionEvent) onStringSelectInteraction(event)
        if (event is ButtonInteractionEvent) onButtonInteraction(event)
        if (event is ModalInteractionEvent) onModalInteraction(event)
    }

    private fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.selectMenu.id == "dayInput") {
            selectedDay = DayOfWeek.of(event.selectedOptions.first().value.toInt())
            event.deferEdit().queue()
        }
    }

    private fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.button.id) {
            "setBtn" -> {
                event.replyModal(dayModal()).queue()
            }

            "cancelBtn" -> {
                event.editMessage("\nCommand was canceled by ${event.user.name}").setComponents().queue()
            }

            "publishBtn" -> {
                event.deferEdit().queue()
                //build schedule
                val weekDates = if(isSettingThisWeek) thisWeekDates() else nextWeekDates()
                imageBuilder.create(weekData).drawBackground().drawWeekDates(weekDates).drawBubbles().writeDaysNames().writeStreamOrNot().writeTimes().writeComments()
                if(LocalDate.now().month== Month.DECEMBER && LocalDate.now().dayOfMonth >= 10)
                    imageBuilder.drawXmasHat()
                val imageStream = imageBuilder.build()
                //post schedule
                val msgContent = if (shouldNotify) "<@&$notifyRoleId> $postMessage" else postMessage
                event.guild!!.getTextChannelsByName(scheduleChannelName, true).firstOrNull()
                    ?.sendFiles(FileUpload.fromData(imageStream, "schedule " + LocalDate.now().toString() + ".png"))
                    ?.setContent(msgContent)?.queue() ?: run {
                    event.guild!!.getNewsChannelsByName(scheduleChannelName, true).firstOrNull()?.sendFiles(
                        FileUpload.fromData(
                            imageStream, "schedule" + LocalDate.now().toString() + ".png"
                        )
                    )?.setContent(msgContent)?.queue() ?: run {
                        event.hook.editOriginal("error: couldn't find the channel $scheduleChannelName").setComponents()
                            .queue()
                    }
                }

                event.hook.editOriginal(event.message.contentRaw + "\nSchedule was published by ${event.user.name}")
                    .setComponents().queue()
            }

            "notifMsgBtn" -> {
                event.replyModal(
                    Modal.create("notifMsgModal", "Notification & Message").addActionRow(
                        TextInput.create(
                            "notifyOrNot", "Mention notification role?", TextInputStyle.SHORT
                        ).setMaxLength(3).setPlaceholder("yes or no").build()
                    ).addActionRow(
                        TextInput.create("msgInput", "Message with the post:", TextInputStyle.PARAGRAPH)
                            .setRequired(false).build()
                    ).build()
                ).queue()
            }
        }
    }

    private fun onModalInteraction(event: ModalInteractionEvent) {
        event.deferEdit().queue()
        if (event.message == null) {
            event.reply("error: can't find the command message. if no one has deleted it, contact developer.")
                .setEphemeral(true).queue()
            return
        }
        when (event.modalId) {
            "dayModal" -> {
                val dayInSchedule = DayInSchedule()
                dayInSchedule.day = selectedDay
                dayInSchedule.isGoingToStream = event.getValue("streamOrNot")!!.asString.contains("ye")
                event.getValue("streamTime")?.let { dayInSchedule.timeComment = it.asString }
                event.getValue("streamTime_data")?.let {
                    if (it.asString.isNotEmpty()) try {
                        dayInSchedule.timeData = LocalTime.parse(it.asString)
                    } catch (e: Exception) {
                        event.reply("The bot couldn't read the 3rd field. Please write it in hh:mm format")
                            .setEphemeral(true).queue()
                        return
                    }
                }
                event.getValue("comment")?.let { dayInSchedule.comment = it.asString }
                dayInSchedule.authorName = event.user.name
                weekData.removeIf { it.day == dayInSchedule.day }
                weekData.add(dayInSchedule)
                weekData.sortBy { it.day.value }

            }

            "notifMsgModal" -> {
                event.getValue("notifyOrNot")?.let { shouldNotify = it.asString.contains("ye") }
                event.getValue("msgInput")?.let { postMessage = it.asString }
            }
        }

        val firstLine =
            if (event.message!!.contentRaw.contains("\n")) event.message!!.contentRaw.substringBefore("\n") else event.message!!.contentRaw
        var savedData = ""
        weekData.forEach {
            savedData += "\n$it"
        }
        var notifMsg = if (shouldNotify) "\nNotification: Yes" else "\nNotification: NO"
        notifMsg += if (postMessage.isNotEmpty()) "\nMessage :$postMessage" else "\nMessage: $postMessage"
        event.hook.editOriginal(firstLine + savedData + notifMsg).setComponents(componentsForCommandsSet(weekData.count() == 7))
            .queue()
    }

    private fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.subcommandGroup) {
            "set" -> {

                isSettingThisWeek = event.subcommandName.equals("this_week")
                val firstLine =
                    if (isSettingThisWeek) "Set this week's schedule ${thisWeekDates()}" else "Set next week's schedule ${nextWeekDates()}"
                var savedData = ""
                weekData.forEach {
                    savedData += "\n $it"
                }
                var notifMsg = if (shouldNotify) "\nNotification: Yes" else "\nNotification: NO"
                event.reply(firstLine + savedData + notifMsg).addComponents(componentsForCommandsSet(weekData.count() == 7))
                    .queue()
            }
        }
    }

    private fun onGuildReady(event: GuildReadyEvent) {
        println("Guild ${event.guild.id} is ready")
        if (event.guild.id == myGuildId) {
            event.guild.updateCommands().addCommands(
                Commands.slash("schedule", "Stream schedule").addSubcommandGroups(
                    SubcommandGroupData("set", "Set stream schedule").addSubcommands(
                        SubcommandData("this_week", "set stream schedule"),
                        SubcommandData("next_week", "set stream schedule")
                    )
                ).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
            ).queue()
        }
    }

    private fun componentsForCommandsSet(publishBtnEnabled: Boolean): MutableCollection<LayoutComponent> {
        val weekOptions = mutableListOf<SelectOption>()
        DayOfWeek.values().forEach {
            weekOptions.add(
                SelectOption.of(
                    it.getDisplayName(TextStyle.FULL, Locale.ENGLISH), it.value.toString()
                )
            )
        }
        var publishBtn = Button.success("publishBtn", "Publish")
        if (!publishBtnEnabled) publishBtn = publishBtn.asDisabled()
        val dayInput: ItemComponent =
            StringSelectMenu.create("dayInput").setMaxValues(1).setPlaceholder("Day to set...").addOptions(weekOptions)
                .build()
        val actionRows = mutableListOf<LayoutComponent>()

        actionRows.add(ActionRow.of(dayInput))
        actionRows.add(ActionRow.of(Button.primary("setBtn", "Set")))
        actionRows.add(ActionRow.of(Button.primary("notifMsgBtn", "Notification & Message")))
        actionRows.add(ActionRow.of(Button.secondary("cancelBtn", "Cancel"), publishBtn))
        return actionRows
    }

    private fun dayModal(): Modal {
        return Modal.create("dayModal", selectedDay.name).addComponents(
            ActionRow.of(
                TextInput.create("streamOrNot", "Streaming that day?:", TextInputStyle.SHORT).setMinLength(2)
                    .setMaxLength(3).build()
            ), ActionRow.of(
                TextInput.create("streamTime", "If yes, when starts:", TextInputStyle.SHORT).setRequired(false)
                    .setPlaceholder("eg. 4-5 ish prob").setMaxLength(10).build()
            ), ActionRow.of(
                TextInput.create(
                    "streamTime_data", "Time in 24h format (for extra bot functions):", TextInputStyle.SHORT
                ).setRequired(false).setPlaceholder("eg. 18:00").setMinLength(5).setMaxLength(5).build()
            ), ActionRow.of(
                TextInput.create("comment", "Comment:", TextInputStyle.SHORT).setMaxLength(19).setRequired(false)
                    .build()
            )
        ).build()
    }

    private fun thisWeekDates(): String {
        var result: String
        Calendar.getInstance().let {
            it[Calendar.DAY_OF_WEEK] = it.firstDayOfWeek
            result = it[Calendar.DAY_OF_MONTH].toString()
            it.add(Calendar.DAY_OF_WEEK, 6)
            result += "-" + SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.time)
        }
        return result
    }

    private fun nextWeekDates(): String {
        var result: String
        Calendar.getInstance().let {
            it.add(Calendar.DAY_OF_WEEK, 7)
            it[Calendar.DAY_OF_WEEK] = it.firstDayOfWeek
            result = it[Calendar.DAY_OF_MONTH].toString()
            it.add(Calendar.DAY_OF_WEEK, 6)
            result += "-" + SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.time)
        }
        return result
    }
}