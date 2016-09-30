from google import search
from lxml import html
import urllib2
import robotparser
import lxml
import heapq
import sys
import time

import timeit

start = timeit.default_timer()

#Your statements here
time.sleep(2)
stop = timeit.default_timer()

dif = int(stop - start)
hour = dif / 3600
dif = dif % 3600
min = dif / 60
sec = dif % 60
print str(hour) + "h " + str(min) + "m " + str(sec) + "s\n"
