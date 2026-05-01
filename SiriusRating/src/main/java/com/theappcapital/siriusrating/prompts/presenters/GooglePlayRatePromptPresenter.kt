package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory

class GooglePlayRatePromptPresenter : RatePromptPresenter {

    override fun show(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object.
                task.result?.let { reviewInfo ->
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                }
            } else {
                val exception = task.exception
                if (exception is ReviewException) {
                    Log.w("SiriusRating", "Google Play review flow request failed (errorCode=${exception.errorCode}).", exception)
                } else {
                    Log.w("SiriusRating", "Google Play review flow request failed.", exception)
                }
            }
        }
    }

}
