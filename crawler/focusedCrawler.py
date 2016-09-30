from google import search
from lxml import html
import urllib2
import robotparser
import lxml
import heapq
import sys
import timeit
import os

# the heap data structure to store the url tuples.
class PriorityQueue:
    def __init__(self):
        self.queue = []

    def push(self, item):
        heapq.heappush(self.queue, (-item[0], item[1], item[2]))

    def pop(self):
        return heapq.heappop(self.queue)

    def size(self):
        return len(self.queue)

    def clear(self):
        self.queue = []

# define the crawler class
class FocusedCrawler:

    def __init__(self, timeout):
        self.timeout = timeout
        self.pq = PriorityQueue()
        self.crawled = 0
        self.visited = 0
        self.urlSet = set()
        self.urlRoots = {}
        self.urlRetrievedSet = set()

    # using google search engine to get the initial urls
    def googleSearch(self, keyword, stop = 10):
        s = set()
        count = 0
        PRIORITY = sys.maxint
        DEPTH = 0
        for url in search(keyword, stop = 2 * stop):
            rootUrl = self.getRootUrl(url)
            if not rootUrl in s and count < stop:
                s.add(rootUrl)
                self.urlSet.add(url)
                self.pq.push((PRIORITY, url, DEPTH))
                print "Add " + url
                count += 1


    # get the data of a url
    def getPage(self, url):
        try:
            data = urllib2.urlopen(url, timeout = self.timeout)
            print "Get page of: " + url
            return data.read()
        except:
            return ""

    # calculate the priority of a url,
    # on the basis of the score obtained from the page
    # where the url is crawled from.
    # if any keyword appears in the url, score will be added onto the original score.
    def getUrlPriority(self, url, priority, keywords):
        urlPriority = priority
        for keyword in keywords:
            if keyword in url:
                urlPriority += 10000
        return urlPriority

    # get the valid and non-duplicated urls in the page,
    # the number of urls to get can be set as parameter.
    def getUrls(self, page, url, number, depth, priority, keywords):
        count = 0
        html = lxml.html.fromstring(page)
        html.make_links_absolute(url)
        urls = html.xpath('//a')
        for u in urls:
            if count > number:
                break
            newUrl = u.get('href')
            newUrlRoot = self.getRootUrl(newUrl)

            if self.isValidUrl(newUrl) and newUrl not in self.urlSet and newUrl not in self.urlRetrievedSet and (not newUrlRoot in self.urlRoots or self.urlRoots[newUrlRoot] < number):
                urlPriority = self.getUrlPriority(newUrl, priority, keywords)
                self.urlSet.add(newUrl)
                self.pq.push((urlPriority, newUrl, depth + 1))
                print "Add " + newUrl
                count += 1
                if not newUrlRoot in self.urlRoots:
                    self.urlRoots[newUrlRoot] = 0
                self.urlRoots[newUrlRoot] += 1

    # get the root url of an url
    def getRootUrl(self, url):
        if '/' in url[url.index('.'):]:
            return url[:url.index('/', url.index('.'))]
        else:
            return url

    # read the content of robots.txt
    def readRobotTxt(self, url):
        rootUrl = self.getRootUrl(url)
        robotUrl = rootUrl + '/robots.txt'
        rp = robotparser.RobotFileParser()
        rp.set_url(robotUrl)

    # determine if the root url could be crawled
    def canFetchUrl(self, url):
        rootUrl = self.getRootUrl(url)
        robotUrl = rootUrl + '/robots.txt'
        rp = robotparser.RobotFileParser()
        rp.set_url(robotUrl)
        try:
            rp.read()
            if not rp.can_fetch("*", url):
                return False
        except:
            pass
        return True

    # exclude the invalid url
    def isValidUrl(self, url):
        if ".jpg" in url or ".pdf" in url or ".exe" in url or ".css" in url or ".js" in url or ".png" in url or ".svg" in url or ".pkg" in url or ".tar" in url or "cgi" in url or ".py" in url or ".zip" in url or ".chm" in url or ".asc" in url or ".msi" in url or "mailto:" in url:
            return False
        else:
            return True

    # calculate the priority score of a page
    # by multiplying the occurrence rate of each keyword
    def calculatePriority(self, page, keywords):
        try:
            content = lxml.html.fromstring(page.lower())
            count = 1
            for keyword in keywords:
                count *= 1 + content.text_content().count(keyword + " ") + content.text_content().count(" " + keyword) - content.text_content().count(" " + keyword + " ")
            return count
        except:
            return 1

    # combine keywords to one string
    def convertKeywordsToOne(self, keywords):
        result = ""
        for keyword in keywords:
            result += keyword + "+"
        return result[0: -1]

    # calculate the rate of crawled urls out of total visited urls
    def calculateSuccessRate(self):
        return float(self.crawled) / self.visited

    # main crawl function
    def crawl(self, outputFile, logFile, keywords, numberOfPages, numberUrlOfEachPage):
        start = timeit.default_timer()
        keywordsString = ""
        for keyword in keywords:
            keywordsString += keyword + " "
        print "\n========================="
        print "Key words to search: " + keywordsString
        print "Output file: " + outputFile
        print "Log file: " + logFile
        print "Total number of pages to crawl: " + str(numberOfPages)
        print "Maximum number of Urls to crawl from each page: " + str(numberUrlOfEachPage)
        print "=========================\n"

        self.pq.clear()
        self.urlSet.clear()
        self.crawled = 0
        self.googleSearch(self.convertKeywordsToOne(keywords))
        while self.crawled < numberOfPages and self.pq.size() != 0:
            tuple = self.pq.pop()
            url = tuple[1]
            depth = tuple[2]
            try:
                data = urllib2.urlopen(url, timeout = self.timeout)
                retrievedUrl = data.geturl()
                statusCode = data.getcode()
                info = data.info()
                dateTime = info["date"]
                type = info.subtype
                self.visited += 1
                print "Visit: " + str(self.visited) + " " + url + "  Status: " + str(statusCode)
                if statusCode == 200 and retrievedUrl not in self.urlRetrievedSet and type == "html" and self.canFetchUrl(url):
                    self.urlRetrievedSet.add(retrievedUrl)
                    page = data.read()
                    pageSize = len(page)
                    priority = self.calculatePriority(page, keywords)
                    if priority == 1:
                        try:
                            harvestRate = "%0.2f" % (self.calculateSuccessRate())
                            log = open(logFile, "a")
                            log.write("URL: " + url + "  Time: " + dateTime + "  Status: " + str(statusCode) + "  Irrelevant  Harvest rate: " + harvestRate + "\n")
                            log.close()
                        except:
                            pass
                    else:
                        self.write(outputFile, logFile, url, priority, depth + 1, pageSize, dateTime, statusCode, page)
                        self.getUrls(page, url, numberUrlOfEachPage, depth + 1, priority, keywords)
                else:
                    harvestRate = "%0.2f" % (self.calculateSuccessRate())
                    log = open(logFile, "a")
                    log.write("URL: " + url + "  Time: " + dateTime + "  Status: " + str(statusCode) + "  Harvest rate: " + harvestRate + "\n")
                    log.close()
            except urllib2.HTTPError, error_code:  # catch the error
                self.visited += 1
                print "Visit: " + str(self.visited) + " " + url + "  Status: " + str(error_code)
                try:
                    harvestRate = "%0.2f" % (self.calculateSuccessRate())
                    log = open(logFile, "a")
                    log.write("URL: " + url + "  Status: " + str(error_code) + "  Harvest rate: " + harvestRate + "\n")
                    log.close()
                except:
                    continue
            except:
                continue

        stop = timeit.default_timer()
        dif = int(stop - start)
        hour = dif / 3600
        dif = dif % 3600
        min = dif / 60
        sec = dif % 60
        totalTime = str(hour) + "h " + str(min) + "m " + str(sec) + "s"
        try:
            log = open(logFile, "a")
            log.write("\n==================================\n")
            log.write("Total pages visited: " + str(self.visited) + "\n")
            log.write("Total pages crawled: " + str(self.crawled) + "\n")
            log.write("Relevant pages: " + str(self.crawled) + "\n")
            harvestRate = "%0.2f" % (self.calculateSuccessRate())
            log.write("Harvest rate: " + harvestRate + "\n")
            log.write("Total output size: " + str(os.path.getsize(outputFile)) + "\n")
            log.write("Total running time: " + totalTime + "\n")
            log.write("==================================\n")
            log.close()
        except:
            pass

    # write the output to file
    def write(self, outputFile, logFile, url, priority, depth, size, dateTime, statusCode, page):
        try:
            f = open(outputFile, "a")
            f.write(page)
            f.write("\n")
            f.close()
            self.crawled += 1
            print "\n========================="
            print "Write to file: " + str(self.crawled) + " " + url
            print "Time: " + dateTime
            # if priority > 1:
            #     print("Relavent")
            # else:
            #     print("Irrelavent")
            print "Depth: " + str(depth)
            print "Page Size: " + str(size)
            harvestRate = "%0.2f" % (self.calculateSuccessRate())
            print "Harvest rate: " + harvestRate
            print "=========================\n"

            log = open(logFile, "a")
            log.write("URL: " + url + "  Time: " + dateTime + "  Page size: " + str(size) + "  Depth: " + str(depth) + "  Status: " + str(statusCode) + "  Relevant  Harvest rate: " + harvestRate + "\n")
            log.close()
        except:
            print "Can't write output"


if __name__ == '__main__':
    outputFile = sys.argv[1]
    logFile = sys.argv[2]
    numberOfPages = int(sys.argv[3])
    numberUrlOfEachPage = int(sys.argv[4])
    keywords = sys.argv[5:]

    crawler = FocusedCrawler(3)
    crawler.crawl(outputFile, logFile, keywords, numberOfPages, numberUrlOfEachPage)



