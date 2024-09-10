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
		for (int i = 1; i <= 32; i++) {
			System.out.println(i + ": " + Arrays.toString(getFacesFromVertex(i)));
		}
	}
}
