package com.ahmed.schedulebot.services

import com.ahmed.schedulebot.entities.ScheduleEntry
import com.ahmed.schedulebot.models.Coordinates
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.awt.FontFormatException
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.format.TextStyle
import java.util.*
import javax.imageio.ImageIO

@Component
class ScheduleImageBuilder() {
    private lateinit var weekData: MutableList<ScheduleEntry>
    private lateinit var image: BufferedImage
    private lateinit var graphics: Graphics2D
    private lateinit var background: BufferedImage
    private lateinit var bubbleBigNo: BufferedImage
    private lateinit var bubbleBigYes: BufferedImage
    private lateinit var bubbleSmallNo: BufferedImage
    private lateinit var bubbleSmallYes: BufferedImage
    private lateinit var fontKiwiDays: Font
    private lateinit var fontRobotoSlab: Font
    private lateinit var bubblesCoordinates: Array<Coordinates>


    private val PADDING_BIG_BUBBLE: Coordinates = Coordinates(100, 62)
    private val PADDING_SMALL_BUBBLE: Coordinates = Coordinates(86, 32)
    private val CENTER_BIG_BUBBLE: Coordinates = Coordinates(269, 222)
    private val CENTER_SMALL_BUBBLE: Coordinates = Coordinates(220, 182)
    private val WEEK_DATES_COORDS: Coordinates = Coordinates(1570, 350)
    private val FOOTNOTE_COORDS: Coordinates = Coordinates(0, 1280)

    fun create(data: MutableList<ScheduleEntry>): ScheduleImageBuilder {
        weekData = data
        background = getImageRes("/images/bg.png")
        bubbleBigNo = getImageRes("/images/no_bubble_frame_big.png")
        bubbleBigYes = getImageRes("/images/yes_bubble_frame_big.png")
        bubbleSmallNo = getImageRes("/images/no_bubble_frame.png")
        bubbleSmallYes = getImageRes("/images/yes_bubble_frame.png")

        image = BufferedImage(background.width, background.height, BufferedImage.TYPE_INT_ARGB)
        graphics = image.createGraphics()
        // add fonts
        try {
            fontKiwiDays = Font.createFont(
                    Font.TRUETYPE_FONT, (
                    this::class.java.getResourceAsStream("/fonts/Kiwi_Days.ttf")
                    )
            )
            fontRobotoSlab = Font.createFont(
                    Font.TRUETYPE_FONT, (
                    this::class.java.getResourceAsStream("/fonts/RobotoSlab-Light.ttf")
                            ?: throw IOException("couldn't find font file")
                    )
            )
        } catch (e: IOException) {
            println(e)
        } catch (e: FontFormatException) {
            println(e)
        }

        bubblesCoordinates = arrayOf(
                Coordinates(147, 454),
                Coordinates(723, 454),
                Coordinates(1293, 454),
                Coordinates(52, 898),
                Coordinates(530, 898),
                Coordinates(1014, 898),
                Coordinates(1488, 898)
        )
        return this
    }

    fun drawBackground(): ScheduleImageBuilder {
        graphics.drawImage(background, 0, 0, null)
        return this
    }

    fun drawBubbles(): ScheduleImageBuilder {
        var bubbleImage: BufferedImage
        var bubblePoint: Coordinates
        weekData.forEach {
            //add the bubble
            bubbleImage = if (it.isGoingToStream) {
                if (it.day.name.value <= 3) {
                    bubbleBigYes
                } else {
                    bubbleSmallYes
                }
            } else {
                if (it.day.name.value <= 3) {
                    bubbleBigNo
                } else {
                    bubbleSmallNo
                }
            }
            bubblePoint = bubblesCoordinates[it.day.name.value - 1]
            graphics.drawImage(bubbleImage, bubblePoint.x, bubblePoint.y, null)
        }
        return this
    }

    fun writeDaysNames(): ScheduleImageBuilder {
        graphics.color = Color.decode("#e00266")
        var dayName: String
        graphics.font = fontKiwiDays.deriveFont(52f)
        val fontMetrics = graphics.fontMetrics
        var x: Int
        var y: Int
        weekData.forEach {
            dayName = it.day.name.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            if (it.day.name.value <= 3) {
                x = bubblesCoordinates[it.day.name.value - 1].x + PADDING_BIG_BUBBLE.x
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height
            } else {
                x = bubblesCoordinates[it.day.name.value - 1].x + PADDING_SMALL_BUBBLE.x
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height
            }
            graphics.drawString(dayName, x, y)
        }
        return this
    }

    fun writeStreamOrNot(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var streamOrNot: String
        graphics.font = fontKiwiDays.deriveFont(Font.BOLD, 60f)
        val fontMetrics = graphics.fontMetrics
        var x: Int
        var y: Int
        weekData.forEach {
            streamOrNot = if (it.isGoingToStream) "STREAM" else "NO STREAM"
            if (it.day.name.value == 1 && it.isGoingToStream) streamOrNot = "STREAMI"
            if (it.day.name.value <= 3) {
                //adjust center
                x = bubblesCoordinates[it.day.name.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(
                        streamOrNot
                ) / 2
                //write on the second line
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height * 2
            } else {
                x = bubblesCoordinates[it.day.name.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(
                        streamOrNot
                ) / 2
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height * 2 - 20
            }
            graphics.drawString(streamOrNot, x, y)
        }
        return this
    }

    fun writeTimes(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var streamTime: String
        graphics.font = fontKiwiDays.deriveFont(Font.BOLD, 60f)
        val fontMetrics = graphics.fontMetrics
        var x: Int
        var y: Int
        weekData.forEach {
            streamTime = it.timeComment
            if (it.day.name.value <= 3) {
                //adjust center
                x = bubblesCoordinates[it.day.name.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(
                        streamTime
                ) / 2
                //write on the third line
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_BIG_BUBBLE.y + fontMetrics.height * 3
            } else {
                x = bubblesCoordinates[it.day.name.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(
                        streamTime
                ) / 2
                y = bubblesCoordinates[it.day.name.value - 1].y + PADDING_SMALL_BUBBLE.y + fontMetrics.height * 3 - 20
            }
            graphics.drawString(streamTime, x, y)
        }
        return this
    }

    fun writeComments(): ScheduleImageBuilder {
        graphics.color = Color.decode("#5176a6")
        var comment: String
        graphics.font = fontKiwiDays.deriveFont(Font.PLAIN, 36f)
        val fontMetrics = graphics.fontMetrics
        var x: Int
        var y: Int
        weekData.forEach {
            comment = it.comment
            if (it.day.name.value <= 3) {
                //adjust center
                x =
                        bubblesCoordinates[it.day.name.value - 1].x + CENTER_BIG_BUBBLE.x - fontMetrics.stringWidth(comment) / 2
                //go up 2 lines from the bottom
                y = bubblesCoordinates[it.day.name.value - 1].y + CENTER_BIG_BUBBLE.y * 2 - fontMetrics.height * 2
            } else {
                x = bubblesCoordinates[it.day.name.value - 1].x + CENTER_SMALL_BUBBLE.x - fontMetrics.stringWidth(
                        comment
                ) / 2
                //go up 3 lines from the bottom
                y = bubblesCoordinates[it.day.name.value - 1].y + CENTER_BIG_BUBBLE.y * 2 - fontMetrics.height * 3
            }
            graphics.drawString(comment, x, y)
        }
        return this
    }

    fun drawWeekDates(weekDates: String): ScheduleImageBuilder {
        val rotation = AffineTransform()
        rotation.rotate(Math.toRadians(18.0), WEEK_DATES_COORDS.x.toDouble(), WEEK_DATES_COORDS.y.toDouble())
        graphics.font = fontKiwiDays.deriveFont(Font.BOLD, 60f)
        graphics.color = Color.decode("#eca7c5")
        graphics.transform(rotation)
        graphics.drawString(weekDates, WEEK_DATES_COORDS.x, WEEK_DATES_COORDS.y)
        //reset graphics
        graphics.dispose()
        graphics = image.createGraphics()
        return this
    }

    fun drawXmasHat(): ScheduleImageBuilder {
        //todo draw xmas hat
        return this
    }

    fun writeFootnote(): ScheduleImageBuilder {
        val comment = "THIS IS JUST AN ESTIMATE.\n" +
                "THINGS CAN CHANGE, SO KEEP AN EYE ON NEWS CHANNEL.\n" +
                "To see schedule in your time zone try /schedule_table"
        graphics.font = fontRobotoSlab.deriveFont(Font.BOLD, 38f)
        graphics.color = Color.decode("#eca7c5")
        var x = FOOTNOTE_COORDS.x
        var y = FOOTNOTE_COORDS.y
        for (line in comment.split("\n")) {
            y += graphics.fontMetrics.height - 4
            //adjust center
            x = (image.width - graphics.fontMetrics.stringWidth(line)) / 2
            graphics.drawString(line, x, y)
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
