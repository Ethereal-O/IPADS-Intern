from __future__ import print_function

import logging

import grpc
import rpc.rpc_pb2 as rpc_pb2
import rpc.rpc_pb2_grpc as rpc_pb2_grpc
from config import config
import rospy
from helper.helper import helper
EDGE_SERVER = config.EDGE_SERVER


class rpc_client:
    def __init__(self):
        self.channel = grpc.insecure_channel(EDGE_SERVER)
        self.stub = rpc_pb2_grpc.RPCStub(self.channel)

    def __del__(self):
        self.channel.close()

    # @helper.clocker
    def get_passenger_num(self, stop_num):
        response = self.stub.GetPassengerNum(rpc_pb2.GPNRequest(
            stationId=int(stop_num)))
        return int(response.passengerNum)

    # @helper.clocker
    def reduce_passenger_num(self, stop_num, reduce_num):
        response = self.stub.ReducePassengerNum(rpc_pb2.RPNRequest(
            stationId=int(stop_num), boarderNum=int(reduce_num)))
        return int(response.ok)
