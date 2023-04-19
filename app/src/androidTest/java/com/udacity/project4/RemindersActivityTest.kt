//package com.udacity.project4
//
//import android.app.Application
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.IdlingRegistry
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.action.ViewActions.longClick
//import androidx.test.espresso.action.ViewActions.replaceText
//import androidx.test.espresso.matcher.RootMatchers.withDecorView
//import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
//import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
//import androidx.test.espresso.matcher.ViewMatchers.withId
//import androidx.test.espresso.matcher.ViewMatchers.withText
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import com.udacity.project4.authentication.AuthenticationActivity
//import com.udacity.project4.locationreminders.RemindersActivity
//import com.udacity.project4.locationreminders.data.ReminderDataSource
//import com.udacity.project4.locationreminders.data.dto.ReminderDTO
//import com.udacity.project4.locationreminders.data.local.LocalDB
//import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
//import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
//import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
//import com.udacity.project4.util.DataBindingIdlingResource
//import com.udacity.project4.util.EspressoIdlingResource
//import com.udacity.project4.util.monitorActivity
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.runBlocking
//import org.hamcrest.CoreMatchers.`is`
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.core.context.startKoin
//import org.koin.core.context.stopKoin
//import org.koin.dsl.module
//import org.koin.test.AutoCloseKoinTest
//import org.koin.test.get
//import java.util.function.Predicate.not
//import java.util.regex.Pattern.matches
//
//@RunWith(AndroidJUnit4::class)
//@LargeTest
////END TO END test to black box test the app
//class RemindersActivityTest :
//    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test
//
//    private lateinit var repository: ReminderDataSource
//    private lateinit var appContext: Application
//    private val dataBindingIdlingResource = DataBindingIdlingResource()
//    private  var reminderDto = ReminderDTO(
//        title = "reminderDtoTitle",
//        description = "reminderDtoDescription",
//        location = "reminderDtoLocation",
//        latitude = 47.toDouble(),
//        longitude = 122.toDouble(),
//    )
//
//    /**
//     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
//     * at this step we will initialize Koin related code to be able to use it in out testing.
//     **/
//
//    @Before
//    fun init() {
//        stopKoin()//stop the original app koin
//        appContext = getApplicationContext()
//        val myModule = module {
//            viewModel {
//                RemindersListViewModel(
//                    appContext,
//                    get() as ReminderDataSource
//                )
//            }
//            single {
//                SaveReminderViewModel(
//                    appContext,
//                    get() as ReminderDataSource
//                )
//            }
//            single { RemindersLocalRepository(get()) as ReminderDataSource }
//            single { LocalDB.createRemindersDao(appContext) }
//        }
//        //declare a new koin module
//        startKoin {
//            modules(listOf(myModule))
//        }
//        //Get our real repository
//        repository = get()
//
//        //clear the data to start fresh
//        runBlocking {
//            repository.deleteAllReminders()
//        }
//    }
//
//    @Before
//    fun registerIdlingResource() {
//        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
//    }
//
//    @After
//    fun unregisterIdlingResource() {
//        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
//    }
//
//
//    @Test
//    fun snackbar_notLocation_e2eTesting() = runBlocking {
//
//        // Start up Tasks screen.
//        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
//        dataBindingIdlingResource.monitorActivity(activityScenario)
//
//        // Espresso code will go here.
//        onView(withId(R.id.addReminderFAB)).perform(click())
//
//        // enter title and description
//        onView(withId(R.id.reminderTitle)).perform(replaceText("one"))
//        onView(withId(R.id.reminderDescription)).perform(replaceText("first"))
//
//        // save reminder.
//        onView(withId(R.id.saveReminder)).perform(click())
//
//        // Check whether snackbar not title error is shown
//        onView(withId(com.google.android.material.R.id.snackbar_text))
//            .check(matches(withText(R.string.err_select_location)))
//
//        activityScenario.close()
//    }
//
//
//    @Test
//    fun showRemindersScreenWithAReminder() {
//        runBlocking {
//            repository.saveReminder(reminderDTO)
//        }
//
//        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
//        dataBindingIdlingResource.monitorActivity(activityScenario)
//
//        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
//        onView(withText("Cardiff Castle")).check(matches(isDisplayed()))
//        onView(withId(R.id.tv_description)).check(matches(isDisplayed()))
//        onView(withText("Oldest castle in Wales.")).check(matches(isDisplayed()))
//        onView(withId(R.id.tv_location)).check(matches(isDisplayed()))
//
//        activityScenario.close()
//    }
//
//
//    @Test
//    fun testAddAReminder_noLocationSelected_snackbarWithErrorMessageAppears(): Unit = runBlocking {
//        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
//        dataBindingIdlingResource.monitorActivity(activityScenario)
//
//        onView(withId(R.id.addReminderFAB)).perform(click())
//        onView(withId(R.id.reminderTitle)).perform(replaceText("title2"))
//        onView(withId(R.id.reminderDescription)).perform(replaceText("description2"))
//        onView(withId(R.id.saveReminder)).perform(click())
//
//        val snackbarMessage = appContext.getString(R.string.select_location)
//        onView(withText(snackbarMessage))
//            .check(matches(isDisplayed()))
//        activityScenario.close()
//    }
//
//    @Test
//    fun testClickingOnListItem_opensRemindersDescriptionActivity(): Unit = runBlocking {
//        val reminder1 = ReminderDTO(
//            "title1", "description1", "location1",
//            11.111, 11.112
//        )
//        repository.saveReminder(reminder1)
//
//        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
//        dataBindingIdlingResource.monitorActivity(activityScenario)
//
//        onView(
//            withId(R.id.reminderssRecyclerView)
//        )
//            .check(matches(hasDescendant(withText("title1"))))
//            .perform(click())
//
//        onView(withId(R.id.reminderssRecyclerView)).perform(
//            RecyclerViewChildActions.Companion.actionOnChild(
//                click(), R.id.reminderCardView
//            )
//        )
//
//        onView(withId(R.id.reminder_title))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.reminder_description))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.reminder_location))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.reminder_latitude))
//            .check(matches(isDisplayed()))
//        onView(withId(R.id.reminder_longitude))
//            .check(matches(isDisplayed()))
//
//        activityScenario.close()
//    }
//
//    @Test
//    fun testAddAReminder_reminderListAppears() = runBlocking {
//
//        repository.saveReminder(reminderDto)
//        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
//        dataBindingIdlingResource.monitorActivity(activityScenario)
//
//
//        onView(withId(R.id.addReminderFAB)).perform(click())
//
//        onView(withId(R.id.reminderTitle)).perform(replaceText("title"))
//        onView(withId(R.id.reminderDescription)).perform(replaceText("description"))
//        onView(withId(R.id.selectLocation)).perform(click())
//
//        onView(withId(R.id.mapView)).perform(click())
//
//        delay(2000)
//        onView(withId(R.id.save_location_btn)).perform(click())
//        onView(withId(R.id.saveReminder)).perform(click())
//
//        delay(2000)
//
//        onView(withText(R.string.reminder_saved)).inRoot(isToast()).check(matches(isDisplayed()))
//
//        onView(withId(R.id.reminderssRecyclerView))
//            .check(matches(hasDescendant(withText("title2"))))
//        onView(withId(R.id.reminderssRecyclerView))
//            .check(matches(hasDescendant(withText("description2"))))
//        activityScenario.close()
//    }
//}
