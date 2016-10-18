package haiku;

import java.util.Random;

public class SentenceGraph {
	
	/**
	 * The order of this array corresponds to the order that respective words would occur in a sentence. 
	 * Each element stores the data that would be held in a corresponding vertex.
	 * 
	 * NOTE: any subsequent mention of a 'node index' refers to the index of a part of speech in this array.
	 */
	public final PartOfSpeech[] data = {
			PartOfSpeech.BLANK,
			PartOfSpeech.ADVERB, 
			PartOfSpeech.PREPOSITION, 
			PartOfSpeech.ARTICLE, 
			PartOfSpeech.ADJECTIVE, 
			PartOfSpeech.NOUN, 
			PartOfSpeech.VERB, 
			PartOfSpeech.ADVERB, 
			PartOfSpeech.PREPOSITION, 
			PartOfSpeech.ARTICLE, 
			PartOfSpeech.ADJECTIVE, 
			PartOfSpeech.NOUN, 
			PartOfSpeech.BLANK
			};
	
	
	/**
	 * This matrix stores the edges that link the vertices above.
	 * 
	 *      [row][colummn]   -->  [current vertex][adjacent vertex]
	 * 
	 * Each edge, if it exists, is assigned a decimal number between 0.0 and 2.0, exclusive;
	 * these numbers correspond to the relative weights of the edges (IE, which one is more "important" to follow).
	 * 
	 * A weight of 0.0 means that the edge is untraversable.
	 */
	private double[][] matrix;
	
	//-- an internal cursor; this stores the index of the vertex last visited
	private int iterator;
	
	
	
	/************************************\
	 *         CONSTRUCTOR
	 * 
	\************************************/

	public SentenceGraph() {
		reset();
	}

	
	
	/**
	 * Returns the size of this graph (number of vertices).
	 */
	public int size() {
		return data.length;
	}
	
	
	/**
	 * Returns the index of the last visited part of speech.
	 */
	public int getIndex() {
		return iterator;
	}

	/**
	 * Returns the part of speech for a provided vertex.
	 */
	public PartOfSpeech getNode(int index) {
		return data[index];
	}
	
	
	/**
	 * Finds the connecting edge linking the current vertex to another that's weighted most heavily.
	 */
	public int nextEdge(int currentNode) 
	{
		double max = 0.0;  // The largest weight for any edge found so far
		int target = -1;   // The index of the node with the largest edge weight
		
		for(int i = 0; i < data.length; i++) 
		{
			double total = getAdjustedWeight(currentNode, i, 0.2);
			
			if(total > max) 
			{
				max = total;
				target = i;
			}
		}
		
		iterator = target;
		System.out.println("         index of heaviest edge: A[" + target + "]  (" + max + ")");
		
		//-- adjust any edge weights that have changed from this move
		adjustMatrix(currentNode, target);
		
		return target;
	}
	
	
	/**
	 * Calculate an adjusted weight value for random edge selection.
	 * 
	 * The variance parameter adjusts how tightly bound the random calculation is to the expected value.
	 *   With a variance of 0.10, the returned value would fall randomly in the range
	 *   between the expected value (ie, stored weight value) plus or minus 10%.
	 *   
	 *   With 0.20, the calculation would fall randomly between (0.8 * weight) and (1.2 * weight).
	 * 
	 * @param current the index of the previous node (visited BEFORE traversing the edge)
	 * @param destination the index of the next (target) node (to be visited AFTER traversing the edge) 
	 * @param variance the percentage of total weight that is affected by a random multiplier
	 */
	private double getAdjustedWeight(int current, int destination, double variance) 
	{
		double r = (new Random().nextInt(10) / 5.0) - 1.0;   // a random decimal  [0 < r < 10]
		double e = matrix[current][destination];			 // the weight of this edge (recorded in matrix)		
		
		double result = e + (r * variance);
		
		/*
		 *  This prevents returning the random result 0.0
		 *  (which would lock out the edge, effectively erasing it from the graph)
		 */
		if (e > 0.0 && result <= 0.0)
			return 0.01;
		
		return result;
	}
	
	
	/**
	 * Determine whether the given vertex has any accessible, adjacent edges.
	 */
	public boolean hasNextEdge(int index) 
	{
		for(int i = 0; i < data.length; i++)
			if(matrix[index][i] > 0) {
				return true;
			}
		return false;
	}
	
	
	/**
	 * Determine whether traversal of the graph has completed (end of a sentence).
	 */
	public boolean reachedEnd() {
		return (iterator == data.length - 1);
	}
	
	
	/**
	 * This method dynamically adjusts stored weight values for edges of the graph.
	 * It is called once for each edge traversal, and it tweaks values for certain edges,
	 * based on the path of traversal.
	 * 
	 * @param current the current vertex
	 * @param next the next vertex to be visited
	 */
	private void adjustMatrix(int current, int next) 
	{
		// ============= START ======================
		if(current == 0 && next == 5) { //start -> NOUN
			setEdge(5, 7, 0.0);
			modifyEdge(6, 7, 2.0);  	// VERB -> ADV ++
			modifyEdge(7, 6, 2.0);		// ADV -> VERB ++
			modifyEdge(6, 8, 2.0);		// VERB -> PREP ++
			modifyEdge(7, 8, 2.0);		// ADV -> PREP ++
			modifyEdge(6, 12, 0.1);		// VERB -> end --
			modifyEdge(7, 12, 0.1);		// ADV -> end --
		}
		
		// ============= ADVERB 1 ===================
		if(current == 1 && next == 1) //ADV -> itself
			
			//reduce the chance of returning again to the same node by 50%
			modifyEdge(1, 1, 0.5);
		
		if(current == 3 && next == 1)  //ART -> ADV
		{
			removeEdge(1, 1);		// block ADV -> itself
			removeEdge(1, 2);		// block ADV -> PREP
			removeEdge(1, 3);		// block ADV -> ART
		}
		// ============= PREP 1 =====================
		
		if(next == 2) 	//landing on PREP 1
		{ 	 
			//1. block travel past noun
			removeEdge(5, 6);
			removeEdge(5, 7);
			
			//2. enable returning from noun
			setEdge(5, 4, 0.2);  // enable N -> ADJ
			setEdge(5, 3, 0.8);  // enable N -> ART
		}
		
			//3. re-enable travel past noun
		if(current == 5 && next == 3)    // [N -> ART]
		{   
			modifyEdge(3, 4, 0.5);  // reduce ART -> ADJ
			addEdge(5, 6);		// re-enable N -> V
			addEdge(5, 7);		// re-enable N -> ADV
		}
		
		if(current == 5 && next == 4)    // [N -> ADJ]
		{   
			modifyEdge(4, 3, 0.5);  // reduce ADJ -> ART
			addEdge(5, 6);		// re-enable N -> V
			addEdge(5, 7);		// re-enable N -> ADV
		}
							
		// ================== VERB ====================
		// Block travel past verb
		if (current == 5 && next == 7)   // [N -> ADV]
		{ 	
			removeEdge(7, 8);		// block ADV -> PREP 2
			removeEdge(7, 12);	// block ADV -> end
		}
		
		// Re-enable travel past verb
		if (next == 6)   // V is landed on
		{			
			addEdge(7, 8);		// enable ADV -> PREP 2
			setEdge(7, 12, 0.1);	// enable ADV -> end
			setEdge(6, 12, 0.01);	// enable V -> end
			setEdge(6, 11, 0.3);	// enable V -> N
			setEdge(6, 8, 0.8);		// enable V -> PREP
			
		}
		
		//Interrupt potential V -> ADV -> V -> ADV cycle
		if(current == 7 && next == 6)   // ADV -> V: prohibit returning to ADV
			modifyEdge(6, 7, 0.3);
			
		if(current == 6 && next == 7)   // V -> ADV: stop from returning to V
			removeEdge(7, 6);
		
		// ============================================		
		
		if(current == 7 && next == 7) // ADV cycle 2
			modifyEdge(7, 7, 0.5);
		
		if(current == 11) {
			setEdge(11, 7, 0.9);
			removeEdge(7, 8);	
		}
		
	}

	
	/**
	 * The below matrix stores the likelihood of traveling to a given connecting edge.
	 * A random number between one and ten is added to each choice in a row, and the edge with the largest value
	 * is chosen.
	 * 
	 * Play around with any nonzero edge weights to experiment.
	 *   traversible values: (0.0, 2.0) exclusive.
	 */
	public void reset() 
	{
		iterator = 0;
							// 0	1	 2	 3	   4   5    6     7    8    9    10  11    12
						   //start adv* prep art  adj  n    v    adv* prep art  adj* n    end <--DESTINATION
		matrix = new double[][]{ 																
				new double[] {0.0, 1.0, 0.3, 20.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //start		0
				new double[] {0.0, 1.0, 0.1, 1.0, 1.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //adv*		1
				new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //prep		2
				new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //art		3
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //adj  [sb] 4
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //noun [sb] 5
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //verb [pr] 6
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.4, 0.0, 0.0, 0.0, 0.0, 0.0}, //adv* [pr] 7
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0}, //prep		8
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0}, //art		9
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0}, //adj*		10
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0}, //noun		11
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //end		12
		};																						// ^ SOURCE
		
	}

	
	
	// ==== methods included as debugging tools === //
	
	/**
	 * Multiply the weight of an edge by the provided amount.
	 */
	public void modifyEdge(int source, int target, double mod) {
		matrix[source][target] = mod * matrix[source][target];
	}
	
	
	public void setEdge(int i, int j, double v) {
		matrix[i][j] = v;
	}
	
	
	public void addEdge(int i, int j) {
		matrix[i][j] = 1.0;
	}
	
	public void removeEdge(int i, int j) {
		matrix[i][j] = 0.0;
	}
	
	
	public double getEdge(int i, int j) {
		// TODO Auto-generated method stub
		return matrix[i][j];
	}
}