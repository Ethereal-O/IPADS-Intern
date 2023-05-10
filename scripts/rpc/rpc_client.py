from __future__ import print_function

import logging

import grpc
import rpc.rpc_pb2 as rpc_pb2
import rpc.rpc_pb2_grpc as rpc_pb2_grpc
from config import config
EDGE_SERVER = config.EDGE_SERVER


class rpc_client:
    def __init__(self):
        self.channel = grpc.insecure_channel(EDGE_SERVER)

    def __del__(self):
        self.channel.close()

    def get_passenger_num(self, stop_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        response = stub.get_passenger_num(rpc_pb2.GPNRequest(
            station_id=int(stop_num)))
        return int(response.passenger_num)

    def reduce_passenger_num(self, stop_num, reduce_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        response = stub.reduce_passenger_num(rpc_pb2.RPNRequest(
            station_id=int(stop_num), boarder_num=int(reduce_num)))
        return int(response.ok)
