package io.chandler.trapentrix;

public class Generators {
	// https://www.sciencedirect.com/science/article/pii/0012365X9390222F?ref=cra_js_challenge&fr=RR-1
	public static final String m12 = "[(1,2,3)(4,5,6)(7,8,9)(10,11,12),(3,4)(6,7)(9,10)(11,12)]";
    public static final String m11 = "[(1,2,3,4,5,6,7,8,9,10,11),(3,7,11,8)(4,10,5,6)]";

	public static final String m11_12pt = "[(1,6)(2,9)(5,7)(8,10),(1,6,7,4)(2,8)(3,9)(5,11,12,10)]";
	public static final String trapentrix = "[(1,2,3)(5,6,7),(2,4,5)(6,8,9)]";
    
	// Mildly interesting - upper vertices on pentultimate with edge turn
	public static final String unk_upper_pentultimate_edge = "[(1,2,3,4,5)(6,7,8,9,10),(4,11)(5,3)(1,2)(6,7)]";

	// Alternating pentultimate
	public static final String alternating_pentultimate = "[(1,2,3,4,5)(6,7,8,9,10),(1,11,4,10,9)(2,8,12,6,3)]";

	// M6 / Eliac: [ (1,2,3,4)(5,6,7,8), (1,5)(2,9)(4,10) ]
	public static final String m6 = "[(1,2,3,4)(5,6,7,8),(1,5)(2,9)(4,10)]";

	// This is order 660, PSL(2,11) ?
	//"[(1,2,3,4,5,6,7,8,9,10,11),(2,4)(3,9)(5,10)(7,11)]";
}
