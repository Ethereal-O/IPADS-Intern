#!/usr/bin/env python
# coding=utf-8
import rospy
from controller import controller


rospy.init_node("car")
controller.start()
rospy.spin()
