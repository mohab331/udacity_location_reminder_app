package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
     val retrievedReminder =  reminders.find { it.id == id }
        if(retrievedReminder != null){
          return  Result.Success(retrievedReminder)
        }
        return Result.Error("Reminder not found !")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}