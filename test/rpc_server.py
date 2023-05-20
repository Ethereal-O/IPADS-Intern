from concurrent import futures
import logging
import threading

import grpc
import rpc_pb2
import rpc_pb2_grpc
import time


class rpcImpl(rpc_pb2_grpc.RPCServicer):
    def GetPassengerNum(self, request, context):
        num = 60
        return rpc_pb2.GPNReply(passengerNum=num)

    def ReducePassengerNum(self, request, context):
        # print("get report")
        return rpc_pb2.RPNReply(ok=1)


def real_serve():
    port = '50051'
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    rpc_pb2_grpc.add_RPCServicer_to_server(rpcImpl(), server)
    server.add_insecure_port('[::]:' + port)
    server.start()
    print("Server started, listening on " + port)
    server.wait_for_termination()


def serve():
    threading.Thread(target=real_serve).start()


if __name__ == '__main__':
    # from grpc.helper import helper
    logging.basicConfig()
    serve()
    
