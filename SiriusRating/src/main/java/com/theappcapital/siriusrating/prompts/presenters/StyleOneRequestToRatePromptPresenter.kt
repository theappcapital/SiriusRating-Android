package com.theappcapital.siriusrating.prompts.presenters

import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.theappcapital.siriusrating.R


class StyleOneRequestToRatePromptPresenter(
    private val activity: Activity,
    private val appName: String? = null,
    private val canOptInForReminder: Boolean = true
) : RequestToRatePromptPresenter {

    private var dialog: Dialog? = null

    private val _appIconDrawable: Drawable?
        get() {
            return try {
                activity.packageManager.getApplicationIcon(activity.applicationInfo)
            } catch (exception: PackageManager.NameNotFoundException) {
                null
            }
        }

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
        if (dialog == null) {
            // Inflate a layout with the custom theme applied
            val dialog = Dialog(activity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            // Create a ContextThemeWrapper with the custom theme
            val themedContext = ContextThemeWrapper(activity, R.style.Theme_SiriusRating)
            // Inflate the layout using the themed context
            val view = LayoutInflater.from(themedContext).inflate(R.layout.sirius_rating_content_view, null)
            dialog.setContentView(view)
            // Set background transparent so it will draw our content view instead of the 'native' view.
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Set decor view properties to avoid clipping of shadow for API 34+.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val rootView = dialog.window?.decorView
                rootView?.outlineProvider = ViewOutlineProvider.BOUNDS
                rootView?.clipToOutline = false
            }

            val titleTextView = dialog.findViewById<TextView>(R.id.text_view_title)
            titleTextView.text = activity.getString(R.string.sirius_rating_text_view_title_text, _appName)

            val durationTextView = dialog.findViewById<TextView>(R.id.text_view_duration)
            durationTextView.text = activity.getString(R.string.sirius_rating_text_view_duration_text, _appName)

            val descriptionTextView = dialog.findViewById<TextView>(R.id.text_view_description)
            descriptionTextView.text = activity.getString(R.string.sirius_rating_text_view_description_text, _appName)

            val rateButton = dialog.findViewById<Button>(R.id.button_rate)
            rateButton.text = activity.getString(R.string.sirius_rating_button_rate_text, _appName)
            rateButton.setOnClickListener {
                didAgreeToRateHandler?.invoke()
                dialog.dismiss()
            }

            val closeIconImageView = dialog.findViewById<ImageView>(R.id.image_view_close_icon)
            closeIconImageView.setOnClickListener {
                didDeclineHandler?.invoke()
                dialog.dismiss()
            }

            val optInForReminderButton = dialog.findViewById<Button>(R.id.button_opt_in_for_reminder)
            optInForReminderButton.text = activity.getString(R.string.sirius_rating_button_opt_in_for_reminder_text)
            optInForReminderButton.visibility = if (canOptInForReminder) View.VISIBLE else View.GONE
            optInForReminderButton.setOnClickListener {
                didOptInForReminderHandler?.invoke()
                dialog.dismiss()
            }

            val appIconImageView = dialog.findViewById<ImageView>(R.id.image_view_app_icon)
            appIconImageView.visibility = if (_appIconDrawable != null) View.VISIBLE else View.GONE
            appIconImageView.setImageDrawable(this._appIconDrawable)

            dialog.setOnDismissListener {
                this.dialog = null
            }

            this.dialog = dialog

            if (!this.activity.isFinishing) {
                this.dialog?.show()
            }
        }
    }

}