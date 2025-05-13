# Methodology for Interpolation

## Contents
- [Overview](#overview)
- [Motivation](#motivation)
- [Strategy](#strategy)
- [One-Dimensional Interpolation](#one-dimensional-interpolation)
- [Two-Dimensional Interpolation](#two-dimensional-interpolation)
- [Three-Dimensional Interpolation](#three-dimensional-interpolation)

## Overview
Interpolation is a method used to estimate unknown values that fall within the range of known data points. It is particularly useful in various fields such as meteorology, computer graphics, and numerical analysis. The following sections describe the interpolation methods used in SOAR.

## Motivation
To simulate the trajectory of a rocket, we need to know the wind speed and direction at various altitudes. The data we receive from MET is not always available at the exact altitudes we need, so we must interpolate between the available data points. This is especially important for small rockets, which can have a wide range of altitudes during their flight.

The interpolation methods used in SOAR are based on the Catmull-Rom spline, which is a type of cubic Hermite spline. This method is particularly well-suited for interpolating data points in a smooth and continuous manner. The Catmull-Rom spline is defined by four control points, and it ensures that the result passes through these points while maintaining a smooth transition between them.

## Strategy
We combine two variants of Catmull-Rom interpolation to estimate altitude, air temperature and the velocity components of the wind at and between the various isobaric levels. The first variant allows us to get an estimate at a given pair of $(x, y)$ coordinates, while the second variant allows us to get an estimate at a given $(x, y, z)$ coordinate. The first variant is used to interpolate between the four control points at the desired $(x, y)$ coordinates, while the second variant is a one-dimensional approach does not require the control points to be equally spaced. This is important since the data we receive from MET has a non-uniform distribution of data points in the vertical direction.

## One-Dimensional Interpolation

![Catmull-Rom Interpolation](images/catmull-rom-interpolation.svg)

This diagram illustrates the concept of Catmull-Rom interpolation. The $x$-axis typically represents distance, while the $y$-axis represents the metric we want to approximate, for instance altitude or wind speed. The curve segments of the Catmull-Rom spline are defined so that they pass through the control points, and the slope of a curve segment at an end point is defined by the adjacent control points.

The red spline is constructed from linear interpolation between the points, while the blue curves show the Catmull-Rom spline. The Catmull-Rom spline has the advantage of creating a smooth transition between segments, while the linear spline is only continuous at the control points.

The process of Catmull-Rom interpolation can be represented as a product of vectors and matrices. If $p_0, p_1, p_2, p_3$ are control points, then we represent the spline segment between $p_1$ and $p_2$ as a function $f$ of $t$ on the interval $[0, 1]$:

$$
f(t) = \vec{t}^T \cdot M \cdot \vec{p}
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

## Two-Dimensional Interpolation

![Interpolation in 2D](images/interpolation-2d.svg)

This diagram shows how interpolation works in 2D space. One can, given 16 control points in a regularly spaced grid, interpolate values within the square defined by $x_1 \leq x \leq x_2$ and $y_1 \leq y \leq y_2$. The interpolation is done in two steps. First, four interpolations are performed at the desired $x$ from the four sets of four points that share $y$-values. The value at $(x, y_0)$, for instance, is interpolated from the values at $(x_0, y_0), (x_1, y_0), (x_2, y_0)$ and $(x_3, y_0)$. Then the four resulting points are interpolated to get the final value at the desired coordinates $(x, y)$.

As with the one-dimensional case, the interpolation can be expressed as a product of vectors and matrices. Given 16 control points $p_{i,j}$, where $i$ and $j$ are the indices of the grid, we can represent the interpolation as a function $f$ of $t_x$ and $t_y$ on the interval $[0, 1]$:

$$
f(t_x, t_y) = \vec{t_x}^T \cdot M \cdot P \cdot M^T \cdot \vec{t_y}
$$

$$
\vec{t_x} =
\begin{pmatrix}
1 \\
t_x \\
t_x^2 \\
t_x^3
\end{pmatrix}
,
\vec{t_y} =
\begin{pmatrix}
1 \\
t_y \\
t_y^2 \\
t_y^3
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

## Three-Dimensional Interpolation

![Vertical Interpolation](images/vertical-interpolation.svg)
This diagram shows how 3D interpolation works. We compute four control point in the $z$-direction, each at the given $(x, y)$-coordinates on some isobaric level, ground level, or a layer extrapolated from the available data. The value at $(x, y, z)$ is then computed by interpolating from those four control points.

The control points are obtained using 2D interpolation, as shown in the previous diagram. The altitudes $z_0, z_1, z_2$ and $z_3$ of the four control points are also calculated using 2D interpolation. 