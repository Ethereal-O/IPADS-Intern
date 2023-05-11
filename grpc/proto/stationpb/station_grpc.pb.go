// Code generated by protoc-gen-go-grpc. DO NOT EDIT.
// versions:
// - protoc-gen-go-grpc v1.2.0
// - protoc             v4.22.3
// source: grpc/proto/station.proto

package stationpb

import (
	context "context"
	grpc "google.golang.org/grpc"
	codes "google.golang.org/grpc/codes"
	status "google.golang.org/grpc/status"
)

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
// Requires gRPC-Go v1.32.0 or later.
const _ = grpc.SupportPackageIsVersion7

// StationPassengerNumServiceClient is the client API for StationPassengerNumService service.
//
// For semantics around ctx use and closing/ending streaming RPCs, please refer to https://pkg.go.dev/google.golang.org/grpc/?tab=doc#ClientConn.NewStream.
type StationPassengerNumServiceClient interface {
	GetPassengerNum(ctx context.Context, in *GPNRequest, opts ...grpc.CallOption) (*GPNReply, error)
	ReducePassengerNum(ctx context.Context, in *RPNRequest, opts ...grpc.CallOption) (*RPNReply, error)
}

type stationPassengerNumServiceClient struct {
	cc grpc.ClientConnInterface
}

func NewStationPassengerNumServiceClient(cc grpc.ClientConnInterface) StationPassengerNumServiceClient {
	return &stationPassengerNumServiceClient{cc}
}

func (c *stationPassengerNumServiceClient) GetPassengerNum(ctx context.Context, in *GPNRequest, opts ...grpc.CallOption) (*GPNReply, error) {
	out := new(GPNReply)
	err := c.cc.Invoke(ctx, "/station.StationPassengerNumService/GetPassengerNum", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *stationPassengerNumServiceClient) ReducePassengerNum(ctx context.Context, in *RPNRequest, opts ...grpc.CallOption) (*RPNReply, error) {
	out := new(RPNReply)
	err := c.cc.Invoke(ctx, "/station.StationPassengerNumService/ReducePassengerNum", in, out, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// StationPassengerNumServiceServer is the server API for StationPassengerNumService service.
// All implementations must embed UnimplementedStationPassengerNumServiceServer
// for forward compatibility
type StationPassengerNumServiceServer interface {
	GetPassengerNum(context.Context, *GPNRequest) (*GPNReply, error)
	ReducePassengerNum(context.Context, *RPNRequest) (*RPNReply, error)
	mustEmbedUnimplementedStationPassengerNumServiceServer()
}

// UnimplementedStationPassengerNumServiceServer must be embedded to have forward compatible implementations.
type UnimplementedStationPassengerNumServiceServer struct {
}

func (UnimplementedStationPassengerNumServiceServer) GetPassengerNum(context.Context, *GPNRequest) (*GPNReply, error) {
	return nil, status.Errorf(codes.Unimplemented, "method GetPassengerNum not implemented")
}
func (UnimplementedStationPassengerNumServiceServer) ReducePassengerNum(context.Context, *RPNRequest) (*RPNReply, error) {
	return nil, status.Errorf(codes.Unimplemented, "method ReducePassengerNum not implemented")
}
func (UnimplementedStationPassengerNumServiceServer) mustEmbedUnimplementedStationPassengerNumServiceServer() {
}

// UnsafeStationPassengerNumServiceServer may be embedded to opt out of forward compatibility for this service.
// Use of this interface is not recommended, as added methods to StationPassengerNumServiceServer will
// result in compilation errors.
type UnsafeStationPassengerNumServiceServer interface {
	mustEmbedUnimplementedStationPassengerNumServiceServer()
}

func RegisterStationPassengerNumServiceServer(s grpc.ServiceRegistrar, srv StationPassengerNumServiceServer) {
	s.RegisterService(&StationPassengerNumService_ServiceDesc, srv)
}

func _StationPassengerNumService_GetPassengerNum_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(GPNRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(StationPassengerNumServiceServer).GetPassengerNum(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/station.StationPassengerNumService/GetPassengerNum",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(StationPassengerNumServiceServer).GetPassengerNum(ctx, req.(*GPNRequest))
	}
	return interceptor(ctx, in, info, handler)
}

func _StationPassengerNumService_ReducePassengerNum_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(RPNRequest)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(StationPassengerNumServiceServer).ReducePassengerNum(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/station.StationPassengerNumService/ReducePassengerNum",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(StationPassengerNumServiceServer).ReducePassengerNum(ctx, req.(*RPNRequest))
	}
	return interceptor(ctx, in, info, handler)
}

// StationPassengerNumService_ServiceDesc is the grpc.ServiceDesc for StationPassengerNumService service.
// It's only intended for direct use with grpc.RegisterService,
// and not to be introspected or modified (even as a copy)
var StationPassengerNumService_ServiceDesc = grpc.ServiceDesc{
	ServiceName: "station.StationPassengerNumService",
	HandlerType: (*StationPassengerNumServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "GetPassengerNum",
			Handler:    _StationPassengerNumService_GetPassengerNum_Handler,
		},
		{
			MethodName: "ReducePassengerNum",
			Handler:    _StationPassengerNumService_ReducePassengerNum_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "grpc/proto/station.proto",
}