package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
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
import kotlin.math.cos
import kotlin.math.sin
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
        estimatedCrossSectionalArea: Double,
        dragCoefficient: Double,
        parachuteDragCoefficient: Double
    ): Pair<List<Pair<D1Array<Double>, Double>>, List<Pair<D1Array<Double>, Double>>> {

        val initialAirValues: CartesianIsobaricValues = isobaricInterpolator.getCartesianIsobaricValues(
            position = initialPosition
        )

        val launchDirectionUnitVector = mk.ndarray(mk[
            sin(launchAzimuth.radians) * cos(launchPitch.radians),
            cos(launchAzimuth.radians) * cos(launchPitch.radians),
            sin(launchPitch.radians)]
        )

        val accelerationFromGravity = -mk.ndarray(mk[0.0, 0.0, GRAVITY])
        val zeroVector = mk.zeros<Double>(3)

        fun calculateParachuteTrajectoryRecursive(
            position: D1Array<Double>,
            velocity: D1Array<Double>,
            timeAfterLaunch: Double,
            result: SimpleLinkedList<Pair<D1Array<Double>, Double>>
        ): SimpleLinkedList<Pair<D1Array<Double>, Double>> {
            return SimpleLinkedList()
        }

        fun calculateTrajectoryRecursive(
            position: D1Array<Double>,
            velocity: D1Array<Double>,
            timeAfterLaunch: Double,
            result: Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>, SimpleLinkedList<Pair<D1Array<Double>, Double>>>
        ): Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>, SimpleLinkedList<Pair<D1Array<Double>, Double>>> {

            val airValues = isobaricInterpolator.getCartesianIsobaricValues(position/*, velocity*/)
            val windVector = mk.ndarray(mk[
                airValues.windXComponent,
                airValues.windYComponent,
                0.0]
            )

            val accelerationAfterIncrementedTime: (Double, D1Array<Double>) -> D1Array<Double> = { incrementedTime, incrementedVelocity ->
                // wind contributes to drag if it is in the opposite direction of the velocity
                // the velocity is not affected by wind when the rocket is on the launch rail
                val velocityWithWind = incrementedVelocity - if ((position - initialPosition).norm() < launchRailLength) {
                    zeroVector
                } else {
                    windVector
                }

                val dragForce = -0.5 * estimatedCrossSectionalArea * dragCoefficient * airValues.pressure * velocityWithWind * velocityWithWind

                val (thrustForce, burnProgress) = if (incrementedTime >= burnTime) {
                    Pair(zeroVector, 1.0)
                } else {
                    Pair(thrust * launchDirectionUnitVector, incrementedTime / burnTime)
                }

                // linear interpolation of mass wrt time
                val massAtIncrementedTime = wetMass * (1 - burnProgress) + dryMass * burnProgress

                // acceleration from thrust, drag and gravity
                (thrustForce + dragForce) / massAtIncrementedTime + accelerationFromGravity
            }

            // Runge-Kutta 4th order method for velocity
            val kv1 = accelerationAfterIncrementedTime(timeAfterLaunch, velocity)
            val kv2 = accelerationAfterIncrementedTime(timeAfterLaunch + stepSize / 2, velocity + kv1 * (stepSize / 2))
            val kv3 = accelerationAfterIncrementedTime(timeAfterLaunch + stepSize / 2, velocity + kv2 * (stepSize / 2))
            val kv4 = accelerationAfterIncrementedTime(timeAfterLaunch + stepSize, velocity + kv3 * stepSize)

            val newVelocity = velocity + (kv1 + 2.0 * kv2 + 2.0 * kv3 + kv4) * (stepSize / 6)

            val velocityAfterIncrementedTime: (/*Double, */D1Array<Double>) -> D1Array<Double> = {/* incrementedTime, */incrementedPosition ->
                // the velocity is not affected by wind when the rocket is on the launch rail
                velocity - if ((incrementedPosition - initialPosition).norm() < launchRailLength) {
                    zeroVector
                } else {
                    windVector
                }
            }

            // Runge-Kutta 4th order method for position
            val ks1 = velocityAfterIncrementedTime(position)
            val ks2 = velocityAfterIncrementedTime(position + ks1 * (stepSize / 2))
            val ks3 = velocityAfterIncrementedTime(position + ks2 * (stepSize / 2))
            val ks4 = velocityAfterIncrementedTime(position + ks3 * stepSize)

            val newPosition = position + (velocity + 2.0 * ks2 + 2.0 * ks3 + ks4) * (stepSize / 6)

            return if (newPosition[2] < initialPosition[2]) { // FIXME: This is a placeholder for the ground check
                // if new altitude is below ground, return result
                result
            } else {
                // add new position and speed to result
                val newPositionWithSpeed = Pair(newPosition, newVelocity.norm())
                result.first += newPositionWithSpeed

                if (velocity[2] >= 0 && newVelocity[2] <= 0) {
                    // if highest point is reached, deploy parachute and calculate new trajectory
                    result.second += calculateParachuteTrajectoryRecursive(
                        position = newPosition,
                        velocity = newVelocity,
                        timeAfterLaunch = timeAfterLaunch + stepSize,
                        result = SimpleLinkedList(newPositionWithSpeed)
                    )
                }
                // recursive call with new position and velocity
                calculateTrajectoryRecursive(
                    position = newPosition,
                    velocity = newVelocity,
                    timeAfterLaunch = timeAfterLaunch + stepSize,
                    result = result
                )
            }
        }

        val result = calculateTrajectoryRecursive(
            position = initialPosition,
            velocity = zeroVector,
            timeAfterLaunch = 0.0,
            result = Pair(SimpleLinkedList(Pair(initialPosition, 0.0)), SimpleLinkedList())
        )

        return Pair(result.first.toList(), result.second.toList())
    }
}

private fun D1Array<Double>.norm(): Double = sqrt(sum(this * this))

