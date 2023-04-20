package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)

//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var localRepository: RemindersLocalRepository
    private var reminderDto = ReminderDTO(
        title = "reminderDtoTitle",
        description = "reminderDtoDescription",
        location = "reminderDtoLocation",
        latitude = 47.toDouble(),
        longitude = 122.toDouble(),
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    /**
     * Initialize database and reminderLocalRepo
     * and using an in-memory database so that the information stored disappears when the
    process is killed.
     */

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        localRepository = RemindersLocalRepository(
            database.reminderDao(), Dispatchers.Main
        )
    }

    /**
     * Close database
     */
    @After
    fun close() {
        database.close()
    }

    @Test
    fun saveReminders_getReminders() = runBlocking {
        localRepository.saveReminder(reminderDto)
        val reminderResult =
            localRepository.getReminder(reminderDto.id) as Result.Success<ReminderDTO>
        MatcherAssert.assertThat(reminderResult.data.id, `is`(reminderDto.id))
    }

    @Test
    fun saveTask_retrievesFail() = runBlocking {
        localRepository.saveReminder(reminderDto)
        val result = localRepository.getReminder("-1") as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }


    @Test
    fun deleteAllReminders() = runBlocking {

        localRepository.saveReminder(reminderDto)
        val reminderList = localRepository.getReminders() as Result.Success<List<ReminderDTO>>

        MatcherAssert.assertThat(reminderList.data.size, `is`(1))
        MatcherAssert.assertThat(reminderList.data, hasItem(reminderDto))

        localRepository.deleteAllReminders()

        val reminders = localRepository.getReminders() as Result.Success<List<ReminderDTO>>
        MatcherAssert.assertThat(reminders.data.size, `is`(0))
    }

}