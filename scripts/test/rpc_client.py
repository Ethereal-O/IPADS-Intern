from __future__ import print_function

import logging

import grpc
import rpc_pb2 as rpc_pb2
import rpc_pb2_grpc as rpc_pb2_grpc
from config import config
EDGE_SERVER = config.EDGE_SERVER


class rpc_client:
    def __init__(self):
        self.channel = grpc.insecure_channel(EDGE_SERVER)

    def __del__(self):
        self.channel.close()

    def get_passenger_num(self, stop_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        response = stub.GetPassengerNum(rpc_pb2.GPNRequest(
            stationId=int(stop_num)))
        return int(response.passengerNum)

    def reduce_passenger_num(self, stop_num, reduce_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        response = stub.ReducePassengerNum(rpc_pb2.RPNRequest(
            stationId=int(stop_num), boarderNum=int(reduce_num)))
        return int(response.ok)


if __name__ == "__main__":
    rpc = rpc_client()
    print(rpc.get_passenger_num())
