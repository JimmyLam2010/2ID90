import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class SpellCorrector {
    final private CorpusReader cr;
    final private ConfusionMatrixReader cmr;
    
    final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz'".toCharArray();
    
    
    public SpellCorrector(CorpusReader cr, ConfusionMatrixReader cmr) 
    {
        this.cr = cr;
        this.cmr = cmr;
    }
    
    public String correctPhrase(String phrase) throws IOException
    {
        if(phrase == null || phrase.length() == 0)
        {
            throw new IllegalArgumentException("phrase must be non-empty.");
        }
            
        String[] words = phrase.split(" ");
        String finalSuggestion = "";
        HashSet<String> candidateWords;
        TreeMap<Double, String> bigramProbabilities = new TreeMap<>();
        double likelihood = 0;
        double prior = 0;
        double channelProbability = 0;
        double LAMBDA = 1;
        double SCALE_FACTOR = 1;
        double bigramProbability = 0;
        
        for (String word : words) {
            if(!cr.inVocabulary(word)) {
                double currentProbability = - Double.MAX_VALUE;
                String correctedWord = "";
                candidateWords = getCandidateWords(word);
                for (String candidateWord : candidateWords) {
                    likelihood = calculateChannelModelProbability(candidateWord, word);
                    prior = (double)cr.getNGramCount(candidateWord) / (double)cr.numberOfWords();
                    channelProbability = likelihood * Math.pow(prior,LAMBDA) * SCALE_FACTOR;
                    if (Arrays.asList(words).indexOf(word) == 0) {
                        bigramProbability = cr.getSmoothedCount(candidateWord + " " + words[Arrays.asList(words).indexOf(word) + 1]) * channelProbability;
                        bigramProbabilities.put(bigramProbability,candidateWord);
                    }
                    else if (Arrays.asList(words).indexOf(word) == words.length) {
                        bigramProbability = cr.getSmoothedCount(words[Arrays.asList(words).indexOf(word) - 1] + " " +candidateWord) * channelProbability;
                        bigramProbabilities.put(bigramProbability,candidateWord);
                    }
                    else {
                        bigramProbability = cr.getSmoothedCount(words[Arrays.asList(words).indexOf(word) - 1] + " " + candidateWord) * 
                        cr.getSmoothedCount(candidateWord + " " + words[Arrays.asList(words).indexOf(word) + 1]) * channelProbability;
                        bigramProbabilities.put(bigramProbability,candidateWord);
                    }
                    if (bigramProbability >currentProbability) {
                        currentProbability = bigramProbability;
                        correctedWord = candidateWord;
                    }
                }
                finalSuggestion = finalSuggestion + correctedWord + " ";
            }
            else {
                finalSuggestion = finalSuggestion + word + " ";
            }
            System.out.println(bigramProbability);
        } 
        return finalSuggestion.trim();
    }
    
    public double calculateChannelModelProbability(String suggested, String incorrect) throws IOException 
    {
        String error;
        String correct;
        double likelihood = 0;
        char[] incorrectLetters = incorrect.toCharArray();
        char[] suggestedLetters = suggested.toCharArray();
        //Calculate the chances of the error having been caused by deletion (x typed instead of xy)
        if (incorrect.length() < suggested.length()) {
            //Doesn't check last letter as this would cause problems with the comparison in the if statement.
            for (int i = 0; i < incorrect.length(); i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                   error = Character.toString(incorrectLetters[i]);
                   correct = Character.toString(suggestedLetters[i]) + Character.toString(suggestedLetters[i+1]);
                   likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
                }
            }
            //If the deleted letter is the last letter of the word.
            if (Character.toString(incorrectLetters[incorrect.length() - 1]).equals(Character.toString(suggestedLetters[suggested.length() - 2]))) {
                error = Character.toString(incorrectLetters[incorrect.length() - 1]);
                correct = Character.toString(suggestedLetters[suggested.length() - 2]) + 
                          Character.toString(suggestedLetters[suggested.length() - 1]);
                likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
            }
        }        
        //Calculate the chances of the error having been caused by instertion (xy typed instead of x)
        else if (incorrect.length() > suggested.length()) {
            for (int i = 1; i < suggested.length(); i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                    error = Character.toString(incorrectLetters[i-1]) + Character.toString(incorrectLetters[i]);
                    correct = Character.toString(suggestedLetters[i-1]);
                    likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
                }    
            }
            //If the letter was inserted after the final letter of the suggested word.
            if (Character.toString(incorrectLetters[incorrect.length() - 2]).equals(Character.toString(suggestedLetters[suggested.length() - 1]))) {
                error = Character.toString(incorrectLetters[incorrect.length() - 2]) +
                        Character.toString(incorrectLetters[incorrect.length() - 1]);
                correct = Character.toString(suggestedLetters[suggested.length() - 1]);
                likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
            }
        }
        else {
            for (int i = 0; i < incorrect.length() - 1; i++) {
                if (incorrectLetters[i] != suggestedLetters[i]) {
                    //Substitution
                    if (incorrectLetters[i+1] == suggestedLetters[i+1]) {
                        error = Character.toString(incorrectLetters[i]);
                        correct = Character.toString(suggestedLetters[i]);
                        likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
                    }
                    //Transposition
                    else {
                        error = Character.toString(incorrectLetters[i]) + Character.toString(incorrectLetters[i+1]);
                        correct = Character.toString(suggestedLetters[i]) + Character.toString(suggestedLetters[i+1]);
                        likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
                    }
                }    
            }
            //Last letter of the word has been substituted
            if (incorrectLetters[incorrect.length()-1] != suggestedLetters[suggested.length()-1] && 
                incorrectLetters[incorrect.length()-2] == suggestedLetters[suggested.length()-2]) {
                    error = Character.toString(incorrectLetters[incorrect.length()-1]);
                    correct = Character.toString(suggestedLetters[suggested.length()-1]);
                    likelihood = cmr.getConfusionCount(error, correct) / cr.characterCount(correct);
            }
        }
    return likelihood;    
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