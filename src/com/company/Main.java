package com.company;

import java.io.*;
import java.util.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// I chose to use the Hash Table Representation
// Symbol table uses a map as container
// The hash function is the ASCII sum of all chars
// To prevent collisions, i simply increment the hashKey until i no longer have collisions
// The pos method returns the hashKey if the element is not already in the map or -1 otherwise
// add method adds the Key Value Tuple to the map


// Scanner Documentation
// First i read line by line the program, then split the line by whitespaces
// I add all these split tokens to a queue called unclassified tokens and then iterate trough it
// I check each one of them if they are token/separator --> add it to PIF accordingly
// Then i check if the token is either constant or identifier --> add it to PIF and ST
// If they are none of the above, i further check for other separators inside the token,
// If it contains a separators i use a split with look ahead and look behind in order to also keep the elements i split by
// I add the results to the unclassified tokens queue
// Add keep this process until all tokens from the line are either classified or i encounter a lexical Error

class MyTuple {
    String token;
    Number position;

    public MyTuple(String token, Number position) {
        this.position = position;
        this.token = token;
    }
}

class SymbolTable {
    private Map<Number, MyTuple> hashTable;
    private static int position = 1;

    public SymbolTable() {
        this.hashTable = new HashMap<>();
    }

    private int ASCIIsum(String token) {
        int sum = 0;
        int tokenLength = token.length();

        for (int i = 0; i < tokenLength; i++) {
            sum += token.charAt(i);
        }

        return sum;
    }

    public int position(String token) {
        int hashKey = ASCIIsum(token);
        MyTuple hashedValue = hashTable.get(hashKey);
//        System.out.println("THIS IS THE HASHED VAlUE  --> " + hashedValue);

//        if (hashedValue != null) {
            // if token is already found
            if (hashedValue != null && token.equals(hashedValue.token)) {
                return -1;
            }
            while (hashTable.containsKey(hashKey)) {
                hashKey += 1;
                hashedValue = hashTable.get(hashKey);
                if (hashedValue != null && token.equals(hashedValue.token)) {
                    return -1;
                }
            }
            return hashKey;
//        }
//        return -1;
    }

    public void add(String token, Number hashKey) {
//        System.out.println("ADDED INSIDE ST");
        if (hashKey.equals(-1)) {
            return;
        }
//        System.out.println(hashTable);
        hashTable.put(hashKey, new MyTuple(token, position++));
    }

    public Map<Number, MyTuple> getHashTable() {
        return hashTable;
    }
}

class MyScanner {
    private ArrayList<String> tokens;
    private ArrayList<String> separators;
    private ArrayList<String> escapedSeparators;
    private SymbolTable symbolTable;
    private Map<Integer, MyTuple> PIF = new HashMap<>();
    private String fileName;
    private static int addToPifIndex = 1;

    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public MyScanner(String fileName) {
        this.tokens = new ArrayList<>(Arrays.asList( "+", "%", "-","*","/", "=", "<", ">", "<=", ">=","<>","==","===","!=","&&","||","Start","End","const","let","int","array","char","string","read","write","check","else","while","for","break"));
        this.separators = new ArrayList<>(Arrays.asList( " ", "{","}","[","]",";",":","(",")","'","\n"));
        this.escapedSeparators = new ArrayList<>(Arrays.asList( " ", "\\{","\\}","\\[","\\]","\\;","\\:","\\(","\\)","\\'","\n"));
        this.symbolTable = new SymbolTable();
        this.fileName = fileName;
    }

    private void analyzeLine(String line, int lineCount) {
        String[] tokens = line.split("\\s+");
        Deque<String> unclassifiedTokens = new LinkedList<>(Arrays.asList(tokens));


        while (!unclassifiedTokens.isEmpty()) {
            String token = unclassifiedTokens.getFirst();
//            System.out.println("TOKEN ----> " +  token);

            unclassifiedTokens.removeFirst();
            if (token.isEmpty() || token.isBlank()) {
                continue;
            } else if (this.tokens.contains(token) || this.separators.contains(token)) {
                PIF.put(addToPifIndex++, new MyTuple(token, -1));
                continue;
            } else if (isConstant(token)) {
                Integer index = symbolTable.position(token);
                symbolTable.add(token, index);
                PIF.put(addToPifIndex++, new MyTuple("constant-" + token, index));
                continue;
            } else if (isIdentifier(token)) {
                Integer index = symbolTable.position(token);
                symbolTable.add(token, index);
                if (index != -1) {
//                    System.out.println("ADDED IN PIF ---> " + token);
                    PIF.put(addToPifIndex++, new MyTuple("identifier-" + token, index));
                }
                continue;
            }

            boolean wasTokenAdded = false;
            for (int index = 0; index < this.separators.size(); index++) {
                String separator = this.separators.get(index);
                if (token.contains(separator)) {
                  String separatorWithDelimiter = escapedSeparators.get(index);
                  String[] splitToken = token.split(String.format(WITH_DELIMITER, separatorWithDelimiter));

                  int splitTokenLength = splitToken.length - 1;
                  for (int j = splitTokenLength; j >= 0; j--) {
                      unclassifiedTokens.addFirst(splitToken[j]);
                  }

                  wasTokenAdded = true;
                  break;
                }
            }

            if (wasTokenAdded)
                continue;
            throw new RuntimeException("Lexical error on the line " + lineCount + " token " + token + " is not recognized by the scanner");
        }
    }

    private boolean isIdentifier(String token) {
//        Pattern identifier = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9]*");
//        Matcher match = identifier.matcher(token);
//        return match.matches();

        FiniteAutomata fa = new FiniteAutomata("/Users/newuser/IdeaProjects/Flcd_Lab2/src/Identifier.in");
        return  fa.checkSequence(token);
    }

    private boolean isConstant(String token) {
        return isBoolean(token) || isInteger(token) || isString(token);
    }

    private boolean isBoolean(String token) {
        return token.equals("true") || token.equals("false");
    }

    private boolean isInteger(String token) {
//        String regexExpression = "0|(-?[1-9][0-9]*)";
//        Pattern identifier = Pattern.compile(regexExpression);
//        Matcher match = identifier.matcher(token);
//        return match.matches();

        FiniteAutomata fa = new FiniteAutomata("/Users/newuser/IdeaProjects/Flcd_Lab2/src/FaInteger.in");
        return  fa.checkSequence(token);
    }

    private boolean isString(String token) {
        String regexExpression = "\"([0-9A-Za-z])+\"";
        Pattern identifier = Pattern.compile(regexExpression);
        Matcher match = identifier.matcher(token);
        return match.matches();
    }

    private void writePifToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("Pif.out"));
            PIF.forEach((key, value) -> {
                try {
                    String stringifyKey = value.token;
                    if (value.token.contains("constant")) {
                        stringifyKey = "constant";
                    }
                    if (value.token.contains("identifier")) {
                        stringifyKey = "identifier";
                    }
                    writer.write(stringifyKey + " - " + value.position + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeStToFile() {
//        System.out.println("MENS");
//        System.out.println(symbolTable.getHashTable());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("ST.out"));
            symbolTable.getHashTable().forEach((key, value) -> {
                try {
                    writer.write(key.toString() + "-" + "[" + value.token + " " + value.position + "]" + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void scan() throws RuntimeException {
        try {
            int lineCount = 0;
            File prg = new File(this.fileName);
            Scanner reader = new Scanner(prg);

            while (reader.hasNextLine()) {
                lineCount++;
                String lineToAnalyze = reader.nextLine();
                try {
                    this.analyzeLine(lineToAnalyze, lineCount);
                } catch (RuntimeException err) {
                    this.writePifToFile();
                    this.writeStToFile();
                    throw new RuntimeException(err.getMessage());
                }
            }
            System.out.println("LEXICALLY CORRECT WOAH");
            reader.close();
            this.writePifToFile();
            this.writeStToFile();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

class MyOtherTuple {
    private final String first;
    private final String second;

    public String getFirst() {
        return this.first;
    }

    public String getSecond() {
        return this.second;
    }

    public MyOtherTuple(String first, String second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return 42069;
    }

    @Override
    public String toString()
    {
        return "(" + this.first + ", " + this.second + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if(! (o instanceof MyOtherTuple myOtherTuple))
            return false;

        return (myOtherTuple.getFirst().equals(this.first) && myOtherTuple.getSecond().equals(this.second));
    }
}

class FiniteAutomata {

    public List<String> states, alphabet, finalStates;

    public String initialState;

    public Map<MyOtherTuple,List<String>> transitions;

    public FiniteAutomata(String filename)
    {
        this.states = new ArrayList<>();

        this.alphabet = new ArrayList<>();

        this.finalStates = new ArrayList<>();

        this.transitions = new HashMap<>();

        readElements(filename);
    }

    public void readElements(String filename)  {

        try {
            File file = new File(filename);
            Scanner reader = new Scanner(file);

            String statesLine = reader.nextLine();
            this.states = Arrays.asList(statesLine.split(" "));

            String alphabetLine = reader.nextLine();
            this.alphabet = Arrays.asList(alphabetLine.split(" "));

            this.initialState = reader.nextLine();

            String finalStatesLine = reader.nextLine();

            this.finalStates = Arrays.asList(finalStatesLine.split(" "));

            while(reader.hasNextLine())
            {
                String[] transition = reader.nextLine().split(" ");
                MyOtherTuple newPair = new MyOtherTuple(transition[0],transition[1]);

                if(this.transitions.containsKey(newPair))
                {
                    if(!this.transitions.get(newPair).contains(transition[2]))
                        this.transitions.get(newPair).add(transition[2]);
                }
                else {
                    this.transitions.put(new MyOtherTuple(transition[0], transition[1]), new ArrayList<>(Collections.singletonList(transition[2])));
                };
            }


        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isDeterministic()
    {
        for(MyOtherTuple key: this.transitions.keySet())
        {
            if(this.transitions.get(key).size() > 1)
            {
                return false;
            }
        }
        return true;
    }

    public boolean checkSequence(String sequence){
        if(!isDeterministic())
        {
            return false;
        }
        else{
            String currentState = this.initialState;
            for(char c: sequence.toCharArray())
            {
                MyOtherTuple myTuple = new MyOtherTuple(currentState,String.valueOf(c));
                if(this.transitions.containsKey(myTuple))
                {
                    currentState = this.transitions.get(myTuple).get(0);
                }
                else {return false;}
            }

            return this.finalStates.contains(currentState);
        }
    }
    public String getStates() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("States: ------>");
        for(String s: this.states)
        {
            stringBuilder.append(s).append(" ");
        }
        return stringBuilder.toString();
    }

    public String getAlphabet() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Alphabet: ------>");
        for(String s: this.alphabet)
        {
            stringBuilder.append(s).append(" ");
        }
        return stringBuilder.toString();
    }

    public String getFinalStates() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Final States: ------> ");
        for(String s: this.finalStates)
        {
            stringBuilder.append(s).append(" ");
        }
        return stringBuilder.toString();
    }

    public String getTransition() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Transitions: ------>: ");
        for(var s: this.transitions.entrySet())
        {
            stringBuilder.append(s.getKey()).append("->");
            stringBuilder.append(s.getValue());
        }
        return stringBuilder.toString();
    }
}

public class Main {
    public static void main(String[] args) {
//        MyScanner myScanner = new MyScanner("/Users/newuser/IdeaProjects/Flcd_Lab2/src/com/company/p1.in");
//        MyScanner myScanner = new MyScanner("/Users/newuser/IdeaProjects/Flcd_Lab2/src/p2.in");
//        try {
//            myScanner.scan();
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        }

        FiniteAutomata FA = new FiniteAutomata("/Users/newuser/IdeaProjects/Flcd_Lab2/src/Identifier.in");
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while(running)
        {
            System.out.println("Select option: ");
            System.out.println("1: get the states");
            System.out.println("2: get the alphabet");
            System.out.println("3: get the final states");
            System.out.println("4: get the transitions");
            System.out.println("5: check if deterministic");
            System.out.println("5: Check a given sequence");
            String writtenValue = scanner.nextLine();
            switch (writtenValue) {
                case "1" -> System.out.println(FA.getStates());
                case "2" -> System.out.println(FA.getAlphabet());
                case "3" -> System.out.println(FA.getFinalStates());
                case "4" -> System.out.println(FA.getTransition());
                case "5" -> System.out.println(FA.isDeterministic());
                case "6" -> {
                    System.out.println("Check sequence");
                    String sequence = scanner.nextLine();

                    System.out.println(FA.checkSequence(sequence));
                }
                case "0" -> running = false;
                default -> System.out.println("Please select of the options!");
            }
        }
    }
}

// 1. b
