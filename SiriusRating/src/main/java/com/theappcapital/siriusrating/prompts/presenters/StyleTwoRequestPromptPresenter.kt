package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import android.content.DialogInterface
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.theappcapital.siriusrating.R

class StyleTwoRequestPromptPresenter(
    private val appName: String? = null,
    private val canOptInForReminder: Boolean = true
) : RequestPromptPresenter {

    private var alertDialog: AlertDialog? = null

    private fun appNameOrDefault(activity: Activity): String {
        return appName ?: activity.packageManager.getApplicationLabel(activity.applicationInfo).toString()
    }

    override fun show(
        activity: Activity,
        didAgreeToRateHandler: (() -> Unit)?,
        didOptInForReminderHandler: (() -> Unit)?,
        didDeclineToRateHandler: (() -> Unit)?,
        @ColorInt colorPrimary: Int?,
        @ColorInt colorOnPrimary: Int?
    ) {
        if (alertDialog != null) return // Dialog already exists, no need to recreate it

        val appNameOrDefault = appNameOrDefault(activity)

        val context = ContextThemeWrapper(activity, R.style.Theme_SiriusRating)
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder.setTitle(activity.getString(R.string.sirius_rating_text_view_title_text, appNameOrDefault))
        alertDialogBuilder.setMessage(activity.getString(R.string.sirius_rating_text_view_description_text, appNameOrDefault))

        alertDialogBuilder.setPositiveButton(activity.getString(R.string.sirius_rating_button_rate_text, appNameOrDefault)) { dialog, _ ->
            didAgreeToRateHandler?.invoke()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(activity.getString(R.string.sirius_rating_button_decline_text, appNameOrDefault)) { dialog, _ ->
            didDeclineToRateHandler?.invoke()
            dialog.dismiss()
        }

        if (canOptInForReminder) {
            alertDialogBuilder.setNeutralButton(activity.getString(R.string.sirius_rating_button_opt_in_for_reminder_text)) { dialog, _ ->
                didOptInForReminderHandler?.invoke()
                dialog.dismiss()
            }
        }

        this.alertDialog = alertDialogBuilder.create()

        val resolvedColorPrimary = colorPrimary ?: run {
            val colorPrimaryTypedValue = TypedValue()
            // Resolve `colorPrimary` via the AppCompat attr. Material themes inherit this attr from
            // AppCompat, so a single lookup covers both AppCompat- and Material-based themes.
            val colorExists = activity.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, colorPrimaryTypedValue, true)

            if (colorExists && colorPrimaryTypedValue.data != 0) {
                colorPrimaryTypedValue.data
            } else {
                null
            }
        }

        if (resolvedColorPrimary != null) {
            this.alertDialog?.setOnShowListener { dialog ->
                (dialog as? AlertDialog)?.let { alertDialog ->
                    val buttonTypes = listOf(
                        DialogInterface.BUTTON_POSITIVE,
                        DialogInterface.BUTTON_NEGATIVE,
                        DialogInterface.BUTTON_NEUTRAL
                    )

                    buttonTypes.forEach { buttonType ->
                        alertDialog.getButton(buttonType)?.setTextColor(resolvedColorPrimary)
                    }
                }
            }
        }

        this.alertDialog?.setOnDismissListener {
            this.alertDialog = null
        }

        if (!activity.isFinishing) {
            this.alertDialog?.show()
        }
    }

}
