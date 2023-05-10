from __future__ import print_function

import logging

import grpc
import rpc_pb2
import rpc_pb2_grpc
EDGE_SERVER = "localhost:50051"


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


if __name__ == "__main__":
    rpc = rpc_client()
    print(rpc.get_passenger_num())
