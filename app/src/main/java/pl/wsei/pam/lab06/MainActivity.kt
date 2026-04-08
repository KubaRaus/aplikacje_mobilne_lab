package pl.wsei.pam.lab06

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import pl.wsei.pam.lab06.data.LocalDateConverter
import pl.wsei.pam.lab06.model.Priority
import pl.wsei.pam.lab06.model.TodoTask
import pl.wsei.pam.lab06.ui.AppViewModelProvider
import pl.wsei.pam.lab06.ui.form.FormViewModel
import pl.wsei.pam.lab06.ui.form.TodoTaskForm
import pl.wsei.pam.lab06.ui.form.TodoTaskUiState
import pl.wsei.pam.lab06.ui.list.ListViewModel
import pl.wsei.pam.lab06.ui.theme.Lab01Theme
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab01Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") { ListScreen(navController) }
        composable("form") { FormScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String,
    onSaveClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate(route) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (route != "form") {
                OutlinedButton(onClick = onSaveClick) {
                    Text(text = "Zapisz", fontSize = 18.sp)
                }
            } else {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { navController.navigate("list") }) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    )
}

@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val listUiState by viewModel.listUiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { navController.navigate("form") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.scale(1.5f)
                )
            }
        },
        topBar = {
            AppTopBar(
                navController = navController,
                title = "List",
                showBackIcon = false,
                route = "form"
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(items = listUiState.items, key = { it.id }) { item ->
                ListItem(item = item)
            }
        }
    }
}

@Composable
fun FormScreen(
    navController: NavController,
    viewModel: FormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Form",
                showBackIcon = true,
                route = "list",
                onSaveClick = {
                    coroutineScope.launch {
                        val saved = viewModel.save()
                        if (saved) {
                            navController.navigate("list")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        TodoTaskInputBody(
            todoUiState = viewModel.todoTaskUiState,
            onItemValueChange = viewModel::updateUiState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun TodoTaskInputBody(
    todoUiState: TodoTaskUiState,
    onItemValueChange: (TodoTaskForm) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TodoTaskInputForm(
            item = todoUiState.todoTask,
            onValueChange = onItemValueChange
        )

        if (!todoUiState.isValid) {
            Text(
                text = "Uzupełnij tytuł i wybierz deadline późniejszy niż dzisiaj.",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTaskInputForm(
    item: TodoTaskForm,
    modifier: Modifier = Modifier,
    onValueChange: (TodoTaskForm) -> Unit = {}
) {
    Text("Tytuł zadania")
    OutlinedTextField(
        value = item.title,
        onValueChange = { onValueChange(item.copy(title = it)) },
        modifier = modifier.fillMaxWidth()
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = item.isDone,
            onCheckedChange = { onValueChange(item.copy(isDone = it)) }
        )
        Text(text = "Wykonane")
    }

    Text("Priorytet")
    Priority.entries.forEach { priority ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onValueChange(item.copy(priority = priority.name)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = item.priority == priority.name,
                onClick = { onValueChange(item.copy(priority = priority.name)) }
            )
            Text(text = priority.name)
        }
    }

    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        yearRange = IntRange(2000, 2030),
        initialSelectedDateMillis = item.deadline
    )
    var showDialog by remember { mutableStateOf(false) }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        text = "Deadline: ${LocalDateConverter.fromMillis(item.deadline).format(DateTimeFormatter.ISO_DATE)}",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    datePickerState.selectedDateMillis?.let {
                        onValueChange(item.copy(deadline = it))
                    }
                }) {
                    Text("Wybierz")
                }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = true)
        }
    }
}

@Composable
fun ListItem(item: TodoTask, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tytul")
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                }
                Column {
                    Text("Deadline")
                    Text(item.deadline.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Priority")
                    Text(item.priority.name)
                }
                Text(if (item.isDone) "Done" else "Not done")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Lab01Theme {
        MainScreen()
    }
}
