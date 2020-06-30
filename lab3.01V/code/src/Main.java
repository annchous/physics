import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.*;

public class Main {

    static class CPoint {
        double x;
        double y;
        double v;

        CPoint(double x_, double y_, double v_)
        {
            x = x_;
            y = y_;
            v = v_;
        }
    }

    static class CField {
        double x;
        double y;
        double x_value;
        double y_value;

        CField(double x_, double y_, double x_v, double y_v)
        {
            x = x_;
            y = y_;
            x_value = x_v;
            y_value = y_v;
        }

        double getTension()
        {
            return sqrt(x_value * x_value + y_value * y_value);
        }
    }

    private static double getDistance(double x1, double x2, double y1, double y2)
    {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private static int getNearestXBefore(Vector<CPoint> points, double x, double y)
    {
        double curMaxD = 1e18;
        double nextD;
        int c = -1;
        for (int i = 0; i < points.size(); ++i) {
            if ((nextD = getDistance(points.get(i).x, x, points.get(i).y, y)) < curMaxD && (points.get(i).x < x))
            {
                curMaxD = nextD;
                c = i;
            }
        }
        return c;
    }

    private static int getNearestXAfter(Vector<CPoint> points, double x, double y)
    {
        double curMaxD = 1e18;
        double nextD;
        int c = -1;
        for (int i = 0; i < points.size(); ++i) {
            if ((nextD = getDistance(points.get(i).x, x, points.get(i).y, y)) < curMaxD && (points.get(i).x > x))
            {
                curMaxD = nextD;
                c = i;
            }
        }
        return c;
    }

    private static int getNearestYBefore(Vector<CPoint> points, double x, double y)
    {
        double curMaxD = 1e18;
        double nextD;
        int c = -1;
        for (int i = 0; i < points.size(); ++i) {
            if ((nextD = getDistance(points.get(i).x, x, points.get(i).y, y)) < curMaxD && (points.get(i).y < y))
            {
                curMaxD = nextD;
                c = i;
            }
        }
        return c;
    }

    private static int getNearestYAfter(Vector<CPoint> points, double x, double y)
    {
        double curMaxD = 1e18;
        double nextD;
        int c = -1;
        for (int i = 0; i < points.size(); ++i) {
            if ((nextD = getDistance(points.get(i).x, x, points.get(i).y, y)) < curMaxD && (points.get(i).y > y))
            {
                curMaxD = nextD;
                c = i;
            }
        }
        return c;
    }

    private static void calculation(Vector<CPoint> points, Vector<CField> field, ArrayList<Double> tensions)
    {
        for (double y = -5.0; y <= 5.0; y += 0.1)
        {
            for (double x = -8.0; x <= 8.0; x += 0.1)
            {
                int xBefore = getNearestXBefore(points, x, y);
                int xAfter = getNearestXAfter(points, x, y);
                int yBefore = getNearestYBefore(points, x, y);
                int yAfter = getNearestYAfter(points, x, y);

                if (xBefore == -1) xBefore = 0;
                if (xAfter == -1) xAfter = 0;
                if (yBefore == -1) yBefore = 0;
                if (yAfter == -1) yAfter = 0;

                double diffValue1 = points.get(xAfter).v - points.get(xBefore).v;
                double diffX = points.get(xAfter).x - points.get(xBefore).x;

                double diffValue2 = points.get(yAfter).v - points.get(yBefore).v;
                double diffY = points.get(yAfter).y - points.get(yBefore).y;

                field.add(new CField(x, y, diffValue1 / diffX, diffValue2 / diffY));
                tensions.add(field.lastElement().getTension());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner consoleIn = new Scanner(System.in);
        String fileName;
        System.out.println("Enter the name of the input file.");
        fileName = consoleIn.nextLine();
        Scanner input = new Scanner(new FileReader(fileName)).useLocale(Locale.US);

        double x;
        double y;
        double v;
        Vector<CPoint> points = new Vector<CPoint>();
        Vector<CField> field = new Vector<CField>();
        ArrayList<Double> tensions = new ArrayList<>();

        String line;

        for (int i = 0; i < 9; ++i)
        {
            line = input.nextLine();
        }

        while (input.hasNextLine())
        {
            if (input.hasNextDouble())
                x = input.nextDouble();
            else break;
            y = input.nextDouble();

            line = input.next();
            String val = line.split("([+-])")[0];
            if (val.length() > 1 && val.charAt(val.length() - 1) == 'E')
            {
                val += line.charAt(val.length()) + line.split("([+-])")[1];
                v = Double.parseDouble(val);
            }
            else {
                v = Double.parseDouble(val);
            }

            points.add(new CPoint(x, y, v));
        }
        calculation(points, field, tensions);
        Collections.sort(tensions);
        int size = tensions.size();
        int index = size * 95 / 100;
        double minTension = tensions.get(0);
        double maxTension = tensions.get(index);

        BufferedImage image = new BufferedImage(360, 240, TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        double delta = 0.1;

        for (CField t : field)
        {
            int w = (int)((t.x + 8) / 16.0 * 360);
            int h = (int)((t.y + 5 ) / 10.0 * 240);
            double c = ((t.getTension() - minTension) / (maxTension - minTension) * (360 - 240) + 240) / 360;
            if (c > 1.0) c = 1.0;
            g2d.setColor(Color.getHSBColor((float)c, 1.0f, 1.0f));
            g2d.fillRect(w, h, (int)(delta / 8.0 * 360), (int)(delta / 5.0 * 240));
        }

        File output = new File(fileName.substring(0, fileName.length() - 4) + ".jpg");
        ImageIO.write(image, "jpg", output);
    }
}