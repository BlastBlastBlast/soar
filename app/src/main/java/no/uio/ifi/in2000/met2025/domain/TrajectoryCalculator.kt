package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import kotlin.math.cos
import kotlin.math.sin

class TrajectoryCalculator(
    weatherModel: WeatherModel
) {
    fun calcutaleTrajectory(
        initialPosition: Triple<Double, Double, Double>,
        launchDirection: Angle,
        launchAngle: Angle, //upwards is 90 degrees
        initialSpeed: Double,
        wetMass: Double,
        dryMass: Double,
        massLossRate: Double,
        thrust: Double,
        timeStep: Double,
        dragCoefficient: Double,
        parachuteDragCoefficient: Double
    ): Pair<
            List<
                    Pair<
                            Triple<Double, Double, Double>,
                            Double
                    >
            >,
            List<
                    Pair<
                            Triple<Double, Double, Double>,
                            Double
                    >
            >
        > {
        fun calculateTrajectoriesRecursive(
            currentPosition: Triple<Double, Double, Double>,
            currentVelocity: Triple<Double, Double, Double>,
            currentAcceleration: Triple<Double, Double, Double>,
            disposableMass: Double,
            result: Pair<List<Pair<Triple<Double, Double, Double>, Double>>,List<Pair<Triple<Double, Double, Double>, Double>>>
        ): Pair<List<Pair<Triple<Double, Double, Double>, Double>>,List<Pair<Triple<Double, Double, Double>, Double>>> {
            return Pair(emptyList(), emptyList())
        }

        fun calculateParachuteTrajectoryRecursive(
            currentPosition: Triple<Double, Double, Double>,
            currentVelocity: Triple<Double, Double, Double>,
            currentAcceleration: Triple<Double, Double, Double>,
            disposableMass: Double,
            result: List<Pair<Triple<Double, Double, Double>, Double>>
        ): List<Pair<Triple<Double, Double, Double>, Double>> {
            return emptyList()
        }

        val sphereComponents = Triple(
            cos(launchDirection.radians) * cos(launchAngle.radians),
            sin(launchDirection.radians) * cos(launchAngle.radians),
            sin(launchAngle.radians)
        )

        calculateTrajectoriesRecursive(
            currentPosition = initialPosition,
            currentVelocity = Triple(
                initialSpeed * sphereComponents.first,
                initialSpeed * sphereComponents.second,
                initialSpeed * sphereComponents.third
            ),
            currentAcceleration = Triple(
                thrust * sphereComponents.first / wetMass,
                thrust * sphereComponents.second / wetMass,
                thrust * sphereComponents.third / wetMass
            ),
            disposableMass = wetMass,
            result = Pair(listOf(Pair(initialPosition, initialSpeed)), emptyList())
        )

        return Pair(emptyList(), emptyList())
    }
}