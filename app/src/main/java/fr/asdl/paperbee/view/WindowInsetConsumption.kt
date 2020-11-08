package fr.asdl.paperbee.view

/**
 * See [android.view.WindowInsets.consumeSystemWindowInsets],
 * [android.view.WindowInsets.consumeStableInsets] and
 * [android.view.WindowInsets.consumeDisplayCutout] for more information.
 */
enum class WindowInsetConsumption {
    DISPLAY_CUTOUT,
    STABLE,
    SYSTEM_WINDOW;
}