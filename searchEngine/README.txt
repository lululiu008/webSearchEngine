This package is a minimum functional web search engine.

It has three main parts:

--------- Hw3Reader
 |------- CompressSorter
 |------- SearchEngine


The Hw3Reader is for reading the crawled web pages from http://commoncrawl.org/ in gzip wet files. Each of these files the contains multiple records of crawled webpages and informations of the pages. Each record is like:

---------------------------------------------------------------
WARC/1.0
WARC-Type: conversion
WARC-Target-URI: http://02xxf.net/allxxxein/xxxxxxxxxxxxxxx
WARC-Date: 2016-09-24T20:51:09Z
WARC-Record-ID: <urn:uuid:6acc8899-80d5-429b-bd47-6aa762b23f5d>
WARC-Refers-To: <urn:uuid:59ecee1d-22db-4abf-a0c1-99239645ec91>
WARC-Block-Digest: sha1:RV7ONRDFR6QPLPAW4DAF3I5HKWSLELW3
Content-Type: text/plain
Content-Length: 5796

[text]
---------------------------------------------------------------

The record has url, crawl datetime, content type, content length, content and etc.

Two data strctures and several methods are defined and used in the Hw3Reader program:

Hw3Reader --------- Posting[] postingPool
           |------- UrlTable urlTable
           |
           |------- void outputPosting()
           |------- void parsingGzipFile(File file)
           |------- void parsingFolder(String folderName)
           |------- void outputUrlTable(String urlTableName)

The Posting class stores the keyword, the assigned docid of the page and the frequency of the keyword appearring in the page.

Posting --------- String keyword
         |------- int docId
         |------- int frequency

After finishing parsing, the posting records are output into disk chunkwise in order to avoid memory crashing. The records are sorted by alphabetic order of the keyword. Once the record array is filled, it is sorted and outputs the records into a chunk file. To sort the array, the Arrays.sort() method is used, the comparator is difined as comparing the word first and comparing the docID second. In this way, we could assure that records of same word cluster together and the docID are also sorted. The records with same word are sorted and merged together, and the docIDs are compressed by calculating the differences of neighboring records. Each chunk has 10M records and is about 60M size.

Each of the parsed urls are stored in a UrlRecord. The UrlRecord records the url and the content length of a page. The UrlRecords are stored in the UrlTable. After parsing all the gzip files, the records in UrlTable are stored into disk.

UrlRecord --------- String url
           |------- int length

UrlTable --------- List<UrlRecord> list
          |
          |------- void insertUrlRecord(UrlRecord urlRecord)
          |------- UrlRecord getUrlRecord(int index)
          |------- int size()

The Hw3Reader has a method named "parsingGzipFile", which is for parsing the words in a file. This method uses a parser originally adoped from Alek Dembowski and modified by myself to fit the reqirement of the project.
In the parsing process, I filtered out
1. the numbers and
2. the strings longer than 12 characters and
3. the strings starting with two duplicated characters and
4. the strings containing three tandom duplicated characters.
This reduced the total number of the parsed words by half.

The "parsingFolder" method parse all the gzip files in a directory by calling the "parsingGzipFile" method.
The "outputPosting" and "outputUrlTable" methods output the posting records into the "posting" folder and urlTable into "urlTable.txt" file, repectively. The parsing step takes about half to an hour to finish for 50 dataset. For 50 dataset, the urlTable file is about 222M.

Parser.java defines the parser class.

Each piece of the posting record has five columns.
First, the word;
Second, the docID of the first record of this word in this batch of records;
Third, a string recording the compressed docIDs and frequencies of the word;
Fourth, the docID of the last record of this word in this batch of records;
Fifth, the number of docs that having the word.
In this way, when we combining multiple records, we could calculated the differences of docIDs and concatenate them together.

The second step is to sort and merge the positing files in the "posting" folder into one single file named "sortedPosting.txt". The process is excuted using the unix sorting function by calling "sort -t ';' -k 1,1 -k 2,2n * > sortedPosting.txt". The records in all the posting files will be sorted by the keywords and records having same keyword will be merged into one record. This process takes about 15min. The output is about 5G.

After the Unix sorting, use the CompressSorter.java to compress the posting records into binary record file. The CompressSorter has two main methods: sort, writeOutLexicon.

CompressSorter --------- void sort(Lexicon lexicon,
                |             String input,
                |             String indexOutput,
                |             int chunkSize)
                |------- void writeOutLexicon(Lexicon lexicon, String lexiconOutput)

For sorting, the CompressSorter uses a compress method named "simple9" to compress the records. To know how simple9 comression works, please refer to literatures.

Simple9 --------- int selectModel(int[] input, int index)
         |------- int encodeOneBatch(int[] input, int index, int model)
         |------- List<Integer> decodeOneNumber(int num)
         |------- List<Integer> encodeChunk(int[] input)
         |------- List<Integer> decodeChunk(List<Integer> nums)
         |------- void encode(int[] docidArray,
                              int[] frequencyArray,
                              int chunkSize,
                              List<MetaData> metaDataList,
                              List<Integer> invertedIndexList)

The "selectModel" is defined to select the proper model for converting the next batch of number in an array.
The "encodeOneBatch" method defines how to convert one batch of numbers in an array.
The "decodeOneNumber" method defines how to decode one integer number into a list of integer numbers.
The "encodeChunk" method defines how to convert an array of number into a list of numbers.
The "decodeChunk" method defines how to decode one chunk of number.
The "encode" method defines how to convert the docid records and the coresponding frequency records to metadata list and inverted index list.

The CompressSort.java uses the Simple9 methods the compress the invertedIndexList into chunks of integers. It also extracts the last docid and the total lengthes of docid records and frequency records of each chunk, and stores them into the data structure "MetaData".

MetaData --------- int lastDocid
           |------- int docidLength
           |------- int freqnencyLength

Finally, the MetaDada together with the compressed chunks and the total number of the MeataDadas are stored in the "indexOutput" file as pieces of invertedIndexLists. The starting byte of each invertedIndexList(index pointer) is recorded and together with the keyword stored in the lexicon datastructure and output into disk. For 50 dataset, the lexicon is about 90M, and the "indexOutput" file is about 1.4G. To search for the record in the "indexOutput" file, we can use the seek() method of the java RandomAccessFile class. It takes constant time to get the position in the file.

The lexicon record stores the index pointer, the length of the invertedIndexList, and the total occurrency of the pages that contains the word.

LexiconRecord --------- long index
               |------- int length
               |------- int total

The lexicon use a TreeMap structure. In this way, the words could be sorted and takes O(log(n)) time to search.

Lexicon --------- TreeMap<String, LexiconRecord> map
         |
         |------- void add(String keyword, LexiconRecord lexiconRecord)
         |------- void addFrequency(String keyword)
         |------- long getIndex(String keyword)
         |------- boolean containsKey(String keyword)
         |------- LexiconRecord getLexiconRecord(String keyword)

The size of the lexicon will not keep growing after reach a certain size, because the number of words will be close to the total number of vocabulary of English. It will be one or at most several megabyte. Therefore, when starts the search engine, the lexicon could be load into the memory all at once.

The last step is to starts the search engine.
The "SearchEngine.java" class is for searching the urls based on the inputed keywords. It has several components.


SearchEngine --------- String invertedIndexListSource
              |------- LFUCache<String, InvertedIndexListItem> lfuCache
              |------- Map<String, LexiconRecord> lexicon
              |------- List<UrlRecord> urlTable
              |------- UrlTableStatistics urlTableStatistics
              |------- ArrayList<InvertedIndexListItem> searchPool
              |------- SearchResultSet searchResultSet
              |
              |------- void loadLexicon(String lexiconSource)
              |------- void loadUrlTable(String urlTableSource)
              |------- void setInvertedIndexListSource(String invertedIndexListSource)
              |------- Set<String> extractKeyWords(String query)
              |------- void openList(RandomAccessFile raf, String keyword)
              |------- void addInvertedListsIntoSearchPool(RandomAccessFile raf,
              |                                            Set<String> keywords)
              |------- int nextGEQ(InvertedIndexListItem invertedIndexListItem, int docid)
              |------- void loadDataChunk(InvertedIndexListItem invertedIndexListItem, int i)
              |------- int getFreq(InvertedIndexListItem invertedIndexListItem, int docid)
              |------- double bm25(String keyword, int docid, int fdt)
              |------- void search(String query)
              |------- void conjunctiveSearch(Set<String> keywords)
              |------- void disjunctiveSearch(Set<String> keywords)
              |------- void clean()
              |------- void start(String lexiconSource, String urlTableSource,
                                                        String invertedIndexListSource)


First, the search engine has a lexicon structure to store the lexicon data. This lexicon will be loaded from the disk once the search engine starts. It take about 5 ~ 10 seconds.

Second, the search engine has a urlTable structure to store the url table. This uel table will also be loaded from the disk once the search engine starts. It take about 10 ~ 15 seconds.

Third, the total number of docids and the average length of the pages are calculated and loaded into the UrlTableStatistics class.

UrlTableStatistics --------- int count
                    |------- long averageLength

Fourth, the search engine has a cache for storing the invertedIndexLists read from the disk. The cache uses the least frequent use method to store the lists. The "LFUCache.java" class is adoped from Sergio Bossa and modified a little bit to fit the program. The cache could runs in *amortized* O(1) time complexity for Querying and updating. The capacity of the cache could be set. If the cache is full, the least frequent used records are cleanned from the cache. The eviction factor(percentage of the total cache to be cleanned) can be set by inputing number.

Once user inputs a or mutiple keywords to search query. The engine will first check whether to use conjunctive search(all keywords must appear at least once) or disjunctive search(at least one keyword must appear at least once).
If use the conjunctive method to search,
first the inverted index lists of the keywords will be loaded from the invertedIndexList file on the disk into the searchPool. The docids that contain all the keywords will be screened out. For each of the docid in the result, a significance value will be calculated using the BM25 formula. All the calculated results together with the urls are printed out.

InvertedIndexListItem --------- String keyword
                       |------- int total
                       |------- List<MetaData> metaDataList
                       |------- List<List<Integer>> docidChunks
                       |------- List<List<Integer>> frequencyChunks
                       |------- CurrentChunk currentChunk

CurrentChunk --------- int index
              |------- List<Integer> docidChunk
              |------- List<Integer> freqencyChunk
              |
              |------- int getFreqency(int docid)
              |------- int findDocPos(int docid)

If use the disjunctive method,
all the docids containing at least one keyword will be found out, and each docid will calculate the BM25 score, The other parts are same as conjuncted search.

The return page number should also be feeded in when starting the search engine.

To use the search engine:
1. Starts the SearchEngine program and waits for the data to be loaded into memory. It takes about 10 - 15 seconds.
2. Inputs the keywords for searching. The default method for searching is conjunctive search. If wants to use disjunctive search, add one or more "or" in the search keywords.
For most of the query, the search engine will show the searched result immediately. For some very common words such as "the" and "about", it takes about 2 second to show the result.

Problems:
1. I filtered out the words with less than 3 characters and more than 12 characters. So if search words within these lengths, it will not work.
2. I didn't implement the web portal for the user interface.
3. Because the limit of memory size, it doesn't work for too many dataset. Because the size of the urlTale has a linear relation to the dataset number.