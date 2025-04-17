package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.domain.helperclasses.SimpleLinkedList
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.unaryMinus
import kotlin.math.cos
import kotlin.math.sin

class TrajectoryCalculator(
    private val isobaricInterpolator: IsobaricInterpolator
) {
    fun calculateTrajectory(
        initialPosition: D1Array<Double>,
        launchDirection: Angle,
        launchAngle: Angle, //upwards is 90 degrees
        initialSpeed: Double,
        wetMass: Double,
        dryMass: Double,
        massLossRate: Double,
        thrust: Double,
        timeStep: Double,
        estimatedCrossSectionalArea: Double,
        dragCoefficient: Double,
        parachuteDragCoefficient: Double
    ): Pair<List<Pair<D1Array<Double>, Double>>, List<Pair<D1Array<Double>, Double>>> {

        val initialAirValues: CartesianIsobaricValues = isobaricInterpolator.getCartesianIsobaricValues(
            position = initialPosition
        )

        val initialWindVector = mk.ndarray(mk[
            initialAirValues.windXComponent,
            initialAirValues.windYComponent,
            0.0]
        )

        val initialSpatialDirection = mk.ndarray(mk[
            cos(launchDirection.radians) * cos(launchAngle.radians),
            sin(launchDirection.radians) * cos(launchAngle.radians),
            sin(launchAngle.radians)]
        )

        val accelerationFromGravity = -mk.ndarray(mk[0.0, 0.0, GRAVITY])
        
        val zeroVector = mk.ndarray(mk[0.0, 0.0, 0.0])

        fun calculateTrajectoryRecursive(
            position: D1Array<Double>,
            velocity: D1Array<Double>,
            //acceleration: D1Array<Double>,
            currentMass: Double,
            result: Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>,SimpleLinkedList<Pair<D1Array<Double>, Double>>>
        ): Pair<SimpleLinkedList<Pair<D1Array<Double>, Double>>,SimpleLinkedList<Pair<D1Array<Double>, Double>>> {
            // calculate position
            // check with mapbox if altitude is above ground
            // if not, return result
            // calculate velocity
            // calculate acceleration
            // if disposableMass > 0, calculate new acceleration and update mass
            // new mass: currentMass - massLossRate * timeStep
            val airValues = isobaricInterpolator.getCartesianIsobaricValues(position)

            val thrustForce = (if (dryMass < currentMass) thrust * initialSpatialDirection else zeroVector)
            val dragForce = -0.5 * estimatedCrossSectionalArea * dragCoefficient * airValues.pressure * velocity * velocity

            val acceleration = (thrustForce + dragForce) / currentMass + accelerationFromGravity

            val newVelocity = velocity + acceleration * timeStep
            val newPosition = position + newVelocity * timeStep

            return Pair(SimpleLinkedList(), SimpleLinkedList())
        }

        fun calculateParachuteTrajectoryRecursive(
            position: D1Array<Double>,
            velocity: D1Array<Double>,
            //acceleration: D1Array<Double>,
            currentMass: Double,
            result: SimpleLinkedList<Pair<D1Array<Double>, Double>>
        ): SimpleLinkedList<Pair<D1Array<Double>, Double>> {
            return SimpleLinkedList()
        }

        calculateTrajectoryRecursive(
            position = initialPosition,
            velocity = initialSpeed * initialSpatialDirection + initialWindVector,
            //acceleration = thrust / wetMass * initialSpatialDirection - gravityVector,
            currentMass = wetMass,
            result = Pair(SimpleLinkedList(Pair(initialPosition, initialSpeed)), SimpleLinkedList())
        )

        return Pair(emptyList(), emptyList())
    }
}

