package com.floxd.schedulebot.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.floxd.schedulebot.models.Coordinates
import com.floxd.schedulebot.models.DayInSchedule
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.awt.FontFormatException
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.*
import java.time.format.TextStyle
import java.util.*
import javax.imageio.ImageIO

@Component
class ScheduleImageBuilder() {
    private lateinit var weekData: MutableList<DayInSchedule>
    private lateinit var image: BufferedImage
    private lateinit var graphics: Graphics
    private lateinit var background: BufferedImage
    private lateinit var bubbleBigNo: BufferedImage
    private lateinit var bubbleBigYes: BufferedImage
    private lateinit var bubbleSmallNo: BufferedImage
    private lateinit var bubbleSmallYes: BufferedImage
    private lateinit var fontKiwiDays: Font
    private lateinit var bubblesCoordinates: MutableList<Coordinates>

    final val PADDING_BIG_BUBBLE: Coordinates = Coordinates(100, 62)
    final val PADDING_SMALL_BUBBLE: Coordinates = Coordinates(86, 32)
    final val CENTER_BIG_BUBBLE: Coordinates = Coordinates(269,222)
    final val CENTER_SMALL_BUBBLE: Coordinates = Coordinates(220,182)

    fun create(data: MutableList<DayInSchedule>): ScheduleImageBuilder {
        weekData = data
        background = getImageRes("/images/bg.png")
        bubbleBigNo = getImageRes("/images/no_bubble_frame_big.png")
        bubbleBigYes = getImageRes("/images/yes_bubble_frame_big.png")
        bubbleSmallNo = getImageRes("/images/no_bubble_frame.png")
        bubbleSmallYes = getImageRes("/images/yes_bubble_frame.png")

        image = BufferedImage(background.width, background.height, BufferedImage.TYPE_INT_ARGB)
        graphics = image.graphics
        // add font
        try {
            fontKiwiDays = Font.createFont(
                Font.TRUETYPE_FONT, File(
                    this::class.java.getResource("/fonts/Kiwi_Days.ttf")?.file
                        ?: throw IOException("couldn't find font file")
                )
            )
        } catch (e: IOException) {
            println(e)
        } catch (e: FontFormatException) {
            println(e)
        }

        val mapper = ObjectMapper()
        val jsonFile = File(
            this::class.java.getResource("/JSON.documents/bubbles.json")?.file
                ?: throw Exception("couldn't find JSON file")
        )
        //Read points from the JSON file associated with the template
        bubblesCoordinates = mapper.readValue(
            jsonFile.readText(),
            mapper.typeFactory.constructCollectionType(
                MutableList::class.java,
                Coordinates::class.java
            )
        )
        return this
    }

    fun drawBackground(): ScheduleImageBuilder {
        graphics.drawImage(background, 0, 0, null)
        return this
    }

    fun drawBubbles(): ScheduleImageBuilder {
        weekData.forEach {
            //add the bubble
            var bubbleImage: BufferedImage
            if (it.isGoingToStream) {
                if (it.day.value <= 3) {
                    bubbleImage = bubbleBigYes
                } else {
                    bubbleImage = bubbleSmallYes
                }
            } else {
                if (it.day.value <= 3) {
                    bubbleImage = bubbleBigNo
                } else {
                    bubbleImage = bubbleSmallNo
                }
            }
            var bubblePoint = bubblesCoordinates[it.day.value - 1]
            graphics.drawImage(bubbleImage, bubblePoint.x, bubblePoint.y, null)
        }
        return this
    }

    fun writeDaysNames(): ScheduleImageBuilder {
        graphics.color = Color.decode("#e00266")
        var dayName: String
        graphics.font = fontKiwiDays.deriveFont(52f)
        val fontMetrics = graphics.fontMetrics
        weekData.forEach {
            dayName = it.day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            var x = 0
            var y = 0
            if (it.day.value <= 3) {
                x = bubblesCoordinates[it.day.value - 1].x + PADDING_BIG_BUBBLE.x
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height
            } else {
                x = bubblesCoordinates[it.day.value - 1].x + PADDING_SMALL_BUBBLE.x
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height
            }
            graphics.drawString(dayName, x, y)
        }
        return this
    }

    fun writeStreamOrNot(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var streamOrNot: String
        graphics.font = fontKiwiDays.deriveFont(60f)
        val fontMetrics = graphics.fontMetrics
        weekData.forEach {
            streamOrNot = if(it.isGoingToStream) "STREAM" else "NO STREAM"
            if(it.day.value ==1 && it.isGoingToStream) streamOrNot = "STREAMI"
            var x = 0
            var y = 0
            if (it.day.value <= 3) {
                //adjust center
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(streamOrNot) / 2
                //write on the second line
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height * 2
            } else {
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(streamOrNot) / 2
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height * 2 -20
            }
            graphics.drawString(streamOrNot, x, y)
        }
        return this
    }

    fun writeTimes(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var streamTime: String
        graphics.font = fontKiwiDays.deriveFont(60f)
        val fontMetrics = graphics.fontMetrics
        graphics.font = fontKiwiDays.deriveFont(60f)
        weekData.forEach {
            streamTime = it.timeComment
            var x = 0
            var y = 0
            if (it.day.value <= 3) {
                //adjust center
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(streamTime) / 2
                //write on the third line
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height * 3
            } else {
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(streamTime) / 2
                y = bubblesCoordinates[it.day.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height * 3 -20
            }
            graphics.drawString(streamTime, x, y)
        }
        return this
    }

    fun writeComments(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var comment: String
        graphics.font = fontKiwiDays.deriveFont(36f)
        val fontMetrics = graphics.fontMetrics
        weekData.forEach {
            comment = it.comment
            var x = 0
            var y = 0
            if (it.day.value <= 3) {
                //adjust center
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(comment) / 2
                //go up 2 lines from the bottom
                y = bubblesCoordinates[it.day.value - 1].y + CENTER_BIG_BUBBLE.y * 2 - fontMetrics.height * 2
            } else {
                x = bubblesCoordinates[it.day.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(comment) / 2
                //go up 3 lines from the bottom
                y = bubblesCoordinates[it.day.value - 1].y + CENTER_BIG_BUBBLE.y * 2 - fontMetrics.height * 3
            }
            graphics.drawString(comment, x, y)
        }
        return this
    }

    fun build(): InputStream {
        val output = ByteArrayOutputStream()
        ImageIO.write(image, "png", output)
        graphics.dispose()
        return ByteArrayInputStream(output.toByteArray())
    }

    private fun getImageRes(name: String): BufferedImage {
        return ImageIO.read(this::class.java.getResourceAsStream(name))
            ?: throw Exception("couldn't find image: $name")
    }
}