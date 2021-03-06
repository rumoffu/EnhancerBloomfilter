package bf;
import java.util.*;
import java.io.*;
public class bloom_driver{

  public static class read
  {
    //constructor
    read( String namey, String seqy, int len )
    {
      name   = namey;
      seq = seqy;
      kmer_len = len;
    }
    String name; // e.g. read1002
    String seq; // sequence (e.g. AGCT)
    int kmer_len; // k-mer length used
    int num_kmers = 0; // number of overlapping k-mers
    int num_pos_kmers = 0; // number of k-mers in the + filter
    int num_neg_kmers = 0; // number of k-mers in the - filter
    boolean actual_pos; // T/F if actually positive 
    boolean called_pos; // T/F if called positive

  }
  /**
   * @param args - an array with the lower k-mer length, and upper k-mer length
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException {

    boolean consoleprint = false; // to print results to the console
    double false_positive_probability = 0.1; // Bloom filter false probability rate desired
    int lower_kmer_len = 0; int upper_kmer_len = 0;
    boolean normalize = true;
    int testcase = 0;
    int kmer_size = 0;
    if(args.length == 2)
    {
      lower_kmer_len = Integer.parseInt(args[0]);
      upper_kmer_len = Integer.parseInt(args[1]);
    }
    else
    {
      System.out.println("usage: lower_k-mer_length(int) upper_k-mer_length(int)");
      System.exit(0);
    }
    //PrintWriter toList = new PrintWriter(new FileWriter("list.txt")); // to count unique k-mers
    PrintWriter toFile = new PrintWriter(new FileWriter("summary.txt"));
    toFile.println("k-mer_len\tTestcase\tNormalized\tPercent Positive Right\tPercent Negative Right\t" +
        "Average Percent Right\tPercent Positive filter Hits\tPercent Negative filter Hits\t" +
        "Runtime(ms)\tAverage Positive Hit Difference\tAverage Negative Hits Difference");
    //for normalize and non-normalizing
    for(int norm = 1; norm < 2; norm++)
    {
      if(norm == 0) normalize = false;
      else if(norm == 1) normalize = true;
    
      //for testcase rotation
      for(int tc = 1; tc <= 4; tc++)
      {
        testcase = tc;
        //for loop for kmer's
        for(int km = lower_kmer_len; km <= upper_kmer_len; km++)
        {
          // Get and store the current time -- for timing
          long runstart;
          runstart = System.currentTimeMillis();
      
          kmer_size = km;
          Scanner infile1 = new Scanner( new FileReader( "enh_fb.fa" ) );
          Scanner infile2 = new Scanner( new FileReader( "nullseqsi_200_1.fa" ) );
          String title;
          String seq;
          int in_size = 50000; //arbitrary size, assuming less than 50,000 reads in a file 
          read[] pos_reads = new read[in_size];
          read[] neg_reads = new read[in_size];
          int num_pos_reads = 0;
          int num_neg_reads = 0;
          int total_pos_length = 0;
          int total_neg_length = 0;
      
          while(infile1.hasNext())
          {//read in positive naively
            title = infile1.nextLine();
            if(infile1.hasNext())
            {
              seq = infile1.nextLine();
              pos_reads[num_pos_reads++] = new read(title.substring(1), seq, kmer_size);
              total_pos_length += seq.length();
            }
          }
          while(infile2.hasNext())
          {//read in negative naively
            title = infile2.nextLine();
            if(infile2.hasNext())
            {
              seq = infile2.nextLine();
              neg_reads[num_neg_reads++] = new read(title.substring(1), seq, kmer_size);
              total_neg_length += seq.length();
            }
          }

          if(normalize)
          { //ensure to use lower value
            if( num_neg_reads > num_pos_reads) num_neg_reads = num_pos_reads;
            else num_pos_reads = num_neg_reads; 
          }
          //test on 1/4, train on 3/4
          int[] ptest = new int[num_pos_reads / 4];
          int[] ntest = new int[num_neg_reads / 4];
          int[] ptrain = new int[num_pos_reads - num_pos_reads/4];
          int[] ntrain = new int[num_neg_reads - num_neg_reads/4];
          int pcount = 0; int ncount = 0; int pt = 0; int nt = 0;

          switch(testcase)
          {//set up data arrays based on data rotations
          default:
          case 1: //test on first 1/4, train on rest
            for(int i = 0; i < num_pos_reads / 4; i++)
              ptest[pt++] = i;
            for(int i = 0; i < num_neg_reads / 4; i++)
              ntest[nt++] = i;
            for(int i = num_pos_reads/4; i < num_pos_reads; i++)
              ptrain[pcount++] = i;
            for(int i = num_neg_reads/4; i < num_neg_reads; i++)
            ntrain[ncount++] = i;
            break;
          case 2://test on second 1/4, train on rest
            for(int i = num_pos_reads / 4; i < (2 * num_pos_reads) / 4; i++)
              ptest[pt++] = i;
            for(int i = num_neg_reads / 4; i < (2 * num_neg_reads) / 4; i++)
              ntest[nt++] = i;
            for(int i = 0; i < num_pos_reads / 4; i++)
              ptrain[pcount++] = i;
            for(int j = (2 * num_pos_reads) / 4; j < num_pos_reads; j++)
              ptrain[pcount++] = j;
            for(int i = 0; i < num_neg_reads / 4; i++)
              ntrain[ncount++] = i;
            for(int j = (2 * num_neg_reads) / 4; j < num_neg_reads; j++)
              ntrain[ncount++] = j;
            break;
          case 3://test on third 1/4, train on rest
            for(int i = (2 * num_pos_reads) / 4; i < (3 * num_pos_reads) / 4; i++)
              ptest[pt++] = i;
            for(int i = (2 * num_neg_reads) / 4; i < (3 * num_neg_reads) / 4; i++)
              ntest[nt++] = i;
            for(int i = 0; i < (2 * num_pos_reads) / 4; i++)
              ptrain[pcount++] = i;
            for(int j = (3 * num_pos_reads) / 4; j < num_pos_reads; j++)
              ptrain[pcount++] = j;
            for(int i = 0; i < (2 * num_neg_reads) / 4; i++)
              ntrain[ncount++] = i;
            for(int j = (3 * num_neg_reads) / 4; j < num_neg_reads; j++)
              ntrain[ncount++] = j;
            break;
          case 4://test on fourth 1/4, train on rest
            for(int i = (3 * num_pos_reads) / 4; i < num_pos_reads -1; i++)
              ptest[pt++] = i;
            for(int i = (3 * num_neg_reads) / 4; i < num_neg_reads -1; i++)
              ntest[nt++] = i;
            for(int i = 0; i < (3 * num_pos_reads) / 4; i++)
              ptrain[pcount++] = i;
            for(int i = 0; i < (3 * num_neg_reads) / 4; i++)
              ntrain[ncount++] = i;
            break;
          }
          //real training length is only 3/4 so calculate expected Bloom filter size
          int use_pos_length = 0; int use_neg_length = 0;
          for(int i = 0; i < ptrain.length; i++) 
          {
            use_pos_length += pos_reads[ptrain[i]].seq.length();
          }
          for(int i = 0; i < ntrain.length; i++) 
          {
            use_neg_length += neg_reads[ntrain[i]].seq.length();
          }
          
          //create the two Bloom filters
          BloomFilter<String> bloom_filter_pos = new BloomFilter<String>(false_positive_probability, use_pos_length);
          BloomFilter<String> bloom_filter_neg = new BloomFilter<String>(false_positive_probability, use_neg_length);
    
          String kmer = "";
          //training positive then negative filters
          for(int i = 0; i < ptrain.length; i++) 
          {//for training portion of the reads
            for(int j = 0; j <= pos_reads[ptrain[i]].seq.length() - kmer_size; j++) 
            {//for each character, make kmer of kmer_size
              kmer = pos_reads[ptrain[i]].seq.substring(j, j + kmer_size);
              bloom_filter_pos.add(kmer);   
            }
            pos_reads[ptrain[i]].num_kmers = pos_reads[ptrain[i]].seq.length() - kmer_size + 1;
            pos_reads[ptrain[i]].actual_pos = true;
          }
          for(int i = 0; i < ntrain.length; i++) 
          {//for training portion of the reads
            for(int j = 0; j <= neg_reads[ntrain[i]].seq.length() - kmer_size; j++) 
            {//for each character, make kmer of kmer_size
              kmer = neg_reads[ntrain[i]].seq.substring(j, j + kmer_size);
              bloom_filter_neg.add(kmer); 
            }
            neg_reads[ntrain[i]].num_kmers = neg_reads[ntrain[i]].seq.length() - kmer_size + 1;
            neg_reads[ntrain[i]].actual_pos = false;
          }

          //test on 1/4 of saved data - the test set
          int num_pos_right = 0; //number of positive reads called positive
          int num_neg_right = 0; //number of negative reads called negative
          double pphit = 0; double nphit = 0; //for tracking hit percent
          int num_pkmers = 0; int num_nkmers = 0; // for measuring hit percent
    
          //positive tests
          for(int i = 0; i < ptest.length; i++) 
          {//for test portion of the reads
            for(int j = 0; j <= pos_reads[ptest[i]].seq.length()-kmer_size; j++) 
            {//for each character make kmer of kmer_size
              kmer = pos_reads[ptest[i]].seq.substring(j, j+kmer_size);
              if(bloom_filter_pos.contains(kmer))
              {
                pos_reads[ptest[i]].num_pos_kmers++;
                /*if(!bloom_filter_neg.contains(kmer))//kmer + but not -
                {
                  toList.println(kmer);
                }*/
              }
              if(bloom_filter_neg.contains(kmer))
              {
                pos_reads[ptest[i]].num_neg_kmers++;  
              }
            }
            pos_reads[ptest[i]].num_kmers = pos_reads[ptest[i]].seq.length()-kmer_size + 1;
            pos_reads[ptest[i]].actual_pos = true;
            if( pos_reads[ptest[i]].num_pos_kmers - pos_reads[ptest[i]].num_neg_kmers > 0)
            {
              pos_reads[ptest[i]].called_pos = true;
              num_pos_right += 1;
            }
            else //more negative so call negative
            {
              pos_reads[ptest[i]].called_pos = false;
            }
            pphit += pos_reads[ptest[i]].num_pos_kmers;
            num_pkmers += pos_reads[ptest[i]].num_kmers;
          }
          //negative tests
          for(int i = 0 ; i < ntest.length; i++) 
          {//for test portion of the reads
            for(int j = 0; j <= neg_reads[ntest[i]].seq.length()-kmer_size; j++) 
            {//for each character, make kmer of kmer_size
              kmer = neg_reads[ntest[i]].seq.substring(j, j+kmer_size);
              if(bloom_filter_pos.contains(kmer))
              {
                neg_reads[ntest[i]].num_pos_kmers++;
              }
              if(bloom_filter_neg.contains(kmer))
              {
                neg_reads[ntest[i]].num_neg_kmers++;
              }
            }
            neg_reads[ntest[i]].num_kmers = neg_reads[ntest[i]].seq.length()-kmer_size + 1;
            neg_reads[ntest[i]].actual_pos = false;
            if( neg_reads[ntest[i]].num_pos_kmers - neg_reads[ntest[i]].num_neg_kmers > 0)
            {
              neg_reads[ntest[i]].called_pos = true;
            }
            else //more negative so call negative
            {
              neg_reads[ntest[i]].called_pos = false;
              num_neg_right += 1;
            }
            nphit += neg_reads[ntest[i]].num_pos_kmers;
            num_nkmers += neg_reads[ntest[i]].num_kmers;
          }
          
          ///String s = "" + kmer_size + "_" + testcase + "_" + normalize + ".txt";
          double ppos_neg_diff = 0; //absolute difference between # kmers called pos and neg
          double npos_neg_diff = 0; //same but for negative tests
          ///to print data for each file 
          ///PrintWriter toFile1 = new PrintWriter(new FileWriter(s));
          for(int i = 0; i < ptest.length; i++)
          {
            ppos_neg_diff += pos_reads[ptest[i]].num_pos_kmers - pos_reads[ptest[i]].num_neg_kmers;
            ///toFile1.println(i + "\t" + pos_reads[ptest[i]].num_pos_kmers + "\t" + 
            ///       pos_reads[ptest[i]].num_neg_kmers + "\t" + pos_reads[ptest[i]].num_kmers);
          }
          ///toFile1.println("NEGATIVE");
          for(int i = 0; i < ntest.length; i++)
          {
            npos_neg_diff += neg_reads[ntest[i]].num_pos_kmers - neg_reads[ntest[i]].num_neg_kmers;
            ///toFile1.println(i + "\t" + neg_reads[ntest[i]].num_pos_kmers + "\t" + 
            ///       neg_reads[ntest[i]].num_neg_kmers + "\t" + neg_reads[ntest[i]].num_kmers);
          }
          ///toFile1.close();
          
          double average_ppos_neg_difference = ppos_neg_diff / (ptest.length);
          double average_npos_neg_difference = npos_neg_diff / (ntest.length);
          double average_right = (num_pos_right + num_neg_right ) / (double) (ptest.length + ntest.length);
          if(consoleprint)
          {
            System.out.println("kmer_size: " + kmer_size);
            System.out.println("testcase: " + testcase);
            System.out.println("normalize: " + normalize);
            System.out.println("#positive right: " + num_pos_right);
            System.out.println("#positive tests: " + ptest.length);
            System.out.println("% positive right: " + num_pos_right / (double) ptest.length);
            System.out.println("#negative right: " + num_neg_right);
            System.out.println("#negtaive tests: " + ntest.length);
            System.out.println("% negative right: " + num_neg_right / (double) ntest.length);
            //System.out.println("average_ppos_neg_difference: " + average_ppos_neg_difference);
            double average_kmers = (total_pos_length + total_neg_length) / (num_pos_reads + num_neg_reads);
            System.out.println("average_kmers per read: " + average_kmers);
            System.out.println("average % right:"+"\t" + average_right);
          }
          String data = "";
          data = data + kmer_size + "\t";
          data = data + testcase + "\t";
          data = data + normalize + "\t";
          data = data + num_pos_right / (double) ptest.length + "\t";
          data = data + num_neg_right / (double) ntest.length + "\t";
          data = data + average_right + "\t"; //average right
          data = data + pphit / num_pkmers+ "\t";
          data = data + nphit / num_nkmers+ "\t";
          // Output search time 
          long elapsed = System.currentTimeMillis() - runstart;
          data = data + elapsed + "\t";
          data = data + average_ppos_neg_difference + "\t";
          data = data + average_npos_neg_difference + "\t";
          toFile.println(data);
        }//end kmer loop
      }//end testcase loop
    }//end normalize loop
    toFile.close(); // close summary.txt
    //toList.close(); // close list.txt
  } // end main

} // end bloom_driver.java
