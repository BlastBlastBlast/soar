package no.uio.ifi.in2000.met2025.domain.trajectorySimulation

import android.util.Log
import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.domain.helperclasses.SimpleLinkedList
import no.uio.ifi.in2000.met2025.domain.helpers.div
import no.uio.ifi.in2000.met2025.domain.helpers.get
import no.uio.ifi.in2000.met2025.domain.helpers.minus
import no.uio.ifi.in2000.met2025.domain.helpers.plus
import no.uio.ifi.in2000.met2025.domain.helpers.times
import no.uio.ifi.in2000.met2025.domain.interpolation.IsobaricInterpolator
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector
import java.time.Instant
import kotlin.math.cos
import no.uio.ifi.in2000.met2025.data.models.cos

/**
 * TrajectoryCalculator is responsible for calculating the trajectory of a rocket
 * based on the initial conditions and parameters provided.
 * Uses the IsobaricInterpolator to get the air values at a given position and time.
 * Applies numerical integration (Runge-Kutta method) to estimate the position and velocity at each time step.
 */
class TrajectoryCalculator(
    private val isobaricInterpolator: IsobaricInterpolator
) {
    private var refLatRad = 0.0
    private var refLonRad = 0.0

    /**
     * Convert a geodetic point (latDeg, lonDeg, altM)
     * into ENU meters relative to the launch origin.
     */
    private fun geoToEnu(latDeg: Double, lonDeg: Double, alt: Double): RealVector {
        val lat = Math.toRadians(latDeg)
        val lon = Math.toRadians(lonDeg)
        val dLat = lat - refLatRad
        val dLon = lon - refLonRad
        val north = dLat * Constants.EARTH_RADIUS
        val east  = dLon * Constants.EARTH_RADIUS * cos(refLatRad)
        return ArrayRealVector(doubleArrayOf(east, north, alt))
    }

    /**
     * Convert an ENU‐vector back into (latDeg, lonDeg, altM).
     */
    private fun enuToGeo(enu: RealVector): Triple<Double, Double, Double> {
        val (east, north, alt) = enu.toArray()
        val lat = refLatRad + north / Constants.EARTH_RADIUS
        val lon = refLonRad + east  / (Constants.EARTH_RADIUS * cos(refLatRad))
        return Triple(Math.toDegrees(lat), Math.toDegrees(lon), alt)
    }

    /**
     * Initiates the trajectory calculation.
     * @return Result containing a list of tuples with the position, velocity, and rocket state at each time step.
     */
    suspend fun calculateTrajectory(
        initialPosition: RealVector,            // lat, long, elevation from viewmodel function call
        launchAzimuthInDegrees: Double,         // degrees
        launchPitchInDegrees: Double,           // degrees
        launchRailLength: Double,               // meters
        wetMass: Double,                        // kg
        dryMass: Double,                        // kg
        burnTime: Double,                       // seconds
        thrust: Double,                         // Newtons
        stepSize: Double,                       // seconds
        crossSectionalArea: Double,             // m^2
        dragCoefficient: Double,                // dimensionless
        parachuteCrossSectionalArea: Double,    // m^2
        parachuteDragCoefficient: Double,       // dimensionless
        timeOfLaunch: Instant = Instant.now()   // time of launch
    ): Result<List<Triple<RealVector, Double, RocketState>>> {

        Log.i("TrajectoryCalculator", "calculateTrajectory: initial position: $initialPosition")

        // get forecast data at initial position and time of launch, to avoid doing it for every time step in the recursive function
        isobaricInterpolator.setForecastDataForInitialPosition(initialPosition, timeOfLaunch).fold(
            onSuccess = { Log.i("TrajectoryCalculator", "calculateTrajectory: forecast data set successfully") },
            onFailure = { return Result.failure(it) }
        )

        val lat0 = initialPosition[0]
        val lon0 = initialPosition[1]
        this.refLatRad = Math.toRadians(lat0)
        this.refLonRad = Math.toRadians(lon0)

        // calculates using ENU coordinates (in meters)
        val enuStart = geoToEnu(lat0, lon0, initialPosition[2])

        val launchAzimuth = Angle(launchAzimuthInDegrees)
        val launchPitch = Angle(launchPitchInDegrees)

        val launchDirectionUnitVector = ArrayRealVector(
            doubleArrayOf(
                sin(launchAzimuth) * cos(launchPitch),
                cos(launchAzimuth) * cos(launchPitch),
                sin(launchPitch)
            )
        ).unitVector()

        Log.i("TrajectoryCalculator", "calculateTrajectory: launchDirectionUnitVector: $launchDirectionUnitVector, length: ${launchDirectionUnitVector.norm}")

        val accelerationFromGravity =
            ArrayRealVector(doubleArrayOf(0.0, 0.0, -Constants.GRAVITY))
        // The acceleration from gravity on the launch rail is calculated by projecting the gravity vector onto the launch direction.
        // It is parallel to the launch direction.
        val accelerationFromGravityOnLaunchRail =
            -cos(Angle(90.0) - launchPitch) *
                Constants.GRAVITY * launchDirectionUnitVector
        val zeroVector = ArrayRealVector(doubleArrayOf(0.0, 0.0, 0.0))

        Log.i("TrajectoryCalculator", "calculateTrajectory: accelerationFromGravity: $accelerationFromGravity")
        Log.i("TrajectoryCalculator", "calculateTrajectory: accelerationFromGravityOnLaunchRail: $accelerationFromGravityOnLaunchRail")

        /**
         * The Runge-Kutta method is used to solve the ordinary differential equations (ODEs) that describe the motion of the rocket.
         * The ODEs are derived from Newton's second law of motion, taking into account the forces acting on the rocket (thrust, drag, and gravity).
         * The method provides a numerical solution to the ODEs by approximating the trajectory at discrete time steps.
         */
        tailrec suspend fun calculateTrajectoryRecursive(
            currentPosition: RealVector,
            currentVelocity: RealVector,
            timeAfterLaunch: Double,
            coefficientOfDrag: Double,
            areaOfCrossSection: Double,
            result: SimpleLinkedList<Triple<RealVector, Double, RocketState>>
        ): Result<SimpleLinkedList<Triple<RealVector, Double, RocketState>>> {

            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: timeAfterLaunch: $timeAfterLaunch")
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: current velocity: $currentVelocity")
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: current position: $currentPosition")
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: coefficientOfDrag: $coefficientOfDrag")
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: areaOfCrossSection: $areaOfCrossSection")

            val onLaunchRail: (RealVector) -> Boolean = { position ->
                (position - initialPosition).norm <= launchRailLength
            }

            // the interpolator needs the position in degrees
            val (latDeg, lonDeg, altM) = enuToGeo(currentPosition)
            val currentGeoPosition = ArrayRealVector(doubleArrayOf(latDeg, lonDeg, altM))

            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: currentGeoPosition: $currentGeoPosition")

            val airValues = isobaricInterpolator.getCartesianIsobaricValues(currentGeoPosition, timeOfLaunch)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                )

            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: airValues: $airValues")

            // we don't have data for wind in the vertical direction
            val windVector = ArrayRealVector(
                doubleArrayOf(airValues.windXComponent, airValues.windYComponent, 0.0)
            )

            // Used to calculate drag force
            val airDensity = 100.0 * airValues.pressure * Constants.EARTH_AIR_MOLAR_MASS / ((airValues.temperature + Constants.CELSIUS_TO_KELVIN) * Constants.UNIVERSAL_GAS_CONSTANT)
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: airDensity: $airDensity")

            val newVelocity = rungeKutta4(
                initialVector = currentVelocity,
                time = timeAfterLaunch,
                stepSize = stepSize,
                derivative = { incrementedTime, incrementedVelocity ->
                    // calculate the acceleration

                    // ignores the wind when the rocket is on the launch rail
                    val velocityWithWind = if (onLaunchRail(currentPosition)) {
                        incrementedVelocity
                    } else {
                        incrementedVelocity - windVector
                    }

                    val dragForce = -0.5 * (areaOfCrossSection * coefficientOfDrag * airDensity * velocityWithWind.norm * velocityWithWind)
                    Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: dragForce: $dragForce")

                    // burnProgress is used to calculate current mass
                    val (thrustForce, burnProgress) = if (incrementedTime >= burnTime) {
                        Pair(zeroVector, 1.0)
                    } else {
                        Pair(thrust * launchDirectionUnitVector, incrementedTime / burnTime)
                    }
                    Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: thrustVector: $thrustForce, burnProgress: $burnProgress")

                    // equals wetMass when burnProgress is 0, and dryMass when burnProgress is 1
                    val massAtIncrement = wetMass * (1 - burnProgress) + dryMass * burnProgress
                    Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: massAtIncrement: $massAtIncrement")

                    Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: acceleration from drag: ${dragForce / massAtIncrement}")

                    (dragForce + thrustForce) / massAtIncrement +
                        if (onLaunchRail(currentPosition)) accelerationFromGravityOnLaunchRail else accelerationFromGravity
                }
            )
            Log.i("TrajectoryCalculator", "calculateTrajectoryRecursive: newVelocity: $newVelocity")

            // assumes constant velocity during the time step
            val newPosition = currentPosition + currentVelocity * stepSize

            // frontend expects the position in degrees
            val nextGeoPositionTriple = enuToGeo(newPosition)
            val nextGeoPosition = ArrayRealVector(
                doubleArrayOf(
                    nextGeoPositionTriple.first,
                    nextGeoPositionTriple.second,
                    nextGeoPositionTriple.third
                )
            )

            val rocketState = when {
                onLaunchRail(currentPosition) -> RocketState.ON_LAUNCH_RAIL
                timeAfterLaunch < burnTime -> RocketState.THRUSTING
                newVelocity[2] > 0 -> RocketState.FREE_FLIGHT
                newVelocity[2] <= 0 && newPosition[2] > enuStart[2] -> RocketState.PARACHUTE_DEPLOYED
                else -> RocketState.LANDED
            }

            result += Triple(nextGeoPosition, newVelocity.norm, rocketState)

            return if (newPosition[2] < enuStart[2]) {
                // rocket has landed
                Result.success(result)
            } else if (currentVelocity[2] >= 0 && newVelocity[2] <= 0) {
                // switch to parachute parameters when rocket reached its apex
                calculateTrajectoryRecursive(
                    currentPosition = newPosition,
                    currentVelocity = newVelocity,
                    timeAfterLaunch = timeAfterLaunch + stepSize,
                    coefficientOfDrag = parachuteDragCoefficient,
                    areaOfCrossSection = parachuteCrossSectionalArea,
                    result = result
                )
            } else {
                // continue with parameters from current iteration
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


        return Result.success(
            calculateTrajectoryRecursive(
                currentPosition = enuStart,
                currentVelocity = zeroVector,
                timeAfterLaunch = 0.0,
                coefficientOfDrag = dragCoefficient,
                areaOfCrossSection = crossSectionalArea,
                // SimpleLinkedList is used to avoid resizing
                result = SimpleLinkedList(Triple(initialPosition, 0.0, RocketState.ON_LAUNCH_RAIL))
            ).fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
                .toList()
        )
    }
}

