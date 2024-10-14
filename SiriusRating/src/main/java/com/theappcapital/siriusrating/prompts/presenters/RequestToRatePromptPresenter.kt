package com.theappcapital.siriusrating.prompts.presenters

import androidx.annotation.ColorInt

interface RequestToRatePromptPresenter {

    fun show(
        didAgreeToRateHandler: (() -> Unit)?,
        didOptInForReminderHandler: (() -> Unit)?,
        didDeclineHandler: (() -> Unit)?,
        @ColorInt colorPrimary: Int? = null,
        @ColorInt colorOnPrimary: Int? = null
    )

}