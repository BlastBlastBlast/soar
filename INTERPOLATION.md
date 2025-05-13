![Catmull-Rom Interpolation](images/catmull-rom-interpolation.svg)

This diagram illustrates the concept of Catmull-Rom interpolation. The curve segments are defined so that they pass through the control points, and the slope at each control point is defined by adjacent control points. The red line segments show linear interpolation between the points, while the blue curve segments show the Catmull-Rom splines. The Catmull-Rom splines have the advantage of creating a smooth transition between segments, while the linear segments are only continuous.


![Interpolation in 2D](images/interpolation-2d.svg)

This diagram shows how interpolation works in a 2D space. One can, given 16 control points in a regularly spaced grid, interpolate values within the square defined by $x_1 \leq x \leq x_2$ and $y_1 \leq y \leq y_2$. The interpolation is done in two steps. First, four interpolations are performed at the desired $x$ from the four sets of four points that share $y$-values. The value at $(x, y_0)$, for instance, is interpolated from $\{(x_0, y_0), (x_1, y_0), (x_2, y_0), (x_3, y_0)\}$. Then the four resulting points are interpolated to get the final value at the desired coordinates $(x, y)$.


![Vertical Interpolation](images/vertical-interpolation.svg)
