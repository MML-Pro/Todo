package com.mml.todo.ui.tasks

import androidx.lifecycle.*
import com.mml.todo.data.PreferencesManager
import com.mml.todo.data.SortOrder
import com.mml.todo.data.Task
import com.mml.todo.data.TaskDao
import com.mml.todo.ui.ADD_TASK_RESULT_OK
import com.mml.todo.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle

) : ViewModel() {


    val preferencesFlow = preferencesManager.preferencesFlow

    //    val searchQuery = MutableStateFlow("")
    val searchQuery = state.getLiveData("searchQuery", "")


    private val taskEventChannel = Channel<TaskEvent>()

    val taskEvent = taskEventChannel.receiveAsFlow()

//    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
//
//    val hideComleted = MutableStateFlow(false)

//    private val taskFlow = searchQuery.flatMapLatest {
//        taskDao.getTasks(it)
//    }

    private val taskFlow = combine(
        searchQuery.asFlow(), preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)

    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) {
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOrder)
        }
    }

    fun onHideCompeleted(hideCompeleted: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHideCompelted(hideCompeleted)
        }
    }

    val task = taskFlow.asLiveData()

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChanged(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowDeleteUndoTaskMessage(task))
    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }


    private fun showTaskSavedConfirmatiomMessage(msg: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(msg))
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmatiomMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmatiomMessage("Task updated")
        }
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowDeleteUndoTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TaskEvent()
        object NavigateToDeleteAllCompletedScreen: TaskEvent()
    }


}