from concurrent import futures
import logging
import threading

import grpc
import rpc_pb2
import rpc_pb2_grpc


class rpcImpl(rpc_pb2_grpc.RPCServicer):
    def get_num(self, request, context):
        num = 60
        return rpc_pb2.RpcReply(num=str(num))


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
    logging.basicConfig()
    serve()
