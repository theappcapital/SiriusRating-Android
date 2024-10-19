<p align="center">
  <img src="https://github.com/user-attachments/assets/ea137fe9-596b-4afb-95a1-cac80c68b1d6" height="128">
  <h1 align="center">SiriusRating Android</h1>
  <p align="center">See: https://github.com/theappcapital/SiriusRating-iOS for iOS.</p>
</p>

[![Kotlin](https://img.shields.io/badge/Kotlin-1.5%2B-blue?style=flat-square)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-24%2B-green?style=flat-square)](https://developer.android.com/about/versions)
[![Maven Central](https://img.shields.io/maven-central/v/com.theappcapital/siriusrating-android.svg?style=flat-square)](https://central.sonatype.com/artifact/com.theappcapital/siriusrating-android)

<img width="1012" alt="github-banner" src="https://github.com/user-attachments/assets/c4aa8c79-3195-43c4-95c8-822c3b5e7cbc">

A non-invasive and friendly way to remind users to review an Android app.

## Features

- [x] Automatically adapts to your theme
- [x] Jetpack Compose support
- [x] Dark mode support
- [x] Unit tested
- [x] Configurable rating conditions
- [x] Write your own rating conditions to further stimulate positive reviews.
- [x] Modern design
- [x] Non-invasive approach
- [x] Recurring prompts that are configurable using back-off factors
- [x] Option to create your own prompt style

## Setup

Configure a SiriusRating shared instance, typically in your MainActivity or your app's initializer.

### Simple One-line Setup

In the `onCreate()` function in MainActivity:

```kotlin
SiriusRating.setup(this)
```

For example:
```kotlin
// ...
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // ...
    SiriusRating.setup(this)
}
```

By default, the user will be prompted to rate the app when the following conditions are met:

- The app has been `installed for at least 30 days`,
- The user has `opened the app at least 15 times`, and
- The user has `completed 20 significant events`.

If the user selects 'Remind me later,' they will be prompted again after 7 days.
If the user declines the prompt, they will be prompted again after 30 days, with a back-off factor of 2. This means that if the user declines a second time, they will be prompted again in 60 days, a third time in 120 days, and so on.

## Installation

### Gradle

To integrate SiriusRating into your Android project, specify it in your `build.gradle.kts`:

```kotlin
dependencies {
    //...
    implementation("com.theappcapital.siriusrating-android:1.0.2")
}
```

Or `build.gradle`:

```groovy
dependencies {
    //...
    implementation 'com.theappcapital.siriusrating-android:1.0.2'
}
```

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

## Styles

| StyleOneRequestPromptPresenter (light, default)                                                                            | StyleOneRequestPromptPresenter (dark, default)                                                                            |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| ![Style One (light)](https://github.com/user-attachments/assets/88deafe9-0feb-4875-8b72-09dea9dd1a53) | ![Style Two (Dark)](https://github.com/user-attachments/assets/ed7ebdd8-d92f-4523-8f9b-4d61848e48f4) |

| StyleTwoRequestPromptPresenter (light)                                                                                     | StyleTwoRequestPromptPresenter (dark)                                                                                     |
|----------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| ![Style Two (light)](https://github.com/user-attachments/assets/4e589f06-3b78-4dd3-905d-465818369cf1) | ![Style Two (dark)](https://github.com/user-attachments/assets/3e1e6f2b-05bb-4149-bdf6-d0af68475262) |


## Rating conditions

The rating conditions are used to validate if the user can be prompted to rate the app. The validation process happens after the user did a significant event (`userDidSignificantEvent()`) or if configured when the app was opened. The user will be prompted to rate the app if all rating conditions are
satisfied (returning `true`).

| Rating Condition                             | Description                                                                                                                                                                                     |
|----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `EnoughAppSessionsRatingCondition`           | Validates whether the app has been launched or brought into the foreground a sufficient number of times.                                                                                        |
| `EnoughDaysUsedRatingCondition`              | Validates whether the app has been in use for a sufficient duration (in days).                                                                                                                  |
| `EnoughSignificantEventsRatingCondition`     | Validates whether the user has completed enough significant events.                                                                                                                             |
| `NotDeclinedToRateAnyVersionRatingCondition` | Validates that the user hasn’t declined to rate any version of the app. If declined, it checks whether enough time has passed since the initial decline before prompting again.                 |
| `NotPostponedDueToReminderRatingCondition`   | Validates whether the user has opted to be reminded later. If so, it checks if the required number of days has passed to prompt again.                                                          |
| `NotRatedCurrentVersionRatingCondition`      | Validates whether the user has already rated the current version of the app. The user won’t be prompted again if they’ve already rated this version.                                            |
| `NotRatedAnyVersionRatingCondition`          | Validates that the user hasn’t rated any version of the app. If the user has previously rated the app, it checks whether enough time has passed since their last rating before prompting again. |

## Customization

### Custom Configuration

```kotlin
SiriusRating.setup(activity) {
    debugEnabled(true)
    canPromptUserToRateOnLaunch(true)
    requestToRatePromptPresenter(StyleTwoRequestToRatePromptPresenter(activity))
    ratingConditions(
        EnoughDaysUsedRatingCondition(totalDaysRequired = 0),
        EnoughAppSessionsRatingCondition(totalAppSessionsRequired = 0),
        EnoughSignificantEventsRatingCondition(significantEventsRequired = 5),
        // Essential rating conditions below: Ensure these are included to prevent the prompt from appearing continuously.
        NotPostponedDueToReminderRatingCondition(totalDaysBeforeReminding = 14),
        NotDeclinedToRateAnyVersionRatingCondition(daysAfterDecliningToPromptUserAgain = 30, backOffFactor = 2.0, maxRecurringPromptsAfterDeclining = 2),
        NotRatedCurrentVersionRatingCondition(appVersionProvider = PackageInfoCompatAppVersionProvider(context = activity)),
        NotRatedAnyVersionRatingCondition(daysAfterRatingToPromptUserAgain = 240, maxRecurringPromptsAfterRating = Int.MAX_VALUE)
    )
    didAgreeToRateHandler {
        //...
    }
    didOptInForReminderHandler {
        //...
    }
    didDeclineToRateHandler {
        //...
    }
}
```

### Custom rating conditions

You can write your own rating conditions in addition to the current rating conditions to further stimulate positive reviews.

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

To make use of the new rating condition simply add it to the list.

```kotlin
SiriusRating.setup(this) {
    ratingConditions(
        // ...,
        GoodWeatherRatingCondition(weatherRepository = WeatherDataRepository())
    )
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
SiriusRating.setup(activity) {
    requestToRatePromptPresenter(StyleTwoRequestToRatePromptPresenter(activity, appName = "App Name"))
}
```

## License

SiriusRating is released under the MIT license. [See LICENSE](https://github.com/theappcapital/SiriusRating-Android/blob/master/LICENSE) for details.
