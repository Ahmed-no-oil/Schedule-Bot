package com.floxd.schedulebot.models

import com.fasterxml.jackson.annotation.JsonProperty

class Coordinates(
    @JsonProperty("X")
    val x: Int,
    @JsonProperty("Y")
    val y: Int ) {
}