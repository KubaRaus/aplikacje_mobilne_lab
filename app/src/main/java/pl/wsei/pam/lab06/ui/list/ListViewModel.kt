package pl.wsei.pam.lab06.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.wsei.pam.lab06.alarm.TaskAlarmScheduler
import pl.wsei.pam.lab06.data.TodoTaskRepository
import pl.wsei.pam.lab06.model.TodoTask

class ListViewModel(
    private val repository: TodoTaskRepository,
    private val taskAlarmScheduler: TaskAlarmScheduler
) : ViewModel() {
    val listUiState: StateFlow<ListUiState> = repository.getAllAsStream()
        .map { ListUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ListUiState()
        )

    fun toggleDone(item: TodoTask, isDone: Boolean) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isDone = isDone))
            taskAlarmScheduler.rescheduleNearestTaskAlarm()
        }
    }
    fun deleteTask(item: TodoTask) {
        viewModelScope.launch {
            repository.deleteItem(item)
            taskAlarmScheduler.rescheduleNearestTaskAlarm()
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ListUiState(val items: List<TodoTask> = emptyList())
