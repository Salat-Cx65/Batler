package com.s_komlach.batler.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dataconnectiontoggler.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;

import com.s_komlach.batler.R;

import java.util.ArrayList;
import java.util.Date;

public class InfoFragment extends Fragment implements OnChartValueSelectedListener {
    private LineChart mChart;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info, container, false);


        mChart = (LineChart) view.findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable value highlighting
        mChart.setHighlightEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        // add data
        setData();



        mChart.animateX(2500);

        //Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
        //l.setTypeface(tf);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
//        l.setYOffset(11f);

        XAxis xAxis = mChart.getXAxis();
        //xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(tf);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setDrawGridLines(true);


        YAxis rightAxis = mChart.getAxisRight();
        //rightAxis.setTypeface(tf);
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaxValue(100);
       // rightAxis.setStartAtZero(false);
       // rightAxis.setAxisMinValue(-100);
        rightAxis.setDrawGridLines(true);

        return view;
    }


    private void setData() {
        AsyncTask<Void, Void, Void> launchTask = new AsyncTask<Void, Void, Void>() {
            ArrayList<Entry> yVals1 = new ArrayList<Entry>();
            ArrayList<Entry> yVals2 = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();

            @Override
        protected Void doInBackground(Void... params) {

            ArrayList<Date> batLevels = Utils.getBatteryLevelKeyset(getActivity());
            int rcount = batLevels.size();

            if(rcount < 7)
                rcount = 7;


            for (int i = 0; i < rcount; i++) {
                xVals.add((i) + "");
            }


            int count = batLevels.size();

            for (int i = 0; i < count; i++) {
                double batLevel = Utils.getBatteryLevel(getActivity(), batLevels.get(i));
                int saveLevel = Utils.getBatterySaveLevel(getActivity(), batLevels.get(i));
                yVals1.add(new Entry(((float) batLevel) * 100, i));
                yVals2.add(new Entry( ((float) (batLevel * saveLevel)), i));
            }

      return null;
        }
        @Override
        protected void onPostExecute(Void p) {

            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(yVals1, "Saved usage");
            set1.setAxisDependency(AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleSize(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
//        set1.setDrawHorizontalHighlightIndicator(false);
//        set1.setVisible(false);
//        set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            LineDataSet set2 = new LineDataSet(yVals2, "Normal usage");
            set2.setAxisDependency(AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.WHITE);
            set2.setLineWidth(2f);
            set2.setCircleSize(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set2);
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }
        };
        launchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[]{});

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        //Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        //Log.i("Nothing selected", "Nothing selected.");
    }


}