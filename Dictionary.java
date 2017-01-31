package haiku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;


public class Dictionary
{	
	static final int MAX_SIZE = 100000;
	static final int TABLE_MAX_SIZE = 800;
	static final int WORD_MAX_SYL = 8;
	
	//protected HashMap<Integer, String>[] wordtables;
	
	protected HashSet<String>[] nountable;
	protected HashSet<String>[] verbtable;
	protected HashSet<String>[] advtable;
	protected HashSet<String>[] adjtable;
	protected HashSet<String>[] preptable;
	protected HashSet<String>[] arttable;
	
	/**
	 * Creates an empty Dictionary.
	 */
	public Dictionary() 
	{
		nountable = new HashSet[WORD_MAX_SYL+1];
		verbtable = new HashSet[WORD_MAX_SYL+1];
		advtable = new HashSet[WORD_MAX_SYL+1];
		adjtable = new HashSet[WORD_MAX_SYL+1];
		preptable = new HashSet[WORD_MAX_SYL+1];
		arttable = new HashSet[WORD_MAX_SYL+1];
		
		for (int i = 1; i <= WORD_MAX_SYL; i++)
		{
			nountable[i] = new HashSet<String>(tableInitSize(i));
			verbtable[i] = new HashSet<String>(tableInitSize(i));
			advtable[i] = new HashSet<String>(tableInitSize(i));
			adjtable[i] = new HashSet<String>(tableInitSize(i));
			preptable[i] = new HashSet<String>(tableInitSize(i));
			arttable[i] = new HashSet<String>(20);
		}
	}
	
	
	/**
	 * This constructor only loads the specified dictionary text file into memory.
	 * @param filename the dictionary text file to initially load
	 * @throws IOException specified dictionary txt file not found
	 */
	public Dictionary(String filename) throws IOException 
	{
		nountable = new HashSet[WORD_MAX_SYL + 1];
		verbtable = new HashSet[WORD_MAX_SYL + 1];
		advtable = new HashSet[WORD_MAX_SYL + 1];
		adjtable = new HashSet[WORD_MAX_SYL + 1];
		preptable = new HashSet[WORD_MAX_SYL + 1];
		arttable = new HashSet[WORD_MAX_SYL + 1];
		
		for (int i = 1; i <= WORD_MAX_SYL; i++)
		{
			nountable[i] = new HashSet<String>(tableInitSize(i));
			verbtable[i] = new HashSet<String>(tableInitSize(i));
			advtable[i] = new HashSet<String>(tableInitSize(i));
			adjtable[i] = new HashSet<String>(tableInitSize(i));
			preptable[i] = new HashSet<String>(tableInitSize(i));
			arttable[i] = new HashSet<String>(20);
		}
		
		load(filename);
	}
	
	
	/**
	 * Read the given dictionary text file, and add its contents to this class' internal dictionary.
	 * 
	 * @param filename the filename of a dictionary text file
	 * 
	 * @throws FileNotFoundException if the given file cannot be located
	 * 
	 * @return true if the dictionary file was loaded successfully
	 */
	public boolean load(String filename) throws IOException
	{
		if (filename == null)
			return false;
		if (!filename.toUpperCase().endsWith(".TXT"))
			return false;

		Scanner inFile = new Scanner(new File(filename));		

		while(inFile.hasNextLine()) 	
			loadItem(inFile.nextLine());
		
		inFile.close();
		return true;
	}
	
	
	private boolean loadItem(String entry)
	{
		// this line enforces entry format, (word | PARTOFSPEECH) without parenthesis 
		if (!entry.matches(".*|.*")) 
			return false;
		
		//prune everything in line before delimiter (inclusive)
		String posString = entry.substring(entry.indexOf('|') + 1);
		
		//prune everything in line after delimiter
		String word = entry.substring(0, entry.indexOf('|'));
		
		if(posString.contains(" ADJECTIVE")) 
		{
			adjtable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
		if(posString.contains(" ADVERB")) 
		{
			advtable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
		if(posString.contains(" PREPOSITION")) 
		{
			preptable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
		if(posString.contains(" ARTICLE")) 
		{
			arttable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
		if(posString.contains(" NOUN")) 
		{
			nountable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
		if(posString.contains(" VERB"))
		{
			verbtable[SyllableCounter.syllables(word)].add(word);
			return true;
		}
		
	
		return false;
	}

	
	/**
	 * Saves the dictionary loaded in memory to a specified text file.
	 * 
	 * @return true if the dictionary was saved successfully to the text file.
	 */
	public boolean save(String filename) 
	{
		if (filename == null)
			return false;
		if (!filename.toUpperCase().endsWith(".TXT"))
			return false;
		
		try {
			PrintWriter outFile = new PrintWriter(filename);
			
			for (int i = 1; i <= WORD_MAX_SYL; i++)
			{
				for (String word : nountable[i])
					outFile.println(word + " | " + "NOUN");
				for (String word : verbtable[i])
					outFile.println(word + " | " + "VERB");
				for (String word : adjtable[i])
					outFile.println(word + " | " + "ADJECTIVE");
				for (String word : advtable[i])
					outFile.println(word + " | " + "ADVERB");
				for (String word : preptable[i])
					outFile.println(word + " | " + "PREPOSITION");
				for (String word : arttable[i])
					outFile.println(word + " | " + "ARTICLE");
			}
			
			
			outFile.close();
			return true;
			
		} catch (IOException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/**********************************************************\
	 *	The following methods deal with reading and modifying 
	 * 	word elements in the internal dictionary.
	 *
	\**********************************************************/
	
	public String getNextWord(PartOfSpeech pos)
	{
		Random rand = new Random();
		Iterator it;
		int sylcount = rand.nextInt(WORD_MAX_SYL-1) + 1;
		
		switch(pos) {
		case NOUN:
			it = nountable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(nountable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case VERB:
			it = verbtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(verbtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ADJECTIVE:
			it = verbtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(verbtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ADVERB:
			it = advtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(advtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case PREPOSITION:
			it = preptable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(preptable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ARTICLE:
			it = arttable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(arttable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		default:
			return null;
		}
	}
	
	
	public String getNextWord(PartOfSpeech pos, int sylcount)
	{
		Random rand = new Random();
		Iterator it;
		
		switch(pos) {
		case NOUN:
			it = nountable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(nountable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case VERB:
			it = verbtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(verbtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ADJECTIVE:
			it = verbtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(verbtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ADVERB:
			it = advtable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(advtable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case PREPOSITION:
			it = preptable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(preptable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		case ARTICLE:
			it = arttable[sylcount].iterator();
			for (int i = 0; i < rand.nextInt(arttable[sylcount].size()); i++)
				it.next();
			return (String) it.next();
		default:
			return null;
		}
	}
	
	
	/**
	 * Returns a set containing all dictionary words with the given part of speech.
	 */
	public Set<String> wordSet(PartOfSpeech pos) 
	{
		HashSet<String> set = new HashSet<String>();
		
		switch(pos) {
		case NOUN:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(nountable[i]);
			break;
		case VERB:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(verbtable[i]);
			break;
		case ADJECTIVE:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(adjtable[i]);
			break;
		case ADVERB:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(advtable[i]);
			break;
		case PREPOSITION:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(preptable[i]);
			break;
		case ARTICLE:
			for (int i = 1; i <= WORD_MAX_SYL; i++)
				set.addAll(arttable[i]);
			break;
		}
		return set;
		
	}
	
	
	/**
	 * Returns a set containing all dictionary words that have both the specified part of speech,
	 *  and the specified number of syllables
	 */
	public Set<String> wordSet(PartOfSpeech pos, int syl) 
	{
		switch(pos) {
		case NOUN:
			return nountable[syl];
		case VERB:
			return verbtable[syl];
		case ADJECTIVE:
			return adjtable[syl];
		case ADVERB:
			return advtable[syl];
		case PREPOSITION:
			return preptable[syl];
		case ARTICLE:
			return arttable[syl];
		default:
			return new HashSet<String>();
		}
		
	}
	
	
	/**
	 * Returns a set containing all the dictionary words that have the specified part of speech, 
	 * as well as a syllable count between sMin and sMax (inclusive).
	 * 
	 * @param pos the part of speech
	 * @param sMin the smallest number of syllables usable
	 * @param sMax the largest number of syllables usable
	 */
	public Set<String> wordSet(PartOfSpeech pos, int sMin, int sMax) 
	{
		HashSet<String> set = new HashSet<String>();
		
		switch(pos) {
		case NOUN:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(nountable[i]);
			break;
		case VERB:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(verbtable[i]);
			break;
		case ADJECTIVE:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(adjtable[i]);
			break;
		case ADVERB:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(advtable[i]);
			break;
		case PREPOSITION:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(preptable[i]);
			break;
		case ARTICLE:
			for (int i = sMin; i <= sMax; i++)
				set.addAll(arttable[i]);
			break;
		}
		return set;
	}
	
	
	
	
	/**********************************************************\
	 *	The following methods deal with syllable analysis.
	 *
	\**********************************************************/
	
	/**
	 * Counts the number of syllables in a word.
	 * Returns 0 if input word is null.
	 */
	public static int sylCount(final String word) 
	{
		if (word == null || word.equals(""))
			return 0;
		
		int vowels = vowelCount(word);
		
		if (vowels > 1) {
			if (word.matches(".*less"))
				return 1 + sylCount(word.substring(0, word.lastIndexOf('l')));
			/*
			 *  The number of vowels is decreased if the word ends in -ed,
			 *  or -ly
			 */
			if (hasIrreg(word))
				vowels--;
		}
		
		return vowels - diphCount(word);
	}
	
	
	private static boolean hasIrreg(final String word) 
	{
		if (word.matches(".*" + "[A-Za-z&&[^AaEeIiOoUuYy]]" + "[Ee][DdSsRr]"))
			return true;
		if (word.matches(".*" + "[A-Za-z&&[^AaEeIiOoUuYy]]" + "[Ee]"))
			return true;
		if (word.matches(".*" + "[Ll][Yy]?"))
			return true;
		
		return false;
	}
	
	
	/**
	 * Counts the number of diphthongs (one-syllable vowel pairs) in a word.
	 * PRECONDITION: word is not null
	 */
	public static int diphCount(final String word) 
	{		
		if (word == null || word.equals(""))
			return 0;
		
		int count = 0;
		// 'AEro', '-AI-', 'AUburn', 'dAY'
		if(word.matches(".*[Aa][EeIiUuYy].*")) 	count++;
		// 'EArth', 'tEEth', '-EI-', '-EU-', 'hEY'
		if(word.matches(".*[Ee][AaEeIiUuYy].*")) 	count++;
		// '-IA-', '-IE-', '-IO-', '-IU-'
		if(word.matches(".*[Ii][AaOoUu].*")) 	count++;
		if (word.matches(".*[A-Za-z&&[^AaEeIiOoUu]][Ii][Ee][A-Za-z&&[^AaEeIiOoUu]&&[^Rr]].*"))	count++;
		// 'whOA', '-OI-', 'bOOth', 'mOUth', 'bOY'
		if(word.matches(".*[Oo][Aa].*")) 	count++;
		if (word.matches(".*[Oo][Ee].*")) 	count++;
		if (word.matches(".*[Oo][Ii].*")) 	count++;
		if (word.matches(".*[Oo][Oo].*")) 	count++;
		if (word.matches(".*[Oo][Uu].*")) 	count++;
		if (word.matches(".*[Oo][Yy].*")) 	count++;
		// 'UA', 'UE', 'UI', 'vacUUm', 'gUY'
		if(word.matches(".*[Uu][EeIiUuYy].*")) 	count++;
		// 'cistacEOUs'
		if (word.matches(".*[Ee][Oo][Uu].*"))		count++;
		if(word.matches(".*[^[A-Za-z&&[^AaEeIiOoUu]]][Yy][AaEeIiOoUu].*")) count++;
		
		return count;
	}
	
	/**
	 * Counts the number of vowels in a word.
	 */
	public static int vowelCount(final String word) 
	{
		if (word == null || word.equals(""))
			return 0;
		
		int count = 0;
		for(int i = 0; i < word.length(); i++)
			if(isVowel(word.charAt(i)))
				count++;
		
		return count;
	}
	
	
	/**
	 * Determines whether a given character is a vowel.
	 */
	public static boolean isVowel(final char c) 
	{
		switch(c) 
		{
		case 'A': 
		case 'a':	
		case 'E': 
		case 'e':
		case 'I': 
		case 'i':
		case 'O': 
		case 'o':
		case 'U': 
		case 'u':
		case 'Y': 
		case 'y':
				return true;
		default: 	
				return false;
		}
	}

	
	/**********************************************************\
	 *	The following methods provide information about the underlying data.
	 *
	\**********************************************************/
	
	
	

	/**
	 * Add the specified word to this dictionary.
	 * 
	 */
	public void add(String word, PartOfSpeech pos) 
	{	
		switch(pos) {
		case NOUN:
			nountable[SyllableCounter.syllables(word)].add(word);
			break;
		case VERB:
			verbtable[SyllableCounter.syllables(word)].add(word);
			break;
		case ADJECTIVE:
			adjtable[SyllableCounter.syllables(word)].add(word);
			break;
		case ADVERB:
			advtable[SyllableCounter.syllables(word)].add(word);
			break;
		case PREPOSITION:
			preptable[SyllableCounter.syllables(word)].add(word);
			break;
		case ARTICLE:
			arttable[SyllableCounter.syllables(word)].add(word);
			break;
		}
	}
	
	
	
	private int tableInitSize(int syl)
	{
		double temp = syl;
		
		return (int) (TABLE_MAX_SIZE / Math.ceil(temp / 2.0));
	}
}
