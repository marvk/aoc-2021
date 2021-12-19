import StopWatch.Companion.runTimed
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.plot.CategoryPlot
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.Color
import java.nio.file.Paths
import kotlin.time.Duration
import kotlin.time.DurationUnit

private val dayClasses = dayClasses()

private val dataSet = dataSet()

private val values = dataSet.values()
private val min = values.minOf { it }
private val max = values.maxOf { it }

private const val expandBy = 1.1

fun main() {
    CategoryPlot(
        dataSet,
        CategoryAxis("Day").apply {
            categoryLabelPositions = CategoryLabelPositions.UP_45
//            categoryMargin = 0.0
        },
        NumberAxis("time in s"),
//        LogAxis("time in s").apply {
//            base = 10.0
//            standardTickUnits = NumberAxis.createIntegerTickUnits()
//            range = Range(min / expandBy, max * expandBy)
//        },
        BarRenderer().apply {
            setShadowVisible(false)
            barPainter = StandardBarPainter()
            setSeriesPaint(0, Color(250, 100, 100))
            setSeriesPaint(1, Color(100, 255, 150))
            itemMargin = 0.0
        }
    ).let {
        JFreeChart("Execution Times", it)
    }.also {
        ChartUtils.saveChartAsPNG(Paths.get("result.png").toFile(), it, 600, 800)
    }
}

private fun DefaultCategoryDataset.values() =
    (0 until rowCount).flatMap { row ->
        (0 until columnCount).map { column ->
            getValue(row, column).toDouble()
        }
    }

private fun dataSet() =
    dayClasses
        .map { it.kotlin.objectInstance as Day }
        .map { Result(it) }
        .let { results ->
            DefaultCategoryDataset().apply {
                results.forEach {
                    addValue(it.part1Duration.toDouble(DurationUnit.SECONDS), "Part 1", it.name)
                    addValue(it.part2Duration.toDouble(DurationUnit.SECONDS), "Part 2", it.name)
                }
            }
        }

private data class Result(
    val day: Day,
) {
    val name: String = day.javaClass.name
    val part1Duration by lazy { day.part1.runTimed(1) }
    val part2Duration by lazy { day.part2.runTimed(2) }

    private fun Day.Part<*>.runTimed(index: Int) =
        repeat(5) { runTimed { runActual(index) } }
            .let {
                (0 until 10).map { runTimed { runActual(index) } }.reduce(Duration::plus).div(10)
            }
}