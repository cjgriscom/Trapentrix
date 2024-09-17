package io.chandler.gap;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Dodecahedron representation as a dual of the snub tetrahedron
 * 
 * The vertices are limited to the non-octahedral variety
 */
public class Dodecahedron_STDual {
	static final int[][] pifaceToTetrahedronFaceVertex = new int[12][];

	// Faces listed in clockwise order
	static final int[][] piverticesToPifaces = new int[12+4][];


	private static final HashMap<Integer, Integer> faceIVertexIToPifaceI = new HashMap<>();
	private static int getPifaceBetweenTetrahedronFaceAndVertex(int face, int vertex) {
		return faceIVertexIToPifaceI.get(vertex*100 + face) + 1;
	}

	static {
		// Generate pentagonal icositrahedron geometries
		int piface = 0;
		for (int vertexI = 0; vertexI < 4; vertexI++) {
			piverticesToPifaces[12 + vertexI] = new int[3];
			for (int faceI = 0; faceI < 3; faceI++) { // 3 faces per vertex
				int vertex = vertexI + 1;
				int face = Tetrahedron.vertexFaces[vertexI][faceI];
				//System.out.println(face + " " + vertex + " " + piface);
				faceIVertexIToPifaceI.put(vertex*100 + face, piface);
				pifaceToTetrahedronFaceVertex[piface] = new int[]{face, vertex};
				piverticesToPifaces[12 + vertexI][faceI] = piface + 1;
				piface++;
			}
		}

		// One pi vertex per face
		for (piface = 0; piface < 12; piface++) {
			int face = pifaceToTetrahedronFaceVertex[piface][0];
			int vertex = pifaceToTetrahedronFaceVertex[piface][1];

			// Get the tetrahedron face clockwise
			int tetrahedronfaceright = Tetrahedron.getClockwiseFace(vertex, face);
			int tetrahedronvertexleft = Tetrahedron.getCounterclockwiseVertex(face, vertex);

			piverticesToPifaces[piface] = new int[] {
				piface + 1,
				getPifaceBetweenTetrahedronFaceAndVertex(face, tetrahedronvertexleft),
				getPifaceBetweenTetrahedronFaceAndVertex(tetrahedronfaceright, vertex)
			};
		}


	}

	public static int[] getFacesFromVertex(int vertex) {
		return piverticesToPifaces[vertex - 1];
	}

	public static void main(String[] args) {
		for (int i = 1; i <= 12; i++) {
			System.out.println(i + ": V" + pifaceToTetrahedronFaceVertex[i-1][1] + " F" + pifaceToTetrahedronFaceVertex[i-1][0]);
		}



		// Label generator

		/*String[] gens = new String[] {
			"[(4,1,5)(9,22,4)(12,2,10)(15,12,14)(14,3,16)(21,23,20)]",
			"[(1,10,2)(6,19,5)(9,11,7)(13,12,14)(14,20,17)(23,21,22)]",
			"[(2,16,3)(4,4,6)(11,7,10)(15,17,13)(19,14,20)(22,9,12)]",
			"[(3,5,1)(7,4,4)(12,14,11)(17,13,16)(21,6,19)(12,15,23)]"
		};

		for (String gen : gens) {
			printVertexGeneratorNotations(GroupExplorer.parseOperationsArr(gen));
		}*/

		
	}

	public static void printVertexGeneratorNotations(int[][][] g0) {

		// Each inner array corresponds to something in getFacesFromVertex(1 - 16)


		System.out.print("[");
		for (int a = 0; a < g0.length; a++) {
			if (a != 0) System.out.print(",");
			System.out.print("(");
			boolean first = true;
			for (int b = 0; b < g0[a].length; b++) {
				int[] cycle = g0[a][b].clone();
				
				int vert = getMatchingVertexFromFaces(cycle);
				
				if (!first) System.out.print(",");
				first = false;
				if (vert == 0) {
					throw new RuntimeException("No match found for " + Arrays.toString(cycle));
				} else if (vert > 0) {
					System.out.print("V" + vert + "R");
					
				} else {
					System.out.print("V" + (-vert) + "L");
				}

			}
			System.out.print(")");
		}

		System.out.println("]");
	}

	public static int getMatchingVertexFromFaces(int[] src) {
		for (int i = 1; i <= 16; i++) {
			int[] faces = getFacesFromVertex(i).clone();
			
			for (int j = 0; j < faces.length; j++) {
				if (Arrays.equals(faces, src)) return i;
				ArrayRotator.rotateRight(faces);
			}

			faces = CycleInverter.invertArray(faces);
			for (int j = 0; j < faces.length; j++) {
				if (Arrays.equals(faces, src)) return -i;
				ArrayRotator.rotateRight(faces);
			}
			
		}
		return 0;
	}
}
