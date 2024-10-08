package io.chandler.gap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class models the Deltoidal Icositetrahedron, a Catalan solid dual to the Rhombicuboctahedron.
 *
 * The Deltoidal Icositetrahedron has:
 * - 26 vertices:
 *   - 6 of degree 4 (corresponding to cube faces)
 *   - 8 of degree 3 (corresponding to cube vertices)
 *   - 12 of degree 4 (corresponding to cube edges)
 * - 24 kite-shaped faces, which correspond to the vertices of a rhombicuboctahedron
 *
 * This class provides methods to retrieve the adjacent faces for each vertex in clockwise order.
 */
public class DeltoidalIcositetrahedron {

    // Total faces: 24 (indexed from 1 to 24)
    // Total vertices: 26 (indexed from 1 to 26)


    public static final int[][] cubeFaceToDifaces = {
        {1, 2, 4, 3},
        {9, 10, 12, 11},
        {5, 6, 8, 7},
        {13, 14, 16, 15},
        {17, 18, 20, 19},
        {21, 22, 24, 23}
    };

    public static final int[][] cubeVertexToDifaces = {
        {1, 20, 15},
        {2, 16, 23},
        {4, 24, 6},
        {3, 5, 19},
        {9, 17, 7},
        {10, 8, 22},
        {12, 21, 14},
        {11, 13, 18}
    };

    public static final int[][] cubeEdgeToDifaces = {
        {1, 3, 19, 20},
        {2, 1, 15, 16},
        {4, 2, 23, 24},
        {3, 4, 6, 5},
        {9, 11, 18, 17},
        {10, 9, 7, 8},
        {12, 10, 22, 21},
        {11, 12, 14, 13},
        {5, 7, 17, 19},
        {24, 22, 8, 9},
        {16, 14, 21, 23},
        {20, 18, 13, 15}
    };

    public static final int[][][] cubeFaceToDifacesPairs = {
        {{1, 4}, {2, 3}},
        {{9, 12}, {10, 11}},
        {{5, 8}, {6, 7}},
        {{13, 16}, {14, 15}},
        {{17, 20}, {18, 19}},
        {{21, 24}, {22, 23}}
    };


    public static final int[][][] cubeEdgeToDifacesPairs = {
        {{1, 19}, {3, 20}},
        {{2, 15}, {1, 16}},
        {{4, 23}, {2, 24}},
        {{3, 6}, {4, 5}},
        {{9, 18}, {11, 17}},
        {{10, 7}, {9, 8}},
        {{12, 22}, {10, 21}},
        {{11, 14}, {12, 13}},
        {{5, 17}, {7, 19}},
        {{24, 8}, {22, 6}},
        {{16, 21}, {14, 23}},
        {{20, 13}, {18, 15}}
    };
}
