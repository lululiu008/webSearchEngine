################################################################
#
# Simple crawler
#
# 06/23/15, Jiwei
# Welcome test page: http://endof30.com
#
################################################################
import urllib2, sys
from bs4 import BeautifulSoup

# MaxCount to avoid infinit loop
MAXCOUNT = 200
# test page
thisurl = "http://endof30.com"


def getPage(url):
    # return page content
    return urllib2.urlopen(url).read()


def getNextUrl(page):
    # return the next url from the current page content
    if page.find('<a href="') == -1:
        return None, -1
    startPoint = page.find('<a href="') + 9
    endPoint = page.find('"', startPoint)
    return page[startPoint:endPoint], endPoint


def fetchUrls(page, mainSite):
    # return all urls in a single page
    urls = []
    while True:
        url, endPoint = getNextUrl(page)
        if url:
            if url[0] == '/':
                url = mainSite + url
            # Avoid run to other sites
            if url.find(mainSite) == 0 and url not in urls:
                urls.append(url)
            page = page[endPoint:]
        else:
            break
    return urls


# This is the function in which to extract the useful data
def fetchContent(page):
    soup = BeautifulSoup(page, "lxml")
    return soup.get_text(strip=True)


def crawlSite(siteUrl):
    # Initialize
    toCrawl = fetchUrls(getPage(siteUrl), siteUrl)
    crawled = []
    pageContent = []
    count = 0
    # Crawling while there are still urls in toCrawl list
    while toCrawl:
        site1 = toCrawl.pop()
        if site1 not in crawled:
            page = getPage(site1)
            pageContent.append(fetchContent(page))
            urlList = fetchUrls(page, siteUrl)
            # union(toCrawl, urlList)
            for url in urlList:
                if url not in toCrawl:
                    if url not in crawled:
                        toCrawl.append(url)
            crawled.append(site1)
        count += 1
        if count > MAXCOUNT:
            break
    return pageContent


if __name__ == '__main__':
    # the first argument should be the site url, eg. http://endof30.com
    # the second argument should be the output file name, eg. test.txt
    siteToCrawl = str(sys.argv[1])
    fileToWrite = str(sys.argv[2])
    with open(fileToWrite, 'w') as f:
        for item in crawlSite(siteToCrawl):
            f.write("%s\n" % item.encode('utf8'))


