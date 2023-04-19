package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var database: RemindersDatabase

    private var reminderDto = ReminderDTO(
        title = "reminderDtoTitle",
        description = "reminderDtoDescription",
        location = "reminderDtoLocation",
        latitude = 47.toDouble(),
        longitude = 122.toDouble(),
    )

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    /**
     * Testing that save reminder and retrieving data is correct and return
     * correct size and retrieve data successfully
     */
    @Test
    fun saveReminder_and_retrieve_reminder_successful() = runTest {
        database.reminderDao().deleteAllReminders()
        database.reminderDao().saveReminder(reminderDto)
        val savedReminders = database.reminderDao().getReminders()

        assertThat(savedReminders.size, `is`(1))
        assertThat(savedReminders[0], notNullValue())
        assertThat(savedReminders, hasItem(reminderDto))
        assertThat(savedReminders[0].id, `is`(reminderDto.id))
    }

    /**
     * Delete all reminders in database and expecting to return empty result
     */
    @Test
    fun deleteAllReminders() = runTest {
        database.reminderDao().saveReminder(reminderDto)
        database.reminderDao().deleteAllReminders()
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.isEmpty(), `is`(true))
    }
}