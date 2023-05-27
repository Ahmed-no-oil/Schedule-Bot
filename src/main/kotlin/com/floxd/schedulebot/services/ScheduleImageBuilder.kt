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

    final val DAY_NAME_PADDING_BIG: Coordinates = Coordinates(100, 62)
    final val DAY_NAME_PADDING_SMALL: Coordinates = Coordinates(86, 32)

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
                x = bubblesCoordinates[it.day.value - 1].x + DAY_NAME_PADDING_BIG.x
                y = bubblesCoordinates[it.day.value - 1].y + DAY_NAME_PADDING_BIG.y + fontMetrics.height
            } else {
                x = bubblesCoordinates[it.day.value - 1].x + DAY_NAME_PADDING_SMALL.x
                y = bubblesCoordinates[it.day.value - 1].y + DAY_NAME_PADDING_SMALL.y + fontMetrics.height
            }
            graphics.drawString(dayName, x, y)
        }

        return this
    }


    fun writeStreamOrNot(): ScheduleImageBuilder {
        //todo
        return this
    }

    fun writeTimes(): ScheduleImageBuilder {
        //todo
        return this
    }

    fun writeComments(): ScheduleImageBuilder {
        //todo
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