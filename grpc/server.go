package grpc

import (
	"context"
	"fmt"
	"log"
	"net"

	pb "edgesystem/grpc/proto/stationpb"
	"edgesystem/simulate"

	grpclib "google.golang.org/grpc"
)

type server struct {
	stations []simulate.Station
	pb.UnimplementedStationPassengerNumServiceServer
}

func (s *server) GetPassengerNum(ctx context.Context, req *pb.GPNRequest) (*pb.GPNReply, error) {
	log.Printf("Received: %v", req.GetStationId())
	stationId := req.GetStationId()
	return &pb.GPNReply{PassengerNum: int32(s.stations[stationId].GetPassengers())}, nil
}

func (s *server) ReducePassengerNum(ctx context.Context, req *pb.RPNRequest) (*pb.RPNReply, error) {
	log.Printf("Received: %v", req.GetStationId())
	stationId := req.GetStationId()
	boarderNum := int(req.GetBoarderNum())
	s.stations[stationId].SetNumOfPassengers(s.stations[stationId].GetPassengers() - boarderNum)
	return &pb.RPNReply{Ok: 1}, nil
}

func RunGrpcServer(port uint16, stations []simulate.Station) {
	listen, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpclib.NewServer()
	pb.RegisterStationPassengerNumServiceServer(s, &server{
		stations: stations,
	})
	log.Printf("server listening at %v", listen.Addr())
	if err := s.Serve(listen); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
