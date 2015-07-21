package com.example.kimmy.glasstest;

/**
 * Created by Karl on 2/11/2015.
 *
 * Stores all values for signal to run.
 *
 * May need to add additional fields later.
 *
 */
public class Signal
{
    int portNumber;
    boolean continuousMode;
    double[] xScale;
    double[] xData;
    double[] yScale;
    double[] yData;

    String name;

    public int getPortNumber()
    {
        return portNumber;
    }

    public void setPortNumber(int portNum)
    {
        portNumber = portNum;
    }

    boolean getMode(){
        return continuousMode;
    }

    void setMode(boolean contMode)
    {
        continuousMode = contMode;
    }

    void setXScale(double[] xAxis)
    {
        for (int x = 0; x < xAxis.length; x++) {
            xScale[x] = xAxis[x];
        }
    }

    double[] getXScale()
    {
        return xScale;
    }

    void setYScale(double[] yAxis)
    {
        for(int x = 0; x < yAxis.length; x++)
        {
            yScale[x] = yAxis[x];
        }
    }

    double[] getYScale()
    {
        return yScale;
    }

    String getName()
    {
        return name;
    }

    void setName(String signalName)
    {
        name = signalName;
    }
}
