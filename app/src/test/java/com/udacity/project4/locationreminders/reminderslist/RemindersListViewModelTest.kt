package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.coroutines.ContinuationInterceptor

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private var reminderDto = ReminderDTO(
        title = "reminderDtoTitle",
        description = "reminderDtoDescription",
        location = "reminderDtoLocation",
        latitude = 47.toDouble(),
        longitude = 122.toDouble(),
    )

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        dataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun close() {
        stopKoin()
    }


    /**
     * Testing that no data loaded
     */
    @Test
    fun no_data_found_test() = runBlockingTest {
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    /**
     * testing app shows loading when getting data
     * testing that loading disappear and data returned
     * and the list contains value added
     */
    @Test
    fun load_data_success() = mainCoroutineRule.runBlockingTest {
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).pauseDispatcher()
        dataSource.saveReminder(reminderDto)

        remindersListViewModel.loadReminders()
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )
        (mainCoroutineRule.coroutineContext[ContinuationInterceptor]!! as DelayController).resumeDispatcher()

        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
        val remindersList = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.size, `is`(1))
        assertThat(remindersList[0].id, `is`(reminderDto.id))
    }


    /**
     * testing that list doesn't contain data
     */
    @Test
    fun empty_list() = mainCoroutineRule.runBlockingTest {
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
    }

}