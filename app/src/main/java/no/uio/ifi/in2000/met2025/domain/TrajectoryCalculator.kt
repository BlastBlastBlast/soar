package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.cos
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.domain.helperclasses.SimpleLinkedList
import no.uio.ifi.in2000.met2025.domain.helpers.get
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import no.uio.ifi.in2000.met2025.domain.helpers.times
import no.uio.ifi.in2000.met2025.domain.helpers.plus
import no.uio.ifi.in2000.met2025.domain.helpers.minus
import no.uio.ifi.in2000.met2025.domain.helpers.div

class TrajectoryCalculator(
    private val isobaricInterpolator: IsobaricInterpolator

) {
    fun calculateTrajectory(
        initialPosition: RealVector,
        launchAzimuth: Double,
        launchPitch: Double,
        launchRailLength: Double,
        wetMass: Double,
        dryMass: Double,
        burnTime: Double,
        thrust: Double,
        stepSize: Double,
        crossSectionalArea: Double,
        dragCoefficient: Double,
        parachuteCrossSectionalArea: Double,
        parachuteDragCoefficient: Double,
    ): List<Pair<RealVector, Double>> {
        val AzimuthAngle = Angle(launchAzimuth)
        val launchPitchAngle = Angle(launchPitch)

        val launchDirectionUnitVector = ArrayRealVector(
            doubleArrayOf(
                sin(AzimuthAngle) * cos(launchPitchAngle),
                cos(AzimuthAngle) * cos(launchPitchAngle),
                sin(launchPitchAngle)
            )
        ).unitVector()

        val accelerationFromGravity = ArrayRealVector(doubleArrayOf(0.0, 0.0, -GRAVITY))
        val accelerationFromGravityOnLaunchRail = -cos(Angle(90.0) - launchPitchAngle) * GRAVITY * launchDirectionUnitVector
        val zeroVector = ArrayRealVector(doubleArrayOf(0.0, 0.0, 0.0))

        tailrec fun calculateTrajectoryRecursive(
            currentPosition: RealVector,
            currentVelocity: RealVector,
            timeAfterLaunch: Double,
            coefficientOfDrag: Double,
            areaOfCrossSection: Double,
            result: SimpleLinkedList<Pair<RealVector, Double>>
        ): SimpleLinkedList<Pair<RealVector, Double>> {

            val airValues = isobaricInterpolator.getCartesianIsobaricValues(currentPosition)
            val windVector = ArrayRealVector(
                doubleArrayOf(airValues.windXComponent, airValues.windYComponent, 0.0)
            )

            val isOnLaunchRail: (RealVector) -> Boolean = { position ->
                (position - initialPosition).norm <= launchRailLength
            }

            val newVelocity = rungeKutta4(
                initialVector = currentVelocity,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { incrementedTime, incrementedVelocity ->
                    val velocityWithWind = if (isOnLaunchRail(currentPosition)) {
                        incrementedVelocity
                    } else {
                        incrementedVelocity.subtract(windVector)
                    }

                    val dragForce = -0.5 * crossSectionalArea * dragCoefficient * airValues.pressure * velocityWithWind.norm * velocityWithWind

                    val (thrustForce, burnProgress) = if (incrementedTime >= burnTime) {
                        Pair(zeroVector, 1.0)
                    } else {
                        Pair(thrust * launchDirectionUnitVector, incrementedTime / burnTime)
                    }

                    val massAtIncrement = wetMass * (1 - burnProgress) + dryMass * burnProgress

                    (dragForce + thrustForce) / massAtIncrement +
                        if (isOnLaunchRail(currentPosition)) accelerationFromGravityOnLaunchRail else accelerationFromGravity
                }
            )

            val newPosition = rungeKutta4(
                initialVector = currentPosition,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { _, incrementedPosition ->
                    currentVelocity - if (isOnLaunchRail(incrementedPosition)) zeroVector else windVector
                }
            )

            return if (newPosition[2] < initialPosition[2]) {
                result
            } else {
                val newPositionWithSpeed = Pair(newPosition, newVelocity.norm)
                result += newPositionWithSpeed

                if (currentVelocity[2] >= 0 && newVelocity[2] <= 0) {
                    calculateTrajectoryRecursive(
                        currentPosition = newPosition,
                        currentVelocity = newVelocity,
                        timeAfterLaunch = timeAfterLaunch + stepSize,
                        coefficientOfDrag = parachuteDragCoefficient,
                        areaOfCrossSection = parachuteCrossSectionalArea,
                        result = result
                    )
                } else {
                    calculateTrajectoryRecursive(
                        currentPosition = newPosition,
                        currentVelocity = newVelocity,
                        timeAfterLaunch = timeAfterLaunch + stepSize,
                        coefficientOfDrag = coefficientOfDrag,
                        areaOfCrossSection = areaOfCrossSection,
                        result = result
                    )
                }
            }
        }

        val result = calculateTrajectoryRecursive(
            currentPosition = initialPosition,
            currentVelocity = zeroVector,
            timeAfterLaunch = 0.0,
            coefficientOfDrag = dragCoefficient,
            areaOfCrossSection = crossSectionalArea,
            result = SimpleLinkedList(Pair(initialPosition, 0.0))
        )

        return result.toList()
    }
}

fun rungeKutta4(
    initialVector: RealVector,
    time: Double,
    stepSize: Double,
    derivative: (Double, RealVector) -> RealVector,
): RealVector {
    val k1 = derivative(time, initialVector)
    val k2 = derivative(time + stepSize / 2, initialVector + k1 * stepSize / 2.0)
    val k3 = derivative(time + stepSize / 2, initialVector + k2 * stepSize / 2.0)
    val k4 = derivative(time + stepSize, initialVector + k3 * stepSize)

    return initialVector + (k1 + 2.0 * k2 + 2.0 * k3 + k4) * stepSize / 6.0
}