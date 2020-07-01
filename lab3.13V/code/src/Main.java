import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Main {

    private static final double mu0 = 1.0;

    static class CVector {

        double Hx;
        double Hy;
        double Hz;

        CVector(double Hx_, double Hy_, double Hz_)
        {
            Hx = Hx_;
            Hy = Hy_;
            Hz = Hz_;
        }

        double value()
        {
            return sqrt(Hx * Hx + Hy * Hy + Hz * Hz);
        }

    }

    private static double gradBz(double Bz1, double Bz2)
    {
        return Bz1 / Bz2;
    }

    private static double gradBxy(double Bx1, double By1, double Bx2, double By2)
    {
        double Bxy1 = sqrt(Bx1 * Bx1 + By1 * By1);
        double Bxy2 = sqrt(Bx2 * Bx2 + By2 * By2);
        return Bxy1 / Bxy2;
    }

    private static Vector<Pair<Double, Double>> deltaZ(Vector<Pair<Double, CVector>> data, double precision, Vector<Double> gBz)
    {
        int startPosition = 0;
        int endPosition = 0;
        Vector<Pair<Double, Double>> delta = new Vector<>();

        for (int i = 0; i < gBz.size() - 1; ++i)
        {
            if (abs(gBz.get(i) - gBz.get(i + 1)) <= precision && endPosition < gBz.size() - 1)
            {
                endPosition = i + 1;
            }
            else if (abs(gBz.get(i) - gBz.get(i + 1)) > precision)
            {
                Pair<Double, Double> interval = new Pair<>(data.get(startPosition).getKey(), data.get(endPosition).getKey());
                delta.addElement(interval);
                startPosition = endPosition + 1;
                endPosition = endPosition + 1;
            }
        }

        if (endPosition == gBz.size() - 1)
        {
            Pair<Double, Double> interval = new Pair<>(data.get(startPosition).getKey(), data.get(endPosition + 1).getKey());
            delta.addElement(interval);
        }

        return delta;
    }

    private static Vector<Pair<Double, Double>> theoreticalValue(Vector<Pair<Double, CVector>> data, double I, double R, double d)
    {
        Vector<Pair<Double, Double>> theoreticalValues = new Vector<>();

        for (int i = 0; i < data.size(); ++i)
        {
            double value = ((mu0 * I * R * R) / (2.0)) * (
                    ((1.0) / (Math.pow(Math.pow(data.get(i).getKey() + d / 2.0, 2.0) + R * R, (3.0 / 2.0)))) +
                            ((1.0) / (Math.pow(Math.pow(data.get(i).getKey() + d / 2.0 - d, 2.0) + R * R, (3.0 / 2.0)))));
            Pair<Double, Double> pair = new Pair<>(data.get(i).getKey(), value);
            theoreticalValues.add(pair);
        }

        return theoreticalValues;
    }

    public static void main(String[] args) throws IOException {
        Scanner consoleIn = new Scanner(System.in).useLocale(Locale.US);
        String fileName;
        double I = 0.0;
        double R = 0.0;
        double d = 0.0;
        int equalR;
        System.out.println("Enter the name of the input file.");
        fileName = consoleIn.nextLine();
        System.out.println("R1 = R2? (enter 1 if it's true and 0 in other case)");
        equalR = consoleIn.nextInt();
        if (equalR == 1)
        {
            System.out.println("Enter I, R and d values.");
            I = consoleIn.nextDouble();
            R = consoleIn.nextDouble();
            d = consoleIn.nextDouble();
        }

        Scanner input = new Scanner(new FileReader(fileName)).useLocale(Locale.US);

        Vector<Pair<Double, CVector>> data = new Vector<>();

        double z;

        double Hx;
        double Hy;
        double Hz;

        for (int i = 0; i < 9; ++i)
        {
            input.nextLine();
        }

        while (input.hasNextLine())
        {
            if (input.hasNextDouble())
                input.nextDouble();
            else break;
            input.nextDouble();
            z = input.nextDouble();
            Hx = input.nextDouble();
            Hy = input.nextDouble();
            Hz = input.nextDouble();
            CVector values = new CVector(Hx, Hy, Hz);
            Pair<Double, CVector> pair = new Pair(z, values);
            data.add(pair);
        }

        Comparator<Pair<Double, CVector>> comparator = new Comparator<Pair<Double, CVector>>() {
            @Override
            public int compare(Pair<Double, CVector> o1, Pair<Double, CVector> o2) {
                if (o1.getKey() < o2.getKey()) return -1;
                return 0;
            }
        };

        data.sort(comparator);

        FileWriter graph = new FileWriter("graph" + fileName.substring(fileName.length() - 5));

        graph.write("z value\n");

        for (int i = 0; i < data.size(); ++i)
        {
            graph.write(String.valueOf(data.get(i).getKey()).replace('.', ',') + "\n");
        }

        graph.write("\n");
        graph.write("|B| value\n");

        for (int i = 0; i < data.size(); ++i)
        {
            graph.write(String.valueOf(data.get(i).getValue().value()).replace('.', ',') + "\n");
        }

        graph.close();

        Vector<Double> gBz = new Vector<>();
        FileWriter gradbz = new FileWriter("gradBz" + fileName.substring(fileName.length() - 5));

        gradbz.write("grad Bz value\n");

        for (int i = 0; i < data.size() - 1; ++i)
        {
            double value = gradBz(data.get(i).getValue().Hz, data.get(i + 1).getValue().Hz);
            gradbz.write(String.valueOf(value).replace('.', ',') + "\n");
            gBz.add(value);
        }

        gradbz.close();

        FileWriter gradbxy = new FileWriter("gradBxy" + fileName.substring(fileName.length() - 5));

        gradbxy.write("grad Bxy value\n");

        for (int i = 0; i < data.size() - 1; ++i)
        {
            double value = gradBxy(data.get(i).getValue().Hx, data.get(i).getValue().Hy,
                    data.get(i + 1).getValue().Hx, data.get(i + 1).getValue().Hy);
            gradbxy.write(String.valueOf(value).replace('.', ',') + "\n");
        }

        gradbxy.close();

        FileWriter dZ = new FileWriter("intervals_delta_z" + fileName.substring(fileName.length() - 5));
        dZ.write("delta Z intervals\n");

        for (int i = 1; i <= 10; ++i)
        {
            dZ.write("precision: " + i + "%\n");
            Vector<Pair<Double, Double>> intervals = deltaZ(data, i / 100.0, gBz);
            for (int j = 0; j < intervals.size(); ++j)
            {
                double diff = intervals.get(j).getValue() - intervals.get(j).getKey();
                dZ.write("from " + String.valueOf(intervals.get(j).getKey()) + " to " +
                        String.valueOf(intervals.get(j).getValue()) + " |------------| difference: " + String.valueOf(diff) + "\n");
            }
        }

        dZ.close();

        if (equalR == 1)
        {
            FileWriter theovalues = new FileWriter("theovalues" + fileName.substring(fileName.length() - 5));
            Vector<Pair<Double, Double>> theoreticalValues = theoreticalValue(data, I, R, d);
            theovalues.write("z value\n");
            for (int i = 0; i < theoreticalValues.size(); ++i)
            {
                theovalues.write(String.valueOf(theoreticalValues.get(i).getKey()).replace('.', ',') + "\n");
            }
            theovalues.write("\n|B| value\n");
            for (int i = 0; i < theoreticalValues.size(); ++i)
            {
                theovalues.write(String.valueOf(theoreticalValues.get(i).getValue()).replace('.', ',') + "\n");
            }

            theovalues.close();
        }

    }
}
