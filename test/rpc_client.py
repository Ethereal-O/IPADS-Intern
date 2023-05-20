from __future__ import print_function

import logging

import grpc
import rpc_pb2 as rpc_pb2
import rpc_pb2_grpc as rpc_pb2_grpc
import time
EDGE_SERVER = "localhost:50051"


class rpc_client:
    def __init__(self):
        self.channel = grpc.insecure_channel(EDGE_SERVER)
        self.tot=0

    def __del__(self):
        self.channel.close()

    def get_passenger_num(self, stop_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        a=time.time()
        response = stub.GetPassengerNum(rpc_pb2.GPNRequest(
            stationId=int(stop_num)))
        # print("tottime:")
        self.tot+=time.time()-a
        # print(time.time()-a)
        return int(response.passengerNum)

    def reduce_passenger_num(self, stop_num, reduce_num):
        stub = rpc_pb2_grpc.RPCStub(self.channel)
        response = stub.ReducePassengerNum(rpc_pb2.RPNRequest(
            stationId=int(stop_num), boarderNum=int(reduce_num)))
        return int(response.ok)


if __name__ == "__main__":
    from grpc.helper import helper
    rpc = rpc_client()

    for i in range(100000):
        rpc.get_passenger_num(1)
    print(rpc.tot/100)
    print(1000*helper.clock_tot/helper.num_tot)
    print(1000*(helper.clock_tot-helper.clock_tot_2-helper.clock_tot_3)/(helper.num_tot))
    # print(rpc.get_passenger_num(1))
