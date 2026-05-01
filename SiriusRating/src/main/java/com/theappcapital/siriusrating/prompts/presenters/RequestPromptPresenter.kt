package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import androidx.annotation.ColorInt

interface RequestPromptPresenter {

    fun show(
        activity: Activity,
        didAgreeToRateHandler: (() -> Unit)?,
        didOptInForReminderHandler: (() -> Unit)?,
        didDeclineToRateHandler: (() -> Unit)?,
        @ColorInt colorPrimary: Int? = null,
        @ColorInt colorOnPrimary: Int? = null
    )

}
