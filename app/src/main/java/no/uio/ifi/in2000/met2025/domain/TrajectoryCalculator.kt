package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.cos
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.data.models.Vector3D
import no.uio.ifi.in2000.met2025.data.models.times
import no.uio.ifi.in2000.met2025.domain.helperclasses.SimpleLinkedList

class TrajectoryCalculator(
    private val isobaricInterpolator: IsobaricInterpolator
) {
    fun calculateTrajectory(
        initialPosition: Vector3D,
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
    ): List<Pair<Vector3D, Double>> {

        val launchDirectionUnitVector = Vector3D(
            x = sin(launchAzimuth) * cos(launchPitch),
            y = cos(launchAzimuth) * cos(launchPitch),
            z = sin(launchPitch)
        )

        val accelerationFromGravity = Vector3D(0.0, 0.0, -GRAVITY)
        val accelerationFromGravityOnLaunchRail = -cos(Angle(90.0) - launchPitch) * GRAVITY * launchDirectionUnitVector
        val zeroVector = Vector3D(0.0, 0.0, 0.0)

        tailrec fun calculateTrajectoryRecursive(
            currentPosition: Vector3D,
            currentVelocity: Vector3D,
            timeAfterLaunch: Double,
            coefficientOfDrag: Double,
            areaOfCrossSection: Double,
            result: SimpleLinkedList<Pair<Vector3D, Double>>
        ): SimpleLinkedList<Pair<Vector3D, Double>> {

            val airValues = isobaricInterpolator.getCartesianIsobaricValues(currentPosition)
            val windVector = Vector3D(
                x = airValues.windXComponent,
                y = airValues.windYComponent,
                z = 0.0
            )

            val isOnLaunchRail: (Vector3D) -> Boolean = { position ->
                (position - initialPosition).norm() <= launchRailLength
            }

            val newVelocity = rungeKutta4(
                initialVector = currentVelocity,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { incrementedTime, incrementedVelocity ->
                    // wind contributes to drag if it is in the opposite direction of the velocity
                    // the velocity is not affected by wind when the rocket is on the launch rail
                    val velocityWithWind = if (isOnLaunchRail(currentPosition)) {
                        incrementedVelocity
                    } else {
                        incrementedVelocity - windVector
                    }

                    val dragForce = -0.5 * crossSectionalArea * dragCoefficient * airValues.pressure * velocityWithWind * velocityWithWind

                    val (thrustForce, burnProgress) = if (incrementedTime >= burnTime) {
                        Pair(zeroVector, 1.0)
                    } else {
                        Pair(thrust * launchDirectionUnitVector , incrementedTime / burnTime)
                    }

                    // linear interpolation of mass wrt time
                    val massAtIncrement = wetMass * (1 - burnProgress) + dryMass * burnProgress

                    // acceleration from thrust, drag and gravity
                    (thrustForce + dragForce) / massAtIncrement + if (isOnLaunchRail(currentPosition)) accelerationFromGravityOnLaunchRail else accelerationFromGravity
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

            return if (newPosition.z < initialPosition.z) { // FIXME: This is a placeholder for the ground check
                // if new altitude is below ground, return result
                result
            } else {
                // add new position and speed to result
                val newPositionWithSpeed = Pair(newPosition, newVelocity.norm())
                result += newPositionWithSpeed

                if (currentVelocity.z >= 0 && newVelocity.z <= 0) {
                    // if highest point is reached, deploy parachute and calculate new trajectory
                    calculateTrajectoryRecursive(
                        currentPosition = newPosition,
                        currentVelocity = newVelocity,
                        timeAfterLaunch = timeAfterLaunch + stepSize,
                        coefficientOfDrag = parachuteDragCoefficient,
                        areaOfCrossSection = parachuteCrossSectionalArea,
                        result = result
                    )
                } else {
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

// Runge-Kutta 4th order method for a single step
fun rungeKutta4(
    initialVector: Vector3D,
    time: Double,
    stepSize: Double,
    derivative: (Double, Vector3D) -> Vector3D,
): Vector3D {
    val k1 = derivative(time, initialVector)
    val k2 = derivative(time + stepSize / 2, initialVector + k1 * (stepSize / 2))
    val k3 = derivative(time + stepSize / 2, initialVector + k2 * (stepSize / 2))
    val k4 = derivative(time + stepSize, initialVector + k3 * stepSize)

    return initialVector + (k1 + k2 * 2.0 + k3 * 2.0 + k4) * (stepSize / 6)
}
