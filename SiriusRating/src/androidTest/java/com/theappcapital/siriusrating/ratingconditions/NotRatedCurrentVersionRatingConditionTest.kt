package com.theappcapital.siriusrating.ratingconditions

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.theappcapital.siriusrating.SiriusRatingUserAction
import com.theappcapital.siriusrating.datastores.InMemorySiriusRatingDataStore
import com.theappcapital.siriusrating.support.versionproviders.InMemoryAppVersionProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4ClassRunner::class)
class NotRatedCurrentVersionRatingConditionTest {

    private lateinit var inMemorySiriusRatingDataStore: InMemorySiriusRatingDataStore

    @Before
    fun setUp() {
        this.inMemorySiriusRatingDataStore = InMemorySiriusRatingDataStore()
    }

    @Test
    fun test_condition_is_satisfied_when_the_user_did_not_rate_any_version_of_the_app() {
        val notRatedCurrentVersionRatingCondition = NotRatedCurrentVersionRatingCondition(appVersionProvider = InMemoryAppVersionProvider())

        // By default the user has not rated.
        // The condition should be satisfied, because the user did not rate the app.
        assertTrue(notRatedCurrentVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))

        // Just an extra check: Set the user actions to an empty list (which it should already be by default).
        // Setting it to an empty list means the user has not (yet) rated.
        this.inMemorySiriusRatingDataStore.ratedUserActions = listOf()

        // The condition should be satisfied, because the user did not rate.
        assertTrue(notRatedCurrentVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_satisfied_because_the_user_only_rated_a_version_other_than_the_current_version_of_the_app() {
        val appVersionProvider = InMemoryAppVersionProvider(appVersion = "0.1-currentversion")
        val notRatedCurrentVersionRatingCondition = NotRatedCurrentVersionRatingCondition(appVersionProvider = appVersionProvider)

        // The user rated a version other than the current version of the app.
        this.inMemorySiriusRatingDataStore.ratedUserActions = listOf(SiriusRatingUserAction(appVersion = "0.2-otherversionthancurrentversion", date = Instant.now()))

        // The condition should be satisfied because the user did not (yet) rate the current version, but only another version of the app.
        assertTrue(notRatedCurrentVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

    @Test
    fun test_condition_is_not_satisfied_because_the_user_rated_the_current_version_of_the_app() {
        val appVersionProvider = InMemoryAppVersionProvider()
        val notRatedCurrentVersionRatingCondition = NotRatedCurrentVersionRatingCondition(appVersionProvider = appVersionProvider)

        // The user rated the current version of the app.
        this.inMemorySiriusRatingDataStore.ratedUserActions = listOf(SiriusRatingUserAction(appVersion = appVersionProvider.appVersion, date = Instant.now()))

        // The condition should not be satisfied, because the user rated the current version of the app.
        assertFalse(notRatedCurrentVersionRatingCondition.isSatisfied(dataStore = inMemorySiriusRatingDataStore))
    }

}
