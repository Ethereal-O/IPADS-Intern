import rospy
import time


class helpers:
    def __init__(self):
        rospy.logwarn("helper started!")

    def clocker(self, func):
        def new_func(*arg, **kwargs):
            clock_start = time.time()
            val = func(*arg, **kwargs)
            clock_end = time.time()
            rospy.logwarn("clock start at %s, end at %s, interval %s" %
                          (clock_start, clock_end, clock_end-clock_start))
            return val
        return new_func

    def log_printer(self, func):
        def new_func(_, val):
            rospy.logwarn(val)
            return func(_, val)
        return new_func


helper = helpers()
