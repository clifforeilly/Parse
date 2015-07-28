import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVWriter;
import au.com.bytecode.opencsv.CSVReader;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Created by cliff on 06/01/2014.
 */

//args
// 0=input folder
// 1=output folder
// 2=type
//  1: plain text file
//  2: csv line per sentence with associated extra detail
//  3: csv two first columns are sentences to be parsed
// 3=data column (for type 3 the second data column is assumed to be the next one)
// 4=binary for delete content of output folder before starting parse

public class Parse
{
    static String InputFileFolder = "";
    static String OutputFileFolder = "";
    static int type = 0;
    static List<String> nounNodeNames;
    static List<String> verbNodeNames;
    static List<String> adjectiveNodeNames;
    static List<String> adverbNodeNames;
    static List<String> determinerNodeNames;
    static List<String> prepositionNodeNames;
    static List<String> conjunctionNodeNames;
    static List<String> interjectionNodeNames;
    static List<String> pronounNodeNames;
    static StanfordCoreNLP pipeline;
    static int parsedColumns = 7;


    public static void main(String[] args)
    {
        try
        {
            if(args.length>0)
            {
                System.out.println("Parameter 1:" + args[0]);
                InputFileFolder=args[0];
                System.out.println("Parameter 2:" + args[1]);
                OutputFileFolder=args[1];
                System.out.println("Parameter 3:" + args[2]);
                type=Integer.parseInt(args[2]);

                if(Integer.parseInt(args[4])==1)      //delete the output before parsing
                {
                    System.out.println("Parameter 5:" + args[4] +" ... therefore, deleting the output folder files");

                    File f = new File(OutputFileFolder);
                    File[] matchingFiles = f.listFiles();

                    if(matchingFiles!=null)
                    {
                        for(File tf : matchingFiles)
                        {
                            tf.delete();
                        }
                    }

                    System.out.println("Output folder files deleted");
                }

                if(type==1)  //plain text file
                {
                    File f = new File(InputFileFolder);
                    File[] matchingFiles = f.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".txt");
                        }
                    });
                    System.out.println("Scanning ... " + InputFileFolder);
                    int filecount=0;
                    List<String[]> Lins = new ArrayList<String[]>();

                    setupLookups();
                    Properties props = new Properties();
                    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
                    pipeline = new StanfordCoreNLP(props);

                    int rowcount = 0;
                    String InText = "";

                    for(File tf : matchingFiles)
                    {
                        BufferedReader br = new BufferedReader(new FileReader(tf.getAbsoluteFile()));
                        try {
                            StringBuilder sb = new StringBuilder();
                            String line = br.readLine();

                            while (line != null) {
                                sb.append(line);
                                sb.append(System.lineSeparator());
                                line = br.readLine();
                            }
                            String everything = sb.toString();
                            System.out.print(everything);
                            InText = everything;

                        } finally {
                            br.close();
                        }

                        Lins=parse(2, InText);

                        String[] rowout;
                        CSVWriter csvout = new CSVWriter(new FileWriter(OutputFileFolder + File.separator + "parsed-" + tf.getName().replace("txt", "csv")));

                        for(String[] t : Lins)
                        {
                            if(!t[parsedColumns-1].equals(".") & !t[parsedColumns-1].equals(",") & !t[parsedColumns-1].equals("!") & !t[parsedColumns-1].equals("?"))
                            {
                                if(t[6].equals("tendency"))
                                {
                                    int y = 0;
                                }

                                rowout = new String[parsedColumns + 1];
                                int a = 0;

                                rowout[a]=tf.getName();
                                a++;

                                for(String s2 : t)
                                {
                                    rowout[a]=s2;
                                    a++;
                                }
                                csvout.writeNext(rowout);
                                rowcount++;
                                System.out.println("Written a row: " + rowcount);
                            }
                        }
                        csvout.close();
                    }
                }

                if(type==2)  //csv line per sentence with associated extra detail
                {
                    System.out.println(args[3]);
                    int dataCol = Integer.parseInt(args[3])-1;
                    File f = new File(InputFileFolder);
                    File[] matchingFiles = f.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".csv");
                        }
                    });
                    System.out.println("Scanning ... " + InputFileFolder);
                    int filecount=0;
                    List<String[]> Lins = new ArrayList<String[]>();

                    setupLookups();
                    Properties props = new Properties();
                    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
                    pipeline = new StanfordCoreNLP(props);

                    int rowcount = 0;

                    for(File tf : matchingFiles)
                    {
                        filecount++;
                        CSVReader csvinput = new CSVReader(new FileReader(tf.getAbsolutePath()));
                        List csvinputdata = csvinput.readAll();
                        csvinput.close();
                        String[] row;
                        String[] rowout;

                        CSVWriter csvout = new CSVWriter(new FileWriter(OutputFileFolder + File.separator + "parsed-" + tf.getName()));

                        for(Object ob : csvinputdata)
                        {
                            row=(String[]) ob;
                            Lins=parse(2, row[dataCol]);

                            for(String[] t : Lins)
                            {
                                if(!t[parsedColumns-1].equals(".") & !t[parsedColumns-1].equals(",") & !t[parsedColumns-1].equals("!") & !t[parsedColumns-1].equals("?"))
                                {
                                    rowout = new String[row.length + parsedColumns + 1];
                                    int a = 0;

                                    for(String s : row)
                                    {
                                        rowout[a] = s;
                                        a++;
                                    }

                                    rowout[a]=tf.getName();
                                    a++;

                                    for(String s2 : t)
                                    {
                                        rowout[a]=s2;
                                        a++;
                                    }
                                    csvout.writeNext(rowout);
                                    rowcount++;
                                    System.out.println("Written a row: " + rowcount);
                                }
                            }

                        }

                        csvout.close();
                    }
                }

                if(type==3)   //csv two first columns are sentences to be parsed
                {
                    int includedconfidence = 0; //set this to 1 if there's a confidence value in the 4th column
                    System.out.println(args[3]);
                    int dataCol = Integer.parseInt(args[3])-1;
                    int dataCol2 = Integer.parseInt(args[3]);
                    File f = new File(InputFileFolder);
                    File[] matchingFiles = f.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".csv");
                        }
                    });
                    System.out.println("Scanning ... " + InputFileFolder);
                    int filecount=0;
                    List<String[]> Lins = new ArrayList<String[]>();

                    setupLookups();
                    Properties props = new Properties();
                    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
                    pipeline = new StanfordCoreNLP(props);

                    int rowcount = 0;

                    for(File tf : matchingFiles)
                    {
                        filecount++;
                        CSVReader csvinput = new CSVReader(new FileReader(tf.getAbsolutePath()));
                        List csvinputdata = csvinput.readAll();
                        csvinput.close();
                        String[] row;
                        String[] rowout;
                        int pair = 0;
                        String first = "true";


                        CSVWriter csvout = new CSVWriter(new FileWriter(OutputFileFolder + File.separator + "parsed-" + tf.getName()));

                        for(Object ob : csvinputdata)
                        {
                            pair++;
                            first="true";
                            row=(String[]) ob;

                            //first sentence
                            Lins=parse(2, row[dataCol]);

                            for(String[] t : Lins)
                            {
                                if(!t[parsedColumns-1].equals(".") & !t[parsedColumns-1].equals(",") & !t[parsedColumns-1].equals("!") & !t[parsedColumns-1].equals("?") & !t[parsedColumns-1].equals(";") & !t[parsedColumns-1].equals(":"))
                                {
                                    rowout = new String[row.length + parsedColumns + 5];
                                    int a = 0;

                                    for(String s : row)
                                    {
                                        rowout[a] = s;
                                        a++;
                                    }

                                    rowout[a]=tf.getName();
                                    a++;

                                    rowout[a]=String.valueOf(pair);
                                    a++;

                                    rowout[a]=first;
                                    a++;

                                    for(String s2 : t)
                                    {
                                        rowout[a]=s2;
                                        a++;
                                    }

                                    if(rowout[10 + includedconfidence].equals("Noun"))
                                    {
                                        rowout[a]=rowout[a-1];
                                        a++;
                                    }

                                    if(!rowout[11 + includedconfidence].equals("O"))
                                    {
                                        rowout[a]=rowout[8 + includedconfidence];
                                        //a++;
                                    }

                                    csvout.writeNext(rowout);
                                    rowcount++;
                                    System.out.println("Written a row: " + rowcount);
                                }
                            }

                            //second sentence
                            Lins=parse(2, row[dataCol2]);
                            first="false";

                            for(String[] t : Lins)
                            {
                                if(!t[parsedColumns-1].equals(".") & !t[parsedColumns-1].equals(",") & !t[parsedColumns-1].equals("!") & !t[parsedColumns-1].equals("?") & !t[parsedColumns-1].equals(";") & !t[parsedColumns-1].equals(":"))
                                {
                                    rowout = new String[row.length + parsedColumns + 5];
                                    int a = 0;

                                    for(String s : row)
                                    {
                                        rowout[a] = s;
                                        a++;
                                    }

                                    rowout[a]=tf.getName();
                                    a++;

                                    rowout[a]=String.valueOf(pair);
                                    a++;

                                    rowout[a]=first;
                                    a++;

                                    for(String s2 : t)
                                    {
                                        rowout[a]=s2;
                                        a++;
                                    }

                                    if(rowout[10 + includedconfidence].equals("Noun"))
                                    {
                                        rowout[a]=rowout[a-1];
                                        a++;
                                    }

                                    if(!rowout[11 + includedconfidence].equals("O"))
                                    {
                                        rowout[a]=rowout[8 + includedconfidence];
                                        //a++;
                                    }

                                    csvout.writeNext(rowout);
                                    rowcount++;
                                    System.out.println("Written a row: " + rowcount);
                                }
                            }

                        }

                        csvout.close();
                    }

                }

            }
            else
            {
                System.out.println("Error - no parameters supplied");
            }

        }
        catch (Exception ex)
        {
            System.out.println("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

    }

    static List<String[]> parse(int type, String corpus)
    {
        List<String[]> Louts = new ArrayList<String[]>();
        try
        {
            String[] outs = null;
            Annotation doc = new Annotation(corpus);
            pipeline.annotate(doc);
            List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

            int s=0;
            for(CoreMap sentence : sentences)
            {
                s++;

                int w=0;
                for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class))
                {
                    outs = new String[parsedColumns];
                    w++;
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                    String lem = token.get(CoreAnnotations.LemmaAnnotation.class);
                    String postype = PartOfSpeechType(pos);

                    outs[0]=String.valueOf(s);
                    outs[1]=String.valueOf(w);
                    outs[2]=word;
                    outs[3]=pos;
                    outs[4]=postype;
                    outs[5]=ne;
                    outs[6]=lem;

                    Louts.add(outs);
                }

            }

        }
        catch (Exception ex)
        {
            System.out.println("Error:-" + ex.toString() + ", " + ex.getMessage() + ", " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

        return Louts;
    }

    static String PartOfSpeechType(String pos)
    {
        String type = "";
        if(nounNodeNames.contains(pos))
        {
            type="Noun";
        }
        if(nounNodeNames.contains(pos))
        {
            type="Noun";
        }
        if(verbNodeNames.contains(pos))
        {
            type="Verb";
        }
        if(adjectiveNodeNames.contains(pos))
        {
            type="Adjective";
        }
        if(adverbNodeNames.contains(pos))
        {
            type="Adverb";
        }
        if(conjunctionNodeNames.contains(pos))
        {
            type="Conjunction";
        }
        if(determinerNodeNames.contains(pos))
        {
            type="Determiner";
        }
        if(prepositionNodeNames.contains(pos))
        {
            type="Preposition";
        }
        if(interjectionNodeNames.contains(pos))
        {
            type="Interjection";
        }

        return type;
    }

    static void setupLookups()
    {
        nounNodeNames = new ArrayList<String>();
        nounNodeNames.add( "NP");
        nounNodeNames.add( "NP$");
        nounNodeNames.add( "NPS");
        nounNodeNames.add( "NN");
        nounNodeNames.add( "NN$");
        nounNodeNames.add( "NNS");
        nounNodeNames.add( "NNS$");
        nounNodeNames.add( "NNP");
        nounNodeNames.add( "NNPS");

        verbNodeNames = new ArrayList<String>();
        verbNodeNames.add( "VB");
        verbNodeNames.add( "VBD");
        verbNodeNames.add( "VBG");
        verbNodeNames.add( "VBN");
        verbNodeNames.add( "VBP");
        verbNodeNames.add( "VBZ");
        verbNodeNames.add( "MD" );

        adjectiveNodeNames = new ArrayList<String>();
        adjectiveNodeNames.add( "JJ");
        adjectiveNodeNames.add( "JJR");
        adjectiveNodeNames.add( "JJS");

        adverbNodeNames = new ArrayList<String>();
        adverbNodeNames.add( "RB");
        adverbNodeNames.add( "RBR");
        adverbNodeNames.add( "RBS");

        determinerNodeNames = new ArrayList<String>();
        determinerNodeNames.add( "DT");

        prepositionNodeNames = new ArrayList<String>();
        prepositionNodeNames.add( "IN");

        conjunctionNodeNames = new ArrayList<String>();
        conjunctionNodeNames.add( "CC");

        interjectionNodeNames = new ArrayList<String>();
        interjectionNodeNames.add( "UH");

        pronounNodeNames = new ArrayList<String>();
        pronounNodeNames.add( "PRP");
        pronounNodeNames.add( "PRP$");
    }
}
