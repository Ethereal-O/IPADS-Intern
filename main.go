package main

import (
	"edgesystem/grpc"
	"edgesystem/websocket"
	"edgesystem/simulate"
)

const (
	STATION_NUM = 5
	GRPC_SERVER_PORT = 50051
	WS_SERVER_URL = "ws://localhost:8080/ws"
)

var (
	mins = [STATION_NUM]int {0, 0, 0, 0, 0}
	maxs = [STATION_NUM]int {100, 100, 100, 100, 100}
	means = [STATION_NUM]int {10, 10, 10, 10, 10}
	stds = [STATION_NUM]int {15, 15, 15, 15, 15}
	stations = make([]simulate.Station, STATION_NUM)
)

func main() {
	for i := 0; i < STATION_NUM; i++ {
		stations[i] = *simulate.NewStation(i, maxs[i], mins[i], means[i], stds[i])
	}
	go grpc.RunGrpcServer(GRPC_SERVER_PORT, stations)
	go ws.RunWebsocketClient(WS_SERVER_URL, stations, 6)
	for i := 0; i < STATION_NUM; i++ {
		stations[i].Simulate(5)
	}
	select {}
}