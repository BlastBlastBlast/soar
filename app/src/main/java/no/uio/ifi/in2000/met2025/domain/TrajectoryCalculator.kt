package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.cos
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.domain.helperclasses.SimpleLinkedList
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.default.math.DefaultMath.sum
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.unaryMinus
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import kotlin.math.sqrt

class TrajectoryCalculator(
    private val isobaricInterpolator: IsobaricInterpolator
) {
    fun calculateTrajectory(
        initialPosition: D1Array<Double>,
        launchAzimuth: Angle, //clockwise from north
        launchPitch: Angle, //upwards is 90 degrees
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
    ): Pair<List<Pair<D1Array<Double>, Double>>, List<Pair<D1Array<Double>, Double>>> {

        val initialAirValues: CartesianIsobaricValues = isobaricInterpolator.getCartesianIsobaricValues(
            position = initialPosition
        )

        val launchDirectionUnitVector = mk.ndarray(mk[
            sin(launchAzimuth) * cos(launchPitch),
            cos(launchAzimuth) * cos(launchPitch),
            sin(launchPitch)]
        )

        val accelerationFromGravity = -mk.ndarray(mk[0.0, 0.0, GRAVITY])

        val accelerationFromGravityOnLaunchRail = -launchDirectionUnitVector * cos(Angle(90.0) - launchPitch) * GRAVITY
        val zeroVector = mk.zeros<Double>(3)

        fun calculateTrajectoryRecursive(
            currentPosition: D1Array<Double>,
            currentVelocity: D1Array<Double>,
            timeAfterLaunch: Double,
            coefficientOfDrag: Double,
            areaOfCrossSection: Double,
            result: Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>, SimpleLinkedList<Pair<D1Array<Double>, Double>>>
        ): Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>, SimpleLinkedList<Pair<D1Array<Double>, Double>>> {

            val airValues = isobaricInterpolator.getCartesianIsobaricValues(currentPosition/*, velocity*/)
            val windVector = mk.ndarray(mk[
                airValues.windXComponent,
                airValues.windYComponent,
                0.0]
            )

            val isOnLaunchRail: (D1Array<Double>) -> Boolean = { position ->
                (position - initialPosition).norm() <= launchRailLength
            }

            val newVelocity = rungeKutta4(
                initialVector = currentVelocity,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { incrementedTime, incrementedVelocity ->
                    // wind contributes to drag if it is in the opposite direction of the velocity
                    // the velocity is not affected by wind when the rocket is on the launch rail
                    val velocityWithWind = incrementedVelocity - if (isOnLaunchRail(currentPosition)) {
                        zeroVector
                    } else {
                        windVector
                    }

                    val dragForce = -0.5 * crossSectionalArea * dragCoefficient * airValues.pressure * velocityWithWind * velocityWithWind

                    val (thrustForce, burnProgress) = if (incrementedTime >= burnTime) {
                        Pair(zeroVector, 1.0)
                    } else {
                        Pair(thrust * launchDirectionUnitVector, incrementedTime / burnTime)
                    }

                    // linear interpolation of mass wrt time
                    val massAtIncrementedTime = wetMass * (1 - burnProgress) + dryMass * burnProgress

                    // acceleration from thrust, drag and gravity
                    (thrustForce + dragForce) / massAtIncrementedTime + if (isOnLaunchRail(currentPosition)) accelerationFromGravityOnLaunchRail else accelerationFromGravity
                }
            )

            val newPosition = rungeKutta4(
                initialVector = currentPosition,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { _, incrementedPosition ->
                    // the velocity is not affected by wind when the rocket is on the launch rail
                    currentVelocity - if (isOnLaunchRail(incrementedPosition)) {
                        zeroVector
                    } else {
                        windVector
                    }
                }
            )

            return if (newPosition[2] < initialPosition[2]) { // FIXME: This is a placeholder for the ground check
                // if new altitude is below ground, return result
                result
            } else {
                // add new position and speed to result
                val newPositionWithSpeed = Pair(newPosition, newVelocity.norm())
                result.first += newPositionWithSpeed

                if (currentVelocity[2] >= 0 && newVelocity[2] <= 0) {
                    // if highest point is reached, deploy parachute and calculate new trajectory
                    result.second += calculateTrajectoryRecursive(
                        currentPosition = newPosition,
                        currentVelocity = newVelocity,
                        timeAfterLaunch = timeAfterLaunch + stepSize,
                        coefficientOfDrag = parachuteDragCoefficient,
                        areaOfCrossSection = parachuteCrossSectionalArea,
                        result = Pair(SimpleLinkedList(newPositionWithSpeed), SimpleLinkedList())
                    ).first
                }
                // recursive call with new position and velocity
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

        val result = calculateTrajectoryRecursive(
            currentPosition = initialPosition,
            currentVelocity = zeroVector,
            timeAfterLaunch = 0.0,
            coefficientOfDrag = dragCoefficient,
            areaOfCrossSection = crossSectionalArea,
            result = Pair(SimpleLinkedList(Pair(initialPosition, 0.0)), SimpleLinkedList())
        )

        return Pair(result.first.toList(), result.second.toList())
    }
}

private fun D1Array<Double>.norm(): Double = sqrt(sum(this * this))

// Runge-Kutta 4th order method for a single step
fun rungeKutta4(
    initialVector: D1Array<Double>,
    time: Double,
    stepSize: Double,
    derivative: (Double, D1Array<Double>) -> D1Array<Double>,
): D1Array<Double> {
    val k1 = derivative(time, initialVector)
    val k2 = derivative(time + stepSize / 2, initialVector + k1 * (stepSize / 2))
    val k3 = derivative(time + stepSize / 2, initialVector + k2 * (stepSize / 2))
    val k4 = derivative(time + stepSize, initialVector + k3 * stepSize)

    return initialVector + (k1 + 2.0 * k2 + 2.0 * k3 + k4) * (stepSize / 6)
}
