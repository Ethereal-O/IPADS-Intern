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
        response = stub.get_num(rpc_pb2.RpcRequest(
            name='car', stop_num=str(stop_num)))
        return int(response.num)
