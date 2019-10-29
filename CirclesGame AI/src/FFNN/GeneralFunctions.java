package FFNN;

import java.text.DecimalFormat;
import java.util.LinkedList;

public class GeneralFunctions {
    public static void showVectorValues(String label, LinkedList<Double> v)
    {
        System.out.println(label + " ");
        for (int i = 0; i < v.size(); i++)
        {
            System.out.println(v.get(i) + " ");
        }
        System.out.println();
    }

    public static String formatDoubleToString(double number)
    {
        return new DecimalFormat("##########.########").format(number).toString();
    }
}
