package com.ahmed.schedulebot.entities

import jakarta.persistence.*
import java.time.DayOfWeek

@Entity
@Table(name = "days")
class Day(){
    @Id
    var id: Int = 0
    @Enumerated(EnumType.STRING)
    var name: DayOfWeek = DayOfWeek.THURSDAY

    @OneToMany(mappedBy = "day")
    lateinit var scheduleEntries : MutableList<ScheduleEntry>

    constructor(id: Int ,dayOfWeek: DayOfWeek): this(){
        this.id = id
        name = dayOfWeek
    }
}