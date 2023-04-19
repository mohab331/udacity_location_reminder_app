package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var datasource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private var reminderDto = ReminderDataItem(
        title = "reminderDataItemTitle",
        description = "reminderDataItemDescription",
        location = "reminderDataItemLocation",
        latitude = 47.toDouble(),
        longitude = 122.toDouble(),
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        datasource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), datasource)
    }

    @After
    fun close() {
        stopKoin()
    }

    @Test
    fun validateEnteredData_false() = runBlockingTest {
        val invalidReminderDataItem = ReminderDataItem(
            null,
            null,
            null,
            null,
            null
        )
        val result = saveReminderViewModel.validateEnteredData(invalidReminderDataItem)
        assertFalse(result)
        assertThat(saveReminderViewModel.showSnackBarInt.value, notNullValue())

    }

    @Test
    fun validateEnteredData_true() {

        val result = saveReminderViewModel.validateEnteredData(reminderDto)
        assertTrue(result)
    }

    @Test
    fun save_valid_data_success() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminderDto)
        assertThat(saveReminderViewModel.showLoading.value, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.value, `is`(false))
    }
}