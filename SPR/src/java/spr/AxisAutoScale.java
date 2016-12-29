package spr;

/**
 * Created by Matsishin on 22.12.2016.
 *
 * @author Matsishin Nicolas
 *         this class just makes nice axis scale division
 */
class AxisAutoScale {

    double min, max;
    double tick;

    AxisAutoScale(double begin, double end) {
        if (begin > end) {
            this.max = begin;
            this.min = end;
        } else {
            this.max = end;
            this.min = begin;
        }
        boolean minus = false;
        double dif = Math.abs(this.max - this.min);
        int pov = 0;
        while ((dif > 0) && (dif < 1)) {
            dif = dif * 10;
            pov++;
            minus = true;
        }
        while (dif >= 10) {
            dif = dif / 10;
            pov++;
            minus = false;
        }
        int o2 = (int) (dif * 10 / 2);
        int o5 = (int) (dif * 10 / 5);
        int one = (int) (dif);
        int two = (int) (dif / 2);
        double ord;
        double[] answer;
        if (minus) ord = Math.pow(10, -pov);
        else ord = Math.pow(10, pov);
        if (two > 3) answer = new double[]{two, 2 * ord};
        else if (one > 3) answer = new double[]{one, ord};
        else if (o5 > 3) answer = new double[]{o5, 0.5 * ord};
        else if (o2 > 3) answer = new double[]{o2, 0.2 * ord};
        else {
            System.out.println("zavtuk");
            answer = new double[]{0, 0};
        }

        if (this.min < 0) this.min = (int) (this.min / answer[1] - 1) * answer[1];
        else this.min = (int) (this.min / answer[1]) * answer[1];
        if (this.min < 0) this.max = (int) (this.max / answer[1]) * answer[1];
        else this.max = (int) (this.max / answer[1] + 1) * answer[1];

        this.tick = answer[1];
    }
}
