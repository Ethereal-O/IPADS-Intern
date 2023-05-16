import rospy
import time


class helpers:
    def __init__(self):
        self.clock_tot = 0
        self.num_tot = 0
        rospy.logwarn("[helper] helper started!")

    def __del__(self):
        rospy.logwarn("[clock] clock average interval %s" %
                      (self.clock_tot/self.num_tot))
        rospy.logwarn("[helper] helper ended!")

    def clocker(self, func):
        def new_func(*arg, **kwargs):
            clock_start = time.time()
            val = func(*arg, **kwargs)
            clock_end = time.time()
            self.clock_tot += clock_end-clock_start
            self.num_tot += 1
            rospy.logwarn("[clock] clock start at %s, end at %s, interval %s" %
                          (clock_start, clock_end, clock_end-clock_start))
            return val
        return new_func

    def log_printer(self, func):
        def new_func(*arg, **kwargs):
            val = func(*arg, **kwargs)
            rospy.logwarn("[%s] parameters: %s, return %s" %
                          (func.__name__, arg, val))
            return val
        return new_func


helper = helpers()
