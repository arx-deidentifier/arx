/**
 * Created by Max on 27.08.16.
 */
public class Test {
    public static void main(String[] args) {

        System.out.println("Collisions: " + collisionsOneClass(2, 4));
    }


    private static double collisionsOneClass(int size, int count) {
        // aa bb ccc dddd
        // 2,2   3,1  4,1
        int collisions = 0;
        for (int i = 1; i < count; i++) {
            System.out.println("Schritt " + i + ":");
            int cc = size * ((count - i) * size);
            System.out.println(size + " * " + "((" + ((count + "-" + i)) + ") * " + size + ")");
            System.out.println("Kollisionen: " + cc + "");
            collisions += cc;
            System.out.println("Collisionen bisher: " + collisions + "");

        }
        int formel = (int)Math.pow(size,size) * (fac(count-1));
        System.out.println("Formel: " + formel);

        return collisions;
    }

    public static int fac(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
}
