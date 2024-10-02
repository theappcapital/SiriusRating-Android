package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import android.content.Context
import androidx.annotation.UiContext
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theappcapital.siriusrating.R


class StyleTwoRequestToRatePromptPresenter(
    @UiContext private val activity: Activity,
    private val appName: String? = null,
    private val canOptInForReminder: Boolean = true
) : RequestToRatePromptPresenter {

    private var alertDialog: AlertDialog? = null

    private val _appName: String
        get() {
            if (appName != null) {
                return appName
            }

            val applicationInfo = activity.applicationInfo
            val stringId = applicationInfo.labelRes
            return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else activity.getString(stringId)
        }

    override fun show(
        didAgreeToRateHandler: (() -> Unit)?,
        didOptInForReminderHandler: (() -> Unit)?,
        didDeclineHandler: (() -> Unit)?
    ) {
        if (alertDialog == null) {
            val context = ContextThemeWrapper(activity, R.style.Theme_SiriusRating)
            val alertDialogBuilder = MaterialAlertDialogBuilder(context)
            alertDialogBuilder.setTitle(activity.getString(R.string.sirius_rating_text_view_title_text, _appName))
            alertDialogBuilder.setMessage(activity.getString(R.string.sirius_rating_text_view_description_text, _appName))

            alertDialogBuilder.setPositiveButton(activity.getString(R.string.sirius_rating_button_rate_text, _appName)) { dialog, which ->
                didAgreeToRateHandler?.invoke()
                dialog.dismiss()
            }

            alertDialogBuilder.setNegativeButton(activity.getString(R.string.sirius_rating_button_decline_text, _appName)) { dialog, which ->
                didDeclineHandler?.invoke()
                dialog.dismiss()
            }

            if (canOptInForReminder) {
                alertDialogBuilder.setNeutralButton(activity.getString(R.string.sirius_rating_button_opt_in_for_reminder_text)) { dialog, which ->
                    didOptInForReminderHandler?.invoke()
                    dialog.dismiss()
                }
            }

            this.alertDialog = alertDialogBuilder.create()

            this.alertDialog?.setOnDismissListener {
                this.alertDialog = null
            }

            if (!this.activity.isFinishing) {
                this.alertDialog?.show()
            }
        }
    }

}