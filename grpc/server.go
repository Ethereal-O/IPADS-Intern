package grpc

import (
	"context"
	"fmt"
	"log"
	"net"

	grpclib "google.golang.org/grpc"
	"edgesystem/simulate"
	pb "edgesystem/grpc/proto/stationpb"
)

type server struct {
	stations []simulate.Station
	pb.UnimplementedStationPassagerNumServiceServer
}

func (s *server) GetPassagerNum(ctx context.Context, req *pb.GPNRequest) (*pb.GPNReply, error) {
	log.Printf("Received: %v", req.GetStationId())
	stationId := req.GetStationId()
	return &pb.GPNReply{PassagerNum: int32(s.stations[stationId].GetPassagers())}, nil
}

func (s *server) ReducePassagerNum(ctx context.Context, req *pb.RPNRequest) (*pb.RPNReply, error) {
	log.Printf("Received: %v", req.GetStationId())
	stationId := req.GetStationId()
	boarderNum := int(req.GetBoarderNum())
	s.stations[stationId].SetNumOfPassagers(s.stations[stationId].GetPassagers() - boarderNum)
	return &pb.RPNReply{Ok: 1}, nil
}

func RunGrpcServer(port uint16, stations []simulate.Station) {
	listen, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpclib.NewServer()
	pb.RegisterStationPassagerNumServiceServer(s, &server{
		stations: stations,
	})
	log.Printf("server listening at %v", listen.Addr())
	if err := s.Serve(listen); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}
}
