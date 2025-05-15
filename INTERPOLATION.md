# Methodology for Interpolation

## Table of Contents
- [Overview](#overview)
- [Motivation](#motivation)
- [Strategy](#strategy)
- [1D Uniform Interpolation](#1d-uniform-interpolation)
- [2D Uniform Interpolation](#2d-uniform-interpolation)
- [1D Non-Uniform Interpolation](#1d-non-uniform-interpolation)
- [Extrapolating at the boundaries of the data](#extrapolating-at-the-boundaries-of-the-data)
- [3D Mixed Interpolation](#3d-mixed-interpolation)
- [Notes](#notes)

## Overview
Interpolation is a method used to estimate unknown values that fall within the range of known data points. It is particularly useful in various fields such as meteorology, computer graphics, and numerical analysis. The following sections describe the interpolation methods used in SOAR.

## Motivation
To simulate the trajectory of a rocket, we need to know the wind speed and direction, as well as air temperature, at various altitudes. The data we receive from MET is generally not available at the exact coordinates we need, so we must interpolate between the available data points.

The interpolation methods used in SOAR are based on the Catmull-Rom spline, which is a type of cubic Hermite spline. This method is particularly well-suited for interpolating data points in a smooth and continuous manner while being relatively computationally cheap. The Catmull-Rom spline is defined by four control points, and it ensures that the result passes through these points while maintaining a smooth transition between them.

## Strategy
We combine two variants of Catmull-Rom interpolation to estimate altitude, air temperature and the velocity components of the wind at and between the various isobaric levels in the GRIB data. The first variant allows us to get an estimate at a given pair of $(x, y)$ coordinates where the control points are uniformly spaced, while the second variant is a one-dimensional approach that does not require uniform spacing between the control points.

The uniform variant is used in the horizontal plane, since data from the IsobaricGRIB API is uniformly distributed with respect to latitude and longitude. However, the vertical distribution of data points is not uniform, so we need to use the non-uniform variant to interpolate in this direction.

## 1D Uniform Interpolation

![Catmull-Rom Interpolation](images/catmull-rom-interpolation_t.svg)

This diagram illustrates the concept of Catmull-Rom interpolation. The $x$-axis typically represents distance, while the $y$-axis represents the metric we want to approximate, for instance altitude or wind speed. The curve segments of the Catmull-Rom spline are defined so that they pass through the control points, and the slope of a curve segment at an end point is defined by the adjacent control points.

The red spline is constructed from linear interpolation between the points, while the blue curves show the Catmull-Rom spline. The Catmull-Rom spline has the advantage of creating a smooth transition between segments, while the linear spline is only continuous at the control points.

The process of Catmull-Rom interpolation can be represented as a product of vectors and matrices. If $p_0, p_1, p_2, p_3$ are the control points with a distance of 1 between adjacent points, then we represent the spline segment between $p_1$ and $p_2$ as a function $f_1$ of $t$ on the interval $[0, 1]$:

$$
f_1(t) = \vec{t}^T \cdot M \cdot \vec{p}
$$

$$
\vec{t} = 
\begin{pmatrix} 
1 \\ 
t \\ 
t^2 \\
t^3 
\end{pmatrix}
, 
M =
\frac{1}{2}
\begin{pmatrix}
0 & 2 & 0 & 0 \\
-1 & 0 & 1 & 0 \\
2 & -5 & 4 & -1 \\
-1 & 3 & -3 & 1
\end{pmatrix}
,
\vec{p} =
\begin{pmatrix}
p_0 \\
p_1 \\
p_2 \\
p_3
\end{pmatrix}
$$

## 2D Uniform Interpolation

![Interpolation in 2D](images/interpolation-2d_t.svg)

This diagram shows how uniform interpolation works in 2D space. One can, given 16 control points in a regularly spaced grid, interpolate values within the square defined by $t^x_1 \leq t^x \leq t^x_2$ and $t^y_1 \leq t^y \leq t^y_2$. The interpolation is done in two steps. First, four interpolations are performed at the desired $t^x$ from the four sets of four points that share $t^y$-values. The value at $(t^x, t^y_0)$, for instance, is interpolated from the values at $(t^x_0, t^y_0), (t^x_1, t^y_0), (t^x_2, t^y_0)$ and $(t^x_3, t^y_0)$. Then the four resulting points are interpolated to get the final value at the desired coordinates $(t^x, t^y)$.

As with the one-dimensional case, the interpolation can be expressed as a product of vectors and matrices. Given 16 control points $p_{i,j}$, where $i$ and $j$ are the indices of the grid, and adjacent points along the same axis are spaced with a distance of 1, then we can represent the interpolation as a function $f_2$ of $\delta t^x = t^x - t^x_1$ and $\delta t_y = t^y - t^y_1$ on the interval $[0, 1]$:

$$
f_2(t^x, t^y) = \vec{t^y}^T \cdot M \cdot P \cdot M^T \cdot \vec{t^x}
$$

$$
\vec{t^x} =
\begin{pmatrix}
1 \\
\delta t^x \\
\delta {t^x}^2 \\
\delta {t^x}^3
\end{pmatrix}
,
\vec{t^y} =
\begin{pmatrix}
1 \\
\delta t^y \\
\delta {t^y}^2 \\
\delta {t^y}^3
\end{pmatrix}
,
P =
\begin{pmatrix}
p_{0,0} & p_{1,0} & p_{2,0} & p_{3,0} \\
p_{0,1} & p_{1,1} & p_{2,1} & p_{3,1} \\
p_{0,2} & p_{1,2} & p_{2,2} & p_{3,2} \\
p_{0,3} & p_{1,3} & p_{2,3} & p_{3,3}
\end{pmatrix}
$$

The matrix $M$ is the same as in the one-dimensional case.

A nice property of this method of interpolation is that the order of the axes can be swapped, so that we can first interpolate in the $y$-direction and then in the $x$-direction. We can observe this from the following equation:

$$
f_2(t^x, t^y) = (f_2(t^x, t^y))^T = \vec{t^x}^T \cdot M \cdot P^T \cdot M^T \cdot \vec{t^y}
$$

This equation shows that we can swap $\vec{t^x}$ and $\vec{t^y}$, and as long as we transpose the matrix $P$, we still get the same result. Since transposing $P$ means swapping the $x$ and $y$ coordinates of all its entries, then the above expression is analogous to first interpolating along the $y$-axis and then along the $x$-axis. This shows that our approach of two-dimensional interpolation does not depend on the choice of axis ordering.

## 1D Non-Uniform Interpolation

The principle of non-uniform interpolation is similar to the uniform case, but the control points are not evenly spaced. Instead of using a matrix approach, we use a recursive method to compute the interpolated value. The algorithm is based on the following formula:

$$
f^*(t) = \frac{t_{2} - t}{t_{2} - t_{1}} r_{1} + \frac{t - t_{1}}{t_{2} - t_{1}} r_{2}
$$

$$
r_{1} = \frac{t_{2} - t}{t_{2} - t_{0}} q_{1} + \frac{t - t_{0}}{t_{2} - t_{0}} q_{2}
$$

$$
r_{2} = \frac{t_{3} - t}{t_{3} - t_{1}} q_{2} + \frac{t - t_{1}}{t_{3} - t_{1}} q_{3}
$$

$$
q_{1} = \frac{t_{1} - t}{t_{1} - t_{0}} p_{0} + \frac{t - t_{0}}{t_{1} - t_{0}} p_{1}
$$

$$
q_{2} = \frac{t_{2} - t}{t_{2} - t_{1}} p_{1} + \frac{t - t_{1}}{t_{2} - t_{1}} p_{2}
$$

$$
q_{3} = \frac{t_{3} - t}{t_{3} - t_{2}} p_{2} + \frac{t - t_{2}}{t_{3} - t_{2}} p_{3}
$$

The points $p_0,p_1,p_2,p_3$ are as usual the control points, and $t_0,t_1,t_2,t_3$ are the values that the $p_i$'s are associate with.

## Extrapolating at the boundaries of the data

![Extrapolation](images/extrapolation_t.svg)

This diagram shows how we extrapolate the values at the boundaries of the available data. We use the two closest control points to the boundary at $t_1$ and $t_2$ and extrapolate the value at $t_0$ so that the slope from the value at $t_0$ to the value at $t_1$ is the same as the slope from the value at $t_1$ to the value at $t_2$. The same is done for the other boundary.

## 3D Mixed Interpolation

![Vertical Interpolation](images/vertical-interpolation_t.svg)

This diagram shows how 3D interpolation works. We compute four control points at the target $t^x$ and $t^y$, each on a different isobaric level, ground level, or a layer extrapolated from the available data. The value at $(t^x, t^y, t^z)$ is then computed by interpolating from those four control points.

The control points are obtained using 2D interpolation, as shown in the previous diagram. The altitudes $z_0, z_1, z_2$ and $z_3$ of the four control points are also calculated using 2D interpolation.

When we have the four control points, we use the same recursive method as in the 1D non-uniform interpolation to compute the value at $(t^x, t^y, z)$.

## Notes

- The 1D non-uniform interpolation method comes from a paper by P. J. Barry and R. N. Goldman, "Recursive evaluation algorithm for a class of Catmull-Rom splines". https://doi.org/10.1145/378456.378511
- The interpolation methods are implemented in the 'IsobaricInterpolator' class, which is part of the 'domain' package.
