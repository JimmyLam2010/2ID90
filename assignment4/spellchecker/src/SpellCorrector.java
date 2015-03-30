import java.util.Arrays;
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
        int i = 0;
        while (i < length) {
            for (int j = 0; j < ALPHABET.length; j++) {
                //Substitution
                char[] letters = word.toCharArray();
                letters[i] = ALPHABET[j];
                ListOfWords.add(Arrays.toString(letters));
                //Insertion
                if (i < length - 1) {
                    ListOfWords.add(word.substring(0,i) + ALPHABET[j] + word.substring(i+1,length));
                    //Transposition
                    char x = letters[i];
                    char y = letters[i+1];
                    letters[i] = y;
                    letters[i+1] = x;
                    ListOfWords.add(Arrays.toString(letters));
                    //Deletion
                    ListOfWords.add(word.substring(0,i) + word.substring(i+1,length));
                }
                else {
                    //Boundary case insertion
                    ListOfWords.add(word.substring(0,i) + ALPHABET[j]);
                    //Boundary case deletion
                    ListOfWords.add(word.substring(0,i));
                }
                i++;
            }
        }
        return cr.inVocabulary(ListOfWords);
    }          
}
