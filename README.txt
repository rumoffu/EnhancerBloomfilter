EnhancerBloomfilter
===================

Bloomfilter Implementation for Classifying Enhancers


Our Bloom filter implementation used a java version called BloomFilter.java which was
developed by previous research. 
(Melsted P, Pritchard JK (2011) Efficient counting of k-mers in DNA sequences using a
bloom filter.
Bioinformatics 12:333)

Our code itself is called bloom_driver.java which uses BloomFilter.java to do all 
Bloom filter functions.  

Our program is currently set to take in two text files in FASTA format which would 
be a file of known enhancer (positive) sequences and a file of known non-enhancer 
(negative) sequences.  Then, it uses 3/4 of the data to train a positive and negative 
Bloom filter.  Then it uses the remaining 1/4 of the data for testing to see the 
accuracy of classification.

In our bloom_driver.java code, we use a nested static class called "read" to handle 
the sequence reads and data about them.

Usage: Our code is currently hard-coded to open a file "enh_fb.fa" of enhancer 
sequences in FASTA format and a file "nullseqsi_200_1.fa" of non-enhancer sequences 
in FASTA format.  It also normalizes the data to use the same number of positive 
and negative sequences, and it runs the data for all 4 test cases.  It takes in two 
arguments: lower_kmer_len upper_kmer_len which are the lower and upper k-mer length 
limits to test.  It prints out the summary of results to "summary.txt" in a 
tab separated format.


The program:
1) Reads in the data
2) Sets up to train on 3/4 of the data and test on the remaining 1/4
3) Calculates the estimated size for each Bloom filter and generates the 
   positive and negative filters
4) Trains both Bloom filters
5) Runs the test data
6) Calculates and prints the results






