package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode

class GooglePlayRatePromptPresenter(private val activity: Activity) : RatePromptPresenter {

    override fun show() {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                task.result?.let { reviewInfo ->
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                    }
                }
            } else {
                // There was some problem, log or handle the error code.
                val exception = task.exception
                if (exception is ReviewException) {
                    @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException?)!!.errorCode
                } else {

                }
            }
        }
    }

}