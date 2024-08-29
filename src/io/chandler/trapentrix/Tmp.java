package io.chandler.trapentrix;

import java.util.ArrayList;
import java.util.Scanner;

public class Tmp {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		int br = 0;
		ArrayList<String> l = new ArrayList<>();
		while (in.hasNext()) {
			String s = in.next();
			if (s.endsWith("x")) break;
			l.add(s);
		}
		System.out.println();
		
		for (int i = l.size() - 1; i >= 0; i--) {
			if (br % 16 == 0) System.out.println();
			
			String s = l.get(i);
			if (s.endsWith("D")) s = "U";
			else if (s.endsWith("U")) s = "D";
			
			System.out.print(s);
			if (br % 2 == 1) System.out.print(' ');
			br++;
			
		}
	}
}
