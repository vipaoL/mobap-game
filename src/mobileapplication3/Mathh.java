/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3;

/**
 *
 * @author vipaol
 */
public class Mathh { // надо будет перевести на стандартную
    private static int sin_t[] = {0, 174, 342, 500, 643, 766, 866, 940, 985, 1000};
    private static int sinus(int t) {
        int k;
        k = (int) (t / 10);
        if (t % 10 == 0) {
            return sin_t[k];
        } else {
            return (int) ((sin_t[k + 1] - sin_t[k]) * (t % 10) / 10 + sin_t[k]);
        }
    }

    public static int sin(int t) {
        int sign = 1;
        t = t % 360;
        if (t < 0)
        {
            t = -t;
            sign = -1;
        }
        if (t <= 90) {
            return sign * sinus(t);
        } else if (t <= 180) {
            return sign * sinus(180 - t);
        } else if (t <= 270) {
            return -sign * sinus(t - 180);
        } else {
            return -sign * sinus(360 - t);
        }
    }

    public static int cos(int t) {
        t = t % 360;
        if (t < 0) {
            t = -t;
        }
        if (t <= 90) {
            return sinus(90 - t);
        } else if (t <= 180) {
            return -sinus(t - 90);
        } else if (t <= 270) {
            return -sinus(270 - t);
        } else {
            return sinus(t - 270);
        }
    }
}
