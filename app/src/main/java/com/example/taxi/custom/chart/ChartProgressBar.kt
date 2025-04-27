package com.example.taxi.custom.chart


import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.taxi.R
import com.example.taxi.domain.model.statistics.StatisticsResponse
import com.example.taxi.domain.model.statistics.StatisticsResponseValue


class ChartProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var barHeight: Int = 150 // Default qiymat
    private var selectedTextView: TextView? = null
    private var selectedContainer: LinearLayout? = null
    private var onItemSelected: ((String,StatisticsResponseValue) -> Unit)? = null
    init {
        orientation = HORIZONTAL
        gravity = BOTTOM
        val padding = (16 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)

        context.obtainStyledAttributes(attrs, R.styleable.ChartProgressBar).apply {
            barHeight = getDimensionPixelSize(R.styleable.ChartProgressBar_barHeight, 150)
            recycle()
        }
    }

    fun setData(
        dataList: List<StatisticsResponse<StatisticsResponseValue>>,
        onItemSelected: ((String, StatisticsResponseValue) -> Unit)?,
        layoutSelector: Int) {
        this.onItemSelected = onItemSelected
        removeAllViews() // Avvalgi viewlarni tozalash
        val maxValue = dataList.maxOf { it.data.totalSum }
        for ((index, data) in dataList.withIndex()) {

            val progressBarView =
                LayoutInflater.from(context).inflate(layoutSelector, null)
            val textViewPrice = progressBarView.findViewById<TextView>(R.id.textViewPrice)
            val progressBar = progressBarView.findViewById<View>(R.id.progressBar)
            val textViewDate = progressBarView.findViewById<TextView>(R.id.textViewDate)
            val container = progressBarView.findViewById<LinearLayout>(R.id.liner_container)

            progressBarView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    val textViewsHeight = (textViewPrice?.height ?: 0) + textViewDate.height
                    val adjustedBarHeight = barHeight - textViewsHeight


                    val targetHeight = ((adjustedBarHeight * (data.data.totalSum.toFloat() / maxValue)) * 0.9).toInt()


                    animateHeight(progressBar, targetHeight)

                    progressBarView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })



            val params = progressBar.layoutParams

            progressBar.layoutParams = params


            textViewPrice?.text = formatMoneyNumberPlate(data.data.totalSum.toString())
            textViewDate.text = if(data.period_between_date.length > 13) convertDateRangeToUzbek(data.period_between_date) else getLastDay(data.period_between_date)

            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            )
            layoutParams.weight = 1f
            layoutParams.leftMargin =  8
            layoutParams.rightMargin =  8

            addView(progressBarView, layoutParams)

            container.setOnClickListener {
                handleContainerClick(container)
                onItemSelected?.invoke(data.period_between_date,data.data)
            }

            if (index == dataList.size - 1) {
                handleContainerClick(container)
                onItemSelected?.invoke(data.period_between_date,data.data)
            }
            // TextView uchun click listener qo'shish

        }
    }


    private fun convertDateRangeToUzbek(range: String): String {
        val monthsUzbek = mapOf(
            "01" to "yan",
            "02" to "fev",
            "03" to "mart",
            "04" to "apr",
            "05" to "may",
            "06" to "iyun",
            "07" to "iyul",
            "08" to "avg",
            "09" to "sent",
            "10" to "okt",
            "11" to "noy",
            "12" to "dek"
        )

        val dates = range.split(" - ")
        val startDate = dates[0]
        val endDate = dates[1]

        val startDay = startDate.substring(8, 10)
        val startMonth = startDate.substring(5, 7)

        val endDay = endDate.substring(8, 10)
        val endMonth = endDate.substring(5, 7)

        val monthName = monthsUzbek[startMonth] ?: "no"
        return "$startDay-$endDay\n$monthName"
    }



    private fun getLastDay(day: String): String {
        return day.split("-").last()
    }

    fun formatMoneyNumberPlate(input: String): String {
        val regex = "(\\d)(?=(\\d{3})+$)".toRegex()
        return input.replace(regex, "$1 ")
    }
    private fun animateHeight(view: View, targetHeight: Int) {
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val params = view.layoutParams
            params.height = animatedValue
            view.layoutParams = params
        }
        animator.duration = 1000 // Animatsiya davomiyligi (1 soniya)
        animator.interpolator = DecelerateInterpolator() // Sekinlashuvchi interpolator
        animator.start()
    }

    private fun handleContainerClick(container: LinearLayout) {
        selectedContainer?.let {
            it.isSelected = false
            updateContainerBackground(it, false)
        }


        container.isSelected = true
        updateContainerBackground(container, true)


        selectedContainer = container
    }

    private fun updateContainerBackground(container: LinearLayout, isSelected: Boolean) {
        val progressBar = container.findViewById<View>(R.id.progressBar)

        progressBar.background = if (isSelected) {
            ContextCompat.getDrawable(context, R.drawable.selected_progress_drawable)
        } else {
            ContextCompat.getDrawable(context, R.drawable.custom_progress_drawable)
        }
    }
}