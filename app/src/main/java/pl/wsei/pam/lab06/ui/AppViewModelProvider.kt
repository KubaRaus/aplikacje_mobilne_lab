package pl.wsei.pam.lab06.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pl.wsei.pam.lab06.TodoApplication
import pl.wsei.pam.lab06.ui.form.FormViewModel
import pl.wsei.pam.lab06.ui.list.ListViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ListViewModel(
                repository = todoApplication().container.todoTaskRepository
            )
        }
        initializer {
            FormViewModel(
                repository = todoApplication().container.todoTaskRepository,
                currentDateProvider = todoApplication().container.currentDateProvider
            )
        }
    }
}

fun CreationExtras.todoApplication(): TodoApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TodoApplication
}

