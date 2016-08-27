/**
 * Created by Max on 27.08.16.
 */
public class Test {
    public static void main(String[] args) {
        Test();
        //System.out.println("\nCollisions: " + collisionsOneClassSizeIterative(1, 3));
    }


    private static int collisionsOneClassSizeIterative(int size, int count) {
        // aa bb ccc dddd
        // 2,2   3,1  4,1
        int collisions = 0;
        for (int i = 1; i < count; i++) {
            //System.out.println("Schritt " + i + ":");
            int cc = size * ((count - i) * size);
            //System.out.println(size + " * " + "((" + ((count + "-" + i)) + ") * " + size + ")");
            //System.out.println("Kollisionen: " + cc + "");
            collisions += cc;
            //System.out.println("Collisionen bisher: " + collisions + "");

        }
        return collisions;
    }

//    public static int collisionsOneClassSizeFormula(int size, int count) {
//        if (count == 0 || count == 1) {
//            return 0;
//        }
//
//        return size * (fac(count-1) * size);
//        //return (int) Math.pow(size, size) * (fac(count - 1));
//    }

    public static int fac(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

    public static void Test() {

        EqClassTest[] values = new EqClassTest[]{
                new EqClassTest(new EqClass(1, 1), 0), // 1,1 = 0
                new EqClassTest(new EqClass(1, 2), 1), // 1,2 = 1
                new EqClassTest(new EqClass(1, 3), 3), // 1,3 = 3
                new EqClassTest(new EqClass(1, 4), 6), // 1,4 = 6
                new EqClassTest(new EqClass(1, 5), 10), // 1,5 = 10

                new EqClassTest(new EqClass(2, 1), 0),
                new EqClassTest(new EqClass(2, 2), 4),
                new EqClassTest(new EqClass(2, 3), 12),
                new EqClassTest(new EqClass(2, 4), 24),
                new EqClassTest(new EqClass(2, 5), 40),

                new EqClassTest(new EqClass(3, 1), 0),
                new EqClassTest(new EqClass(3, 2), 9),
                new EqClassTest(new EqClass(3, 3), 27),
                new EqClassTest(new EqClass(3, 4), 54),
                //new EqClassTest(new EqClass(3, 5), 27),

                new EqClassTest(new EqClass(5, 1), 0),
                new EqClassTest(new EqClass(5, 2), 25),

                new EqClassTest(new EqClass(10, 1), 0),
                new EqClassTest(new EqClass(10, 2), 100),
                new EqClassTest(new EqClass(10, 3), 300),


                // new EqClassTest(new EqClass(2, 1), 0),

        };
        boolean error = false;
        for (int i = 0; i < values.length; i++) {
            EqClass eqClass = values[i].getEqClass();

            int size = eqClass.size;
            int count = eqClass.count;

            int resI = collisionsOneClassSizeIterative(size, count);
            //int resF = collisionsOneClassSizeFormula(size, count);


            if (resI != values[i].getResultShouldBe()) {
                System.out.println("\n> Result mismatch:");
                System.out.println("> EqClass: (" + size + "," + count + ")");
                System.out.println("> Iterative Result: " + resI);
                //System.out.println("Formula Result: " + resF);
                System.out.println("> Result should be: " + values[i].getResultShouldBe());
                error = true;
            }
        }
        System.out.println();
        if (!error) {
            System.out.println("> All Tests successful");
        } else
            System.out.println("> ONE OR MORE TESTS FAILED!");

    }

    public static class EqClass {
        private int size;
        private int count;

        public EqClass(int size, int count) {
            this.size = size;
            this.count = count;
        }

        public int getSize() {
            return size;
        }

        public int getCount() {
            return count;
        }
    }

    public static class EqClassTest {
        private EqClass eqClass;

        public EqClass getEqClass() {
            return eqClass;
        }

        public int getResultShouldBe() {
            return resultShouldBe;
        }

        private int resultShouldBe;

        public EqClassTest(EqClass eqClass, int resultShouldBe) {
            this.eqClass = eqClass;
            this.resultShouldBe = resultShouldBe;
        }
    }
}
