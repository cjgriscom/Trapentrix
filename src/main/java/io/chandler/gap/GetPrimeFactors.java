package io.chandler.gap;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class GetPrimeFactors {
	/**
36288
37632
46656
54432
58320
64512
65520
69984
72576
75264
80640
95040
108864
112896
116640
138240
145152
190080
202176
225792
262440
326592
489888
762048
1140480
?

	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        while (scanner.hasNextInt()) {
            int number = scanner.nextInt();
            numbers.add(number);
        }
        for (int number : numbers) {
            List<Integer> primeFactors = getPrimeFactors(number);
            
            System.out.print(number + ": ");
            System.out.println(primeFactors);
        }
        
        scanner.close();
    }
    
    public static Set<Integer> getPrimeFactorsSet(int number) {
        Set<Integer> factors = new TreeSet<>();
        factors.addAll(getPrimeFactors(number));
        return factors;
    }
    public static List<Integer> getPrimeFactors(int number) {
        List<Integer> factors = new ArrayList<>();
        
        for (int i = 2; i <= number; i++) {
            while (number % i == 0) {
                factors.add(i);
                number /= i;
            }
        }
        
        return factors;
    }
}
