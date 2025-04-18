package com.ambrosia.loans.util.clover;

import com.ambrosia.loans.Ambrosia;
import com.ambrosia.loans.util.clover.CloverRequest.CloverTimeResolution;
import com.ambrosia.loans.util.clover.response.PlaySessionTerm;
import com.ambrosia.loans.util.clover.response.PlayerTermsResponse;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class CloverGraph {

    public static byte[] graph(String title, CloverTimeResolution resolution, PlayerTermsResponse response) {

        TimeSeries series = new TimeSeries(title);
        RegularTimePeriod lastDay = resolution.graphPast();
        for (PlaySessionTerm term : response.terms) {
            if (term.playtimeDelta > 24 * 60)
                continue;
            RegularTimePeriod currentDay = resolution.graphUnit(term.retrieved);
            while (lastDay.getSerialIndex() < currentDay.getSerialIndex()) {
                series.addOrUpdate(lastDay, 0.0);
                lastDay = lastDay.next();
            }
            lastDay = currentDay.next();

            double playtimeHours = term.playtimeDelta / 60d;
            series.addOrUpdate(currentDay, playtimeHours);
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection(series);
        theme();

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,
            "Date",
            "Hours per " + resolution.display(),
            dataset
        );

        XYPlot plot = chart.getXYPlot();

        ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setRange(0, Math.max(series.getMaxY(), resolution.upperBounds()));
        yAxis.setAutoRange(false);
        DateAxis xAxis = (DateAxis) plot.getDomainAxis();
        xAxis.setMaximumDate(new Date());
        xAxis.setAutoRange(false);

        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setPadding(new RectangleInsets(25, 10, 25, 10));
        BasicStroke solidStroke = new BasicStroke(1.0f);
        plot.setRangeGridlineStroke(solidStroke);
        plot.setDomainGridlineStroke(solidStroke);
        Color transparentGray = new Color(100, 100, 100, 150);
        plot.setRangeGridlinePaint(transparentGray);
        plot.setDomainGridlinePaint(transparentGray);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ChartUtils.writeScaledChartAsPNG(output, chart, 720, 405, 2, 2);
            return output.toByteArray();
        } catch (IOException e) {
            Ambrosia.get().logger().error("", e);
            throw new RuntimeException(e);
        }
    }

    public static void theme() {
        StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createDarknessTheme();
        theme.setExtraLargeFont(new Font("SansSerif", Font.BOLD, 18)); // Title
        theme.setLargeFont(new Font("SansSerif", Font.PLAIN, 14));     // Axis labels
        theme.setRegularFont(new Font("SansSerif", Font.PLAIN, 12));   // Tick labels
        ChartFactory.setChartTheme(theme);
    }
}
