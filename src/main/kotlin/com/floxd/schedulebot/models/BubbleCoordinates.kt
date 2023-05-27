package com.floxd.schedulebot.models

import com.fasterxml.jackson.annotation.JsonProperty

class BubbleCoordinates(
    @JsonProperty("X")
    val x: Int,
    @JsonProperty("Y")
    val y: Int ) {
}