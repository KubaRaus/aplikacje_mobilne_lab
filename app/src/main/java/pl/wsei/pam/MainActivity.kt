package pl.wsei.pam

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.wsei.pam.lab06.ui.theme.Lab01Theme
import java.time.LocalDate
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
    route: String
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
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (route != "form") {
                OutlinedButton(onClick = { navController.navigate("list") }) {
                    Text(text = "Zapisz", fontSize = 18.sp)
                }
            } else {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
            }
        }
    )
}

@Composable
fun ListScreen(navController: NavController) {
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
            items(todoTasks()) { item ->
                ListItem(item = item)
            }
        }
    }
}

@Composable
fun FormScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(Priority.Medium) }
    var deadline by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Form",
                showBackIcon = true,
                route = "list"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tytuł") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Priorytet")
            Row {
                Priority.entries.forEach { p ->
                    Row(modifier = Modifier.padding(end = 12.dp)) {
                        RadioButton(
                            selected = priority == p,
                            onClick = { priority = p }
                        )
                        Text(text = p.name, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }

            Row {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { isDone = it }
                )
                Text(text = "Wykonane", modifier = Modifier.padding(top = 12.dp))
            }

            DatePickerField(
                selectedDate = deadline,
                onDateSelected = { deadline = it }
            )

            Button(
                onClick = { navController.navigate("list") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zapisz")
            }
        }
    }
}

@Composable
fun DatePickerField(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    OutlinedButton(
        onClick = {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )
            dialog.show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Deadline: ${selectedDate.format(formatter)}")
    }
}

fun todoTasks(): List<TodoTask> {
    return listOf(
        TodoTask("Programming", LocalDate.of(2024, 4, 18), false, Priority.Low),
        TodoTask("Teaching", LocalDate.of(2024, 5, 12), false, Priority.High),
        TodoTask("Learning", LocalDate.of(2024, 6, 28), true, Priority.Low),
        TodoTask("Cooking", LocalDate.of(2024, 8, 18), false, Priority.Medium)
    )
}

enum class Priority {
    High, Medium, Low
}

data class TodoTask(
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)

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
                    Text("Tytuł")
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
