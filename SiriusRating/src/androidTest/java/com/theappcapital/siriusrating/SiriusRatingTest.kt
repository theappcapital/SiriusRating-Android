package com.theappcapital.siriusrating

import android.app.Activity
import com.theappcapital.siriusrating.datastores.InMemoryDataStore
import com.theappcapital.siriusrating.prompts.presenters.RequestToRatePromptPresenter
import com.theappcapital.siriusrating.support.versionproviders.InMemoryAppVersionProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class SiriusRatingTest {

    private lateinit var siriusRating: SiriusRating

    private lateinit var inMemorySiriusRatingDataStore: InMemoryDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemoryDataStore()

        this.siriusRating = SiriusRating(activity = mock(Activity::class.java)) {
            dataStore(dataStore = inMemorySiriusRatingDataStore)
            appVersionProvider(appVersionProvider = InMemoryAppVersionProvider())
            requestToRatePromptPresenter(requestToRatePromptPresenter = mock(RequestToRatePromptPresenter::class.java))
        }
    }

    @Test
    fun test_that_the_significant_event_count_is_incremented_by_one_when_the_user_did_a_significant_event() {
        assertEquals(this.inMemorySiriusRatingDataStore.significantEventCount, 0)

        siriusRating.userDidSignificantEvent()

        assertEquals(this.inMemorySiriusRatingDataStore.significantEventCount, 1)
    }

    @Test
    fun test_that_the_first_use_date_is_set_when_the_user_did_a_significant_event() {
        assertNull(this.inMemorySiriusRatingDataStore.firstUseDate)

        siriusRating.userDidSignificantEvent()

        assertNotNull(this.inMemorySiriusRatingDataStore.firstUseDate)
    }

    @Test
    fun test_that_the_previous_or_current_app_version_is_set_when_the_user_did_a_significant_event() {
        assertNull(this.inMemorySiriusRatingDataStore.previousOrCurrentAppVersion)

        siriusRating.userDidSignificantEvent()

        assertNotNull(this.inMemorySiriusRatingDataStore.previousOrCurrentAppVersion)
    }

    @Test
    fun test_that_the_app_sessions_count_is_incremented_by_one_when_the_user_launched_the_app() {
        assertEquals(this.inMemorySiriusRatingDataStore.appSessionsCount, 0)

        siriusRating.userDidLaunchApp()

        assertEquals(this.inMemorySiriusRatingDataStore.appSessionsCount, 1)
    }

    @Test
    fun test_that_the_first_use_date_is_set_when_the_user_launched_the_app() {
        assertNull(this.inMemorySiriusRatingDataStore.firstUseDate)

        siriusRating.userDidLaunchApp()

        assertNotNull(this.inMemorySiriusRatingDataStore.firstUseDate)
    }

    @Test
    fun test_that_the_previous_or_current_app_version_is_set_when_the_user_launched_the_app() {
        assertNull(this.inMemorySiriusRatingDataStore.previousOrCurrentAppVersion)

        siriusRating.userDidLaunchApp()

        assertNotNull(this.inMemorySiriusRatingDataStore.previousOrCurrentAppVersion)
    }

}