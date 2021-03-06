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

private const val warumps = 5
private const val runs = 10

fun main() {
    CategoryPlot(
        dataSet,
        CategoryAxis("Day").apply {
            categoryLabelPositions = CategoryLabelPositions.UP_45
        },
        NumberAxis("time in s"),
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
        repeat(warumps) { runTimed { runActual(index) } }
            .let {
                (0 until runs)
                    .map { runTimed { runActual(index) } }
                    .reduce(Duration::plus)
                    .div(runs)
            }
}