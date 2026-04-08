package pl.wsei.pam.lab06.model

import java.time.LocalDate

data class TodoTask(
    val id: Int = 0,
    val title: String = "",
    val deadline: LocalDate = LocalDate.now(),
    val isDone: Boolean = false,
    val priority: Priority = Priority.Low
)

