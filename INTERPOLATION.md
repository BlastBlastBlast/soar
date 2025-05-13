![Catmull-Rom Interpolation](images/catmull-rom-interpolation.svg)

This diagram illustrates the concept of Catmull-Rom interpolation. The $x$-axis represents distance, while the $y$-axis represents the metric we want to approximate, for instance altitude or wind speed. The curve segments of the Catmull-Rom spline are defined so that they pass through the control points, and the slope of a curve segment at an end point is defined by the adjacent control points. The red spline is constructed from linear interpolation between the points, while the blue curves show the Catmull-Rom spline. The Catmull-Rom spline has the advantage of creating a smooth transition between segments, while the linear spline is only continuous at the control points.

The process of Catmull-Rom interpolation can be represented as a product of vectors and matrices. If $p_0, p_1, p_2, p_3$ are control points, then we represent the interpolation as a function $f$ of $t$ on the interval $[0, 1]$:

$$
f(t) = \vec{t}^T \cdot M \cdot \vec{p}
$$

where 

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
\begin{pmatrix}
0 & 1 & 0 & 0 \\
-\frac{1}{2} & 0 & \frac{1}{2} & 0 \\
1 & -\frac{5}{2} & 2 & -\frac{1}{2} \\
-\frac{1}{2} & \frac{3}{2} & -\frac{3}{2} & \frac{1}{2}
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

![Interpolation in 2D](images/interpolation-2d.svg)

This diagram shows how interpolation works in a 2D space. One can, given 16 control points in a regularly spaced grid, interpolate values within the square defined by $x_1 \leq x \leq x_2$ and $y_1 \leq y \leq y_2$. The interpolation is done in two steps. First, four interpolations are performed at the desired $x$ from the four sets of four points that share $y$-values. The value at $(x, y_0)$, for instance, is interpolated from the values at $(x_0, y_0), (x_1, y_0), (x_2, y_0)$ and $(x_3, y_0)$. Then the four resulting points are interpolated to get the final value at the desired coordinates $(x, y)$.

![Vertical Interpolation](images/vertical-interpolation.svg)
This diagram shows how 3D interpolation works. We compute four control point in the $z$-direction, each at the given $(x, y)$-coordinates on some isobaric level, ground level, or a layer extrapolated from the available data. The value at $(x, y, z)$ is then computed by interpolating from those four control points.

The control points are obtained using 2D interpolation, as shown in the previous diagram. The altitudes $z_0, z_1, z_2$ and $z_3$ of the four control points are also calculated using 2D interpolation. 