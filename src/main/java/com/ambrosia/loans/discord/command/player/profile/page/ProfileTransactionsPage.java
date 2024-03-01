package com.ambrosia.loans.discord.command.player.profile.page;

import com.ambrosia.loans.database.entity.client.DClient;
import com.ambrosia.loans.discord.base.gui.client.ClientGui;
import com.ambrosia.loans.util.emerald.Emeralds;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

public class ProfileTransactionsPage extends ProfilePage {

    public ProfileTransactionsPage(ClientGui gui) {
        super(gui);
    }

    @NotNull
    public static ByteArrayOutputStream createGraph(List<DClient> clients, File file) {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        for (DClient client : clients) {
            Map<Long, Double> dataPoints = new HashMap<>();
            client.getAccountSnapshots().forEach(snapshot -> {
                long x = snapshot.getDate().toEpochMilli();
                double y = snapshot.getAccountBalance().toStacks();
                Double there = dataPoints.get(x);
                double setToY;
                if (there != null) setToY = Math.max(y, there);
                else setToY = y;
                dataPoints.put(x, setToY);
            });

            XYSeries series = new XYSeries(client.getEffectiveName(), false, false);
            List<Entry<Long, Double>> list = dataPoints.entrySet().stream()
                .sorted(Entry.comparingByKey()).toList();
            for (Entry<Long, Double> snapshot : list) {
                series.add(snapshot.getKey(), snapshot.getValue());
            }
            Instant now = Instant.now();
            Emeralds balanceNow = client.getBalance(now);
            series.add(now.toEpochMilli(), balanceNow.toStacks());
            dataset.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("Account Balance", "Date", "Stacks", dataset);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new XYStepRenderer());

        plot.setDomainAxis(axis(new DateAxis("Date")));
        plot.setRangeAxis(axis(new NumberAxis("Stacks")));

        chart.getTitle().setFont(font(50));
        chart.getLegend().setItemFont(font(40));
//        chart.getLegend().setVisible(false);
        XYStepRenderer renderer = (XYStepRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        renderer.setDefaultEntityRadius(50);
        renderer.setDefaultFillPaint(Color.BLUE);
        renderer.setDrawOutlines(true);
        renderer.setUseFillPaint(true);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesShape(0, new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            BufferedImage img = chart.createBufferedImage(2000, 1000);
//            ImageIO.write(img, "png", bytes);
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static ValueAxis axis(ValueAxis axis) {
        axis.setLabelFont(font(35));
        axis.setTickLabelFont(font(25));
        return axis;
    }

    @NotNull
    public static Font font(int size) {
        return new Font("Roboto", Font.PLAIN, size);
    }


    @Override
    public MessageCreateData makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        author(embed);
        balance(embed);
        return new MessageCreateBuilder()
            .setEmbeds(embed.build())
            .setComponents(ActionRow.of(pageBtns()))
            .build();
    }
}
