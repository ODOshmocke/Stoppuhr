package com.example.stoppwatch

import android.app.ActivityManager.AppTask
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stoppwatch.ui.theme.StoppwatchTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.Map



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StoppwatchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TabBar()

                }
            }
        }
    }
}


data class TabItem(
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)


fun allTabs() = listOf(
    TabItem(
        title = "Home",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    ),
    TabItem(
        title = "Search",
        unselectedIcon = Icons.Outlined.Search,
        selectedIcon = Icons.Filled.Search
    ),
    TabItem(
        title = "Profile",
        unselectedIcon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabBar(tabItems: List<TabItem> = allTabs()) {

    var selectedTabIndex = remember { mutableIntStateOf(0) }
    var pagerState = rememberPagerState {
        tabItems.size
    }

    LaunchedEffect(selectedTabIndex.intValue) {
        println(selectedTabIndex.intValue)
        pagerState.animateScrollToPage(selectedTabIndex.intValue)
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        selectedTabIndex.intValue = pagerState.currentPage
    }

    Column(
        modifier = Modifier.fillMaxSize(),

        ) {
        TabRow(selectedTabIndex = selectedTabIndex.intValue) {
            tabItems.forEachIndexed { index, tabItem ->
                Tab(selected = index == selectedTabIndex.intValue,
                    onClick = {
                        selectedTabIndex.intValue = index
                    },
                    text = {
                        Text(text = tabItem.title)
                    },
                    icon = {
                        if (index == selectedTabIndex.intValue) {
                            Icon(tabItem.selectedIcon, contentDescription = null)
                        } else {
                            Icon(tabItem.unselectedIcon, contentDescription = null)

                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {index ->
            when (index) {

                0 -> {
                    Stopwatch()
                }
                1 -> {
                    show()
                }
                2 -> {
                    Text("Profile")
                }
            }
        }

    }
}



class StopwatchState {
    var isRunning by mutableStateOf(false)
    var elapsedSeconds by mutableIntStateOf(0)
    private var job: Job? = null


    fun startStopwatch() {
        if (!isRunning) {
            job = CoroutineScope(Dispatchers.Default).launch {
                while (isRunning) {
                    delay(1000)
                    elapsedSeconds++
                }
            }
        } else {
            job?.cancel()
        }
        isRunning = !isRunning
    }

    fun resetStopwatch() {
        isRunning = false
        elapsedSeconds = 0
        job?.cancel()
    }
}
val stopwatchState = StopwatchState()


class TaskSaverAndReader(context: Context) {


    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("task_saver_Map", Context.MODE_PRIVATE)

    fun saveMap(map: Map<String, MutableMap<String, Int>>) {
        val jsonString = Gson().toJson(map)
        sharedPreferences.edit().putString("my_map", jsonString).apply()
        println("This is the json $jsonString")
    }

    fun loadMap(): Map<String, MutableMap<String, Int>>? {
        val jsonString = sharedPreferences.getString("my_map", null)
        println("This is the json loaded $jsonString")
        return if (jsonString != null) {
            Gson().fromJson(jsonString, object : TypeToken<Map<String, MutableMap<String, Int>>>() {}.type)
        } else {
            null
        }
    }

    fun deleteMap() {
        sharedPreferences.edit().remove("my_map").apply()
    }

}


@Composable
fun Stopwatch() {
    var currentTask by remember { mutableStateOf("Click to Select Task") }
    var showTaskList by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatTime(stopwatchState.elapsedSeconds),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { stopwatchState.startStopwatch()},
                modifier = Modifier
                    .padding(8.dp)
                    .width(100.dp)
                    .height(42.dp)
            ) {
                Text(if (stopwatchState.isRunning) "Stop" else "Start")
            }
            Button(
                onClick = {
                    stopwatchState.resetStopwatch()
                },
                modifier = Modifier
                    .padding(8.dp)
                    .width(100.dp)
                    .height(42.dp)
            ) {
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Button(onClick = {
            showTaskList = true
        }
        ) {
            Text(currentTask)

        }


    }
    if (showTaskList) {
        TaskList(onClose = { showTaskList = false }, onSelectedTask = { currentTask = it })
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskList(onClose: () -> Unit, onSelectedTask: (String) -> Unit = {}) {
    var openAddWindow by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val taskList = (TaskSaverAndReader(context = context).loadMap() ?: mutableMapOf<String, MutableMap<String, Int>>()).toMutableMap()


    AlertDialog(

        onDismissRequest = onClose,
        modifier = Modifier
            .height(300.dp)
            .width(250.dp)
            //blur background
            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
    )
    {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            items(taskList.size) {
                Button(
                    onClick = {
                        onSelectedTask(taskList.keys.toMutableList()[it])
                        onClose()
                    },)
                    {
                        Text(
                            text = taskList.keys.toMutableList()[it], Modifier.padding(10.dp),
                        )

                    }
                Divider()
            }
            item {
                Button(
                    onClick = {
                        openAddWindow = true
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .width(100.dp)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Add Task"
                    )
                }
            }
        }

    }
        if (openAddWindow) {
        AddTaskWindow(
            onClose = { openAddWindow = false },
            onTaskAdded = {
                run {

                    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString()

                    taskList.putIfAbsent(it, mutableMapOf(currentDate to 0))
                    println("$taskList This is the map before the save")
                    TaskSaverAndReader(context = context).deleteMap()
                    TaskSaverAndReader(context = context).saveMap(taskList)
                }
            }
            )
    }
}

@Composable
fun AddTaskWindow(onClose: () -> Unit, onTaskAdded:(String) -> Unit) {
    var taskName by remember { mutableStateOf(TextFieldValue()) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Add Task") },
        text = {
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Task Name") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    println("${taskName.text} This is the task name")
                    onTaskAdded(taskName.text)
                    onClose()
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}


private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StoppwatchTheme {
        //TaskList(taskList = mutableListOf("Task1", "Task2", "TaskArsch"), onClose = {})

    }
}





