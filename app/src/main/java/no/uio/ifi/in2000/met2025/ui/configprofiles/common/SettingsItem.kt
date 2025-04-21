package no.uio.ifi.in2000.met2025.ui.configprofiles.common

data class SettingItem(
    val label: String,
    val value: String,
    val onValueChange: (String) -> Unit,
    val enabled: Boolean,
    val onEnabledChange: (Boolean) -> Unit
)