package haiku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;


public class Dictionary 
{	
	//-- stores information about loaded words
	private Map<String, PartOfSpeech> dictionary;
	
	
	/**
	 * Creates an empty Dictionary.
	 */
	public Dictionary() 
	{
		dictionary = new HashMap<String, PartOfSpeech>();
	}
	
	
	/**
	 * This constructor only loads the specified dictionary text file into memory.
	 * @param filename the dictionary text file to initially load
	 * @throws IOException specified dictionary txt file not found
	 */
	public Dictionary(String filename) throws IOException 
	{
		dictionary = new HashMap<String, PartOfSpeech>();
		
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
			dictionary.put(word, PartOfSpeech.ADJECTIVE);
			return true;
		}
		
		if(posString.contains(" ADVERB")) 
		{
			dictionary.put(word, PartOfSpeech.ADVERB);
			return true;
		}
		
		if(posString.contains(" PREPOSITION")) 
		{
			dictionary.put(word, PartOfSpeech.PREPOSITION);
			return true;
		}
		
		if(posString.contains(" ARTICLE")) 
		{
			dictionary.put(word, PartOfSpeech.ARTICLE);
			return true;
		}
		
		if(posString.contains(" NOUN")) 
		{
			dictionary.put(word, PartOfSpeech.NOUN);
			return true;
		}
		
		if(posString.contains(" VERB"))
		{
			dictionary.put(word, PartOfSpeech.VERB);
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
			for( Entry<String, PartOfSpeech> element : dictionary.entrySet())
				outFile.println(element.getKey() + " | " + element.getValue());
			
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
	
	/**
	 * Returns a set containing all dictionary words with the given part of speech.
	 */
	public Set<String> wordSet(PartOfSpeech pos) 
	{
		Set<String> set = new HashSet<String>();
		
		for( Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos)
				set.add(element.getKey());
		
		return set;
	}
	
	/**
	 * Returns a set containing all dictionary words that have both the specified part of speech,
	 *  and the specified number of syllables
	 */
	public Set<String> wordSet(PartOfSpeech pos, int syl) 
	{
		Set<String> set = new HashSet<String>();
		
		for( Entry<String, PartOfSpeech> item : dictionary.entrySet())
			if(item.getValue() == pos)
				if(sylCount(item.getKey()) == syl)
					set.add(item.getKey());
		
		return set;
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
		Set<String> set = new HashSet<String>();
		
		System.out.println("INITIAL WORDSET SIZE: " + set.size()
				+ "\n Populating with " + pos + " with length between " + sMin + " and " + sMax);
		
		for(Entry<String, PartOfSpeech> item : dictionary.entrySet())
			if(item.getValue() == pos) 
			{
				int syl = sylCount(item.getKey());
				
				if(sMin <= syl && syl <= sMax)
					set.add(item.getKey());
			}
		System.out.println("RETURNING WORD SET WITH SIZE: " + set.size());
		return set;
	}
	
	/**
	 *  Returns the part of speech of the given word.
	 */
	public PartOfSpeech getPOS(String word) {
		return dictionary.get(word);
	}
	
	
	
	
	/**********************************************************\
	 *	The following methods deal with syllable analysis.
	 *
	\**********************************************************/
	
	/**
	 * Counts the number of syllables in a word.
	 * Returns 0 if input word is null.
	 */
	public static int sylCount(String word) 
	{
		if(word == null) 
			return 0;
		
		word = word.trim().toUpperCase();	
		int vowels = vowelCount(word);
		
		if(vowels > 1)
			
			/*
			 *  The number of vowels is decreased if the word ends in -ed,
			 *  or -ly
			 */
			if(word.matches(".*[A-Z && [^AEIOUY]]ED?" + "LY?"))
				vowels--;
		
		return vowels - diphCount(word);
	}
	
	/**
	 * Counts the number of diphthongs (one-syllable vowel pairs) in a word.
	 */
	private static int diphCount(String word) 
	{
		if (word == null)
			return 0;
		
		word = word.toUpperCase();
		
		int count = 0;
		if(word.matches(".*A[EIUY].*")) 	count++;
		if(word.matches(".*E[AEIUY].*")) 	count++;
		if(word.matches(".*I[AEOU].*")) 	count++;
		if(word.matches(".*O[AIOUY].*")) 	count++;
		if(word.matches(".*U[AEIUY].*")) 	count++;
		if(word.matches(".*[A-Z&&[^AEIOU]]Y[AEIOU].*")) count++;
		
		return count;
	}
	
	/**
	 * Counts the number of vowels in a word.
	 */
	public static int vowelCount(String word) 
	{
		if (word == null)
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
	public static boolean isVowel(char c) 
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
	 * Returns true if this dictionary contains no elements (words).
	 */
	public boolean isEmpty() {
		return dictionary.isEmpty();
	}
	
	
	/**
	 * Returns the number of entries in this dictionary.
	 */
	public int size() {
		return dictionary.size();
	}
	
	
	/**
	 * Returns true if this dictionary contains the specified word.
	 */
	public boolean contains(String word) {
		return dictionary.containsValue(word);
	}
	
	
	/**
	 * Returns an array containing all of the words in this dictionary.
	 */
	public String[] toArray() 
	{	
		String[] array = new String[dictionary.size()];
		
		int i = 0;
		for(String item: dictionary.keySet()) 
		{
			array[i] = item;
			i++;
		}
		
		return array;
	}
	

	/**
	 * Add the specified word to this dictionary.
	 * 
	 * @return true if the new word was added successfully
	 */
	public boolean add(String word, PartOfSpeech pos) 
	{
		if (word == null || pos == null)
			return false;
		
		if (word.length() == 0)
			return false;
		
		if (pos == PartOfSpeech.BLANK)
			return false;

		dictionary.put(word, pos);
		return true;
	}
}