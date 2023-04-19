package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repo: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private var reminderDto = ReminderDTO(
        title = "reminderDataItemTitle",
        description = "reminderDataItemDescription",
        location = "reminderDataItemLocation",
        latitude = 47.toDouble(),
        longitude = 122.toDouble(),
    )


    @Before
    fun setUp() {
        stopKoin()
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                val dataSource: ReminderDataSource = RemindersLocalRepository(get())
                dataSource // required cast
            }
            single { LocalDB.createRemindersDao(appContext) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        repo = GlobalContext.get().koin.get()

        runBlocking {
            repo.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister()
    }


    /**
     * Test navigation from
     */
    @Test
    fun test_nav_of_fragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<*>)
        val mockNavController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mockNavController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mockNavController).navigate(ReminderListFragmentDirections.toSaveReminder())
        scenario.onFragment { Navigation.setViewNavController(it.view!!, mockNavController) }
    }


    /**
     * Test the displayed data on the UI
     */
    @Test
    fun test_displayed_data() = runTest {
        repo.saveReminder(reminderDto)
        val fragmentScenario = FragmentScenario.launchInContainer(
            ReminderListFragment::class.java,
            themeResId = R.style.AppTheme
        )
        dataBindingIdlingResource.monitorFragment(fragmentScenario)


        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withText(reminderDto.title)).check(matches(isDisplayed()))
        onView(withText(reminderDto.description)).check(matches(isDisplayed()))

        fragmentScenario.close()
    }


    /**
     *  Add testing for the error messages.
     */
    @Test
    fun check_to_error_messages() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario as FragmentScenario<*>)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}