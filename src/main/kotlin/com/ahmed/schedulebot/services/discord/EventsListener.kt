package com.ahmed.schedulebot.services.discord

import com.ahmed.schedulebot.entities.Day
import com.ahmed.schedulebot.entities.ScheduleEntry
import com.ahmed.schedulebot.services.HistoryService
import com.ahmed.schedulebot.services.ScheduleDataService
import com.ahmed.schedulebot.services.ScheduleImageBuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.EventListener
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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.TextStyle
import java.util.*

@Component
class EventsListener(
        val imageBuilder: ScheduleImageBuilder,
        val dataService: ScheduleDataService,
        val historyLogger: HistoryService,
        @Value("\${my-guild.id}") val myGuildId: String,
        @Value("\${my-guild.schedule-channel-id}") val scheduleChannelId: String,
        @Value("\${my-guild.notify-role-id}") val notifyRoleId: String
) : EventListener {

    private val LOGGER = LoggerFactory.getLogger(EventsListener::class.java)
    private var latestMsgId = 0L
    private var isNewSetCommandInteraction = true
    private var selectedDay = DayOfWeek.MONDAY
    private var isSettingThisWeek = false
    private var weekData = mutableListOf<ScheduleEntry>()
    private var shouldNotify = false
    private var publishingMessage = ""

    override fun onEvent(event: GenericEvent) {
        //todo add logging
        try {
            if (event is GuildReadyEvent) onGuildReady(event)
            if (event is SlashCommandInteractionEvent) onSlashCommandInteraction(event)
            if (event is ButtonInteractionEvent) onButtonInteraction(event)
            if (event is StringSelectInteractionEvent) onStringSelectInteraction(event)
            if (event is ModalInteractionEvent) onModalInteraction(event)
        } catch (e: Exception) {
            LOGGER.error("Error happened :(", e)
        }
    }

    private fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (event.selectMenu.id == "dayInput") {
            if (!checkIfLatestMessage(event)) return
            selectedDay = DayOfWeek.of(event.selectedOptions.first().value.toInt())
            event.deferEdit().queue()
        }
    }

    private fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (!checkIfLatestMessage(event)) return
        when (event.button.id) {
            "setBtn" -> {
                event.replyModal(dayModal()).queue()
            }

            "cancelBtn" -> {
                historyLogger.log("canceled the command", event.user.name)
                event.editMessage("Command was canceled").setComponents().queue()
            }

            "publishBtn" -> {
                event.deferEdit().queue()
                //write to database
                if (weekData.count() < 7) {
                    event.hook.editOriginal("Data in this command was lost. Start a new one").setComponents().queue()
                    return
                }
                dataService.saveWeekData(getYear(!isSettingThisWeek),getWeekNumber(!isSettingThisWeek), weekData)
                //build schedule image
                imageBuilder.create(weekData).drawBackground().drawWeekDates(getWeekDates()).drawBubbles()
                        .writeDaysNames().writeStreamOrNot().writeTimes().writeComments().writeFootnote()
                if (LocalDate.now().month == Month.DECEMBER && LocalDate.now().dayOfMonth >= 10) imageBuilder.drawXmasHat()
                val imageStream = imageBuilder.build()
                //post schedule
                val msgContent = if (shouldNotify) "<@&$notifyRoleId> $publishingMessage" else publishingMessage
                event.guild!!.getTextChannelById(scheduleChannelId)
                        ?.sendFiles(FileUpload.fromData(imageStream, "schedule " + LocalDate.now().toString() + ".png"))
                        ?.setContent(msgContent)?.queue()
                        ?: run {
                            event.guild!!.getNewsChannelById(scheduleChannelId)?.sendFiles(
                                    FileUpload.fromData(imageStream, "schedule" + LocalDate.now().toString() + ".png")
                            )?.setContent(msgContent)?.queue()
                                    ?: run {
                                        event.hook.editOriginal("error: couldn't find the channel $scheduleChannelId")
                                                .setComponents()
                                                .queue()
                                        return
                                    }
                        }
                historyLogger.log("published schedule", event.user.name)
                event.hook.editOriginal(event.message.contentRaw + "\nSchedule was published").setComponents().queue()
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
            event.hook.sendMessage("error: can't find the command message. if no one has deleted it, contact developer.")
                    .setEphemeral(true).queue()
            return
        }
        when (event.modalId) {
            "dayModal" -> {
                val entry = ScheduleEntry()
                entry.day = Day(selectedDay.value, selectedDay)
                entry.isGoingToStream = event.getValue("streamOrNot")!!.asString.contains("ye", true)
                event.getValue("streamTime")?.let { entry.timeComment = it.asString }
                event.getValue("streamTime_data")?.let {
                    var time = it.asString
                    if (time.isEmpty())
                        time = "00:00"
                    try {
                        entry.dateTime =
                                LocalDateTime.of(getSelectedDayDate(), LocalTime.parse(time)).atZone(ZoneId.of("CET"))
                                        .toOffsetDateTime()
                    } catch (e: Exception) {
                        event.hook.sendMessage("The bot couldn't read the 3rd field. Please write it in hh:mm format")
                                .setEphemeral(true).queue()
                        return
                    }
                }
                event.getValue("comment")?.let { entry.comment = it.asString }
                weekData.find { it.day.name == entry.day.name }?.let {
                    dataService.deleteScheduleEntry(it)
                    weekData.remove(it)
                }
                weekData.add(entry)
                weekData.sortBy { it.day.name.value }
                historyLogger.log("set $selectedDay", event.user.name)
            }

            "notifMsgModal" -> {
                event.getValue("notifyOrNot")?.let { shouldNotify = it.asString.contains("ye", true) }
                event.getValue("msgInput")?.let { publishingMessage = it.asString }
                historyLogger.log("Notification & Message", event.user.name)
            }
        }

        val firstLine =
                if (event.message!!.contentRaw.contains("\n")) event.message!!.contentRaw.substringBefore("\n") else event.message!!.contentRaw
        var savedData = ""
        weekData.forEach {
            savedData += "\n$it"
        }
        var notifMsg = if (shouldNotify) "\nNotification: Yes" else "\nNotification: NO"
        notifMsg += if (publishingMessage.isNotEmpty()) "\nMessage :$publishingMessage" else "\nMessage: $publishingMessage"
        event.hook.editOriginal(firstLine + savedData + notifMsg)
                .setComponents(componentsForCommandsSet(weekData.count() == 7)).queue()
    }

    private fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "schedule" -> {
                event.deferReply().queue()
                when (event.subcommandGroup) {
                    "set" -> {
                        isNewSetCommandInteraction = true
                        isSettingThisWeek = event.subcommandName.equals("this_week")
                        //try to get data from database
                        weekData = dataService.findWeekData(getYear(!isSettingThisWeek),getWeekNumber(!isSettingThisWeek)) ?: mutableListOf()
                        val firstLine =
                                (if (isSettingThisWeek) "Set this week's schedule " else "Set next week's schedule ") + getWeekDates()
                        var savedData = ""
                        weekData.forEach {
                            savedData += "\n $it"
                        }
                        val notifMsg = if (shouldNotify) "\nNotification: Yes" else "\nNotification: NO"
                        event.hook.sendMessage(firstLine + savedData + notifMsg)
                                .addComponents(componentsForCommandsSet(weekData.count() == 7)).queue()
                    }

                    "history" -> {
                        if (event.subcommandName == "show_log") {
                            val history = historyLogger.getLast20Logs()
                            var message = "The last 20 command interactions:"
                            history.forEach {
                                message += "\n* $it"
                            }
                            event.hook.sendMessage(message).queue()
                        }
                    }
                }
            }

            "schedule_table" -> {
                event.deferReply(true).queue()
                dataService.findWeekData(getYear(false),getWeekNumber(false))?.run {
                    var message = "The schedule for this week"
                    this.forEach {
                        val unixTime = it.dateTime.toEpochSecond()
                        message += "\n* ${it.day.name}:\n"
                        message += if (it.isGoingToStream) "   Stream, <t:$unixTime:t>" else "   No Stream"
                        message += if (it.comment.isEmpty()) "" else ", ${it.comment}."
                    }
                    event.hook.sendMessage(message).queue()
                } ?: event.hook.sendMessage("Couldn't find this week's schedule in database.")
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
                                    SubcommandData("this_week", "set stream schedule for this week."),
                                    SubcommandData("next_week", "set stream schedule for next week.")
                            ),
                            SubcommandGroupData("history", "interactions history.").addSubcommands(
                                    SubcommandData("show_log", "Show 'schedule set' commands interactions history.")
                            )
                    )//.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL))
                    ,
                    Commands.slash("schedule_table", "Get stream schedule in a text format.")
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
                StringSelectMenu.create("dayInput").setMaxValues(1).setPlaceholder("Select a day to set")
                        .addOptions(weekOptions).build()
        val actionRows = mutableListOf<LayoutComponent>()

        actionRows.add(ActionRow.of(dayInput))
        actionRows.add(ActionRow.of(Button.primary("setBtn", "Set...")))
        actionRows.add(ActionRow.of(Button.primary("notifMsgBtn", "Notification & Message...")))
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
                        "streamTime_data", "24h format in Sweden's Timezone (CET):", TextInputStyle.SHORT
                ).setRequired(false).setPlaceholder("eg. 18:00").setMinLength(5).setMaxLength(5).build()
        ), ActionRow.of(
                TextInput.create("comment", "Comment:", TextInputStyle.SHORT).setMaxLength(19).setRequired(false)
                        .build()
        )
        ).build()
    }

    private fun getSelectedDayDate(): LocalDate {
        Calendar.getInstance(Locale("sv","SE")).let {
            if (!isSettingThisWeek) it.add(Calendar.DAY_OF_WEEK, 7)
            var a = selectedDay.value
            //In java.time.DayOfWeek the value 1 is Monday, but in java.util.Calendar.DayOfWeek it's Sunday.
            //Shift the selectedDay value by +1 .
            a += 1
            if (a == 8) a = 1
            it[Calendar.DAY_OF_WEEK] = a
            return LocalDate.ofYearDay(it[Calendar.YEAR], it[Calendar.DAY_OF_YEAR])
        }
    }
    private fun getYear(isGettingNextWeek: Boolean): Int {
        Calendar.getInstance(Locale("sv","SE")).let {
            if (isGettingNextWeek) it.add(Calendar.DAY_OF_WEEK, 7)
            //The week containing the first Thursday of January is the first week of the year, in ISO system.
            //that's why it's better to get the year(of the week) from a Thursday (value 5 in Calendar.DAY_OF_WEEK)
            it[Calendar.DAY_OF_WEEK] = 5
            return it[Calendar.YEAR]
        }
    }
    private fun getWeekDates(): String {
        var result: String
        Calendar.getInstance(Locale("sv","SE")).let {
            if (!isSettingThisWeek) it.add(Calendar.DAY_OF_WEEK, 7)
            it[Calendar.DAY_OF_WEEK] = it.firstDayOfWeek
            result = it[Calendar.DAY_OF_MONTH].toString()
            it.add(Calendar.DAY_OF_WEEK, 6)
            result += "-" + SimpleDateFormat("dd MMM", Locale.ENGLISH).format(it.time)
        }
        return result
    }

    private fun getWeekNumber(isGettingNextWeek: Boolean): Int {
        Calendar.getInstance(Locale("sv","SE")).let {
            if (isGettingNextWeek) it.add(Calendar.DAY_OF_WEEK, 7)
            return it[Calendar.WEEK_OF_YEAR]
        }
    }



    private fun checkIfLatestMessage(event: GenericComponentInteractionCreateEvent): Boolean {
        if (!isNewSetCommandInteraction) {
            if (event.message.idLong != latestMsgId) {
                event.editMessage("Another 'schedule set' interaction is opened. Therefore this one is canceled.")
                        .setComponents().queue()
                return false
            }
            latestMsgId = event.message.idLong
            return true
        } else {
            latestMsgId = event.message.idLong
            isNewSetCommandInteraction = false
            return true
        }
    }
}
