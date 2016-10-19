package haiku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * <p>
 * This class manages the loading, parsing, and sorting of words provided in a 
 * dictionary ".dic" file.
 * </p>
 * 
 * <p><b>Input file format</b></p>
 * A dictionary file is used to store words. A dictionary file begins with a header,
 * which specifies the number of words listed in the file, as well as any string tokens
 * used as delimiters within the file.
 * and then lists its words, each followed by its respective part of speech.
 * 
 * 
 * <br>
 * Headers use the following format:
 * 
 *   <br><br>
 *   <code> &lt;H lines=<i>val</i>; delim="<i>d</i>";></code>
 *   <br><br>
 * 
 * where <code><i>val</i></code> is the number of words stored in the file, 
 * and <code><i>d</i></code> is the delimiter token seperating words from
 * their parts of speech.
 *   
 * 
 * <br><br>
 * @author Jobin
 *
 */
public class Dictionary
{	
	/**
	 * Constructs a new Dictionary that manages pre-loaded words.
	 * @param filename a dictionary file to be loaded
	 * @throws FileNotFoundException if specified file cannot be found
	 * @throws IllegalStateException if specified file is improperly formatted
	 */
	public Dictionary(String filename) throws FileNotFoundException, IllegalStateException 
	{
		randGen = new java.util.Random();
		final String errmsg = "Error loading dictionary file \"" + filename + "\": ";
		
		System.out.println(" ==== Initializing Dictionary ====");
		
		System.out.print("Loading dictionary file...");
		// ---- Create File scanner ---- //
		Scanner inFile;
		try {
			inFile = new Scanner( new File(filename) );
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(errmsg + "file not found!");
		}
		System.out.println("loaded");
		
		// ---- Strip file header ---- //
		System.out.print("Stripping file header...");
		String header = inFile.findWithinHorizon("<H.*>", 200);
		if (header == null) {
			inFile.close();
			throw new IllegalStateException(errmsg + "missing file header!");
		}
		
		// ---- Parse header ---- //
		final String l_token = "lines=";
		final String d_token = "delim=\"";
		
		int l_stPos = header.indexOf(l_token) + l_token.length();
		int l_endPos = header.indexOf(';', l_stPos);
		int d_stPos = header.indexOf(d_token) + d_token.length();
		int d_endPos = header.indexOf('\"', d_stPos);
		
		if (l_stPos < 1 || l_endPos < 1 || d_stPos < 1 || d_endPos < 1) {
			inFile.close();
			throw new IllegalStateException(errmsg + "invalid file header!");
		}
		
		final String delim = header.substring(d_stPos, d_endPos);
		//TODO: debug statement
		System.out.println("delim: \"" + delim + "\"");
		size = Integer.parseInt(header.substring(l_stPos, l_endPos));
		System.out.println("done");
		
		// ---- Instantiate data arrays ---- //
		allWords = new String[size];
		wordSyllables = new int[size];
		wordPOS = new PartOfSpeech[size];
		
		System.out.print("Parsing dictionary...");
		// ---- Process remainder of file ---- //
		
		inFile.nextLine();   // prime inFile scanner (header stripping leaves blank line)
		for (int i = 0; inFile.hasNextLine() && i < size; i++) 
		{	
			String line = inFile.nextLine();
			//TODO: debug statement
			if (!line.contains(delim))
				System.out.println("\nError parsing line " + i + ": doesn't contain delim: \"" 
									+ delim + "\"\n" + line);
			else {
				String word = line.substring(0, line.indexOf(delim));
				
				// -- store the word to next slot in arrays -- //
				allWords[i] = word;
				wordPOS[i] = parsePOS(line);
				wordSyllables[i] = SyllableCounter.syllables(word);
				if (wordSyllables[i] > maxSyllableCount)
					maxSyllableCount = wordSyllables[i];
			}
		}
		
		inFile.close();
		System.out.println("done");
		
		System.out.print("Sorting arrays... [NOT YET IMPLEMENTED -- SKIPPING]\n");
		//TODO: bucketsort the dictionary arrays
		
		setArrayPointers();
		System.out.println(" ====== DICTIONARY INITIALIZED ======");
	}
	
	
	/**
	 * Get the number of stored words with {@link PartOfSpeech} <code>p</code>
	 * and <code>m</code> syllables.
	 * 
	 * @param m number of syllables
	 * @param p PartOfSpeech of the desired words
	 */
	public int subsetSize(PartOfSpeech p, int m) {
		final String errMsg = "Error counting " + m + "-syllable " + p + "s: ";
		if (p == null)
			throw new IllegalArgumentException(errMsg + "POS is null!");
		if (m < 1)
			throw new IllegalArgumentException(errMsg + "too few syllables!");
		if (m > maxSyllableCount)
			throw new IllegalArgumentException(errMsg 
					+ "too many syllables! (max = " + maxSyllableCount + ")");
		
		int q = indexOfPOS(p);
		return sz[q][m];
	}
	
	/**
	 * Randomly select a word with the given part of speech and syllable count.
	 * 
	 * This method retrieves the entire set of internally stored words meeting the criteria,
	 * and selects one at random.
	 * 
	 * @param p Desired part of speech
	 * @param m syllables in word
	 * @return the word
	 * @throws IllegalArgumentException if p is null, or if no word exists that matches
	 */
	public String getRandomWord(PartOfSpeech p, int m) {
		final String errMsg = "Error getting random " + m + "-syllable " + p + ": ";
		if (p == null || p == PartOfSpeech.BLANK)
			throw new IllegalArgumentException(errMsg + "invalid Part of speech \'" + p + "\'");
		if (m < 1) { 
			//throw new IllegalArgumentException(errMsg + "too few syllables!");
			return null;
		}
		if (m > maxSyllableCount) {
			//throw new IllegalArgumentException(errMsg 
			//+ "too many syllables! (max = " + maxSyllableCount + ")");
			return getRandomWord(p, m-1);
		}
		
		int px = indexOfPOS(p);
		if (sz[px][m] < 1) {
			//throw new IllegalArgumentException(errMsg + "no " + m + "-syllable words!");
			return null;
		}
		
		int r = randGen.nextInt(sz[px][m]);
		return allWords[si[px][m] + r];
	}
	
	
	private void setArrayPointers() 
	{
		System.out.println(" ---- Setting array pointers ---- ");
		System.out.println("POS count: "+ POS_COUNT + "\nmax syllables: " + maxSyllableCount);	
		
		//		index 'i' corresponds to words with 'i' syllables
		si = new int[POS_COUNT][maxSyllableCount + 1];
		sz = new int[POS_COUNT][maxSyllableCount + 1];
		
		
		//---		populate start index table 		---//
		for (int wi = 1, p = 0, m = 1; 
				wi < allWords.length && p < POS.length-1; wi++) {
			
			// iterate through words, looking for where POS changes
			if (wordPOS[wi] != wordPOS[wi-1]) 
			{
				while (++m <= maxSyllableCount)
					si[p][m] = -1;
				
				si[p][0] = wi-1;
				si[++p][1] = wi;
				m = 1;
			}
			
			// iterate through words, looking for where syl count changes
			else if (wordSyllables[wi] != wordSyllables[wi-1]) 
			{
				while (++m < wordSyllables[wi]) 
					si[p][m] = -1;	
				if (m == wordSyllables[wi])
					si[p][m] = wi;
			}
		}
		
		//---		populate size table 		---//
		for (int p = 0; p < POS.length; p++)
			for (int m = 1; m <= maxSyllableCount; m++) {
				if (m == maxSyllableCount)
					sz[p][m] = (si[p][m] < 0)?  0 : si[p][0] - si[p][m];
				else
					sz[p][m] = (si[p][m] < 0 && si[p][m+1] < 0)?  0 : si[p][m+1] - si[p][m];
			}
		System.out.println(" ---- Array pointers set ---- ");
	}
	
	
	private int indexOfPOS(PartOfSpeech p) {
		for (int i = 0; i < POS.length-1; i++)
			if (p.equals(POS[i]))
				return i;
		
		return -1;
	}
	
	
	static PartOfSpeech parsePOS(String s) {
		final String errMsg = "Error parsing Part of speech from \"" + s + "\": ";
		if (s.equals(""))
			throw new IllegalArgumentException(errMsg + "null string!");
		
		for (int i = 0; i < POS.length; i++) {
			if (s.contains(POS[i].toString()))
				return POS[i];
		}
		
		return null;
	}
	
	
	// -------------- INTERNAL COMPONENTS ---------- //
	private String[] allWords;
	private int[] wordSyllables;
	private PartOfSpeech[] wordPOS;
	private int size;
	
	private static final PartOfSpeech[] POS = PartOfSpeech.values();
	private static final int POS_COUNT = POS.length;
	private static int maxSyllableCount;
	
	/* The following two arrays use the following format:
	 * 		si[p][m]
	 * 		p:= int corresponding to part of speech
	 * 		m:= number of syllables
	 */
	private int[][] si;			//	stores start indices for words matching criteria
	private int[][] sz;			//	stores number of words matching criteria

	private java.util.Random randGen;
}
