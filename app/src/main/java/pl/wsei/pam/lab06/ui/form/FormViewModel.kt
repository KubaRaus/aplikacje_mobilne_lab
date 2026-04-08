package pl.wsei.pam.lab06.ui.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pl.wsei.pam.lab06.data.CurrentDateProvider
import pl.wsei.pam.lab06.data.LocalDateConverter
import pl.wsei.pam.lab06.data.TodoTaskRepository
import pl.wsei.pam.lab06.model.Priority
import pl.wsei.pam.lab06.model.TodoTask
import java.time.LocalDate

class FormViewModel(
    private val repository: TodoTaskRepository,
    private val currentDateProvider: CurrentDateProvider
) : ViewModel() {

    var todoTaskUiState by mutableStateOf(TodoTaskUiState())
        private set

    suspend fun save(): Boolean {
        if (!validate()) {
            return false
        }
        repository.insertItem(todoTaskUiState.todoTask.toTodoTask())
        return true
    }

    fun updateUiState(todoTaskForm: TodoTaskForm) {
        todoTaskUiState = TodoTaskUiState(
            todoTask = todoTaskForm,
            isValid = validate(todoTaskForm)
        )
    }

    private fun validate(uiState: TodoTaskForm = todoTaskUiState.todoTask): Boolean {
        val pickedDate = LocalDateConverter.fromMillis(uiState.deadline)
        return uiState.title.isNotBlank() && pickedDate.isAfter(currentDateProvider.currentDate)
    }
}

data class TodoTaskUiState(
    val todoTask: TodoTaskForm = TodoTaskForm(),
    val isValid: Boolean = false
)

data class TodoTaskForm(
    val id: Int = 0,
    val title: String = "",
    val deadline: Long = LocalDateConverter.toMillis(LocalDate.now().plusDays(1)),
    val isDone: Boolean = false,
    val priority: String = Priority.Low.name
)

fun TodoTask.toTodoTaskUiState(isValid: Boolean = false): TodoTaskUiState = TodoTaskUiState(
    todoTask = this.toTodoTaskForm(),
    isValid = isValid
)

fun TodoTaskForm.toTodoTask(): TodoTask = TodoTask(
    id = id,
    title = title,
    deadline = LocalDateConverter.fromMillis(deadline),
    isDone = isDone,
    priority = Priority.valueOf(priority)
)

fun TodoTask.toTodoTaskForm(): TodoTaskForm = TodoTaskForm(
    id = id,
    title = title,
    deadline = LocalDateConverter.toMillis(deadline),
    isDone = isDone,
    priority = priority.name
)

