package com.aware.plugin.motionacceleration;

/**
 * Created by CLUO29 on 3/20/2015.
 */

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.plugin.motionacceleration.Provider.MotionAcceleration_Data;
import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {
    private static TextView Text_a_0;
    private static TextView Text_a_1;
    private static TextView Text_a_2;
    private static TextView Text_g_0;
    private static TextView Text_g_1;
    private static TextView Text_g_2;
    private static LinearLayout A_plot;
    public ContextCard(){};
    public View getContextCard(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.inflate(R.layout.card, null);
        Text_a_0 = (TextView) card.findViewById(R.id.a_0);
        Text_a_1 = (TextView) card.findViewById(R.id.a_1);
        Text_a_2 = (TextView) card.findViewById(R.id.a_2);
        Text_g_0 = (TextView) card.findViewById(R.id.g_0);
        Text_g_1 = (TextView) card.findViewById(R.id.g_1);
        Text_g_2 = (TextView) card.findViewById(R.id.g_2);
        Cursor latest = context.getContentResolver().query(MotionAcceleration_Data.CONTENT_URI, null, null, null, MotionAcceleration_Data.TIMESTAMP + " DESC LIMIT 1");
        if (latest != null && latest.moveToFirst()) {
            //frequency.setText("There was data");

            Text_a_0.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_0))) + " m/s^2");
            Text_a_1.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_1))) + " m/s^2");
            Text_a_2.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_2))) + " m/s^2");
            Text_g_0.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.G_VALUES_0))) + " rad/s");
            Text_g_1.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.G_VALUES_1))) + " rad/s");
            Text_g_2.setText(String.format("%.1f", latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.G_VALUES_2))) + " rad/s");
        } else {
            Text_a_0.setText("no data");

        }
        if (latest != null && !latest.isClosed()) latest.close();

        A_plot = (LinearLayout) card.findViewById(com.aware.plugin.motionacceleration.R.id.A_plot);
        A_plot.removeAllViews();
        A_plot.addView(drawGraph(context));

        return card;
    }
    private static GraphicalView drawGraph(Context context ) {

        GraphicalView mChart;
        long delta_time = System.currentTimeMillis()-(1 * 60 * 1000);
        XYSeries frequency_series = new XYSeries("Frequency (Hz)");
        Cursor latest = context.getContentResolver().query(MotionAcceleration_Data.CONTENT_URI, null, MotionAcceleration_Data.TIMESTAMP + " > " + delta_time, null, MotionAcceleration_Data.TIMESTAMP + " ASC");
        if (latest != null && latest.moveToFirst()) {
            do {
                double sum = latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_0))*latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_0));
                sum = sum + latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_1))*latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_1));
                sum = sum + latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_2))*latest.getDouble(latest.getColumnIndex(MotionAcceleration_Data.A_VALUES_2));
                sum = Math.sqrt(sum);
                frequency_series.add(latest.getPosition(), sum);
            } while(latest.moveToNext() );
        }
        if (latest != null && !latest.isClosed()) latest.close();

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(frequency_series);


        //setup frequency
        XYSeriesRenderer frequency_renderer = new XYSeriesRenderer();
        frequency_renderer.setColor(Color.BLUE);
        frequency_renderer.setPointStyle(PointStyle.POINT);
        frequency_renderer.setDisplayChartValues(false);
        frequency_renderer.setLineWidth(2);
        frequency_renderer.setFillPoints(true);


        //Setup graph
        XYMultipleSeriesRenderer dataset_renderer = new XYMultipleSeriesRenderer();
        dataset_renderer.setChartTitle("Ambient noise");
        dataset_renderer.setLabelsColor(Color.BLACK);
        dataset_renderer.setDisplayValues(true);
        dataset_renderer.setFitLegend(false);
        dataset_renderer.setXLabelsColor(Color.BLACK);
        dataset_renderer.setYLabelsColor(0, Color.BLACK);
        dataset_renderer.setLegendHeight(0);
        dataset_renderer.setYLabels(0);
        dataset_renderer.setYTitle("Hz & dB");
        dataset_renderer.setXTitle("Time");
        dataset_renderer.setZoomButtonsVisible(false);
        dataset_renderer.setXLabels(0);
        dataset_renderer.setPanEnabled(false);
        dataset_renderer.setShowGridY(false);
        dataset_renderer.setClickEnabled(false);
        dataset_renderer.setAntialiasing(true);
        dataset_renderer.setAxesColor(Color.BLACK);
        dataset_renderer.setApplyBackgroundColor(true);
        dataset_renderer.setBackgroundColor(Color.WHITE);
        dataset_renderer.setMarginsColor(Color.WHITE);

        //add plot renderers to main renderer
        dataset_renderer.addSeriesRenderer(frequency_renderer);


        //put everything together
        mChart = ChartFactory.getLineChartView(context, dataset, dataset_renderer);

        return mChart;
    }
}