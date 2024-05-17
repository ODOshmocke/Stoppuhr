package com.example.stoppwatch

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter


object TimeUnits {
    val lastWeek: LocalDate = LocalDate.now().minusWeeks(1)
    val lastMonth = LocalDate.now().minusMonths(1)
    val lastYear = LocalDate.now().minusYears(1)

    val lastMonthLength = LocalDate.now().minusMonths(1).lengthOfMonth()
    val yearLength = LocalDate.now().minusYears(1).lengthOfYear()
}


@Composable
fun LineChartShow(
    data: MutableList<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    lineWidth: Float = 4f,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    pointSize: Float = 5f,
    timeUnit: String = "month",
    amountYsteps: Int = 4,
    shiftingAmount: Float = 60f
    ) {

    var maxX by remember { mutableFloatStateOf(0f) }
    var maxY by remember { mutableFloatStateOf(0f) }


    data.forEachIndexed { index, value ->
        if (value > maxY) maxY = value
        if (index > maxX) maxX = index.toFloat()
    }

    maxY += (10f - (maxY % 10))


    Canvas(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth() // Width of the chart
            .requiredHeight(200.dp) // Height of the chart
    ) {
        val chartWidth = size.width - shiftingAmount
        val chartHeight = size.height - shiftingAmount

        val stepX = chartWidth / maxX
        val stepY = chartHeight / maxY





        drawLine(
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            color = Color.Red,
            strokeWidth = 5f
        )
        drawLine(
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
            color = Color.Red,
            strokeWidth = 5f
        )








        // ist um -5f versetzt damit die Kreisse nicht auf dem Rand liegen

        // Draw Y-axis
        drawLine(
            start = Offset(0f + shiftingAmount, 0f),
            end = Offset(0f + shiftingAmount, chartHeight),
            color = Color.DarkGray,
            strokeWidth = 5f
        )

        //Draw X-axis
        drawLine(
            start = Offset(0f + shiftingAmount, chartHeight),
            end = Offset(chartWidth + shiftingAmount, chartHeight),
            color = Color.DarkGray,
            strokeWidth = 5f
        )



        //Coordinate XSteps

        val paint = Paint()

        paint.apply {
            textSize = 30f

        }
        paint.strokeWidth = 5f
        paint.color = Color.DarkGray.toArgb()



        if (timeUnit == "week"){
            drawXLabelsAndLines(this, timeUnit, stepX, chartHeight, shiftingAmount,7, paint)
        }else if (timeUnit == "month"){
            val relevantDates =  (Math.round((TimeUnits.lastMonthLength / 8) * 1.0) / 1)

            drawXLabelsAndLines(this, timeUnit, stepX, chartHeight, shiftingAmount,TimeUnits.lastMonthLength, paint)
        }else if (timeUnit == "year"){
            drawXLabelsAndLines(this, timeUnit, stepX, chartHeight, shiftingAmount,TimeUnits.yearLength, paint)
        }



        //Coordinate YSteps

        //um den spass zu reversen
        val yLabels = createYLabel(amountYsteps, maxY)
        println(yLabels)
        for (index in 0 until  amountYsteps + 1) {
            val yLineHeight = chartHeight / amountYsteps
            drawLine(
                start = Offset(-20f + shiftingAmount, yLineHeight * index),
                end = Offset(20f + shiftingAmount, yLineHeight * index),
                color = Color.Gray,
                strokeWidth = 3f
            )

            println(yLabels[index])

            labelingYAchsis(this, yLineHeight, shiftingAmount, index, maxY, yLabels[index], paint)

            drawYGridLines(this, yLineHeight * index, chartWidth, shiftingAmount)


        }




        val path = Path()


        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = (chartHeight - (value * stepY))

            if (index == 0) {
                path.moveTo(x + shiftingAmount, y)
            } else {
                path.lineTo(x + shiftingAmount, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineWidth)
            )
            drawCircle(
                radius = pointSize,
                color = lineColor,
                center = Offset(x + shiftingAmount, y),
            )


            // Draw circles at data points
            drawCircle(
                color = pointColor,
                radius = (pointSize / 2) + 5f,
                center = Offset(x + shiftingAmount, y),
                style = Stroke(width = pointSize)
            )




        }

        // Draw line


    }
}




fun drawXLabelsAndLines(canvas: DrawScope, timeUnit: String, stepX: Float, chartHeight: Float, shiftingAmount:Float, iterations: Int, paint: Paint) {
    for(index in 0 until iterations){
        val x = index * stepX
        canvas.drawLine(
            start = Offset(x + shiftingAmount, chartHeight-20f),
            end = Offset(x + shiftingAmount, chartHeight+20f),
            color = Color.Gray,
            strokeWidth = 5f
        )
        // Add the Label


        var xMid = (paint.measureText("hkajs") / 2)

        //sonst der einfach nicht Mitte LOL
        if (index == 0){
            xMid += 20f
        }

        textXAxis(canvas, timeUnit, x, xMid, chartHeight, shiftingAmount,index, paint)

        drawXGridLines(canvas, x, chartHeight, shiftingAmount)


    }
}



fun createYLabel(amountYsteps: Int, maxY: Float): MutableList<String> {
    val labels = mutableListOf<String>()
    for (i in 0 until amountYsteps + 1) {

        if (maxY > 100){
            val label = (Math.round((maxY / amountYsteps * i) * 1) / 1).toString()
            labels.add(label)
        }
        else{
            val label = (Math.round((maxY / amountYsteps * i) * 10.0) / 10.0).toString()
            labels.add(label)
    }
        }
    labels.reverse()
    return labels
}


fun labelingYAchsis(canvas: DrawScope, yLineHeight: Float, shiftingAmount: Float, index: Int,maxY: Float, label: String, paint: Paint){

    canvas.drawContext.canvas.nativeCanvas.drawText(
        label, -80f + shiftingAmount, yLineHeight * index + paint.textSize/3 , paint
    )
}

fun textXAxis(canvas: DrawScope, timeUnit: String, x: Float, xMid: Float, chartHeight: Float, shiftingAmount:Float, index: Int, paint: Paint){

    if (timeUnit == "week"){

        val date = TimeUnits.lastWeek.plusDays(index.toLong())
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM"))

        println(formattedDate)
        canvas.drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(45f, x - xMid + shiftingAmount, chartHeight + 45f)
            drawText(
                formattedDate, x - xMid + shiftingAmount, chartHeight + 45f, paint
            )
            restore()
        }

    } else if (timeUnit == "month"){
        val date = TimeUnits.lastMonth.plusDays(index.toLong())
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM"))

        canvas.drawContext.canvas.nativeCanvas.apply {
            save()
            rotate(45f, x - xMid + shiftingAmount, chartHeight + 45f)
            drawText(
                formattedDate, x - xMid + shiftingAmount, chartHeight + 45f, paint
            )
            restore()
        }
    }
}

fun drawXGridLines(canvas: DrawScope, x: Float, chartHeight: Float, shiftingAmount:Float) {
    canvas.drawLine(
        start = Offset(x + shiftingAmount, 0f),
        end = Offset(x + shiftingAmount, chartHeight),
        color = Color.Gray,
        strokeWidth = 2f,
        alpha = 0.3f,
    )
}

fun drawYGridLines(canvas: DrawScope, y: Float, chartWidth: Float, shiftingAmount:Float){
    canvas.drawLine(
        start = Offset(shiftingAmount, y),
        end = Offset(chartWidth + shiftingAmount, y),
        color = Color.Gray,
        strokeWidth = 2f,
        alpha = 0.3f,
    )
}


@Preview
@Composable
fun LineChartDemo() {
    val data = remember { listOf(30f, 15f, 10f, 40f, 35f, 10f, 30f, 15f, 10f, 40f,30f, 15f, 10f, 40f, 35f, 10f, 30f, 15f, 10f, 40f,30f, 15f, 10f, 40f, 35f, 10f, 30f, 15f, 10f, 40f) }
    LineChartShow(
        data = data.toMutableList(),
        )
}