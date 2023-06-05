package com.ahmed.schedulebot.entities

import jakarta.persistence.*

@Entity
@Table(name = "weeks")
class Week(
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Int=0
    var weekNumber: Int = 0

    @OneToMany(mappedBy = "week")
    lateinit var scheduleEntries : MutableList<ScheduleEntry>

    constructor(number:Int):this(){weekNumber=number}
}