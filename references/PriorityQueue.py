import heapq

class PriorityQueue:
    def __init__(self):
        self._queue = []

    def push(self, item, priority):
        heapq.heappush(self._queue, (-priority, item))


    def pop(self):
        return heapq.heappop(self._queue)[-1]




pq = PriorityQueue()
pq.push("hahah", 5)
pq.push("lala", 2)
pq.push("kaka", 3)
pq.push("lalaaaa", 2)

print pq.pop()
print pq.pop()
print pq.pop()
print pq.pop()