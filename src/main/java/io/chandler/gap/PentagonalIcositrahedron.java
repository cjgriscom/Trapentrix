package io.chandler.gap;

import java.util.Arrays;
import java.util.HashMap;

public class PentagonalIcositrahedron {
	static final int[][] pifaceToCubeFaceVertex = new int[24][];

	// Faces listed in clockwise order
	static final int[][] piverticesToPifaces = new int[24+8][];


	private static final HashMap<Integer, Integer> faceIVertexIToPifaceI = new HashMap<>();
	private static int getPifaceBetweenCubeFaceAndVertex(int face, int vertex) {
		return faceIVertexIToPifaceI.get(vertex*100 + face) + 1;
	}

	static {
		// Generate pentagonal icositrahedron geometries
		int piface = 0;
		for (int vertexI = 0; vertexI < 8; vertexI++) {
			piverticesToPifaces[24 + vertexI] = new int[3];
			for (int faceI = 0; faceI < 3; faceI++) { // 3 faces per vertex
				int vertex = vertexI + 1;
				int face = Cube.vertexFaces[vertexI][faceI];
				//System.out.println(face + " " + vertex + " " + piface);
				faceIVertexIToPifaceI.put(vertex*100 + face, piface);
				pifaceToCubeFaceVertex[piface] = new int[]{face, vertex};
				piverticesToPifaces[24 + vertexI][faceI] = piface + 1;
				piface++;
			}
		}

		// One pi vertex per face
		for (piface = 0; piface < 24; piface++) {
			int face = pifaceToCubeFaceVertex[piface][0];
			int vertex = pifaceToCubeFaceVertex[piface][1];

			// Get the cube face clockwise
			int cubefaceright = Cube.getClockwiseFace(vertex, face);
			int cubevertexleft = Cube.getCounterclockwiseVertex(face, vertex);

			piverticesToPifaces[piface] = new int[] {
				piface + 1,
				getPifaceBetweenCubeFaceAndVertex(face, cubevertexleft),
				getPifaceBetweenCubeFaceAndVertex(cubefaceright, vertex)
			};
		}


	}

	public static int[] getFacesFromVertex(int vertex) {
		return piverticesToPifaces[vertex - 1];
	}

	public static void main(String[] args) {
		for (int i = 1; i <= 24; i++) {
			System.out.println(i + ": V" + pifaceToCubeFaceVertex[i-1][1] + " F" + pifaceToCubeFaceVertex[i-1][0]);
		}



		// Label generator

		String[] gens = new String[] {
			"[(2,16,3)(7,11,9)(13,17,15)(20,18,19)(4,5,6)(24,23,22),(3,5,1)(9,22,8)(14,24,15)(20,23,21)(12,11,10)(16,17,18)]",
			"[(2,16,3)(7,11,9)(13,17,15)(20,18,19)(6,5,4)(24,23,22),(3,5,1)(9,22,8)(14,24,15)(20,23,21)(12,11,10)(16,17,18)]",
			"[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(22,21,23)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(19,18,20)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(19,18,20)(22,21,23)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(1,10,2)(9,11,7)(13,12,14)(16,3,18)(20,23,21)(4,5,6),(2,16,3)(8,22,9)(18,20,17)(21,6,19)(24,15,23)(10,11,12)]",
			"[(1,10,2)(9,11,7)(13,12,14)(16,3,18)(20,23,21)(6,5,4),(2,16,3)(8,22,9)(18,20,17)(21,6,19)(24,15,23)(10,11,12)]",
			"[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(20,18,19)(22,21,23)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(19,18,20)(23,21,22)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
			"[(2,16,3)(4,1,5)(13,17,15)(19,18,20)(22,21,23)(9,8,7),(3,5,1)(6,8,4)(11,7,10)(13,12,14)(22,9,24)(18,17,16)]",
		};

		for (String gen : gens) {
			printVertexGeneratorNotations(GroupExplorer.parseOperationsArr(gen));
		}

		
	}

	public static void printVertexGeneratorNotations(int[][][] g0) {

		// Each inner array corresponds to something in getFacesFromVertex(1 - 32)


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
		for (int i = 1; i <= 32; i++) {
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
