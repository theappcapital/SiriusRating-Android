<p align="center">
  <img src="https://github.com/user-attachments/assets/ea137fe9-596b-4afb-95a1-cac80c68b1d6" height="128">
  <h1 align="center">SiriusRating Android</h1>
</p>

<p align="center">See: https://github.com/theappcapital/SiriusRating-iOS for iOS.</p>

[![Kotlin](https://img.shields.io/badge/Kotlin-1.5%2B-blue?style=flat-square)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-24%2B-green?style=flat-square)](https://developer.android.com/about/versions)
[![Maven Central](https://img.shields.io/maven-central/v/com.theappcapital/siriusrating-android.svg?style=flat-square)](https://central.sonatype.com/artifact/com.theappcapital/siriusrating-android)

<img width="1012" alt="github-banner" src="https://github.com/user-attachments/assets/c4aa8c79-3195-43c4-95c8-822c3b5e7cbc">

A non-invasive and friendly way to remind users to review and rate an Android app.

## Features

- [x] Supports 32 languages
- [x] Automatically adapts to your theme
- [x] Unit tested
- [x] Dark mode compatibility
- [x] Supports Jetpack Compose and XML views
- [x] Configurable rating conditions
- [x] Write your own rating conditions to further stimulate positive reviews.
- [x] Modern, sleek design
- [x] Non-invasive prompts
- [x] Configurable recurring prompts with back-off factors
- [x] Create custom prompt styles

## Requirements
- Android API 24+
- Kotlin 1.5+

## Setup
Configure a SiriusRating shared instance, typically in a custom `Application` subclass's `onCreate()`. SiriusRating automatically uses your app's name in the prompt.

### Simple One-line Setup
Using default configuration (e.g. in your `Application`):
```kotlin
SiriusRating.setup(this)
```

For example:
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // ...
        SiriusRating.setup(this)
    }
}
```

Or with custom thresholds:

```kotlin
SiriusRating.setup(this) {
    // Prompt thresholds
    daysUntilPrompt = 14
    appSessionsUntilPrompt = 10
    significantEventsUntilPrompt = 5

    // Reminder behavior
    daysBeforeReminding = 7

    // Decline behavior
    daysAfterDecliningToPromptAgain = 30
    declineBackOffFactor = 2.0
    maxPromptsAfterDeclining = 2

    // Re-prompt after rating
    daysAfterRatingToPromptAgain = 240
    maxPromptsAfterRating = 3

    // Custom conditions
    additionalConditions = listOf(
        /* ... */
    )
    customCondition = { dataStore ->
        // Don't prompt on weekends.
        val day = LocalDate.now().dayOfWeek
        day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
    }

    // Handlers
    didAgreeToRateHandler = { /* ... */ }
    didOptInForReminderHandler = { /* ... */ }
    didDeclineToRateHandler = { /* ... */ }

    // Misc
    canPromptUserToRateOnLaunch = true
}
```

By default, the user will be prompted to rate the app when the following conditions are met:

- The app has been `installed for at least 30 days`,
- The user has `opened the app at least 15 times`, and
- The user has `completed 20 significant events`.

If the user selects 'Remind me later,' they will be prompted again after 7 days.
If the user declines the prompt, they will be prompted again after 30 days, with a back-off factor of 2. This means that if the user declines a second time, they will be prompted again in 60 days, a third time in 120 days, and so on.

## Usage

### Significant event
A significant event defines an important event that occurred in your app. In a time tracking app it might be
that a user registered a time entry. In a game, it might be completing a level.

```kotlin
SiriusRating.instance().userDidSignificantEvent()
```

SiriusRating will validate the conditions after each significant event and prompt the user if all conditions are satisfied.

### Test request prompt
To see how the request prompt will look like in your app simply use the following code.

```kotlin
// For test purposes only.
SiriusRating.instance().showRequestPrompt()
```

### Available Configuration Properties

| Property | Default | Description |
|---|---|---|
| `daysUntilPrompt` | `30` | The number of days the app must be installed before prompting |
| `appSessionsUntilPrompt` | `15` | The number of app sessions (launches or foreground entries) before prompting |
| `significantEventsUntilPrompt` | `20` | The number of significant events before prompting |
| `daysBeforeReminding` | `7` | The number of days to wait before reminding a user who chose "Remind me later" |
| `daysAfterDecliningToPromptAgain` | `30` | The number of days to wait before prompting again after the user declines |
| `declineBackOffFactor` | `2.0` | The back-off multiplier applied for each successive decline (e.g. 30, 60, 120 days). Set to `null` to disable |
| `maxPromptsAfterDeclining` | `2` | The maximum number of times the user can be re-prompted after declining |
| `daysAfterRatingToPromptAgain` | `240` | The number of days to wait before prompting a user who has already rated |
| `maxPromptsAfterRating` | `Int.MAX_VALUE` | The maximum number of times the user can be re-prompted after rating |
| `debugEnabled` | `false` | When `true`, prints diagnostic information to the log. Automatically disabled in non-debug builds |
| `canPromptUserToRateOnLaunch` | `false` | When `true`, checks conditions and potentially shows the prompt on app launch or foreground |

## Installation

### Gradle

To integrate SiriusRating into your Android project, specify it in your `build.gradle.kts`:

```kotlin
dependencies {
    //...
    implementation("com.theappcapital.siriusrating-android:2.0.0")
}
```

Or `build.gradle`:

```groovy
dependencies {
    //...
    implementation 'com.theappcapital.siriusrating-android:2.0.0'
}
```

## Styles

| StyleOneRequestPromptPresenter (light, default) | StyleOneRequestPromptPresenter (dark, default) |
| --- | --- |
| ![Style One (light)](https://github.com/user-attachments/assets/88deafe9-0feb-4875-8b72-09dea9dd1a53) | ![Style One (dark)](https://github.com/user-attachments/assets/ed7ebdd8-d92f-4523-8f9b-4d61848e48f4) |

| StyleTwoRequestPromptPresenter (light) | StyleTwoRequestPromptPresenter (dark) |
| --- | --- |
| ![Style Two (light)](https://github.com/user-attachments/assets/4e589f06-3b78-4dd3-905d-465818369cf1) | ![Style Two (dark)](https://github.com/user-attachments/assets/3e1e6f2b-05bb-4149-bdf6-d0af68475262) |

## Customization

### Custom rating conditions

You can write your own rating conditions in addition to the default conditions to further stimulate positive reviews.

```kotlin
class GoodWeatherRatingCondition(
    private val weatherRepository: WeatherRepository
) : RatingCondition {

    override fun isSatisfied(dataStore: DataStore): Boolean {
        // Only show the rating prompt when it's sunny outside.
        return weatherRepository.getWeather().isSunny
    }
}
```

Add it alongside the default conditions:

```kotlin
SiriusRating.setup(this) {
    additionalConditions = listOf(
        GoodWeatherRatingCondition(weatherRepository = WeatherDataRepository())
    )
}
```

For simple conditions, you can use an inline closure instead of a class:

```kotlin
SiriusRating.setup(this) {
    customCondition = { dataStore ->
        // Don't prompt on weekends.
        val day = LocalDate.now().dayOfWeek
        day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
    }
}
```

### Change prompt style
```kotlin
SiriusRating.setup(this) {
    requestPromptPresenter = StyleTwoRequestPromptPresenter()
}
```

### Change texts

You can override the texts in `strings.xml`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="sirius_rating.text_view_title.text">Rate %1$s</string>
    <string name="sirius_rating.text_view_duration.text">(duration: less than 10 seconds)</string>
    <string name="sirius_rating.text_view_description.text">If you enjoy using %1$s, would you mind taking a moment to rate it? Thanks for your support!</string>
    <string name="sirius_rating.button_rate.text">Rate %1$s</string>
    <string name="sirius_rating.button_decline.text">No, thanks</string>
    <string name="sirius_rating.button_opt_in_for_reminder.text">Remind me later</string>
</resources>
```

### Change colors

SiriusRating will automatically use your primary theme colors. You can manually customize them in `themes.xml`.

For Google Material theme:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.YourApp" parent="android:Theme.Material.Light.NoActionBar">
        <item name="colorPrimary">@color/black</item>
        <item name="colorOnPrimary">@color/white</item>
    </style>
</resources>
```

Or if you're using App Compat theme:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.YourApp" parent="Theme.AppCompat.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorControlNormal">@color/white</item>
    </style>
</resources>
```

### Change app name

SiriusRating will automatically use the `app_name` in the `strings.xml`. If you don't want to use this name you can set it manually.

```kotlin
SiriusRating.setup(this) {
    requestPromptPresenter = StyleTwoRequestPromptPresenter(appName = "App Name")
}
```

## License

SiriusRating is released under the MIT license. [See LICENSE](https://github.com/theappcapital/SiriusRating-Android/blob/master/LICENSE) for details.