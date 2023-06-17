package com.ahmed.schedulebot.entities

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "schedule_entries")
class ScheduleEntry() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0
    var timeComment: String = ""
    var dateTime: OffsetDateTime = OffsetDateTime.MIN
    var isGoingToStream: Boolean = false
    var comment: String = ""

    @ManyToOne
    @JoinColumn(name = "day_id")
    lateinit var day: Day

    @ManyToOne
    @JoinColumn(name = "week_id")
    lateinit var week: Week

    override fun toString(): String {
        return "${day.name.name}: " + (if (isGoingToStream) "STREAM, " else "NO STREAM, ") + "$timeComment, $comment"
    }
}
