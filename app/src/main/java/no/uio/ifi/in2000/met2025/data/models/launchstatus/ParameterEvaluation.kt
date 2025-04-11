package no.uio.ifi.in2000.met2025.data.models.launchstatus

data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus,
    val icon: EvaluationIcon? = null
)
