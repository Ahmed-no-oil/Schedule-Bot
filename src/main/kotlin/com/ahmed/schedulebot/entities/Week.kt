package com.ahmed.schedulebot.entities

import jakarta.persistence.*

@Entity
@Table(name = "weeks")
class Week(
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Int = 0
    var weekNumber: Int = 0
    var year: Int = 1970

    @OneToMany(mappedBy = "week")
    lateinit var scheduleEntries: MutableList<ScheduleEntry>

    constructor(year: Int, weekNumber: Int) : this() {
        this.year = year
        this.weekNumber = weekNumber
    }
}
