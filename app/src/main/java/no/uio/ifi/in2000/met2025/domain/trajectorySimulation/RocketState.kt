package no.uio.ifi.in2000.met2025.domain.trajectorySimulation

enum class RocketState {
    ON_LAUNCH_RAIL,
    THRUSTING,
    FREE_FLIGHT,
    PARACHUTE_DEPLOYED,
    LANDED
}