<<<<<<< HEAD
import java.util.HashSet;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        for (String word : words) {
            if (cr.inVocabulary(word) == false) {
                getCandidateWords(word);
            } 
        } 
        
        /** CODE TO BE ADDED **/
        
        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) 
    {
         /** CODE TO BE ADDED **/
        
        return 0.0;
    }
         
      
    public HashSet<String> getCandidateWords(String word)
    {
        HashSet<String> ListOfWords = new HashSet<String>();
        
        /** CODE TO BE ADDED **/
        
        return cr.inVocabulary(ListOfWords);
    }          
=======
import java.util.HashSet;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase)
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        for (String word : words) {
            if (cr.inVocabulary(word) == false) {
                getCandidateWords(word);
            } 
        } 
        
        /** CODE TO BE ADDED **/
        
        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) 
    {
         /** CODE TO BE ADDED **/
        
        return 0.0;
    }
         
      
    public HashSet<String> getCandidateWords(String word)
    {
        HashSet<String> ListOfWords = new HashSet<String>();
        int length = word.length();
        char[] letters = word.toCharArray();
        int i = 0;
        while (i <= length) {
            for (int j = 0; j <= ALPHABET.length; j++) {
                letters[i] = ALPHABET[j];
                String possibleCandidate = letters.toString();
                ListOfWords.add(possibleCandidate);
            }
        }        
        return cr.inVocabulary(ListOfWords);
    }          
>>>>>>> origin/master
}