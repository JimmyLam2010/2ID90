import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
            finalSuggestion = finalSuggestion + word + " ";
        } 
        
        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) 
    {
        //check if mistake is insertion
        String error;
        String correct;
        char[] incorrectLetters = incorrect.toCharArray();
        char[] suggestedLetters = suggested.toCharArray();
        //deletion
        if (incorrect.length() > suggested.length()) {
            for (int i = 0; i < suggested.length(); i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                   error = Character.toString(incorrectLetters[i-1]) + Character.toString(incorrectLetters[i]);
                   correct = Character.toString(suggestedLetters[i-1]);
                   return cmr.getConfusionCount(error, correct);
                }
            }
        }
        //insertion
        else if (incorrect.length() < suggested.length()) {
            for (int i = 0; i < incorrect.length(); i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                    error = Character.toString(incorrectLetters[i]);
                    correct = Character.toString(suggestedLetters[i-1]) + Character.toString(suggestedLetters[i]);;
                    return cmr.getConfusionCount(error, correct);
                }    
            }
        }
        else {
            for (int i = 0; i < incorrect.length(); i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                    error = Character.toString(incorrectLetters[i]);
                    correct = Character.toString(suggestedLetters[i]);
                    return cmr.getConfusionCount(error, correct);
                }    
            }
        }
        return 0.0;
    }     
      
    public HashSet<String> getCandidateWords(String word)
    {
        HashSet<String> ListOfWords = new HashSet<String>();
        int length = word.length();
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < ALPHABET.length; j++) {
                //Substitution
                char[] letters = word.toCharArray();
                letters[i] = ALPHABET[j];
                ListOfWords.add(Arrays.toString(letters));
                //Insertion
                if (i < length - 1) {
                    ListOfWords.add(word.substring(0,i) + ALPHABET[j] + word.substring(i+1,length));
                }
                else {
                    //Boundary case insertion
                    ListOfWords.add(word.substring(0,i) + ALPHABET[j]);
                }
            }
            if (i < length - 1) {
                //Transposition
                char[] letters = word.toCharArray();
                char x = letters[i];
                char y = letters[i+1];
                letters[i] = y;
                letters[i+1] = x;
                ListOfWords.add(Arrays.toString(letters));
                //Deletion
                ListOfWords.add(word.substring(0,i) + word.substring(i+1,length));
            }
            else {
                //Boundary case deletion
                ListOfWords.add(word.substring(0,i));               
            }
        }
        return cr.inVocabulary(ListOfWords);
    }          
}