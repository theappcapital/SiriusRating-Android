package com.theappcapital.siriusrating.prompts.presenters

interface RequestToRatePromptPresenter {

    fun show(
        didAgreeToRateHandler: (() -> Unit)?,
        didOptInForReminderHandler: (() -> Unit)?,
        didDeclineHandler: (() -> Unit)?
    )

}