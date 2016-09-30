The submitted files include:
The main scripts "focusedCrawler.py" and "bfsCrawler.py";
The log file of searching "emporer penguin" using focused crawling: "emporerPenguinFocusedCrawlerLog.txt";
The log file of searching "emporer penguin" using BFS crawling: "emporerPenguinBfsCrawlerLog.txt";
The log file of searching "duke flatbush" using focused crawling: "dukeFlatbushFocusedCrawlerLog.txt";
The log file of searching "duke flatbush" using BFS crawling: "dukeFlatbushBfsdCrawlerLog.txt";

To run the crawler, at the terminal run the command in this format:
python crawlerVersion.py outputFile outputLogFile totalNumberToCrawl maximumNumberOfUrlToCrawlFromEachWebsite keywordsSeperatedByWhiteSpace
For example:
python focusedCrawler.py dukeFlatbushFocusedCrawlerOutput.xml dukeFlatbushFocusedCrawlerLog.txt 1000 40 duke flatbush
python bfsCrawler.py dukeFlatbushBfsCrawlerOutput.xml dukeFlatbushBfsCrawlerLog.txt 1000 40 duke flatbush