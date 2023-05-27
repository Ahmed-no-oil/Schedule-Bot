package com.floxd.schedulebot.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.floxd.schedulebot.models.DayInSchedule
import com.floxd.schedulebot.models.BubbleCoordinates
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


class ScheduleImageBuilder(private val weekData: MutableList<DayInSchedule>) {

    private val image: BufferedImage
    private val graphics :Graphics
    private val background : BufferedImage
    private val bubbleBigNo: BufferedImage
    private val bubbleBigYes: BufferedImage
    private val bubbleSmallNo: BufferedImage
    private val bubbleSmallYes : BufferedImage
    init {
        background = getImageRes("/images/bg.png")
        image = BufferedImage(background.width,background.height, BufferedImage.TYPE_INT_ARGB)
        graphics = image.graphics
        bubbleBigNo = getImageRes("/images/no_bubble_frame_big.png")
        bubbleBigYes = getImageRes("/images/yes_bubble_frame_big.png")
        bubbleSmallNo = getImageRes("/images/no_bubble_frame.png")
        bubbleSmallYes = getImageRes("/images/yes_bubble_frame.png")
    }

    fun drawBackground (): ScheduleImageBuilder{
        graphics.drawImage(background,0,0,null)
        return this
    }

    fun drawBubbles(): ScheduleImageBuilder {
        val mapper = ObjectMapper()
        val jsonFile = File(
            this::class.java.getResource("/JSON.documents/bubbles.json")?.file
                ?: throw Exception("couldn't find JSON file")
        )
        //Read points from the JSON file associated with the template
        val bubblesCoordinates: MutableList<BubbleCoordinates> = mapper.readValue(
            jsonFile.readText(),
            mapper.typeFactory.constructCollectionType(
                MutableList::class.java,
                BubbleCoordinates::class.java
            )
        )
        weekData.forEach {
            //add the bubble
            var bubbleImage :BufferedImage
            if(it.isGoingToStream){
                if(it.day.value <= 3){
                    bubbleImage = bubbleBigYes
                }
                else{
                    bubbleImage= bubbleSmallYes
                }
            }else{
                if(it.day.value <= 3){
                    bubbleImage = bubbleBigNo
                }
                else{
                    bubbleImage= bubbleSmallNo
                }
            }
            var bubblePoint = bubblesCoordinates[it.day.value-1]
            graphics.drawImage(bubbleImage, bubblePoint.x, bubblePoint.y, null)
        }
        return this
    }

    fun writeDaysNames() :ScheduleImageBuilder{
    //todo
        return this
    }


    fun writeStreamOrNot() :ScheduleImageBuilder{
    //todo
        return this
    }

    fun writeTimes() :ScheduleImageBuilder{
        //todo
        return this
    }

    fun writeComments() :ScheduleImageBuilder{
        //todo
        return this
    }

    /*
            graphics.color = Color.getColor()
           graphics.font = Font.createFont()
            for (d in 0..6) {
                var streamOrNo = if (week[d].isGoingToStream) "STREAM" else "NO STREAM"
                if (d == 0 && streamOrNo == "STREAM")
                    streamOrNo = "STREAMI"
                graphics.drawString(week[d].timeComment + week[d].comment, bubblesCoordinates[d].x, bubblesCoordinates[d].y)
            }*/

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