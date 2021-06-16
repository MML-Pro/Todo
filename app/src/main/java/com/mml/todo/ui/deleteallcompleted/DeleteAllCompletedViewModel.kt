package com.mml.todo.ui.deleteallcompleted

import androidx.lifecycle.ViewModel
import com.mml.todo.data.TaskDao
import com.mml.todo.di.AppModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @AppModule.ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {
    fun onConfirmClick()= applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}