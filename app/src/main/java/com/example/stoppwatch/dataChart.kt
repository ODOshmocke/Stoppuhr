package com.example.stoppwatch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import co.yml.charts.ui.linechart.model.SelectionHighlightPoint
import co.yml.charts.ui.linechart.model.SelectionHighlightPopUp
import co.yml.charts.ui.linechart.model.ShadowUnderLine
import androidx.compose.ui.Modifier



val pointsData: List<Point> = listOf(
    Point(1f, 2f),
    Point(3f, 4f),
    Point(5f, 6f)
)


val xAxisData = AxisData.Builder()
    .axisStepSize(30.dp)
    .steps(pointsData.size)
    .labelData { index -> index.toString() }
    .build()

val yAxisData = AxisData.Builder()
    .axisStepSize(30.dp)
    .steps(pointsData.size)
    .labelData { index -> index.toString() }
    .build()

val lineData = LineChartData(
    linePlotData = LinePlotData(
        lines = listOf(
            Line(
                dataPoints = pointsData,
                lineStyle = LineStyle(),
                IntersectionPoint(),
                SelectionHighlightPoint(),
                ShadowUnderLine(),
                SelectionHighlightPopUp()
            )
        ),
    ),

    xAxisData = xAxisData,
    yAxisData = yAxisData,
    gridLines = GridLines(),

)

@Composable
fun show() {
    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        lineChartData = lineData
    )
}