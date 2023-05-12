package com.floxd.schedulebot.services

import com.floxd.schedulebot.models.DayInSchedule
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

@Service
class ImageProcessor {
    fun getTemplate() : BufferedImage{
        return ImageIO.read(this::class.java.getResourceAsStream("/image_templates/schedule1.png"))
    }

    fun populateSchedule(scheduleDays: MutableCollection<DayInSchedule>) : InputStream{
        return TODO("Provide the return value")
    }
}